package org.sitenv.service.ccda.smartscorecard.processor;

import java.util.ArrayList;
import java.util.List;

import org.sitenv.ccdaparsing.model.CCDACode;
import org.sitenv.ccdaparsing.model.CCDAII;
import org.sitenv.ccdaparsing.model.CCDAMedication;
import org.sitenv.ccdaparsing.model.CCDAMedicationActivity;
import org.sitenv.service.ccda.smartscorecard.model.CCDAScoreCardRubrics;
import org.sitenv.service.ccda.smartscorecard.model.Category;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationConstants;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationUtil;
import org.springframework.stereotype.Service;

@Service
public class MedicationScorecard {
	
	public Category getMedicationCategory(CCDAMedication medications, String birthDate)
	{
		
		Category medicationCategory = new Category();
		medicationCategory.setCategoryName("Medications");
		
		List<CCDAScoreCardRubrics> medicationScoreList = new ArrayList<CCDAScoreCardRubrics>();
		medicationScoreList.add(getTimePrecisionScore(medications));
		medicationScoreList.add(getValidDateTimeScore(medications, birthDate));
		medicationScoreList.add(getValidDisplayNameScoreCard(medications));
		medicationScoreList.add(getValidMedActivityScore(medications));
		
		medicationCategory.setCategoryRubrics(medicationScoreList);
		medicationCategory.setCategoryGrade(calculateSectionGrade(medicationScoreList));
		
		return medicationCategory;
		
	}
	
	
	public String calculateSectionGrade(List<CCDAScoreCardRubrics> rubricsList)
	{
		int actualPoints=0;
		int maxPoints = 0;
		float percentage ;
		for(CCDAScoreCardRubrics rubrics : rubricsList)
		{
			actualPoints = actualPoints + rubrics.getActualPoints();
			maxPoints = maxPoints + rubrics.getMaxPoints();
		}
		
		percentage = (actualPoints * 100)/maxPoints;
		
		if(percentage < 70)
		{
			return "D";
		}else if (percentage >=70 && percentage <80)
		{
			return "C";
		}else if(percentage >=80 && percentage <85)
		{
			return "B-";
		}else if(percentage >=85 && percentage <90)
		{
			return "B+";
		}else if(percentage >=90 && percentage <95)
		{
			return "A-";
		}else if(percentage >=95 && percentage <=100)
		{
			return "A+";
		}else
		{
			return "UNKNOWN GRADE";
		}
	}
	
	
	public CCDAScoreCardRubrics getTimePrecisionScore(CCDAMedication medications)
	{
		CCDAScoreCardRubrics timePrecisionScore = new CCDAScoreCardRubrics();
		timePrecisionScore.setPoints(ApplicationConstants.TIME_PRECISION_POINTS);
		timePrecisionScore.setRequirement(ApplicationConstants.MEDICATION_TIME_PRECISION_REQUIREMENT);
		timePrecisionScore.setSubCategory(ApplicationConstants.SUBCATEGORIES.TIME_PRECISION.getSubcategory());
		
		int actualPoints =0;
		int maxPoints = 0;
			
		if(medications != null)
		{
			for (CCDAMedicationActivity medActivity : medications.getMedActivities())
			{
				maxPoints++;
				if(medActivity.getDuration() != null)
				{
					if(medActivity.getDuration().getSingleAdministration() != null)
					{
						if(ApplicationUtil.validateMinuteFormat(medActivity.getDuration().getSingleAdministration()));
						{
							actualPoints++;
						}
					}
					else if(medActivity.getDuration().getLow() != null)
					{
						if(ApplicationUtil.validateDayFormat(medActivity.getDuration().getLow().getValue()));
						{
							actualPoints++;
						}
						if(medActivity.getDuration().getHigh() != null)
						{
							maxPoints++;
							if(ApplicationUtil.validateDayFormat(medActivity.getDuration().getHigh().getValue()));
							{
								actualPoints++;
							}
						}
					}
					
				}
			}
		}
				
		if(maxPoints!=0 && maxPoints == actualPoints)
		{
			timePrecisionScore.setComment("All the time elememts under Medication section has proper precision");
		}else
		{
			timePrecisionScore.setComment("Some effective time elements under Medication are not properly precisioned");
		}
		
		if(maxPoints!=0)
		{
			timePrecisionScore.setActualPoints(ApplicationUtil.calculateActualPoints(maxPoints, actualPoints));
		}else
		{
			timePrecisionScore.setActualPoints(0);
		}
		
		timePrecisionScore.setMaxPoints(4);
		return timePrecisionScore;
	}
	
	
	public CCDAScoreCardRubrics getValidDateTimeScore(CCDAMedication medications, String birthDate)
	{
		CCDAScoreCardRubrics validateTimeScore = new CCDAScoreCardRubrics();
		validateTimeScore.setPoints(ApplicationConstants.VALID_TIME_POINTS);
		validateTimeScore.setRequirement(ApplicationConstants.MEDICATION_TIMEDATE_VALID_REQUIREMENT);
		validateTimeScore.setSubCategory(ApplicationConstants.SUBCATEGORIES.TIME_VALIDATION.getSubcategory());
		
		int actualPoints =0;
		int maxPoints = 0;
			
		if(medications != null)
		{
			for (CCDAMedicationActivity medActivity : medications.getMedActivities())
			{
				maxPoints++;
				if(medActivity.getDuration() != null)
				{
					if(medActivity.getDuration().getSingleAdministration() != null)
					{
						if(ApplicationUtil.checkDateRange(birthDate, medActivity.getDuration().getSingleAdministration()))
						{
							actualPoints++;
						}
					}
					else if(medActivity.getDuration().getLow() != null)
					{
						if(ApplicationUtil.checkDateRange(birthDate, medActivity.getDuration().getLow().getValue()))
						{
							actualPoints++;
						}
						if(medActivity.getDuration().getHigh() != null)
						{
							maxPoints++;
							if(ApplicationUtil.checkDateRange(birthDate, medActivity.getDuration().getHigh().getValue()))
							{
								actualPoints++;
							}
						}
					}
					
				}
			}
		}
				
		if(maxPoints!=0 && maxPoints == actualPoints)
		{
			validateTimeScore.setComment("All the time elememts under Medication are valid.");
		}else
		{
			validateTimeScore.setComment("Some effective time elements under Medication are not valid or not present within human lifespan");
		}
		
		if(maxPoints!=0)
		{
			validateTimeScore.setActualPoints(ApplicationUtil.calculateActualPoints(maxPoints, actualPoints));
		}else
		{
			validateTimeScore.setActualPoints(0);
		}
		
		validateTimeScore.setMaxPoints(4);
		return validateTimeScore;
	}
	
	
	public CCDAScoreCardRubrics getValidDisplayNameScoreCard(CCDAMedication medications)
	{
		CCDAScoreCardRubrics validateDisplayNameScore = new CCDAScoreCardRubrics();
		validateDisplayNameScore.setPoints(ApplicationConstants.VALID_CODE_DISPLAYNAME_POINTS);
		validateDisplayNameScore.setRequirement(ApplicationConstants.MEDICATION_CODE_DISPLAYNAME_REQUIREMENT);
		validateDisplayNameScore.setSubCategory(ApplicationConstants.SUBCATEGORIES.CODE_DISPLAYNAME_VALIDATION.getSubcategory());
		
		int maxPoints = 0;
		int actualPoints = 0;
		if(medications != null)
		{
			maxPoints++;
			if(medications.getSectionCode().getDisplayName()!= null)
			{
				if(ApplicationUtil.validateDisplayName(medications.getSectionCode().getCode(), 
							ApplicationConstants.CODE_SYSTEM_MAP.get(medications.getSectionCode().getCodeSystem()),
						medications.getSectionCode().getDisplayName().toUpperCase()))
				{
					actualPoints++;
				}
			}
			
			if(!ApplicationUtil.isEmpty(medications.getMedActivities()))
			{
				for (CCDAMedicationActivity medActivity : medications.getMedActivities())
				{
					maxPoints++;
					if(medActivity.getApproachSiteCode()!= null)
					{
						if(ApplicationUtil.validateDisplayName(medActivity.getApproachSiteCode().getCode(), 
								ApplicationConstants.CODE_SYSTEM_MAP.get(medActivity.getApproachSiteCode().getCodeSystem()),
																medActivity.getApproachSiteCode().getDisplayName().toUpperCase()))
						{
							actualPoints++;
						}
					}
					
					if(medActivity.getConsumable() != null)
					{
						if(!ApplicationUtil.isEmpty(medActivity.getConsumable().getTranslations()))
						{
							for (CCDACode translationCode : medActivity.getConsumable().getTranslations())
							{
								maxPoints++;
								if(ApplicationUtil.validateDisplayName(translationCode.getCode(), 
												ApplicationConstants.CODE_SYSTEM_MAP.get(translationCode.getCodeSystem().toUpperCase()),
														translationCode.getDisplayName()))
								{
									actualPoints++;
								}
							}
						}
					}
				}
			}
		}
		
		if(maxPoints!=0 && maxPoints == actualPoints)
		{
			validateDisplayNameScore.setComment("All the code elements under Medication are having valid display name");
		}else
		{
			validateDisplayNameScore.setComment("Some code elements under Medication are not having valid display name");
		}
		
		if(maxPoints!=0)
		{
			validateDisplayNameScore.setActualPoints(ApplicationUtil.calculateActualPoints(maxPoints, actualPoints));
		}else
		{
			validateDisplayNameScore.setActualPoints(0);
		}
		validateDisplayNameScore.setMaxPoints(4);
		return validateDisplayNameScore;
	}
	
