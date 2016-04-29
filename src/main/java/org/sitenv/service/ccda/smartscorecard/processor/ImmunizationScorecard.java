package org.sitenv.service.ccda.smartscorecard.processor;

import java.util.ArrayList;
import java.util.List;

import org.sitenv.ccdaparsing.model.CCDACode;
import org.sitenv.ccdaparsing.model.CCDAImmunization;
import org.sitenv.ccdaparsing.model.CCDAImmunizationActivity;
import org.sitenv.service.ccda.smartscorecard.model.CCDAScoreCardRubrics;
import org.sitenv.service.ccda.smartscorecard.model.Category;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationConstants;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationUtil;
import org.springframework.stereotype.Service;

@Service
public class ImmunizationScorecard {
	
	public Category getImmunizationCategory(CCDAImmunization immunizatons, String birthDate)
	{
		
		Category immunizationCategory = new Category();
		immunizationCategory.setCategoryName("Immunizations");
		
		List<CCDAScoreCardRubrics> immunizationScoreList = new ArrayList<CCDAScoreCardRubrics>();
		immunizationScoreList.add(getTimePrecisionScore(immunizatons));
		immunizationScoreList.add(getValidDateTimeScore(immunizatons,birthDate));
		//immunizationScoreList.add(getValidDisplayNameScoreCard(immunizatons));
		
		immunizationCategory.setCategoryRubrics(immunizationScoreList);
		immunizationCategory.setCategoryGrade(calculateSectionGrade(immunizationScoreList));
		
		return immunizationCategory;
		
	}
	
	public  String calculateSectionGrade(List<CCDAScoreCardRubrics> rubricsList)
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
		
