package org.sitenv.service.ccda.smartscorecard.processor;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.sitenv.ccdaparsing.model.CCDAAllergy;
import org.sitenv.ccdaparsing.model.CCDAAllergyConcern;
import org.sitenv.ccdaparsing.model.CCDAAllergyObs;
import org.sitenv.service.ccda.smartscorecard.model.CCDAScoreCardRubrics;
import org.sitenv.service.ccda.smartscorecard.model.Category;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationConstants;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationUtil;
import org.springframework.stereotype.Service;

@Service
public class AllergiesScorecard {
	
	public Category getAllergiesCategory(CCDAAllergy allergies, String birthDate)throws UnsupportedEncodingException
	{
		
		Category allergyCategory = new Category();
		allergyCategory.setCategoryName("Allergies");
		
		List<CCDAScoreCardRubrics> allergyScoreList = new ArrayList<CCDAScoreCardRubrics>();
		allergyScoreList.add(getTimePrecisionScore(allergies));
		allergyScoreList.add(getValidDateTimeScore(allergies, birthDate));
		allergyScoreList.add(getValidDisplayNameScoreCard(allergies));
		
		allergyCategory.setCategoryRubrics(allergyScoreList);
		allergyCategory.setCategoryGrade(calculateSectionGrade(allergyScoreList));
		
		return allergyCategory;
		
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
	
	
	public CCDAScoreCardRubrics getTimePrecisionScore(CCDAAllergy allergies)
	{
		CCDAScoreCardRubrics timePrecisionScore = new CCDAScoreCardRubrics();
		timePrecisionScore.setPoints(ApplicationConstants.TIME_PRECISION_POINTS);
		timePrecisionScore.setRequirement(ApplicationConstants.ALLERGIES_TIME_PRECISION_REQUIREMENT);
		timePrecisionScore.setSubCategory(ApplicationConstants.SUBCATEGORIES.TIME_PRECISION.getSubcategory());
		
		int maxPoints = 0;
		int actualPoints = 0;
		if(allergies != null)
		{
			for (CCDAAllergyConcern allergyConcern : allergies.getAllergyConcern())
			{
				if(allergyConcern.getStatusCode() != null)
				{
					if(allergyConcern.getStatusCode().getCode().equals(ApplicationConstants.PROBLEMACT_STATUS.ACTIVE))
					{
						maxPoints++;
					}else if(allergyConcern.getStatusCode().getCode().equals(ApplicationConstants.PROBLEMACT_STATUS.COMPLETED))
					{
							maxPoints = maxPoints + 2;
					}else
					{
						maxPoints++;
					}
				}else
				{
					maxPoints++;
				}
				if(allergyConcern.getEffTime() != null)
				{
					if(allergyConcern.getEffTime().getHigh() != null)
					{
						if(ApplicationUtil.validateMinuteFormat(allergyConcern.getEffTime().getHigh().getValue()))
						{
							actualPoints++;
						}
					}
					if(allergyConcern.getEffTime().getLow() != null)
					{
						if(ApplicationUtil.validateMinuteFormat(allergyConcern.getEffTime().getLow().getValue()))
						{
							actualPoints++;
						}
					}
				}
					
				if(!ApplicationUtil.isEmpty(allergyConcern.getAllergyObs()))
				{
					for (CCDAAllergyObs allergyObservation : allergyConcern.getAllergyObs() )
					{
						if(allergyConcern.getStatusCode() != null)
						{
							if(allergyConcern.getStatusCode().getCode().equals(ApplicationConstants.PROBLEMACT_STATUS.ACTIVE))
							{
								maxPoints++;
							}else if(allergyConcern.getStatusCode().getCode().equals(ApplicationConstants.PROBLEMACT_STATUS.COMPLETED))
							{
									maxPoints = maxPoints + 2;
							}else
							{
								maxPoints++;
							}
						}else
						{
							maxPoints++;
						}
						if(allergyObservation.getEffTime() != null)
						{
							if(allergyObservation.getEffTime().getHigh() != null)
							{
								if(ApplicationUtil.validateMinuteFormat(allergyObservation.getEffTime().getHigh().getValue()))
								{
									actualPoints++;
								}
							}
							if(allergyObservation.getEffTime().getLow() != null)
							{
								if(ApplicationUtil.validateMinuteFormat(allergyObservation.getEffTime().getLow().getValue()))
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
			timePrecisionScore.setComment("All the time elememts under allergies section has proper precision");
		}else
		{
			timePrecisionScore.setComment("Some effective time elements under allergies are not properly precisioned");
		}
		
		timePrecisionScore.setActualPoints(ApplicationUtil.calculateActualPoints(maxPoints, actualPoints));
		timePrecisionScore.setMaxPoints(4);
		return timePrecisionScore;
	}
	
	public CCDAScoreCardRubrics getValidDateTimeScore(CCDAAllergy allergies, String birthDate)
	{
		CCDAScoreCardRubrics validateTimeScore = new CCDAScoreCardRubrics();
		validateTimeScore.setPoints(ApplicationConstants.VALID_TIME_POINTS);
		validateTimeScore.setRequirement(ApplicationConstants.ALLERGIES_TIMEDATE_VALID_REQUIREMENT);
		validateTimeScore.setSubCategory(ApplicationConstants.SUBCATEGORIES.TIME_VALIDATION.getSubcategory());
		
		int maxPoints = 0;
		int actualPoints = 0;
		if(allergies != null)
		{
			for (CCDAAllergyConcern allergyConcern : allergies.getAllergyConcern())
			{
				if(allergyConcern.getStatusCode() != null)
				{
					if(allergyConcern.getStatusCode().getCode().equals(ApplicationConstants.PROBLEMACT_STATUS.ACTIVE))
					{
						maxPoints++;
					}else if(allergyConcern.getStatusCode().getCode().equals(ApplicationConstants.PROBLEMACT_STATUS.COMPLETED))
					{
							maxPoints = maxPoints + 2;
					}else
					{
						maxPoints++;
					}
				}else
				{
					maxPoints++;
				}
				if(allergyConcern.getEffTime() != null)
				{
					if(allergyConcern.getEffTime().getHigh() != null)
					{
						if(ApplicationUtil.validateDateTime(allergyConcern.getEffTime().getHigh().getValue()) &&
								ApplicationUtil.checkDateRange(birthDate, allergyConcern.getEffTime().getHigh().getValue(),ApplicationConstants.MINUTE_FORMAT))
						{
							actualPoints++;
						}
					}
					if(allergyConcern.getEffTime().getLow() != null)
					{
						if(ApplicationUtil.validateDateTime(allergyConcern.getEffTime().getLow().getValue()) &&
								ApplicationUtil.checkDateRange(birthDate, allergyConcern.getEffTime().getLow().getValue(), ApplicationConstants.MINUTE_FORMAT))
						{
							actualPoints++;
						}
					}
				}
					
				if(!ApplicationUtil.isEmpty(allergyConcern.getAllergyObs()))
				{
					for (CCDAAllergyObs allergyObservation : allergyConcern.getAllergyObs() )
					{
						if(allergyConcern.getStatusCode() != null)
						{
							if(allergyConcern.getStatusCode().getCode().equals(ApplicationConstants.PROBLEMACT_STATUS.ACTIVE))
							{
								maxPoints++;
							}else if(allergyConcern.getStatusCode().getCode().equals(ApplicationConstants.PROBLEMACT_STATUS.COMPLETED))
							{
									maxPoints = maxPoints + 2;
							}else
							{
								maxPoints++;
							}
						}else
						{
							maxPoints++;
						}
						if(allergyObservation.getEffTime() != null)
						{
							if(allergyObservation.getEffTime().getHigh() != null)
							{
								if(ApplicationUtil.validateDateTime(allergyObservation.getEffTime().getHigh().getValue()) && 
											ApplicationUtil.checkDateRange(birthDate, allergyObservation.getEffTime().getHigh().getValue(),ApplicationConstants.MINUTE_FORMAT))
								{
									actualPoints++;
								}
							}
							if(allergyObservation.getEffTime().getLow() != null)
							{
								if(ApplicationUtil.validateDateTime(allergyObservation.getEffTime().getLow().getValue()) &&
										ApplicationUtil.checkDateRange(birthDate, allergyObservation.getEffTime().getLow().getValue(), ApplicationConstants.MINUTE_FORMAT))
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
			validateTimeScore.setComment("All the time elememts under allergies are valid.");
		}else
		{
			validateTimeScore.setComment("Some effective time elements under allergies are not valid or not present within human lifespan");
		}
		
		validateTimeScore.setActualPoints(ApplicationUtil.calculateActualPoints(maxPoints, actualPoints));
		validateTimeScore.setMaxPoints(4);
		return validateTimeScore;
	}
	
	
	public CCDAScoreCardRubrics getValidDisplayNameScoreCard(CCDAAllergy allergies)throws UnsupportedEncodingException
	{
		CCDAScoreCardRubrics validateDisplayNameScore = new CCDAScoreCardRubrics();
		validateDisplayNameScore.setPoints(ApplicationConstants.VALID_CODE_DISPLAYNAME_POINTS);
		validateDisplayNameScore.setRequirement(ApplicationConstants.ALLERGIES_CODE_DISPLAYNAME_REQUIREMENT);
		validateDisplayNameScore.setSubCategory(ApplicationConstants.SUBCATEGORIES.CODE_DISPLAYNAME_VALIDATION.getSubcategory());
		
		int maxPoints = 0;
		int actualPoints = 0;
		if(allergies != null)
		{
			maxPoints++;
			if(allergies.getSectionCode().getDisplayName()!= null)
			{
				if(ApplicationUtil.validateDisplayName(allergies.getSectionCode().getCode(), ApplicationConstants.CODE_SYSTEM_MAP.get(allergies.getSectionCode().getCodeSystem()),
														allergies.getSectionCode().getDisplayName()))
				{
					actualPoints++;
				}
			}
			
			if(!ApplicationUtil.isEmpty(allergies.getAllergyConcern()))
			{
				for (CCDAAllergyConcern allergyConcern : allergies.getAllergyConcern())
				{
					if(!ApplicationUtil.isEmpty(allergyConcern.getAllergyObs()))
					{
						for (CCDAAllergyObs allergyObs : allergyConcern.getAllergyObs())
						{
							maxPoints = maxPoints + 2;
							if(allergyObs.getAllergyIntoleranceType() != null)
							{
								if(ApplicationUtil.validateDisplayName(allergyObs.getAllergyIntoleranceType().getCode(), ApplicationConstants.CODE_SYSTEM_MAP.get(allergyObs.getAllergyIntoleranceType().getCodeSystem()),
																allergyObs.getAllergyIntoleranceType().getDisplayName()))
								{
									actualPoints++;
								}
							}
							
							if(allergyObs.getAllergySubstance() != null)
							{
								if(ApplicationUtil.validateDisplayName(allergyObs.getAllergySubstance().getCode(), ApplicationConstants.CODE_SYSTEM_MAP.get(allergyObs.getAllergySubstance().getCodeSystem()),
																allergyObs.getAllergySubstance().getDisplayName()))
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
			validateDisplayNameScore.setComment("All the code elements under Allergies are having valid display name");
		}else
		{
			validateDisplayNameScore.setComment("Some code elements under allergies are not having valid display name");
		}
		
		validateDisplayNameScore.setActualPoints(ApplicationUtil.calculateActualPoints(maxPoints, actualPoints));
		validateDisplayNameScore.setMaxPoints(4);
		return validateDisplayNameScore;
	}
		
}
