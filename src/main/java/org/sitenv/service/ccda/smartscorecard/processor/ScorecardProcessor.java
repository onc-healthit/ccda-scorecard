package org.sitenv.service.ccda.smartscorecard.processor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.sitenv.ccdaparsing.model.CCDARefModel;
import org.sitenv.ccdaparsing.model.UsrhSubType;
import org.sitenv.ccdaparsing.service.CCDAParserAPI;
import org.sitenv.ccdaparsing.util.PositionalXMLReader;
import org.sitenv.service.ccda.smartscorecard.model.Category;
import org.sitenv.service.ccda.smartscorecard.model.ReferenceError;
import org.sitenv.service.ccda.smartscorecard.model.ReferenceResult;
import org.sitenv.service.ccda.smartscorecard.model.ReferenceTypes.ValidationResultType;
import org.sitenv.service.ccda.smartscorecard.model.Results;
import org.sitenv.service.ccda.smartscorecard.model.ReferenceTypes.ReferenceInstanceType;
import org.sitenv.service.ccda.smartscorecard.model.ResponseTO;
import org.sitenv.service.ccda.smartscorecard.model.ScorecardProperties;
import org.sitenv.service.ccda.smartscorecard.model.referencedto.ResultMetaData;
import org.sitenv.service.ccda.smartscorecard.model.referencedto.ValidationResultsDto;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationConstants;
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
	
	private static final Logger logger = Logger.getLogger(ScorecardProcessor.class);
	
	public ResponseTO processCCDAFile(MultipartFile ccdaFile)
	{
		ValidationResultsDto referenceValidatorResults = null;
		ValidationResultsDto certificationResults;
		List<ReferenceError> schemaErrorList;
		ResponseTO scorecardResponse = new ResponseTO();
		List<String> errorSectionList = new ArrayList<>();
		String birthDate = null;
		List<Category> categoryList = new ArrayList<Category>();
		String docType = null;
		VALIDATION_OBJECTIVES validationObjective = null;
		Results results = new Results();
		try{
			CCDARefModel ccdaModels = CCDAParserAPI.parseCCDA2_1(ccdaFile.getInputStream());
			scorecardResponse.setFilename(ccdaFile.getOriginalFilename());
			if (!ccdaModels.isEmpty() && ccdaModels.getUsrhSubType() != UsrhSubType.UNSTRUCTURED_DOCUMENT)
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
						callReferenceValidator(ccdaFile, validationObjective.getValidationObjective(), "No Scenario File",scorecardProperties.getIgConformanceURL());
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
					if(referenceValidatorCallReturnedErrors)
					{
						errorSectionList = getErrorSectionList(getReferenceResults(referenceValidatorResults.getCcdaValidationResults(), 
								ReferenceInstanceType.IG_CONFORMANCE).getReferenceErrors(), ccdaFile);
					}
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
						callReferenceValidator(ccdaFile, validationObjective.getValidationObjective(), "No Scenario File",scorecardProperties.getCertificatinResultsURL());
				
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
					for (Iterator<ReferenceError> errorIterator = referenceValidatorResults
							.getCcdaValidationResults().iterator(); errorIterator.hasNext();) 
					{	
						ReferenceError currentError = errorIterator.next();
						if (currentError.getType() != ValidationResultType.CCDA_MDHT_CONFORMANCE_ERROR) 
						{
							errorIterator.remove();
						}
					}	
					
					// Store the 2 instances in the JSON (referenceResults array)
				     scorecardResponse.getReferenceResults().add(getReferenceResults(referenceValidatorResults.getCcdaValidationResults(), 
				       ReferenceInstanceType.IG_CONFORMANCE));
				     
				     
					     scorecardResponse.getReferenceResults().add((getReferenceResults(certificationResults.getCcdaValidationResults(), 
					       ReferenceInstanceType.CERTIFICATION_2015)));
				}
				
				docType = ApplicationUtil.checkDocType(ccdaModels);
				if(ccdaModels.getPatient() != null && ccdaModels.getPatient().getDob()!= null)
				{
					birthDate = ccdaModels.getPatient().getDob().getValue();
				}
			
				categoryList.add(miscScorecard.getMiscCategory(ccdaModels));
			
				for (Entry<String, String> entry : ApplicationConstants.SECTION_TEMPLATEID_MAP.entrySet()) {
					if(!errorSectionList.contains(entry.getValue()))
					{
						categoryList.add(getSectionCategory(entry.getValue(),ccdaModels,birthDate,docType));
					}else
					{
						categoryList.add(new Category(true,entry.getValue()));
					}
				}
			}else
			{
				String specificErrorReason= null;
				String errorMessage= null;
				scorecardResponse.setSuccess(false);
				if(ccdaModels.isEmpty()) 
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
			scoreCardStatisticProcessor.saveDetails(results,ccdaFile.getOriginalFilename());
			results.setIndustryAverageScore(scoreCardStatisticProcessor.calculateIndustryAverage());
			results.setNumberOfDocumentsScored(scoreCardStatisticProcessor.numberOfDocsScored());
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
		Element templateId;
		String sectionName;
		for(ReferenceError referenceError : ccdaValidationResults)
		{
			if(referenceError.getxPath()!= null && referenceError.getxPath()!= "")
			{
				if(!referenceError.getxPath().equals("/ClinicalDocument"))
				{
					errorElement = (Element) xPath.compile(referenceError.getxPath()).evaluate(doc, XPathConstants.NODE);
					parentElement = (Element)errorElement.getParentNode();
					while(!(parentElement.getTagName().equals("section") || parentElement.getTagName().equals("patientRole") || parentElement.getTagName().equals("ClinicalDocument")))
					{
						parentElement = (Element)parentElement.getParentNode();
					}
					
					if(parentElement.getTagName().equals("section"))
					{
						templateId = (Element) xPath.compile(ApplicationConstants.TEMPLATEID_XPATH).evaluate(parentElement, XPathConstants.NODE);
						sectionName = ApplicationConstants.SECTION_TEMPLATEID_MAP.get(templateId.getAttribute("root"));
						errorSectionList.add(sectionName);
						referenceError.setSectionName(sectionName);
					}else if(parentElement.getTagName().equals("patientRole"))
					{
						errorSectionList.add(ApplicationConstants.CATEGORIES.PATIENT.getCategoryDesc());
						referenceError.setSectionName(ApplicationConstants.CATEGORIES.PATIENT.getCategoryDesc());
					}
				}
			}
		}
		
		return errorSectionList;
	}
	
	public Category getSectionCategory(String sectionName,CCDARefModel ccdaModels, String birthDate, String docType )
	{
		if(sectionName.equalsIgnoreCase(ApplicationConstants.CATEGORIES.ALLERGIES.getCategoryDesc()))
		{
			return allergiesScorecard.getAllergiesCategory(ccdaModels.getAllergy(),birthDate,docType);
		}
		else if (sectionName.equalsIgnoreCase(ApplicationConstants.CATEGORIES.ENCOUNTERS.getCategoryDesc()))
		{
			return encountersScorecard.getEncounterCategory(ccdaModels.getEncounter(),birthDate,docType);
		}
		else if (sectionName.equalsIgnoreCase(ApplicationConstants.CATEGORIES.IMMUNIZATIONS.getCategoryDesc()))
		{
			return immunizationScorecard.getImmunizationCategory(ccdaModels.getImmunization(),birthDate,docType);
		}
		else if (sectionName.equalsIgnoreCase(ApplicationConstants.CATEGORIES.RESULTS.getCategoryDesc()))
		{
			 return labresultsScorecard.getLabResultsCategory(ccdaModels.getLabResults(),ccdaModels.getLabTests(),birthDate,docType);
		}
		else if (sectionName.equalsIgnoreCase(ApplicationConstants.CATEGORIES.MEDICATIONS.getCategoryDesc()))
		{
			return medicationScorecard.getMedicationCategory(ccdaModels.getMedication(),birthDate,docType);
		}
		else if (sectionName.equalsIgnoreCase(ApplicationConstants.CATEGORIES.PROBLEMS.getCategoryDesc()))
		{
			return problemsScorecard.getProblemsCategory(ccdaModels.getProblem(),birthDate,docType);
		}
		else if (sectionName.equalsIgnoreCase(ApplicationConstants.CATEGORIES.PROCEDURES.getCategoryDesc()))
		{
			return procedureScorecard.getProceduresCategory(ccdaModels.getProcedure(),birthDate,docType);
		}
		else if (sectionName.equalsIgnoreCase(ApplicationConstants.CATEGORIES.SOCIALHISTORY.getCategoryDesc()))
		{
			return socialhistoryScorecard.getSocialHistoryCategory(ccdaModels.getSmokingStatus(),birthDate,docType);
		}
		else if (sectionName.equalsIgnoreCase(ApplicationConstants.CATEGORIES.VITALS.getCategoryDesc()))
		{
			return vitalScorecard.getVitalsCategory(ccdaModels.getVitalSigns(),birthDate,docType);
		}
		else if (sectionName.equalsIgnoreCase(ApplicationConstants.CATEGORIES.PATIENT.getCategoryDesc()))
		{
			return patientScorecard.getPatientCategory(ccdaModels.getPatient(),docType);
		}
		else 
		{
			return null;
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