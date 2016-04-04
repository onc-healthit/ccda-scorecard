package org.sitenv.service.ccda.smartscorecard.processor;

import java.util.ArrayList;
import java.util.List;

import org.sitenv.ccdaparsing.model.CCDAProblem;
import org.sitenv.ccdaparsing.model.CCDARefModel;
import org.sitenv.service.ccda.smartscorecard.model.CCDAScoreCardRubrics;
import org.sitenv.service.ccda.smartscorecard.model.Category;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationConstants;

public class ProblemsScorecard {
	
	public static Category getProblemsCategory(CCDARefModel refModel)
	{
		
		Category problemsCategory = new Category();
		problemsCategory.setCategoryName("Problems");
		
		List<CCDAScoreCardRubrics> problemsScoreList = new ArrayList<CCDAScoreCardRubrics>();
		problemsScoreList.add(getProblemsSectionScore(refModel.getProblem()));
		problemsCategory.setCategoryRubrics(problemsScoreList);
		
		problemsCategory.setCategoryGrade("B");
		
		return problemsCategory;
		
	}
	
	
	public static CCDAScoreCardRubrics getProblemsSectionScore(CCDAProblem problems)
	{
		CCDAScoreCardRubrics problemsSectionScore = new CCDAScoreCardRubrics();
		problemsSectionScore.setPoints(ApplicationConstants.PROBLEMS_SECTION_POINTS);
		problemsSectionScore.setRequirement(ApplicationConstants.PROBLEMS_SECTION_REQUIREMENT);
		problemsSectionScore.setSubCategory(ApplicationConstants.SUBCATEGORIES.SECTION.getSubcategory());
		problemsSectionScore.setMaxPoints(ApplicationConstants.SUBCATEGORIES.SECTION.getMaxPoints());
		int actualPoints = 0;
		if(problems !=null)
		{
			actualPoints++;
			
		}
		
		if(actualPoints == ApplicationConstants.SUBCATEGORIES.SECTION.getMaxPoints())
		{
			problemsSectionScore.setComment("Problems Section present in CCDA document");
		}else
			problemsSectionScore.setComment("Problems Section is missing in CCDA document");
		
		problemsSectionScore.setActualPoints(actualPoints);
		
		return problemsSectionScore;
	}

}
