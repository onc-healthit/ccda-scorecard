package org.sitenv.service.ccda.smartscorecard.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.sitenv.ccdaparsing.model.CCDARefModel;
import org.sitenv.ccdaparsing.service.CCDAParserAPI;
import org.sitenv.service.ccda.smartscorecard.model.Category;
import org.sitenv.service.ccda.smartscorecard.model.ResponseTO;
import org.sitenv.service.ccda.smartscorecard.model.Results;
import org.sitenv.service.ccda.smartscorecard.processor.AllergiesScorecard;
import org.sitenv.service.ccda.smartscorecard.processor.EncounterScorecard;
import org.sitenv.service.ccda.smartscorecard.processor.ImmunizationScorecard;
import org.sitenv.service.ccda.smartscorecard.processor.LabresultsScorecard;
import org.sitenv.service.ccda.smartscorecard.processor.MedicationScorecard;
import org.sitenv.service.ccda.smartscorecard.processor.MiscScorecard;
import org.sitenv.service.ccda.smartscorecard.processor.PatientScorecard;
import org.sitenv.service.ccda.smartscorecard.processor.ProblemsScorecard;
import org.sitenv.service.ccda.smartscorecard.processor.ProceduresScorecard;
import org.sitenv.service.ccda.smartscorecard.processor.ScoreCardStatisticProcessor;
import org.sitenv.service.ccda.smartscorecard.processor.SocialHistoryScorecard;
import org.sitenv.service.ccda.smartscorecard.processor.VitalsScorecard;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class CcdaSmartScorecardController {
	
	
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
	
	@RequestMapping(value="/ccdascorecardservice", method= RequestMethod.POST)
	public @ResponseBody ResponseTO ccdascorecardservice(@RequestParam("ccdaFile") MultipartFile ccdaFile){
		
		
		ResponseTO response = new ResponseTO();
		String birthDate = null;
		Results results = new Results();
		
		try
		{
			CCDARefModel ccdaModels = CCDAParserAPI.parseCCDA2_1(ccdaFile.getInputStream());
			if(ccdaModels.getPatient() != null && ccdaModels.getPatient().getDob()!= null)
			{
				birthDate = ccdaModels.getPatient().getDob().getValue();
			}
			List<Category> categoryList = new ArrayList<Category>();
			categoryList.add(patientScorecard.getPatientCategory(ccdaModels.getPatient()));
			categoryList.add(encountersScorecard.getEncounterCategory(ccdaModels.getEncounter(),birthDate));
			categoryList.add(allergiesScorecard.getAllergiesCategory(ccdaModels.getAllergy(),birthDate));
			categoryList.add(problemsScorecard.getProblemsCategory(ccdaModels.getProblem(),birthDate));
			categoryList.add(medicationScorecard.getMedicationCategory(ccdaModels.getMedication(),birthDate));
			categoryList.add(immunizationScorecard.getImmunizationCategory(ccdaModels.getImmunization(),birthDate));
			categoryList.add(socialhistoryScorecard.getSocialHistoryCategory(ccdaModels.getSmokingStatus(),birthDate));
			categoryList.add(labresultsScorecard.getLabResultsCategory(ccdaModels.getLabResults(),ccdaModels.getLabTests(),birthDate));
			categoryList.add(vitalScorecard.getVitalsCategory(ccdaModels.getVitalSigns(),birthDate));
			categoryList.add(procedureScorecard.getProceduresCategory(ccdaModels.getProcedure(),birthDate));
			categoryList.add(miscScorecard.getMiscCategory(ccdaModels));
			
			results.setCategoryList(categoryList);
			ApplicationUtil.calculateFinalGradeAndIssues(categoryList, results);
			results.setIgReferenceUrl(ApplicationConstants.IG_URL);
			results.setDocType(ApplicationUtil.checkDocType(ccdaModels));
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
			response.setSuccess(true);
			response.setResults(results);
		}catch(Exception excp)
		{
			excp.printStackTrace();
			response.setSuccess(false);
		}
		return response;
	}
	
	
	@RequestMapping(value="/ccdavalidatorservice", method= RequestMethod.POST)
	public @ResponseBody String ccdavalidatorservice(@RequestParam("ccdaFile") MultipartFile ccdaFile, @RequestParam("validationObjective")String validationObjective,
													 @RequestParam("referenceFileName")String referenceFileName, @RequestParam("debug_mode")String debug_mode){
		LinkedMultiValueMap<String, Object> requestMap = new LinkedMultiValueMap<String, Object>();
		String response = "";
		try{
			File tempFile = File.createTempFile("ccda", "File");
			FileOutputStream out = new FileOutputStream(tempFile);
			IOUtils.copy(ccdaFile.getInputStream(), out);
			requestMap.add("ccdaFile", new FileSystemResource(tempFile));
			requestMap.add("validationObjective", validationObjective);
			requestMap.add("referenceFileName", referenceFileName);
			requestMap.add("debug_mode", debug_mode);
			
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.MULTIPART_FORM_DATA);
	
			HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<LinkedMultiValueMap<String, Object>>(
																					requestMap, headers);
			RestTemplate restTemplate = new RestTemplate();
		    FormHttpMessageConverter formConverter = new FormHttpMessageConverter();
		    formConverter.setCharset(Charset.forName("UTF8"));
		    restTemplate.getMessageConverters().add(formConverter);
		    restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
		    response = restTemplate.postForObject(ApplicationConstants.REFERENCE_VALIDATOR_URL, requestEntity, String.class);
		    tempFile.delete();
		}catch(Exception exc)
		{
			exc.printStackTrace();
		}
		
	    return response;
	}
	
	

}
