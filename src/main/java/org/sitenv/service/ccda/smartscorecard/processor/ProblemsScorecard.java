package org.sitenv.service.ccda.smartscorecard.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.sitenv.ccdaparsing.model.CCDACode;
import org.sitenv.ccdaparsing.model.CCDAII;
import org.sitenv.ccdaparsing.model.CCDAProblem;
import org.sitenv.ccdaparsing.model.CCDAProblemConcern;
import org.sitenv.ccdaparsing.model.CCDAProblemObs;
import org.sitenv.ccdaparsing.model.CCDAXmlSnippet;
import org.sitenv.service.ccda.smartscorecard.cofiguration.SectionRule;
import org.sitenv.service.ccda.smartscorecard.model.CCDAScoreCardRubrics;
import org.sitenv.service.ccda.smartscorecard.model.Category;
import org.sitenv.service.ccda.smartscorecard.model.PatientDetails;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationConstants;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

@Service
public class ProblemsScorecard {
	
	private static final Logger logger = Logger.getLogger(ProblemsScorecard.class);
	
	@Autowired
	TemplateIdProcessor templateIdProcessor;
	
	@Autowired
	ReferenceValidatorService referenceValidatorService;
	
	@Async()
	public Future<Category> getProblemsCategory(CCDAProblem problems, PatientDetails patientDetails,String ccdaVersion,List<SectionRule> sectionRules)
	{
		long startTime = System.currentTimeMillis();
		logger.info("Problems Start time:"+ startTime);
		if(problems==null || problems.isSectionNullFlavourWithNI())
		{
			return new AsyncResult<Category>(new Category(ApplicationConstants.CATEGORIES.PROBLEMS.getCategoryDesc(),true));
		}
		Category problemsCategory = new Category();
		problemsCategory.setCategoryName(ApplicationConstants.CATEGORIES.PROBLEMS.getCategoryDesc());
		
		List<CCDAScoreCardRubrics> problemsScoreList = new ArrayList<CCDAScoreCardRubrics>();
		
		if (sectionRules==null || ApplicationUtil.isRuleEnabled(sectionRules, ApplicationConstants.RULE_IDS.R1)) {
			problemsScoreList.add(getTimePrecisionScore(problems, ccdaVersion));
		}
		if (sectionRules==null || ApplicationUtil.isRuleEnabled(sectionRules, ApplicationConstants.RULE_IDS.R2)) {
			problemsScoreList.add(getValidDateTimeScore(problems, patientDetails, ccdaVersion));
		}
		if (sectionRules==null || ApplicationUtil.isRuleEnabled(sectionRules, ApplicationConstants.RULE_IDS.R3)) {
			problemsScoreList.add(getValidDisplayNameScoreCard(problems, ccdaVersion));
		}
		if (sectionRules==null || ApplicationUtil.isRuleEnabled(sectionRules, ApplicationConstants.RULE_IDS.R4)) {
			problemsScoreList.add(getValidProblemCodeScoreCard(problems, ccdaVersion));
		}
		if (sectionRules==null || ApplicationUtil.isRuleEnabled(sectionRules, ApplicationConstants.RULE_IDS.R5)) {
			problemsScoreList.add(getValidStatusCodeScoreCard(problems, ccdaVersion));
		}
		// problemsScoreList.add(getApprStatusCodeScore(problems,ccdaVersion));
		// problemsScoreList.add(getApprEffectivetimeScore(problems,ccdaVersion));
		if (sectionRules==null || ApplicationUtil.isRuleEnabled(sectionRules, ApplicationConstants.RULE_IDS.R6)) {
			problemsScoreList.add(getNarrativeStructureIdScore(problems, ccdaVersion));
		}
		if (sectionRules==null || ApplicationUtil.isRuleEnabled(sectionRules, ApplicationConstants.RULE_IDS.R7)) {
			problemsScoreList.add(getTemplateIdScore(problems, ccdaVersion));
		}
		
		if (sectionRules==null || ApplicationUtil.isRuleEnabled(sectionRules, ApplicationConstants.RULE_IDS.R8)) {
			problemsScoreList.add(getValidProblemCodeValueScoreCard(problems, ccdaVersion));
		}
		if (sectionRules==null || ApplicationUtil.isRuleEnabled(sectionRules, ApplicationConstants.RULE_IDS.R9)) {
			problemsScoreList.add(getAuthorEntryScore(problems, ccdaVersion));
		}
		
		ApplicationUtil.calculateSectionGradeAndIssues(problemsScoreList, problemsCategory);
		ApplicationUtil.calculateNumberOfChecksAndFailedRubrics(problemsScoreList, problemsCategory);
		
		problemsCategory.setCategoryRubrics(problemsScoreList);
		logger.info("Problems End time:"+ (System.currentTimeMillis() - startTime));
		return new AsyncResult<Category>(problemsCategory);
		
	}
	
	
	public  CCDAScoreCardRubrics getTimePrecisionScore(CCDAProblem problem,String ccdaVersion)
	{
		CCDAScoreCardRubrics timePrecisionScore = new CCDAScoreCardRubrics();
		timePrecisionScore.setRule(ApplicationConstants.TIME_PRECISION_REQUIREMENT);
		
		int actualPoints =0;
		int maxPoints = 0;
		int numberOfChecks = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue= null;
		if(problem != null)
		{
			if(!ApplicationUtil.isEmpty(problem.getProblemConcerns()))
			{
				for (CCDAProblemConcern problemConcern : problem.getProblemConcerns())
				{
					maxPoints++;
					numberOfChecks++;
					if(problemConcern.getEffTime() != null)
					{
						if(ApplicationUtil.validateYearFormat(problemConcern.getEffTime()) ||
								ApplicationUtil.validateMonthFormat(problemConcern.getEffTime()) ||
								ApplicationUtil.validateDayFormat(problemConcern.getEffTime()) ||
								ApplicationUtil.validateMinuteFormatWithoutPadding(problemConcern.getEffTime()) ||
								ApplicationUtil.validateSecondFormatWithoutPadding(problemConcern.getEffTime()))
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
							numberOfChecks++;
							if(problemObs.getEffTime() != null)
							{
								if(ApplicationUtil.validateYearFormat(problemObs.getEffTime()) ||
										ApplicationUtil.validateMonthFormat(problemObs.getEffTime()) ||
										ApplicationUtil.validateDayFormat(problemObs.getEffTime()) ||
										ApplicationUtil.validateMinuteFormatWithoutPadding(problemObs.getEffTime()) ||
										ApplicationUtil.validateSecondFormatWithoutPadding(problemObs.getEffTime()))
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
		timePrecisionScore.setNumberOfChecks(numberOfChecks);
		if(issuesList.size() > 0)
		{
			timePrecisionScore.setDescription(ApplicationConstants.TIME_PRECISION_DESCRIPTION);
			if(ccdaVersion.equals("") || ccdaVersion.equals(ApplicationConstants.CCDAVersion.R21.getVersion()))
			{
				timePrecisionScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES.PROBLEM_CONCERN_ACT.getIgReference());
			}
			else if (ccdaVersion.equals(ApplicationConstants.CCDAVersion.R11.getVersion()))
			{
				timePrecisionScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES_R1.PROBLEM_CONCERN_ACT.getIgReference());
			}
			timePrecisionScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_LINKS.PROBLEMS.getTaskforceLink());
		}
		return timePrecisionScore;
	}
	
	public  CCDAScoreCardRubrics getValidDateTimeScore(CCDAProblem problem,PatientDetails patientDetails,String ccdaVersion)
	{
		CCDAScoreCardRubrics validDateTimeScore = new CCDAScoreCardRubrics();
		validDateTimeScore.setRule(ApplicationConstants.TIME_VALID_REQUIREMENT);
		
		int actualPoints =0;
		int maxPoints = 0;
		int numberOfChecks = 0;
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
						numberOfChecks++;
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
								numberOfChecks++;
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
		validDateTimeScore.setNumberOfChecks(numberOfChecks);
		if(issuesList.size() > 0)
		{
			validDateTimeScore.setDescription(ApplicationConstants.TIME_VALID_DESCRIPTION);
			if(ccdaVersion.equals("") || ccdaVersion.equals(ApplicationConstants.CCDAVersion.R21.getVersion()))
			{
				validDateTimeScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES.PROBLEM_CONCERN_ACT.getIgReference());
			}
			else if(ccdaVersion.equals(ApplicationConstants.CCDAVersion.R11.getVersion()))
			{
				validDateTimeScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES_R1.PROBLEM_CONCERN_ACT.getIgReference());
			}
			validDateTimeScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_LINKS.PROBLEMS.getTaskforceLink());
		}
		return validDateTimeScore;
	}
	
	public  CCDAScoreCardRubrics getValidDisplayNameScoreCard(CCDAProblem problems,String ccdaVersion)
	{
		CCDAScoreCardRubrics validateDisplayNameScore = new CCDAScoreCardRubrics();
		validateDisplayNameScore.setRule(ApplicationConstants.CODE_DISPLAYNAME_REQUIREMENT);
		
		int maxPoints = 0;
		int actualPoints = 0;
		int numberOfChecks = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue= null;
		if(problems != null)
		{
			if(problems.getSectionCode()!= null && !ApplicationUtil.isEmpty(problems.getSectionCode().getDisplayName())
												&& ApplicationUtil.isCodeSystemAvailable(problems.getSectionCode().getCodeSystem()))
			{
				maxPoints++;
				numberOfChecks++;
				if(referenceValidatorService.validateDisplayName(problems.getSectionCode().getCode(), 
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
								numberOfChecks++;
								if(referenceValidatorService.validateDisplayName(probObs.getProblemType().getCode(), 
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
								numberOfChecks++;
								if(referenceValidatorService.validateDisplayName(probObs.getProblemCode().getCode(), 
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
										numberOfChecks++;
										if(referenceValidatorService.validateDisplayName(translationCode.getCode(), 
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
		validateDisplayNameScore.setNumberOfChecks(numberOfChecks);
		if(issuesList.size() > 0)
		{
			validateDisplayNameScore.setDescription(ApplicationConstants.CODE_DISPLAYNAME_DESCRIPTION);
			if(ccdaVersion.equals("") || ccdaVersion.equals(ApplicationConstants.CCDAVersion.R21.getVersion()))
			{
				validateDisplayNameScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES.PROBLEM_SECTION.getIgReference());
			}else if (ccdaVersion.equals(ApplicationConstants.CCDAVersion.R11.getVersion()))
			{
				validateDisplayNameScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES_R1.PROBLEM_SECTION.getIgReference());
			}
			validateDisplayNameScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_LINKS.PROBLEMS.getTaskforceLink());
		}
		return validateDisplayNameScore;
	}
	
	public  CCDAScoreCardRubrics getValidProblemCodeScoreCard(CCDAProblem problems,String ccdaVersion)
	{
		CCDAScoreCardRubrics validateProblemCodeScore = new CCDAScoreCardRubrics();
		validateProblemCodeScore.setRule(ApplicationConstants.PROBLEMS_CODE_LOINC_REQUIREMENT);
		
		int maxPoints = 0;
		int actualPoints = 0;
		int numberOfChecks = 0;
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
						   numberOfChecks++;
						   if(probObs.getProblemCode()!= null)
						   {
							   if(referenceValidatorService.validateCodeForCodeSystem(probObs.getProblemCode().getCode(), 
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
		validateProblemCodeScore.setNumberOfChecks(numberOfChecks);
		if(issuesList.size() > 0)
		{
			validateProblemCodeScore.setDescription("code validation Rubric failed for Problems");
			if(ccdaVersion.equals("") || ccdaVersion.equals(ApplicationConstants.CCDAVersion.R21.getVersion()))
			{
				validateProblemCodeScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES.PROBLEM_OBSERVATION.getIgReference());
			}
			else if (ccdaVersion.equals(ApplicationConstants.CCDAVersion.R11.getVersion()))
			{
				validateProblemCodeScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES_R1.PROBLEM_OBSERVATION.getIgReference());
			}
			validateProblemCodeScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_LINKS.PROBLEMS.getTaskforceLink());
		}
		return validateProblemCodeScore;
		
	}
	
	
	public  CCDAScoreCardRubrics getValidStatusCodeScoreCard(CCDAProblem problems,String ccdaVersion)
	{
		CCDAScoreCardRubrics validateStatusCodeScore = new CCDAScoreCardRubrics();
		validateStatusCodeScore.setRule(ApplicationConstants.PROBLEM_APR_TIME_REQ);
		
		int maxPoints = 0;
		int actualPoints = 0;
		int numberOfChecks = 0;
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
						numberOfChecks++;
						if(probCon.getProblemObservations() !=null)
						{
							for(CCDAProblemObs probObs : probCon.getProblemObservations())
							{
								if(ApplicationUtil.validateStatusCode(probObs.getEffTime(), probCon.getStatusCode().getCode()))
								{
									actualPoints++;
								}
								else
								{
									issue = new CCDAXmlSnippet();
									issue.setLineNumber(probObs.getEffTime().getLineNumber());
									issue.setXmlString(probObs.getEffTime().getXmlString());
									issuesList.add(issue);
								}
							}
						}
						else
						{
							issue = new CCDAXmlSnippet();
							issue.setLineNumber(probCon.getEffTime().getLineNumber());
							issue.setXmlString(probCon.getEffTime().getXmlString());
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
		validateStatusCodeScore.setNumberOfChecks(numberOfChecks);
		if(issuesList.size() > 0)
		{
			validateStatusCodeScore.setDescription(ApplicationConstants.PROBLEM_APR_TIME_DESC);
			if(ccdaVersion.equals("") || ccdaVersion.equals(ApplicationConstants.CCDAVersion.R21.getVersion()))
			{
				validateStatusCodeScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES.PROBLEM_CONCERN_ACT.getIgReference());
			}else if(ccdaVersion.equals(ApplicationConstants.CCDAVersion.R11.getVersion()))
			{
				validateStatusCodeScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES_R1.PROBLEM_CONCERN_ACT.getIgReference());
			}
			validateStatusCodeScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_LINKS.PROBLEMS.getTaskforceLink());
		}
		return validateStatusCodeScore;
	}
	
	public CCDAScoreCardRubrics getApprEffectivetimeScore(CCDAProblem problems,String ccdaVersion)
	{
		CCDAScoreCardRubrics validateApprEffectiveTimeScore = new CCDAScoreCardRubrics();
		validateApprEffectiveTimeScore.setRule(ApplicationConstants.PROBLEM_TIME_CNST_REQ);
		
		int maxPoints = 0;
		int actualPoints = 0;
		int numberOfChecks = 0;
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
									numberOfChecks++;
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
		validateApprEffectiveTimeScore.setNumberOfChecks(numberOfChecks);
		if(issuesList.size() > 0)
		{
			validateApprEffectiveTimeScore.setDescription(ApplicationConstants.PROBLEM_TIME_CNST_DESC);
			if(ccdaVersion.equals("") || ccdaVersion.equals(ApplicationConstants.CCDAVersion.R21.getVersion()))
			{
				validateApprEffectiveTimeScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES.PROBLEM_CONCERN_ACT.getIgReference());
			}else if (ccdaVersion.equals(ApplicationConstants.CCDAVersion.R11.getVersion()))
			{
				validateApprEffectiveTimeScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES_R1.PROBLEM_CONCERN_ACT.getIgReference());
			}
			validateApprEffectiveTimeScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_LINKS.PROBLEMS.getTaskforceLink());
		}
		return validateApprEffectiveTimeScore;
	}
	
	public CCDAScoreCardRubrics getApprStatusCodeScore(CCDAProblem problems,String ccdaVersion)
	{
		CCDAScoreCardRubrics validateApprEffectiveTimeScore = new CCDAScoreCardRubrics();
		validateApprEffectiveTimeScore.setRule(ApplicationConstants.PROBLEM_APR_STATUS_REQ);
		
		int maxPoints = 0;
		int actualPoints = 0;
		int numberOfChecks = 0;
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
						numberOfChecks++;
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
		validateApprEffectiveTimeScore.setNumberOfChecks(numberOfChecks);
		if(issuesList.size() > 0)
		{
			validateApprEffectiveTimeScore.setDescription(ApplicationConstants.PROBLEM_APR_STATUS_REQ);
			if(ccdaVersion.equals("") || ccdaVersion.equals(ApplicationConstants.CCDAVersion.R21.getVersion()))
			{
				validateApprEffectiveTimeScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES.PROBLEM_CONCERN_ACT.getIgReference());
			}else if (ccdaVersion.equals(ApplicationConstants.CCDAVersion.R11.getVersion()))
			{
				validateApprEffectiveTimeScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES_R1.PROBLEM_CONCERN_ACT.getIgReference());
			}
			validateApprEffectiveTimeScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_LINKS.PROBLEMS.getTaskforceLink());
		}
		return validateApprEffectiveTimeScore;
	}
	
	public CCDAScoreCardRubrics getNarrativeStructureIdScore(CCDAProblem problems,String ccdaVersion)
	{
		CCDAScoreCardRubrics narrativeTextIdScore = new CCDAScoreCardRubrics();
		narrativeTextIdScore.setRule(ApplicationConstants.NARRATIVE_STRUCTURE_ID_REQ);
		
		int maxPoints = 0;
		int actualPoints = 0;
		int numberOfChecks = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue= null;
		if(problems != null)
		{
			if(!ApplicationUtil.isEmpty(problems.getProblemConcerns()))
			{
				for(CCDAProblemConcern probConc : problems.getProblemConcerns())
				{
					maxPoints++;
					numberOfChecks++;
					if(probConc.getReferenceText()!= null)
					{
						if(problems.getReferenceLinks()!= null && problems.getReferenceLinks().contains(probConc.getReferenceText().getValue()))
						{
							actualPoints++;
						}
						else
						{
							issue = new CCDAXmlSnippet();
							issue.setLineNumber(probConc.getReferenceText().getLineNumber());
							issue.setXmlString(probConc.getReferenceText().getXmlString());
							issuesList.add(issue);
						}
					}
					else
					{
						issue = new CCDAXmlSnippet();
						issue.setLineNumber(probConc.getLineNumber());
						issue.setXmlString(probConc.getXmlString());
						issuesList.add(issue);
					}
					if(probConc.getProblemObservations()!=null)
					{
						for(CCDAProblemObs probObs : probConc.getProblemObservations())
						{
							maxPoints++;
							numberOfChecks++;
							if(probObs.getReferenceText()!= null)
							{
								if(problems.getReferenceLinks()!= null && problems.getReferenceLinks().contains(probObs.getReferenceText().getValue()))
								{
									actualPoints++;
								}
								else
								{
									issue = new CCDAXmlSnippet();
									issue.setLineNumber(probObs.getReferenceText().getLineNumber());
									issue.setXmlString(probObs.getReferenceText().getXmlString());
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
		}
		if(maxPoints ==0)
		{
			maxPoints =1;
			actualPoints =1;
		}
		
		narrativeTextIdScore.setActualPoints(actualPoints);
		narrativeTextIdScore.setMaxPoints(maxPoints);
		narrativeTextIdScore.setRubricScore(ApplicationUtil.calculateRubricScore(maxPoints, actualPoints));
		narrativeTextIdScore.setIssuesList(issuesList);
		narrativeTextIdScore.setNumberOfIssues(issuesList.size());
		narrativeTextIdScore.setNumberOfChecks(numberOfChecks);
		if(issuesList.size() > 0)
		{
			narrativeTextIdScore.setDescription(ApplicationConstants.NARRATIVE_STRUCTURE_ID_DESC);
			if(ccdaVersion.equals("") || ccdaVersion.equals(ApplicationConstants.CCDAVersion.R21.getVersion()))
			{
				narrativeTextIdScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES.PROBLEM_SECTION.getIgReference());
			}
			else if (ccdaVersion.equals(ApplicationConstants.CCDAVersion.R11.getVersion()))
			{
				narrativeTextIdScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES_R1.PROBLEM_SECTION.getIgReference());
			}
			narrativeTextIdScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_LINKS.PROBLEMS.getTaskforceLink());
		}
		
		return narrativeTextIdScore;
	}
	
	
	public CCDAScoreCardRubrics getTemplateIdScore(CCDAProblem problems,String ccdaVersion)
	{
		CCDAScoreCardRubrics templateIdScore = new CCDAScoreCardRubrics();
		templateIdScore.setRule(ApplicationConstants.TEMPLATEID_DESC);
		
		int maxPoints = 0;
		int actualPoints = 0;
		int numberOfChecks = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		
		if(problems!=null)
		{
			if(!ApplicationUtil.isEmpty(problems.getSectionTemplateId()))
			{
				for (CCDAII templateId : problems.getSectionTemplateId())
				{
					maxPoints = maxPoints++;
					numberOfChecks++;
					templateIdProcessor.scoreTemplateId(templateId,actualPoints,issuesList,ccdaVersion);
				}
			}
			
			if(!ApplicationUtil.isEmpty(problems.getProblemConcerns()))
			{
				for(CCDAProblemConcern probConcern : problems.getProblemConcerns())
				{
					if(!ApplicationUtil.isEmpty(probConcern.getTemplateId()))
					{
						for (CCDAII templateId : probConcern.getTemplateId())
						{
							maxPoints = maxPoints++;
							numberOfChecks++;
							templateIdProcessor.scoreTemplateId(templateId,actualPoints,issuesList,ccdaVersion);
						}
					}
					
					if(!ApplicationUtil.isEmpty(probConcern.getProblemObservations()))
					{
						for(CCDAProblemObs probObs : probConcern.getProblemObservations())
						{
							if(!ApplicationUtil.isEmpty(probObs.getTemplateId()))
							{
								for (CCDAII templateId : probObs.getTemplateId())
								{
									maxPoints = maxPoints++;
									numberOfChecks++;
									templateIdProcessor.scoreTemplateId(templateId,actualPoints,issuesList,ccdaVersion);
								}
							}
						}
					}
				}
			}
		}
		
		if(maxPoints==0)
		{
			maxPoints =1;
			actualPoints =1;
		}
		
		templateIdScore.setActualPoints(actualPoints);
		templateIdScore.setMaxPoints(maxPoints);
		templateIdScore.setRubricScore(ApplicationUtil.calculateRubricScore(maxPoints, actualPoints));
		templateIdScore.setIssuesList(issuesList);
		templateIdScore.setNumberOfIssues(issuesList.size());
		templateIdScore.setNumberOfChecks(numberOfChecks);
		if(issuesList.size() > 0)
		{
			templateIdScore.setDescription(ApplicationConstants.TEMPLATEID_REQ);
			if(ccdaVersion.equals("") || ccdaVersion.equals(ApplicationConstants.CCDAVersion.R21.getVersion()))
			{
				templateIdScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES.ALLERGY_SECTION.getIgReference());
			}
			else if (ccdaVersion.equals(ApplicationConstants.CCDAVersion.R11.getVersion()))
			{
				templateIdScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES_R1.ALLERGY_SECTION.getIgReference());
			}
			templateIdScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_LINKS.ALLERGIES.getTaskforceLink());
		}
		
		return templateIdScore;
	}
	
	public  CCDAScoreCardRubrics getValidProblemCodeValueScoreCard(CCDAProblem problems,String ccdaVersion)
	{
		CCDAScoreCardRubrics validateProblemCodeValueScore = new CCDAScoreCardRubrics();
		validateProblemCodeValueScore.setRule(ApplicationConstants.PROBLEMS_CODE_VALUE_REQUIREMENT);
		
		int maxPoints = 0;
		int actualPoints = 0;
		int numberOfChecks = 0;
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
						   numberOfChecks++;
						   if(probObs.getProblemCode()!= null && probObs.getProblemType()!=null)
						   {
							   if(probObs.getProblemCode().getCode()!= probObs.getProblemType().getCode()) {
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
		
		validateProblemCodeValueScore.setActualPoints(actualPoints);
		validateProblemCodeValueScore.setMaxPoints(maxPoints);
		validateProblemCodeValueScore.setRubricScore(ApplicationUtil.calculateRubricScore(maxPoints, actualPoints));
		validateProblemCodeValueScore.setIssuesList(issuesList);
		validateProblemCodeValueScore.setNumberOfIssues(issuesList.size());
		validateProblemCodeValueScore.setNumberOfChecks(numberOfChecks);
		if(issuesList.size() > 0)
		{
			validateProblemCodeValueScore.setDescription(ApplicationConstants.PROBLEMS_CODE_VALUE_REQUIREMENT);
			if(ccdaVersion.equals("") || ccdaVersion.equals(ApplicationConstants.CCDAVersion.R21.getVersion()))
			{
				validateProblemCodeValueScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES.PROBLEM_OBSERVATION.getIgReference());
			}
			else if (ccdaVersion.equals(ApplicationConstants.CCDAVersion.R11.getVersion()))
			{
				validateProblemCodeValueScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES_R1.PROBLEM_OBSERVATION.getIgReference());
			}
			validateProblemCodeValueScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_LINKS.PROBLEMS.getTaskforceLink());
		}
		return validateProblemCodeValueScore;
		
	}
	
	public CCDAScoreCardRubrics getAuthorEntryScore(CCDAProblem problems,String ccdaVersion)
	{
		CCDAScoreCardRubrics authorEntryScore = new CCDAScoreCardRubrics();
		authorEntryScore.setRule(ApplicationConstants.PROBLEMS_AUTHOR_REQUIREMENT);
		
		int maxPoints = 0;
		int actualPoints = 0;
		int numberOfChecks = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue= null;
		if(problems != null)
		{
			if(!ApplicationUtil.isEmpty(problems.getProblemConcerns()))
			{
				for(CCDAProblemConcern problemConcern : problems.getProblemConcerns())
				{
					maxPoints++;
					numberOfChecks++;
					if(problemConcern.getAuthor()!=null && problemConcern.getAuthor().getTime()!=null) {
						actualPoints++;
					}
					else if(problemConcern.getProblemObservations()!=null && problemConcern.getProblemObservations().size() > 0) {
						for(CCDAProblemObs problemObs : problemConcern.getProblemObservations()) {
							if(problemObs.getAuthor()!=null && problemObs.getAuthor().getTime()!=null) {
								actualPoints++;
								break;
							}else {
								issue = new CCDAXmlSnippet();
								issue.setLineNumber(problemConcern.getLineNumber());
								issue.setXmlString(problemConcern.getXmlString());
								issuesList.add(issue);
							}
						}
					}
				}
			}
		}
		
		authorEntryScore.setActualPoints(actualPoints);
		authorEntryScore.setMaxPoints(maxPoints);
		authorEntryScore.setRubricScore(ApplicationUtil.calculateRubricScore(maxPoints, actualPoints));
		authorEntryScore.setIssuesList(issuesList);
		authorEntryScore.setNumberOfIssues(issuesList.size());
		authorEntryScore.setNumberOfChecks(numberOfChecks);
		if(issuesList.size() > 0)
		{
			authorEntryScore.setDescription(ApplicationConstants.PROBLEMS_AUTHOR_REQUIREMENT);
			if(ccdaVersion.equals("") || ccdaVersion.equals(ApplicationConstants.CCDAVersion.R21.getVersion()))
			{
				authorEntryScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES.PROBLEM_SECTION.getIgReference());
			}else if (ccdaVersion.equals(ApplicationConstants.CCDAVersion.R11.getVersion()))
			{
				authorEntryScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES_R1.PROBLEM_SECTION.getIgReference());
			}
			authorEntryScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_LINKS.PROBLEMS.getTaskforceLink());
		}
		return authorEntryScore;
	}
}
