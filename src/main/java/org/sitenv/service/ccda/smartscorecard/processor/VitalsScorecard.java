package org.sitenv.service.ccda.smartscorecard.processor;

import java.util.ArrayList;
import java.util.List;

import org.sitenv.ccdaparsing.model.CCDADataElement;
import org.sitenv.ccdaparsing.model.CCDAVitalObs;
import org.sitenv.ccdaparsing.model.CCDAVitalOrg;
import org.sitenv.ccdaparsing.model.CCDAVitalSigns;
import org.sitenv.ccdaparsing.model.CCDAXmlSnippet;
import org.sitenv.service.ccda.smartscorecard.model.CCDAScoreCardRubrics;
import org.sitenv.service.ccda.smartscorecard.model.Category;
import org.sitenv.service.ccda.smartscorecard.repositories.inmemory.VitalsRepository;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationConstants;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VitalsScorecard {
	
	@Autowired
	VitalsRepository vitalsRepository;
	
	public Category getVitalsCategory(CCDAVitalSigns vitals, String birthDate)
	{
		
		Category vitalsCategory = new Category();
		vitalsCategory.setCategoryName(ApplicationConstants.CATEGORIES.VITALS.getCategoryDesc());
		
		List<CCDAScoreCardRubrics> vitalsScoreList = new ArrayList<CCDAScoreCardRubrics>();
		vitalsScoreList.add(getTimePrecisionScore(vitals));
		vitalsScoreList.add(getValidDateTimeScore(vitals,birthDate));
		vitalsScoreList.add(getValidDisplayNameScoreCard(vitals));
		vitalsScoreList.add(getValidLoincCodesScore(vitals));
		vitalsScoreList.add(getValidUCUMScore(vitals));
		vitalsScoreList.add(getApprEffectivetimeScore(vitals));
		vitalsScoreList.add(getNarrativeStructureIdScore(vitals));
		
		vitalsCategory.setCategoryRubrics(vitalsScoreList);
		ApplicationUtil.calculateSectionGradeAndIssues(vitalsScoreList, vitalsCategory);
		
		return vitalsCategory;
		
	}
	
	
	public CCDAScoreCardRubrics getTimePrecisionScore(CCDAVitalSigns vitals)
	{
		CCDAScoreCardRubrics timePrecisionScore = new CCDAScoreCardRubrics();
		timePrecisionScore.setRule(ApplicationConstants.TIME_PRECISION_REQUIREMENT);
		
		int actualPoints = 0;
		int maxPoints = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue= null;
		if(vitals != null)
		{
			if(!ApplicationUtil.isEmpty(vitals.getVitalsOrg()))
			{
				for (CCDAVitalOrg vitalOrg : vitals.getVitalsOrg())
				{
					maxPoints = maxPoints + 2;
					if(vitalOrg.getEffTime() != null)
					{
						if(vitalOrg.getEffTime().getLow() != null)
						{
							if(ApplicationUtil.validateDayFormat(vitalOrg.getEffTime().getLow().getValue()))
							{
								actualPoints++;
							}
							else
							{
								issue = new CCDAXmlSnippet();
								issue.setLineNumber(vitalOrg.getEffTime().getLow().getLineNumber());
								issue.setXmlString(vitalOrg.getEffTime().getLow().getXmlString());
								issuesList.add(issue);
							}
						}
						else
						{
							issue = new CCDAXmlSnippet();
							issue.setLineNumber(vitalOrg.getEffTime().getLineNumber());
							issue.setXmlString(vitalOrg.getEffTime().getXmlString());
							issuesList.add(issue);
						}
						if(vitalOrg.getEffTime().getHigh() != null)
						{
							if(ApplicationUtil.validateDayFormat(vitalOrg.getEffTime().getHigh().getValue()))
							{
								actualPoints++;
							}
							else
							{
								issue = new CCDAXmlSnippet();
								issue.setLineNumber(vitalOrg.getEffTime().getHigh().getLineNumber());
								issue.setXmlString(vitalOrg.getEffTime().getHigh().getXmlString());
								issuesList.add(issue);
							}
						}
						else
						{
							issue = new CCDAXmlSnippet();
							issue.setLineNumber(vitalOrg.getEffTime().getLineNumber());
							issue.setXmlString(vitalOrg.getEffTime().getXmlString());
							issuesList.add(issue);
						}
					}
					else
					{
						issue = new CCDAXmlSnippet();
						issue.setLineNumber(vitalOrg.getLineNumber());
						issue.setXmlString(vitalOrg.getXmlString());
						issuesList.add(issue);
					}
					
					if(!ApplicationUtil.isEmpty(vitalOrg.getVitalObs()))
					{
						for (CCDAVitalObs vitalObs : vitalOrg.getVitalObs() )
						{
							maxPoints++;
							if(vitalObs.getMeasurementTime() != null)
							{
								if(ApplicationUtil.validateMinuteFormat(vitalObs.getMeasurementTime().getValue()))
								{
									actualPoints++;
								}
								else
								{
									issue = new CCDAXmlSnippet();
									issue.setLineNumber(vitalObs.getMeasurementTime().getLineNumber());
									issue.setXmlString(vitalObs.getMeasurementTime().getXmlString());
									issuesList.add(issue);
								}
							}
							else
							{
								issue = new CCDAXmlSnippet();
								issue.setLineNumber(vitalObs.getLineNumber());
								issue.setXmlString(vitalObs.getXmlString());
								issuesList.add(issue);
							}
						}
					}
				}
			}
			else
			{
				issue = new CCDAXmlSnippet();
				issue.setLineNumber(vitals.getLineNumber());
				issue.setXmlString(vitals.getXmlString());
				issuesList.add(issue);
			}
		}
		else
		{
			issue = new CCDAXmlSnippet();
			issue.setLineNumber("Vitals section not present");
			issue.setXmlString("Vitals section not present");
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
	
	
	public CCDAScoreCardRubrics getValidDateTimeScore(CCDAVitalSigns vitals, String birthDate)
	{
		CCDAScoreCardRubrics validateTimeScore = new CCDAScoreCardRubrics();
		validateTimeScore.setRule(ApplicationConstants.TIME_VALID_REQUIREMENT);
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue= null;
		int actualPoints = 0;
		int maxPoints = 0;
		
		if(vitals != null)
		{
			if(!ApplicationUtil.isEmpty(vitals.getVitalsOrg()))
			{
				for (CCDAVitalOrg vitalOrg : vitals.getVitalsOrg())
				{
					maxPoints = maxPoints + 2;
					if(vitalOrg.getEffTime() != null)
					{
						if(vitalOrg.getEffTime().getLow() != null)
						{
							if(ApplicationUtil.checkDateRange(birthDate, vitalOrg.getEffTime().getLow().getValue()))
							{
								actualPoints++;
							}
							else
							{
								issue = new CCDAXmlSnippet();
								issue.setLineNumber(vitalOrg.getEffTime().getLow().getLineNumber());
								issue.setXmlString(vitalOrg.getEffTime().getLow().getXmlString());
								issuesList.add(issue);
							}
						}
						else
						{
							issue = new CCDAXmlSnippet();
							issue.setLineNumber(vitalOrg.getEffTime().getLineNumber());
							issue.setXmlString(vitalOrg.getEffTime().getXmlString());
							issuesList.add(issue);
						}
						if(vitalOrg.getEffTime().getHigh() != null)
						{
							if(ApplicationUtil.checkDateRange(birthDate, vitalOrg.getEffTime().getHigh().getValue()))
							{
								actualPoints++;
							}
							else
							{
								issue = new CCDAXmlSnippet();
								issue.setLineNumber(vitalOrg.getEffTime().getHigh().getLineNumber());
								issue.setXmlString(vitalOrg.getEffTime().getHigh().getXmlString());
								issuesList.add(issue);
							}
						}
						else
						{
							issue = new CCDAXmlSnippet();
							issue.setLineNumber(vitalOrg.getEffTime().getLineNumber());
							issue.setXmlString(vitalOrg.getEffTime().getXmlString());
							issuesList.add(issue);
						}
					}
					else
					{
						issue = new CCDAXmlSnippet();
						issue.setLineNumber(vitalOrg.getLineNumber());
						issue.setXmlString(vitalOrg.getXmlString());
						issuesList.add(issue);
					}
					
					if(!ApplicationUtil.isEmpty(vitalOrg.getVitalObs()))
					{
						for (CCDAVitalObs vitalObs : vitalOrg.getVitalObs() )
						{
							maxPoints++;
							if(vitalObs.getMeasurementTime() != null)
							{
								if(ApplicationUtil.checkDateRange(birthDate, vitalObs.getMeasurementTime().getValue()))
								{
									actualPoints++;
								}
								else
								{
									issue = new CCDAXmlSnippet();
									issue.setLineNumber(vitalObs.getMeasurementTime().getLineNumber());
									issue.setXmlString(vitalObs.getMeasurementTime().getXmlString());
									issuesList.add(issue);
								}
							}
							else
							{
								issue = new CCDAXmlSnippet();
								issue.setLineNumber(vitalObs.getLineNumber());
								issue.setXmlString(vitalObs.getXmlString());
								issuesList.add(issue);
							}
						}
					}
				}
			}
			else
			{
				issue = new CCDAXmlSnippet();
				issue.setLineNumber(vitals.getLineNumber());
				issue.setXmlString(vitals.getXmlString());
				issuesList.add(issue);
			}
		}
		else
		{
			issue = new CCDAXmlSnippet();
			issue.setLineNumber("Vitals section not present");
			issue.setXmlString("Vitals section not present");
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
		}
		return validateTimeScore;
	}
	
	public CCDAScoreCardRubrics getValidDisplayNameScoreCard(CCDAVitalSigns vitals)
	{
		CCDAScoreCardRubrics validateDisplayNameScore = new CCDAScoreCardRubrics();
		validateDisplayNameScore.setRule(ApplicationConstants.CODE_DISPLAYNAME_REQUIREMENT);
		
		int maxPoints = 0;
		int actualPoints = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue= null;
		if(vitals != null)
		{
			maxPoints++;
			if(vitals.getSectionCode()!= null)
			{
				if(ApplicationUtil.validateDisplayName(vitals.getSectionCode().getCode(), 
														vitals.getSectionCode().getCodeSystem(),
														vitals.getSectionCode().getDisplayName()))
				{
					actualPoints++;
				}
				else
				{
					issue = new CCDAXmlSnippet();
					issue.setLineNumber(vitals.getSectionCode().getLineNumber());
					issue.setXmlString(vitals.getSectionCode().getXmlString());
					issuesList.add(issue);
				}
			}
			else
			{
				issue = new CCDAXmlSnippet();
				issue.setLineNumber(vitals.getLineNumber());
				issue.setXmlString(vitals.getXmlString());
				issuesList.add(issue);
			}
			
			if(!ApplicationUtil.isEmpty(vitals.getVitalsOrg()))
			{
				for (CCDAVitalOrg vitalsOrg : vitals.getVitalsOrg())
				{
					maxPoints++;
					if(vitalsOrg.getOrgCode() != null)
					{
						if(ApplicationUtil.validateDisplayName(vitalsOrg.getOrgCode().getCode(), 
																vitalsOrg.getOrgCode().getCodeSystem(),
																		vitalsOrg.getOrgCode().getDisplayName()))
						{
							actualPoints++;
						}
						else
						{
							issue = new CCDAXmlSnippet();
							issue.setLineNumber(vitalsOrg.getOrgCode().getLineNumber());
							issue.setXmlString(vitalsOrg.getOrgCode().getXmlString());
							issuesList.add(issue);
						}
					}
					
					else
					{
						issue = new CCDAXmlSnippet();
						issue.setLineNumber(vitalsOrg.getLineNumber());
						issue.setXmlString(vitalsOrg.getXmlString());
						issuesList.add(issue);
					}
					
					if(!ApplicationUtil.isEmpty(vitalsOrg.getVitalObs()))
					{
					
						for(CCDAVitalObs vitalsObs : vitalsOrg.getVitalObs())
						{
							maxPoints++;
							if(vitalsObs.getVsCode() != null)
							{
								if(ApplicationUtil.validateDisplayName(vitalsObs.getVsCode().getCode(), 
																	vitalsObs.getVsCode().getCodeSystem(),
																	vitalsObs.getVsCode().getDisplayName()))
								{
									actualPoints++;
								}
								else
								{
									issue = new CCDAXmlSnippet();
									issue.setLineNumber(vitalsObs.getVsCode().getLineNumber());
									issue.setXmlString(vitalsObs.getVsCode().getXmlString());
									issuesList.add(issue);
								}
							}
							else
							{
								issue = new CCDAXmlSnippet();
								issue.setLineNumber(vitalsObs.getLineNumber());
								issue.setXmlString(vitalsObs.getXmlString());
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
			issue.setLineNumber("Vitals section not present");
			issue.setXmlString("Vitals section not present");
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
	
	public CCDAScoreCardRubrics getValidUCUMScore(CCDAVitalSigns vitals)
	{
		CCDAScoreCardRubrics validateUCUMScore = new CCDAScoreCardRubrics();
		validateUCUMScore.setRule(ApplicationConstants.VITAL_UCUM_REQUIREMENT);
		
		int maxPoints = 0;
		int actualPoints = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue= null;
		if(vitals != null)
		{
			if(!ApplicationUtil.isEmpty(vitals.getVitalsOrg()))
			{
				for(CCDAVitalOrg vitalOrg : vitals.getVitalsOrg())
				{
					if(!ApplicationUtil.isEmpty(vitalOrg.getVitalObs()))
					{
						for(CCDAVitalObs vitalObs : vitalOrg.getVitalObs())
						{
							maxPoints++;
							if(vitalObs.getVsResult() != null && vitalObs.getVsResult().getXsiType().equalsIgnoreCase("PQ"))
							{
								if(vitalObs.getVsCode()!= null)
								{
									if(vitalsRepository.isUCUMCodeValidForVitalCode(vitalObs.getVsCode().getCode(),vitalObs.getVsResult().getUnits()))
									{
										actualPoints++;
									}
									else
									{
										issue = new CCDAXmlSnippet();
										issue.setLineNumber(vitalObs.getVsResult().getLineNumber());
										issue.setXmlString(vitalObs.getVsResult().getXmlString());
										issuesList.add(issue);
									}
								}
								else 
								{
									issue = new CCDAXmlSnippet();
									issue.setLineNumber(vitalObs.getVsResult().getLineNumber());
									issue.setXmlString(vitalObs.getVsResult().getXmlString());
									issuesList.add(issue);
								}
							}
							else if(vitalObs.getVsResult() == null)
							{
								maxPoints++;
								issue = new CCDAXmlSnippet();
								issue.setLineNumber(vitalObs.getLineNumber());
								issue.setXmlString(vitalObs.getXmlString());
								issuesList.add(issue);
							}
						}
					}
				}
			}
			else
			{
				issue = new CCDAXmlSnippet();
				issue.setLineNumber(vitals.getLineNumber());
				issue.setXmlString(vitals.getXmlString());
				issuesList.add(issue);
			}
		}
		else
		{
			issue = new CCDAXmlSnippet();
			issue.setLineNumber("Vitals section not present");
			issue.setXmlString("Vitals section not present");
			issuesList.add(issue);
		}
		
		validateUCUMScore.setActualPoints(actualPoints);
		validateUCUMScore.setMaxPoints(maxPoints);
		validateUCUMScore.setRubricScore(ApplicationUtil.calculateRubricScore(maxPoints, actualPoints));
		validateUCUMScore.setIssuesList(issuesList);
		validateUCUMScore.setNumberOfIssues(issuesList.size());
		if(issuesList.size() > 0)
		{
			validateUCUMScore.setDescription(ApplicationConstants.VITAL_UCUM_DESCRIPTION);
			validateUCUMScore.getIgReferences().add(ApplicationConstants.IG_SECTION_REFERENCES);
			validateUCUMScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_URL);
		}
		return validateUCUMScore;
	}
	
	public CCDAScoreCardRubrics getValidLoincCodesScore(CCDAVitalSigns vitals)
	{
		CCDAScoreCardRubrics validatLoincCodeScore = new CCDAScoreCardRubrics();
		validatLoincCodeScore.setRule(ApplicationConstants.VITAL_LOINC_REQUIREMENT);
		
		int maxPoints = 0;
		int actualPoints = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue= null;
		if(vitals != null)
		{
			if( !ApplicationUtil.isEmpty(vitals.getVitalsOrg()))
			{
				for(CCDAVitalOrg vitalOrg : vitals.getVitalsOrg())
				{
				   if(!ApplicationUtil.isEmpty(vitalOrg.getVitalObs()))
				   {
					   for(CCDAVitalObs vitalObs : vitalOrg.getVitalObs())
					   {
						   maxPoints++;
						   if(vitalObs.getVsCode() != null)
						   {
							   if(ApplicationUtil.validateCodeForValueset(vitalObs.getVsCode().getCode(), ApplicationConstants.HITSP_VITAL_VALUESET_OID))
							   {
								   actualPoints++;
							   }
							   else 
							   {
								   issue = new CCDAXmlSnippet();
								   issue.setLineNumber(vitalObs.getVsCode().getLineNumber());
								   issue.setXmlString(vitalObs.getVsCode().getXmlString());
								   issuesList.add(issue);
							   }
						   }
						   else 
						   {
							   issue = new CCDAXmlSnippet();
							   issue.setLineNumber(vitalObs.getLineNumber());
							   issue.setXmlString(vitalObs.getXmlString());
							   issuesList.add(issue);
						   }
					   }
				   }
				}
			}
			else
			{
				issue = new CCDAXmlSnippet();
				issue.setLineNumber(vitals.getLineNumber());
				issue.setXmlString(vitals.getXmlString());
				issuesList.add(issue);
			}
		}
		else
		{
			issue = new CCDAXmlSnippet();
			issue.setLineNumber("Vitals section not present");
			issue.setXmlString("Vitals section not present");
			issuesList.add(issue);
		}
		
		validatLoincCodeScore.setActualPoints(actualPoints);
		validatLoincCodeScore.setMaxPoints(maxPoints);
		validatLoincCodeScore.setRubricScore(ApplicationUtil.calculateRubricScore(maxPoints, actualPoints));
		validatLoincCodeScore.setIssuesList(issuesList);
		validatLoincCodeScore.setNumberOfIssues(issuesList.size());
		if(issuesList.size() > 0)
		{
			validatLoincCodeScore.setDescription(ApplicationConstants.VITAL_LOINC_DESCRIPTION);
			validatLoincCodeScore.getIgReferences().add(ApplicationConstants.IG_SECTION_REFERENCES);
			validatLoincCodeScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_URL);
		}
		return validatLoincCodeScore;
		
	}
	
	public CCDAScoreCardRubrics getApprEffectivetimeScore(CCDAVitalSigns vitals)
	{
		CCDAScoreCardRubrics validateApprEffectiveTimeScore = new CCDAScoreCardRubrics();
		validateApprEffectiveTimeScore.setRule(ApplicationConstants.VITAL_AAPR_DATE_REQUIREMENT);
		
		int maxPoints = 0;
		int actualPoints = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue= null;
		if(vitals != null)
		{
			if(!ApplicationUtil.isEmpty(vitals.getVitalsOrg()))
			{
				for(CCDAVitalOrg vitalOrg : vitals.getVitalsOrg())
				{
					if(vitalOrg.getEffTime()!= null)
					{
						if(!ApplicationUtil.isEmpty(vitalOrg.getVitalObs()))
						{
							for(CCDAVitalObs vitalObs : vitalOrg.getVitalObs())
							{
								maxPoints++;
								if(vitalObs.getMeasurementTime() != null)
								{
									if(ApplicationUtil.checkDateRange(vitalOrg.getEffTime().getLow(), vitalObs.getMeasurementTime().getValue(), 
																	vitalOrg.getEffTime().getHigh()))
									{
										actualPoints++;
									}
									else 
									{
										issue = new CCDAXmlSnippet();
										issue.setLineNumber(vitalObs.getMeasurementTime().getLineNumber());
										issue.setXmlString(vitalObs.getMeasurementTime().getXmlString());
										issuesList.add(issue);
								    }
								}
								else 
								{
									issue = new CCDAXmlSnippet();
									issue.setLineNumber(vitalObs.getLineNumber());
									issue.setXmlString(vitalObs.getXmlString());
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
				issue.setLineNumber(vitals.getLineNumber());
				issue.setXmlString(vitals.getXmlString());
				issuesList.add(issue);
			}
		}
		else
		{
			issue = new CCDAXmlSnippet();
			issue.setLineNumber("Vitals section not present");
			issue.setXmlString("Vitals section not present");
			issuesList.add(issue);
		}
		
		validateApprEffectiveTimeScore.setActualPoints(actualPoints);
		validateApprEffectiveTimeScore.setMaxPoints(maxPoints);
		validateApprEffectiveTimeScore.setRubricScore(ApplicationUtil.calculateRubricScore(maxPoints, actualPoints));
		validateApprEffectiveTimeScore.setIssuesList(issuesList);
		validateApprEffectiveTimeScore.setNumberOfIssues(issuesList.size());
		if(issuesList.size() > 0)
		{
			validateApprEffectiveTimeScore.setDescription(ApplicationConstants.VITAL_AAPR_DATE_DESCRIPTION);
			validateApprEffectiveTimeScore.getIgReferences().add(ApplicationConstants.IG_SECTION_REFERENCES);
			validateApprEffectiveTimeScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_URL);
		}
		return validateApprEffectiveTimeScore;
	}
	
	public CCDAScoreCardRubrics getNarrativeStructureIdScore(CCDAVitalSigns vitals)
	{
		CCDAScoreCardRubrics narrativeTextIdScore = new CCDAScoreCardRubrics();
		narrativeTextIdScore.setRule(ApplicationConstants.NARRATIVE_STRUCTURE_ID_REQ);
		
		int maxPoints = 0;
		int actualPoints = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue= null;
		if(vitals != null)
		{
			if(!ApplicationUtil.isEmpty(vitals.getVitalsOrg()))
			{
				for(CCDAVitalOrg vitalOrg : vitals.getVitalsOrg())
				{
					if(!ApplicationUtil.isEmpty(vitalOrg.getReferenceTexts()))
					{
						for(CCDADataElement referenceText : vitalOrg.getReferenceTexts())
						{
							maxPoints++;
							if(vitals.getReferenceLinks().contains(referenceText.getValue()))
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
			narrativeTextIdScore.getIgReferences().add(ApplicationConstants.IG_SECTION_REFERENCES);
			narrativeTextIdScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_URL);
		}
		
		return narrativeTextIdScore;
	}

}
