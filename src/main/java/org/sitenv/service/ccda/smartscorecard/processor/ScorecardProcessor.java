package org.sitenv.service.ccda.smartscorecard.processor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.sitenv.ccdaparsing.model.CCDARefModel;
import org.sitenv.ccdaparsing.model.UsrhSubType;
import org.sitenv.ccdaparsing.service.CCDAParserAPI;
import org.sitenv.ccdaparsing.util.PositionalXMLReader;
import org.sitenv.service.ccda.smartscorecard.model.CCDAScoreCardRubrics;
import org.sitenv.service.ccda.smartscorecard.model.Category;
import org.sitenv.service.ccda.smartscorecard.model.PatientDetails;
import org.sitenv.service.ccda.smartscorecard.model.ReferenceError;
import org.sitenv.service.ccda.smartscorecard.model.ReferenceResult;
import org.sitenv.service.ccda.smartscorecard.model.ReferenceTypes.ReferenceInstanceType;
import org.sitenv.service.ccda.smartscorecard.model.ReferenceTypes.ValidationResultType;
import org.sitenv.service.ccda.smartscorecard.model.ResponseTO;
import org.sitenv.service.ccda.smartscorecard.model.Results;
import org.sitenv.service.ccda.smartscorecard.model.ScorecardProperties;
import org.sitenv.service.ccda.smartscorecard.cofiguration.ScorecardConfigurationLoader;
import org.sitenv.service.ccda.smartscorecard.cofiguration.ScorecardSection;
import org.sitenv.service.ccda.smartscorecard.cofiguration.SectionRule;
import org.sitenv.service.ccda.smartscorecard.model.referencedto.ResultMetaData;
import org.sitenv.service.ccda.smartscorecard.model.referencedto.ValidationResultsDto;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationConstants;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationConstants.CATEGORIES;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationConstants.VALIDATION_OBJECTIVES;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

@Service
public class ScorecardProcessor {
	
	@Autowired
	VitalsScorecard vitalScorecard;
	
	@Autowired
	AllergiesScorecard allergiesScorecard;
	
	@Autowired
	EncounterScorecard encountersScorecard;
	
	@Autowired
	ImmunizationScorecard immunizationScorecard;
	
	@Autowired
	LabresultsScorecard labresultsScorecard;
	
	@Autowired
	MedicationScorecard medicationScorecard;
	
	@Autowired
	ProblemsScorecard problemsScorecard;
	
	@Autowired
	SocialHistoryScorecard socialhistoryScorecard;
	
	@Autowired
	PatientScorecard patientScorecard;
	
	@Autowired
	ProceduresScorecard procedureScorecard;
	
	@Autowired
	MiscScorecard miscScorecard;
	
	@Autowired
	ScoreCardStatisticProcessor scoreCardStatisticProcessor;
	
	@Autowired
	@Qualifier("scorecardProperties")
	ScorecardProperties scorecardProperties;
	
	@Autowired
	@Qualifier("scorecardConfigurationLoader")
	ScorecardConfigurationLoader scorecardConfigurationLoader;
	
	@Autowired
	ScorecardExcelGenerator scorecardExcelGenerator;
	
	private static final Logger logger = Logger.getLogger(ScorecardProcessor.class);
	
