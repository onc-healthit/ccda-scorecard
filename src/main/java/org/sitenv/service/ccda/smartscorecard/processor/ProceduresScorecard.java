package org.sitenv.service.ccda.smartscorecard.processor;

import java.util.ArrayList;
import java.util.List;

import org.sitenv.ccdaparsing.model.CCDAMedication;
import org.sitenv.ccdaparsing.model.CCDARefModel;
import org.sitenv.service.ccda.smartscorecard.model.CCDAScoreCardRubrics;
import org.sitenv.service.ccda.smartscorecard.model.Category;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationConstants;
import org.springframework.stereotype.Service;

@Service
public class ProceduresScorecard {
	
	public static Category getProceduresCategory(CCDARefModel refModel)
	{
		
		Category medicationCategory = new Category();
		medicationCategory.setCategoryName("Procedures");
		
		List<CCDAScoreCardRubrics> medicationScoreList = new ArrayList<CCDAScoreCardRubrics>();
		//medicationScoreList.add(getMedicationSectionScore(refModel.getMedication()));
		medicationCategory.setCategoryRubrics(medicationScoreList);
		
		medicationCategory.setCategoryGrade("B");
		
		return medicationCategory;
		
	}
	
	
	
}
