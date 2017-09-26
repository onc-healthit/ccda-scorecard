package org.sitenv.service.ccda.smartscorecard.processor;

import java.util.ArrayList;
import java.util.List;

import org.sitenv.ccdaparsing.model.CCDAAllergy;
import org.sitenv.ccdaparsing.model.CCDAAllergyConcern;
import org.sitenv.ccdaparsing.model.CCDAAllergyObs;
import org.sitenv.ccdaparsing.model.CCDAAllergyReaction;
import org.sitenv.ccdaparsing.model.CCDAII;
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
public class AllergiesScorecard {
	
	@Autowired
	TemplateIdProcessor templateIdProcessor;
	
	public Category getAllergiesCategory(CCDAAllergy allergies, PatientDetails patientDetails,String docType, List<SectionRule> sectionRules)
	{
		if(allergies== null || allergies.isSectionNullFlavourWithNI())
		{
			return new Category(ApplicationConstants.CATEGORIES.ALLERGIES.getCategoryDesc(),true);
		}
		Category allergyCategory = new Category();
		allergyCategory.setCategoryName(ApplicationConstants.CATEGORIES.ALLERGIES.getCategoryDesc());
		
		List<CCDAScoreCardRubrics> allergyScoreList = new ArrayList<CCDAScoreCardRubrics>();
		
		if (sectionRules==null || ApplicationUtil.isRuleEnabled(sectionRules, ApplicationConstants.TIME_PRECISION_REQUIREMENT)) {
			allergyScoreList.add(getTimePrecisionScore(allergies, docType));
		}
		if (sectionRules==null || ApplicationUtil.isRuleEnabled(sectionRules, ApplicationConstants.TIME_VALID_REQUIREMENT)) {
			allergyScoreList.add(getValidDateTimeScore(allergies, patientDetails, docType));
		}
		if (sectionRules==null || ApplicationUtil.isRuleEnabled(sectionRules, ApplicationConstants.CODE_DISPLAYNAME_REQUIREMENT)) {
			allergyScoreList.add(getValidDisplayNameScoreCard(allergies, docType));
		}
		// allergyScoreList.add(getApprEffectivetimeScore(allergies,docType));
		if (sectionRules==null || ApplicationUtil.isRuleEnabled(sectionRules, ApplicationConstants.ALLERGIES_APR_TIME_REQ)) {
			allergyScoreList.add(getApprStatusCodeScore(allergies, docType));
		}
		if (sectionRules==null || ApplicationUtil.isRuleEnabled(sectionRules, ApplicationConstants.NARRATIVE_STRUCTURE_ID_REQ)) {
			allergyScoreList.add(getNarrativeStructureIdScore(allergies, docType));
		}
		if (sectionRules==null || ApplicationUtil.isRuleEnabled(sectionRules, ApplicationConstants.TEMPLATEID_DESC)) {
			allergyScoreList.add(getTemplateIdScore(allergies, docType));
		}

		allergyCategory.setCategoryRubrics(allergyScoreList);
		ApplicationUtil.calculateSectionGradeAndIssues(allergyScoreList,allergyCategory);
		
		return allergyCategory;
		
	}
	
