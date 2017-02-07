package org.sitenv.service.ccda.smartscorecard.controller;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.sitenv.ccdaparsing.model.CCDAXmlSnippet;
import org.sitenv.service.ccda.smartscorecard.model.CCDAScoreCardRubrics;
import org.sitenv.service.ccda.smartscorecard.model.Category;
import org.sitenv.service.ccda.smartscorecard.model.ReferenceError;
import org.sitenv.service.ccda.smartscorecard.model.ReferenceResult;
import org.sitenv.service.ccda.smartscorecard.model.ReferenceTypes.ReferenceInstanceType;
import org.sitenv.service.ccda.smartscorecard.model.ResponseTO;
import org.sitenv.service.ccda.smartscorecard.model.Results;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationConstants;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationUtil;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lowagie.text.DocumentException;

@RestController
public class SaveReportController {

	public static final String SAVE_REPORT_CHARSET_NAME = "UTF8";

	/**
	 * Converts received JSON to a ResponseTO POJO (via method signature
	 * automagically), converts the ResponseTO to a cleaned (parsable) HTML
	 * report including relevant data, converts the HTML to a PDF report, and,
	 * finally, streams the data for consumption
	 * 
	 * @param jsonReportData
	 *            JSON which resembles ResponseTO
	 * @param response
	 *            used to stream the PDF
	 */
	@RequestMapping(value = "/savescorecardservice", method = RequestMethod.POST)
	public void savescorecardservice(@RequestBody ResponseTO jsonReportData,
			HttpServletResponse response) {

		convertHTMLToPDFAndStreamToOutput(
				ensureLogicalParseTreeInHTML(convertReportToHTML(jsonReportData)),
				response);

	}

	/**
	 * A single service to handle a pure back-end implementation of the
	 * scorecard which streams back a PDF report
	 * 
	 * @param ccdaFile
	 *            The C-CDA XML file intended to be scored
	 */
	@RequestMapping(value = "/savescorecardservicebackend", method = RequestMethod.POST)
	public void savescorecardservicebackend(
			@RequestParam("ccdaFile") MultipartFile ccdaFile,
			HttpServletResponse response) {

		ResponseTO pojoResponse = callCcdascorecardservice(ccdaFile);
		if (pojoResponse == null) {
			pojoResponse = new ResponseTO();
			pojoResponse.setResults(null);
			pojoResponse.setSuccess(false);
			pojoResponse
					.setErrorMessage(ApplicationConstants.ErrorMessages.NULL_RESULT_ON_SAVESCORECARDSERVICEBACKEND_CALL);
		} else {
			if (!ApplicationUtil.isEmpty(ccdaFile.getOriginalFilename())
					&& ccdaFile.getOriginalFilename().contains(".")) {
				pojoResponse.setFilename(ccdaFile.getOriginalFilename());
			} else if (!ApplicationUtil.isEmpty(ccdaFile.getName())
					&& ccdaFile.getName().contains(".")) {
				pojoResponse.setFilename(ccdaFile.getName());
			}
			// otherwise it uses the name given by ccdascorecardservice
		}
		convertHTMLToPDFAndStreamToOutput(
				ensureLogicalParseTreeInHTML(convertReportToHTML(pojoResponse)),
				response);

	}

