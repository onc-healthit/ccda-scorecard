package org.sitenv.service.ccda.smartscorecard.processor;

import java.util.ArrayList;
import java.util.List;

import org.sitenv.ccdaparsing.model.CCDAAllergy;
import org.sitenv.ccdaparsing.model.CCDACode;
import org.sitenv.ccdaparsing.model.CCDADataElement;
import org.sitenv.ccdaparsing.model.CCDAProblem;
import org.sitenv.ccdaparsing.model.CCDAProblemConcern;
import org.sitenv.ccdaparsing.model.CCDAProblemObs;
import org.sitenv.ccdaparsing.model.CCDAXmlSnippet;
import org.sitenv.service.ccda.smartscorecard.model.CCDAScoreCardRubrics;
import org.sitenv.service.ccda.smartscorecard.model.Category;
import org.sitenv.service.ccda.smartscorecard.model.PatientDetails;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationConstants;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationUtil;
import org.springframework.stereotype.Service;

@Service
public class ProblemsScorecard {
	
	public Category getProblemsCategory(CCDAProblem problems, PatientDetails patientDetails,String docType)
	{
		if(problems==null || problems.isSectionNullFlavourWithNI())
		{
			return new Category(ApplicationConstants.CATEGORIES.PROBLEMS.getCategoryDesc(),true);
		}
		Category problemsCategory = new Category();
		problemsCategory.setCategoryName(ApplicationConstants.CATEGORIES.PROBLEMS.getCategoryDesc());
		
		List<CCDAScoreCardRubrics> problemsScoreList = new ArrayList<CCDAScoreCardRubrics>();
		problemsScoreList.add(getTimePrecisionScore(problems,docType));
		problemsScoreList.add(getValidDateTimeScore(problems,patientDetails,docType));
		problemsScoreList.add(getValidDisplayNameScoreCard(problems,docType));
		problemsScoreList.add(getValidProblemCodeScoreCard(problems,docType));
		problemsScoreList.add(getValidStatusCodeScoreCard(problems,docType));
		problemsScoreList.add(getApprEffectivetimeScore(problems,docType));
		problemsScoreList.add(getApprStatusCodeScore(problems,docType));
		problemsScoreList.add(getNarrativeStructureIdScore(problems,docType));
		
		ApplicationUtil.calculateSectionGradeAndIssues(problemsScoreList, problemsCategory);
		
		problemsCategory.setCategoryRubrics(problemsScoreList);
		
		return problemsCategory;
		
	}
	
	
	public  CCDAScoreCardRubrics getTimePrecisionScore(CCDAProblem problem,String docType)
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
						if(ApplicationUtil.validateDayFormat(problemConcern.getEffTime()) ||
								ApplicationUtil.validateMonthFormat(problemConcern.getEffTime()) ||
								ApplicationUtil.validateMinuteFormat(problemConcern.getEffTime()) ||
								ApplicationUtil.validateSecondFormat(problemConcern.getEffTime()))
						{
							actualPoints++;
						}
						else 
						{
							issue = new CCDAXmlSnippet();
							issue.setLineNumber(problemConcern.getEffTime().getLineNumber());
							issue.setXmlString(problemConcern.getEffTime().getXmlString());
							issuesList.add(issue);
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
								if(ApplicationUtil.validateDayFormat(problemObs.getEffTime()) ||
										ApplicationUtil.validateMonthFormat(problemObs.getEffTime()) ||
										ApplicationUtil.validateMinuteFormat(problemObs.getEffTime()) ||
										ApplicationUtil.validateSecondFormat(problemObs.getEffTime()))
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
			issue.setLineNumber("Problems section not present");
			issue.setXmlString("Problems section not present");
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
			if(docType.equalsIgnoreCase("") || docType.equalsIgnoreCase("R2.1"))
			{
				timePrecisionScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES.PROBLEM_CONCERN_ACT.getIgReference());
			}
			else if (docType.equalsIgnoreCase("R1.1"))
			{
				timePrecisionScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES_R1.PROBLEM_CONCERN_ACT.getIgReference());
			}
			timePrecisionScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_LINKS.PROBLEMS.getTaskforceLink());
		}
		return timePrecisionScore;
	}
	
	public  CCDAScoreCardRubrics getValidDateTimeScore(CCDAProblem problem,PatientDetails patientDetails,String docType)
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
					if(problemConcern.getEffTime() != null && ApplicationUtil.isEffectiveTimePresent(problemConcern.getEffTime()))
					{
						maxPoints++;
						if(ApplicationUtil.checkDateRange(patientDetails, problemConcern.getEffTime()))
						{
							actualPoints++;
						}
						else 
						{
							issue = new CCDAXmlSnippet();
							issue.setLineNumber(problemConcern.getEffTime().getLineNumber());
							issue.setXmlString(problemConcern.getEffTime().getXmlString());
							issuesList.add(issue);
						}
					}
					
					if(!ApplicationUtil.isEmpty(problemConcern.getProblemObservations()))
					{
						for (CCDAProblemObs problemObs : problemConcern.getProblemObservations() )
						{
							if(problemObs.getEffTime() != null && ApplicationUtil.isEffectiveTimePresent(problemObs.getEffTime()))
							{
								maxPoints++;
								if(ApplicationUtil.checkDateRange(patientDetails, problemObs.getEffTime()))
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
						}
					}
				}
			}
		}
		
		if(maxPoints ==0)
		{
			maxPoints =1;
			actualPoints =1;
		}
		
		validDateTimeScore.setActualPoints(actualPoints);
		validDateTimeScore.setMaxPoints(maxPoints);
		validDateTimeScore.setRubricScore(ApplicationUtil.calculateRubricScore(maxPoints, actualPoints));
		validDateTimeScore.setIssuesList(issuesList);
		validDateTimeScore.setNumberOfIssues(issuesList.size());
		if(issuesList.size() > 0)
		{
			validDateTimeScore.setDescription(ApplicationConstants.TIME_VALID_DESCRIPTION);
			if(docType.equalsIgnoreCase("") || docType.equalsIgnoreCase("R2.1"))
			{
				validDateTimeScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES.PROBLEM_CONCERN_ACT.getIgReference());
			}
			else if(docType.equalsIgnoreCase("R1.1"))
			{
				validDateTimeScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES_R1.PROBLEM_CONCERN_ACT.getIgReference());
			}
			validDateTimeScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_LINKS.PROBLEMS.getTaskforceLink());
		}
		return validDateTimeScore;
	}
	
	public  CCDAScoreCardRubrics getValidDisplayNameScoreCard(CCDAProblem problems,String docType)
	{
		CCDAScoreCardRubrics validateDisplayNameScore = new CCDAScoreCardRubrics();
		validateDisplayNameScore.setRule(ApplicationConstants.CODE_DISPLAYNAME_REQUIREMENT);
		
		int maxPoints = 0;
		int actualPoints = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue= null;
		if(problems != null)
		{
			if(problems.getSectionCode()!= null && !ApplicationUtil.isEmpty(problems.getSectionCode().getDisplayName())
												&& ApplicationUtil.isCodeSystemAvailable(problems.getSectionCode().getCodeSystem()))
			{
				maxPoints++;
				if(ApplicationUtil.validateDisplayName(problems.getSectionCode().getCode(), 
														problems.getSectionCode().getCodeSystem(),
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
			
			if(!ApplicationUtil.isEmpty(problems.getProblemConcerns()))
			{
				for(CCDAProblemConcern probCon : problems.getProblemConcerns())
				{
					if(!ApplicationUtil.isEmpty(probCon.getProblemObservations()))
					{
						for (CCDAProblemObs probObs : probCon.getProblemObservations())
						{
							if(probObs.getProblemType()!= null && !ApplicationUtil.isEmpty(probObs.getProblemType().getDisplayName())
																&& ApplicationUtil.isCodeSystemAvailable(probObs.getProblemType().getCodeSystem()))
							{
								maxPoints++;
								if(ApplicationUtil.validateDisplayName(probObs.getProblemType().getCode(), 
																		probObs.getProblemType().getCodeSystem(),
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
							if(probObs.getProblemCode()!= null && !ApplicationUtil.isEmpty(probObs.getProblemCode().getDisplayName())
									&& ApplicationUtil.isCodeSystemAvailable(probObs.getProblemCode().getCodeSystem()))
							{
								maxPoints++;
								if(ApplicationUtil.validateDisplayName(probObs.getProblemCode().getCode(), 
																		probObs.getProblemCode().getCodeSystem(),
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
							if(!ApplicationUtil.isEmpty(probObs.getTranslationProblemType()))
							{
								for (CCDACode translationCode : probObs.getTranslationProblemType())
								{
									if(!ApplicationUtil.isEmpty(translationCode.getDisplayName())
											&& ApplicationUtil.isCodeSystemAvailable(translationCode.getCodeSystem()))
									{
										maxPoints++;
										if(ApplicationUtil.validateDisplayName(translationCode.getCode(), 
															translationCode.getCodeSystem(),
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
		}
		if(maxPoints==0)
		{
			maxPoints=1;
			actualPoints=1;
		}
		
		validateDisplayNameScore.setActualPoints(actualPoints);
		validateDisplayNameScore.setMaxPoints(maxPoints);
		validateDisplayNameScore.setRubricScore(ApplicationUtil.calculateRubricScore(maxPoints, actualPoints));
		validateDisplayNameScore.setIssuesList(issuesList);
		validateDisplayNameScore.setNumberOfIssues(issuesList.size());
		if(issuesList.size() > 0)
		{
			validateDisplayNameScore.setDescription(ApplicationConstants.CODE_DISPLAYNAME_DESCRIPTION);
			if(docType.equalsIgnoreCase("") || docType.equalsIgnoreCase("R2.1"))
			{
				validateDisplayNameScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES.PROBLEM_SECTION.getIgReference());
			}else if (docType.equalsIgnoreCase("R1.1"))
			{
				validateDisplayNameScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES_R1.PROBLEM_SECTION.getIgReference());
			}
			validateDisplayNameScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_LINKS.PROBLEMS.getTaskforceLink());
		}
		return validateDisplayNameScore;
	}
	
	public  CCDAScoreCardRubrics getValidProblemCodeScoreCard(CCDAProblem problems,String docType)
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
									   					probObs.getProblemCode().getCodeSystem()))
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
			issue.setLineNumber("Problems section not present");
			issue.setXmlString("Problems section not present");
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
			if(docType.equalsIgnoreCase("") || docType.equalsIgnoreCase("R2.1"))
			{
				validateProblemCodeScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES.PROBLEM_OBSERVATION.getIgReference());
			}
			else if (docType.equalsIgnoreCase("R1.1"))
			{
				validateProblemCodeScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES_R1.PROBLEM_OBSERVATION.getIgReference());
			}
			validateProblemCodeScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_LINKS.PROBLEMS.getTaskforceLink());
		}
		return validateProblemCodeScore;
		
	}
	
	
	public  CCDAScoreCardRubrics getValidStatusCodeScoreCard(CCDAProblem problems,String docType)
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
			issue.setLineNumber("Problems section not present");
			issue.setXmlString("Problems section not present");
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
			if(docType.equalsIgnoreCase("") || docType.equalsIgnoreCase("R2.1"))
			{
				validateStatusCodeScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES.PROBLEM_CONCERN_ACT.getIgReference());
			}else if(docType.equalsIgnoreCase("R1.1"))
			{
				validateStatusCodeScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES_R1.PROBLEM_CONCERN_ACT.getIgReference());
			}
			validateStatusCodeScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_LINKS.PROBLEMS.getTaskforceLink());
		}
		return validateStatusCodeScore;
	}
	
	public CCDAScoreCardRubrics getApprEffectivetimeScore(CCDAProblem problems,String docType)
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
					if(problemAct.getEffTime()!= null && ApplicationUtil.isEffectiveTimePresent(problemAct.getEffTime()))
					{
						if(!ApplicationUtil.isEmpty(problemAct.getProblemObservations()))
						{
							for(CCDAProblemObs problemObs : problemAct.getProblemObservations())
							{
								if(problemObs.getEffTime()!=null && ApplicationUtil.isEffectiveTimePresent(problemObs.getEffTime()))
								{
									maxPoints++;
									if(ApplicationUtil.checkDateRange(problemObs.getEffTime().getLow(),problemObs.getEffTime().getHigh(),
														problemAct.getEffTime().getLow(),problemAct.getEffTime().getHigh()))
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
							}
						}
					}
				}
			}
		}
		
		if(maxPoints ==0)
		{
			maxPoints =1;
			actualPoints =1;
		}
		
		validateApprEffectiveTimeScore.setActualPoints(actualPoints);
		validateApprEffectiveTimeScore.setMaxPoints(maxPoints);
		validateApprEffectiveTimeScore.setRubricScore(ApplicationUtil.calculateRubricScore(maxPoints, actualPoints));
		validateApprEffectiveTimeScore.setIssuesList(issuesList);
		validateApprEffectiveTimeScore.setNumberOfIssues(issuesList.size());
		if(issuesList.size() > 0)
		{
			validateApprEffectiveTimeScore.setDescription(ApplicationConstants.PROBLEM_TIME_CNST_DESC);
			if(docType.equalsIgnoreCase("") || docType.equalsIgnoreCase("R2.1"))
			{
				validateApprEffectiveTimeScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES.PROBLEM_CONCERN_ACT.getIgReference());
			}else if (docType.equalsIgnoreCase("R1.1"))
			{
				validateApprEffectiveTimeScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES_R1.PROBLEM_CONCERN_ACT.getIgReference());
			}
			validateApprEffectiveTimeScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_LINKS.PROBLEMS.getTaskforceLink());
		}
		return validateApprEffectiveTimeScore;
	}
	
	public CCDAScoreCardRubrics getApprStatusCodeScore(CCDAProblem problems,String docType)
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
						if(problemAct.getStatusCode()!=null)
						{
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
			issue.setLineNumber("Problems section not present");
			issue.setXmlString("Problems section not present");
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
			if(docType.equalsIgnoreCase("") || docType.equalsIgnoreCase("R2.1"))
			{
				validateApprEffectiveTimeScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES.PROBLEM_CONCERN_ACT.getIgReference());
			}else if (docType.equalsIgnoreCase("R1.1"))
			{
				validateApprEffectiveTimeScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES_R1.PROBLEM_CONCERN_ACT.getIgReference());
			}
			validateApprEffectiveTimeScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_LINKS.PROBLEMS.getTaskforceLink());
		}
		return validateApprEffectiveTimeScore;
	}
	
	public CCDAScoreCardRubrics getNarrativeStructureIdScore(CCDAProblem problems,String docType)
	{
		CCDAScoreCardRubrics narrativeTextIdScore = new CCDAScoreCardRubrics();
		narrativeTextIdScore.setRule(ApplicationConstants.NARRATIVE_STRUCTURE_ID_REQ);
		
		int maxPoints = 0;
		int actualPoints = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue= null;
		if(problems != null)
		{
			if(!ApplicationUtil.isEmpty(problems.getProblemConcerns()))
			{
				for(CCDAProblemConcern probConc : problems.getProblemConcerns())
				{
					if(!ApplicationUtil.isEmpty(probConc.getReferenceTexts()))
					{
						for(CCDADataElement referenceText : probConc.getReferenceTexts())
						{
							maxPoints++;
							if(problems.getReferenceLinks().contains(referenceText.getValue()))
							{
								actualPoints++;
							}
							else
							{
								issue = new CCDAXmlSnippet();
								issue.setLineNumber(referenceText.getLineNumber());
								issue.setXmlString(referenceText.getXmlString());
								issuesList.add(issue);
							}
						}
					}
				}
			}
			if(maxPoints ==0)
			{
				maxPoints =1;
				actualPoints =1;
			}
		}
		else
		{
			issue = new CCDAXmlSnippet();
			issue.setLineNumber("All sections are empty");
			issue.setXmlString("All sections are empty");
			issuesList.add(issue);
		}
		
		narrativeTextIdScore.setActualPoints(actualPoints);
		narrativeTextIdScore.setMaxPoints(maxPoints);
		narrativeTextIdScore.setRubricScore(ApplicationUtil.calculateRubricScore(maxPoints, actualPoints));
		narrativeTextIdScore.setIssuesList(issuesList);
		narrativeTextIdScore.setNumberOfIssues(issuesList.size());
		if(issuesList.size() > 0)
		{
			narrativeTextIdScore.setDescription(ApplicationConstants.NARRATIVE_STRUCTURE_ID_DESC);
			if(docType.equalsIgnoreCase("") || docType.equalsIgnoreCase("R2.1"))
			{
				narrativeTextIdScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES.PROBLEM_SECTION.getIgReference());
			}
			else if (docType.equalsIgnoreCase("R1.1"))
			{
				narrativeTextIdScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES_R1.PROBLEM_SECTION.getIgReference());
			}
			narrativeTextIdScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_LINKS.PROBLEMS.getTaskforceLink());
		}
		
		return narrativeTextIdScore;
	}
	
	public CCDAScoreCardRubrics getTemplateIdScore(CCDAAllergy allergies,String docType)
	{
		CCDAScoreCardRubrics templateIdScore = new CCDAScoreCardRubrics();
		templateIdScore.setRule(ApplicationConstants.TEMPLATEID_DESC);
		
		int maxPoints = 0;
		int actualPoints = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue= null;
		if(allergies != null)
		{
			if(!ApplicationUtil.isEmpty(allergies.getAllergyConcern()))
			{
			}
		}
		else
		{
			issue = new CCDAXmlSnippet();
			issue.setLineNumber("Allergies Section not present");
			issue.setXmlString("Allergies Section not present");
			issuesList.add(issue);
		}
		
		templateIdScore.setActualPoints(actualPoints);
		templateIdScore.setMaxPoints(maxPoints);
		templateIdScore.setRubricScore(ApplicationUtil.calculateRubricScore(maxPoints, actualPoints));
		templateIdScore.setIssuesList(issuesList);
		templateIdScore.setNumberOfIssues(issuesList.size());
		if(issuesList.size() > 0)
		{
			templateIdScore.setDescription(ApplicationConstants.TEMPLATEID_REQ);
			if(docType.equalsIgnoreCase("") || docType.equalsIgnoreCase("R2.1"))
			{
				templateIdScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES.ALLERGY_SECTION.getIgReference());
			}
			else if (docType.equalsIgnoreCase("R1.1"))
			{
				templateIdScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES_R1.ALLERGY_SECTION.getIgReference());
			}
			templateIdScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_LINKS.ALLERGIES.getTaskforceLink());
		}
		
		return templateIdScore;
	}
}
