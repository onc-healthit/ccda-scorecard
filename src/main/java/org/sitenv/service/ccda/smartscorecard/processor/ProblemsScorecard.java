package org.sitenv.service.ccda.smartscorecard.processor;

import java.util.ArrayList;
import java.util.List;

import org.sitenv.ccdaparsing.model.CCDACode;
import org.sitenv.ccdaparsing.model.CCDAProblem;
import org.sitenv.ccdaparsing.model.CCDAProblemConcern;
import org.sitenv.ccdaparsing.model.CCDAProblemObs;
import org.sitenv.service.ccda.smartscorecard.model.CCDAScoreCardRubrics;
import org.sitenv.service.ccda.smartscorecard.model.Category;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationConstants;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationUtil;
import org.springframework.stereotype.Service;

@Service
public class ProblemsScorecard {
	
	public Category getProblemsCategory(CCDAProblem problems, String birthDate)
	{
		
		Category problemsCategory = new Category();
		problemsCategory.setCategoryName("Problems");
		
		List<CCDAScoreCardRubrics> problemsScoreList = new ArrayList<CCDAScoreCardRubrics>();
		problemsScoreList.add(getTimePrecisionScore(problems));
		problemsScoreList.add(getValidDateTimeScore(problems,birthDate));
		problemsScoreList.add(getValidDisplayNameScoreCard(problems));
		problemsScoreList.add(getValidProblemCodeScoreCard(problems));
		problemsScoreList.add(getValidStatusCodeScoreCard(problems));
		problemsScoreList.add(getApprEffectivetimeScore(problems));
		//problemsScoreList.add(getApprStatusCodeScore(problems));
		
		problemsCategory.setCategoryRubrics(problemsScoreList);
		problemsCategory.setCategoryGrade(calculateSectionGrade(problemsScoreList));
		
		return problemsCategory;
		
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
	
	public  CCDAScoreCardRubrics getTimePrecisionScore(CCDAProblem problem)
	{
		CCDAScoreCardRubrics timePrecisionScore = new CCDAScoreCardRubrics();
		timePrecisionScore.setPoints(ApplicationConstants.TIME_PRECISION_POINTS);
		timePrecisionScore.setRequirement(ApplicationConstants.PROBLEMS_TIME_PRECISION_REQUIREMENT);
		timePrecisionScore.setSubCategory(ApplicationConstants.SUBCATEGORIES.TIME_PRECISION.getSubcategory());
		
		int actualPoints =0;
		int maxPoints = 0;
		if(problem != null)
		{
			for (CCDAProblemConcern problemConcern : problem.getProblemConcerns())
			{
				if(problemConcern.getStatusCode() != null)
				{
					if(problemConcern.getStatusCode().getCode().equalsIgnoreCase(ApplicationConstants.CONCERNACT_STATUS.ACTIVE.getstatus()))
					{
						maxPoints++;
					}else if(problemConcern.getStatusCode().getCode().equalsIgnoreCase(ApplicationConstants.CONCERNACT_STATUS.COMPLETED.getstatus()))
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
				if(problemConcern.getEffTime() != null)
				{
					if(problemConcern.getEffTime().getHigh() != null)
					{
						if(ApplicationUtil.validateDayFormat(problemConcern.getEffTime().getHigh().getValue()))
						{
							actualPoints++;
						}
					}
					if(problemConcern.getEffTime().getLow() != null)
					{
						if(ApplicationUtil.validateDayFormat(problemConcern.getEffTime().getLow().getValue()))
						{
							actualPoints++;
						}
					}
				}
				
				if(!ApplicationUtil.isEmpty(problemConcern.getProblemObservations()))
				{
					for (CCDAProblemObs problemObs : problemConcern.getProblemObservations() )
					{
						if(problemConcern.getStatusCode() != null)
						{
							if(problemConcern.getStatusCode().getCode().equalsIgnoreCase(ApplicationConstants.CONCERNACT_STATUS.ACTIVE.getstatus()))
							{
								maxPoints++;
							}else if(problemConcern.getStatusCode().getCode().equalsIgnoreCase(ApplicationConstants.CONCERNACT_STATUS.COMPLETED.getstatus()))
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
						if(problemObs.getEffTime() != null)
						{
							if(problemObs.getEffTime().getHigh() != null)
							{
								if(ApplicationUtil.validateDayFormat(problemObs.getEffTime().getHigh().getValue()))
								{
									actualPoints++;
								}
							}
							if(problemObs.getEffTime().getLow() != null)
							{
								if(ApplicationUtil.validateDayFormat(problemObs.getEffTime().getLow().getValue()))
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
			timePrecisionScore.setComment("All the time elememts under problems are valid.");
		}else
		{
			timePrecisionScore.setComment("Some effective time elements under Problems are not properly precisioned");
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
	
	public  CCDAScoreCardRubrics getValidDateTimeScore(CCDAProblem problem,String birthDate)
	{
		CCDAScoreCardRubrics validDateTimeScore = new CCDAScoreCardRubrics();
		validDateTimeScore.setPoints(ApplicationConstants.VALID_TIME_POINTS);
		validDateTimeScore.setRequirement(ApplicationConstants.PROBLEMS_TIMEDATE_VALID_REQUIREMENT);
		validDateTimeScore.setSubCategory(ApplicationConstants.SUBCATEGORIES.TIME_VALIDATION.getSubcategory());
		
		int actualPoints =0;
		int maxPoints = 0;
		if(problem != null)
		{
			for (CCDAProblemConcern problemConcern : problem.getProblemConcerns())
			{
				if(problemConcern.getStatusCode() != null)
				{
					if(problemConcern.getStatusCode().getCode().equalsIgnoreCase(ApplicationConstants.CONCERNACT_STATUS.ACTIVE.getstatus()))
					{
						maxPoints++;
					}else if(problemConcern.getStatusCode().getCode().equalsIgnoreCase(ApplicationConstants.CONCERNACT_STATUS.COMPLETED.getstatus()))
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
				if(problemConcern.getEffTime() != null)
				{
					if(problemConcern.getEffTime().getHigh() != null)
					{
						if(ApplicationUtil.checkDateRange(birthDate, problemConcern.getEffTime().getHigh().getValue()))
						{
							actualPoints++;
						}
					}
					if(problemConcern.getEffTime().getLow() != null)
					{
						if(ApplicationUtil.checkDateRange(birthDate, problemConcern.getEffTime().getLow().getValue()))
						{
							actualPoints++;
						}
					}
				}
				
				if(!ApplicationUtil.isEmpty(problemConcern.getProblemObservations()))
				{
					for (CCDAProblemObs problemObs : problemConcern.getProblemObservations() )
					{
						if(problemConcern.getStatusCode() != null)
						{
							if(problemConcern.getStatusCode().getCode().equalsIgnoreCase(ApplicationConstants.CONCERNACT_STATUS.ACTIVE.getstatus()))
							{
								maxPoints++;
							}else if(problemConcern.getStatusCode().getCode().equalsIgnoreCase(ApplicationConstants.CONCERNACT_STATUS.COMPLETED.getstatus()))
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
						if(problemObs.getEffTime() != null)
						{
							if(problemObs.getEffTime().getHigh() != null)
							{
								if(ApplicationUtil.checkDateRange(birthDate, problemObs.getEffTime().getHigh().getValue()))
								{
									actualPoints++;
								}
							}
							if(problemObs.getEffTime().getLow() != null)
							{
								if(ApplicationUtil.checkDateRange(birthDate, problemObs.getEffTime().getLow().getValue()))
								{
									actualPoints++;
								}
							}
						}
					}
				}
			}
		}
		
		if(maxPoints!= 0 && maxPoints == actualPoints)
		{
			validDateTimeScore.setComment("All the effective time elements under problems are valid");
		}else
		{
			validDateTimeScore.setComment("Some effective time elements under problems are not valid or not present within human lifespan");
		}
		if(maxPoints!=0)
		{
			validDateTimeScore.setActualPoints(ApplicationUtil.calculateActualPoints(maxPoints, actualPoints));
		
		}else
		{
			validDateTimeScore.setActualPoints(0);
		}
		
		validDateTimeScore.setMaxPoints(4);
		return validDateTimeScore;
	}
	
	public  CCDAScoreCardRubrics getValidDisplayNameScoreCard(CCDAProblem problems)
	{
		CCDAScoreCardRubrics validateDisplayNameScore = new CCDAScoreCardRubrics();
		validateDisplayNameScore.setPoints(ApplicationConstants.VALID_CODE_DISPLAYNAME_POINTS);
		validateDisplayNameScore.setRequirement(ApplicationConstants.PROBLEMS_CODE_DISPLAYNAME_REQUIREMENT);
		validateDisplayNameScore.setSubCategory(ApplicationConstants.SUBCATEGORIES.CODE_DISPLAYNAME_VALIDATION.getSubcategory());
		
		int maxPoints = 0;
		int actualPoints = 0;
		if(problems != null)
		{
			maxPoints++;
			if(problems.getSectionCode().getDisplayName()!= null)
			{
				if(ApplicationUtil.validateDisplayName(problems.getSectionCode().getCode(), 
						ApplicationConstants.CODE_SYSTEM_MAP.get(problems.getSectionCode().getCodeSystem()),
														problems.getSectionCode().getDisplayName().toUpperCase()))
				{
					actualPoints++;
				}
			}
			
			if(!ApplicationUtil.isEmpty(problems.getProblemConcerns()))
			{
				
				for(CCDAProblemConcern probCon : problems.getProblemConcerns())
				{
					if(!ApplicationUtil.isEmpty(probCon.getProblemObservations()))
					{
					
						for (CCDAProblemObs probObs : probCon.getProblemObservations())
						{
							maxPoints= maxPoints + 2;
							if(probObs.getProblemType()!= null)
							{
								if(ApplicationUtil.validateDisplayName(probObs.getProblemType().getCode(), 
										ApplicationConstants.CODE_SYSTEM_MAP.get(probObs.getProblemType().getCodeSystem()),
																		probObs.getProblemType().getDisplayName().toUpperCase()))
								{
									actualPoints++;
								}
							}
							
							if(probObs.getProblemCode()!= null)
							{
								if(ApplicationUtil.validateDisplayName(probObs.getProblemCode().getCode(), 
										ApplicationConstants.CODE_SYSTEM_MAP.get(probObs.getProblemCode().getCodeSystem()),
																		probObs.getProblemCode().getDisplayName().toUpperCase()))
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
															translationCode.getDisplayName().toUpperCase()))
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
		
		if(maxPoints!=0 && maxPoints == actualPoints)
		{
			validateDisplayNameScore.setComment("All the code elements under Problems are having valid display name");
		}else
		{
			validateDisplayNameScore.setComment("Some code elements under Problems are not having valid display name");
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
	
	public  CCDAScoreCardRubrics getValidProblemCodeScoreCard(CCDAProblem problems)
	{
		CCDAScoreCardRubrics validateProblemCodeScore = new CCDAScoreCardRubrics();
		validateProblemCodeScore.setPoints(ApplicationConstants.PROBLEM_CODE_SCORE);
		validateProblemCodeScore.setRequirement(ApplicationConstants.PROBLEMS_CODE_LOINC_REQUIREMENT);
		validateProblemCodeScore.setSubCategory(ApplicationConstants.SUBCATEGORIES.PROBLEM_CODE.getSubcategory());
		
		int maxPoints = 0;
		int actualPoints = 0;
		if(problems != null)
		{
			if(!ApplicationUtil.isEmpty(problems.getProblemConcerns()))
			{
				for(CCDAProblemConcern probCon : problems.getProblemConcerns())
				{
				   if(!ApplicationUtil.isEmpty(probCon.getProblemObservations()))
				   {
					   for(CCDAProblemObs probObs : probCon.getProblemObservations())
					   {
						   maxPoints++;
						   if(ApplicationUtil.validateCodeForValueset(probObs.getProblemCode().getCode(), ApplicationConstants.PROBLEM_TYPE_VALUESET_OID));
						   {
							   actualPoints++;
						   }
					   }
				   }
				}
			}
		}
		
		if(maxPoints!= 0 && maxPoints == actualPoints)
		{
			validateProblemCodeScore.setComment("All the problem codes are expressed with core subset of SNOMED");
		}else
		{
			validateProblemCodeScore.setComment("Some problme codes are not expressed with core subset of SNOMED");
		}
		
		if(maxPoints!=0)
		{
			validateProblemCodeScore.setActualPoints(ApplicationUtil.calculateActualPoints(maxPoints, actualPoints));
		}else
		{
			validateProblemCodeScore.setActualPoints(0);
		}
		
		validateProblemCodeScore.setMaxPoints(4);
		return validateProblemCodeScore;
		
	}
	
	
	public  CCDAScoreCardRubrics getValidStatusCodeScoreCard(CCDAProblem problems)
	{
		CCDAScoreCardRubrics validateStatusCodeScore = new CCDAScoreCardRubrics();
		validateStatusCodeScore.setPoints(ApplicationConstants.PROBLEM_ACT_STATUS_CNST_SCORE);
		validateStatusCodeScore.setRequirement(ApplicationConstants.PROBLEMS_ACT_STATUS_CODE_REQUIREMENT);
		validateStatusCodeScore.setSubCategory(ApplicationConstants.SUBCATEGORIES.PROBLEM_STATUSCODE.getSubcategory());
		
		int maxPoints = 0;
		int actualPoints = 0;
		if(problems != null)
		{
			if(!ApplicationUtil.isEmpty(problems.getProblemConcerns()))
			{
				for(CCDAProblemConcern probCon : problems.getProblemConcerns())
				{
					if(probCon.getStatusCode() != null )
					{
						maxPoints++;
						if(ApplicationUtil.validateProblemStatusCode(probCon.getEffTime(), probCon.getStatusCode().getCode()))
						{
						   actualPoints++;
						}
					}
				}
			}
		}
		
		if(maxPoints!=0 && maxPoints == actualPoints)
		{
			validateStatusCodeScore.setComment("All the concenrn act status codes are aligned with effective time values");
		}else
		{
			validateStatusCodeScore.setComment("Some concenrn act status codes are not aligned with effective time values");
		}
		
		if(maxPoints!=0)
		{
			validateStatusCodeScore.setActualPoints(ApplicationUtil.calculateActualPoints(maxPoints, actualPoints));
		}else
		{
			validateStatusCodeScore.setActualPoints(0);
		}
		validateStatusCodeScore.setMaxPoints(4);
		return validateStatusCodeScore;
	}
	
	public CCDAScoreCardRubrics getApprEffectivetimeScore(CCDAProblem problems)
	{
		CCDAScoreCardRubrics validateApprEffectiveTimeScore = new CCDAScoreCardRubrics();
		validateApprEffectiveTimeScore.setPoints(ApplicationConstants.PROBLEMS_APPR_TIME_POINTS);
		validateApprEffectiveTimeScore.setRequirement(ApplicationConstants.PROBLEMS_CONCERN_DATE_ALIGN);
		validateApprEffectiveTimeScore.setSubCategory(ApplicationConstants.SUBCATEGORIES.TIME_ALIGN.getSubcategory());
		
		int maxPoints = 0;
		int actualPoints = 0;
		if(problems != null && !ApplicationUtil.isEmpty(problems.getProblemConcerns()))
		{
			for(CCDAProblemConcern problemAct : problems.getProblemConcerns())
			{
				if(problemAct.getEffTime()!= null)
				{
					if(!ApplicationUtil.isEmpty(problemAct.getProblemObservations()))
					{
						for(CCDAProblemObs problemObs : problemAct.getProblemObservations())
						{
							maxPoints++;
							if(problemObs.getEffTime()!=null)
							{
								if(ApplicationUtil.checkDateRange(problemObs.getEffTime().getLow(),problemObs.getEffTime().getHigh(),
													problemObs.getEffTime().getLow(),problemObs.getEffTime().getHigh()))
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
			validateApprEffectiveTimeScore.setComment("All Problems observations effective time are aligned with Problem Concern effective time");
		}else
		{
			validateApprEffectiveTimeScore.setComment("Some Problems observations effective time are not aligned with Problem Concern effective time");
		}
		
		if(maxPoints!=0)
		{
			validateApprEffectiveTimeScore.setActualPoints(ApplicationUtil.calculateActualPoints(maxPoints, actualPoints));
		}else
		{
			validateApprEffectiveTimeScore.setActualPoints(0);
		}
		validateApprEffectiveTimeScore.setMaxPoints(4);
		return validateApprEffectiveTimeScore;
	}
	
	public CCDAScoreCardRubrics getApprStatusCodeScore(CCDAProblem problems)
	{
		CCDAScoreCardRubrics validateApprEffectiveTimeScore = new CCDAScoreCardRubrics();
		validateApprEffectiveTimeScore.setPoints(ApplicationConstants.PROBLEMS_APPR_STATUS_POINTS);
		validateApprEffectiveTimeScore.setRequirement(ApplicationConstants.PROBLEMS_CONCERN_DATE_ALIGN);
		validateApprEffectiveTimeScore.setSubCategory(ApplicationConstants.SUBCATEGORIES.STATUS_ALIGN.getSubcategory());
		
		int maxPoints = 0;
		int actualPoints = 0;
		if(problems != null && !ApplicationUtil.isEmpty(problems.getProblemConcerns()))
		{
			for(CCDAProblemConcern problemAct : problems.getProblemConcerns())
			{
				if(!ApplicationUtil.isEmpty(problemAct.getProblemObservations()))
				{
					maxPoints++;
					if(ApplicationUtil.validateProblemStatusCode(problemAct.getStatusCode().getCode(), problemAct.getProblemObservations()))
					{
						actualPoints++;
					}
				}
			}
		}
		
		if(maxPoints!=0 && maxPoints == actualPoints)
		{
			validateApprEffectiveTimeScore.setComment("All Problems observations status codes are aligned with Problem Concern act status");
		}else
		{
			validateApprEffectiveTimeScore.setComment("Some Problems observations status codes are not aligned with Problem Concern act status code");
		}
		
		if(maxPoints!=0)
		{
			validateApprEffectiveTimeScore.setActualPoints(ApplicationUtil.calculateActualPoints(maxPoints, actualPoints));
		}else
		{
			validateApprEffectiveTimeScore.setActualPoints(0);
		}
		validateApprEffectiveTimeScore.setMaxPoints(4);
		return validateApprEffectiveTimeScore;
	}
}
