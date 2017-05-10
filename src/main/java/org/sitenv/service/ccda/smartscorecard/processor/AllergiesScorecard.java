package org.sitenv.service.ccda.smartscorecard.processor;

import java.util.ArrayList;
import java.util.List;

import org.sitenv.ccdaparsing.model.CCDAAllergy;
import org.sitenv.ccdaparsing.model.CCDAAllergyConcern;
import org.sitenv.ccdaparsing.model.CCDAAllergyObs;
import org.sitenv.ccdaparsing.model.CCDADataElement;
import org.sitenv.ccdaparsing.model.CCDAXmlSnippet;
import org.sitenv.service.ccda.smartscorecard.model.CCDAScoreCardRubrics;
import org.sitenv.service.ccda.smartscorecard.model.Category;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationConstants;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationUtil;
import org.springframework.stereotype.Service;

@Service
public class AllergiesScorecard {
	
	public Category getAllergiesCategory(CCDAAllergy allergies, String birthDate,String docType)
	{
		if(allergies== null || allergies.isSectionNullFlavourWithNI())
		{
			return new Category(ApplicationConstants.CATEGORIES.ALLERGIES.getCategoryDesc(),true);
		}
		Category allergyCategory = new Category();
		allergyCategory.setCategoryName(ApplicationConstants.CATEGORIES.ALLERGIES.getCategoryDesc());
		
		List<CCDAScoreCardRubrics> allergyScoreList = new ArrayList<CCDAScoreCardRubrics>();
		allergyScoreList.add(getTimePrecisionScore(allergies,docType));
		allergyScoreList.add(getValidDateTimeScore(allergies, birthDate,docType));
		allergyScoreList.add(getValidDisplayNameScoreCard(allergies,docType));
		allergyScoreList.add(getApprEffectivetimeScore(allergies,docType));
		allergyScoreList.add(getNarrativeStructureIdScore(allergies,docType));
		
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
						if(ApplicationUtil.validateDayFormat(allergyConcern.getEffTime())||
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
								if(ApplicationUtil.validateDayFormat(allergyObservation.getEffTime()) || 
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
	
	public CCDAScoreCardRubrics getValidDateTimeScore(CCDAAllergy allergies, String birthDate,String docType)
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
						if(ApplicationUtil.checkDateRange(birthDate, allergyConcern.getEffTime()))
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
								if(ApplicationUtil.checkDateRange(birthDate, allergyObservation.getEffTime()))
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
					if(!ApplicationUtil.isEmpty(allergyAct.getReferenceTexts()))
					{
						for(CCDADataElement referenceText : allergyAct.getReferenceTexts())
						{
							maxPoints++;
							if(allergies.getReferenceLinks().contains(referenceText.getValue()))
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
		
}
