package org.sitenv.service.ccda.smartscorecard.controller;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
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
	private static final int CONFORMANCE_ERROR_INDEX = 0;
	private static final int CERTIFICATION_FEEDBACK_INDEX = 1;

	/**
	 * Converts received JSON to a ResponseTO POJO (via method signature
	 * automagically), converts the ResponseTO to a cleaned (parsable) HTML
	 * report including relevant data, converts the HTML to a PDF report, and,
	 * finally, streams the data for consumption. 
	 * This is intended to be called from a frontend which has already collected the JSON results.
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
				ensureLogicalParseTreeInHTML(convertReportToHTML(jsonReportData, SaveReportType.MATCH_UI)),
				response);
	}

	/**
	 * A single service to handle a pure back-end implementation of the
	 * scorecard which streams back a PDF report. 
	 * This does not require the completed JSON up-front, it creates it from the file sent.
	 * 
	 * @param ccdaFile
	 *            The C-CDA XML file intended to be scored
	 */
	@RequestMapping(value = "/savescorecardservicebackend", method = RequestMethod.POST)
	public void savescorecardservicebackend(
			@RequestParam("ccdaFile") MultipartFile ccdaFile,
			HttpServletResponse response) {		
		handlePureBackendCall(ccdaFile, response, SaveReportType.MATCH_UI);
	}
	
	/**
	 * A single service to handle a pure back-end implementation of the
	 * scorecard which streams back a PDF report. 
	 * This does not require the completed JSON up-front, it creates it from the file sent.
	 * This differs from the savescorecardservicebackend in that it has its own specific format of the results 
	 * (dynamic and static table with 'call outs', no filename, more overview type content, etc.) 
	 * and is intended to be used when a Direct Message is received with a C-CDA document.
	 * 
	 * @param ccdaFile
	 *            The C-CDA XML file intended to be scored
	 * @param sender
	 * 			  The email address of the sender to be logged in the report
	 */
	@RequestMapping(value = "/savescorecardservicebackendsummary", method = RequestMethod.POST)
	public void savescorecardservicebackendsummary(
			@RequestParam("ccdaFile") MultipartFile ccdaFile, @RequestParam("sender") String sender,
			HttpServletResponse response) {
		if(ApplicationUtil.isEmpty(sender)) {
			sender = "Unknown Sender";
		}
		handlePureBackendCall(ccdaFile, response, SaveReportType.SUMMARY, sender);
	}
	
	private static void handlePureBackendCall(MultipartFile ccdaFile, HttpServletResponse response, SaveReportType reportType) {
		handlePureBackendCall(ccdaFile, response, reportType, null);
	}
	
	private static void handlePureBackendCall(MultipartFile ccdaFile, HttpServletResponse response, SaveReportType reportType, 
			String sender) {
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
			if(ApplicationUtil.isEmpty(pojoResponse.getFilename())) {
				pojoResponse.setFilename("Unknown");
			}
			// otherwise it uses the name given by ccdascorecardservice
		}
		if(reportType == SaveReportType.SUMMARY) {
			pojoResponse.setFilename(sender);
		}
		convertHTMLToPDFAndStreamToOutput(
				ensureLogicalParseTreeInHTML(convertReportToHTML(pojoResponse, reportType)),
				response);
	};

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
	protected static String convertReportToHTML(ResponseTO report, SaveReportType reportType) {
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

					appendHeader(sb, report, results, reportType);
					appendHorizontalRuleWithBreaks(sb);
					
					if(reportType == SaveReportType.SUMMARY) {						
						appendPreTopLevelResultsContent(sb);
					}
					
					appendTopLevelResults(sb, results, categories,
							referenceResults, reportType, report.getCcdaDocumentType());
					if(reportType == SaveReportType.MATCH_UI) {
						appendHorizontalRuleWithBreaks(sb);
						appendHeatmap(sb, results, categories,
								referenceResults);
					}
					sb.append("<br />");
					appendHorizontalRuleWithBreaks(sb);
					
					if(reportType == SaveReportType.MATCH_UI) appendDetailedResults(sb, categories, referenceResults);
					
					if(reportType == SaveReportType.SUMMARY) appendPictorialGuide(sb, categories, referenceResults);
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
		appendStyleSheet(sb);
		sb.append("</head>");
		sb.append("<body style='font-family: \"Helvetica Neue\",Helvetica,Arial,sans-serif;'>");
	}
	
	private static void appendStyleSheet(StringBuffer sb) {
		sb.append("<style>")
		 .append(System.lineSeparator())
		 .append(".site-header  {")
	     .append("	background: url(\"https://sitenv.org/assets/images/site/bg-header-1920x170.png\")")
	     .append("		repeat-x center top #1fdbfe;")
	     .append("}")
		 .append(System.lineSeparator())
		 .append(".site-logo  {")
	     .append("	text-decoration: none;")
	     .append("}")
	     .append(System.lineSeparator())
	     .append("table {")
	     .append("    font-family: arial, sans-serif;")
	     .append("    border-collapse: separate;")
	     .append("    width: 100%;")
	     .append("}")
	     .append(System.lineSeparator())
	     .append("td, th {")
	     .append("    border: 1px solid #dddddd;")
	     .append("    text-align: left;")
	     .append("    padding: 8px;")	     
	     .append("}")
	     .append(System.lineSeparator())
	     .append("#dynamicTable {")
	     .append("  border-collapse: collapse;")
	     .append("}")
	     .append(System.lineSeparator())
	     .append("#dynamicTable tr:nth-child(even) {")
	     .append("    background-color: #dddddd;")
	     .append("}")	     
	     .append(System.lineSeparator())
	     .append("#staticTable {")
	     .append("	font-size: 11px;")
	     .append("}")
	     .append(System.lineSeparator())	     
	     .append(".removeBorder {")
	     .append("  border: none;")
	     .append("}")
	     .append(System.lineSeparator())
	     .append("#sectionPopOutLink {")
	     .append("  border-top: 6px double MEDIUMPURPLE;")
	     .append("  border-bottom: none;")
	     .append("  border-left: none;")
	     .append("  border-right: none;")
	     .append("}")
	     .append("#gradePopOutLink {")
	     .append("  border-left: 6px solid MEDIUMSEAGREEN;")
	     .append("  border-right: none;")
	     .append("  border-bottom: none;")
	     .append("  border-top: none;  ")
	     .append("}")
	     .append("#issuePopOutLink {")
	     .append("  border-left: 6px double orange;")
	     .append("  border-right: 6px double orange;")
	     .append("  border-bottom: none;")
	     .append("  border-top: none;  ")
	     .append("}")
	     .append("#errorPopOutLink {")
	     .append("  border-right: 6px solid red;")
	     .append("  border-left: none;")
	     .append("  border-bottom: none;")
	     .append("  border-top: none;  ")
	     .append("}")
	     .append("#feedbackPopOutLink {")
	     .append("  border-top: 6px double DEEPSKYBLUE;")
	     .append("  border-bottom: none;")
	     .append("  border-left: none;")
	     .append("  border-right: none;")
	     .append("}")
	     .append(System.lineSeparator())
	     .append("#sectionPopOut {")
	     .append("  border: 6px double MEDIUMPURPLE;")
	     .append("  border-radius: 25px 0px 25px 25px;")
	     .append("}")
	     .append("#gradePopOut {")
	     .append("  border: 6px solid MEDIUMSEAGREEN;")
	     .append("  border-radius: 25px 25px 25px 25px;")
	     .append("}")
	     .append("#issuePopOut {")
	     .append("  border: 6px double orange;")
	     .append("  border-radius: 25px 25px 0px 0px;")
	     .append("}")
	     .append("#errorPopOut {")
	     .append("  border: 6px solid red;")
	     .append("  border-radius: 25px 25px 25px 25px;")
	     .append("}")
	     .append("#feedbackPopOut {")
	     .append("  border: 6px double DEEPSKYBLUE;")
	     .append("  border-radius: 0px 25px 25px 25px;")
	     .append("}")
	     .append(System.lineSeparator())
	     .append("#sectionHeader {")
	     .append("  border: 6px double MEDIUMPURPLE;")
	     .append("}")
	     .append("#gradeHeader {")
	     .append("  border: 6px solid MEDIUMSEAGREEN;")
	     .append("}")
	     .append("#issueHeader {")
	     .append("  border: 6px double orange;")
	     .append("}")
	     .append("#errorHeader {")
	     .append("  border: 6px solid red;")
	     .append("}")
	     .append("#feedbackHeader {")
	     .append("  border: 6px double DEEPSKYBLUE;")
	     .append("}")
	     .append("</style>");		
	}

	private static void appendHeader(StringBuffer sb, ResponseTO report,
			Results results, SaveReportType reportType) {
		sb.append("<header id='topOfScorecard'>");
		sb.append("<center>");
		
		sb.append("<div class=\"site-header\">")
	     .append("  <a class=\"site-logo\" href=\"https://www.healthit.gov/\"")
	     .append("    rel=\"external\" title=\"HealthIT.gov\"> <img alt=\"HealthIT.gov\"")
	     .append("    src=\"https://sitenv.org/assets/images/site/healthit.gov.logo.png\" width='40%'>")
	     .append("  </a>")
	     .append("</div>");		

		sb.append("<br />");
		appendHorizontalRuleWithBreaks(sb);

		sb.append("<h1>" + "C-CDA ");
		if(reportType == SaveReportType.SUMMARY) {
			sb.append("Scorecard For "
					+ (report.getCcdaDocumentType() != null ? report
							.getCcdaDocumentType() : "document") + "</h1>");
			sb.append("<h5>");
			sb.append("<span style='float: left'>" + "Submitted By: "
			+ (!ApplicationUtil.isEmpty(report.getFilename()) ? report.getFilename() : "Unknown")
			+ "</span>");
			DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
			sb.append("<span style='float: right'>" + "Submission Time: " + dateFormat.format(new Date()) + "</span>");
			sb.append("</h5>");
			sb.append("<div style='clear: both'></div>");
		} else {
			sb.append(results.getDocType() + " "  
					+ (report.getCcdaDocumentType() != null ? report .getCcdaDocumentType() : "document") 
					+ " Scorecard For:" + "</h1>");		
			sb.append("<h2>" + report.getFilename() + "</h2>");
		}
		sb.append("</center>");
		sb.append("</header>");
	}
	
	private static void appendPreTopLevelResultsContent(StringBuffer sb) {
		sb.append("<p>")
	     .append("  The C-CDA Scorecard enables providers, implementers, and health")
	     .append("  IT professionals with a tool that compares how artifacts (transition")
	     .append("  of care documents, care plans etc) created by your organization")
	     .append("  stack up against the HL7 C-CDA implementation guide and HL7 best")
	     .append("  practices. The C-CDA Scorecard promotes best practices in C-CDA")
	     .append("  implementation by assessing key aspects of the structured data found")
	     .append("  in individual documents. The Scorecard tool provides a rough")
	     .append("  quantitative assessment and highlights areas of improvement which")
	     .append("  can be made today to move the needle forward in interoperability of")
	     .append("  C-CDA documents. The ")
	     .append("  <a href=\"http://www.hl7.org/documentcenter/public/wg/structure/C-CDA%20Scorecard%20Rubrics%203.pptx\">"
	     			+ "best practices and quantitative scoring criteria"
	     			+ "</a>")
	     .append("  have been developed by HL7 through the HL7-ONC Cooperative agreement")
	     .append("  to improve the implementation of health care standards. We hope that")
	     .append("  providers and health IT developers will use the tool to identify and")
	     .append("  resolve issues around C-CDA document interoperability in their")
	     .append("  health IT systems.")
	     .append("<p>")
	     .append("<p>")
	     .append("  The report has two pages. The first page contains the summary")
	     .append("  of the C-CDA Scorecard results highlighting the overall document")
	     .append("  grade compared to the industry, a quantitative score out of a maximum")
	     .append("  of 100, and areas for improvement organized by clinical domains. The")
	     .append("  second page on the other hand contains a guide to help the providers")
	     .append("  interpret the scorecard results and take appropriate action.")
	     .append("</p>");
	}
	
	private static void appendTopLevelResults(StringBuffer sb, Results results,
			List<Category> categories, List<ReferenceResult> referenceResults, SaveReportType reportType, String ccdaDocumentType) {
		
		boolean isReferenceResultsEmpty = ApplicationUtil.isEmpty(referenceResults);
		int conformanceErrorCount = isReferenceResultsEmpty ? 0 : referenceResults.get(CONFORMANCE_ERROR_INDEX).getTotalErrorCount();
		int certificationFeedbackCount = isReferenceResultsEmpty ? 0 : referenceResults.get(CERTIFICATION_FEEDBACK_INDEX).getTotalErrorCount();
		if(reportType == SaveReportType.SUMMARY) {
			//brief summary of overall document results (without scorecard issues count listed)
			sb.append("<p>"
					+ "Your " + ccdaDocumentType + " document received a grade of <b>" + results.getFinalGrade() + "</b>"
					+ " compared to an industry average of " + "<b>" + results.getIndustryAverageGrade() + "</b>" + ". "
					+ "The document scored " + "<b>" + results.getFinalNumericalGrade() + "/100" + "</b>"
					+ " and is "
					+ (conformanceErrorCount > 0 ? "non-compliant" : "compliant")
					+ " with the HL7 C-CDA IG"
					+ " and is "
					+ (certificationFeedbackCount > 0 ? "non-compliant" : "compliant")
					+ " with 2015 Edition Certification requirements. "
					+ "The detailed results organized by clinical domains are provided in the table below:"
					+ "</p>");
			//dynamic table
			appendDynamicTopLevelResultsTable(sb, results, categories, referenceResults);			
		} else {		
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
			if (isReferenceResultsEmpty) {
				for (ReferenceInstanceType refType : ReferenceInstanceType.values()) {
					if (refType == ReferenceInstanceType.CERTIFICATION_2015) {
						messageSuffix = "results";
					}
					appendSummaryRow(sb, 0, refType.getTypePrettyName(),
							messageSuffix, refType.getTypePrettyName(), false, reportType);
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
							messageSubject, isSingular, reportType);
					sb.append("</ul></li>");
				}
			}
		}
		
	}
	
	private static int getFailingSectionSpecificErrorCount(String categoryName,
			ReferenceInstanceType refType, List<ReferenceResult> referenceResults) {
		if (!ApplicationUtil.isEmpty(referenceResults)) {
			if (refType == ReferenceInstanceType.IG_CONFORMANCE) {
				List<ReferenceError> igErrors = 
						referenceResults.get(CONFORMANCE_ERROR_INDEX).getReferenceErrors();
				if (!ApplicationUtil.isEmpty(igErrors)) {
					return getFailingSectionSpecificErrorCountProcessor(categoryName, igErrors);
				}
			} else if (refType == ReferenceInstanceType.CERTIFICATION_2015) {
				List<ReferenceError> certErrors = 
						referenceResults.get(CERTIFICATION_FEEDBACK_INDEX).getReferenceErrors();
				if (!ApplicationUtil.isEmpty(certErrors)) {
					return getFailingSectionSpecificErrorCountProcessor(categoryName, certErrors);
				}
			}
		}
		return 0;
	}

	private static int getFailingSectionSpecificErrorCountProcessor(
			String categoryName, List<ReferenceError> errors) {
		int count = 0;
		for (int i = 0; i < errors.size(); i++) {
			String currentSectionInReferenceErrors = errors.get(i).getSectionName();
			if (!ApplicationUtil.isEmpty(currentSectionInReferenceErrors)) {
				if (currentSectionInReferenceErrors.equalsIgnoreCase(categoryName)) {
					count++;
				}
			}
		}
		return count;
	}
	
	private static void appendDynamicTopLevelResultsTable(StringBuffer sb, Results results,
			List<Category> categories, List<ReferenceResult> referenceResults) { 
		sb.append("<table id='dynamicTable'>")
		     .append("  <tr>")
		     .append("    <th>Section</th>")
		     .append("    <th>Scorecard Grade/Score</th>")
		     .append("    <th>Scorecard Issues</th>")
		     .append("    <th>Conformance Errors</th>")
		     .append("    <th>Certification Feedback</th>")
		     .append("  </tr>");				
		for(Category category : categories) {
//			if(category.getNumberOfIssues() > 0 || referenceResults.get(ReferenceInstanceType.IG/CERT).getTotalErrorCount() > 0) {
			if(category.getNumberOfIssues() > 0 || category.isFailingConformance() || category.isCertificationFeedback()) {				
			sb.append("  <tr>")
		     .append("    <td>" + (category.getCategoryName() != null ? category.getCategoryName() : "Unknown") + "</td>")
		     .append("    <td>" + (category.getCategoryGrade() != null ? category.getCategoryGrade() : "N/A") + "</td>")
		     .append("    <td>" + category.getNumberOfIssues() + "</td>")
		     .append("    <td>" 
		     + (!ApplicationUtil.isEmpty(category.getCategoryName()) 
		    		 ? getFailingSectionSpecificErrorCount(category.getCategoryName(), ReferenceInstanceType.IG_CONFORMANCE, referenceResults) 
		    		 : "N/A")
		     + "</td>")
		     .append("    <td>" 
		     + (!ApplicationUtil.isEmpty(category.getCategoryName()) 
		    		 ? getFailingSectionSpecificErrorCount(category.getCategoryName(), ReferenceInstanceType.CERTIFICATION_2015, referenceResults) 
		    		 : "N/A") 
		     + "</td>")
		     .append("  </tr>");
			}
		}
		sb.append("</table>");
	}	
	
	private static void appendHeatmap(StringBuffer sb, Results results,
			List<Category> categories, List<ReferenceResult> referenceResults) {
		
		sb.append("<span id='heatMap'>" + "</span>");
		for (Category curCategory : categories) {
			sb.append("<h3>" 
					+ (curCategory.getNumberOfIssues() > 0 
							? "<a href='#" + curCategory.getCategoryName() + "-category" + "'>" : "")
					+ curCategory.getCategoryName()
					+ (curCategory.getNumberOfIssues() > 0
							? "</a>" : "")
					+ "</h3>");

			sb.append("<ul>");
			if (curCategory.getCategoryGrade() != null) {
				sb.append("<li>" + "Section Grade: " + "<b>"
						+ curCategory.getCategoryGrade() + "</b>" + "</li>"
						+ "<li>" + "Number of Issues: " + "<b>"
						+ curCategory.getNumberOfIssues() + "</b>" + "</li>");
			} else {
				sb.append("<li>"
						+ "This category was not scored as it ");
				if(curCategory.isNullFlavorNI()) {
					sb.append("is an <b>empty section</b>");
				}
			  	boolean failingConformance = curCategory.isFailingConformance();
			  	boolean failingCertification = curCategory.isCertificationFeedback();
			  	if(failingConformance || failingCertification) {
			  		if(failingConformance && failingCertification || failingConformance && !failingCertification) {
				  		//we default to IG if true for both since IG is considered a more serious issue (same with the heatmap label, so we match that)
				  		//there could be a duplicate for two reasons, right now, there's always at least one duplicate since we derive ig from cert in the backend
				  		//in the future this might not be the case, but, there could be multiple section fails in the same section, so we have a default for those too				  			
						sb.append("contains <a href='#" + ReferenceInstanceType.IG_CONFORMANCE.getTypePrettyName() 
								+ "-category'" + ">" + "Conformance Errors" + "</a>");
					} else if(failingCertification && !failingConformance) {
						sb.append("contains <a href='#" + ReferenceInstanceType.CERTIFICATION_2015.getTypePrettyName() 
								+ "-category'" + ">" + "Certification Feedback" + "</a>");
					}
			  	}
				sb.append("</li>");
				
				
			}
			sb.append("</ul></li>");
		}
		
	}

	private static void appendSummaryRow(StringBuffer sb, int result,
			String header, String messageSuffix, String messageSubject,
			boolean isSingular) {
		appendSummaryRow(sb, result, header, messageSuffix, messageSubject, isSingular, null);
	}
	
	private static void appendSummaryRow(StringBuffer sb, int result,
			String header, String messageSuffix, String messageSubject,
			boolean isSingular, SaveReportType reportType) {
		if(reportType == null) {
			reportType = SaveReportType.MATCH_UI;
		}
		sb.append("<h3>"
				+ header
				+ ": "
				+ ("Scorecard Issues".equals(header) || result < 1 || reportType == SaveReportType.SUMMARY ? result
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
				if (curRefInstance.getTotalErrorCount() > 0) {
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

			} //END for (ReferenceResult curRefInstance : referenceResults)
		}

		for (Category curCategory : categories) {
			if (curCategory.getNumberOfIssues() > 0) {
			sb.append("<h3 id='" + curCategory.getCategoryName() + "-category"
					+ "'>" + curCategory.getCategoryName() + "</h3>");

				sb.append("<ul>"); // START curCategory ul
				sb.append("<li>" + "Section Grade: "
						+ curCategory.getCategoryGrade() + "</li>" + "<li>"
						+ "Number of Issues: "
						+ curCategory.getNumberOfIssues() + "</li>");

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

				sb.append("</ul>"); // END curCategory ul
			
			appendBackToTopWithBreaks(sb);
			
			} //END if (curCategory.getNumberOfIssues() > 0)
		} //END for (Category curCategory : categories)
		
	}
	
	private static void appendPictorialGuide(StringBuffer sb,
			List<Category> categories, List<ReferenceResult> referenceResults) {
		
		//TODO: ensure this starts on a new page in a more proper manner - the first table is dynamic in size
		sb.append("<br />");
//		sb.append("<br />");
		
		sb.append("<h2>" + "Scorecard Results Guide" + "</h2>");		
		
		sb.append("<p>" + "The following guide defines each item and explains how the information should be used. " 
		+ "Note: The guide displays mocked static data for informational purposes only." + "</p>");
		
		//static table
		sb.append("<table id='staticTable'>")
		     .append("  <tr>")
		     .append("    <td class=\"removeBorder\"></td>")
		     .append("    <td class=\"removeBorder\"></td>")
		     .append("    <td class=\"removeBorder\"></td>")
		     .append("    <td class=\"removeBorder\"></td>")
		     .append("    <td id=\"issuePopOut\" rowspan='2'>")
		     .append("      A Scorecard Issue identifies data which could be presented in a better way. It is best to ensure that this number is low.")
		     .append("    </td>")
		     .append("    <td class=\"removeBorder\"></td>")
		     .append("    <td class=\"removeBorder\"></td>")
		     .append("    <td class=\"removeBorder\"></td>")
		     .append("    <td class=\"removeBorder\"></td>")
		     .append("  </tr>")
		     .append("  <tr>")
		     .append("    <td class=\"removeBorder\"></td>")
		     .append("    <td class=\"removeBorder\"></td>")
		     .append("    <td id=\"gradePopOut\" colspan='2'>")
		     .append("      The Scorecard Grade identifies the overall quality of the document in reference to the Scorecard Issues found. The score, in parentheses, is where the grade is derived from based on the following criteria: A+ (more than 94 points), A- (90 to 94 points), B+ (85 to 89 points), B- (80 to 84 points), C (70 to 79 points), D (less than 70 points).")
		     .append("    </td>")
		     .append("    <td id=\"errorPopOut\" colspan='2'>")
		     .append("      A Conformance Error implies that the document does not adhere to a SHALL-based severity level rule listed in the relevant HL7 C-CDA Implementation Guide. A well-formed document will have 0 errors. Work with your vendor to understand the conformance errors and rectify them.")
		     .append("    </td>")
		     .append("    <td class=\"removeBorder\"></td>")
		     .append("    <td class=\"removeBorder\"></td>")
		     .append("  </tr>")
		     .append("  <tr>")
		     .append("    <td class=\"removeBorder\"></td>")
		     .append("    <td class=\"removeBorder\"></td>")
		     .append("    <td class=\"removeBorder\"></td>")
		     .append("    <td id=\"gradePopOutLink\"></td>")
		     .append("    <td id=\"issuePopOutLink\"></td>")
		     .append("    <td id=\"errorPopOutLink\"></td>")
		     .append("    <td class=\"removeBorder\"></td>")
		     .append("    <td class=\"removeBorder\"></td>")
		     .append("    <td class=\"removeBorder\"></td>")
		     .append("  </tr>  ")
		     .append("  <tr>")
		     .append("    <td id=\"sectionPopOut\" rowspan='4'>")
		     .append("      The Section refers to the part of the document which may need attention depending on the identified results in the related row. Refer to the relevant HL7 C-CDA Implementation Guide to find the related sections your document.")
		     .append("    </td>")
		     .append("    <td id=\"sectionPopOutLink\"></td>")
		     .append("    <th id=\"sectionHeader\">Section</th>")
		     .append("    <th id=\"gradeHeader\">Scorecard Grade/Score</th>")
		     .append("    <th id=\"issueHeader\">Scorecard Issues</th>")
		     .append("    <th id=\"errorHeader\">Conformance Errors</th>")
		     .append("    <th id=\"feedbackHeader\">Certification Feedback</th>")
		     .append("    <td id=\"feedbackPopOutLink\"></td>")
		     .append("    <td id=\"feedbackPopOut\" rowspan='4'>")
		     .append("      A Certification Feedback result is not as severe as a Conformance Error. It deals with vocabulary issues and other suggestions. One should strive for 0 feedback results in their document.")
		     .append("    </td>")
		     .append("  </tr>")
		     .append("  <tr>")
		     .append("    <td class=\"removeBorder\"></td>")
		     .append("    <td>Problems</td>")
		     .append("    <td>A+</td>")
		     .append("    <td>5</td>")
		     .append("    <td>0</td>")
		     .append("    <td>0</td>")
		     .append("    <td class=\"removeBorder\"></td>")
		     .append("  </tr>")
		     .append("  <tr>")
		     .append("    <td class=\"removeBorder\"></td>")
		     .append("    <td>Lab Results</td>")
		     .append("    <td>A-</td>")
		     .append("    <td>4</td>")
		     .append("    <td>0</td>")
		     .append("    <td>0</td>")
		     .append("    <td class=\"removeBorder\"></td>")
		     .append("  </tr>")
		     .append("  <tr>")
		     .append("    <td class=\"removeBorder\"></td>")
		     .append("    <td>Vital Signs</td>")
		     .append("    <td>A-</td>")
		     .append("    <td>6</td>")
		     .append("    <td>0</td>")
		     .append("    <td>0</td>")
		     .append("    <td class=\"removeBorder\"></td>")
		     .append("  </tr>  ")
		     .append("  <tr>")
		     .append("    <td class=\"removeBorder\"></td>")
		     .append("    <td class=\"removeBorder\"></td>")
		     .append("    <td>Encounters</td>")
		     .append("    <td>D</td>")
		     .append("    <td>6</td>")
		     .append("    <td>0</td>")
		     .append("    <td>0</td>")
		     .append("    <td class=\"removeBorder\"></td>")
		     .append("    <td class=\"removeBorder\"></td>")
		     .append("  </tr>")
		     .append("  <tr>")
		     .append("    <td class=\"removeBorder\"></td>")
		     .append("    <td class=\"removeBorder\"></td>")
		     .append("    <td>Medications</td>")
		     .append("    <td>D</td>")
		     .append("    <td>0</td>")
		     .append("    <td>0</td>")
		     .append("    <td>1</td>")
		     .append("    <td class=\"removeBorder\"></td>")
		     .append("    <td class=\"removeBorder\"></td>")
		     .append("  </tr>")
		     .append("  <tr>")
		     .append("    <td class=\"removeBorder\"></td>")
		     .append("    <td class=\"removeBorder\"></td>")
		     .append("    <td>Allergies</td>")
		     .append("    <td>D</td>")
		     .append("    <td>0</td>")
		     .append("    <td>0</td>")
		     .append("    <td>1</td>")
		     .append("    <td class=\"removeBorder\"></td>")
		     .append("    <td class=\"removeBorder\"></td>")
		     .append("  </tr>")
		     .append("  <tr>")
		     .append("    <td class=\"removeBorder\"></td>")
		     .append("    <td class=\"removeBorder\"></td>")
		     .append("    <td>Immunizations</td>")
		     .append("    <td>D</td>")
		     .append("    <td>0</td>")
		     .append("    <td>0</td>")
		     .append("    <td>2</td>")
		     .append("    <td class=\"removeBorder\"></td>")
		     .append("    <td class=\"removeBorder\"></td>")
		     .append("  </tr>  ")
		     .append("</table>");		
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
	
	private static void convertHTMLToPDF(String cleanHtmlReport) {
		convertHTMLToPDF(cleanHtmlReport, null);
	}
	
	private static void convertHTMLToPDF(String cleanHtmlReport, HttpServletResponse response) {
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

			if(response != null) {
				//Stream to Output
				response.setContentType("application/pdf");
				response.setHeader("Content-disposition", "attachment; filename="
						+ "scorecardReport.pdf");
				response.setHeader("max-age=3600", "must-revalidate");
				response.addCookie(new Cookie("fileDownload=true", "path=/"));
				out = response.getOutputStream();				
			} else {
				//Save to local file system
				out = new FileOutputStream(new File("testSaveReportImplementation.pdf"));
			}
			
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
	
	protected static void convertHTMLToPDFAndStreamToOutput(
			String cleanHtmlReport, HttpServletResponse response) {
		convertHTMLToPDF(cleanHtmlReport, response);
	}
	
	private static void convertHTMLToPDFAndSaveToLocalFileSystem(String cleanHtmlReport) {
		convertHTMLToPDF(cleanHtmlReport);
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
	
	private enum SaveReportType {
		MATCH_UI, SUMMARY;
	}
	
	private static void buildReportUsingJSONFromLocalFile(String filenameWithoutExtension, 
			SaveReportType reportType) throws URISyntaxException {
		URI jsonFileURI = new File(
				"src/main/webapp/resources/"
						+ filenameWithoutExtension + ".json").toURI();		
		System.out.println("jsonFileURI");
		System.out.println(jsonFileURI);
		String jsonReportData = convertFileToString(jsonFileURI);
		System.out.println("jsonReportData");
		System.out.println(jsonReportData);
		ResponseTO pojoResponse = convertJsonToPojo(jsonReportData);
		System.out.println("response");
		System.out.println(pojoResponse.getCcdaDocumentType());
		
		convertHTMLToPDFAndSaveToLocalFileSystem(
				ensureLogicalParseTreeInHTML(convertReportToHTML(pojoResponse, reportType)));		
	}
	
	private static String convertFileToString(URI fileURI) {
		StringBuilder sb = new StringBuilder();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(fileURI.getPath()));
			String sCurrentLine = "";
			while ((sCurrentLine = br.readLine()) != null) {
				sb.append(sCurrentLine);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}	
	
	public static void main(String[] args) {
		String[] filenames = {"highScoringSample", "lowScoringSample", "sampleWithErrors"};
		final int HIGH_SCORING_SAMPLE = 0, LOW_SCORING_SAMPLE = 1, SAMPLE_WITH_ERRORS = 2;
		try {
			buildReportUsingJSONFromLocalFile(filenames[SAMPLE_WITH_ERRORS], SaveReportType.SUMMARY);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}	

}