	public ResponseTO processCCDAFile(MultipartFile ccdaFile,boolean isOneClickScorecard)
	{
		ValidationResultsDto referenceValidatorResults = null;
		ValidationResultsDto certificationResults;
		List<ReferenceError> schemaErrorList;
		ResponseTO scorecardResponse = new ResponseTO();
		List<String> errorSectionList = new ArrayList<>();
		List<String> certSectionList = new ArrayList<>();
		PatientDetails patientDetails = null;
		List<Category> categoryList = new ArrayList<Category>();
		String docType = null;
		VALIDATION_OBJECTIVES validationObjective = null;
		Results results = new Results();
		List<ScorecardSection> scorecardSections=null;
		try{
			CCDARefModel ccdaModels = CCDAParserAPI.parseCCDA2_1(ccdaFile.getInputStream());
			scorecardResponse.setFilename(ccdaFile.getOriginalFilename());
			boolean ccdaModelsIsEmpty = ccdaModels.isEmpty();
			
			if(scorecardConfigurationLoader!=null && scorecardConfigurationLoader.getConfigurations()!=null){
				scorecardSections = scorecardConfigurationLoader.getConfigurations().getScorecardSections();
			}
			if(!ccdaModelsIsEmpty && ccdaModels.getUsrhSubType() != null) 
			{
				scorecardResponse.setCcdaDocumentType(ccdaModels.getUsrhSubType().getName());
			}
			if (!ccdaModelsIsEmpty && ccdaModels.getUsrhSubType() != UsrhSubType.UNSTRUCTURED_DOCUMENT)
			{	
				boolean referenceValidatorCallReturnedErrors = false;
				if(scorecardProperties.getIgConformanceCall())
				{
					validationObjective = 
						determineValidationObjectiveType(ccdaModels, ReferenceInstanceType.IG_CONFORMANCE);
					logger.info("Calling ReferenceInstanceType.IG_CONFORMANCE with:" + System.lineSeparator()
						+ "validationObjective: " + validationObjective.getValidationObjective()
						+ " determined by ccdaModels.getUsrhSubType(): " + ccdaModels.getUsrhSubType());
					referenceValidatorResults = 
						callReferenceValidator(ccdaFile, validationObjective.getValidationObjective(), 
								"No Scenario File", scorecardProperties.getIgConformanceURL());
					schemaErrorList = checkForSchemaErrors(referenceValidatorResults.getCcdaValidationResults());
				
					if(schemaErrorList.size() > 0)
					{
						scorecardResponse.setSchemaErrorList(schemaErrorList);
						scorecardResponse.setSchemaErrors(true);
						scorecardResponse.setErrorMessage(ApplicationConstants.ErrorMessages.SCHEMA_ERRORS_GENERIC);
						logger.info("Halting collection and processing of more results due to schema errors found in first instance");
						return scorecardResponse;
					}
				
					referenceValidatorCallReturnedErrors = 
							checkForReferenceValidatorErrors(referenceValidatorResults.getResultsMetaData().getResultMetaData());
				}
				
				// Commenting 2nd call for now as the results are currently the same as the 1st
				/*				
				if(scorecardProperties.getCertificationResultsCall())
				{
					validationObjective = determineValidationObjectiveType(ccdaModels, ReferenceInstanceType.CERTIFICATION_2015);
					logger.info("Calling ReferenceInstanceType.CERTIFICATION_2015 with:" + System.lineSeparator()
						+ "validationObjective: " + validationObjective.getValidationObjective()
						+ " determined by ccdaModels.getUsrhSubType(): " + ccdaModels.getUsrhSubType());
					certificationResults = 
						callReferenceValidator(ccdaFile, validationObjective.getValidationObjective(), 
								"No Scenario File", scorecardProperties.getCertificatinResultsURL());
				
					if(checkForReferenceValidatorErrors(certificationResults.getResultsMetaData().getResultMetaData()))
					{
						scorecardResponse.getReferenceResults().add((getReferenceResults(certificationResults.getCcdaValidationResults(), 
																								ReferenceInstanceType.CERTIFICATION_2015)));
					}
				}
				*/
				
				if(referenceValidatorCallReturnedErrors) 
				{				
					if(scorecardProperties.getCertificationResultsCall())
					{
						// Copy results from referenceValidatorResults (IG_CONFORMANCE) to certificationResults (CERTIFICATION_2015)
						certificationResults = new ValidationResultsDto(referenceValidatorResults);
					}else 
					{
						certificationResults = new ValidationResultsDto();
					}
					
					// Remove non-IG based results from referenceValidatorResults (IG_CONFORMANCE)					
					removeExcessResults(referenceValidatorResults, ReferenceInstanceType.IG_CONFORMANCE);
					// Remove IG based results from certificationResults (CERTIFICATION_2015)
					removeExcessResults(certificationResults, ReferenceInstanceType.CERTIFICATION_2015);												
					
					if(scorecardProperties.getIgConformanceCall()) 
					{
						// set IG errors AFTER non-IG results are removed so current cert results aren't flagged as failingConformance						
						errorSectionList = getErrorSectionList(getReferenceResults(referenceValidatorResults.getCcdaValidationResults(), 
								ReferenceInstanceType.IG_CONFORMANCE).getReferenceErrors(), ccdaFile);
					}
					if(scorecardProperties.getCertificationResultsCall())
					{
						certSectionList = getErrorSectionList(getReferenceResults(certificationResults.getCcdaValidationResults(), 
								ReferenceInstanceType.CERTIFICATION_2015).getReferenceErrors(), ccdaFile);
					}
					
					// Store the 2 instances in the JSON (referenceResults array)
					scorecardResponse.getReferenceResults().add(getReferenceResults(referenceValidatorResults.getCcdaValidationResults(),
									ReferenceInstanceType.IG_CONFORMANCE));
					scorecardResponse.getReferenceResults().add((getReferenceResults(certificationResults.getCcdaValidationResults(),
									ReferenceInstanceType.CERTIFICATION_2015)));
				}
				
				docType = ApplicationUtil.checkDocType(ccdaModels);
				if(ccdaModels.getPatient() != null)
				{
					patientDetails = new PatientDetails();
					if(ccdaModels.getPatient().getDob()!= null)
					{
						patientDetails.setPatientDob(ccdaModels.getPatient().getDob().getValue());
						patientDetails.setDobValid(ApplicationUtil.validateBirthDate(ccdaModels.getPatient().getDob().getValue()));
					}
					
					if(ccdaModels.getPatient().getDod()!= null)
					{
						patientDetails.setDodPresent(ccdaModels.getPatient().getDod().getValuePresent());
						patientDetails.setPatientDod(ccdaModels.getPatient().getDod().getValue());
						patientDetails.setDodValid(patientDetails.isDobValid()? ApplicationUtil.isDodValid(patientDetails.getPatientDob(),
																					ccdaModels.getPatient().getDod().getValue()):false);
					}
					
				}
				
				List<SectionRule> sectionRules=null;
				if(scorecardSections!=null){
					sectionRules= ApplicationUtil.getSectionRules(scorecardSections, CATEGORIES.MISC.getCategoryDesc());
				}
				
				categoryList.add(miscScorecard.getMiscCategory(ccdaModels,sectionRules));
				Category scorecardCategory = null;
				ApplicationUtil.debugLog("certSectionList", certSectionList.toString());
				for (Entry<String, String> entry : ApplicationConstants.SECTION_TEMPLATEID_MAP.entrySet()) 
				{
					if(!errorSectionList.contains(entry.getValue()))
					{
						if(scorecardSections!=null){
							sectionRules= ApplicationUtil.getSectionRules(scorecardSections, entry.getValue());
						}
						scorecardCategory = getSectionCategory(entry.getValue(),ccdaModels,patientDetails,docType,sectionRules);
						if(scorecardCategory!= null)
						{
							categoryList.add(scorecardCategory);
						}
					}else
					{
						categoryList.add(new Category(true,entry.getValue()));
					}
					
					
					if(certSectionList.contains(entry.getValue()))
					{						
						int indexOfScorecardCategory = -1;
						for(int i = 0; i < categoryList.size(); i++) {
							String curCatName = categoryList.get(i).getCategoryName();
							if(entry.getValue().equals(curCatName)) {
								indexOfScorecardCategory = i;
								break;
							}
						}						
						if(indexOfScorecardCategory != -1)
						{							
							Category curCat = categoryList.get(indexOfScorecardCategory);
							curCat.setCertificationFeedback(true);
							curCat.setCategoryGrade(null);
							curCat.setCategoryNumericalScore(0);
							curCat.setCategoryRubrics(new ArrayList<CCDAScoreCardRubrics>());
							curCat.setNumberOfIssues(0);
						}
					}
					
				}
			}else
			{
				String specificErrorReason= null;
				String errorMessage= null;
				scorecardResponse.setSuccess(false);
				if(ccdaModelsIsEmpty) 
				{
					errorMessage = ApplicationConstants.EMPTY_DOC_ERROR_MESSAGE;
					specificErrorReason = "empty model";
				} else if(ccdaModels.getUsrhSubType() == UsrhSubType.UNSTRUCTURED_DOCUMENT) 
				{
					errorMessage = ApplicationConstants.ErrorMessages.UNSTRUCTURED_DOCUMENT;
					specificErrorReason = "Unstructured Document type";
				}
				scorecardResponse.setErrorMessage(errorMessage);
				logger.warn("Skipped ReferenceInstanceType calls due to: " + specificErrorReason
						+ ", and applied an appropriate error message to the response");
			    return scorecardResponse;
			}
			results.setCategoryList(categoryList);
			ApplicationUtil.calculateFinalGradeAndIssues(categoryList, results);
			results.setIgReferenceUrl(ApplicationConstants.IG_URL);
			results.setDocType(docType);
			String ccdaDocumentType = "Unknown";
			if(!ApplicationUtil.isEmpty(scorecardResponse.getCcdaDocumentType()))
			{
				ccdaDocumentType = scorecardResponse.getCcdaDocumentType();
			}
			scoreCardStatisticProcessor.saveDetails(results,ccdaFile.getOriginalFilename(),isOneClickScorecard,ccdaDocumentType);					
			results.setIndustryAverageScore(scoreCardStatisticProcessor.calculateIndustryAverage(isOneClickScorecard));
			results.setNumberOfDocumentsScored(scoreCardStatisticProcessor.numberOfDocsScored(isOneClickScorecard));
			results.setNumberOfDocsScoredPerCcdaDocumentType(
					scoreCardStatisticProcessor.numberOfDocsScoredPerCcdaDocumentType(ccdaDocumentType,isOneClickScorecard));
			if(results.getIndustryAverageScore() != 0)
			{
				results.setIndustryAverageGrade(ApplicationUtil.calculateIndustryAverageGrade(results.getIndustryAverageScore()));
			}else 
			{
				results.setIndustryAverageGrade("N/A");
			}
			scorecardResponse.setResults(results);
			scorecardResponse.setSuccess(true);
			
		}catch(Exception exc)
		{
			logger.error("Exception while processing CCDA DOC", exc);
			scorecardResponse.setSuccess(false);
			scorecardResponse.setErrorMessage(ApplicationConstants.ErrorMessages.GENERIC_WITH_CONTACT);
		}
		return scorecardResponse;
	}
	
