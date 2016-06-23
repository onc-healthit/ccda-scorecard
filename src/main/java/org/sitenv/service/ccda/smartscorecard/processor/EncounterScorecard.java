package org.sitenv.service.ccda.smartscorecard.processor;

import java.util.ArrayList;
import java.util.List;

import org.sitenv.ccdaparsing.model.CCDACode;
import org.sitenv.ccdaparsing.model.CCDADataElement;
import org.sitenv.ccdaparsing.model.CCDAEncounter;
import org.sitenv.ccdaparsing.model.CCDAEncounterActivity;
import org.sitenv.ccdaparsing.model.CCDAEncounterDiagnosis;
import org.sitenv.ccdaparsing.model.CCDAProblemObs;
import org.sitenv.ccdaparsing.model.CCDAXmlSnippet;
import org.sitenv.service.ccda.smartscorecard.model.CCDAScoreCardRubrics;
import org.sitenv.service.ccda.smartscorecard.model.Category;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationConstants;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationUtil;
import org.springframework.stereotype.Service;

@Service
public class EncounterScorecard {
	
	public Category getEncounterCategory(CCDAEncounter encounter, String birthDate)
	{
		
		Category encounterCategory = new Category();
		encounterCategory.setCategoryName(ApplicationConstants.CATEGORIES.ENCOUNTERS.getCategoryDesc());
		
		List<CCDAScoreCardRubrics> encounterScoreList = new ArrayList<CCDAScoreCardRubrics>();
		encounterScoreList.add(getTimePrecisionScore(encounter));
		encounterScoreList.add(getValidDateTimeScore(encounter,birthDate));
		encounterScoreList.add(getValidDisplayNameScoreCard(encounter));
		encounterScoreList.add(getNarrativeStructureIdScore(encounter));
		
		encounterCategory.setCategoryRubrics(encounterScoreList);
		ApplicationUtil.calculateSectionGradeAndIssues(encounterScoreList, encounterCategory);
		
		return encounterCategory;
		
	}
	
