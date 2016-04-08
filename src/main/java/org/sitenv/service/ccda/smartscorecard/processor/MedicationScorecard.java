package org.sitenv.service.ccda.smartscorecard.processor;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.sitenv.ccdaparsing.model.CCDAMedication;
import org.sitenv.ccdaparsing.model.CCDAMedicationActivity;
import org.sitenv.service.ccda.smartscorecard.model.CCDAScoreCardRubrics;
import org.sitenv.service.ccda.smartscorecard.model.Category;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationConstants;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationUtil;

public class MedicationScorecard {
	
	public static Category getMedicationCategory(CCDAMedication medication)
	{
		
		Category medicationCategory = new Category();
		medicationCategory.setCategoryName("Medication");
		
		List<CCDAScoreCardRubrics> medicationScoreList = new ArrayList<CCDAScoreCardRubrics>();
		medicationScoreList.add(getMedicationSectionScore(medication));
		medicationScoreList.add(getTimePrecisionScore(medication));
		medicationScoreList.add(getTranslationsScore(medication));
		medicationCategory.setCategoryRubrics(medicationScoreList);
		
		medicationCategory.setCategoryGrade("B");
		
		return medicationCategory;
		
	}
	
	
	public static CCDAScoreCardRubrics getMedicationSectionScore(CCDAMedication medications)
	{
		CCDAScoreCardRubrics medicationSectionScore = new CCDAScoreCardRubrics();
		medicationSectionScore.setPoints(ApplicationConstants.MEDICATION_SECTION_POINTS);
		medicationSectionScore.setRequirement(ApplicationConstants.MEDICATION_SECTION_REQUIREMENT);
		medicationSectionScore.setSubCategory(ApplicationConstants.SUBCATEGORIES.SECTION.getSubcategory());
		medicationSectionScore.setMaxPoints(ApplicationConstants.SUBCATEGORIES.SECTION.getMaxPoints());
		int actualPoints = 0;
		if(medications !=null)
		{
			actualPoints++;
			
		}
		
		if(actualPoints == ApplicationConstants.SUBCATEGORIES.SECTION.getMaxPoints())
		{
			medicationSectionScore.setComment("Medication Section present in CCDA document");
		}else
			medicationSectionScore.setComment("Medication Section is missing in CCDA document");
		
		medicationSectionScore.setActualPoints(actualPoints);
		
		return medicationSectionScore;
	}
	
	public static CCDAScoreCardRubrics getTimePrecisionScore(CCDAMedication medications)
	{
		CCDAScoreCardRubrics timePrecisionScore = new CCDAScoreCardRubrics();
		timePrecisionScore.setPoints(ApplicationConstants.MEDICATION_TIME_PRECISION_POINTS);
		timePrecisionScore.setRequirement(ApplicationConstants.MEDICATION_TIME_PRECISION_REQUIREMENT);
		timePrecisionScore.setSubCategory(ApplicationConstants.SUBCATEGORIES.TIME_PRECISION.getSubcategory());
		timePrecisionScore.setMaxPoints(ApplicationConstants.SUBCATEGORIES.TIME_PRECISION.getMaxPoints());
		
		int actualPoints =1;
		try
		{
			if(medications != null)
			{
				for (CCDAMedicationActivity medActivity : medications.getMedActivities())
				{
					if(medActivity.getDuration() != null)
					{
						if(medActivity.getDuration().getHigh() != null)
						{
							ApplicationUtil.convertStringToDate(medActivity.getDuration().getHigh().getValue(), ApplicationConstants.DAY_FORMAT);
						}
						if(medActivity.getDuration().getLow() != null)
						{
							ApplicationUtil.convertStringToDate(medActivity.getDuration().getLow().getValue(), ApplicationConstants.DAY_FORMAT);
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
			timePrecisionScore.setComment("All the time elememts under encounter section has proper precision");
		}else
		{
			timePrecisionScore.setComment("Some effective time elements under encounter are not properly precisioned");
		}
		
		timePrecisionScore.setActualPoints(actualPoints);
		return timePrecisionScore;
	}
	
	public static CCDAScoreCardRubrics getTranslationsScore(CCDAMedication medications)
	{
		CCDAScoreCardRubrics translationsScore = new CCDAScoreCardRubrics();
		translationsScore.setPoints(ApplicationConstants.MEDICATION_TRANSLATIONS_POINTS);
		translationsScore.setRequirement(ApplicationConstants.MEDICATION_TRANSLATIONS_REQUIREMENT);
		translationsScore.setSubCategory(ApplicationConstants.SUBCATEGORIES.TRANSLATIONS.getSubcategory());
		translationsScore.setMaxPoints(ApplicationConstants.SUBCATEGORIES.TRANSLATIONS.getMaxPoints());
		
		int actualPoints =1;
		
		if(medications != null)
		{
			for (CCDAMedicationActivity medActivity : medications.getMedActivities())
			{
				if(!ApplicationUtil.isEmpty(medActivity.getConsumable().getTranslations()))
				{
					if(medActivity.getConsumable().getMedcode() == null)
					{
						actualPoints =0;
					}
					
				}
			}
		}
		
		if(ApplicationConstants.SUBCATEGORIES.TIME_PRECISION.getMaxPoints() == actualPoints)
		{
			translationsScore.setComment("All the translations under Medication section has root element");
		}else
		{
			translationsScore.setComment("Some translation elements under Medication is having root element as nullfalvour");
		}
		
		translationsScore.setActualPoints(actualPoints);
		return translationsScore;
	}

}