	public ValidationResultsDto callReferenceValidator(MultipartFile ccdaFile, String validationObjective, String referenceFileName,String referenceValidatorUrl)throws Exception
	{
		LinkedMultiValueMap<String, Object> requestMap = new LinkedMultiValueMap<String, Object>();
		ValidationResultsDto referenceValidatorResults = null;
		File tempFile = File.createTempFile("ccda", "File");
		FileOutputStream out = new FileOutputStream(tempFile);
		IOUtils.copy(ccdaFile.getInputStream(), out);
		requestMap.add("ccdaFile", new FileSystemResource(tempFile));
			
		requestMap.add("validationObjective", validationObjective);
		requestMap.add("referenceFileName", referenceFileName);
			
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
	
		HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<LinkedMultiValueMap<String, Object>>(
																					requestMap, headers);
		RestTemplate restTemplate = new RestTemplate();
		FormHttpMessageConverter formConverter = new FormHttpMessageConverter();
		formConverter.setCharset(Charset.forName("UTF8"));
		restTemplate.getMessageConverters().add(formConverter);
		restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
		referenceValidatorResults = restTemplate.postForObject(referenceValidatorUrl, requestEntity, ValidationResultsDto.class);
		tempFile.delete();
		
		return referenceValidatorResults;
	}
	
