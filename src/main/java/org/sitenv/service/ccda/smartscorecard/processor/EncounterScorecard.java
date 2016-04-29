package org.sitenv.service.ccda.smartscorecard.processor;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.sitenv.ccdaparsing.model.CCDACode;
import org.sitenv.ccdaparsing.model.CCDAEncounter;
import org.sitenv.ccdaparsing.model.CCDAEncounterActivity;
import org.sitenv.ccdaparsing.model.CCDAEncounterDiagnosis;
import org.sitenv.ccdaparsing.model.CCDAProblemObs;
import org.sitenv.service.ccda.smartscorecard.model.CCDAScoreCardRubrics;
import org.sitenv.service.ccda.smartscorecard.model.Category;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationConstants;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationUtil;
import org.springframework.stereotype.Service;

@Service
public class EncounterScorecard {
	
	public Category getEncounterCategory(CCDAEncounter encounter, String birthDate)throws UnsupportedEncodingException
	{
		
		Category encounterCategory = new Category();
		encounterCategory.setCategoryName("Encounters");
		
		List<CCDAScoreCardRubrics> encounterScoreList = new ArrayList<CCDAScoreCardRubrics>();
		encounterScoreList.add(getTimePrecisionScore(encounter));
		encounterScoreList.add(getValidDateTimeScore(encounter,birthDate));
		encounterScoreList.add(getValidDisplayNameScoreCard(encounter));
		
		encounterCategory.setCategoryRubrics(encounterScoreList);
		encounterCategory.setCategoryGrade(calculateSectionGrade(encounterScoreList));
		
		return encounterCategory;
		
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
	
	public  CCDAScoreCardRubrics getTimePrecisionScore(CCDAEncounter encounter)
	{
		CCDAScoreCardRubrics timePrecisionScore = new CCDAScoreCardRubrics();
		timePrecisionScore.setPoints(ApplicationConstants.TIME_PRECISION_POINTS);
		timePrecisionScore.setRequirement(ApplicationConstants.ENCOUNTER_TIME_PRECISION_REQUIREMENT);
		timePrecisionScore.setSubCategory(ApplicationConstants.SUBCATEGORIES.TIME_PRECISION.getSubcategory());
		
		int maxPoints = 0;
		int actualPoints = 0;
		if(encounter != null)
		{
			for (CCDAEncounterActivity encounterActivity : encounter.getEncActivities())
			{
				maxPoints++;
				if(encounterActivity.getEffectiveTime() != null)
				{
					
					if(ApplicationUtil.validateMinuteFormat(encounterActivity.getEffectiveTime().getValue()))
					{
						actualPoints++;
					}
				}
					
				if(!ApplicationUtil.isEmpty(encounterActivity.getDiagnoses()))
				{
					for (CCDAEncounterDiagnosis encounterDiagnosis : encounterActivity.getDiagnoses())
					{
						if(!ApplicationUtil.isEmpty(encounterDiagnosis.getProblemObs()))
						{
							for (CCDAProblemObs problemObs : encounterDiagnosis.getProblemObs() )
							{
								maxPoints++;
								if(problemObs.getEffTime() != null)
								{
									if(problemObs.getEffTime().getLow() != null)
									{
										if(ApplicationUtil.validateMinuteFormat(problemObs.getEffTime().getLow().getValue()));
										{
											actualPoints++;
										}
									}
									if(problemObs.getEffTime().getHigh() != null)
									{
										maxPoints++;
										if(ApplicationUtil.validateMinuteFormat(problemObs.getEffTime().getHigh().getValue()));
										{
											actualPoints++;
										}
									}
								}
							}
						}
					}
				}
			}
		}

		if(maxPoints == actualPoints)
		{
			timePrecisionScore.setComment("All the time elememts under encounter section has proper precision");
		}else
		{
			timePrecisionScore.setComment("Some effective time elements under encounter are not properly precisioned");
		}
		
		timePrecisionScore.setActualPoints(ApplicationUtil.calculateActualPoints(maxPoints, actualPoints));
		timePrecisionScore.setMaxPoints(4);
		return timePrecisionScore;
	}
	
	public CCDAScoreCardRubrics getValidDateTimeScore(CCDAEncounter encounter, String birthDate)
	{
		CCDAScoreCardRubrics validateTimeScore = new CCDAScoreCardRubrics();
		validateTimeScore.setPoints(ApplicationConstants.VALID_TIME_POINTS);
		validateTimeScore.setRequirement(ApplicationConstants.ENCOUNTER_TIMEDATE_VALID_REQUIREMENT);
		validateTimeScore.setSubCategory(ApplicationConstants.SUBCATEGORIES.TIME_VALIDATION.getSubcategory());
		
		int maxPoints = 0;
		int actualPoints = 0;
		if(encounter != null)
		{
			for (CCDAEncounterActivity encounterActivity : encounter.getEncActivities())
			{
				maxPoints++;
				if(encounterActivity.getEffectiveTime() != null)
				{
					if(ApplicationUtil.validateDateTime(encounterActivity.getEffectiveTime().getValue()) &&
							ApplicationUtil.checkDateRange(birthDate, encounterActivity.getEffectiveTime().getValue(),ApplicationConstants.MINUTE_FORMAT))
					{
						actualPoints++;
					}
				}
					
				if(!ApplicationUtil.isEmpty(encounterActivity.getDiagnoses()))
				{
					for (CCDAEncounterDiagnosis encounterDiagnosis : encounterActivity.getDiagnoses())
					{
						if(!ApplicationUtil.isEmpty(encounterDiagnosis.getProblemObs()))
						{
							for (CCDAProblemObs problemObs : encounterDiagnosis.getProblemObs() )
							{
								maxPoints++;
								if(problemObs.getEffTime() != null)
								{
									if(problemObs.getEffTime().getLow() != null)
									{
										if(ApplicationUtil.validateDateTime(problemObs.getEffTime().getLow().getValue()) &&
												ApplicationUtil.checkDateRange(birthDate, problemObs.getEffTime().getLow().getValue(),ApplicationConstants.MINUTE_FORMAT))
										{
											actualPoints++;
										}
									}
									if(problemObs.getEffTime().getHigh() != null)
									{
										maxPoints++;
										if(ApplicationUtil.validateDateTime(problemObs.getEffTime().getHigh().getValue()) &&
												ApplicationUtil.checkDateRange(birthDate, problemObs.getEffTime().getHigh().getValue(),ApplicationConstants.MINUTE_FORMAT))
										{
											actualPoints++;
										}
									}
								}
							}
						}
					}
				}
			}
		}

		if(maxPoints == actualPoints)
		{
			validateTimeScore.setComment("All the time elememts under encounters section are valid.");
		}else
		{
			validateTimeScore.setComment("Some effective time elements under Encounters section are not valid or not present within human lifespan");
		}
		
		validateTimeScore.setActualPoints(ApplicationUtil.calculateActualPoints(maxPoints, actualPoints));
		validateTimeScore.setMaxPoints(4);
		return validateTimeScore;
	}
	
	public CCDAScoreCardRubrics getValidDisplayNameScoreCard(CCDAEncounter encounters)throws UnsupportedEncodingException
	{
		CCDAScoreCardRubrics validateDisplayNameScore = new CCDAScoreCardRubrics();
		validateDisplayNameScore.setPoints(ApplicationConstants.VALID_CODE_DISPLAYNAME_POINTS);
		validateDisplayNameScore.setRequirement(ApplicationConstants.ENCOUNTER_CODE_DISPLAYNAME_REQUIREMENT);
		validateDisplayNameScore.setSubCategory(ApplicationConstants.SUBCATEGORIES.CODE_DISPLAYNAME_VALIDATION.getSubcategory());
		
		int maxPoints = 0;
		int actualPoints = 0;
		if(encounters != null)
		{
			maxPoints++;
			if(encounters.getSectionCode().getDisplayName()!= null)
			{
				if(ApplicationUtil.validateDisplayName(encounters.getSectionCode().getCode(), 
												ApplicationConstants.CODE_SYSTEM_MAP.get(encounters.getSectionCode().getCodeSystem()),
											encounters.getSectionCode().getDisplayName()))
				{
					actualPoints++;
				}
			}
			
			if(!ApplicationUtil.isEmpty(encounters.getEncActivities()))
			{
			
				for (CCDAEncounterActivity encounterActivity : encounters.getEncActivities())
				{
					maxPoints++;
					if(encounterActivity.getEncounterTypeCode()!= null)
					{
						if(ApplicationUtil.validateDisplayName(encounterActivity.getEncounterTypeCode().getCode(), 
									ApplicationConstants.CODE_SYSTEM_MAP.get(encounterActivity.getEncounterTypeCode().getCodeSystem()),
																encounterActivity.getEncounterTypeCode().getDisplayName()))
						{
							actualPoints++;
						}
					}
					
					if(!ApplicationUtil.isEmpty(encounterActivity.getIndications()))
					{
					
						for (CCDAProblemObs indication : encounterActivity.getIndications())
						{
							maxPoints = maxPoints +2;
							if(indication.getProblemType()!= null)
							{
								if(ApplicationUtil.validateDisplayName(indication.getProblemType().getCode(), 
											ApplicationConstants.CODE_SYSTEM_MAP.get(indication.getProblemType().getCodeSystem()),
																			indication.getProblemType().getDisplayName()))
								{
									actualPoints++;
								}
							}
							
							if(indication.getProblemCode()!= null)
							{
								if(ApplicationUtil.validateDisplayName(indication.getProblemCode().getCode(), 
										ApplicationConstants.CODE_SYSTEM_MAP.get(indication.getProblemCode().getCodeSystem()),
																			indication.getProblemCode().getDisplayName()))
								{
									actualPoints++;
								}
							}
						}
					}
					
					if(!ApplicationUtil.isEmpty(encounterActivity.getDiagnoses()))
					{
					
						for (CCDAEncounterDiagnosis diagnosis : encounterActivity.getDiagnoses())
						{
							maxPoints++;
							if(diagnosis.getEntryCode()!= null)
							{
								if(ApplicationUtil.validateDisplayName(diagnosis.getEntryCode().getCode(), 
										ApplicationConstants.CODE_SYSTEM_MAP.get(diagnosis.getEntryCode().getCodeSystem()),
																diagnosis.getEntryCode().getDisplayName()))
								{
									actualPoints++;
								}
							}
							if(!ApplicationUtil.isEmpty(diagnosis.getProblemObs()))
							{
								for (CCDAProblemObs probObs : diagnosis.getProblemObs())
								{
									maxPoints= maxPoints + 2;
									if(probObs.getProblemType()!= null)
									{
										if(ApplicationUtil.validateDisplayName(probObs.getProblemType().getCode(), 
												ApplicationConstants.CODE_SYSTEM_MAP.get(probObs.getProblemType().getCodeSystem()),
																				probObs.getProblemType().getDisplayName()))
										{
											actualPoints++;
										}
									}
									
									if(probObs.getProblemCode()!= null)
									{
										if(ApplicationUtil.validateDisplayName(probObs.getProblemCode().getCode(), 
												ApplicationConstants.CODE_SYSTEM_MAP.get(probObs.getProblemCode().getCodeSystem()),
																				probObs.getProblemCode().getDisplayName()))
										{
											actualPoints++;
										}
									}
									
									if(!ApplicationUtil.isEmpty(probObs.getTranslationProblemType()))
									{
										for (CCDACode translationCode : probObs.getTranslationProblemType())
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
				}
			}
		}
		
		if(maxPoints == actualPoints)
		{
			validateDisplayNameScore.setComment("All the code elements under Encounters are having valid display name");
		}else
		{
			validateDisplayNameScore.setComment("Some code elements under Encounters are not having valid display name");
		}
		
		validateDisplayNameScore.setActualPoints(ApplicationUtil.calculateActualPoints(maxPoints, actualPoints));
		validateDisplayNameScore.setMaxPoints(4);
		return validateDisplayNameScore;
	}
	
	
}
