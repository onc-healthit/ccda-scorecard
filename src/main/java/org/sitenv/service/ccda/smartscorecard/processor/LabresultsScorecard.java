package org.sitenv.service.ccda.smartscorecard.processor;

import java.util.ArrayList;
import java.util.List;

import org.sitenv.ccdaparsing.model.CCDADataElement;
import org.sitenv.ccdaparsing.model.CCDALabResult;
import org.sitenv.ccdaparsing.model.CCDALabResultObs;
import org.sitenv.ccdaparsing.model.CCDALabResultOrg;
import org.sitenv.ccdaparsing.model.CCDAXmlSnippet;
import org.sitenv.service.ccda.smartscorecard.model.CCDAScoreCardRubrics;
import org.sitenv.service.ccda.smartscorecard.model.Category;
import org.sitenv.service.ccda.smartscorecard.repositories.inmemory.LoincRepository;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationConstants;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LabresultsScorecard {
	
	@Autowired
	LoincRepository loincRepository;
	
	public Category getLabResultsCategory(CCDALabResult labResults, CCDALabResult labTests, String birthDate,String docType)
	{
		
		if(labResults.isSectionNullFlavourWithNI())
		{
			return new Category(ApplicationConstants.CATEGORIES.RESULTS.getCategoryDesc(),true);
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
		
		Category labResultsCategory = new Category();
		labResultsCategory.setCategoryName(ApplicationConstants.CATEGORIES.RESULTS.getCategoryDesc());
		
		List<CCDAScoreCardRubrics> labResultsScoreList = new ArrayList<CCDAScoreCardRubrics>();
		labResultsScoreList.add(getTimePrecisionScore(results,docType));
		labResultsScoreList.add(getValidDateTimeScore(results,birthDate,docType));
		labResultsScoreList.add(getValidDisplayNameScoreCard(results,docType));
		labResultsScoreList.add(getValidUCUMScore(labResults,docType));
		labResultsScoreList.add(getValidLoincCodesScore(results,docType));
		labResultsScoreList.add(getApprEffectivetimeScore(results,docType));
		labResultsScoreList.add(getNarrativeStructureIdScore(results,docType));
		
		labResultsCategory.setCategoryRubrics(labResultsScoreList);
		ApplicationUtil.calculateSectionGradeAndIssues(labResultsScoreList, labResultsCategory);
		
		return labResultsCategory;
		
	}
	
	public CCDAScoreCardRubrics getTimePrecisionScore(CCDALabResult labResults,String docType)
	{
		CCDAScoreCardRubrics timePrecisionScore = new CCDAScoreCardRubrics();
		timePrecisionScore.setRule(ApplicationConstants.TIME_PRECISION_REQUIREMENT);
		
		int actualPoints =0;
		int maxPoints = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue= null;
		if(labResults != null)
		{
			if(!ApplicationUtil.isEmpty(labResults.getResultOrg()))
			{
				for (CCDALabResultOrg resultOrg : labResults.getResultOrg())
				{
					maxPoints = maxPoints + 2;
					if(resultOrg.getEffTime() != null)
					{
						if(resultOrg.getEffTime().getLow() != null)
						{
							if(ApplicationUtil.validateDayFormat(resultOrg.getEffTime().getLow().getValue()) ||
									ApplicationUtil.validateMinuteFormat(resultOrg.getEffTime().getLow().getValue()) ||
									ApplicationUtil.validateSecondFormat(resultOrg.getEffTime().getLow().getValue()))
							{
								actualPoints++;
							}
							else
							{
								issue = new CCDAXmlSnippet();
								issue.setLineNumber(resultOrg.getEffTime().getLow().getLineNumber());
								issue.setXmlString(resultOrg.getEffTime().getLow().getXmlString());
								issuesList.add(issue);
							}
						}
						else
						{
							issue = new CCDAXmlSnippet();
							issue.setLineNumber(resultOrg.getEffTime().getLineNumber());
							issue.setXmlString(resultOrg.getEffTime().getXmlString());
							issuesList.add(issue);
						}
						if(resultOrg.getEffTime().getHigh() != null)
						{
							if(ApplicationUtil.validateDayFormat(resultOrg.getEffTime().getHigh().getValue()) ||
									ApplicationUtil.validateMinuteFormat(resultOrg.getEffTime().getHigh().getValue()) ||
									ApplicationUtil.validateSecondFormat(resultOrg.getEffTime().getHigh().getValue()))
							{
								actualPoints++;
							}
							else
							{
								issue = new CCDAXmlSnippet();
								issue.setLineNumber(resultOrg.getEffTime().getHigh().getLineNumber());
								issue.setXmlString(resultOrg.getEffTime().getHigh().getXmlString());
								issuesList.add(issue);
							}
						}
						else
						{
							issue = new CCDAXmlSnippet();
							issue.setLineNumber(resultOrg.getEffTime().getLineNumber());
							issue.setXmlString(resultOrg.getEffTime().getXmlString());
							issuesList.add(issue);
						}
					}
					else
					{
						issue = new CCDAXmlSnippet();
						issue.setLineNumber(resultOrg.getLineNumber());
						issue.setXmlString(resultOrg.getXmlString());
						issuesList.add(issue);
					}
					
					if(!ApplicationUtil.isEmpty(resultOrg.getResultObs()))
					{
						for (CCDALabResultObs resultObs : resultOrg.getResultObs() )
						{
							maxPoints++;
							if(resultObs.getMeasurementTime() != null)
							{
								if(ApplicationUtil.validateDayFormat(resultObs.getMeasurementTime().getValue()) ||
										ApplicationUtil.validateMinuteFormat(resultObs.getMeasurementTime().getValue()) ||
										ApplicationUtil.validateSecondFormat(resultObs.getMeasurementTime().getValue()))
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
			else
			{
				issue = new CCDAXmlSnippet();
				issue.setLineNumber(labResults.getLineNumber());
				issue.setXmlString(labResults.getXmlString());
				issuesList.add(issue);
			}
		}
		else
		{
			issue = new CCDAXmlSnippet();
			issue.setLineNumber("Results section not present");
			issue.setXmlString("Results section not present");
			issuesList.add(issue);
		}

		timePrecisionScore.setActualPoints(actualPoints);
		timePrecisionScore.setMaxPoints(maxPoints);
		timePrecisionScore.setMaxPoints(maxPoints);
		timePrecisionScore.setRubricScore(ApplicationUtil.calculateRubricScore(maxPoints, actualPoints));
		timePrecisionScore.setIssuesList(issuesList);
		timePrecisionScore.setNumberOfIssues(issuesList.size());
		if(issuesList.size() > 0)
		{
			timePrecisionScore.setDescription(ApplicationConstants.TIME_PRECISION_DESCRIPTION);
			if(docType.equalsIgnoreCase("") || docType.equalsIgnoreCase("R2.1"))
			{
				timePrecisionScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES.RESULT_ORGANIZER.getIgReference());
			}
			else if (docType.equalsIgnoreCase("R1.1"))
			{
				timePrecisionScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES_R1.RESULT_ORGANIZER.getIgReference());
			}
			timePrecisionScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_LINKS.RESULTS.getTaskforceLink());
		}
		return timePrecisionScore;
	}
	
	
	public CCDAScoreCardRubrics getValidDateTimeScore(CCDALabResult labResults, String birthDate,String docType)
	{
		CCDAScoreCardRubrics validateTimeScore = new CCDAScoreCardRubrics();
		validateTimeScore.setRule(ApplicationConstants.TIME_VALID_REQUIREMENT);
		
		int actualPoints =0;
		int maxPoints = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue= null;
		if(labResults != null)
		{
			if(!ApplicationUtil.isEmpty(labResults.getResultOrg()))
			{
				for (CCDALabResultOrg resultOrg : labResults.getResultOrg())
				{
					maxPoints = maxPoints + 2;
					if(resultOrg.getEffTime() != null)
					{
						if(resultOrg.getEffTime().getLow() != null)
						{
							if(resultOrg.getEffTime().getLow() != null)
							{
								if(ApplicationUtil.checkDateRange(birthDate, resultOrg.getEffTime().getLow().getValue()))
								{
									actualPoints++;
								}
								else
								{
									issue = new CCDAXmlSnippet();
									issue.setLineNumber(resultOrg.getEffTime().getLow().getLineNumber());
									issue.setXmlString(resultOrg.getEffTime().getLow().getXmlString());
									issuesList.add(issue);
								}
							}
							else
							{
								issue = new CCDAXmlSnippet();
								issue.setLineNumber(resultOrg.getEffTime().getLineNumber());
								issue.setXmlString(resultOrg.getEffTime().getXmlString());
								issuesList.add(issue);
							}
						}
						if(resultOrg.getEffTime().getHigh() != null)
						{
							if(ApplicationUtil.checkDateRange(birthDate, resultOrg.getEffTime().getHigh().getValue()))
							{
								actualPoints++;
							}
							else
							{
								issue = new CCDAXmlSnippet();
								issue.setLineNumber(resultOrg.getEffTime().getHigh().getLineNumber());
								issue.setXmlString(resultOrg.getEffTime().getHigh().getXmlString());
								issuesList.add(issue);
							}
						}
						else
						{
							issue = new CCDAXmlSnippet();
							issue.setLineNumber(resultOrg.getEffTime().getLineNumber());
							issue.setXmlString(resultOrg.getEffTime().getXmlString());
							issuesList.add(issue);
						}
					}
					else
					{
						issue = new CCDAXmlSnippet();
						issue.setLineNumber(resultOrg.getLineNumber());
						issue.setXmlString(resultOrg.getXmlString());
						issuesList.add(issue);
					}
					
					if(!ApplicationUtil.isEmpty(resultOrg.getResultObs()))
					{
						for (CCDALabResultObs resultObs : resultOrg.getResultObs() )
						{
							maxPoints++;
							if(resultObs.getMeasurementTime() != null)
							{
								if(ApplicationUtil.checkDateRange(birthDate, resultObs.getMeasurementTime().getValue()))
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
			else
			{
				issue = new CCDAXmlSnippet();
				issue.setLineNumber(labResults.getLineNumber());
				issue.setXmlString(labResults.getXmlString());
				issuesList.add(issue);
			}
		}
		else
		{
			issue = new CCDAXmlSnippet();
			issue.setLineNumber("Results section not present");
			issue.setXmlString("Results section not present");
			issuesList.add(issue);
		}

		
		validateTimeScore.setActualPoints(actualPoints);
		validateTimeScore.setMaxPoints(maxPoints);
		validateTimeScore.setRubricScore(ApplicationUtil.calculateRubricScore(maxPoints, actualPoints));
		validateTimeScore.setIssuesList(issuesList);
		validateTimeScore.setNumberOfIssues(issuesList.size());
		if(issuesList.size() > 0)
		{
			validateTimeScore.setDescription(ApplicationConstants.TIME_VALID_DESCRIPTION);
			if(docType.equalsIgnoreCase("") || docType.equalsIgnoreCase("R2.1"))
			{
				validateTimeScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES.RESULT_ORGANIZER.getIgReference());
			}
			else if (docType.equalsIgnoreCase("R1.1"))
			{
				validateTimeScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES_R1.RESULT_ORGANIZER.getIgReference());
			}
			validateTimeScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_LINKS.RESULTS.getTaskforceLink());
		}
		return validateTimeScore;
	}
	
	public CCDAScoreCardRubrics getValidDisplayNameScoreCard(CCDALabResult labresults,String docType)
	{
		CCDAScoreCardRubrics validateDisplayNameScore = new CCDAScoreCardRubrics();
		validateDisplayNameScore.setRule(ApplicationConstants.CODE_DISPLAYNAME_REQUIREMENT);

		int maxPoints = 0;
		int actualPoints = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue= null;
		if(labresults != null)
		{
			if(labresults.getSectionCode()!= null && !ApplicationUtil.isEmpty(labresults.getSectionCode().getDisplayName()))
			{
				maxPoints++;
				if(ApplicationUtil.validateDisplayName(labresults.getSectionCode().getCode(), 
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
					if(resultOrg.getOrgCode()!= null && !ApplicationUtil.isEmpty(resultOrg.getOrgCode().getDisplayName()))
					{
						maxPoints++;
						if(ApplicationUtil.validateDisplayName(resultOrg.getOrgCode().getCode(), 
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
							if(resultobs.getResultCode()!= null && !ApplicationUtil.isEmpty(resultobs.getResultCode().getDisplayName()))
							{
								maxPoints++;
								if(ApplicationUtil.validateDisplayName(resultobs.getResultCode().getCode(), 
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
		if(issuesList.size() > 0)
		{
			validateDisplayNameScore.setDescription(ApplicationConstants.CODE_DISPLAYNAME_DESCRIPTION);
			if(docType.equalsIgnoreCase("") || docType.equalsIgnoreCase("R2.1"))
			{
				validateDisplayNameScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES.RESULT_SECTION.getIgReference());
			}
			else if (docType.equalsIgnoreCase("R1.1"))
			{
				validateDisplayNameScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES_R1.RESULT_SECTION.getIgReference());
			}
			validateDisplayNameScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_LINKS.RESULTS.getTaskforceLink());
		}
		return validateDisplayNameScore;
	}
	
	public  CCDAScoreCardRubrics getValidUCUMScore(CCDALabResult labresults,String docType)
	{
		CCDAScoreCardRubrics validateUCUMScore = new CCDAScoreCardRubrics();
		validateUCUMScore.setRule(ApplicationConstants.RESULTS_UCUM_REQ);
		
		int maxPoints = 0;
		int actualPoints = 0;
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
							if(resultsObs.getResults() != null && resultsObs.getResults().getXsiType().equalsIgnoreCase("PQ"))
							{
								maxPoints++;
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
								issue = new CCDAXmlSnippet();
								issue.setLineNumber(resultsObs.getLineNumber());
								issue.setXmlString(resultsObs.getXmlString());
								issuesList.add(issue);
							}
						}
					}
				}
			}
			else
			{
				issue = new CCDAXmlSnippet();
				issue.setLineNumber(labresults.getLineNumber());
				issue.setXmlString(labresults.getXmlString());
				issuesList.add(issue);
			}
		}
		else
		{
			issue = new CCDAXmlSnippet();
			issue.setLineNumber("Results section not present");
			issue.setXmlString("Results section not present");
			issuesList.add(issue);
		}
		
		validateUCUMScore.setActualPoints(actualPoints);
		validateUCUMScore.setMaxPoints(maxPoints);
		validateUCUMScore.setRubricScore(ApplicationUtil.calculateRubricScore(maxPoints, actualPoints));
		validateUCUMScore.setIssuesList(issuesList);
		validateUCUMScore.setNumberOfIssues(issuesList.size());
		if(issuesList.size() > 0)
		{
			validateUCUMScore.setDescription(ApplicationConstants.RESULTS_UCUM_DESC);
			if(docType.equalsIgnoreCase("") || docType.equalsIgnoreCase("R2.1"))
			{
				validateUCUMScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES.RESULT_OBSERVATION.getIgReference());
			}
			else if (docType.equalsIgnoreCase("R1.1"))
			{
				validateUCUMScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES_R1.RESULT_OBSERVATION.getIgReference());
			}
			validateUCUMScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_LINKS.RESULTS_UCUM.getTaskforceLink());
		}
		return validateUCUMScore;
	}
	
	public CCDAScoreCardRubrics getValidLoincCodesScore(CCDALabResult labresults,String docType)
	{
		CCDAScoreCardRubrics validatLoincCodeScore = new CCDAScoreCardRubrics();
		validatLoincCodeScore.setRule(ApplicationConstants.LABRESULTS_LOIN_CODE_REQ);
		
		int maxPoints = 0;
		int actualPoints = 0;
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
			else
			{
				issue = new CCDAXmlSnippet();
				issue.setLineNumber(labresults.getLineNumber());
				issue.setXmlString(labresults.getXmlString());
				issuesList.add(issue);
			}
		}
		else
		{
			issue = new CCDAXmlSnippet();
			issue.setLineNumber("Results section not present");
			issue.setXmlString("Results section not present");
			issuesList.add(issue);
		}
		
		validatLoincCodeScore.setActualPoints(actualPoints);
		validatLoincCodeScore.setMaxPoints(maxPoints);
		validatLoincCodeScore.setRubricScore(ApplicationUtil.calculateRubricScore(maxPoints, actualPoints));
		validatLoincCodeScore.setIssuesList(issuesList);
		validatLoincCodeScore.setNumberOfIssues(issuesList.size());
		if(issuesList.size() > 0)
		{
			validatLoincCodeScore.setDescription(ApplicationConstants.LABRESULTS_LOIN_CODE_REQ);
			if(docType.equalsIgnoreCase("") || docType.equalsIgnoreCase("R2.1"))
			{
				validatLoincCodeScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES.RESULT_OBSERVATION.getIgReference());
			}
			else if(docType.equalsIgnoreCase("R1.1"))
			{
				validatLoincCodeScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES_R1.RESULT_OBSERVATION.getIgReference());
			}
			validatLoincCodeScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_LINKS.RESULTS_UCUM.getTaskforceLink());
		}
		return validatLoincCodeScore;
		
	}
	
	public CCDAScoreCardRubrics getApprEffectivetimeScore(CCDALabResult results,String docType)
	{
		CCDAScoreCardRubrics validateApprEffectiveTimeScore = new CCDAScoreCardRubrics();
		validateApprEffectiveTimeScore.setRule(ApplicationConstants.LABRESULTS_APR_TIME_REQ);
		
		int maxPoints = 0;
		int actualPoints = 0;
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
								maxPoints++;
								if(resultsObs.getMeasurementTime() != null)
								{
									if(ApplicationUtil.checkDateRange(resultOrg.getEffTime().getLow(), resultsObs.getMeasurementTime().getValue(), 
															resultOrg.getEffTime().getHigh()))
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
								else 
								{
									issue = new CCDAXmlSnippet();
									issue.setLineNumber(resultsObs.getLineNumber());
									issue.setXmlString(resultsObs.getXmlString());
									issuesList.add(issue);
								}
							}
						}
					}
					else 
					{
						issue = new CCDAXmlSnippet();
						issue.setLineNumber(resultOrg.getLineNumber());
						issue.setXmlString(resultOrg.getXmlString());
						issuesList.add(issue);
					}
				}
			}
			else
			{
				issue = new CCDAXmlSnippet();
				issue.setLineNumber(results.getLineNumber());
				issue.setXmlString(results.getXmlString());
				issuesList.add(issue);
			}
		}
		else
		{
			issue = new CCDAXmlSnippet();
			issue.setLineNumber("Results section not present");
			issue.setXmlString("Results section not present");
			issuesList.add(issue);
		}
		
		validateApprEffectiveTimeScore.setActualPoints(actualPoints);
		validateApprEffectiveTimeScore.setMaxPoints(maxPoints);
		validateApprEffectiveTimeScore.setRubricScore(ApplicationUtil.calculateRubricScore(maxPoints, actualPoints));
		validateApprEffectiveTimeScore.setIssuesList(issuesList);
		validateApprEffectiveTimeScore.setNumberOfIssues(issuesList.size());
		if(issuesList.size() > 0)
		{
			validateApprEffectiveTimeScore.setDescription(ApplicationConstants.LABRESULTS_APR_TIME_DESC);
			if(docType.equalsIgnoreCase("") || docType.equalsIgnoreCase("R2.1"))
			{
				validateApprEffectiveTimeScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES.RESULT_ORGANIZER.getIgReference());
			}
			else if(docType.equalsIgnoreCase("R1.1"))
			{
				validateApprEffectiveTimeScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES_R1.RESULT_ORGANIZER.getIgReference());
			}
			validateApprEffectiveTimeScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_LINKS.RESULTS.getTaskforceLink());
		}
		return validateApprEffectiveTimeScore;
	}
	
	public CCDAScoreCardRubrics getNarrativeStructureIdScore(CCDALabResult results,String docType)
	{
		CCDAScoreCardRubrics narrativeTextIdScore = new CCDAScoreCardRubrics();
		narrativeTextIdScore.setRule(ApplicationConstants.NARRATIVE_STRUCTURE_ID_REQ);
		
		int maxPoints = 0;
		int actualPoints = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue= null;
		if(results != null)
		{
			if(!ApplicationUtil.isEmpty(results.getResultOrg()))
			{
				for(CCDALabResultOrg resultOrg : results.getResultOrg())
				{
					if(!ApplicationUtil.isEmpty(resultOrg.getReferenceTexts()))
					{
						for(CCDADataElement referenceText : resultOrg.getReferenceTexts())
						{
							maxPoints++;
							if(results.getReferenceLinks().contains(referenceText.getValue()))
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
				narrativeTextIdScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES.RESULT_SECTION.getIgReference());
			}
			else if (docType.equalsIgnoreCase("R1.1"))
			{
				narrativeTextIdScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES_R1.RESULT_SECTION.getIgReference());
			}
			narrativeTextIdScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_LINKS.RESULTS.getTaskforceLink());
		}
		
		return narrativeTextIdScore;
	}
}