	public List<ReferenceError> checkForSchemaErrors(List<ReferenceError> ccdaValidationResults)
	{
		List<ReferenceError> schemaErrorList = new ArrayList<>();
		for(ReferenceError referenceError : ccdaValidationResults)
		{
			if(referenceError.getSchemaError())
			{
				schemaErrorList.add(referenceError);
			}
		}
		return schemaErrorList;
	}
	
	public boolean checkForReferenceValidatorErrors(List<ResultMetaData> resultMetaData)
	{
		for(ResultMetaData result : resultMetaData)
		{
			if(ApplicationConstants.referenceValidatorErrorList.contains(result.getType()) && result.getCount() > 0)
			{
				return true;
			}
		}
		return false;
	}
	
	public ReferenceResult getReferenceResults(List<ReferenceError> referenceValidatorErrors, ReferenceInstanceType instanceType)
	{
		ReferenceResult results = new ReferenceResult();
		results.setType(instanceType);
		List<ReferenceError> referenceErrors = new ArrayList<>();
		if(referenceValidatorErrors != null)
		{
			for(ReferenceError error : referenceValidatorErrors)
			{
				if(ApplicationConstants.referenceValidatorErrorList.contains(error.getType().getTypePrettyName()))
				{
					referenceErrors.add(error);
				}
			}
		}
		results.setReferenceErrors(referenceErrors);
		results.setTotalErrorCount(referenceErrors.size());
		return results;
	}
	
