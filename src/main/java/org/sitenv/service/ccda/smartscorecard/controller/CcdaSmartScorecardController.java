package org.sitenv.service.ccda.smartscorecard.controller;

import java.io.IOException;
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
import org.sitenv.service.ccda.smartscorecard.processor.ProceduresScorecard;
import org.sitenv.service.ccda.smartscorecard.processor.VitalsScorecard;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationConstants;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CcdaSmartScorecardController {
	
	@RequestMapping(value="/ccdascorecardservice", method= RequestMethod.GET)
	public @ResponseBody ResponseTO ccdascorecardservice() throws IOException {
		
		CCDARefModel ccdaModels = CCDAParserAPI.parseCCDA2_1(ApplicationConstants.FILEPATH);
		ResponseTO response = new ResponseTO();
		
		Results results = new Results();
		
		List<Category> categoryList = new ArrayList<Category>();
		categoryList.add(PatientScorecard.getPatientCategory(ccdaModels.getPatient()));
		categoryList.add(EncounterScorecard.getEncounterCategory(ccdaModels.getEncounter()));
		categoryList.add(AllergiesScorecard.getAllergiesCategory(ccdaModels.getAllergy()));
		categoryList.add(ProblemsScorecard.getProblemsCategory(ccdaModels.getProblem()));
		categoryList.add(MedicationScorecard.getMedicationCategory(ccdaModels.getMedication()));
		categoryList.add(ImmunizationScorecard.getImmunizationCategory(ccdaModels.getImmunization()));
		categoryList.add(LabresultsScorecard.getLabResultsCategory(ccdaModels));
		categoryList.add(VitalsScorecard.getVitalsCategory(ccdaModels.getVitalSigns()));
		categoryList.add(ProceduresScorecard.getProceduresCategory(ccdaModels));
		
		results.setFinalGrade("B");
		results.setCategoryList(categoryList);
		response.setSuccess(true);
		response.setResults(results);
		return response;
	}
	
	

}
