package org.sitenv.service.ccda.smartscorecard.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jsoup.Jsoup;
import org.sitenv.ccdaparsing.model.CCDAXmlSnippet;
import org.sitenv.service.ccda.smartscorecard.model.CCDAScoreCardRubrics;
import org.sitenv.service.ccda.smartscorecard.model.Category;
import org.sitenv.service.ccda.smartscorecard.model.ResponseTO;
import org.sitenv.service.ccda.smartscorecard.model.Results;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationConstants;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationUtil;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.w3c.dom.Document;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lowagie.text.DocumentException;

@RestController
public class SaveReportController {

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
				
		if (report != null && report.getResults() != null) {
			if (report.isSuccess()) {
				Results results = report.getResults();
				List<Category> categories = results.getCategoryList();

				appendHeader(sb, report, results);
				appendHorizontalRuleWithBreaks(sb);

				appendTopLevelResults(sb, results, categories);
				appendHorizontalRuleWithBreaks(sb);

				appendDetailedResults(sb, categories);
			}
		} else {
			if (!report.isSuccess()) {
				appendErrorMessageFromReport(sb, report,
						ApplicationConstants.Error.IS_SUCCESS_FALSE);
			} else {
				appendErrorMessageFromReport(sb, report);
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
			List<Category> categories) {

		// If it's null we don't append the certification results since we
		// never received them. The typical scenario for this would be a pure
		// back-end (most likely local) implementation which already/only has
		// the results from ccdascorecardservice.
		// If it's not null, then it is true or false, which means it was set
		// after calling ccdascorecardservice and combined those results with
		// the (calculated) pass or fail result from referenceccdaservice (most
		// likely by a front-end call), and we do append the results.
		Boolean passedCertification = results.getPassedCertification();
		if (passedCertification != null) {
			sb.append("<center>");
			sb.append("<h2 style='color: "
					+ (passedCertification ? "green" : "red")
					+ ";background-color: "
					+ (passedCertification ? "#e6ffe6" : "#ffe6e6") + "'>");
			sb.append("Certification Score: "
					+ (passedCertification ? "Pass" : "Fail") + "</h2>");
			sb.append("</center>");
		}

		sb.append("<h3>Grade: " + results.getFinalGrade() + "</h3>");
		sb.append("<ul><li>");
		sb.append("<p>Your document scored a " + "<b>"
				+ results.getFinalGrade() + "</b>"
				+ " compared to an industry average of " + "<b>"
				+ results.getIndustryAverageGrade() + "</b>" + ".</p>");
		sb.append("</ul></li>");
		sb.append("<h3>Score: " + results.getFinalNumericalGrade() + "</h3>");
		sb.append("<ul><li>");
		sb.append("<p>Your document scored " + "<b>"
				+ +results.getFinalNumericalGrade() + "</b>" + " out of "
				+ "<b>" + " 100 " + "</b>" + " total possible points.</p>");
		sb.append("</ul></li>");
		sb.append("<h3> Number of Issues: " + results.getNumberOfIssues()
				+ "</h3>");
		sb.append("<ul><li>");
		sb.append("<p>There are " + "<b>" + results.getNumberOfIssues()
				+ "</b>" + " specific issues in your document.</p>");
		sb.append("</ul></li>");

		appendHorizontalRuleWithBreaks(sb);

		sb.append("<h2 id='sectionList'>"
				+ "Section Grades with Number of Issues per Section" + "</h2>");
		for (Category curCategory : categories) {
			sb.append("<h3>" + "<a href='#" + curCategory.getCategoryName()
					+ "-category" + "'>" + curCategory.getCategoryName()
					+ "</a>" + "</h3>");
			sb.append("<ul>");
			sb.append("<li>" + "Section Grade: " + "<b>"
					+ curCategory.getCategoryGrade() + "</b>" + "</li>"
					+ "<li>" + "Number of Issues: " + "<b>"
					+ curCategory.getNumberOfIssues() + "</b>" + "</li>");
			sb.append("</ul></li>");
		}
	}

	private static void appendDetailedResults(StringBuffer sb,
			List<Category> categories) {

		sb.append("<h2>" + "Detailed Results" + "</h2>");

		for (Category curCategory : categories) {
			sb.append("<h3 id='" + curCategory.getCategoryName() + "-category"
					+ "'>" + curCategory.getCategoryName() + "</h3>");
			sb.append("<ul>"); // START curCategory ul
			sb.append("<li>" + "Section Grade: "
					+ curCategory.getCategoryGrade() + "</li>" + "<li>"
					+ "Number of Issues: " + curCategory.getNumberOfIssues()
					+ "</li>");

			if (curCategory.getNumberOfIssues() > 0) {

				sb.append("<ol>"); // START rules ol
				for (CCDAScoreCardRubrics curRubric : curCategory
						.getCategoryRubrics()) {
					if (curRubric.getNumberOfIssues() > 0) {
						sb.append("<li>" + "Rule: " + curRubric.getRule()
								+ "</li>");
						if (curRubric.getDescription() != null) {
							sb.append("<ul>" + "<li>" + "Description" + "</li>"
									+ "<ul>" + "<li>"
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
		// sb.append("<a href='#sectionList'>Back to Section List</a>");
		sb.append("<br />");
		// sb.append("<br />");
	}
	
	private static void appendErrorMessage(StringBuffer sb, String errorMessage) {
		sb.append("<h2 style='color:red; background-color: #ffe6e6'>");
		sb.append(errorMessage);
		sb.append("</h2>");
	}
	
	private static void appendGenericErrorMessage(StringBuffer sb) {
		sb.append("<p>" + ApplicationConstants.Error.JSON_TO_JAVA_JACKSON
				+ "<br />" + ApplicationConstants.Error.CONTACT + "</p>");
	}
	
	private static void appendErrorMessageFromReport(StringBuffer sb, ResponseTO report) {
		appendErrorMessageFromReport(sb, report, null);
	}
	
	private static void appendErrorMessageFromReport(StringBuffer sb, ResponseTO report, String extraMessage) {
		if(report.getErrorMessage() != null && !report.getErrorMessage().isEmpty()) {
			appendErrorMessage(sb, report.getErrorMessage());
		} else {
			appendGenericErrorMessage(sb);
		}
		if(extraMessage != null && !extraMessage.isEmpty()) {
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