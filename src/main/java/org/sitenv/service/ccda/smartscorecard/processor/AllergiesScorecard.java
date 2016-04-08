package org.sitenv.service.ccda.smartscorecard.processor;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.sitenv.ccdaparsing.model.CCDAAllergy;
import org.sitenv.ccdaparsing.model.CCDAAllergyConcern;
import org.sitenv.ccdaparsing.model.CCDAAllergyObs;
import org.sitenv.service.ccda.smartscorecard.model.CCDAScoreCardRubrics;
import org.sitenv.service.ccda.smartscorecard.model.Category;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationConstants;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationUtil;

public class AllergiesScorecard {
	
	public static Category getAllergiesCategory(CCDAAllergy allergies)
	{
		
		Category allergyCategory = new Category();
		allergyCategory.setCategoryName("Allergies");
		
		List<CCDAScoreCardRubrics> allergyScoreList = new ArrayList<CCDAScoreCardRubrics>();
		allergyScoreList.add(getAllergiesSectionScore(allergies));
		allergyCategory.setCategoryRubrics(allergyScoreList);
		
		allergyCategory.setCategoryGrade("B");
		
		return allergyCategory;
		
	}
	
	
	public static CCDAScoreCardRubrics getAllergiesSectionScore(CCDAAllergy allergies)
	{
		CCDAScoreCardRubrics allergySectionScore = new CCDAScoreCardRubrics();
		allergySectionScore.setPoints(ApplicationConstants.ALLERGIES_TIME_PRECISION_POINTS);
		allergySectionScore.setRequirement(ApplicationConstants.ALLERGIES_TIME_PRECISION_REQUIREMENT);
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
	
	
	public static CCDAScoreCardRubrics getTimePrecisionScore(CCDAAllergy allergies)
	{
		CCDAScoreCardRubrics timePrecisionScore = new CCDAScoreCardRubrics();
		timePrecisionScore.setPoints(ApplicationConstants.ALLERGIES_TIME_PRECISION_POINTS);
		timePrecisionScore.setRequirement(ApplicationConstants.ALLERGIES_TIME_PRECISION_REQUIREMENT);
		timePrecisionScore.setSubCategory(ApplicationConstants.SUBCATEGORIES.TIME_PRECISION.getSubcategory());
		timePrecisionScore.setMaxPoints(ApplicationConstants.SUBCATEGORIES.TIME_PRECISION.getMaxPoints());
		
		int actualPoints =1;
		try
		{
			if(allergies != null)
			{
				for (CCDAAllergyConcern allergyConcern : allergies.getAllergyConcern())
				{
					if(allergyConcern.getEffTime() != null)
					{
						if(allergyConcern.getEffTime().getHigh() != null)
						{
							ApplicationUtil.getTsFromString(allergyConcern.getEffTime().getHigh().getValue(), ApplicationConstants.MINUTE_FORMAT);
						}
						if(allergyConcern.getEffTime().getLow() != null)
						{
							ApplicationUtil.getTsFromString(allergyConcern.getEffTime().getLow().getValue(), ApplicationConstants.MINUTE_FORMAT);
						}
					}
					
					if(!ApplicationUtil.isEmpty(allergyConcern.getAllergyObs()))
					{
						for (CCDAAllergyObs allergyObservation : allergyConcern.getAllergyObs() )
						{
							if(allergyObservation.getEffTime() != null)
							{
								if(allergyObservation.getEffTime().getHigh() != null)
								{
									ApplicationUtil.convertStringToDate(allergyObservation.getEffTime().getHigh().getValue(), ApplicationConstants.DAY_FORMAT);
								}
								if(allergyObservation.getEffTime().getLow() != null)
								{
									ApplicationUtil.convertStringToDate(allergyObservation.getEffTime().getLow().getValue(), ApplicationConstants.DAY_FORMAT);
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
			timePrecisionScore.setComment("All the time elememts under allergies section has proper precision");
		}else
		{
			timePrecisionScore.setComment("Some effective time elements under allergies are not properly precisioned");
		}
		
		timePrecisionScore.setActualPoints(actualPoints);
		return timePrecisionScore;
	}

}