	public List<String> getErrorSectionList(List<ReferenceError> ccdaValidationResults,MultipartFile ccdaFile)throws SAXException,IOException,XPathExpressionException
	{
		List<String> errorSectionList = new ArrayList<>();
		Document doc = PositionalXMLReader.readXML(ccdaFile.getInputStream());
		XPath xPath = XPathFactory.newInstance().newXPath();
		Element errorElement;
		Element parentElement= null;
		String sectionName;
		for(ReferenceError referenceError : ccdaValidationResults)
		{
			if(referenceError.getxPath()!= null && referenceError.getxPath()!= "")
			{
				if(!referenceError.getxPath().equals("/ClinicalDocument"))
				{
					errorElement = (Element) xPath.compile(referenceError.getxPath()).evaluate(doc, XPathConstants.NODE);
					if(errorElement!= null)
					{
						if(errorElement.getTagName().equalsIgnoreCase("section")|| errorElement.getTagName().equalsIgnoreCase("patientRole"))
						{
							sectionName = getSectionName(errorElement);
							if(sectionName!= null)
							{
								errorSectionList.add(sectionName);
								referenceError.setSectionName(sectionName);
							}
						}
						else
						{
							parentElement = (Element)errorElement.getParentNode();
							while(!(parentElement.getTagName().equalsIgnoreCase("section") || parentElement.getTagName().equalsIgnoreCase("patientRole") || 
																							parentElement.getTagName().equalsIgnoreCase("ClinicalDocument")))
							{
								parentElement = (Element)parentElement.getParentNode();
							}
							if(parentElement.getTagName().equals("section")|| parentElement.getTagName().equalsIgnoreCase("patientRole"))
							{
								sectionName = getSectionName(parentElement);
								if(sectionName!= null)
								{
									errorSectionList.add(sectionName);
									referenceError.setSectionName(sectionName);
								}
							}
						}
					}
				}
			}
		}
		return errorSectionList;
	}
	
