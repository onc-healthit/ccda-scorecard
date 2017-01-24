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
	
	public Category getEncounterCategory(CCDAEncounter encounter, String birthDate,String docType)
	{
		
		if(encounter.isSectionNullFlavourWithNI())
		{
			return new Category(ApplicationConstants.CATEGORIES.ENCOUNTERS.getCategoryDesc(),true);
		}
		Category encounterCategory = new Category();
		encounterCategory.setCategoryName(ApplicationConstants.CATEGORIES.ENCOUNTERS.getCategoryDesc());
		
		List<CCDAScoreCardRubrics> encounterScoreList = new ArrayList<CCDAScoreCardRubrics>();
		encounterScoreList.add(getTimePrecisionScore(encounter,docType));
		encounterScoreList.add(getValidDateTimeScore(encounter,birthDate,docType));
		encounterScoreList.add(getValidDisplayNameScoreCard(encounter,docType));
		encounterScoreList.add(getNarrativeStructureIdScore(encounter,docType));
		
		encounterCategory.setCategoryRubrics(encounterScoreList);
		ApplicationUtil.calculateSectionGradeAndIssues(encounterScoreList, encounterCategory);
		
		return encounterCategory;
		
	}
	
	public  CCDAScoreCardRubrics getTimePrecisionScore(CCDAEncounter encounter,String docType)
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
						if(ApplicationUtil.validateMinuteFormat(encounterActivity.getEffectiveTime().getValue()) ||
								ApplicationUtil.validateSecondFormat(encounterActivity.getEffectiveTime().getValue()))
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
											if(ApplicationUtil.validateMinuteFormat(problemObs.getEffTime().getLow().getValue()) ||
													ApplicationUtil.validateSecondFormat(problemObs.getEffTime().getLow().getValue()))
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
											if(ApplicationUtil.validateMinuteFormat(problemObs.getEffTime().getHigh().getValue()) ||
													ApplicationUtil.validateSecondFormat(problemObs.getEffTime().getHigh().getValue()))
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
			if(docType.equalsIgnoreCase("") || docType.equalsIgnoreCase("R2.1"))
			{
				timePrecisionScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES.ENCOUNTER_ACTIVITY.getIgReference());
			}
			else if(docType.equalsIgnoreCase("R1.1"))
			{
				timePrecisionScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES_R1.ENCOUNTER_ACTIVITY.getIgReference());
			}
			timePrecisionScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_LINKS.ENCOUNTERS.getTaskforceLink());
		}
		return timePrecisionScore;
	}
	
	public CCDAScoreCardRubrics getValidDateTimeScore(CCDAEncounter encounter, String birthDate, String docType)
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
			if(docType.equalsIgnoreCase("") || docType.equalsIgnoreCase("R2.1"))
			{
				validateTimeScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES.ENCOUNTER_ACTIVITY.getIgReference());
			}
			else if(docType.equalsIgnoreCase("R1.1"))
			{
				validateTimeScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES_R1.ENCOUNTER_ACTIVITY.getIgReference());
			}
			validateTimeScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_LINKS.ENCOUNTERS.getTaskforceLink());
		}
		return validateTimeScore;
	}
	
	public CCDAScoreCardRubrics getValidDisplayNameScoreCard(CCDAEncounter encounters,String docType)
	{
		CCDAScoreCardRubrics validateDisplayNameScore = new CCDAScoreCardRubrics();
		validateDisplayNameScore.setRule(ApplicationConstants.CODE_DISPLAYNAME_REQUIREMENT);
		
		int maxPoints = 0;
		int actualPoints = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue= null;
		if(encounters != null)
		{
			if(encounters.getSectionCode()!= null && !ApplicationUtil.isEmpty(encounters.getSectionCode().getDisplayName()))
			{
				maxPoints++;
				if(ApplicationUtil.validateDisplayName(encounters.getSectionCode().getCode(), 
											encounters.getSectionCode().getCodeSystem(),
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
			
			if(!ApplicationUtil.isEmpty(encounters.getEncActivities()))
			{
				for (CCDAEncounterActivity encounterActivity : encounters.getEncActivities())
				{
					
					if(encounterActivity.getEncounterTypeCode()!= null && !ApplicationUtil.isEmpty(encounterActivity.getEncounterTypeCode().getDisplayName()))
					{
						maxPoints++;
						if(ApplicationUtil.validateDisplayName(encounterActivity.getEncounterTypeCode().getCode(), 
																encounterActivity.getEncounterTypeCode().getCodeSystem(),
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
					if(!ApplicationUtil.isEmpty(encounterActivity.getIndications()))
					{
						for (CCDAProblemObs indication : encounterActivity.getIndications())
						{
							if(indication.getProblemType()!= null && !ApplicationUtil.isEmpty(indication.getProblemType().getDisplayName()))
							{
								maxPoints++;
								if(ApplicationUtil.validateDisplayName(indication.getProblemType().getCode(), 
																		indication.getProblemType().getCodeSystem(),
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
							if(indication.getProblemCode()!= null && !ApplicationUtil.isEmpty(indication.getProblemCode().getDisplayName()))
							{
								maxPoints++;
								if(ApplicationUtil.validateDisplayName(indication.getProblemCode().getCode(), 
																		indication.getProblemCode().getCodeSystem(),
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
						}
					}
					
					if(!ApplicationUtil.isEmpty(encounterActivity.getDiagnoses()))
					{
						for (CCDAEncounterDiagnosis diagnosis : encounterActivity.getDiagnoses())
						{
							if(diagnosis.getEntryCode()!= null && !ApplicationUtil.isEmpty(diagnosis.getEntryCode().getDisplayName()))
							{
								maxPoints++;
								if(ApplicationUtil.validateDisplayName(diagnosis.getEntryCode().getCode(), 
																diagnosis.getEntryCode().getCodeSystem(),
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
							if(!ApplicationUtil.isEmpty(diagnosis.getProblemObs()))
							{
								for (CCDAProblemObs probObs : diagnosis.getProblemObs())
								{
									if(probObs.getProblemType()!= null && !ApplicationUtil.isEmpty(probObs.getProblemType().getDisplayName()))
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
									if(probObs.getProblemCode()!= null && !ApplicationUtil.isEmpty(probObs.getProblemCode().getDisplayName()))
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
											if(!ApplicationUtil.isEmpty(translationCode.getDisplayName()))
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
			}
		}
		if(maxPoints==0)
		{
			maxPoints = 1;
			actualPoints = 1 ;
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
				validateDisplayNameScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES.ENCOUNTER_SECTION.getIgReference());
			}
			else if(docType.equalsIgnoreCase("R1.1"))
			{
				validateDisplayNameScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES_R1.ENCOUNTER_SECTION.getIgReference());
			}
			validateDisplayNameScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_LINKS.ENCOUNTERS.getTaskforceLink());
		}
		return validateDisplayNameScore;
	}
	
	
	public CCDAScoreCardRubrics getNarrativeStructureIdScore(CCDAEncounter encounters,String docType)
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
				narrativeTextIdScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES.ENCOUNTER_SECTION.getIgReference());
			}else if(docType.equalsIgnoreCase("R1.1"))
			{
				narrativeTextIdScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES_R1.ENCOUNTER_SECTION.getIgReference());
			}
			narrativeTextIdScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_LINKS.ENCOUNTERS.getTaskforceLink());
		}
		
		return narrativeTextIdScore;
	}
	
	
}
