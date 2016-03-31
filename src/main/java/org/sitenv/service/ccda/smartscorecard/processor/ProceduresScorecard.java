package org.sitenv.service.ccda.smartscorecard.processor;

import java.util.ArrayList;
import java.util.List;

import org.sitenv.ccdaparsing.model.CCDAMedication;
import org.sitenv.ccdaparsing.model.CCDARefModel;
import org.sitenv.service.ccda.smartscorecard.model.CCDAScoreCardRubrics;
import org.sitenv.service.ccda.smartscorecard.model.Category;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationConstants;

public class ProceduresScorecard {
	
	public static Category getProceduresCategory(CCDARefModel refModel)
	{
		
		Category medicationCategory = new Category();
		medicationCategory.setCategoryName("Medication");
		
		List<CCDAScoreCardRubrics> medicationScoreList = new ArrayList<CCDAScoreCardRubrics>();
		medicationScoreList.add(getMedicationSectionScore(refModel.getMedication()));
		medicationCategory.setCategoryRubrics(medicationScoreList);
		
		medicationCategory.setCategoryGrade("B");
		
		return medicationCategory;
		
	}
	
	
	public static CCDAScoreCardRubrics getMedicationSectionScore(CCDAMedication medications)
	{
		CCDAScoreCardRubrics medicationSectionScore = new CCDAScoreCardRubrics();
		medicationSectionScore.setPoints(ApplicationConstants.MEDICATION_SECTION_POINTS);
		medicationSectionScore.setRequirement(ApplicationConstants.MEDICATION_SECTION_REQUIREMENT);
		medicationSectionScore.setSubCategory(ApplicationConstants.SUBCATEGORIES.SECTION.getSubcategory());
		medicationSectionScore.setMaxPoints(ApplicationConstants.SUBCATEGORIES.SECTION.getMaxPoints());
		int actualPoints = 0;
		if(medications !=null)
		{
			actualPoints++;
			
		}
		
		if(actualPoints == ApplicationConstants.SUBCATEGORIES.SECTION.getMaxPoints())
		{
			medicationSectionScore.setComment("Medication Section present in CCDA document");
		}else
			medicationSectionScore.setComment("Medication Section is missing in CCDA document");
		
		medicationSectionScore.setActualPoints(actualPoints);
		
		return medicationSectionScore;
	}

}