	public  CCDAScoreCardRubrics getTimePrecisionScore(CCDAEncounter encounter)
	{
		CCDAScoreCardRubrics timePrecisionScore = new CCDAScoreCardRubrics();
		timePrecisionScore.setRule(ApplicationConstants.TIME_PRECISION_REQUIREMENT);
		
		int maxPoints = 0;
		int actualPoints = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue= null;
		if(encounter != null)
		{
			if(!ApplicationUtil.isEmpty(encounter.getEncActivities()))
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
						else
						{
							issue = new CCDAXmlSnippet();
							issue.setLineNumber(encounterActivity.getEffectiveTime().getLineNumber());
							issue.setXmlString(encounterActivity.getEffectiveTime().getXmlString());
							issuesList.add(issue);
						}
					}
					else
					{
						issue = new CCDAXmlSnippet();
						issue.setLineNumber(encounterActivity.getLineNumber());
						issue.setXmlString(encounterActivity.getXmlString());
						issuesList.add(issue);
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
											if(ApplicationUtil.validateMinuteFormat(problemObs.getEffTime().getLow().getValue()))
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
											if(ApplicationUtil.validateMinuteFormat(problemObs.getEffTime().getHigh().getValue()))
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
				}
			}
			else
			{
				issue = new CCDAXmlSnippet();
				issue.setLineNumber(encounter.getLineNumber());
				issue.setXmlString(encounter.getXmlString());
				issuesList.add(issue);
			}
		}
		else
		{
			issue = new CCDAXmlSnippet();
			issue.setLineNumber("Encounter section not present");
			issue.setXmlString("Encounter Section not present");
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
	
	public CCDAScoreCardRubrics getValidDateTimeScore(CCDAEncounter encounter, String birthDate)
	{
		CCDAScoreCardRubrics validateTimeScore = new CCDAScoreCardRubrics();
		validateTimeScore.setRule(ApplicationConstants.TIME_VALID_REQUIREMENT);
		
		int maxPoints = 0;
		int actualPoints = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue= null;
		if(encounter != null)
		{
			if(!ApplicationUtil.isEmpty(encounter.getEncActivities()))
			{
				for (CCDAEncounterActivity encounterActivity : encounter.getEncActivities())
				{
					maxPoints++;
					if(encounterActivity.getEffectiveTime() != null)
					{
						if(ApplicationUtil.checkDateRange(birthDate, encounterActivity.getEffectiveTime().getValue()))
						{
							actualPoints++;
						}
						else
						{
							issue = new CCDAXmlSnippet();
							issue.setLineNumber(encounterActivity.getEffectiveTime().getLineNumber());
							issue.setXmlString(encounterActivity.getEffectiveTime().getXmlString());
							issuesList.add(issue);
						}
					}
					else
					{
						issue = new CCDAXmlSnippet();
						issue.setLineNumber(encounterActivity.getLineNumber());
						issue.setXmlString(encounterActivity.getXmlString());
						issuesList.add(issue);
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
				}
			}
			else
			{
				issue = new CCDAXmlSnippet();
				issue.setLineNumber(encounter.getLineNumber());
				issue.setXmlString(encounter.getXmlString());
				issuesList.add(issue);
			}
		}
		else
		{
			issue = new CCDAXmlSnippet();
			issue.setLineNumber("Encounter section not present");
			issue.setXmlString("Encounter Section not present");
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
	
	public CCDAScoreCardRubrics getValidDisplayNameScoreCard(CCDAEncounter encounters)
	{
		CCDAScoreCardRubrics validateDisplayNameScore = new CCDAScoreCardRubrics();
		validateDisplayNameScore.setRule(ApplicationConstants.CODE_DISPLAYNAME_REQUIREMENT);
		
		int maxPoints = 0;
		int actualPoints = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue= null;
		if(encounters != null)
		{
			maxPoints++;
			if(encounters.getSectionCode()!= null)
			{
				if(ApplicationUtil.validateDisplayName(encounters.getSectionCode().getCode(), 
												ApplicationConstants.CODE_SYSTEM_MAP.get(encounters.getSectionCode().getCodeSystem()),
											encounters.getSectionCode().getDisplayName()))
				{
					actualPoints++;
				}
				else
				{
					issue = new CCDAXmlSnippet();
					issue.setLineNumber(encounters.getSectionCode().getLineNumber());
					issue.setXmlString(encounters.getSectionCode().getXmlString());
					issuesList.add(issue);
				}
			}
			else
			{
				issue = new CCDAXmlSnippet();
				issue.setLineNumber(encounters.getLineNumber());
				issue.setXmlString(encounters.getXmlString());
				issuesList.add(issue);
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
						else
						{
							issue = new CCDAXmlSnippet();
							issue.setLineNumber(encounterActivity.getEncounterTypeCode().getLineNumber());
							issue.setXmlString(encounterActivity.getEncounterTypeCode().getXmlString());
							issuesList.add(issue);
						}
					}
					else 
					{
						issue = new CCDAXmlSnippet();
						issue.setLineNumber(encounterActivity.getLineNumber());
						issue.setXmlString(encounterActivity.getXmlString());
						issuesList.add(issue);
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
								else 
								{
									issue = new CCDAXmlSnippet();
									issue.setLineNumber(indication.getProblemType().getLineNumber());
									issue.setXmlString(indication.getProblemType().getXmlString());
									issuesList.add(issue);
								}
							}
							else 
							{
								issue = new CCDAXmlSnippet();
								issue.setLineNumber(indication.getLineNumber());
								issue.setXmlString(indication.getXmlString());
								issuesList.add(issue);
							}
							
							if(indication.getProblemCode()!= null)
							{
								if(ApplicationUtil.validateDisplayName(indication.getProblemCode().getCode(), 
										ApplicationConstants.CODE_SYSTEM_MAP.get(indication.getProblemCode().getCodeSystem()),
																			indication.getProblemCode().getDisplayName()))
								{
									actualPoints++;
								}
								else 
								{
									issue = new CCDAXmlSnippet();
									issue.setLineNumber(indication.getProblemCode().getLineNumber());
									issue.setXmlString(indication.getProblemCode().getXmlString());
									issuesList.add(issue);
								}
							}
							else 
							{
								issue = new CCDAXmlSnippet();
								issue.setLineNumber(indication.getLineNumber());
								issue.setXmlString(indication.getXmlString());
								issuesList.add(issue);
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
								else 
								{
									issue = new CCDAXmlSnippet();
									issue.setLineNumber(diagnosis.getEntryCode().getLineNumber());
									issue.setXmlString(diagnosis.getEntryCode().getXmlString());
									issuesList.add(issue);
								}
							}
							else 
							{
								issue = new CCDAXmlSnippet();
								issue.setLineNumber(diagnosis.getLineNumber());
								issue.setXmlString(diagnosis.getXmlString());
								issuesList.add(issue);
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
			}
		}
		else
		{
			issue = new CCDAXmlSnippet();
			issue.setLineNumber("Encounter section not present");
			issue.setXmlString("Encounter section not present");
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
	
	
	public CCDAScoreCardRubrics getNarrativeStructureIdScore(CCDAEncounter encounters)
	{
		CCDAScoreCardRubrics narrativeTextIdScore = new CCDAScoreCardRubrics();
		narrativeTextIdScore.setRule(ApplicationConstants.NARRATIVE_STRUCTURE_ID_REQ);
		
		int maxPoints = 0;
		int actualPoints = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue= null;
		if(encounters != null)
		{
			if(!ApplicationUtil.isEmpty(encounters.getEncActivities()))
			{
				for(CCDAEncounterActivity encAct : encounters.getEncActivities())
				{
					if(!ApplicationUtil.isEmpty(encAct.getReferenceTexts()))
					{
						for(CCDADataElement referenceText : encAct.getReferenceTexts())
						{
							maxPoints++;
							if(encounters.getReferenceLinks().contains(referenceText.getValue()))
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
