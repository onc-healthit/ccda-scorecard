package org.sitenv.service.ccda.smartscorecard.processor;

import java.util.ArrayList;
import java.util.List;

import org.sitenv.ccdaparsing.model.CCDACode;
import org.sitenv.ccdaparsing.model.CCDAEncounter;
import org.sitenv.ccdaparsing.model.CCDAEncounterActivity;
import org.sitenv.ccdaparsing.model.CCDAEncounterDiagnosis;
import org.sitenv.ccdaparsing.model.CCDAII;
import org.sitenv.ccdaparsing.model.CCDAProblemObs;
import org.sitenv.ccdaparsing.model.CCDAServiceDeliveryLoc;
import org.sitenv.ccdaparsing.model.CCDAXmlSnippet;
import org.sitenv.service.ccda.smartscorecard.cofiguration.SectionRule;
import org.sitenv.service.ccda.smartscorecard.model.CCDAScoreCardRubrics;
import org.sitenv.service.ccda.smartscorecard.model.Category;
import org.sitenv.service.ccda.smartscorecard.model.PatientDetails;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationConstants;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EncounterScorecard {
	
	@Autowired
	TemplateIdProcessor templateIdProcessor;
	
	public Category getEncounterCategory(CCDAEncounter encounter, PatientDetails patientDetails,String docType,List<SectionRule> sectionRules)
	{
		if(encounter== null || encounter.isSectionNullFlavourWithNI())
		{
			return new Category(ApplicationConstants.CATEGORIES.ENCOUNTERS.getCategoryDesc(),true);
		}
		Category encounterCategory = new Category();
		encounterCategory.setCategoryName(ApplicationConstants.CATEGORIES.ENCOUNTERS.getCategoryDesc());
		
		List<CCDAScoreCardRubrics> encounterScoreList = new ArrayList<CCDAScoreCardRubrics>();
		
		if (sectionRules==null || ApplicationUtil.isRuleEnabled(sectionRules, ApplicationConstants.TIME_PRECISION_REQUIREMENT)) {
			encounterScoreList.add(getTimePrecisionScore(encounter, docType));
		}
		if (sectionRules==null || ApplicationUtil.isRuleEnabled(sectionRules, ApplicationConstants.TIME_VALID_REQUIREMENT)) {
			encounterScoreList.add(getValidDateTimeScore(encounter, patientDetails, docType));
		}
		if (sectionRules==null || ApplicationUtil.isRuleEnabled(sectionRules, ApplicationConstants.CODE_DISPLAYNAME_REQUIREMENT)) {
			encounterScoreList.add(getValidDisplayNameScoreCard(encounter, docType));
		}
		if (sectionRules==null || ApplicationUtil.isRuleEnabled(sectionRules, ApplicationConstants.NARRATIVE_STRUCTURE_ID_REQ)) {
			encounterScoreList.add(getNarrativeStructureIdScore(encounter, docType));
		}
		if (sectionRules==null || ApplicationUtil.isRuleEnabled(sectionRules, ApplicationConstants.TEMPLATEID_DESC)) {
			encounterScoreList.add(getTemplateIdScore(encounter, docType));
		}
		
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
					if(encounterActivity.getEffectiveTime() != null)
					{
						maxPoints++;
						if(ApplicationUtil.validateMinuteFormat(encounterActivity.getEffectiveTime()) ||
								ApplicationUtil.validateSecondFormat(encounterActivity.getEffectiveTime()))
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
	
	public CCDAScoreCardRubrics getValidDateTimeScore(CCDAEncounter encounter, PatientDetails patientDetails, String docType)
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
					if(encounterActivity.getEffectiveTime() != null && ApplicationUtil.isEffectiveTimePresent(encounterActivity.getEffectiveTime()))
					{
						maxPoints++;
						if(ApplicationUtil.checkDateRange(patientDetails, encounterActivity.getEffectiveTime()))
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
			if(encounters.getSectionCode()!= null && !ApplicationUtil.isEmpty(encounters.getSectionCode().getDisplayName())
														&& ApplicationUtil.isCodeSystemAvailable(encounters.getSectionCode().getCodeSystem()))
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
					
					if(encounterActivity.getEncounterTypeCode()!= null && !ApplicationUtil.isEmpty(encounterActivity.getEncounterTypeCode().getDisplayName())
															&& ApplicationUtil.isCodeSystemAvailable(encounterActivity.getEncounterTypeCode().getCodeSystem()))
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
							if(indication.getProblemType()!= null && !ApplicationUtil.isEmpty(indication.getProblemType().getDisplayName())
																  && ApplicationUtil.isCodeSystemAvailable(indication.getProblemType().getCodeSystem()))
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
							if(indication.getProblemCode()!= null && !ApplicationUtil.isEmpty(indication.getProblemCode().getDisplayName())
																	&& ApplicationUtil.isCodeSystemAvailable(indication.getProblemCode().getCodeSystem()))
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
							if(diagnosis.getEntryCode()!= null && !ApplicationUtil.isEmpty(diagnosis.getEntryCode().getDisplayName())
																&& ApplicationUtil.isCodeSystemAvailable(diagnosis.getEntryCode().getCodeSystem()))
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
					maxPoints++;
					if(encAct.getReferenceText()!= null)
					{
						if(encounters.getReferenceLinks()!= null && encounters.getReferenceLinks().contains(encAct.getReferenceText().getValue()))
						{
							actualPoints++;
						}
						else
						{
							issue = new CCDAXmlSnippet();
							issue.setLineNumber(encAct.getReferenceText().getLineNumber());
							issue.setXmlString(encAct.getReferenceText().getXmlString());
							issuesList.add(issue);
						}
					}
					else
					{
						issue = new CCDAXmlSnippet();
						issue.setLineNumber(encAct.getLineNumber());
						issue.setXmlString(encAct.getXmlString());
						issuesList.add(issue);
					}
					if(encAct.getIndications()!=null)
					{
						for(CCDAProblemObs indications : encAct.getIndications())
						{
							maxPoints++;
							if(indications.getReferenceText()!=null)
							{
								if(encounters.getReferenceLinks()!= null && encounters.getReferenceLinks().contains(indications.getReferenceText().getValue()))
								{
									actualPoints++;
								}
								else
								{
									issue = new CCDAXmlSnippet();
									issue.setLineNumber(indications.getReferenceText().getLineNumber());
									issue.setXmlString(indications.getReferenceText().getXmlString());
									issuesList.add(issue);
								}
							}
							else
							{
								issue = new CCDAXmlSnippet();
								issue.setLineNumber(indications.getLineNumber());
								issue.setXmlString(indications.getXmlString());
								issuesList.add(issue);
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
	
	
	public CCDAScoreCardRubrics getTemplateIdScore(CCDAEncounter encounters,String docType)
	{
		CCDAScoreCardRubrics templateIdScore = new CCDAScoreCardRubrics();
		templateIdScore.setRule(ApplicationConstants.TEMPLATEID_DESC);
		
		int maxPoints = 0;
		int actualPoints = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		
		if(encounters!=null)
		{
			if(!ApplicationUtil.isEmpty(encounters.getTemplateId()))
			{
				for (CCDAII templateId : encounters.getTemplateId())
				{
					maxPoints = maxPoints++;
					templateIdProcessor.scoreTemplateId(templateId,actualPoints,issuesList,docType);
				}
			}
			
			if(!ApplicationUtil.isEmpty(encounters.getEncActivities()))
			{
				for(CCDAEncounterActivity encAct : encounters.getEncActivities())
				{
					if(!ApplicationUtil.isEmpty(encAct.getTemplateId()))
					{
						for (CCDAII templateId : encAct.getTemplateId())
						{
							maxPoints = maxPoints++;
							templateIdProcessor.scoreTemplateId(templateId,actualPoints,issuesList,docType);
						}
					}
					
					if(!ApplicationUtil.isEmpty(encAct.getIndications()))
					{
						for(CCDAProblemObs probObs :  encAct.getIndications())
						{
							if(!ApplicationUtil.isEmpty(probObs.getTemplateId()))
							{
								for (CCDAII templateId : probObs.getTemplateId())
								{
									maxPoints = maxPoints++;
									templateIdProcessor.scoreTemplateId(templateId,actualPoints,issuesList,docType);
								}
							}
						}
					}
					
					if(!ApplicationUtil.isEmpty(encAct.getDiagnoses()))
					{
						for(CCDAEncounterDiagnosis encDiagnosis :  encAct.getDiagnoses())
						{
							if(!ApplicationUtil.isEmpty(encDiagnosis.getTemplateId()))
							{
								for (CCDAII templateId : encDiagnosis.getTemplateId())
								{
									maxPoints = maxPoints++;
									templateIdProcessor.scoreTemplateId(templateId,actualPoints,issuesList,docType);
								}
							}
						}
					}
					
					if(!ApplicationUtil.isEmpty(encAct.getSdLocs()))
					{
						for(CCDAServiceDeliveryLoc sdlLoc :  encAct.getSdLocs())
						{
							if(!ApplicationUtil.isEmpty(sdlLoc.getTemplateId()))
							{
								for (CCDAII templateId : sdlLoc.getTemplateId())
								{
									maxPoints = maxPoints++;
									templateIdProcessor.scoreTemplateId(templateId,actualPoints,issuesList,docType);
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
