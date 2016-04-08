package org.sitenv.service.ccda.smartscorecard.processor;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.sitenv.ccdaparsing.model.CCDAImmunization;
import org.sitenv.ccdaparsing.model.CCDAImmunizationActivity;
import org.sitenv.service.ccda.smartscorecard.model.CCDAScoreCardRubrics;
import org.sitenv.service.ccda.smartscorecard.model.Category;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationConstants;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationUtil;

public class ImmunizationScorecard {
	
	public static Category getImmunizationCategory(CCDAImmunization immunizatons)
	{
		
		Category immunizationCategory = new Category();
		immunizationCategory.setCategoryName("Immunization");
		
		List<CCDAScoreCardRubrics> immunizationScoreList = new ArrayList<CCDAScoreCardRubrics>();
		immunizationScoreList.add(getImmunizationSectionScore(immunizatons));
		immunizationScoreList.add(getTimePrecisionScore(immunizatons));
		immunizationScoreList.add(getTranslationsScore(immunizatons));
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
	
	public static CCDAScoreCardRubrics getTimePrecisionScore(CCDAImmunization immunizatons)
	{
		CCDAScoreCardRubrics timePrecisionScore = new CCDAScoreCardRubrics();
		timePrecisionScore.setPoints(ApplicationConstants.IMMUNIZATION_TIME_PRECISION_POINTS);
		timePrecisionScore.setRequirement(ApplicationConstants.IMMUNIZATION_EFF_TIME_REQUIREMENT);
		timePrecisionScore.setSubCategory(ApplicationConstants.SUBCATEGORIES.TIME_PRECISION.getSubcategory());
		timePrecisionScore.setMaxPoints(ApplicationConstants.SUBCATEGORIES.TIME_PRECISION.getMaxPoints());
		
		int actualPoints =1;
		try
		{
			if(immunizatons != null)
			{
				for (CCDAImmunizationActivity immunizationActivity : immunizatons.getImmActivity())
				{
					if(immunizationActivity.getTime() != null)
					{
						if(immunizationActivity.getTime().getValue() != null)
						{
							ApplicationUtil.convertStringToDate(immunizationActivity.getTime().getValue(), ApplicationConstants.DAY_FORMAT);
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
			timePrecisionScore.setComment("All the time elememts under Immunization section has proper precision");
		}else
		{
			timePrecisionScore.setComment("Some effective time elements under Immunization are not properly precisioned");
		}
		
		timePrecisionScore.setActualPoints(actualPoints);
		return timePrecisionScore;
	}
	
	public static CCDAScoreCardRubrics getTranslationsScore(CCDAImmunization immunizatons)
	{
		CCDAScoreCardRubrics translationsScore = new CCDAScoreCardRubrics();
		translationsScore.setPoints(ApplicationConstants.IMMUNIZATION_TRANSLATIONS_POINTS);
		translationsScore.setRequirement(ApplicationConstants.IMMUNIZATION_TRANSLATIONS_REQUIREMENT);
		translationsScore.setSubCategory(ApplicationConstants.SUBCATEGORIES.TRANSLATIONS.getSubcategory());
		translationsScore.setMaxPoints(ApplicationConstants.SUBCATEGORIES.TRANSLATIONS.getMaxPoints());
		
		int actualPoints =1;
		
		if(immunizatons != null)
		{
			for (CCDAImmunizationActivity immunizationActivity : immunizatons.getImmActivity())
			{
				if(!ApplicationUtil.isEmpty(immunizationActivity.getConsumable().getTranslations()))
				{
					if(immunizationActivity.getConsumable().getMedcode() == null)
					{
						actualPoints =0;
					}
					
				}
			
			}
		}
		
		if(ApplicationConstants.SUBCATEGORIES.TIME_PRECISION.getMaxPoints() == actualPoints)
		{
			translationsScore.setComment("All the translations under Immunization section has root element");
		}else
		{
			translationsScore.setComment("Some translation elements under Immunization is having root element as nullfalvour");
		}
		
		translationsScore.setActualPoints(actualPoints);
		return translationsScore;
	}
}
