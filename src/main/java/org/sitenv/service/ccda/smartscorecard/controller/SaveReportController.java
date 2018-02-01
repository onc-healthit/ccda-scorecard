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
import java.util.ArrayList;
import java.util.Collections;
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
import org.sitenv.service.ccda.smartscorecard.cofiguration.ApplicationConfiguration;
import org.sitenv.service.ccda.smartscorecard.model.CCDAScoreCardRubrics;
import org.sitenv.service.ccda.smartscorecard.model.Category;
import org.sitenv.service.ccda.smartscorecard.model.ReferenceError;
import org.sitenv.service.ccda.smartscorecard.model.ReferenceResult;
import org.sitenv.service.ccda.smartscorecard.model.ReferenceTypes.ReferenceInstanceType;
import org.sitenv.service.ccda.smartscorecard.model.ResponseTO;
import org.sitenv.service.ccda.smartscorecard.model.Results;
import org.sitenv.service.ccda.smartscorecard.processor.ScorecardProcessor;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationConstants;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationUtil;
import org.springframework.beans.factory.annotation.Autowired;
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
	
	@Autowired
	private ScorecardProcessor scorecardProcessor;	

	public static final String SAVE_REPORT_CHARSET_NAME = "UTF8";
	private static final int CONFORMANCE_ERROR_INDEX = 0;
	private static final int CERTIFICATION_FEEDBACK_INDEX = 1;
	private static final boolean LOG_HTML = true;

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
		handlePureBackendCall(ccdaFile, response, SaveReportType.MATCH_UI, scorecardProcessor);
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
		handlePureBackendCall(ccdaFile, response, SaveReportType.SUMMARY, scorecardProcessor, sender);
	}
	
	private static void handlePureBackendCall(MultipartFile ccdaFile, HttpServletResponse response, SaveReportType reportType, 
			ScorecardProcessor scorecardProcessor) {
		handlePureBackendCall(ccdaFile, response, reportType, scorecardProcessor, "savescorecardservicebackend");
	}
	
	private static void handlePureBackendCall(MultipartFile ccdaFile, HttpServletResponse response, SaveReportType reportType, 
			ScorecardProcessor scorecardProcessor, String sender) {
		ResponseTO pojoResponse = callCcdascorecardserviceInternally(ccdaFile, scorecardProcessor, sender, reportType);
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
	
	protected static ResponseTO callCcdascorecardserviceInternally(MultipartFile ccdaFile, 
			ScorecardProcessor scorecardProcessor, String sender, SaveReportType reportType) {
		return scorecardProcessor.processCCDAFile(ccdaFile, reportType == SaveReportType.SUMMARY, sender);
	}

	public static ResponseTO callCcdascorecardserviceExternally(MultipartFile ccdaFile) {
		String endpoint = ApplicationConfiguration.CCDASCORECARDSERVICE_URL;
		return callServiceExternally(endpoint, ccdaFile, null);
	}	

	public static ResponseTO callSavescorecardservicebackendExternally(MultipartFile ccdaFile) {
		String endpoint = ApplicationConfiguration.SAVESCORECARDSERVICEBACKEND_URL;
		return callServiceExternally(endpoint, ccdaFile, null);
	}
	
	public static ResponseTO callSavescorecardservicebackendsummaryExternally(MultipartFile ccdaFile, String senderValue) {
		String endpoint = ApplicationConfiguration.SAVESCORECARDSERVICEBACKENDSUMMARY_URL;
		return callServiceExternally(endpoint, ccdaFile, senderValue);
	}
	
	private static ResponseTO callServiceExternally(String endpoint, MultipartFile ccdaFile, String senderValue) {
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
			if(senderValue != null) {
				String senderKey = "sender";
				requestMap.add(senderKey, senderValue);
			}

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

			pojoResponse = restTemplate.postForObject(endpoint, requestEntity, ResponseTO.class);
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
					
					if(reportType == SaveReportType.MATCH_UI) {
						if(!ApplicationUtil.isEmpty(report.getReferenceResults()) || results.getNumberOfIssues() > 0) {
							appendDetailedResults(sb, categories, referenceResults);
						}
					}
					if(reportType == SaveReportType.SUMMARY) {
						/*
						appendPictorialGuide(sb, categories, referenceResults);
						appendPictorialGuideKey(sb);
						*/
					} 					
				}
			} else {
				// report.getResults() == null
				if (!report.isSuccess()) {
					appendErrorMessageFromReport(sb, report, null);
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
		sb.append("<html lang='en' xml:lang='en'>");
		sb.append("	<head>");
		sb.append("		<title>SITE C-CDA Scorecard Report</title>");
		sb.append("		<meta name='author' content='ONC SITE'>");
		sb.append("		<meta name='subject' content='ONC Scorecard'>");
		sb.append("		<meta name='keywords' content='Scorecard, C-CDA, ONC, R1.1, R2.1, CDA, Report, SITE, "
				+ 		"Validation, Validator, Standards, HL7, Structured Documents, Healthcare, HIT, HIE, Interoperability'>");
		sb.append("		<meta name='language' content='en'>");
		appendStyleSheet(sb);
		sb.append("	</head>");
		sb.append("<body style='font-family: \"Helvetica Neue\",Helvetica,Arial,sans-serif;'>");
		sb.append("<body style='font-family: Helvetica, Arial, sans-serif '>");
	}
	
	private static void appendStyleSheet(StringBuffer sb) {
		 sb.append("<style>" + 
		 "     .site-header {    background: url('https://sitenv.org/assets/images/site/bg-header-1920x170.png') repeat-x center top #1fdbfe;  }  "  + 
		 "     .site-logo {    text-decoration: none;  }  "  + 
		 "     table {    font-family: arial, sans-serif;    border-collapse: separate;    width: 100%;  }  "  + 
		 "     td,  th {    border: 1px solid #dddddd;    text-align: left;    padding: 8px;  }  "  + 
		 "     #dynamicTable tr:nth-child(even) {    background-color: #dddddd;  }  "  + 
		 "     #staticTable {    font-size: 11px;  }  "  + 
		 "     .popOuts {    font-size: 11px;  background-color: ghostwhite !important;  }  "  + 
		 "     .removeBorder {    border: none;    background-color: white;  }  "  + 
		 "     #notScoredRowPopOutLink {    border-top: 6px double MAGENTA;    border-bottom: none;    border-left: none;    border-right: none;  }  "  + 
		 "     #perfectRowPopOutLink {    border-top: 6px double MEDIUMPURPLE;    border-bottom: none;    border-left: none;    border-right: none;  }    "  + 
		 "     #gradePopOutLink {    border-left: 6px solid MEDIUMSEAGREEN;    border-right: none;    border-bottom: none;    border-top: none;  }  "  + 
		 "     #issuePopOutLink {    border-left: 6px double orange;    border-right: none;    border-bottom: none;    border-top: none;  }  "  + 
		 "     #errorPopOutLink {    border-right: none;    border-left: 6px solid red;    border-bottom: none;    border-top: none;  }  "  + 
		 "     #feedbackPopOutLink {    border-right: none;    border-left: 6px double DEEPSKYBLUE;    border-bottom: none;    border-top: none;  }  "  + 
		 "     #notScoredRowPopOut {    border: 6px double MAGENTA;    border-radius: 0px 25px 25px 25px;  }  "  + 
		 "     #perfectRowPopOut {    border: 6px double MEDIUMPURPLE;    border-radius: 25px 0px 25px 25px;  }  "  + 
		 "     #gradePopOut {    border: 6px solid MEDIUMSEAGREEN;    border-radius: 25px 25px 25px 25px;  }  "  + 
		 "     #issuePopOut {    border: 6px double orange;    border-radius: 25px 25px 25px 0px;  }  "  + 
		 "     #errorPopOut {    border: 6px solid red;    border-radius: 25px 25px 25px 0px;  }  "  + 
		 "     #feedbackPopOut {    border: 6px double DEEPSKYBLUE;    border-radius: 25px 25px 25px 0px;  }  "  + 
		 "     #perfectRowLeftHeader {    border-left: 6px double MEDIUMPURPLE;    border-top: 6px double MEDIUMPURPLE;    border-bottom: 6px double MEDIUMPURPLE;  }  " + 
		 "     #perfectRowRightHeader {    border-right: 6px double MEDIUMPURPLE;    border-top: 6px double MEDIUMPURPLE;    border-bottom: 6px double MEDIUMPURPLE;  }  " + 
		 "     .perfectRowMiddleHeader {    border-top: 6px double MEDIUMPURPLE;    border-bottom: 6px double MEDIUMPURPLE;  }  "  + 
		 "     #notScoredRowLeftHeader {    border-left: 6px double MAGENTA;    border-top: 6px double MAGENTA;    border-bottom: 6px double MAGENTA;  }  " + 
		 "	   #notScoredRowRightHeader {    border-right: 6px double MAGENTA;    border-top: 6px double MAGENTA;    border-bottom: 6px double MAGENTA;  }  " + 
		 "     .notScoredRowMiddleHeader {    border-top: 6px double MAGENTA;    border-bottom: 6px double MAGENTA;  }   "  + 
		 "     #gradeHeader {    border: 6px solid MEDIUMSEAGREEN;  }  "  + 
		 "     #issueHeader {    border: 6px double orange;  }  "  + 
		 "     #errorHeader {    border: 6px solid red;  }  "  + 
		 "     #feedbackHeader {    border: 6px double DEEPSKYBLUE;  }  "  + 
		 "     #keyGradeHeader {    color: MEDIUMSEAGREEN;    font-weight: bold;  }  "  + 
		 "     #keyIssueHeader {    color: orange;    font-weight: bold;  }  "  + 
		 "     #keyErrorHeader {    color: red;    font-weight: bold;  }  "  + 
		 "     #keyFeedbackHeader {    color: DEEPSKYBLUE;    font-weight: bold;  }  "  + 
		 "    </style>  ");
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
			appendBasicResults(sb, results);			
		} else {
			sb.append(results.getCcdaVersion() + " "  
					+ (report.getCcdaDocumentType() != null ? report .getCcdaDocumentType() : "document") 
					+ " Scorecard For:" + "</h1>");		
			sb.append("<h2>" + report.getFilename() + "</h2>");
		}
		sb.append("</center>");
		sb.append("</header>");
	}
	
	private static void appendPreTopLevelResultsContent(StringBuffer sb) {
		/*
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
	     .append("</p>")
	     */
		
		sb.append("<p>If you are not satisfied with your results:</p>");
		sb.append("<ul>");
			sb.append("<li>");
				sb.append("Please ask your vendor to submit the document to the SITE C-CDA Scorecard website "
						+ " at " + "<a href=\"https://healthit.gov/scorecard/\">" + "www.healthit.gov/scorecard" + "</a>"
						+ " for more detailed information");
			sb.append("</li>");
			sb.append("<li>");
				sb.append("Using the results provided by the online tool, "
						+ "your vendor can update or provide insight on how to update your document to meet the latest HL7 best-practices");
			sb.append("</li>");
			sb.append("<li>");
				sb.append("Once updated, "
						+ "resubmitting your document to the ONC One Click Scorecard will produce a report with a higher score "
						+ "and/or less or no conformance errors or less or no certification feedback results");
			sb.append("</li>");
		sb.append("</ul>");
		
	     sb.append("<p>")
	     .append("This page contains a summary of the C-CDA Scorecard results highlighting the overall document grade "
	     		+ "which is derived from a quantitative score out of a maximum of 100. ")
	     .append("</p>");
	     sb.append("<p>")
	     .append("The second and final page identifies areas for improvement organized by clinical domains.")
	     .append("</p>");
	}
	
	private static void appendBasicResults(StringBuffer sb, Results results) {
		sb.append("<center>");
		sb.append("<h2>");
		sb.append("<span style=\"margin-right: 40px\">Grade: " + results.getFinalGrade() + "</span>"); 
		sb.append("<span style=\"margin-left: 40px\">Score: " + results.getFinalNumericalGrade() + "/100" + "</span>");
		sb.append("</h2>");
		sb.append("</center>");
	}
	
	private static void appendTopLevelResults(StringBuffer sb, Results results,
			List<Category> categories, List<ReferenceResult> referenceResults, SaveReportType reportType, String ccdaDocumentType) {
		
		boolean isReferenceResultsEmpty = ApplicationUtil.isEmpty(referenceResults);
		int conformanceErrorCount = isReferenceResultsEmpty ? 0 : referenceResults.get(CONFORMANCE_ERROR_INDEX).getTotalErrorCount();
		int certificationFeedbackCount = isReferenceResultsEmpty ? 0 : referenceResults.get(CERTIFICATION_FEEDBACK_INDEX).getTotalErrorCount();
		if(reportType == SaveReportType.SUMMARY) {
			//brief summary of overall document results (without scorecard issues count listed)
			sb.append("<h3>Summary</h3>");
			sb.append("<p>"
					+ "Your " + ccdaDocumentType + " document received a grade of <b>" + results.getFinalGrade() + "</b>"
					+ " compared to an industry average of " + "<b>" + results.getIndustryAverageGradeForCcdaDocumentType() + "</b>" + ". "
					+ "The industry average, specific to the document type sent, was computed by scoring " 
					+ results.getNumberOfDocsScoredPerCcdaDocumentType() + " " + ccdaDocumentType + 
					(results.getNumberOfDocsScoredPerCcdaDocumentType() > 1 ? "s" : "" ) + ". " 
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
			appendHorizontalRuleWithBreaks(sb);
			sb.append("<br /><br />");
			sb.append("<h2>Scorecard Results by Clinical Domain</h2>");
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
		sb.append("<table id='dynamicTable'>");
		sb.append("   <tbody>");
		sb.append("    <tr class=\"popOuts\">");
		sb.append("      <td id=\"gradePopOut\" colspan=\"2\">");
		sb.append("        The Scorecard grade is a quantitative assessment of the data quality of the submitted document. "
				+ "A higher grade indicates that HL7 best practices for C-CDA implementation are being followed by the organization and "
				+ "has higher probability of being interoperable with other organizations.");
		sb.append("      </td>");
		sb.append("      <td id=\"issuePopOut\">");
		sb.append("        A Scorecard Issue identifies data within the document which can be represented in a better way using "
				+ "HL7 best practices for C-CDA. This column should have numbers as close to zero as possible.");
		sb.append("      </td>");
		sb.append("      <td id=\"errorPopOut\">");
		sb.append("        A Conformance Error implies that the document is non-compliant with the HL7 C-CDA IG requirements. "
				+ "This column should have zeros ideally. Providers should work with their health IT vendor to rectify the errors.");
		sb.append("      </td>");
		sb.append("      <td id=\"feedbackPopOut\">");
		sb.append("        A Certification Feedback result identifies areas where the generated documents are not compliant with "
				+ "the requirements of 2015 Edition Certification. Ideally, this column should have all zeros.");
		sb.append("      </td>");
		sb.append("    </tr>");
		sb.append("    <tr>");
		sb.append("      <td class=\"removeBorder\"></td>");
		sb.append("      <td class=\"removeBorder\" id=\"gradePopOutLink\"></td>");
		sb.append("      <td class=\"removeBorder\" id=\"issuePopOutLink\"></td>");
		sb.append("      <td class=\"removeBorder\" id=\"errorPopOutLink\"></td>");
		sb.append("      <td class=\"removeBorder\" id=\"feedbackPopOutLink\"></td>");
		sb.append("    </tr>    ");
		sb.append("    <tr style=\"background-color: ghostwhite\">");
		sb.append("     <th>Clinical Domain</th> ");
		sb.append("     <th id=\"gradeHeader\">Scorecard Grade</th> ");
		sb.append("     <th id=\"issueHeader\">Scorecard Issues</th> ");
		sb.append("     <th id=\"errorHeader\">Conformance Errors</th> ");
		sb.append("     <th id=\"feedbackHeader\">Certification Feedback</th> ");
		sb.append("    </tr> ");				
		for(Category category : getSortedCategories(categories)) {
//			if(category.getNumberOfIssues() > 0 || referenceResults.get(ReferenceInstanceType.IG/CERT).getTotalErrorCount() > 0) {
//			if(category.getNumberOfIssues() > 0 || category.isFailingConformance() || category.isCertificationFeedback()) {				
			sb.append("  <tr>")
		     .append("    <td>" + (category.getCategoryName() != null ? category.getCategoryName() : "Unknown") + "</td>")
		     .append("    <td>" + (category.getCategoryGrade() != null ? category.getCategoryGrade() : "N/A") + "</td>")
		     .append("    <td>" + (category.isFailingConformance() || category.isCertificationFeedback() || category.isNullFlavorNI() 
		    		 ? "N/A" 
		    		 : category.getNumberOfIssues()) 
		    		 + "</td>")
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
//			}
		}
		sb.append("   <tbody>");
		sb.append("</table>");
	}
	
	/**
	 * Order from best to worst:<br/>
	 * 1. Numerical score high to low 2. Empty/null 3. Certification Feedback 4. Conformance Error
	 * @param categories - Category List to sort
	 * @return sorted categories
	 */
	private static List<Category> getSortedCategories(List<Category> categories) {
		List<Category> sortedCategories = new ArrayList<Category>(categories);
		//prepare for sort of non-scored items
		for(Category category : sortedCategories) {
			if(category.isFailingConformance()) {
				category.setCategoryNumericalScore(-3);
			} else if(category.isCertificationFeedback()) {
				category.setCategoryNumericalScore(-2);
			} else if(category.isNullFlavorNI()) {
				category.setCategoryNumericalScore(-1);
			}
		}
		//sorts by numerical score via Comparable implementation in Category
		Collections.sort(sortedCategories, Collections.reverseOrder());
		return sortedCategories;
	}
	
	private static void appendHeatmap(StringBuffer sb, Results results,
			List<Category> categories, List<ReferenceResult> referenceResults) {
		
		sb.append("<span id='heatMap'>" + "</span>");
		for (Category curCategory : getSortedCategories(categories)) {
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
		  			boolean isCategoryNameValid = !ApplicationUtil.isEmpty(curCategory.getCategoryName());
			  		if(failingConformance && failingCertification || failingConformance && !failingCertification) {
				  		//we default to IG if true for both since IG is considered a more serious issue (same with the heatmap label, so we match that)
				  		//there could be a duplicate for two reasons, right now, there's always at least one duplicate since we derive ig from cert in the backend
				  		//in the future this might not be the case, but, there could be multiple section fails in the same section, so we have a default for those too
			  			int igErrorCount = isCategoryNameValid 
			  					? getFailingSectionSpecificErrorCount(curCategory.getCategoryName(), ReferenceInstanceType.IG_CONFORMANCE, referenceResults)
			  					: -1;
						sb.append("contains" + (isCategoryNameValid ? " <b>" + igErrorCount + "</b> " : "")
								+ "<a href='#" + ReferenceInstanceType.IG_CONFORMANCE.getTypePrettyName() 
								+ "-category'" + ">" + "Conformance Error" + (igErrorCount != 1 ? "s" : "") + "</a>");
					} else if(failingCertification && !failingConformance) {
						int certFeedbackCount = isCategoryNameValid
								? getFailingSectionSpecificErrorCount(curCategory.getCategoryName(), ReferenceInstanceType.CERTIFICATION_2015, referenceResults)
								: -1;
						sb.append("contains" + (isCategoryNameValid ? " <b>" + certFeedbackCount + "</b> " : "")
								+ "<a href='#" + ReferenceInstanceType.CERTIFICATION_2015.getTypePrettyName() 
								+ "-category'" + ">" + "Certification Feedback" + "</a>" + " result" + (certFeedbackCount != 1 ? "s" : ""));
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
		
		sb.append("<h3>");
		if("Scorecard Issues".equals(header) || result < 1 || reportType == SaveReportType.SUMMARY) {
			sb.append(header + ": " + result);
		} else {
			sb.append("<a href=\"#" + header + "-category\">" + header + ": " + result + "</a>");
		}
		sb.append("</h3>");
		
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

		for (Category curCategory : getSortedCategories(categories)) {
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
		//minimum breaks based on 11 categories
		sb.append("<br /><br /><br /><br /><br /><br /><br /><br /><br /><br /><br /><br /><br /><br /><br /><br /><br /><br />");
		sb.append("<h2>" + "Guide to Interpret the Scorecard Results Table" + "</h2>");		
		
		sb.append("<p>" + "The following sample table identifies how to use the C-CDA Scorecard results "
						+ "to improve the C-CDA documents generated by your organization. " 
		+ "Note: The table below is an example containing fictitious results "
		+ "and does not reflect C-CDAs generated by your organization." + "</p>");
		
		//static table
		sb.append("<table id=\"staticTable\">")
		     .append("  <tbody>")
		     .append("    <tr>")
		     .append("      <td class=\"removeBorder\"></td>")
		     .append("      <td class=\"removeBorder\"></td>")
		     .append("      <td class=\"removeBorder\"></td>")
		     .append("      <td class=\"removeBorder\"></td>")
		     .append("      <td id=\"issuePopOut\" rowspan=\"2\">A Scorecard Issue identifies data within the document which can be represented in a better way using HL7 best practices for C-CDA. This column should have numbers as close to zero as possible.</td>")
		     .append("      <td class=\"removeBorder\"></td>")
		     .append("      <td class=\"removeBorder\"></td>")
		     .append("      <td class=\"removeBorder\"></td>")
		     .append("      <td class=\"removeBorder\"></td>")
		     .append("    </tr>")
		     .append("    <tr>")
		     .append("      <td class=\"removeBorder\"></td>")
		     .append("      <td class=\"removeBorder\"></td>")
		     .append("      <td id=\"gradePopOut\" colspan=\"2\">The Scorecard grade is a quantitative assessment of the data quality of the submitted document. A higher grade indicates that HL7 best practices for C-CDA implementation are being followed by the organization and has higher probability of being interoperable")
		     .append("        with other organizations.</td>")
		     .append("      <td id=\"errorPopOut\" colspan=\"2\">A Conformance Error implies that the document is non-compliant with the HL7 C-CDA IG requirements. This column should have zeros ideally. Providers should work with their health IT vendor to rectify the errors.</td>")
		     .append("      <td class=\"removeBorder\"></td>")
		     .append("      <td class=\"removeBorder\"></td>")
		     .append("    </tr>")
		     .append("    <tr>")
		     .append("      <td class=\"removeBorder\"></td>")
		     .append("      <td class=\"removeBorder\"></td>")
		     .append("      <td class=\"removeBorder\"></td>")
		     .append("      <td id=\"gradePopOutLink\"></td>")
		     .append("      <td id=\"issuePopOutLink\"></td>")
		     .append("      <td id=\"errorPopOutLink\"></td>")
		     .append("      <td class=\"removeBorder\"></td>")
		     .append("      <td class=\"removeBorder\"></td>")
		     .append("      <td class=\"removeBorder\"></td>")
		     .append("    </tr>")
		     .append("    <tr>")
		     .append("      <td class=\"removeBorder\"></td>")
		     .append("      <td class=\"removeBorder\"></td>")
		     .append("      <th>Clinical Domain</th>")
		     .append("      <th id=\"gradeHeader\">Scorecard Grade</th>")
		     .append("      <th id=\"issueHeader\">Scorecard Issues</th>")
		     .append("      <th id=\"errorHeader\">Conformance Errors</th>")
		     .append("      <th id=\"feedbackHeader\">Certification Feedback</th>")
		     .append("      <td id=\"feedbackPopOutLink\"></td>")
		     .append("      <td id=\"feedbackPopOut\" rowspan=\"5\">A Certification Feedback result identifies areas where the generated documents are not compliant with the requirements of 2015 Edition Certification. Ideally, this column should have all zeros.</td>")
		     .append("    </tr>")
		     .append("    <tr>")
		     .append("      <td id=\"perfectRowPopOut\" rowspan=\"4\">The Problems row in this example has an A+ grade and all zeros across the board. This is the most desirable outcome for a Clinical Domain result set.</td>")
		     .append("      <td id=\"perfectRowPopOutLink\"></td>")
		     .append("      <td id=\"perfectRowLeftHeader\">Problems</td>")
		     .append("      <td class=\"perfectRowMiddleHeader\">A+</td>")
		     .append("      <td class=\"perfectRowMiddleHeader\">0</td>")
		     .append("      <td class=\"perfectRowMiddleHeader\">0</td>")
		     .append("      <td id=\"perfectRowRightHeader\">0</td>")
		     .append("      <td class=\"removeBorder\"></td>")
		     .append("    </tr>")
		     .append("    <tr>")
		     .append("      <td class=\"removeBorder\"></td>")
		     .append("      <td>Lab Results</td>")
		     .append("      <td>A-</td>")
		     .append("      <td>2</td>")
		     .append("      <td>0</td>")
		     .append("      <td>0</td>")
		     .append("      <td class=\"removeBorder\"></td>")
		     .append("    </tr>")
		     .append("    <tr>")
		     .append("      <td class=\"removeBorder\"></td>")
		     .append("      <td>Vital Signs</td>")
		     .append("      <td>A-</td>")
		     .append("      <td>1</td>")
		     .append("      <td>0</td>")
		     .append("      <td>0</td>")
		     .append("      <td class=\"removeBorder\"></td>")
		     .append("    </tr>")
		     .append("    <tr>")
		     .append("      <td class=\"removeBorder\"></td>")
		     .append("      <td>Encounters</td>")
		     .append("      <td>D</td>")
		     .append("      <td>12</td>")
		     .append("      <td>0</td>")
		     .append("      <td>0</td>")
		     .append("      <td class=\"removeBorder\"></td>")
		     .append("      <td class=\"removeBorder\"></td>")
		     .append("    </tr>")
		     .append("    <tr>")
		     .append("      <td class=\"removeBorder\"></td>")
		     .append("      <td class=\"removeBorder\"></td>")
		     .append("      <td id=\"notScoredRowLeftHeader\">Medications</td>")
		     .append("      <td class=\"notScoredRowMiddleHeader\">N/A</td>")
		     .append("      <td class=\"notScoredRowMiddleHeader\">N/A</td>")
		     .append("      <td class=\"notScoredRowMiddleHeader\">2</td>")
		     .append("      <td id=\"notScoredRowRightHeader\">0</td>")
		     .append("      <td id=\"notScoredRowPopOutLink\"></td>")
		     .append("      <td id=\"notScoredRowPopOut\" rowspan=\"3\">This domain was not scored because the document did not have data pertaining to the clinical domain or there were conformance errors or certification results.</td>")
		     .append("    </tr>")
		     .append("    <tr>")
		     .append("      <td class=\"removeBorder\"></td>")
		     .append("      <td class=\"removeBorder\"></td>")
		     .append("      <td>Allergies</td>")
		     .append("      <td>N/A</td>")
		     .append("      <td>N/A</td>")
		     .append("      <td>0</td>")
		     .append("      <td>4</td>")
		     .append("      <td class=\"removeBorder\"></td>")
		     .append("      <td class=\"removeBorder\"></td>")
		     .append("    </tr>")
		     .append("    <tr>")
		     .append("      <td class=\"removeBorder\"></td>")
		     .append("      <td class=\"removeBorder\"></td>")
		     .append("      <td>Immunizations</td>")
		     .append("      <td>N/A</td>")
		     .append("      <td>N/A</td>")
		     .append("      <td>0</td>")
		     .append("      <td>0</td>")
		     .append("      <td class=\"removeBorder\"></td>")
		     .append("      <td class=\"removeBorder\"></td>")
		     .append("    </tr>")
		     .append("  </tbody>")
		     .append("</table>");		
	}
	
	private static void appendPictorialGuideKey(StringBuffer sb) {
		sb.append("<h3>Additional Guidance to Interpret the Scorecard Results and Achieve Higher Grades</h3>")
	     .append("<p>")
	     .append("  <span id=\"keyGradeHeader\">Scorecard Grade: </span>")
	     .append("The Scorecard grade is a quantitative assessment of the data quality of the submitted document. "
	     		+ "A higher grade indicates that HL7 best practices for C-CDA implementation are being followed by the organization "
	     		+ "and has higher probability of being interoperable with other organizations. "
	     		+ "The grades are derived from the scores as follows: "
	     		+ "A+ ( > 94), A- ( 90 to 94), B+ (85 to 89), B- (80 to 84), C (70 to 79) and D (< 70).")
	     .append("</p>")
	     .append("<p>")
	     .append("  <span id=\"keyIssueHeader\">Scorecard Issues: </span>")
	     .append("A Scorecard Issue identifies data within the document which can be represented in a better way using HL7 best practices for C-CDA. "
	     		+ "This column should have numbers as close to zero as possible. "
	     		+ "The issues are counted for each occurrence of unimplemented best practice. "
	     		+ "For example, if a Vital Sign measurement is not using the appropriate UCUM units then each such occurrence would be flagged "
	     		+ "as an issue. A provider should work with their health IT vendor to better understand the source for why a best practice "
	     		+ "may not be implemented and then determine if it can be implemented in the future. Note: Scorecard Issues will be listed as "
	     		+ "'N/A' for a clinical domain, when there is no data for the domain or if there are conformance or certification feedback results.")
	     .append("</p>")
	     .append("<p>")
	     .append("  <span id=\"keyErrorHeader\">Conformance Errors: </span>")
	     .append("A Conformance Error implies that the document is non-compliant with the HL7 C-CDA IG requirements. "
	     		+ "This column should have zeros ideally. "
	     		+ "Providers should work with their health IT vendor to rectify the errors.")
	     .append("</p>")
	     .append("<p>")
	     .append("  <span id=\"keyFeedbackHeader\">Certification Feedback: </span>")
	     .append("A Certification Feedback result identifies areas where the generated documents are not compliant with "
	     		+ "the requirements of 2015 Edition Certification. Ideally, this column should have all zeros."
	     		+ "Most of these results fall into incorrect use of vocabularies and terminologies. "
	     		+ "Although not as severe as a Conformance Error, providers should work with their health IT vendor "
	     		+ "to address feedback provided to improve interoperable use of structured data between systems.")
	     .append("</p>");		
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
		if(!errorMessage.contains("HL7 CDA Schema Errors")) {
			sb.append("<p>" + ApplicationConstants.ErrorMessages.CONTACT + "</p>");
		}
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
			boolean hasSchemaErrors = report.getSchemaErrorList() != null && !report.getSchemaErrorList().isEmpty();
			if(!hasSchemaErrors) {
				appendErrorMessage(sb, report.getErrorMessage());
			} else {
				sb.append("<h3>" + ApplicationConstants.ErrorMessages.ONE_CLICK_SCHEMA_MESSAGE_PART1 + "</h3>");
				sb.append("<h3>" + ApplicationConstants.ErrorMessages.ONE_CLICK_SCHEMA_MESSAGE_PART2 + "</h3>");
				final String noData = "No data available";
				sb.append("<ol style='color:red'>");
				for(ReferenceError schemaError : report.getSchemaErrorList()) {
					sb.append("<li>");
					sb.append("<ul>");
					sb.append("<li>Message: " + (schemaError.getDescription() != null ? schemaError.getDescription() : noData));
					sb.append("</li>");
					sb.append("<li>Path: " + (schemaError.getxPath() != null ? schemaError.getxPath() : noData));
					sb.append("</li>");
					sb.append("<li>Line Number (approximate): " + 
							(schemaError.getDocumentLineNumber() != null ? schemaError.getDocumentLineNumber() : noData));
					sb.append("</li>");
					sb.append("<li>xPath: "
							+ "<xmp style='font-family: Consolas, monaco, monospace;'>"
							+ (schemaError.getxPath() != null ? schemaError.getxPath() : noData) + "</xmp>");
					sb.append("<p></p>");
					sb.append("</li>");
					sb.append("</ul>");
					sb.append("</li>");
				}
				sb.append("</ol>");
			}
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
			Document refinedDoc = builder.parse(new ByteArrayInputStream(
					cleanHtmlReport.getBytes("UTF-8")));

			ITextRenderer renderer = new ITextRenderer();
			renderer.setDocument(refinedDoc, null);
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
			if(LOG_HTML) {
				ApplicationUtil.debugLog("cleanHtmlReport", cleanHtmlReport);
			}
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
	
	public enum SaveReportType {
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
		String[] filenames = {"highScoringSample", "lowScoringSample", "sampleWithErrors", 
				"sampleWithSchemaErrors", "sampleWithoutAnyContent"};
		final int HIGH_SCORING_SAMPLE = 0, LOW_SCORING_SAMPLE = 1, SAMPLE_WITH_ERRORS = 2, 
				SAMPLE_WITH_SCHEMA_ERRORS = 3, SAMPLE_WITHOUT_ANY_CONTENT = 4;
		try {
			buildReportUsingJSONFromLocalFile(filenames[SAMPLE_WITH_ERRORS], SaveReportType.MATCH_UI);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}	

}