	protected static ResponseTO callCcdascorecardservice(MultipartFile ccdaFile) {
		ResponseTO pojoResponse = null;

		LinkedMultiValueMap<String, Object> requestMap = new LinkedMultiValueMap<>();
		FileOutputStream out = null;
		File tempFile = null;
		try {
			final String tempCcdaFileName = "ccdaFile";
			tempFile = File.createTempFile(tempCcdaFileName, "xml");
			out = new FileOutputStream(tempFile);
			IOUtils.copy(ccdaFile.getInputStream(), out);
			requestMap.add(tempCcdaFileName, new FileSystemResource(tempFile));

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.MULTIPART_FORM_DATA);
			HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(
					requestMap, headers);

			FormHttpMessageConverter formConverter = new FormHttpMessageConverter();
			formConverter.setCharset(Charset.forName(SAVE_REPORT_CHARSET_NAME));
			RestTemplate restTemplate = new RestTemplate();
			restTemplate.getMessageConverters().add(formConverter);
			restTemplate.getMessageConverters().add(
					new MappingJackson2HttpMessageConverter());

			pojoResponse = restTemplate.postForObject(
					ApplicationConstants.CCDASCORECARDSERVICE_URL,
					requestEntity, ResponseTO.class);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (out != null) {
				try {
					out.flush();
					out.close();
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
			if (tempFile != null && tempFile.isFile()) {
				tempFile.delete();
			}
		}

		return pojoResponse;
	}

	/**
	 * Parses (POJO ResponseTO converted) JSON and builds the results into an
	 * HTML report
	 * 
	 * @param report
	 *            the ResponseTO report intended to be converted to HTML
	 * @return the converted HTML report as a String
	 */
	protected static String convertReportToHTML(ResponseTO report) {
		StringBuffer sb = new StringBuffer();
		appendOpeningHtml(sb);

		if (report == null) {
			report = new ResponseTO();
			report.setResults(null);
			report.setSuccess(false);
			report.setErrorMessage(ApplicationConstants.ErrorMessages.GENERIC_WITH_CONTACT);
			appendErrorMessageFromReport(sb, report,
					ApplicationConstants.ErrorMessages.RESULTS_ARE_NULL);
		} else {
			// report != null
			if (report.getResults() != null) {
				if (report.isSuccess()) {
					Results results = report.getResults();
					List<Category> categories = results.getCategoryList();
					List<ReferenceResult> referenceResults = report
							.getReferenceResults();

					appendHeader(sb, report, results);
					appendHorizontalRuleWithBreaks(sb);

					appendTopLevelResults(sb, results, categories,
							referenceResults);
					appendHorizontalRuleWithBreaks(sb);

					appendDetailedResults(sb, categories, referenceResults);
				}
			} else {
				// report.getResults() == null
				if (!report.isSuccess()) {
					appendErrorMessageFromReport(sb, report,
							ApplicationConstants.ErrorMessages.IS_SUCCESS_FALSE);
				} else {
					appendErrorMessageFromReport(sb, report);
				}
			}
		}

		appendClosingHtml(sb);
		return sb.toString();
	}

	private static void appendOpeningHtml(StringBuffer sb) {
		sb.append("<!DOCTYPE html>");
		sb.append("<html>");
		sb.append("<head>");
		sb.append("<title>SITE C-CDA Scorecard Report</title>");
		sb.append("</head>");
		sb.append("<body style='font-family: \"Helvetica Neue\",Helvetica,Arial,sans-serif;'>");
	}

	private static void appendHeader(StringBuffer sb, ResponseTO report,
			Results results) {
		sb.append("<header id='topOfScorecard'>");
		final String logoPath = "https://devportal.sitenv.org/site-portal-responsivebootstrap-theme/images/site/site-header.png";
		sb.append("<center>");
		sb.append("<img src='" + logoPath + "' alt='SITE logo' width='100%'>");

		sb.append("<br />");
		appendHorizontalRuleWithBreaks(sb);

		sb.append("<h1>" + "C-CDA " + results.getDocType() + " Scorecard For: ");
		sb.append("<h2>" + report.getFilename() + "</h2>");
		sb.append("</center>");
		sb.append("</header>");
	}

	private static void appendTopLevelResults(StringBuffer sb, Results results,
			List<Category> categories, List<ReferenceResult> referenceResults) {

		sb.append("<h3>Scorecard Grade: " + results.getFinalGrade() + "</h3>");
		sb.append("<ul><li>");
		sb.append("<p>Your document scored a " + "<b>"
				+ results.getFinalGrade() + "</b>"
				+ " compared to an industry average of " + "<b>"
				+ results.getIndustryAverageGrade() + "</b>" + ".</p>");
		sb.append("</ul></li>");

		sb.append("<h3>Scorecard Score: " + results.getFinalNumericalGrade()
				+ "</h3>");
		sb.append("<ul><li>");
		sb.append("<p>Your document scored " + "<b>"
				+ +results.getFinalNumericalGrade() + "</b>" + " out of "
				+ "<b>" + " 100 " + "</b>" + " total possible points.</p>");
		sb.append("</ul></li>");

		boolean isSingular = results.getNumberOfIssues() == 1;
		appendSummaryRow(sb, results.getNumberOfIssues(), "Scorecard Issues",
				null, isSingular ? "Scorecard Issue" : "Scorecard Issues",
				isSingular);
		sb.append("</ul></li>");

		String messageSuffix = null;
		if (ApplicationUtil.isEmpty(referenceResults)) {
			for (ReferenceInstanceType refType : ReferenceInstanceType.values()) {
				if (refType == ReferenceInstanceType.CERTIFICATION_2015) {
					messageSuffix = "results";
				}
				appendSummaryRow(sb, 0, refType.getTypePrettyName(),
						messageSuffix, refType.getTypePrettyName(), false);
				sb.append("</ul></li>");
			}
		} else {
			for (ReferenceResult refResult : referenceResults) {
				int refErrorCount = refResult.getTotalErrorCount();
				isSingular = refErrorCount == 1;
				String refTypeName = refResult.getType().getTypePrettyName();
				String messageSubject = "";
				if (refResult.getType() == ReferenceInstanceType.IG_CONFORMANCE) {
					messageSubject = isSingular ? refTypeName.substring(0,
							refTypeName.length() - 1) : refTypeName;
				} else if (refResult.getType() == ReferenceInstanceType.CERTIFICATION_2015) {
					messageSuffix = isSingular ? "result" : "results";
					messageSubject = refTypeName;
				}
				appendSummaryRow(sb, refErrorCount, refTypeName, messageSuffix,
						messageSubject, isSingular);
				sb.append("</ul></li>");
			}
		}

		appendHorizontalRuleWithBreaks(sb);

		sb.append("<span id='heatMap'>" + "</span>");
		for (Category curCategory : categories) {
			sb.append("<h3>" + "<a href='#" + curCategory.getCategoryName()
					+ "-category" + "'>" + curCategory.getCategoryName()
					+ "</a>" + "</h3>");

			sb.append("<ul>");
			if (curCategory.getCategoryGrade() != null) {
				sb.append("<li>" + "Section Grade: " + "<b>"
						+ curCategory.getCategoryGrade() + "</b>" + "</li>"
						+ "<li>" + "Number of Issues: " + "<b>"
						+ curCategory.getNumberOfIssues() + "</b>" + "</li>");
			} else {
				sb.append("<li>"
						+ "This category was not scored as it "
						+ "<b>");  
				if(curCategory.isFailingConformance() && !curCategory.isCertificationFeedback()) {
					sb.append("contains Conformance Errors");
				} else if(curCategory.isCertificationFeedback()) {
					sb.append("contains Certification Feedback");
				} else if(curCategory.isNullFlavorNI()) {
					sb.append("is an empty section");
				}
				sb.append("</b>" + "</li>");
				
				
			}
			sb.append("</ul></li>");
		}

	}

	private static void appendSummaryRow(StringBuffer sb, int result,
			String header, String messageSuffix, String messageSubject,
			boolean isSingular) {
		sb.append("<h3>"
				+ header
				+ ": "
				+ ("Scorecard Issues".equals(header) || result < 1 ? result
						: ("<a href=\"#" + header + "-category\">" + result + "</a>"))
				+ "</h3>");
		sb.append("<ul><li>");
		sb.append("<p>There " + (isSingular ? "is" : "are") + " " + "<b>"
				+ result + "</b>" + " " + messageSubject
				+ (messageSuffix != null ? " " + messageSuffix : "")
				+ " in your document.</p>");
	}

	private static void appendDetailedResults(StringBuffer sb,
			List<Category> categories, List<ReferenceResult> referenceResults) {

		sb.append("<h2>" + "Detailed Results" + "</h2>");

		if (!ApplicationUtil.isEmpty(referenceResults)) {
			for (ReferenceResult curRefInstance : referenceResults) {

				ReferenceInstanceType refType = curRefInstance.getType();
				String refTypeName = refType.getTypePrettyName();
				sb.append("<h3 id=\"" + refTypeName + "-category\">"
						+ refTypeName + "</h3>");

				sb.append("<ul>"); // START curRefInstance ul
				sb.append("<li>"
						+ "Number of "
						+ (refType == ReferenceInstanceType.CERTIFICATION_2015 ? "Results:"
								: "Errors:") + " "
						+ curRefInstance.getTotalErrorCount() + "</li>");

				sb.append("<ol>"); // START reference errors ol

				if (curRefInstance.getTotalErrorCount() > 0) {
					for (ReferenceError curRefError : curRefInstance
							.getReferenceErrors()) {

						sb.append("<li>"
								+ (refType == ReferenceInstanceType.CERTIFICATION_2015 ? "Feedback:"
										: "Error:") + " "
								+ curRefError.getDescription() + "</li>");

						sb.append("<ul>"); // START ul within the curRefError
						if (!ApplicationUtil.isEmpty(curRefError
								.getSectionName())) {
							sb.append("<li>" + "Related Section: "
									+ curRefError.getSectionName() + "</li>");
						}
						sb.append("<li>"
								+ "Document Line Number (approximate): "
								+ curRefError.getDocumentLineNumber() + "</li>");
						sb.append("<li>"
								+ "xPath: "
								+ "<xmp style='font-family: Consolas, monaco, monospace;'>"
								+ curRefError.getxPath() + "</xmp>" + "</li>");
						sb.append("</ul>"); // END ul within the curRefError
					}
				}
				sb.append("</ol>"); // END reference errors ol
				sb.append("</ul>"); // END curRefInstance ul
				appendBackToTopWithBreaks(sb);

			}
		}

		for (Category curCategory : categories) {
			sb.append("<h3 id='" + curCategory.getCategoryName() + "-category"
					+ "'>" + curCategory.getCategoryName() + "</h3>");

			if (curCategory.getCategoryNumericalScore() != -1) {
				sb.append("<ul>"); // START curCategory ul
				sb.append("<li>" + "Section Grade: "
						+ curCategory.getCategoryGrade() + "</li>" + "<li>"
						+ "Number of Issues: "
						+ curCategory.getNumberOfIssues() + "</li>");

				if (curCategory.getNumberOfIssues() > 0) {
					sb.append("<ol>"); // START rules ol
					for (CCDAScoreCardRubrics curRubric : curCategory
							.getCategoryRubrics()) {
						if (curRubric.getNumberOfIssues() > 0) {
							sb.append("<li>" + "Rule: " + curRubric.getRule()
									+ "</li>");
							if (curRubric.getDescription() != null) {
								sb.append("<ul>" + "<li>" + "Description"
										+ "</li>" + "<ul>" + "<li>"
										+ curRubric.getDescription() + "</li>"
										+ "</ul>" + "</ul>");
								sb.append("<br />");

								sb.append("<ol>"); // START snippets ol
								for (CCDAXmlSnippet curSnippet : curRubric
										.getIssuesList()) {
									sb.append("<li>"
											+ "XML at line number "
											+ curSnippet.getLineNumber()
											+ "</li>"
											+ "<br /><xmp style='font-family: Consolas, monaco, monospace;'>"
											+ curSnippet.getXmlString()
											+ "</xmp><br /><br />");
								}
								sb.append("</ol>"); // END snippets ol
							}
						} else {
							// don't display rules without occurrences
							sb.append("</ol>");
						}
					}
					sb.append("</ol>"); // END rules ol
				}
				sb.append("</ul>"); // END curCategory ul
			}
			appendBackToTopWithBreaks(sb);
		}

	}

	private static void appendClosingHtml(StringBuffer sb) {
		sb.append("</body>");
		sb.append("</html>");
	}

	private static void appendHorizontalRuleWithBreaks(StringBuffer sb) {
		sb.append("<br />");
		sb.append("<hr />");
		sb.append("<br />");
	}

	private static void appendBackToTopWithBreaks(StringBuffer sb) {
		// sb.append("<br />");
		sb.append("<a href='#topOfScorecard'>Back to Top</a>");
		// sb.append("<br />");
		// A PDF conversion bug is not processing this valid HTML so commenting
		// out until time to address
		// sb.append("<a href='#heatMap'>Back to Section List</a>");
		sb.append("<br />");
		// sb.append("<br />");
	}

	private static void appendErrorMessage(StringBuffer sb, String errorMessage) {
		sb.append("<h2 style='color:red; background-color: #ffe6e6'>");
		sb.append(errorMessage);
		sb.append("</h2>");
		sb.append("<p>" + ApplicationConstants.ErrorMessages.CONTACT + "</p>");
	}

	private static void appendGenericErrorMessage(StringBuffer sb) {
		sb.append("<p>"
				+ ApplicationConstants.ErrorMessages.JSON_TO_JAVA_JACKSON
				+ "<br />" + ApplicationConstants.ErrorMessages.CONTACT
				+ "</p>");
	}

	private static void appendErrorMessageFromReport(StringBuffer sb,
			ResponseTO report) {
		appendErrorMessageFromReport(sb, report, null);
	}

	private static void appendErrorMessageFromReport(StringBuffer sb,
			ResponseTO report, String extraMessage) {
		if (report.getErrorMessage() != null
				&& !report.getErrorMessage().isEmpty()) {
			appendErrorMessage(sb, report.getErrorMessage());
		} else {
			appendGenericErrorMessage(sb);
		}
		if (extraMessage != null && !extraMessage.isEmpty()) {
			sb.append("<p>" + extraMessage + "</p>");
		}
	}

	protected static String ensureLogicalParseTreeInHTML(String htmlReport) {
		org.jsoup.nodes.Document doc = Jsoup.parse(htmlReport);
		String cleanHtmlReport = doc.toString();
		return cleanHtmlReport;
	}

	protected static void convertHTMLToPDFAndStreamToOutput(
			String cleanHtmlReport, HttpServletResponse response) {

		OutputStream out = null;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			factory.setNamespaceAware(false);
			factory.setValidating(false);

			DocumentBuilder builder = factory.newDocumentBuilder();
			Document refineddoc = builder.parse(new ByteArrayInputStream(
					cleanHtmlReport.getBytes("UTF-8")));

			ITextRenderer renderer = new ITextRenderer();
			renderer.setDocument(refineddoc, null);
			renderer.layout();

			response.setContentType("application/pdf");
			response.setHeader("Content-disposition", "attachment; filename="
					+ "scorecardReport.pdf");
			response.setHeader("max-age=3600", "must-revalidate");
			response.addCookie(new Cookie("fileDownload=true", "path=/"));
			out = response.getOutputStream();
			renderer.createPDF(out);
		} catch (ParserConfigurationException pcE) {
			pcE.printStackTrace();
		} catch (SAXException saxE) {
			saxE.printStackTrace();
		} catch (DocumentException docE) {
			docE.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (out != null) {
				try {
					out.flush();
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			ApplicationUtil.debugLog("cleanHtmlReport", cleanHtmlReport);
		}
	}

	/**
	 * Converts JSON to ResponseTO Java Object using The Jackson API
	 * 
	 * @param jsonReportData
	 *            JSON which resembles ResponseTO
	 * @return converted ResponseTO POJO
	 */
	protected static ResponseTO convertJsonToPojo(String jsonReportData) {
		ObjectMapper mapper = new ObjectMapper();
		ResponseTO pojo = null;
		try {
			pojo = mapper.readValue(jsonReportData, ResponseTO.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return pojo;
	}

}
