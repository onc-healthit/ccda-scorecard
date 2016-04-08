package org.sitenv.service.ccda.smartscorecard.processor;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.sitenv.ccdaparsing.model.CCDAVitalObs;
import org.sitenv.ccdaparsing.model.CCDAVitalOrg;
import org.sitenv.ccdaparsing.model.CCDAVitalSigns;
import org.sitenv.service.ccda.smartscorecard.model.CCDAScoreCardRubrics;
import org.sitenv.service.ccda.smartscorecard.model.Category;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationConstants;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationUtil;

public class VitalsScorecard {
	
	public static Category getVitalsCategory(CCDAVitalSigns vitals)
	{
		
		Category vitalsCategory = new Category();
		vitalsCategory.setCategoryName("Vitals");
		
		List<CCDAScoreCardRubrics> vitalsScoreList = new ArrayList<CCDAScoreCardRubrics>();
		vitalsScoreList.add(getVitalsSectionScore(vitals));
		vitalsScoreList.add(getTimePrecisionScore(vitals));
		vitalsScoreList.add(getTranslationsScore(vitals));
		vitalsCategory.setCategoryRubrics(vitalsScoreList);
		
		vitalsCategory.setCategoryGrade("B");
		
		return vitalsCategory;
		
	}
	
	
	public static CCDAScoreCardRubrics getVitalsSectionScore(CCDAVitalSigns vitals)
	{
		CCDAScoreCardRubrics vitalsSectionScore = new CCDAScoreCardRubrics();
		vitalsSectionScore.setPoints(ApplicationConstants.VITALS_SECTION_POINTS);
		vitalsSectionScore.setRequirement(ApplicationConstants.VITALS_SECTION_REQUIREMENT);
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
	
	public static CCDAScoreCardRubrics getTimePrecisionScore(CCDAVitalSigns vitals)
	{
		CCDAScoreCardRubrics timePrecisionScore = new CCDAScoreCardRubrics();
		timePrecisionScore.setPoints(ApplicationConstants.VITALS_TIME_PRECISION_POINTS);
		timePrecisionScore.setRequirement(ApplicationConstants.VITALS_TIME_PRECISION_REQUIREMENT);
		timePrecisionScore.setSubCategory(ApplicationConstants.SUBCATEGORIES.TIME_PRECISION.getSubcategory());
		timePrecisionScore.setMaxPoints(ApplicationConstants.SUBCATEGORIES.TIME_PRECISION.getMaxPoints());
		
		int actualPoints =1;
		try
		{
			if(vitals != null)
			{
				for (CCDAVitalOrg vitalOrg : vitals.getVitalsOrg())
				{
					if(vitalOrg.getEffTime() != null)
					{
						if(vitalOrg.getEffTime().getHigh() != null)
						{
							ApplicationUtil.convertStringToDate(vitalOrg.getEffTime().getHigh().getValue(), ApplicationConstants.DAY_FORMAT);
						}
						if(vitalOrg.getEffTime().getLow() != null)
						{
							ApplicationUtil.convertStringToDate(vitalOrg.getEffTime().getLow().getValue(), ApplicationConstants.DAY_FORMAT);
						}
					}
					
					
					if(!ApplicationUtil.isEmpty(vitalOrg.getVitalObs()))
					{
						for (CCDAVitalObs vitalObs : vitalOrg.getVitalObs() )
						{
							if(vitalObs.getMeasurementTime() != null)
							{
								if(vitalObs.getMeasurementTime().getValue() != null)
								{
									ApplicationUtil.getTsFromString(vitalObs.getMeasurementTime().getValue(), ApplicationConstants.DAY_FORMAT);
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
			timePrecisionScore.setComment("All the time elememts under Vitals section has proper precision");
		}else
		{
			timePrecisionScore.setComment("Some effective time elements under Vitals are not properly precisioned");
		}
		
		timePrecisionScore.setActualPoints(actualPoints);
		return timePrecisionScore;
	}
	
	public static CCDAScoreCardRubrics getTranslationsScore(CCDAVitalSigns vitals)
	{
		CCDAScoreCardRubrics translationsScore = new CCDAScoreCardRubrics();
		translationsScore.setPoints(ApplicationConstants.VITALS_TRANSLATIONS_POINTS);
		translationsScore.setRequirement(ApplicationConstants.VITALS_TRANSLATIONS_REQUIREMENT);
		translationsScore.setSubCategory(ApplicationConstants.SUBCATEGORIES.TRANSLATIONS.getSubcategory());
		translationsScore.setMaxPoints(ApplicationConstants.SUBCATEGORIES.TRANSLATIONS.getMaxPoints());
		
		int actualPoints =1;
		for (CCDAVitalOrg vitalOrg : vitals.getVitalsOrg())
		{
			if(vitalOrg.getTranslationCode() != null)
			{
				if(vitalOrg.getOrgCode() == null)
				{
					actualPoints = 0;
				 }
			}
		}
		
		if(ApplicationConstants.SUBCATEGORIES.TIME_PRECISION.getMaxPoints() == actualPoints)
		{
			translationsScore.setComment("All the translations under Vitals section has root element");
		}else
		{
			translationsScore.setComment("Some translation elements under Vitals is having root element as nullfalvour");
		}
		
		translationsScore.setActualPoints(actualPoints);
		return translationsScore;
	}

}
