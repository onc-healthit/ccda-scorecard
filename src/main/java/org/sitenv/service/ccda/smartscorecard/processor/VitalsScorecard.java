package org.sitenv.service.ccda.smartscorecard.processor;

import java.util.ArrayList;
import java.util.List;

import org.sitenv.ccdaparsing.model.CCDARefModel;
import org.sitenv.ccdaparsing.model.CCDAVitalSigns;
import org.sitenv.service.ccda.smartscorecard.model.CCDAScoreCardRubrics;
import org.sitenv.service.ccda.smartscorecard.model.Category;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationConstants;

public class VitalsScorecard {
	
	public static Category getVitalsCategory(CCDARefModel refModel)
	{
		
		Category vitalsCategory = new Category();
		vitalsCategory.setCategoryName("Vitals");
		
		List<CCDAScoreCardRubrics> vitalsScoreList = new ArrayList<CCDAScoreCardRubrics>();
		vitalsScoreList.add(getVitalsSectionScore(refModel.getVitalSigns()));
		vitalsCategory.setCategoryRubrics(vitalsScoreList);
		
		vitalsCategory.setCategoryGrade("B");
		
		return vitalsCategory;
		
	}
	
	
	public static CCDAScoreCardRubrics getVitalsSectionScore(CCDAVitalSigns vitals)
	{
		CCDAScoreCardRubrics vitalsSectionScore = new CCDAScoreCardRubrics();
		vitalsSectionScore.setPoints(ApplicationConstants.MEDICATION_SECTION_POINTS);
		vitalsSectionScore.setRequirement(ApplicationConstants.MEDICATION_SECTION_REQUIREMENT);
		vitalsSectionScore.setSubCategory(ApplicationConstants.SUBCATEGORIES.SECTION.getSubcategory());
		vitalsSectionScore.setMaxPoints(ApplicationConstants.SUBCATEGORIES.SECTION.getMaxPoints());
		int actualPoints = 0;
		if(vitals !=null)
		{
			actualPoints++;
			
		}
		
		if(actualPoints == ApplicationConstants.SUBCATEGORIES.SECTION.getMaxPoints())
		{
			vitalsSectionScore.setComment("Vitals Section present in CCDA document");
		}else
			vitalsSectionScore.setComment("Vitals Section is missing in CCDA document");
		
		vitalsSectionScore.setActualPoints(actualPoints);
		
		return vitalsSectionScore;
	}

}