	public CCDAScoreCardRubrics getTimePrecisionScore(CCDAAllergy allergies, String docType)
	{
		CCDAScoreCardRubrics timePrecisionScore = new CCDAScoreCardRubrics();
		timePrecisionScore.setRule(ApplicationConstants.TIME_PRECISION_REQUIREMENT);
		
		int maxPoints = 0;
		int actualPoints = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue= null;
		if(allergies != null)
		{
			if(!ApplicationUtil.isEmpty(allergies.getAllergyConcern()))
			{
				for (CCDAAllergyConcern allergyConcern : allergies.getAllergyConcern())
				{
					maxPoints++;
					if(allergyConcern.getEffTime() != null)
					{
						if(ApplicationUtil.validateYearFormat(allergyConcern.getEffTime())||
								ApplicationUtil.validateMonthFormat(allergyConcern.getEffTime())||
								ApplicationUtil.validateDayFormat(allergyConcern.getEffTime())||
									ApplicationUtil.validateMinuteFormat(allergyConcern.getEffTime()) ||
									ApplicationUtil.validateSecondFormat(allergyConcern.getEffTime()))
						{
							actualPoints++;
						}
						else 
						{
							issue = new CCDAXmlSnippet();
							issue.setLineNumber(allergyConcern.getEffTime().getLineNumber());
							issue.setXmlString(allergyConcern.getEffTime().getXmlString());
							issuesList.add(issue);
						}
					}
					else 
					{
						issue = new CCDAXmlSnippet();
						issue.setLineNumber(allergyConcern.getLineNumber());
						issue.setXmlString(allergyConcern.getXmlString());
						issuesList.add(issue);
					}
						
					if(!ApplicationUtil.isEmpty(allergyConcern.getAllergyObs()))
					{
						for (CCDAAllergyObs allergyObservation : allergyConcern.getAllergyObs() )
						{
							maxPoints++;
							if(allergyObservation.getEffTime() != null)
							{
								if(ApplicationUtil.validateYearFormat(allergyObservation.getEffTime()) ||
									ApplicationUtil.validateMonthFormat(allergyObservation.getEffTime()) ||
										ApplicationUtil.validateDayFormat(allergyObservation.getEffTime()) || 
											ApplicationUtil.validateMinuteFormat(allergyObservation.getEffTime()) ||
											ApplicationUtil.validateSecondFormat(allergyObservation.getEffTime()))
								{
									actualPoints++;
								}
								else 
								{
									issue = new CCDAXmlSnippet();
									issue.setLineNumber(allergyObservation.getEffTime().getLineNumber());
									issue.setXmlString(allergyObservation.getEffTime().getXmlString());
									issuesList.add(issue);
								}
							}
							else
							{
								issue = new CCDAXmlSnippet();
								issue.setLineNumber(allergyObservation.getLineNumber());
								issue.setXmlString(allergyObservation.getXmlString());
								issuesList.add(issue);
							}
						}
					}
				}
			}
			else
			{
				issue = new CCDAXmlSnippet();
				issue.setLineNumber(allergies.getLineNumber());
				issue.setXmlString(allergies.getXmlString());
				issuesList.add(issue);
			}
		}
		else
		{
			issue = new CCDAXmlSnippet();
			issue.setLineNumber("Allergies Section not present");
			issue.setXmlString("Allergies section not present");
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
			   timePrecisionScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES.ALLERGY_CONCERN.getIgReference());
		   }
		   if(docType.equalsIgnoreCase("") || docType.equalsIgnoreCase("R1.1"))
		   {
			   timePrecisionScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES_R1.ALLERGY_CONCERN.getIgReference());
		   }
		   timePrecisionScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_LINKS.ALLERGIES.getTaskforceLink());
	   }
	   return timePrecisionScore;
	}
	
	public CCDAScoreCardRubrics getValidDateTimeScore(CCDAAllergy allergies, PatientDetails patientDetails,String docType)
	{
		CCDAScoreCardRubrics validateTimeScore = new CCDAScoreCardRubrics();
		validateTimeScore.setRule(ApplicationConstants.TIME_VALID_REQUIREMENT);
		
		int maxPoints = 0;
		int actualPoints = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue= null;
		if(allergies != null)
		{
			if(!ApplicationUtil.isEmpty(allergies.getAllergyConcern()))
			{
				for (CCDAAllergyConcern allergyConcern : allergies.getAllergyConcern())
				{
					if(allergyConcern.getEffTime() != null && ApplicationUtil.isEffectiveTimePresent(allergyConcern.getEffTime()))
					{
						maxPoints++;
						if(ApplicationUtil.checkDateRange(patientDetails, allergyConcern.getEffTime()))
						{
							actualPoints++;
						}
						else 
						{
							issue = new CCDAXmlSnippet();
							issue.setLineNumber(allergyConcern.getEffTime().getLineNumber());
							issue.setXmlString(allergyConcern.getEffTime().getXmlString());
							issuesList.add(issue);
						}
					}
						
					if(!ApplicationUtil.isEmpty(allergyConcern.getAllergyObs()))
					{
						for (CCDAAllergyObs allergyObservation : allergyConcern.getAllergyObs() )
						{
							if(allergyObservation.getEffTime() != null && ApplicationUtil.isEffectiveTimePresent(allergyObservation.getEffTime()))
							{
								maxPoints++;
								if(ApplicationUtil.checkDateRange(patientDetails, allergyObservation.getEffTime()))
								{
									actualPoints++;
								}
								else 
								{
									issue = new CCDAXmlSnippet();
									issue.setLineNumber(allergyObservation.getEffTime().getLineNumber());
									issue.setXmlString(allergyObservation.getEffTime().getXmlString());
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
		if(issuesList.size() > 0)
		{
			validateTimeScore.setDescription(ApplicationConstants.TIME_VALID_DESCRIPTION);
			if(docType.equalsIgnoreCase("") || docType.equalsIgnoreCase("R2.1"))
			{
				validateTimeScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES.ALLERGY_CONCERN.getIgReference());
			}
			else if (docType.equalsIgnoreCase("R1.1"))
			{
				validateTimeScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES_R1.ALLERGY_CONCERN.getIgReference());
			}
			validateTimeScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_LINKS.ALLERGIES.getTaskforceLink());
		}
		return validateTimeScore;
	}
	
	
	public CCDAScoreCardRubrics getValidDisplayNameScoreCard(CCDAAllergy allergies,String docType)
	{
		CCDAScoreCardRubrics validateDisplayNameScore = new CCDAScoreCardRubrics();
		validateDisplayNameScore.setRule(ApplicationConstants.CODE_DISPLAYNAME_REQUIREMENT);
		
		int maxPoints = 0;
		int actualPoints = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue= null;
		if(allergies != null)
		{
			if(allergies.getSectionCode() != null && !ApplicationUtil.isEmpty(allergies.getSectionCode().getDisplayName()) 
																			&& ApplicationUtil.isCodeSystemAvailable(allergies.getSectionCode().getCodeSystem()))
			{
				maxPoints++;
				if(ApplicationUtil.validateDisplayName(allergies.getSectionCode().getCode(), allergies.getSectionCode().getCodeSystem(),
														allergies.getSectionCode().getDisplayName()))
				{
					actualPoints++;
				}
				else 
				{
					issue = new CCDAXmlSnippet();
					issue.setLineNumber(allergies.getSectionCode().getLineNumber());
					issue.setXmlString(allergies.getSectionCode().getXmlString());
					issuesList.add(issue);
				}
			}
			
			if(!ApplicationUtil.isEmpty(allergies.getAllergyConcern()))
			{
				for (CCDAAllergyConcern allergyConcern : allergies.getAllergyConcern())
				{
					if(!ApplicationUtil.isEmpty(allergyConcern.getAllergyObs()))
					{
						for (CCDAAllergyObs allergyObs : allergyConcern.getAllergyObs())
						{
							if(allergyObs.getAllergyIntoleranceType() != null && !ApplicationUtil.isEmpty(allergyObs.getAllergyIntoleranceType().getDisplayName())
																				&& ApplicationUtil.isCodeSystemAvailable(allergyObs.getAllergyIntoleranceType().getCodeSystem()))
							{
								maxPoints++;
								if(ApplicationUtil.validateDisplayName(allergyObs.getAllergyIntoleranceType().getCode(), 
																allergyObs.getAllergyIntoleranceType().getCodeSystem(),
																allergyObs.getAllergyIntoleranceType().getDisplayName()))
								{
									actualPoints++;
								}
								else 
								{
									issue = new CCDAXmlSnippet();
									issue.setLineNumber(allergyObs.getAllergyIntoleranceType().getLineNumber());
									issue.setXmlString(allergyObs.getAllergyIntoleranceType().getXmlString());
									issuesList.add(issue);
								}
							}
							
							if(allergyObs.getAllergySubstance() != null && !ApplicationUtil.isEmpty(allergyObs.getAllergySubstance().getDisplayName())
																			&& ApplicationUtil.isCodeSystemAvailable(allergyObs.getAllergySubstance().getCodeSystem()))
							{
								maxPoints++;
								if(ApplicationUtil.validateDisplayName(allergyObs.getAllergySubstance().getCode(), 
																allergyObs.getAllergySubstance().getCodeSystem(),
																allergyObs.getAllergySubstance().getDisplayName()))
								{
									actualPoints++;
								}
								else 
								{
									issue = new CCDAXmlSnippet();
									issue.setLineNumber(allergyObs.getAllergySubstance().getLineNumber());
									issue.setXmlString(allergyObs.getAllergySubstance().getXmlString());
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
				validateDisplayNameScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES.ALLERGY_SECTION.getIgReference());
			}
			else if(docType.equalsIgnoreCase("R1.1"))
			{
				validateDisplayNameScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES_R1.ALLERGY_SECTION.getIgReference());
			}
			validateDisplayNameScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_LINKS.ALLERGIES.getTaskforceLink());
		}
		return validateDisplayNameScore;
	}
	
	public CCDAScoreCardRubrics getApprEffectivetimeScore(CCDAAllergy allergies,String docType)
	{
		CCDAScoreCardRubrics validateApprEffectiveTimeScore = new CCDAScoreCardRubrics();
		validateApprEffectiveTimeScore.setRule(ApplicationConstants.ALLERGIES_CONCERN_DATE_ALIGN_REQ);
		
		int maxPoints = 0;
		int actualPoints = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue= null;
		if(allergies != null)
		{
			if(!ApplicationUtil.isEmpty(allergies.getAllergyConcern()))
			{
				for(CCDAAllergyConcern allergyAct : allergies.getAllergyConcern())
				{
					if(allergyAct.getEffTime()!= null && ApplicationUtil.isEffectiveTimePresent(allergyAct.getEffTime()))
					{
						if(!ApplicationUtil.isEmpty(allergyAct.getAllergyObs()))
						{
							for(CCDAAllergyObs allergyObs : allergyAct.getAllergyObs())
							{
								if(allergyObs.getEffTime()!=null && ApplicationUtil.isEffectiveTimePresent(allergyObs.getEffTime()))
								{
									maxPoints++;
									if(ApplicationUtil.checkDateRange(allergyAct.getEffTime().getLow(),allergyAct.getEffTime().getHigh(),
																		allergyObs.getEffTime().getLow(),allergyObs.getEffTime().getHigh()))
									{
										actualPoints++;
									}
									else
									{
										issue = new CCDAXmlSnippet();
										issue.setLineNumber(allergyObs.getEffTime().getLineNumber());
										issue.setXmlString(allergyObs.getEffTime().getXmlString());
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
			validateApprEffectiveTimeScore.setDescription(ApplicationConstants.ALLERGIES_CONCERN_DATE_ALIGN_DESC);
			if(docType.equalsIgnoreCase("") || docType.equalsIgnoreCase("R2.1"))
			{
				validateApprEffectiveTimeScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES.ALLERGY_CONCERN.getIgReference());
			}
			else if(docType.equalsIgnoreCase("R1.1"))
			{
				validateApprEffectiveTimeScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES_R1.ALLERGY_CONCERN.getIgReference());
			}
			validateApprEffectiveTimeScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_LINKS.ALLERGIES.getTaskforceLink());
		}
		return validateApprEffectiveTimeScore;
	}
	
	public CCDAScoreCardRubrics getNarrativeStructureIdScore(CCDAAllergy allergies,String docType)
	{
		CCDAScoreCardRubrics narrativeTextIdScore = new CCDAScoreCardRubrics();
		narrativeTextIdScore.setRule(ApplicationConstants.NARRATIVE_STRUCTURE_ID_REQ);
		
		int maxPoints = 0;
		int actualPoints = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue= null;
		if(allergies != null)
		{
			if(!ApplicationUtil.isEmpty(allergies.getAllergyConcern()))
			{
				for(CCDAAllergyConcern allergyAct : allergies.getAllergyConcern())
				{
					maxPoints++;
					if(allergyAct.getReferenceText()!= null)
					{
						if(allergies.getReferenceLinks()!= null && allergies.getReferenceLinks().contains(allergyAct.getReferenceText().getValue()))
						{
							actualPoints++;
						}
						else
						{
							issue = new CCDAXmlSnippet();
							issue.setLineNumber(allergyAct.getReferenceText().getLineNumber());
							issue.setXmlString(allergyAct.getReferenceText().getXmlString());
							issuesList.add(issue);
						}
					}
					else
					{
						issue = new CCDAXmlSnippet();
						issue.setLineNumber(allergyAct.getLineNumber());
						issue.setXmlString(allergyAct.getXmlString());
						issuesList.add(issue);
					}
					
					if(allergyAct.getAllergyObs()!= null)
					{
						for(CCDAAllergyObs allObs : allergyAct.getAllergyObs())
						{
							if(allObs.getReactions()!= null)
							{
								for(CCDAAllergyReaction allReaction : allObs.getReactions())
								{
									maxPoints++;
									if(allReaction.getReferenceText()!= null)
									{
										if(allergies.getReferenceLinks()!= null && allergies.getReferenceLinks().contains(allReaction.getReferenceText().getValue()))
										{
											actualPoints++;
										}
										else
										{
											issue = new CCDAXmlSnippet();
											issue.setLineNumber(allReaction.getReferenceText().getLineNumber());
											issue.setXmlString(allReaction.getReferenceText().getXmlString());
											issuesList.add(issue);
										}
									}
									else{
										issue = new CCDAXmlSnippet();
										issue.setLineNumber(allReaction.getLineNumber());
										issue.setXmlString(allReaction.getXmlString());
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
				narrativeTextIdScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES.ALLERGY_SECTION.getIgReference());
			}
			else if (docType.equalsIgnoreCase("R1.1"))
			{
				narrativeTextIdScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES_R1.ALLERGY_SECTION.getIgReference());
			}
			narrativeTextIdScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_LINKS.ALLERGIES.getTaskforceLink());
		}
		
		return narrativeTextIdScore;
	}
	
	public CCDAScoreCardRubrics getApprStatusCodeScore(CCDAAllergy allergies,String docType)
	{
		CCDAScoreCardRubrics validateApprEffectiveTimeScore = new CCDAScoreCardRubrics();
		validateApprEffectiveTimeScore.setRule(ApplicationConstants.ALLERGIES_APR_TIME_REQ);
		
		int maxPoints = 0;
		int actualPoints = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue= null;
		if(allergies != null)
		{
			if(!ApplicationUtil.isEmpty(allergies.getAllergyConcern()))
			{
				for(CCDAAllergyConcern allergyConcern : allergies.getAllergyConcern())
				{
					if(allergyConcern.getStatusCode() != null )
					{
						maxPoints++;
						if(allergyConcern.getAllergyObs()!=null)
						{
							for(CCDAAllergyObs allergyObs : allergyConcern.getAllergyObs())
							{	
								if(ApplicationUtil.validateStatusCode(allergyObs.getEffTime(), allergyConcern.getStatusCode().getCode()))
								{
									actualPoints++;
								}
								else
								{
									issue = new CCDAXmlSnippet();
									issue.setLineNumber(allergyObs.getEffTime()!=null ? allergyObs.getEffTime().getLineNumber():allergyObs.getLineNumber());
									issue.setXmlString(allergyObs.getEffTime()!=null ? allergyObs.getEffTime().getXmlString():allergyObs.getXmlString());
									issuesList.add(issue);
								}
							}
						}
						else
						{
							issue = new CCDAXmlSnippet();
							issue.setLineNumber(allergyConcern.getLineNumber());
							issue.setXmlString(allergyConcern.getXmlString());
							issuesList.add(issue);
						}
					}
				}
			}
			else
			{
				issue = new CCDAXmlSnippet();
				issue.setLineNumber(allergies.getLineNumber());
				issue.setXmlString(allergies.getXmlString());
				issuesList.add(issue);
			}
		}
		else
		{
			issue = new CCDAXmlSnippet();
			issue.setLineNumber("Allergies section not present");
			issue.setXmlString("Allergies section not present");
			issuesList.add(issue);
		}
		
		
		validateApprEffectiveTimeScore.setActualPoints(actualPoints);
		validateApprEffectiveTimeScore.setMaxPoints(maxPoints);
		validateApprEffectiveTimeScore.setRubricScore(ApplicationUtil.calculateRubricScore(maxPoints, actualPoints));
		validateApprEffectiveTimeScore.setIssuesList(issuesList);
		validateApprEffectiveTimeScore.setNumberOfIssues(issuesList.size());
		if(issuesList.size() > 0)
		{
			validateApprEffectiveTimeScore.setDescription(ApplicationConstants.ALLERGIES_APR_TIME_DESC);
			if(docType.equalsIgnoreCase("") || docType.equalsIgnoreCase("R2.1"))
			{
				validateApprEffectiveTimeScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES.ALLERGY_SECTION.getIgReference());
			}else if (docType.equalsIgnoreCase("R1.1"))
			{
				validateApprEffectiveTimeScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES_R1.ALLERGY_SECTION.getIgReference());
			}
			validateApprEffectiveTimeScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_LINKS.ALLERGIES.getTaskforceLink());
		}
		return validateApprEffectiveTimeScore;
	}
	
	public CCDAScoreCardRubrics getTemplateIdScore(CCDAAllergy allergies,String docType)
	{
		CCDAScoreCardRubrics templateIdScore = new CCDAScoreCardRubrics();
		templateIdScore.setRule(ApplicationConstants.TEMPLATEID_DESC);
		
		int maxPoints = 0;
		int actualPoints = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		if(allergies!=null)
		{
			if(!ApplicationUtil.isEmpty(allergies.getSectionTemplateId()))
			{
				for (CCDAII templateId : allergies.getSectionTemplateId())
				{
					maxPoints = maxPoints++;
					templateIdProcessor.scoreTemplateId(templateId,actualPoints,issuesList,docType);
				}
			}
			if(!ApplicationUtil.isEmpty(allergies.getAllergyConcern()))
			{
				for(CCDAAllergyConcern allergyConcern : allergies.getAllergyConcern())
				{
					if(!ApplicationUtil.isEmpty(allergyConcern.getTemplateId()))
					{
						for (CCDAII templateId : allergyConcern.getTemplateId())
						{
							maxPoints = maxPoints++;
							templateIdProcessor.scoreTemplateId(templateId,actualPoints,issuesList,docType);
						}
					}
					
					if(!ApplicationUtil.isEmpty(allergyConcern.getAllergyObs()))
					{
						for (CCDAAllergyObs allergyObs : allergyConcern.getAllergyObs())
						{
							if(!ApplicationUtil.isEmpty(allergyObs.getTemplateId()))
							{
								for (CCDAII templateId : allergyObs.getTemplateId())
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
