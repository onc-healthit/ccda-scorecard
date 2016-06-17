package org.sitenv.service.ccda.smartscorecard.processor;

import java.util.ArrayList;
import java.util.List;

import org.sitenv.ccdaparsing.model.CCDACode;
import org.sitenv.ccdaparsing.model.CCDAProblem;
import org.sitenv.ccdaparsing.model.CCDAProblemConcern;
import org.sitenv.ccdaparsing.model.CCDAProblemObs;
import org.sitenv.ccdaparsing.model.CCDAXmlSnippet;
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
		problemsCategory.setCategoryName(ApplicationConstants.CATEGORIES.PROBLEMS.getCategoryDesc());
		
		List<CCDAScoreCardRubrics> problemsScoreList = new ArrayList<CCDAScoreCardRubrics>();
		problemsScoreList.add(getTimePrecisionScore(problems));
		problemsScoreList.add(getValidDateTimeScore(problems,birthDate));
		problemsScoreList.add(getValidDisplayNameScoreCard(problems));
		problemsScoreList.add(getValidProblemCodeScoreCard(problems));
		problemsScoreList.add(getValidStatusCodeScoreCard(problems));
		problemsScoreList.add(getApprEffectivetimeScore(problems));
		problemsScoreList.add(getApprStatusCodeScore(problems));
		
		ApplicationUtil.calculateSectionGradeAndIssues(problemsScoreList, problemsCategory);
		
		problemsCategory.setCategoryRubrics(problemsScoreList);
		
		return problemsCategory;
		
	}
	
	
	public  CCDAScoreCardRubrics getTimePrecisionScore(CCDAProblem problem)
	{
		CCDAScoreCardRubrics timePrecisionScore = new CCDAScoreCardRubrics();
		timePrecisionScore.setRule(ApplicationConstants.TIME_PRECISION_REQUIREMENT);
		
		int actualPoints =0;
		int maxPoints = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue= null;
		if(problem != null)
		{
			if(!ApplicationUtil.isEmpty(problem.getProblemConcerns()))
			{
				for (CCDAProblemConcern problemConcern : problem.getProblemConcerns())
				{
					maxPoints++;
					if(problemConcern.getEffTime() != null)
					{
						if(problemConcern.getEffTime().getLow() != null)
						{
							if(ApplicationUtil.validateDayFormat(problemConcern.getEffTime().getLow().getValue()) ||
									ApplicationUtil.validateMonthFormat(problemConcern.getEffTime().getLow().getValue()))
							{
								actualPoints++;
							}
							else 
							{
								issue = new CCDAXmlSnippet();
								issue.setLineNumber(problemConcern.getEffTime().getLow().getLineNumber());
								issue.setXmlString(problemConcern.getEffTime().getLow().getXmlString());
								issuesList.add(issue);
							}
						}
						else
						{
							issue = new CCDAXmlSnippet();
							issue.setLineNumber(problemConcern.getEffTime().getLineNumber());
							issue.setXmlString(problemConcern.getEffTime().getXmlString());
							issuesList.add(issue);
						}
						if(problemConcern.getEffTime().getHigh() != null)
						{
							maxPoints++;
							if(ApplicationUtil.validateDayFormat(problemConcern.getEffTime().getHigh().getValue()) ||
									ApplicationUtil.validateMonthFormat(problemConcern.getEffTime().getHigh().getValue()))
							{
								actualPoints++;
							}
							else 
							{
								issue = new CCDAXmlSnippet();
								issue.setLineNumber(problemConcern.getEffTime().getHigh().getLineNumber());
								issue.setXmlString(problemConcern.getEffTime().getHigh().getXmlString());
								issuesList.add(issue);
							}
						}
					}
					else 
					{
						issue = new CCDAXmlSnippet();
						issue.setLineNumber(problemConcern.getLineNumber());
						issue.setXmlString(problemConcern.getXmlString());
						issuesList.add(issue);
					}
					
					if(!ApplicationUtil.isEmpty(problemConcern.getProblemObservations()))
					{
						for (CCDAProblemObs problemObs : problemConcern.getProblemObservations() )
						{
							maxPoints++;
							if(problemObs.getEffTime() != null)
							{
								if(problemObs.getEffTime().getLow() != null)
								{
									if(ApplicationUtil.validateDayFormat(problemObs.getEffTime().getLow().getValue()) ||
											ApplicationUtil.validateMonthFormat(problemObs.getEffTime().getLow().getValue()))
									{
										actualPoints++;
									}
									else 
									{
										issue = new CCDAXmlSnippet();
										issue.setLineNumber(problemObs.getEffTime().getLow().getLineNumber());
										issue.setXmlString(problemObs.getEffTime().getLow().getXmlString());
										issuesList.add(issue);
									}
								}
								else
								{
									issue = new CCDAXmlSnippet();
									issue.setLineNumber(problemObs.getEffTime().getLineNumber());
									issue.setXmlString(problemObs.getEffTime().getXmlString());
									issuesList.add(issue);
								}
								if(problemObs.getEffTime().getHigh() != null)
								{
									maxPoints++;
									if(ApplicationUtil.validateDayFormat(problemObs.getEffTime().getHigh().getValue()) ||
											ApplicationUtil.validateMonthFormat(problemObs.getEffTime().getHigh().getValue()))
									{
										actualPoints++;
									}
									else 
									{
										issue = new CCDAXmlSnippet();
										issue.setLineNumber(problemObs.getEffTime().getHigh().getLineNumber());
										issue.setXmlString(problemObs.getEffTime().getHigh().getXmlString());
										issuesList.add(issue);
									}
								}
							}
							else
							{
								issue = new CCDAXmlSnippet();
								issue.setLineNumber(problemObs.getLineNumber());
								issue.setXmlString(problemObs.getXmlString());
								issuesList.add(issue);
							}
						}
					}
				}
			}
			else
			{
				issue = new CCDAXmlSnippet();
				issue.setLineNumber(problem.getLineNumber());
				issue.setXmlString(problem.getXmlString());
				issuesList.add(issue);
			}
		}
		else
		{
			issue = new CCDAXmlSnippet();
			issue.setLineNumber("Problmes section not present");
			issue.setXmlString("Problmes section not present");
			issuesList.add(issue);
		}
		
		
		timePrecisionScore.setActualPoints(actualPoints);
		timePrecisionScore.setMaxPoints(maxPoints);
		timePrecisionScore.setRubricScore(ApplicationUtil.calculateRubricScore(maxPoints, actualPoints));
		timePrecisionScore.setIssuesList(issuesList);
		timePrecisionScore.setNumberOfIssues(issuesList.size());
		if(issuesList.size() > 0)
		{
			timePrecisionScore.setDescription(ApplicationConstants.TIME_PRECISION_DESCRIPTION);
			timePrecisionScore.getIgReferences().add(ApplicationConstants.IG_SECTION_REFERENCES);
			timePrecisionScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_URL);
		}
		return timePrecisionScore;
	}
	
	public  CCDAScoreCardRubrics getValidDateTimeScore(CCDAProblem problem,String birthDate)
	{
		CCDAScoreCardRubrics validDateTimeScore = new CCDAScoreCardRubrics();
		validDateTimeScore.setRule(ApplicationConstants.TIME_VALID_REQUIREMENT);
		
		int actualPoints =0;
		int maxPoints = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue= null;
		if(problem != null)
		{
			if(!ApplicationUtil.isEmpty(problem.getProblemConcerns()))
			{
				for (CCDAProblemConcern problemConcern : problem.getProblemConcerns())
				{
					maxPoints++;
					if(problemConcern.getEffTime() != null)
					{
						if(problemConcern.getEffTime().getLow() != null)
						{
							if(ApplicationUtil.checkDateRange(birthDate, problemConcern.getEffTime().getLow().getValue()))
							{
								actualPoints++;
							}
							else 
							{
								issue = new CCDAXmlSnippet();
								issue.setLineNumber(problemConcern.getEffTime().getLow().getLineNumber());
								issue.setXmlString(problemConcern.getEffTime().getLow().getXmlString());
								issuesList.add(issue);
							}
						}
						else
						{
							issue = new CCDAXmlSnippet();
							issue.setLineNumber(problemConcern.getEffTime().getLineNumber());
							issue.setXmlString(problemConcern.getEffTime().getXmlString());
							issuesList.add(issue);
						}
						if(problemConcern.getEffTime().getHigh() != null)
						{
							maxPoints++;
							if(ApplicationUtil.checkDateRange(birthDate, problemConcern.getEffTime().getHigh().getValue()))
							{
								actualPoints++;
							}
							else 
							{
								issue = new CCDAXmlSnippet();
								issue.setLineNumber(problemConcern.getEffTime().getHigh().getLineNumber());
								issue.setXmlString(problemConcern.getEffTime().getHigh().getXmlString());
								issuesList.add(issue);
							}
						}
					}
					else 
					{
						issue = new CCDAXmlSnippet();
						issue.setLineNumber(problemConcern.getLineNumber());
						issue.setXmlString(problemConcern.getXmlString());
						issuesList.add(issue);
					}
					
					if(!ApplicationUtil.isEmpty(problemConcern.getProblemObservations()))
					{
						for (CCDAProblemObs problemObs : problemConcern.getProblemObservations() )
						{
							maxPoints++;
							if(problemObs.getEffTime() != null)
							{
								if(problemObs.getEffTime().getLow() != null)
								{
									if(ApplicationUtil.checkDateRange(birthDate, problemObs.getEffTime().getLow().getValue()))
									{
										actualPoints++;
									}
									else 
									{
										issue = new CCDAXmlSnippet();
										issue.setLineNumber(problemObs.getEffTime().getLow().getLineNumber());
										issue.setXmlString(problemObs.getEffTime().getLow().getXmlString());
										issuesList.add(issue);
									}
								}
								else 
								{
									issue = new CCDAXmlSnippet();
									issue.setLineNumber(problemObs.getEffTime().getLineNumber());
									issue.setXmlString(problemObs.getEffTime().getXmlString());
									issuesList.add(issue);
								}
								if(problemObs.getEffTime().getHigh() != null)
								{
									maxPoints++;
									if(ApplicationUtil.checkDateRange(birthDate, problemObs.getEffTime().getHigh().getValue()))
									{
										actualPoints++;
									}
									else 
									{
										issue = new CCDAXmlSnippet();
										issue.setLineNumber(problemObs.getEffTime().getHigh().getLineNumber());
										issue.setXmlString(problemObs.getEffTime().getHigh().getXmlString());
										issuesList.add(issue);
									}
								}
							}
							else
							{
								issue = new CCDAXmlSnippet();
								issue.setLineNumber(problemObs.getLineNumber());
								issue.setXmlString(problemObs.getXmlString());
								issuesList.add(issue);
							}
						}
					}
				}
			}
			else
			{
				issue = new CCDAXmlSnippet();
				issue.setLineNumber(problem.getLineNumber());
				issue.setXmlString(problem.getXmlString());
				issuesList.add(issue);
			}
		}
		else
		{
			issue = new CCDAXmlSnippet();
			issue.setLineNumber("Problmes section not present");
			issue.setXmlString("Problmes section not present");
			issuesList.add(issue);
		}
		
		validDateTimeScore.setActualPoints(actualPoints);
		validDateTimeScore.setMaxPoints(maxPoints);
		validDateTimeScore.setRubricScore(ApplicationUtil.calculateRubricScore(maxPoints, actualPoints));
		validDateTimeScore.setIssuesList(issuesList);
		validDateTimeScore.setNumberOfIssues(issuesList.size());
		if(issuesList.size() > 0)
		{
			validDateTimeScore.setDescription(ApplicationConstants.TIME_VALID_DESCRIPTION);
			validDateTimeScore.getIgReferences().add(ApplicationConstants.IG_SECTION_REFERENCES);
			validDateTimeScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_URL);
		}
		return validDateTimeScore;
	}
	
	public  CCDAScoreCardRubrics getValidDisplayNameScoreCard(CCDAProblem problems)
	{
		CCDAScoreCardRubrics validateDisplayNameScore = new CCDAScoreCardRubrics();
		validateDisplayNameScore.setRule(ApplicationConstants.CODE_DISPLAYNAME_REQUIREMENT);
		
		int maxPoints = 0;
		int actualPoints = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue= null;
		if(problems != null)
		{
			maxPoints++;
			if(problems.getSectionCode()!= null)
			{
				if(ApplicationUtil.validateDisplayName(problems.getSectionCode().getCode(), 
						ApplicationConstants.CODE_SYSTEM_MAP.get(problems.getSectionCode().getCodeSystem()),
														problems.getSectionCode().getDisplayName()))
				{
					actualPoints++;
				}
				else 
				{
					issue = new CCDAXmlSnippet();
					issue.setLineNumber(problems.getSectionCode().getLineNumber());
					issue.setXmlString(problems.getSectionCode().getXmlString());
					issuesList.add(issue);
				}
			}
			else
			{
				issue = new CCDAXmlSnippet();
				issue.setLineNumber(problems.getLineNumber());
				issue.setXmlString(problems.getXmlString());
				issuesList.add(issue);
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
																		probObs.getProblemType().getDisplayName()))
								{
									actualPoints++;
								}
								else 
								{
									issue = new CCDAXmlSnippet();
									issue.setLineNumber(probObs.getProblemType().getLineNumber());
									issue.setXmlString(probObs.getProblemType().getXmlString());
									issuesList.add(issue);
								}
							}
							else
							{
								issue = new CCDAXmlSnippet();
								issue.setLineNumber(probObs.getLineNumber());
								issue.setXmlString(probObs.getXmlString());
								issuesList.add(issue);
							}
							
							if(probObs.getProblemCode()!= null)
							{
								if(ApplicationUtil.validateDisplayName(probObs.getProblemCode().getCode(), 
										ApplicationConstants.CODE_SYSTEM_MAP.get(probObs.getProblemCode().getCodeSystem()),
																		probObs.getProblemCode().getDisplayName()))
								{
									actualPoints++;
								}
								else 
								{
									issue = new CCDAXmlSnippet();
									issue.setLineNumber(probObs.getProblemCode().getLineNumber());
									issue.setXmlString(probObs.getProblemCode().getXmlString());
									issuesList.add(issue);
								}
							}
							else
							{
								issue = new CCDAXmlSnippet();
								issue.setLineNumber(probObs.getLineNumber());
								issue.setXmlString(probObs.getXmlString());
								issuesList.add(issue);
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
									else 
									{
										issue = new CCDAXmlSnippet();
										issue.setLineNumber(translationCode.getLineNumber());
										issue.setXmlString(translationCode.getXmlString());
										issuesList.add(issue);
									}
									
								}
							}
						}
					}
				}
			}
		}
		else
		{
			issue = new CCDAXmlSnippet();
			issue.setLineNumber("Problmes section not present");
			issue.setXmlString("Problmes section not present");
			issuesList.add(issue);
		}
		
		validateDisplayNameScore.setActualPoints(actualPoints);
		validateDisplayNameScore.setMaxPoints(maxPoints);
		validateDisplayNameScore.setRubricScore(ApplicationUtil.calculateRubricScore(maxPoints, actualPoints));
		validateDisplayNameScore.setIssuesList(issuesList);
		validateDisplayNameScore.setNumberOfIssues(issuesList.size());
		if(issuesList.size() > 0)
		{
			validateDisplayNameScore.setDescription(ApplicationConstants.CODE_DISPLAYNAME_DESCRIPTION);
			validateDisplayNameScore.getIgReferences().add(ApplicationConstants.IG_SECTION_REFERENCES);
			validateDisplayNameScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_URL);
		}
		return validateDisplayNameScore;
	}
	
	public  CCDAScoreCardRubrics getValidProblemCodeScoreCard(CCDAProblem problems)
	{
		CCDAScoreCardRubrics validateProblemCodeScore = new CCDAScoreCardRubrics();
		validateProblemCodeScore.setRule(ApplicationConstants.PROBLEMS_CODE_LOINC_REQUIREMENT);
		
		int maxPoints = 0;
		int actualPoints = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue= null;
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
						   if(probObs.getProblemCode()!= null)
						   {
							   if(ApplicationUtil.validateCodeForCodeSystem(probObs.getProblemCode().getCode(), 
									   					ApplicationConstants.CODE_SYSTEM_MAP.get(probObs.getProblemCode().getCodeSystem())))
							   {
								   actualPoints++;
							   }
							   else
							   {
								   issue = new CCDAXmlSnippet();
								   issue.setLineNumber(probObs.getProblemCode().getLineNumber());
								   issue.setXmlString(probObs.getProblemCode().getXmlString());
								   issuesList.add(issue);
							   }
						   }
						   else
						   {
							   issue = new CCDAXmlSnippet();
							   issue.setLineNumber(probObs.getLineNumber());
							   issue.setXmlString(probObs.getXmlString());
							   issuesList.add(issue);
						   }
						   
					   }
				   }
				}
			}
			else
			{
				issue = new CCDAXmlSnippet();
				issue.setLineNumber(problems.getLineNumber());
				issue.setXmlString(problems.getXmlString());
				issuesList.add(issue);
			}
		}
		else
		{
			issue = new CCDAXmlSnippet();
			issue.setLineNumber("Problmes section not present");
			issue.setXmlString("Problmes section not present");
			issuesList.add(issue);
		}
		
		validateProblemCodeScore.setActualPoints(actualPoints);
		validateProblemCodeScore.setMaxPoints(maxPoints);
		validateProblemCodeScore.setRubricScore(ApplicationUtil.calculateRubricScore(maxPoints, actualPoints));
		validateProblemCodeScore.setIssuesList(issuesList);
		validateProblemCodeScore.setNumberOfIssues(issuesList.size());
		if(issuesList.size() > 0)
		{
			validateProblemCodeScore.setDescription("code validation Rubric failed for Problems");
			validateProblemCodeScore.getIgReferences().add(ApplicationConstants.IG_SECTION_REFERENCES);
			validateProblemCodeScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_URL);
		}
		return validateProblemCodeScore;
		
	}
	
	
	public  CCDAScoreCardRubrics getValidStatusCodeScoreCard(CCDAProblem problems)
	{
		CCDAScoreCardRubrics validateStatusCodeScore = new CCDAScoreCardRubrics();
		validateStatusCodeScore.setRule(ApplicationConstants.PROBLEM_APR_TIME_REQ);
		
		int maxPoints = 0;
		int actualPoints = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue= null;
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
						else
						{
							issue = new CCDAXmlSnippet();
							issue.setLineNumber(probCon.getStatusCode().getLineNumber());
							issue.setXmlString(probCon.getStatusCode().getXmlString());
							issuesList.add(issue);
						}
					}
				}
			}
			else
			{
				issue = new CCDAXmlSnippet();
				issue.setLineNumber(problems.getLineNumber());
				issue.setXmlString(problems.getXmlString());
				issuesList.add(issue);
			}
		}
		else
		{
			issue = new CCDAXmlSnippet();
			issue.setLineNumber("Problmes section not present");
			issue.setXmlString("Problmes section not present");
			issuesList.add(issue);
		}
		
	    validateStatusCodeScore.setActualPoints(actualPoints);
		validateStatusCodeScore.setMaxPoints(maxPoints);
		validateStatusCodeScore.setRubricScore(ApplicationUtil.calculateRubricScore(maxPoints, actualPoints));
		validateStatusCodeScore.setIssuesList(issuesList);
		validateStatusCodeScore.setNumberOfIssues(issuesList.size());
		if(issuesList.size() > 0)
		{
			validateStatusCodeScore.setDescription(ApplicationConstants.PROBLEM_APR_TIME_DESC);
			validateStatusCodeScore.getIgReferences().add(ApplicationConstants.IG_SECTION_REFERENCES);
			validateStatusCodeScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_URL);
		}
		return validateStatusCodeScore;
	}
	
	public CCDAScoreCardRubrics getApprEffectivetimeScore(CCDAProblem problems)
	{
		CCDAScoreCardRubrics validateApprEffectiveTimeScore = new CCDAScoreCardRubrics();
		validateApprEffectiveTimeScore.setRule(ApplicationConstants.PROBLEM_TIME_CNST_REQ);
		
		int maxPoints = 0;
		int actualPoints = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue= null;
		if(problems != null)
		{
			if(!ApplicationUtil.isEmpty(problems.getProblemConcerns()))
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
									else
									{
										issue = new CCDAXmlSnippet();
										issue.setLineNumber(problemObs.getEffTime().getLineNumber());
										issue.setXmlString(problemObs.getEffTime().getXmlString());
										issuesList.add(issue);
									}
								}
								else
								{
									issue = new CCDAXmlSnippet();
									issue.setLineNumber(problemObs.getLineNumber());
									issue.setXmlString(problemObs.getXmlString());
									issuesList.add(issue);
								}
							}
						}
					}
				}
			}
			else
			{
				issue = new CCDAXmlSnippet();
				issue.setLineNumber(problems.getLineNumber());
				issue.setXmlString(problems.getXmlString());
				issuesList.add(issue);
			}
		}
		else
		{
			issue = new CCDAXmlSnippet();
			issue.setLineNumber("Problmes section not present");
			issue.setXmlString("Problmes section not present");
			issuesList.add(issue);
		}
		
		validateApprEffectiveTimeScore.setActualPoints(actualPoints);
		validateApprEffectiveTimeScore.setMaxPoints(maxPoints);
		validateApprEffectiveTimeScore.setRubricScore(ApplicationUtil.calculateRubricScore(maxPoints, actualPoints));
		validateApprEffectiveTimeScore.setIssuesList(issuesList);
		validateApprEffectiveTimeScore.setNumberOfIssues(issuesList.size());
		if(issuesList.size() > 0)
		{
			validateApprEffectiveTimeScore.setDescription(ApplicationConstants.PROBLEM_TIME_CNST_DESC);
			validateApprEffectiveTimeScore.getIgReferences().add(ApplicationConstants.IG_SECTION_REFERENCES);
			validateApprEffectiveTimeScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_URL);
		}
		return validateApprEffectiveTimeScore;
	}
	
	public CCDAScoreCardRubrics getApprStatusCodeScore(CCDAProblem problems)
	{
		CCDAScoreCardRubrics validateApprEffectiveTimeScore = new CCDAScoreCardRubrics();
		validateApprEffectiveTimeScore.setRule(ApplicationConstants.PROBLEM_APR_STATUS_REQ);
		
		int maxPoints = 0;
		int actualPoints = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue= null;
		if(problems != null)
		{
			if(!ApplicationUtil.isEmpty(problems.getProblemConcerns()))
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
						else
						{
							issue = new CCDAXmlSnippet();
							issue.setLineNumber(problemAct.getLineNumber());
							issue.setXmlString(problemAct.getXmlString());
							issuesList.add(issue);
						}
					}
				}
			}
			else
			{
				issue = new CCDAXmlSnippet();
				issue.setLineNumber(problems.getLineNumber());
				issue.setXmlString(problems.getXmlString());
				issuesList.add(issue);
			}
		}
		else
		{
			issue = new CCDAXmlSnippet();
			issue.setLineNumber("Problmes section not present");
			issue.setXmlString("Problmes section not present");
			issuesList.add(issue);
		}
		
		
		validateApprEffectiveTimeScore.setActualPoints(actualPoints);
		validateApprEffectiveTimeScore.setMaxPoints(maxPoints);
		validateApprEffectiveTimeScore.setRubricScore(ApplicationUtil.calculateRubricScore(maxPoints, actualPoints));
		validateApprEffectiveTimeScore.setIssuesList(issuesList);
		validateApprEffectiveTimeScore.setNumberOfIssues(issuesList.size());
		if(issuesList.size() > 0)
		{
			validateApprEffectiveTimeScore.setDescription(ApplicationConstants.PROBLEM_APR_STATUS_REQ);
			validateApprEffectiveTimeScore.getIgReferences().add(ApplicationConstants.IG_SECTION_REFERENCES);
			validateApprEffectiveTimeScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_URL);
		}
		return validateApprEffectiveTimeScore;
	}
}
