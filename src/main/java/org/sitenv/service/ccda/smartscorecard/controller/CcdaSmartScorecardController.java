package org.sitenv.service.ccda.smartscorecard.controller;

import java.util.ArrayList;
import java.util.List;

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
import org.sitenv.service.ccda.smartscorecard.processor.PatientScorecard;
import org.sitenv.service.ccda.smartscorecard.processor.ProblemsScorecard;
import org.sitenv.service.ccda.smartscorecard.processor.SocialHistoryScorecard;
import org.sitenv.service.ccda.smartscorecard.processor.VitalsScorecard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
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
	
	@RequestMapping(value="/ccdascorecardservice", method= RequestMethod.POST)
	public @ResponseBody ResponseTO ccdascorecardservice(@RequestParam("ccdaFile") MultipartFile ccdaFile){
		
		
		ResponseTO response = new ResponseTO();
		String birthDate;
		Results results = new Results();
		
		try
		{
			CCDARefModel ccdaModels = CCDAParserAPI.parseCCDA2_1(ccdaFile.getInputStream());
			birthDate = ccdaModels.getPatient().getDob().getValue();
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
			//categoryList.add(ProceduresScorecard.getProceduresCategory(ccdaModels));
			
			results.setFinalGrade("B");
			results.setFinalNumericalGrade(87);
			results.setCategoryList(categoryList);
			response.setSuccess(true);
			response.setResults(results);
		}catch(Exception excp)
		{
			excp.printStackTrace();
			response.setSuccess(false);
		}
		return response;
	}
	
	

}
