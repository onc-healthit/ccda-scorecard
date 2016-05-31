package org.sitenv.service.ccda.smartscorecard.processor;

import java.util.ArrayList;
import java.util.List;

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
	
	public Category getLabResultsCategory(CCDALabResult labResults, CCDALabResult labTests, String birthDate)
	{
		
		CCDALabResult results =null;
		if(labResults!= null && !ApplicationUtil.isEmpty(labResults.getResultOrg()))
		{
			results = labResults;
			if(labTests!= null && !ApplicationUtil.isEmpty(labTests.getResultOrg()))
			{
				results.getResultOrg().addAll(labTests.getResultOrg());

			}
		}
		
		Category labResultsCategory = new Category();
		labResultsCategory.setCategoryName("Laboratory Tests and Results");
		
		List<CCDAScoreCardRubrics> labResultsScoreList = new ArrayList<CCDAScoreCardRubrics>();
		labResultsScoreList.add(getTimePrecisionScore(results));
		labResultsScoreList.add(getValidDateTimeScore(results,birthDate));
		labResultsScoreList.add(getValidDisplayNameScoreCard(results));
		labResultsScoreList.add(getValidUCUMScore(labResults));
		labResultsScoreList.add(getValidLoincCodesScore(results));
		labResultsScoreList.add(getApprEffectivetimeScore(results));
		
		labResultsCategory.setCategoryRubrics(labResultsScoreList);
		ApplicationUtil.calculateSectionGradeAndIssues(labResultsScoreList, labResultsCategory);
		
		return labResultsCategory;
		
	}
	
	public CCDAScoreCardRubrics getTimePrecisionScore(CCDALabResult labResults)
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
							if(ApplicationUtil.validateDayFormat(resultOrg.getEffTime().getLow().getValue()))
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
							if(ApplicationUtil.validateDayFormat(resultOrg.getEffTime().getHigh().getValue()))
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
								if(ApplicationUtil.validateDayFormat(resultObs.getMeasurementTime().getValue()))
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
			timePrecisionScore.getIgReferences().add(ApplicationConstants.IG_SECTION_REFERENCES);
			timePrecisionScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_URL);
		}else 
		{
		    timePrecisionScore.setDescription("Time precision Rubric executed successfully for Labresults");
		}
		return timePrecisionScore;
	}
	
	
	public CCDAScoreCardRubrics getValidDateTimeScore(CCDALabResult labResults, String birthDate)
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
			validateTimeScore.getIgReferences().add(ApplicationConstants.IG_SECTION_REFERENCES);
			validateTimeScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_URL);
		}else 
		{
			validateTimeScore.setDescription("Time validation Rubric executed successfully for Labresults");
		}
		return validateTimeScore;
	}
	
	public CCDAScoreCardRubrics getValidDisplayNameScoreCard(CCDALabResult labresults)
	{
		CCDAScoreCardRubrics validateDisplayNameScore = new CCDAScoreCardRubrics();
		validateDisplayNameScore.setRule(ApplicationConstants.CODE_DISPLAYNAME_REQUIREMENT);

		int maxPoints = 0;
		int actualPoints = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue= null;
		if(labresults != null)
		{
			maxPoints++;
			if(labresults.getSectionCode()!= null)
			{
				if(ApplicationUtil.validateDisplayName(labresults.getSectionCode().getCode(), 
						ApplicationConstants.CODE_SYSTEM_MAP.get(labresults.getSectionCode().getCodeSystem()),
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
			else
			{
				issue = new CCDAXmlSnippet();
				issue.setLineNumber(labresults.getLineNumber());
				issue.setXmlString(labresults.getXmlString());
				issuesList.add(issue);
			}
			
			if(!ApplicationUtil.isEmpty(labresults.getResultOrg()))
			{
				for (CCDALabResultOrg resultOrg : labresults.getResultOrg())
				{
					maxPoints++;
					if(resultOrg.getOrgCode()!= null)
					{
						if(ApplicationUtil.validateDisplayName(resultOrg.getOrgCode().getCode(), 
								ApplicationConstants.CODE_SYSTEM_MAP.get(resultOrg.getOrgCode().getCodeSystem()),
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
					else
					{
						issue = new CCDAXmlSnippet();
						issue.setLineNumber(resultOrg.getLineNumber());
						issue.setXmlString(resultOrg.getXmlString());
						issuesList.add(issue);
					}
					
					if(!ApplicationUtil.isEmpty(resultOrg.getResultObs()))
					{
						for (CCDALabResultObs resultobs : resultOrg.getResultObs())
						{
							maxPoints++;
							if(resultobs.getResultCode()!= null)
							{
								if(ApplicationUtil.validateDisplayName(resultobs.getResultCode().getCode(), 
										ApplicationConstants.CODE_SYSTEM_MAP.get(resultobs.getResultCode().getCodeSystem()),
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
							else
							{
								issue = new CCDAXmlSnippet();
								issue.setLineNumber(resultobs.getLineNumber());
								issue.setXmlString(resultobs.getXmlString());
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
			issue.setLineNumber("Results section not present");
			issue.setXmlString("Results section not present");
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
		}else 
		{
			validateDisplayNameScore.setDescription("code display name validation Rubric executed successfully for Labresults");
		}
		return validateDisplayNameScore;
	}
	
	public  CCDAScoreCardRubrics getValidUCUMScore(CCDALabResult labresults)
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
									if(loincRepository.foundUCUMUnitsForLoincCode(resultsObs.getResultCode().getCode(),resultsObs.getResults().getUnits()))
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
			validateUCUMScore.getIgReferences().add(ApplicationConstants.IG_SECTION_REFERENCES);
			validateUCUMScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_URL);
		}else 
		{
			validateUCUMScore.setDescription("UCUM units validation validation Rubric executed successfully for Labresults");
		}
		return validateUCUMScore;
	}
	
	public CCDAScoreCardRubrics getValidLoincCodesScore(CCDALabResult labresults)
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
			validatLoincCodeScore.getIgReferences().add(ApplicationConstants.IG_SECTION_REFERENCES);
			validatLoincCodeScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_URL);
		}else 
		{
			validatLoincCodeScore.setDescription("Loinc code validation validation Rubric executed successfully for Labresults");
		}
		return validatLoincCodeScore;
		
	}
	
	public CCDAScoreCardRubrics getApprEffectivetimeScore(CCDALabResult results)
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
			validateApprEffectiveTimeScore.getIgReferences().add(ApplicationConstants.IG_SECTION_REFERENCES);
			validateApprEffectiveTimeScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_URL);
		}else 
		{
			validateApprEffectiveTimeScore.setDescription("Appropriate effective time validation validation Rubric executed successfully for Labresults");
		}
		return validateApprEffectiveTimeScore;
	}
}
