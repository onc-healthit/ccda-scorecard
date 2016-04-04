package org.sitenv.service.ccda.smartscorecard.processor;

import java.util.ArrayList;
import java.util.List;

import org.sitenv.ccdaparsing.model.CCDAImmunization;
import org.sitenv.ccdaparsing.model.CCDARefModel;
import org.sitenv.service.ccda.smartscorecard.model.CCDAScoreCardRubrics;
import org.sitenv.service.ccda.smartscorecard.model.Category;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationConstants;

public class ImmunizationScorecard {
	
	public static Category getImmunizationCategory(CCDARefModel refModel)
	{
		
		Category immunizationCategory = new Category();
		immunizationCategory.setCategoryName("Immunization");
		
		List<CCDAScoreCardRubrics> immunizationScoreList = new ArrayList<CCDAScoreCardRubrics>();
		immunizationScoreList.add(getImmunizationSectionScore(refModel.getImmunization()));
		immunizationCategory.setCategoryRubrics(immunizationScoreList);
		
		immunizationCategory.setCategoryGrade("B");
		
		return immunizationCategory;
		
	}
	
	
	public static CCDAScoreCardRubrics getImmunizationSectionScore(CCDAImmunization immunizatons)
	{
		CCDAScoreCardRubrics immunizationSectionScore = new CCDAScoreCardRubrics();
		immunizationSectionScore.setPoints(ApplicationConstants.IMMUNIZATION_SECTION_POINTS);
		immunizationSectionScore.setRequirement(ApplicationConstants.IMMUNIZATION_SECTION_REQUIREMENT);
		immunizationSectionScore.setSubCategory(ApplicationConstants.SUBCATEGORIES.SECTION.getSubcategory());
		immunizationSectionScore.setMaxPoints(ApplicationConstants.SUBCATEGORIES.SECTION.getMaxPoints());
		int actualPoints = 0;
		if(immunizatons !=null)
		{
			actualPoints++;
			
		}
		
		if(actualPoints == ApplicationConstants.SUBCATEGORIES.SECTION.getMaxPoints())
		{
			immunizationSectionScore.setComment("Immunization Section present in CCDA document");
		}else
			immunizationSectionScore.setComment("Immunization Section is missing in CCDA document");
		
		immunizationSectionScore.setActualPoints(actualPoints);
		
		return immunizationSectionScore;
	}

}
