package org.sitenv.service.ccda.smartscorecard.processor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.IOUtils;
import org.sitenv.ccdaparsing.model.CCDARefModel;
import org.sitenv.ccdaparsing.service.CCDAParserAPI;
import org.sitenv.ccdaparsing.util.PositionalXMLReader;
import org.sitenv.service.ccda.smartscorecard.model.Category;
import org.sitenv.service.ccda.smartscorecard.model.ReferenceError;
import org.sitenv.service.ccda.smartscorecard.model.ResponseTO;
import org.sitenv.service.ccda.smartscorecard.model.referencedto.ValidationResultsDto;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationConstants;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationUtil;
import org.springframework.beans.factory.annotation.Autowired;
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
	
	public ResponseTO processCCDAFile(MultipartFile ccdaFile)
	{
		ValidationResultsDto referenceValidatorResults;
		List<ReferenceError> schemaErrorList;
		ResponseTO scorecardResponse = new ResponseTO();
		List<String> errorSectionList;
		String birthDate = null;
		List<Category> categoryList = new ArrayList<Category>();
		String docType = null;
		try{
			CCDARefModel ccdaModels = CCDAParserAPI.parseCCDA2_1(ccdaFile.getInputStream());
			if (!ccdaModels.isEmpty())
			{
				referenceValidatorResults = callReferenceValidator(ccdaFile, ApplicationConstants.VALIDATION_OBJECTIVES.CCDA_IG_PLUS_VOCAB.getValidationObjective(), "No Scenario File");
				schemaErrorList = checkForSchemaErrors(referenceValidatorResults.getCcdaValidationResults()); 
				if(schemaErrorList.size() > 0)
				{
					scorecardResponse.setSchemaErrorList(schemaErrorList);
					scorecardResponse.setSchemaErrors(true);
					return scorecardResponse;
				}
				errorSectionList = getErrorSectionList(referenceValidatorResults.getCcdaValidationResults(), ccdaFile);
				docType = ApplicationUtil.checkDocType(ccdaModels);
				if(ccdaModels.getPatient() != null && ccdaModels.getPatient().getDob()!= null)
				{
					birthDate = ccdaModels.getPatient().getDob().getValue();
				}
			
				categoryList.add(patientScorecard.getPatientCategory(ccdaModels.getPatient(),docType));
				categoryList.add(miscScorecard.getMiscCategory(ccdaModels));
			
				for (Entry<String, String> entry : ApplicationConstants.SECTION_TEMPLATEID_MAP.entrySet()) {
					if(!errorSectionList.contains(entry.getKey()))
					{
						getSectionCategory(entry.getValue(),ccdaModels,birthDate,docType);
					}
				}
			}else
			{
				scorecardResponse.setSuccess(false);
				scorecardResponse.setErrorMessage(ApplicationConstants.EMPTY_DOC_ERROR_MESSAGE);
			}
			scorecardResponse.setFilename(ccdaFile.getOriginalFilename());
			
		}catch(Exception exc)
		{
			exc.printStackTrace();
		}
		return scorecardResponse;
	}
	
	public ValidationResultsDto callReferenceValidator(MultipartFile ccdaFile, String validationObjective, String referenceFileName)throws Exception
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
		referenceValidatorResults = restTemplate.postForObject(ApplicationConstants.REFERENCE_VALIDATOR_URL, requestEntity, ValidationResultsDto.class);
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
	
	public List<String> getErrorSectionList(List<ReferenceError> ccdaValidationResults,MultipartFile ccdaFile)throws SAXException,IOException,XPathExpressionException
	{
		List<String> errorSectionList = new ArrayList<>();
		Document doc = PositionalXMLReader.readXML(ccdaFile.getInputStream());
		XPath xPath = XPathFactory.newInstance().newXPath();
		Element errorElement;
		Element parentElement;
		Element templateId;
		for(ReferenceError referenceError : ccdaValidationResults)
		{
			errorElement = (Element) xPath.compile(referenceError.getxPath()).evaluate(doc, XPathConstants.NODE);
			parentElement = (Element)errorElement.getParentNode();
			System.out.println("Tag Name:"+ parentElement.getTagName());
			do {
				parentElement = (Element)parentElement.getParentNode();
		      }while( !parentElement.getTagName().equals("section"));
			
			if(parentElement.getTagName().equals("section"))
			{
				templateId = (Element) xPath.compile(ApplicationConstants.TEMPLATEID_XPATH).evaluate(doc, XPathConstants.NODE);
				errorSectionList.add(ApplicationConstants.SECTION_TEMPLATEID_MAP.get(templateId));
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
		}else 
		{
			return null;
		}
	}

}