	public CCDAScoreCardRubrics getValidMedActivityScore(CCDAMedication medications)
	{
		CCDAScoreCardRubrics validateMedActivityScore = new CCDAScoreCardRubrics();
		validateMedActivityScore.setPoints(ApplicationConstants.VALID_MEDICATIONS_POINTS);
		validateMedActivityScore.setRequirement(ApplicationConstants.MEDICATION_ACTIVITY_VALID_REQUIREMENT);
		validateMedActivityScore.setSubCategory(ApplicationConstants.SUBCATEGORIES.MEDICATION_VALID.getSubcategory());
		
		int actualPoints =1;
		int maxPoints = 1;
			
		if(medications != null)
		{
			if(!ApplicationUtil.isEmpty(medications.getMedActivities()))
			{
				for (CCDAMedicationActivity medAct : medications.getMedActivities())
				{
					if(!ApplicationUtil.isEmpty(medAct.getTemplateIds()))
					{
						for (CCDAII templateId : medAct.getTemplateIds())
						{
							if(templateId.getValue() != null && templateId.getValue().equals(ApplicationConstants.IMMUNIZATION_ACTIVITY_ID))
							{
								actualPoints = 0;
							}
						} 
					}
				}
			}
		}
		
		if(maxPoints == actualPoints)
		{
			validateMedActivityScore.setComment("No Immunizations under Medication section");
		}else
		{
			validateMedActivityScore.setComment("Immunizations should be under Immunization section and not in Medication section");
		}
		
		validateMedActivityScore.setActualPoints(actualPoints);
		validateMedActivityScore.setMaxPoints(maxPoints);
		return validateMedActivityScore;
	}
}
