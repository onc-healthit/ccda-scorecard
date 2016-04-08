package org.sitenv.service.ccda.smartscorecard.processor;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.sitenv.ccdaparsing.model.CCDALabResult;
import org.sitenv.ccdaparsing.model.CCDALabResultObs;
import org.sitenv.ccdaparsing.model.CCDALabResultOrg;
import org.sitenv.ccdaparsing.model.CCDARefModel;
import org.sitenv.service.ccda.smartscorecard.model.CCDAScoreCardRubrics;
import org.sitenv.service.ccda.smartscorecard.model.Category;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationConstants;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationUtil;

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
	
	public static CCDAScoreCardRubrics getTimePrecisionScore(CCDALabResult labResults)
	{
		CCDAScoreCardRubrics timePrecisionScore = new CCDAScoreCardRubrics();
		timePrecisionScore.setPoints(ApplicationConstants.LABRESULTS_TIME_PRECISION_POINTS);
		timePrecisionScore.setRequirement(ApplicationConstants.LABRESULTS_TIME_PRECISION_REQUIREMENT);
		timePrecisionScore.setSubCategory(ApplicationConstants.SUBCATEGORIES.TIME_PRECISION.getSubcategory());
		timePrecisionScore.setMaxPoints(ApplicationConstants.SUBCATEGORIES.TIME_PRECISION.getMaxPoints());
		
		int actualPoints =1;
		try
		{
			if(labResults != null)
			{
				for (CCDALabResultOrg resultOrg : labResults.getResultOrg())
				{
					if(resultOrg.getEffTime() != null)
					{
						if(resultOrg.getEffTime().getHigh() != null)
						{
							ApplicationUtil.getTsFromString(resultOrg.getEffTime().getHigh().getValue(), ApplicationConstants.SECOND_FORMAT);
						}
						if(resultOrg.getEffTime().getLow() != null)
						{
							ApplicationUtil.getTsFromString(resultOrg.getEffTime().getLow().getValue(), ApplicationConstants.SECOND_FORMAT);
						}
					}
					
					if(!ApplicationUtil.isEmpty(resultOrg.getResultObs()))
					{
						for (CCDALabResultObs resultObs : resultOrg.getResultObs())
						{
							if(resultObs.getMeasurementTime() != null)
							{
								if(resultObs.getMeasurementTime().getValue() != null)
								{
									ApplicationUtil.getTsFromString(resultObs.getMeasurementTime().getValue(), ApplicationConstants.SECOND_FORMAT);
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
			timePrecisionScore.setComment("All the time elememts under Results section has proper precision");
		}else
		{
			timePrecisionScore.setComment("Some effective time elements under Results are not properly precisioned");
		}
		
		timePrecisionScore.setActualPoints(actualPoints);
		return timePrecisionScore;
	}


}
