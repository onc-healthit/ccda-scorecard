package org.sitenv.service.ccda.smartscorecard.processor;

import java.util.ArrayList;
import java.util.List;

import org.sitenv.ccdaparsing.model.CCDAII;
import org.sitenv.ccdaparsing.model.CCDASmokingStatus;
import org.sitenv.ccdaparsing.model.CCDASocialHistory;
import org.sitenv.ccdaparsing.model.CCDATobaccoUse;
import org.sitenv.service.ccda.smartscorecard.model.CCDAScoreCardRubrics;
import org.sitenv.service.ccda.smartscorecard.model.Category;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationConstants;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationUtil;
import org.springframework.stereotype.Service;

@Service
public class SocialHistoryScorecard {
	
	public Category getSocialHistoryCategory(CCDASocialHistory socialHistory, String birthDate)
	{
		
		Category socialHistoryCategory = new Category();
		socialHistoryCategory.setCategoryName("Social History");
		
		List<CCDAScoreCardRubrics> socialHistoryScoreList = new ArrayList<CCDAScoreCardRubrics>();
		socialHistoryScoreList.add(getTimePrecisionScore(socialHistory));
		socialHistoryScoreList.add(getValidDateTimeScore(socialHistory,birthDate));
		socialHistoryScoreList.add(getValidDisplayNameScoreCard(socialHistory));
		socialHistoryScoreList.add(getValidSmokingStatusScore(socialHistory));
		socialHistoryScoreList.add(getValidSmokingStatuIdScore(socialHistory));
		
		socialHistoryCategory.setCategoryRubrics(socialHistoryScoreList);
		socialHistoryCategory.setCategoryGrade(calculateSectionGrade(socialHistoryScoreList));
		
		return socialHistoryCategory;
		
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
	
	public  CCDAScoreCardRubrics getTimePrecisionScore(CCDASocialHistory socialHistory)
	{
		CCDAScoreCardRubrics timePrecisionScore = new CCDAScoreCardRubrics();
		timePrecisionScore.setPoints(ApplicationConstants.TIME_PRECISION_POINTS);
		timePrecisionScore.setRequirement(ApplicationConstants.SOCIALHISTORY_TIME_PRECISION_REQUIREMENT);
		timePrecisionScore.setSubCategory(ApplicationConstants.SUBCATEGORIES.TIME_PRECISION.getSubcategory());
		
		int actualPoints =0;
		int maxPoints = 0;
		if(socialHistory != null)
		{
			for ( CCDASmokingStatus smokingStatus : socialHistory.getSmokingStatus())
			{
				maxPoints++;
				if(smokingStatus.getObservationTime() != null)
				{
					if(ApplicationUtil.validateDayFormat(smokingStatus.getObservationTime().getValue()))
					{
						actualPoints++;
					}
				}
			}
			
			for ( CCDATobaccoUse tobaccoUse : socialHistory.getTobaccoUse())
			{
				if(tobaccoUse.getTobaccoUseTime().getLow() != null)
				{
					maxPoints++;
					if(ApplicationUtil.validateDayFormat(tobaccoUse.getTobaccoUseTime().getLow().getValue()))
					{
						actualPoints++;
					}
				}
				if(tobaccoUse.getTobaccoUseTime().getHigh() != null)
				{
					maxPoints++;
					if(ApplicationUtil.validateDayFormat(tobaccoUse.getTobaccoUseTime().getHigh().getValue()))
					{
						actualPoints++;
					}
				}
			}
		}
		
		if(maxPoints!=0 && maxPoints == actualPoints)
		{
			timePrecisionScore.setComment("All the time elememts under social History has proper precision");
		}else
		{
			timePrecisionScore.setComment("Some effective time elements under Social History are not properly precisioned");
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
	
	public  CCDAScoreCardRubrics getValidDateTimeScore(CCDASocialHistory socialHistory, String birthDate)
	{
		CCDAScoreCardRubrics validateTimeScore = new CCDAScoreCardRubrics();
		validateTimeScore.setPoints(ApplicationConstants.VALID_TIME_POINTS);
		validateTimeScore.setRequirement(ApplicationConstants.SOCIALHISTORY_TIME_PRECISION_REQUIREMENT);
		validateTimeScore.setSubCategory(ApplicationConstants.SUBCATEGORIES.TIME_PRECISION.getSubcategory());
		
		int actualPoints =0;
		int maxPoints = 0;
		if(socialHistory != null)
		{
			for ( CCDASmokingStatus smokingStatus : socialHistory.getSmokingStatus())
			{
				maxPoints++;
				if(smokingStatus.getObservationTime() != null)
				{
					if(ApplicationUtil.validateDate(smokingStatus.getObservationTime().getValue()) &&
							ApplicationUtil.checkDateRange(birthDate, smokingStatus.getObservationTime().getValue(),ApplicationConstants.DAY_FORMAT))
					{
						actualPoints++;
					}
				}
			}
			
			for ( CCDATobaccoUse tobaccoUse : socialHistory.getTobaccoUse())
			{
				if(tobaccoUse.getTobaccoUseTime().getLow() != null)
				{
					maxPoints++;
					if(ApplicationUtil.validateDate(tobaccoUse.getTobaccoUseTime().getLow().getValue()) &&
							ApplicationUtil.checkDateRange(birthDate, tobaccoUse.getTobaccoUseTime().getLow().getValue(),ApplicationConstants.DAY_FORMAT))
					{
						actualPoints++;
					}
				}
				if(tobaccoUse.getTobaccoUseTime().getHigh() != null)
				{
					maxPoints++;
					if(ApplicationUtil.validateDate(tobaccoUse.getTobaccoUseTime().getHigh().getValue()) &&
							ApplicationUtil.checkDateRange(birthDate, tobaccoUse.getTobaccoUseTime().getHigh().getValue(),ApplicationConstants.DAY_FORMAT))
					{
						actualPoints++;
					}
				}
			}
		}
		
		if(maxPoints!=0 && maxPoints == actualPoints)
		{
			validateTimeScore.setComment("All the time elememts under Social History are valid.");
		}else
		{
			validateTimeScore.setComment("Some effective time elements under Social History are not valid or not present within human lifespan");
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
	
	public  CCDAScoreCardRubrics getValidDisplayNameScoreCard(CCDASocialHistory socialHistory)
	{
		CCDAScoreCardRubrics validateDisplayNameScore = new CCDAScoreCardRubrics();
		validateDisplayNameScore.setPoints(ApplicationConstants.VALID_CODE_DISPLAYNAME_POINTS);
		validateDisplayNameScore.setRequirement(ApplicationConstants.SOCIALHISTORY_CODE_DISPLAYNAME_REQUIREMENT);
		validateDisplayNameScore.setSubCategory(ApplicationConstants.SUBCATEGORIES.CODE_DISPLAYNAME_VALIDATION.getSubcategory());
		
		int maxPoints = 0;
		int actualPoints = 0;
		if(socialHistory != null)
		{
			maxPoints++;
			if(socialHistory.getSectionCode().getDisplayName()!= null)
			{
				if(ApplicationUtil.validateDisplayName(socialHistory.getSectionCode().getCode(), 
						ApplicationConstants.CODE_SYSTEM_MAP.get(socialHistory.getSectionCode().getCodeSystem()),
											socialHistory.getSectionCode().getDisplayName()))
				{
					actualPoints++;
				}
			}
			
			if(!ApplicationUtil.isEmpty(socialHistory.getSmokingStatus()))
			{
				for (CCDASmokingStatus smokingStatus : socialHistory.getSmokingStatus())
				{
					maxPoints++;
					if(smokingStatus.getSmokingStatusCode() != null)
					{
						if(ApplicationUtil.validateDisplayName(smokingStatus.getSmokingStatusCode().getCode(), 
								ApplicationConstants.CODE_SYSTEM_MAP.get(smokingStatus.getSmokingStatusCode().getCodeSystem()),
																	smokingStatus.getSmokingStatusCode().getDisplayName()))
						{
							actualPoints++;
						}
					}
				}
			}
			
			if(!ApplicationUtil.isEmpty(socialHistory.getTobaccoUse()))
			{
				for (CCDATobaccoUse tobaccoUse : socialHistory.getTobaccoUse())
				{
					maxPoints++;
					if(tobaccoUse.getTobaccoUseCode() != null)
					{
						if(ApplicationUtil.validateDisplayName(tobaccoUse.getTobaccoUseCode().getCode(), 
								ApplicationConstants.CODE_SYSTEM_MAP.get(tobaccoUse.getTobaccoUseCode().getCodeSystem()),
														tobaccoUse.getTobaccoUseCode().getDisplayName()))
						{
							actualPoints++;
						}
					}
				}
			}
		}
		
		if(maxPoints!=0 && maxPoints == actualPoints)
		{
			validateDisplayNameScore.setComment("All the code elements under Social History are having valid display name");
		}else
		{
			validateDisplayNameScore.setComment("Some code elements under social History are not having valid display name");
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
	
	public  CCDAScoreCardRubrics getValidSmokingStatusScore(CCDASocialHistory socialHistory)
	{
		CCDAScoreCardRubrics validSmokingStausScore = new CCDAScoreCardRubrics();
		validSmokingStausScore.setPoints(ApplicationConstants.VALID_SMOKING_STATUS_POINTS);
		validSmokingStausScore.setRequirement(ApplicationConstants.SOCIALHISTORY_SMOKING_STATUS_REQUIREMENT);
		validSmokingStausScore.setSubCategory(ApplicationConstants.SUBCATEGORIES.SMOKING_STATUS.getSubcategory());
		
		int actualPoints =1;
		int maxPoints = 1;
		if(socialHistory != null)
		{
			if(!ApplicationUtil.isEmpty(socialHistory.getSmokingStatus()))
			{
				for (CCDASmokingStatus smokingStatus : socialHistory.getSmokingStatus())
				{
					if(!ApplicationConstants.SMOKING_STATUS_CODES.contains(smokingStatus.getSmokingStatusCode().getCode()))
					{
						actualPoints = 0;
					}
				}
			}
		}
		
		if(maxPoints == actualPoints)
		{
			validSmokingStausScore.setComment("Smoking status code is valid");
		}else
		{
			validSmokingStausScore.setComment("Smoking status code is not valid");
		}
		
		validSmokingStausScore.setActualPoints(actualPoints);
		validSmokingStausScore.setMaxPoints(maxPoints);
		return validSmokingStausScore;
	}
	
	public static CCDAScoreCardRubrics getValidSmokingStatuIdScore(CCDASocialHistory socialHistory)
	{
		CCDAScoreCardRubrics validSmokingStausIDScore = new CCDAScoreCardRubrics();
		validSmokingStausIDScore.setPoints(ApplicationConstants.VALID_SMOKING_STATUS_ID_POINTS);
		validSmokingStausIDScore.setRequirement(ApplicationConstants.SOCIALHISTORY_SMOKING_STATUS_OBS_ID_REQUIREMENT);
		validSmokingStausIDScore.setSubCategory(ApplicationConstants.SUBCATEGORIES.SMOKING_STATUS_ID.getSubcategory());
		
		int actualPoints =1;
		int maxPoints = 1;
		if(socialHistory != null)
		{
			if(!ApplicationUtil.isEmpty(socialHistory.getSmokingStatus()))
			{
				for (CCDASmokingStatus smokingStatus : socialHistory.getSmokingStatus())
				{
					if(!ApplicationUtil.isEmpty(smokingStatus.getSmokingStatusTemplateIds()))
					{
						for (CCDAII templateId : smokingStatus.getSmokingStatusTemplateIds())
						{
							if(!(templateId.getValue() != null && templateId.getRootValue().equals(ApplicationConstants.SMOKING_STATUS_OBSERVATION_ID)))
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
			validSmokingStausIDScore.setComment("Smoking status obervation Template Id is valid");
		}else
		{
			validSmokingStausIDScore.setComment("Smoking status observation Template Id is not valid");
		}
		
		validSmokingStausIDScore.setActualPoints(actualPoints);
		validSmokingStausIDScore.setMaxPoints(maxPoints);
		return validSmokingStausIDScore;
	}

}
