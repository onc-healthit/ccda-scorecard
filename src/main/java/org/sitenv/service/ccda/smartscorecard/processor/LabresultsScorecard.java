package org.sitenv.service.ccda.smartscorecard.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.sitenv.ccdaparsing.model.CCDAII;
import org.sitenv.ccdaparsing.model.CCDALabResult;
import org.sitenv.ccdaparsing.model.CCDALabResultObs;
import org.sitenv.ccdaparsing.model.CCDALabResultOrg;
import org.sitenv.ccdaparsing.model.CCDAXmlSnippet;
import org.sitenv.service.ccda.smartscorecard.configuration.SectionRule;
import org.sitenv.service.ccda.smartscorecard.model.CCDAScoreCardRubrics;
import org.sitenv.service.ccda.smartscorecard.model.Category;
import org.sitenv.service.ccda.smartscorecard.model.PatientDetails;
import org.sitenv.service.ccda.smartscorecard.repositories.inmemory.LoincRepository;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationConstants;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

@Service
public class LabresultsScorecard {
	
	private static final Logger logger = Logger.getLogger(LabresultsScorecard.class);
	
	@Autowired
	LoincRepository loincRepository;
	
	@Autowired
	TemplateIdProcessor templateIdProcessor;
	
	@Autowired
	ReferenceValidatorService referenceValidatorService;
	
	@Async()
	public Future<Category> getLabResultsCategory(CCDALabResult labResults, CCDALabResult labTests, PatientDetails patientDetails,String ccdaVersion,List<SectionRule> sectionRules)
	{
		
		long startTime = System.currentTimeMillis();
		logger.info("Labresults Start time:"+ startTime);
		Category labResultsCategory = new Category();
		try {
			if(labResults== null || labResults.isSectionNullFlavourWithNI())
			{
				return new AsyncResult<Category>(new Category(ApplicationConstants.CATEGORIES.RESULTS.getCategoryDesc(),true));
			}
			CCDALabResult results =null;
			if(labResults!= null)
			{
				results = labResults;
				if(labTests!= null && !ApplicationUtil.isEmpty(labTests.getResultOrg()))
				{
					results.getResultOrg().addAll(labTests.getResultOrg());
	
				}
			}
			
			
			labResultsCategory.setCategoryName(ApplicationConstants.CATEGORIES.RESULTS.getCategoryDesc());
			
			List<CCDAScoreCardRubrics> labResultsScoreList = new ArrayList<CCDAScoreCardRubrics>();
			
			if (sectionRules==null || ApplicationUtil.isRuleEnabled(sectionRules, ApplicationConstants.RULE_IDS.L1)) {
				labResultsScoreList.add(getTimePrecisionScore(results, ccdaVersion));
			}
			if (sectionRules==null || ApplicationUtil.isRuleEnabled(sectionRules, ApplicationConstants.RULE_IDS.L2)) {
				labResultsScoreList.add(getValidDateTimeScore(results, patientDetails, ccdaVersion));
			}
			if (sectionRules==null || ApplicationUtil.isRuleEnabled(sectionRules, ApplicationConstants.RULE_IDS.L3)) {
				labResultsScoreList.add(getValidDisplayNameScoreCard(results, ccdaVersion));
			}
			if (sectionRules==null || ApplicationUtil.isRuleEnabled(sectionRules, ApplicationConstants.RULE_IDS.L4)) {
				labResultsScoreList.add(getValidUCUMScore(labResults, ccdaVersion));
			}
			if (sectionRules==null || ApplicationUtil.isRuleEnabled(sectionRules, ApplicationConstants.RULE_IDS.L5)) {
				labResultsScoreList.add(getValidLoincCodesScore(results, ccdaVersion));
			}
			if (sectionRules==null || ApplicationUtil.isRuleEnabled(sectionRules, ApplicationConstants.RULE_IDS.L6)) {
				labResultsScoreList.add(getApprEffectivetimeScore(results, ccdaVersion));
			}
			if (sectionRules==null || ApplicationUtil.isRuleEnabled(sectionRules, ApplicationConstants.RULE_IDS.L7)) {
				labResultsScoreList.add(getNarrativeStructureIdScore(results, ccdaVersion));
			}
			if (sectionRules==null || ApplicationUtil.isRuleEnabled(sectionRules, ApplicationConstants.RULE_IDS.L8)) {
				labResultsScoreList.add(getTemplateIdScore(results, ccdaVersion));
			}
			
			labResultsCategory.setCategoryRubrics(labResultsScoreList);
			ApplicationUtil.calculateSectionGradeAndIssues(labResultsScoreList, labResultsCategory);
			ApplicationUtil.calculateNumberOfChecksAndFailedRubrics(labResultsScoreList, labResultsCategory);
			logger.info("Labresults End time:"+ (System.currentTimeMillis() - startTime));
		}catch (Exception e) {
			logger.info("Exception occured while scoring Lab Results section:"+e.getMessage());
		}
		return  new AsyncResult<Category>(labResultsCategory);
		
	}
	
