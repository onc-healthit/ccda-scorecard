package org.sitenv.service.ccda.smartscorecard.processor;

import java.util.ArrayList;
import java.util.List;

import org.sitenv.ccdaparsing.model.CCDALabResult;
import org.sitenv.ccdaparsing.model.CCDARefModel;
import org.sitenv.service.ccda.smartscorecard.model.CCDAScoreCardRubrics;
import org.sitenv.service.ccda.smartscorecard.model.Category;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationConstants;

public class LabresultsScorecard {
	
	public static Category getLabResultsCategory(CCDARefModel refModel)
	{
		
		Category labResultsCategory = new Category();
		labResultsCategory.setCategoryName("Lab Results");
		
		List<CCDAScoreCardRubrics> labResultsScoreList = new ArrayList<CCDAScoreCardRubrics>();
		labResultsScoreList.add(getLabResultsSectionScore(refModel.getLabResults()));
		labResultsCategory.setCategoryRubrics(labResultsScoreList);
		
		labResultsCategory.setCategoryGrade("B");
		
		return labResultsCategory;
		
	}
	
	
	public static CCDAScoreCardRubrics getLabResultsSectionScore(CCDALabResult labResults)
	{
		CCDAScoreCardRubrics labResultsSectionScore = new CCDAScoreCardRubrics();
		labResultsSectionScore.setPoints(ApplicationConstants.LABRESULTS_SECTION_POINTS);
		labResultsSectionScore.setRequirement(ApplicationConstants.LABRESULTS_SECTION_REQUIREMENT);
		labResultsSectionScore.setSubCategory(ApplicationConstants.SUBCATEGORIES.SECTION.getSubcategory());
		labResultsSectionScore.setMaxPoints(ApplicationConstants.SUBCATEGORIES.SECTION.getMaxPoints());
		int actualPoints = 0;
		if(labResults !=null)
		{
			actualPoints++;
			
		}
		
		if(actualPoints == ApplicationConstants.SUBCATEGORIES.SECTION.getMaxPoints())
		{
			labResultsSectionScore.setComment("Lab Results Section present in CCDA document");
		}else
			labResultsSectionScore.setComment("Lab Results Section is missing in CCDA document");
		
		labResultsSectionScore.setActualPoints(actualPoints);
		
		return labResultsSectionScore;
	}


}