	public String getSectionName(Element errorElement)throws SAXException,IOException,XPathExpressionException
	{
		Element templateId;
		String sectionName=null;
		XPath xPath = XPathFactory.newInstance().newXPath();
		if(errorElement.getTagName().equals("section"))
		{
			templateId = (Element) xPath.compile(ApplicationConstants.TEMPLATEID_XPATH).evaluate(errorElement, XPathConstants.NODE);
			sectionName = ApplicationConstants.SECTION_TEMPLATEID_MAP.get(templateId.getAttribute("root"));
		}
		else if(errorElement.getTagName().equals("patientRole"))
		{
			sectionName = ApplicationConstants.CATEGORIES.PATIENT.getCategoryDesc();
		}
		return sectionName;
	}
	
	public Category getSectionCategory(String sectionName,CCDARefModel ccdaModels, PatientDetails patientDetails, String docType,List<SectionRule> sectionRules)
	{
		if(sectionName.equalsIgnoreCase(CATEGORIES.ALLERGIES.getCategoryDesc()))
		{
			return allergiesScorecard.getAllergiesCategory(ccdaModels.getAllergy(),patientDetails,docType,sectionRules);
		}
		else if (sectionName.equalsIgnoreCase(CATEGORIES.ENCOUNTERS.getCategoryDesc()))
		{
			return encountersScorecard.getEncounterCategory(ccdaModels.getEncounter(),patientDetails,docType,sectionRules);
		}
		else if (sectionName.equalsIgnoreCase(CATEGORIES.IMMUNIZATIONS.getCategoryDesc()))
		{
			return immunizationScorecard.getImmunizationCategory(ccdaModels.getImmunization(),patientDetails,docType,sectionRules);
		}
		else if (sectionName.equalsIgnoreCase(ApplicationConstants.CATEGORIES.RESULTS.getCategoryDesc()))
		{
			 return labresultsScorecard.getLabResultsCategory(ccdaModels.getLabResults(),ccdaModels.getLabTests(),patientDetails,docType,sectionRules);
		}
		else if (sectionName.equalsIgnoreCase(ApplicationConstants.CATEGORIES.MEDICATIONS.getCategoryDesc()))
		{
			return medicationScorecard.getMedicationCategory(ccdaModels.getMedication(),patientDetails,docType,sectionRules);
		}
		else if (sectionName.equalsIgnoreCase(ApplicationConstants.CATEGORIES.PROBLEMS.getCategoryDesc()))
		{
			return problemsScorecard.getProblemsCategory(ccdaModels.getProblem(),patientDetails,docType,sectionRules);
		}
		else if (sectionName.equalsIgnoreCase(ApplicationConstants.CATEGORIES.PROCEDURES.getCategoryDesc()))
		{
			return procedureScorecard.getProceduresCategory(ccdaModels.getProcedure(),patientDetails,docType,sectionRules);
		}
		else if (sectionName.equalsIgnoreCase(ApplicationConstants.CATEGORIES.SOCIALHISTORY.getCategoryDesc()))
		{
			return socialhistoryScorecard.getSocialHistoryCategory(ccdaModels.getSmokingStatus(),patientDetails,docType,sectionRules);
		}
		else if (sectionName.equalsIgnoreCase(ApplicationConstants.CATEGORIES.VITALS.getCategoryDesc()))
		{
			return vitalScorecard.getVitalsCategory(ccdaModels.getVitalSigns(),patientDetails,docType,sectionRules);
		}
		else if (sectionName.equalsIgnoreCase(ApplicationConstants.CATEGORIES.PATIENT.getCategoryDesc()))
		{
			return patientScorecard.getPatientCategory(ccdaModels.getPatient(),docType,sectionRules);
		}
		else 
		{
			return null;
		}
	}
	
	
	public HSSFWorkbook generateScorecardData(String fromDate, String toDate) throws IOException, ParseException {
		if(fromDate!=null && toDate!= null){
			if(ApplicationUtil.validateDatesForExcelGeneration(fromDate, toDate)){
				return scorecardExcelGenerator.exportToExcel(scoreCardStatisticProcessor.
						getAllRecordsForDateRange(Timestamp.valueOf(fromDate + " 00:00:00"), Timestamp.valueOf(toDate + " 23:59:59")));
			}else{
				throw new IllegalArgumentException("Invalid Fromdate and Todate values");
			}
		}else if (fromDate!= null){
			String currentDate = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
			if(ApplicationUtil.validateFromDateForExcelGeneration(fromDate)){
				return scorecardExcelGenerator.exportToExcel(scoreCardStatisticProcessor.
						getAllRecordsForDateRange(Timestamp.valueOf(fromDate + " 00:00:00"), Timestamp.valueOf(currentDate + " 23:59:59")));
			}else{
				throw new IllegalArgumentException("Invalid Fromdate value");
			}
		}else{
			return scorecardExcelGenerator.exportToExcel(scoreCardStatisticProcessor.getAllRecords());
		}
	}
	