	public CCDAScoreCardRubrics getTimePrecisionScore(CCDALabResult labResults,String ccdaVersion)
	{
		CCDAScoreCardRubrics timePrecisionScore = new CCDAScoreCardRubrics();
		timePrecisionScore.setRule(ApplicationConstants.TIME_PRECISION_REQUIREMENT);
		
		int actualPoints =0;
		int maxPoints = 0;
		int numberOfChecks = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue= null;
		if(labResults != null)
		{
			if(!ApplicationUtil.isEmpty(labResults.getResultOrg()))
			{
				for (CCDALabResultOrg resultOrg : labResults.getResultOrg())
				{
					if(resultOrg.getEffTime() != null && !resultOrg.getEffTime().isNullFlavour())
					{
						maxPoints++;
						numberOfChecks++;
						if(ApplicationUtil.validateDayFormat(resultOrg.getEffTime()) ||
									ApplicationUtil.validateMinuteFormatWithoutPadding(resultOrg.getEffTime()) ||
									ApplicationUtil.validateSecondFormatWithoutPadding(resultOrg.getEffTime()))
						{
							actualPoints++;
						}
						else
						{
							issue = new CCDAXmlSnippet();
							issue.setLineNumber(resultOrg.getEffTime().getLineNumber());
							issue.setXmlString(resultOrg.getEffTime().getXmlString());
							issuesList.add(issue);
						}
					}
					
					if(!ApplicationUtil.isEmpty(resultOrg.getResultObs()))
					{
						for (CCDALabResultObs resultObs : resultOrg.getResultObs() )
						{
							if(resultObs.getMeasurementTime() != null && !resultObs.getMeasurementTime().isNullFlavour())
							{
								maxPoints++;
								numberOfChecks++;
								if(ApplicationUtil.validateDayFormat(resultObs.getMeasurementTime()) ||
										ApplicationUtil.validateMinuteFormatWithoutPadding(resultObs.getMeasurementTime()) ||
										ApplicationUtil.validateSecondFormatWithoutPadding(resultObs.getMeasurementTime()))
								{
									actualPoints++;
								}
								else
								{
									issue = new CCDAXmlSnippet();
									issue.setLineNumber(resultObs.getMeasurementTime().getLineNumber());
									issue.setXmlString(resultObs.getMeasurementTime().getXmlString());
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

		timePrecisionScore.setActualPoints(actualPoints);
		timePrecisionScore.setMaxPoints(maxPoints);
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
				timePrecisionScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES.RESULT_ORGANIZER.getIgReference());
			}
			else if (ccdaVersion.equals(ApplicationConstants.CCDAVersion.R11.getVersion()))
			{
				timePrecisionScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES_R1.RESULT_ORGANIZER.getIgReference());
			}
			timePrecisionScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_LINKS.RESULTS.getTaskforceLink());
		}
		return timePrecisionScore;
	}
	
	
	public CCDAScoreCardRubrics getValidDateTimeScore(CCDALabResult labResults, PatientDetails patientDetails,String ccdaVersion)
	{
		CCDAScoreCardRubrics validateTimeScore = new CCDAScoreCardRubrics();
		validateTimeScore.setRule(ApplicationConstants.TIME_VALID_REQUIREMENT);
		
		int actualPoints =0;
		int maxPoints = 0;
		int numberOfChecks = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue= null;
		if(labResults != null)
		{
			if(!ApplicationUtil.isEmpty(labResults.getResultOrg()))
			{
				for (CCDALabResultOrg resultOrg : labResults.getResultOrg())
				{
					if(resultOrg.getEffTime() != null && ApplicationUtil.isEffectiveTimePresent(resultOrg.getEffTime()))
					{
						maxPoints++;
						numberOfChecks++;
						if(ApplicationUtil.checkDateRange(patientDetails, resultOrg.getEffTime()))
						{
							actualPoints++;
						}
						else
						{
							issue = new CCDAXmlSnippet();
							issue.setLineNumber(resultOrg.getEffTime().getLineNumber());
							issue.setXmlString(resultOrg.getEffTime().getXmlString());
							issuesList.add(issue);
						}
					}
					
					if(!ApplicationUtil.isEmpty(resultOrg.getResultObs()))
					{
						for (CCDALabResultObs resultObs : resultOrg.getResultObs() )
						{
							if(resultObs.getMeasurementTime() != null && ApplicationUtil.isEffectiveTimePresent(resultObs.getMeasurementTime()))
							{
								maxPoints++;
								numberOfChecks++;
								if(ApplicationUtil.checkDateRange(patientDetails, resultObs.getMeasurementTime()))
								{
									actualPoints++;
								}
								else
								{
									issue = new CCDAXmlSnippet();
									issue.setLineNumber(resultObs.getMeasurementTime().getLineNumber());
									issue.setXmlString(resultObs.getMeasurementTime().getXmlString());
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
		
		validateTimeScore.setActualPoints(actualPoints);
		validateTimeScore.setMaxPoints(maxPoints);
		validateTimeScore.setRubricScore(ApplicationUtil.calculateRubricScore(maxPoints, actualPoints));
		validateTimeScore.setIssuesList(issuesList);
		validateTimeScore.setNumberOfIssues(issuesList.size());
		validateTimeScore.setNumberOfChecks(numberOfChecks);
		if(issuesList.size() > 0)
		{
			validateTimeScore.setDescription(ApplicationConstants.TIME_VALID_DESCRIPTION);
			if(ccdaVersion.equals("") || ccdaVersion.equals(ApplicationConstants.CCDAVersion.R21.getVersion()))
			{
				validateTimeScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES.RESULT_ORGANIZER.getIgReference());
			}
			else if (ccdaVersion.equals(ApplicationConstants.CCDAVersion.R11.getVersion()))
			{
				validateTimeScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES_R1.RESULT_ORGANIZER.getIgReference());
			}
			validateTimeScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_LINKS.RESULTS.getTaskforceLink());
		}
		return validateTimeScore;
	}
	
	public CCDAScoreCardRubrics getValidDisplayNameScoreCard(CCDALabResult labresults,String ccdaVersion)
	{
		CCDAScoreCardRubrics validateDisplayNameScore = new CCDAScoreCardRubrics();
		validateDisplayNameScore.setRule(ApplicationConstants.CODE_DISPLAYNAME_REQUIREMENT);

		int maxPoints = 0;
		int actualPoints = 0;
		int numberOfChecks = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue= null;
		if(labresults != null)
		{
			if(labresults.getSectionCode()!= null && !ApplicationUtil.isEmpty(labresults.getSectionCode().getDisplayName())
													&& ApplicationUtil.isCodeSystemAvailable(labresults.getSectionCode().getCodeSystem()))
			{
				maxPoints++;
				numberOfChecks++;
				if(referenceValidatorService.validateDisplayName(labresults.getSectionCode().getCode(), 
												labresults.getSectionCode().getCodeSystem(),
												labresults.getSectionCode().getDisplayName()))
				{
					actualPoints++;
				}
				else
				{
					issue = new CCDAXmlSnippet();
					issue.setLineNumber(labresults.getSectionCode().getLineNumber());
					issue.setXmlString(labresults.getSectionCode().getXmlString());
					issuesList.add(issue);
				}
			}
			
			if(!ApplicationUtil.isEmpty(labresults.getResultOrg()))
			{
				for (CCDALabResultOrg resultOrg : labresults.getResultOrg())
				{
					if(resultOrg.getOrgCode()!= null && !ApplicationUtil.isEmpty(resultOrg.getOrgCode().getDisplayName())
													&& ApplicationUtil.isCodeSystemAvailable(resultOrg.getOrgCode().getCodeSystem()))
					{
						maxPoints++;
						numberOfChecks++;
						if(referenceValidatorService.validateDisplayName(resultOrg.getOrgCode().getCode(), 
								resultOrg.getOrgCode().getCodeSystem(),
								resultOrg.getOrgCode().getDisplayName()))
						{
							actualPoints++;
						}
						else
						{
							issue = new CCDAXmlSnippet();
							issue.setLineNumber(resultOrg.getOrgCode().getLineNumber());
							issue.setXmlString(resultOrg.getOrgCode().getXmlString());
							issuesList.add(issue);
						}
					}
					
					if(!ApplicationUtil.isEmpty(resultOrg.getResultObs()))
					{
						for (CCDALabResultObs resultobs : resultOrg.getResultObs())
						{
							if(resultobs.getResultCode()!= null && !ApplicationUtil.isEmpty(resultobs.getResultCode().getDisplayName())
																&& ApplicationUtil.isCodeSystemAvailable(resultobs.getResultCode().getCodeSystem()))
							{
								maxPoints++;
								numberOfChecks++;
								if(referenceValidatorService.validateDisplayName(resultobs.getResultCode().getCode(), 
										resultobs.getResultCode().getCodeSystem(),
										resultobs.getResultCode().getDisplayName()))
								{
									actualPoints++;
								}
								else
								{
									issue = new CCDAXmlSnippet();
									issue.setLineNumber(resultobs.getResultCode().getLineNumber());
									issue.setXmlString(resultobs.getResultCode().getXmlString());
									issuesList.add(issue);
								}
							}
						}
					}
				}
			}
		}
		
		if(maxPoints==0)
		{
			actualPoints = 1;
			maxPoints =1;
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
				validateDisplayNameScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES.RESULT_SECTION.getIgReference());
			}
			else if (ccdaVersion.equals(ApplicationConstants.CCDAVersion.R11.getVersion()))
			{
				validateDisplayNameScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES_R1.RESULT_SECTION.getIgReference());
			}
			validateDisplayNameScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_LINKS.RESULTS.getTaskforceLink());
		}
		return validateDisplayNameScore;
	}
	
	public  CCDAScoreCardRubrics getValidUCUMScore(CCDALabResult labresults,String ccdaVersion)
	{
		CCDAScoreCardRubrics validateUCUMScore = new CCDAScoreCardRubrics();
		validateUCUMScore.setRule(ApplicationConstants.RESULTS_UCUM_REQ);
		
		int maxPoints = 0;
		int actualPoints = 0;
		int numberOfChecks = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue= null;
		if(labresults != null)
		{
			if(!ApplicationUtil.isEmpty(labresults.getResultOrg()))
			{
				for(CCDALabResultOrg resultsOrg : labresults.getResultOrg())
				{
					if(!ApplicationUtil.isEmpty(resultsOrg.getResultObs()))
					{
						for(CCDALabResultObs resultsObs : resultsOrg.getResultObs())
						{
							if(resultsObs.getResults() != null && ApplicationUtil.checkLabResultType(resultsObs.getResults().getXsiType()))
							{
								maxPoints++;
								numberOfChecks++;
								if(resultsObs.getResultCode()!= null)
								{
									if(loincRepository.foundUCUMUnitsForLoincCode(resultsObs.getResultCode().getCode(),
																					resultsObs.getResults().getUnits()!=null ? resultsObs.getResults().getUnits() : ""))
									{
										actualPoints++;
									}
									else
									{
										issue = new CCDAXmlSnippet();
										issue.setLineNumber(resultsObs.getResults().getLineNumber());
										issue.setXmlString(resultsObs.getResults().getXmlString());
										issuesList.add(issue);
									}
								}
								else 
								{
									issue = new CCDAXmlSnippet();
									issue.setLineNumber(resultsObs.getResults().getLineNumber());
									issue.setXmlString(resultsObs.getResults().getXmlString());
									issuesList.add(issue);
								}
							}
							else if(resultsObs.getResults() == null)
							{
								maxPoints++;
								numberOfChecks++;
								issue = new CCDAXmlSnippet();
								issue.setLineNumber(resultsObs.getLineNumber());
								issue.setXmlString(resultsObs.getXmlString());
								issuesList.add(issue);
							}
						}
					}
				}
			}
		}
		
		if(maxPoints==0)
		{
			actualPoints = 1;
			maxPoints =1;
		}
		
		validateUCUMScore.setActualPoints(actualPoints);
		validateUCUMScore.setMaxPoints(maxPoints);
		validateUCUMScore.setRubricScore(ApplicationUtil.calculateRubricScore(maxPoints, actualPoints));
		validateUCUMScore.setIssuesList(issuesList);
		validateUCUMScore.setNumberOfIssues(issuesList.size());
		validateUCUMScore.setNumberOfChecks(numberOfChecks);
		if(issuesList.size() > 0)
		{
			validateUCUMScore.setDescription(ApplicationConstants.RESULTS_UCUM_DESC);
			if(ccdaVersion.equals("") || ccdaVersion.equals(ApplicationConstants.CCDAVersion.R21.getVersion()))
			{
				validateUCUMScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES.RESULT_OBSERVATION.getIgReference());
			}
			else if (ccdaVersion.equals(ApplicationConstants.CCDAVersion.R11.getVersion()))
			{
				validateUCUMScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES_R1.RESULT_OBSERVATION.getIgReference());
			}
			validateUCUMScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_LINKS.RESULTS_UCUM.getTaskforceLink());
		}
		return validateUCUMScore;
	}
	
	public CCDAScoreCardRubrics getValidLoincCodesScore(CCDALabResult labresults,String ccdaVersion)
	{
		CCDAScoreCardRubrics validatLoincCodeScore = new CCDAScoreCardRubrics();
		validatLoincCodeScore.setRule(ApplicationConstants.LABRESULTS_LOIN_CODE_REQ);
		
		int maxPoints = 0;
		int actualPoints = 0;
		int numberOfChecks = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue= null;
		if(labresults != null)
		{
			if(!ApplicationUtil.isEmpty(labresults.getResultOrg()))
			{
				for(CCDALabResultOrg resultOrg : labresults.getResultOrg())
				{
				   if(!ApplicationUtil.isEmpty(resultOrg.getResultObs()))
				   {
					   for(CCDALabResultObs resultObs : resultOrg.getResultObs())
					   {
						   maxPoints++;
						   numberOfChecks++;
						   if(resultObs.getResultCode()!= null)
						   {
							   if(loincRepository.findByCode(resultObs.getResultCode().getCode()))
							   {
								   actualPoints++;
							   }
							   else 
							   {
								   issue = new CCDAXmlSnippet();
								   issue.setLineNumber(resultObs.getResultCode().getLineNumber());
								   issue.setXmlString(resultObs.getResultCode().getXmlString());
								   issuesList.add(issue);
							   }
						   }
						   else 
						   {
							   issue = new CCDAXmlSnippet();
							   issue.setLineNumber(resultObs.getLineNumber());
							   issue.setXmlString(resultObs.getXmlString());
							   issuesList.add(issue);
						   }
					   }
				   }
				}
			}
		}
		
		if(maxPoints==0)
		{
			actualPoints = 1;
			maxPoints =1;
		}
		
		validatLoincCodeScore.setActualPoints(actualPoints);
		validatLoincCodeScore.setMaxPoints(maxPoints);
		validatLoincCodeScore.setRubricScore(ApplicationUtil.calculateRubricScore(maxPoints, actualPoints));
		validatLoincCodeScore.setIssuesList(issuesList);
		validatLoincCodeScore.setNumberOfIssues(issuesList.size());
		validatLoincCodeScore.setNumberOfChecks(numberOfChecks);
		if(issuesList.size() > 0)
		{
			validatLoincCodeScore.setDescription(ApplicationConstants.LABRESULTS_LOIN_CODE_REQ);
			if(ccdaVersion.equals("") || ccdaVersion.equals(ApplicationConstants.CCDAVersion.R21.getVersion()))
			{
				validatLoincCodeScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES.RESULT_OBSERVATION.getIgReference());
			}
			else if(ccdaVersion.equals(ApplicationConstants.CCDAVersion.R11.getVersion()))
			{
				validatLoincCodeScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES_R1.RESULT_OBSERVATION.getIgReference());
			}
			validatLoincCodeScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_LINKS.RESULTS_UCUM.getTaskforceLink());
		}
		return validatLoincCodeScore;
		
	}
	
	public CCDAScoreCardRubrics getApprEffectivetimeScore(CCDALabResult results,String ccdaVersion)
	{
		CCDAScoreCardRubrics validateApprEffectiveTimeScore = new CCDAScoreCardRubrics();
		validateApprEffectiveTimeScore.setRule(ApplicationConstants.LABRESULTS_APR_TIME_REQ);
		
		int maxPoints = 0;
		int actualPoints = 0;
		int numberOfChecks = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue= null;
		if(results != null)
		{
			if(!ApplicationUtil.isEmpty(results.getResultOrg()))
			{
				for(CCDALabResultOrg resultOrg : results.getResultOrg())
				{
					if(resultOrg.getEffTime()!= null)
					{
						if(!ApplicationUtil.isEmpty(resultOrg.getResultObs()))
						{
							for(CCDALabResultObs resultsObs : resultOrg.getResultObs())
							{
								if(resultsObs.getMeasurementTime() != null && ApplicationUtil.isEffectiveTimePresent(resultsObs.getMeasurementTime()))
								{
									maxPoints++;
									numberOfChecks++;
									if(ApplicationUtil.checkDateRange(resultsObs.getMeasurementTime(),resultOrg.getEffTime()))
									{
										actualPoints++;
									}
									else 
									{
										 issue = new CCDAXmlSnippet();
										 issue.setLineNumber(resultsObs.getMeasurementTime().getLineNumber());
										 issue.setXmlString(resultsObs.getMeasurementTime().getXmlString());
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
			validateApprEffectiveTimeScore.setDescription(ApplicationConstants.LABRESULTS_APR_TIME_DESC);
			if(ccdaVersion.equals("") || ccdaVersion.equals(ApplicationConstants.CCDAVersion.R21.getVersion()))
			{
				validateApprEffectiveTimeScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES.RESULT_ORGANIZER.getIgReference());
			}
			else if(ccdaVersion.equals(ApplicationConstants.CCDAVersion.R11.getVersion()))
			{
				validateApprEffectiveTimeScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES_R1.RESULT_ORGANIZER.getIgReference());
			}
			validateApprEffectiveTimeScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_LINKS.RESULTS.getTaskforceLink());
		}
		return validateApprEffectiveTimeScore;
	}
	
	public CCDAScoreCardRubrics getNarrativeStructureIdScore(CCDALabResult results,String ccdaVersion)
	{
		CCDAScoreCardRubrics narrativeTextIdScore = new CCDAScoreCardRubrics();
		narrativeTextIdScore.setRule(ApplicationConstants.NARRATIVE_STRUCTURE_ID_REQ);
		
		int maxPoints = 0;
		int actualPoints = 0;
		int numberOfChecks = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue= null;
		if(results != null)
		{
			if(!ApplicationUtil.isEmpty(results.getResultOrg()))
			{
				for(CCDALabResultOrg resultOrg : results.getResultOrg())
				{
					if(resultOrg.getResultObs()!=null)
					{
						for(CCDALabResultObs resultOrgObs : resultOrg.getResultObs())
						{
							maxPoints++;
							numberOfChecks++;
							if(resultOrgObs.getReferenceText()!= null)
							{
								if(results.getReferenceLinks()!= null && results.getReferenceLinks().contains(resultOrgObs.getReferenceText().getValue()))
								{
									actualPoints++;
								}
								else
								{
									issue = new CCDAXmlSnippet();
									issue.setLineNumber(resultOrgObs.getReferenceText().getLineNumber());
									issue.setXmlString(resultOrgObs.getReferenceText().getXmlString());
									issuesList.add(issue);
								}
							}
							else
							{
								issue = new CCDAXmlSnippet();
								issue.setLineNumber(resultOrgObs.getLineNumber());
								issue.setXmlString(resultOrgObs.getXmlString());
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
				narrativeTextIdScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES.RESULT_SECTION.getIgReference());
			}
			else if (ccdaVersion.equals(ApplicationConstants.CCDAVersion.R11.getVersion()))
			{
				narrativeTextIdScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES_R1.RESULT_SECTION.getIgReference());
			}
			narrativeTextIdScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_LINKS.RESULTS.getTaskforceLink());
		}
		
		return narrativeTextIdScore;
	}
	
	public CCDAScoreCardRubrics getTemplateIdScore(CCDALabResult results,String ccdaVersion)
	{
		CCDAScoreCardRubrics templateIdScore = new CCDAScoreCardRubrics();
		templateIdScore.setRule(ApplicationConstants.TEMPLATEID_DESC);
		
		int maxPoints = 0;
		int actualPoints = 0;
		int numberOfChecks = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		
		if(results!=null)
		{
			if(!ApplicationUtil.isEmpty(results.getResultSectionTempalteIds()))
			{
				for (CCDAII templateId : results.getResultSectionTempalteIds())
				{
					maxPoints++;
					numberOfChecks++;
					actualPoints =  actualPoints + templateIdProcessor.scoreTemplateId(templateId, issuesList, ccdaVersion);
				}
			}
			
			if(!ApplicationUtil.isEmpty(results.getResultOrg()))
			{
				for(CCDALabResultOrg resultOrg :  results.getResultOrg())
				{
					if(!ApplicationUtil.isEmpty(resultOrg.getTemplateIds()))
					{
						for (CCDAII templateId : resultOrg.getTemplateIds())
						{
							maxPoints++;
							numberOfChecks++;
							actualPoints =  actualPoints + templateIdProcessor.scoreTemplateId(templateId, issuesList, ccdaVersion);
						}
					}
					
					if(!ApplicationUtil.isEmpty(resultOrg.getResultObs()))
					{
						for(CCDALabResultObs labResultObs : resultOrg.getResultObs())
						{
							if(!ApplicationUtil.isEmpty(labResultObs.getTemplateIds()))
							{
								for (CCDAII templateId : labResultObs.getTemplateIds())
								{
									maxPoints++;
									numberOfChecks++;
									actualPoints =  actualPoints + templateIdProcessor.scoreTemplateId(templateId, issuesList, ccdaVersion);
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
}
