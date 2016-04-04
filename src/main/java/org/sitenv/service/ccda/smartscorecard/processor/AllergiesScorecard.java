package org.sitenv.service.ccda.smartscorecard.processor;

import java.util.ArrayList;
import java.util.List;

import org.sitenv.ccdaparsing.model.CCDAAllergy;
import org.sitenv.ccdaparsing.model.CCDARefModel;
import org.sitenv.service.ccda.smartscorecard.model.CCDAScoreCardRubrics;
import org.sitenv.service.ccda.smartscorecard.model.Category;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationConstants;

public class AllergiesScorecard {
	
	public static Category getAllergiesCategory(CCDARefModel refModel)
	{
		
		Category allergyCategory = new Category();
		allergyCategory.setCategoryName("Allergies");
		
		List<CCDAScoreCardRubrics> allergyScoreList = new ArrayList<CCDAScoreCardRubrics>();
		allergyScoreList.add(getAllergiesSectionScore(refModel.getAllergy()));
		allergyCategory.setCategoryRubrics(allergyScoreList);
		
		allergyCategory.setCategoryGrade("B");
		
		return allergyCategory;
		
	}
	
	
	public static CCDAScoreCardRubrics getAllergiesSectionScore(CCDAAllergy allergies)
	{
		CCDAScoreCardRubrics allergySectionScore = new CCDAScoreCardRubrics();
		allergySectionScore.setPoints(ApplicationConstants.ALLERGIES_SECTION_POINTS);
		allergySectionScore.setRequirement(ApplicationConstants.ALLERGIES_SECTION_REQUIREMENT);
		allergySectionScore.setSubCategory(ApplicationConstants.SUBCATEGORIES.SECTION.getSubcategory());
		allergySectionScore.setMaxPoints(ApplicationConstants.SUBCATEGORIES.SECTION.getMaxPoints());
		int actualPoints = 0;
		if(allergies !=null)
		{
			actualPoints++;
			
		}
		
		if(actualPoints == ApplicationConstants.SUBCATEGORIES.SECTION.getMaxPoints())
		{
			allergySectionScore.setComment("Allergies Section present in CCDA document");
		}else
			allergySectionScore.setComment("Allergies Section is missing in CCDA document");
		
		allergySectionScore.setActualPoints(actualPoints);
		
		return allergySectionScore;
	}

}
