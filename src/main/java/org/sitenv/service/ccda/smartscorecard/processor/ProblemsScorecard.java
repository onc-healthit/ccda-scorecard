package org.sitenv.service.ccda.smartscorecard.processor;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.sitenv.ccdaparsing.model.CCDAProblem;
import org.sitenv.ccdaparsing.model.CCDAProblemConcern;
import org.sitenv.ccdaparsing.model.CCDAProblemObs;
import org.sitenv.service.ccda.smartscorecard.model.CCDAScoreCardRubrics;
import org.sitenv.service.ccda.smartscorecard.model.Category;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationConstants;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationUtil;

public class ProblemsScorecard {
	
	public static Category getProblemsCategory(CCDAProblem problems)
	{
		
		Category problemsCategory = new Category();
		problemsCategory.setCategoryName("Problems");
		
		List<CCDAScoreCardRubrics> problemsScoreList = new ArrayList<CCDAScoreCardRubrics>();
		problemsScoreList.add(getProblemsSectionScore(problems));
		problemsScoreList.add(getTimePrecisionScore(problems));
		problemsScoreList.add(getTranslationsScore(problems));
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
	
	public static CCDAScoreCardRubrics getTimePrecisionScore(CCDAProblem problem)
	{
		CCDAScoreCardRubrics timePrecisionScore = new CCDAScoreCardRubrics();
		timePrecisionScore.setPoints(ApplicationConstants.PROBLEMS_TIME_PRECISION_POINTS);
		timePrecisionScore.setRequirement(ApplicationConstants.PROBLEMS_TIME_PRECISION_REQUIREMENT);
		timePrecisionScore.setSubCategory(ApplicationConstants.SUBCATEGORIES.TIME_PRECISION.getSubcategory());
		timePrecisionScore.setMaxPoints(ApplicationConstants.SUBCATEGORIES.TIME_PRECISION.getMaxPoints());
		
		int actualPoints =1;
		try
		{
			if(problem != null)
			{
				for (CCDAProblemConcern problemConcern : problem.getProblemConcerns())
				{
					if(problemConcern.getEffTime() != null)
					{
						if(problemConcern.getEffTime().getHigh() != null)
						{
							ApplicationUtil.getTsFromString(problemConcern.getEffTime().getHigh().getValue(), ApplicationConstants.SECOND_FORMAT);
						}
						if(problemConcern.getEffTime().getLow() != null)
						{
							ApplicationUtil.getTsFromString(problemConcern.getEffTime().getLow().getValue(), ApplicationConstants.SECOND_FORMAT);
						}
					}
					
					if(!ApplicationUtil.isEmpty(problemConcern.getProblemObservations()))
					{
						for (CCDAProblemObs problemObs : problemConcern.getProblemObservations() )
						{
							if(problemObs.getEffTime() != null)
							{
								if(problemObs.getEffTime().getHigh() != null)
								{
									ApplicationUtil.getTsFromString(problemObs.getEffTime().getHigh().getValue(), ApplicationConstants.SECOND_FORMAT);
								}
								if(problemObs.getEffTime().getLow() != null)
								{
									ApplicationUtil.getTsFromString(problemObs.getEffTime().getLow().getValue(), ApplicationConstants.SECOND_FORMAT);
								}
							}
						}
					}
				}
			}

		}catch(ParseException e)
		{
			actualPoints =0;
		}
		
		if(ApplicationConstants.SUBCATEGORIES.TIME_PRECISION.getMaxPoints() == actualPoints)
		{
			timePrecisionScore.setComment("All the time elememts under Problmes section has proper precision");
		}else
		{
			timePrecisionScore.setComment("Some effective time elements under Problems are not properly precisioned");
		}
		
		timePrecisionScore.setActualPoints(actualPoints);
		return timePrecisionScore;
	}
	
	public static CCDAScoreCardRubrics getTranslationsScore(CCDAProblem problem)
	{
		CCDAScoreCardRubrics translationsScore = new CCDAScoreCardRubrics();
		translationsScore.setPoints(ApplicationConstants.PROBLEMS_TRANSLATIONS_POINTS);
		translationsScore.setRequirement(ApplicationConstants.PROBLEMS_TRANSLATIONS_REQUIREMENT);
		translationsScore.setSubCategory(ApplicationConstants.SUBCATEGORIES.TRANSLATIONS.getSubcategory());
		translationsScore.setMaxPoints(ApplicationConstants.SUBCATEGORIES.TRANSLATIONS.getMaxPoints());
		
		int actualPoints =1;
		
		if(problem != null)
		{
			for (CCDAProblemConcern problemConcern : problem.getProblemConcerns())
			{
				for (CCDAProblemObs problemObs : problemConcern.getProblemObservations() )
				{
					if(!ApplicationUtil.isEmpty(problemObs.getTranslationProblemType()))
					{
					   if(problemObs.getProblemCode() == null)
					   {
						   actualPoints = 0;
					   }
					}
				}
			}
		}
		
		if(ApplicationConstants.SUBCATEGORIES.TIME_PRECISION.getMaxPoints() == actualPoints)
		{
			translationsScore.setComment("All the teanslations under Problems section has root element");
		}else
		{
			translationsScore.setComment("Some translation elements under Problems is having root element as nullfalvour");
		}
		
		translationsScore.setActualPoints(actualPoints);
		return translationsScore;
	}

}