		if(percentage <= 35)
		{
			return "c";
		}else if(percentage >= 35 && percentage <=70)
		{
			return "B";
		}else if(percentage >=70 && percentage <=100)
		{
			return "A";
		}else 
			return "UNKNOWN GRADE";
	}
	
	
	public CCDAScoreCardRubrics getTimePrecisionScore(CCDAImmunization immunizatons)
	{
		CCDAScoreCardRubrics timePrecisionScore = new CCDAScoreCardRubrics();
		timePrecisionScore.setPoints(ApplicationConstants.TIME_PRECISION_POINTS);
		timePrecisionScore.setRequirement(ApplicationConstants.IMMUNIZATION_TIME_PRECISION_REQUIREMENT);
		timePrecisionScore.setSubCategory(ApplicationConstants.SUBCATEGORIES.TIME_PRECISION.getSubcategory());
		
		int actualPoints =0;
		int maxPoints = 0;
		
		if(immunizatons != null)
		{
			for (CCDAImmunizationActivity immunizationActivity : immunizatons.getImmActivity())
			{
				maxPoints++;
				if(immunizationActivity.getTime() != null)
				{
					if(ApplicationUtil.validateDayFormat(immunizationActivity.getTime().getValue()));
					{
						actualPoints++;
					}
						
				}
			}
		}

		if(maxPoints == actualPoints)
		{
			timePrecisionScore.setComment("All the time elememts under Immunization section are properly precisioned");
		}else
		{
			timePrecisionScore.setComment("Some effective time elements under Immunization are not properly precisioned");
		}
		
		timePrecisionScore.setActualPoints(ApplicationUtil.calculateActualPoints(maxPoints, actualPoints));
		timePrecisionScore.setMaxPoints(4);
		return timePrecisionScore;
	}
	
	
	public CCDAScoreCardRubrics getValidDateTimeScore(CCDAImmunization immunizatons, String birthDate)
	{
		CCDAScoreCardRubrics validateTimeScore = new CCDAScoreCardRubrics();
		validateTimeScore.setPoints(ApplicationConstants.VALID_TIME_POINTS);
		validateTimeScore.setRequirement(ApplicationConstants.IMMUNIZATION_TIMEDATE_VALID_REQUIREMENT);
		validateTimeScore.setSubCategory(ApplicationConstants.SUBCATEGORIES.TIME_VALIDATION.getSubcategory());
		
		int actualPoints =0;
		int maxPoints = 0;
		
		if(immunizatons != null)
		{
			for (CCDAImmunizationActivity immunizationActivity : immunizatons.getImmActivity())
			{
				maxPoints++;
				if(immunizationActivity.getTime() != null)
				{
					if(ApplicationUtil.validateDate(immunizationActivity.getTime().getValue()) &&
							ApplicationUtil.checkDateRange(birthDate, immunizationActivity.getTime().getValue(),ApplicationConstants.DAY_FORMAT))
					{
						actualPoints++;
					}
						
				}
			}
		}

		if(maxPoints == actualPoints)
		{
			validateTimeScore.setComment("All the time elememts under Immunization are valid.");
		}else
		{
			validateTimeScore.setComment("Some effective time elements under Immunization are not valid or not present within human lifespan");
		}
		
		validateTimeScore.setActualPoints(ApplicationUtil.calculateActualPoints(maxPoints, actualPoints));
		validateTimeScore.setMaxPoints(4);
		return validateTimeScore;
	}
	
	
	public CCDAScoreCardRubrics getValidDisplayNameScoreCard(CCDAImmunization immunizatons)
	{
		CCDAScoreCardRubrics validateDisplayNameScore = new CCDAScoreCardRubrics();
		validateDisplayNameScore.setPoints(ApplicationConstants.VALID_CODE_DISPLAYNAME_POINTS);
		validateDisplayNameScore.setRequirement(ApplicationConstants.IMMUNIZATION_CODE_DISPLAYNAME_REQUIREMENT);
		validateDisplayNameScore.setSubCategory(ApplicationConstants.SUBCATEGORIES.CODE_DISPLAYNAME_VALIDATION.getSubcategory());
		
		int maxPoints = 0;
		int actualPoints = 0;
		if(immunizatons != null)
		{
			maxPoints++;
			if(immunizatons.getSectionCode().getDisplayName()!= null)
			{
				if(ApplicationUtil.validateDisplayName(immunizatons.getSectionCode().getCode(), 
						ApplicationConstants.CODE_SYSTEM_MAP.get(immunizatons.getSectionCode().getCodeSystem()),
														immunizatons.getSectionCode().getDisplayName()))
				{
					actualPoints++;
				}
			}
			
			if(!ApplicationUtil.isEmpty(immunizatons.getImmActivity()))
			{
				for (CCDAImmunizationActivity immuActivity : immunizatons.getImmActivity())
				{
					maxPoints++;
					if(immuActivity.getApproachSiteCode().getDisplayName()!= null)
					{
						if(ApplicationUtil.validateDisplayName(immuActivity.getApproachSiteCode().getCode(), 
								ApplicationConstants.CODE_SYSTEM_MAP.get(immuActivity.getApproachSiteCode().getCodeSystem()),
																	immuActivity.getApproachSiteCode().getDisplayName()))
						{
							actualPoints++;
						}
					}
					
					if(immuActivity.getConsumable() != null)
					{
						if(!ApplicationUtil.isEmpty(immuActivity.getConsumable().getTranslations()))
						{
							for (CCDACode translationCode : immuActivity.getConsumable().getTranslations())
							{
								maxPoints++;
								if(ApplicationUtil.validateDisplayName(translationCode.getCode(), 
													ApplicationConstants.CODE_SYSTEM_MAP.get(translationCode.getCodeSystem()),
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
		
		if(maxPoints == actualPoints)
		{
			validateDisplayNameScore.setComment("All the code elements under Immunization are having valid display name");
		}else
		{
			validateDisplayNameScore.setComment("Some code elements under Immunization are not having valid display name");
		}
		
		validateDisplayNameScore.setActualPoints(ApplicationUtil.calculateActualPoints(maxPoints, actualPoints));
		validateDisplayNameScore.setMaxPoints(4);
		return validateDisplayNameScore;
	}
}