	private static void removeExcessResults(ValidationResultsDto results, ReferenceInstanceType referenceInstanceType) {
		if(referenceInstanceType == null) return;
		for (Iterator<ReferenceError> errorIterator = results
				.getCcdaValidationResults().iterator(); errorIterator.hasNext();) 
		{	
			ReferenceError currentError = errorIterator.next();
			if(referenceInstanceType == ReferenceInstanceType.IG_CONFORMANCE) {
				if (currentError.getType() != ValidationResultType.CCDA_MDHT_CONFORMANCE_ERROR) 
				{
					errorIterator.remove();
				}
			} else if(referenceInstanceType == ReferenceInstanceType.CERTIFICATION_2015) {
				if (currentError.getType() == ValidationResultType.CCDA_MDHT_CONFORMANCE_ERROR) 
				{
					errorIterator.remove();
				}
			}
		}						
	}	
	
	private static VALIDATION_OBJECTIVES determineValidationObjectiveType(
			CCDARefModel ccdaModels, ReferenceInstanceType refType) {		
		if (refType != null) {
			switch (refType) {
			case IG_CONFORMANCE:
				return VALIDATION_OBJECTIVES.CCDA_IG_PLUS_VOCAB;
			case CERTIFICATION_2015:
				if (ccdaModels.getUsrhSubType() != null) {
					switch (ccdaModels.getUsrhSubType()) {
					case CARE_PLAN:
						return VALIDATION_OBJECTIVES.CERTIFICATION_B9_CP_OBJECTIVE;
					case CONTINUITY_OF_CARE_DOCUMENT:
					case DISCHARGE_SUMMARY:
					case REFERRAL_NOTE:
					case TRANSFER_SUMMARY:
						return VALIDATION_OBJECTIVES.CERTIFICATION_B1_CCD_DS_RN_OBJECTIVE;
					case CONSULTATION_NOTE:
					case DIAGNOSTIC_IMAGING_REPORT:
					case HISTORY_AND_PHYSICAL_NOTE:
					case OPERATIVE_NOTE:
					case PROCEDURE_NOTE:
					case PROGRESS_NOTE:
					case US_REALM_HEADER_PATIENT_GENERATED_DOCUMENT:
						return VALIDATION_OBJECTIVES.CCDA_IG_PLUS_VOCAB;
					case UNSTRUCTURED_DOCUMENT:
						return null; //Reject Unstructured Document since we cannot score them
					}
				}
				//a null ccdaModels.getUsrhSubType() means a valid document type was not found in the XML
				//we don't reject documents without a type, only identified Unstructured Documents				
				return VALIDATION_OBJECTIVES.CCDA_IG_PLUS_VOCAB;
			}
		}
		throw new NullReferenceInstanceTypeArgumentException(
				"The ReferenceInstanceType was null at compile time (but we can only check at runtime). "
				+ "This is a programmer error since it should have been specified as an argument "
				+ "and should not make it to production as is.");
	}
}

class NullReferenceInstanceTypeArgumentException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public NullReferenceInstanceTypeArgumentException(String message) {
		super(message);
	}
}