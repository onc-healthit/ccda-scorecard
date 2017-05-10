package org.sitenv.service.ccda.smartscorecard.processor;

import java.util.ArrayList;
import java.util.List;

import org.sitenv.ccdaparsing.model.CCDADataElement;
import org.sitenv.ccdaparsing.model.CCDAII;
import org.sitenv.ccdaparsing.model.CCDASmokingStatus;
import org.sitenv.ccdaparsing.model.CCDASocialHistory;
import org.sitenv.ccdaparsing.model.CCDATobaccoUse;
import org.sitenv.ccdaparsing.model.CCDAXmlSnippet;
import org.sitenv.service.ccda.smartscorecard.model.CCDAScoreCardRubrics;
import org.sitenv.service.ccda.smartscorecard.model.Category;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationConstants;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationUtil;
import org.springframework.stereotype.Service;

@Service
public class SocialHistoryScorecard {
	
	public Category getSocialHistoryCategory(CCDASocialHistory socialHistory, String birthDate,String docType)
	{
		if(socialHistory==null || socialHistory.isSectionNullFlavourWithNI())
		{
			return new Category(ApplicationConstants.CATEGORIES.SOCIALHISTORY.getCategoryDesc(),true);
		}
		Category socialHistoryCategory = new Category();
		socialHistoryCategory.setCategoryName(ApplicationConstants.CATEGORIES.SOCIALHISTORY.getCategoryDesc());
		
		List<CCDAScoreCardRubrics> socialHistoryScoreList = new ArrayList<CCDAScoreCardRubrics>();
		socialHistoryScoreList.add(getTimePrecisionScore(socialHistory,docType));
		socialHistoryScoreList.add(getValidDateTimeScore(socialHistory,birthDate,docType));
		socialHistoryScoreList.add(getValidDisplayNameScoreCard(socialHistory,docType));
		socialHistoryScoreList.add(getValidSmokingStatusScore(socialHistory,docType));
		socialHistoryScoreList.add(getValidSmokingStatuIdScore(socialHistory,docType));
		socialHistoryScoreList.add(getNarrativeStructureIdScore(socialHistory,docType));
		
		socialHistoryCategory.setCategoryRubrics(socialHistoryScoreList);
		ApplicationUtil.calculateSectionGradeAndIssues(socialHistoryScoreList, socialHistoryCategory);
		
		return socialHistoryCategory;
		
	}
	
	public  CCDAScoreCardRubrics getTimePrecisionScore(CCDASocialHistory socialHistory,String docType)
	{
		CCDAScoreCardRubrics timePrecisionScore = new CCDAScoreCardRubrics();
		timePrecisionScore.setRule(ApplicationConstants.TIME_PRECISION_REQUIREMENT);
		
		int actualPoints =0;
		int maxPoints = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue= null;
		if(socialHistory != null)
		{
			if(!ApplicationUtil.isEmpty(socialHistory.getSmokingStatus()))
			{
				for ( CCDASmokingStatus smokingStatus : socialHistory.getSmokingStatus())
				{
					maxPoints++;
					if(smokingStatus.getObservationTime() != null)
					{
						if(ApplicationUtil.validateYearFormat(smokingStatus.getObservationTime()) ||
								ApplicationUtil.validateDayFormat(smokingStatus.getObservationTime()) ||
								ApplicationUtil.validateMinuteFormat(smokingStatus.getObservationTime()) ||
								ApplicationUtil.validateSecondFormat(smokingStatus.getObservationTime()))
						{
							actualPoints++;
						}
						else
						{
							issue = new CCDAXmlSnippet();
							issue.setLineNumber(smokingStatus.getObservationTime().getLineNumber());
							issue.setXmlString(smokingStatus.getObservationTime().getXmlString());
							issuesList.add(issue);
						}
					}
					else
					{
						issue = new CCDAXmlSnippet();
						issue.setLineNumber(smokingStatus.getLineNumber());
						issue.setXmlString(smokingStatus.getXmlString());
						issuesList.add(issue);
					}
				}
			}
			
			if(!ApplicationUtil.isEmpty(socialHistory.getTobaccoUse()))
			{
				for ( CCDATobaccoUse tobaccoUse : socialHistory.getTobaccoUse())
				{
					if(tobaccoUse.getTobaccoUseTime() != null)
					{
						maxPoints++;
						if(ApplicationUtil.validateYearFormat(tobaccoUse.getTobaccoUseTime()) ||
								ApplicationUtil.validateDayFormat(tobaccoUse.getTobaccoUseTime()) ||
								ApplicationUtil.validateMinuteFormat(tobaccoUse.getTobaccoUseTime()) ||
								ApplicationUtil.validateSecondFormat(tobaccoUse.getTobaccoUseTime()))
						{
							actualPoints++;
						}
						else
						{
							issue = new CCDAXmlSnippet();
							issue.setLineNumber(tobaccoUse.getTobaccoUseTime().getLineNumber());
							issue.setXmlString(tobaccoUse.getTobaccoUseTime().getXmlString());
							issuesList.add(issue);
						}
					}
				}
			}
		}
		else
		{
			issue = new CCDAXmlSnippet();
			issue.setLineNumber("Social History section not present");
			issue.setXmlString("Social History section not present");
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
				timePrecisionScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES.SOCIAL_HISTORY_SECTION.getIgReference());
			}else if (docType.equalsIgnoreCase("R1.1"))
			{
				timePrecisionScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES_R1.SOCIAL_HISTORY_SECTION.getIgReference());
			}
			timePrecisionScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_LINKS.SOCIALHISTORY.getTaskforceLink());
		}
		return timePrecisionScore;
	}
	
	public  CCDAScoreCardRubrics getValidDateTimeScore(CCDASocialHistory socialHistory, String birthDate,String docType)
	{
		CCDAScoreCardRubrics validateTimeScore = new CCDAScoreCardRubrics();
		validateTimeScore.setRule(ApplicationConstants.TIME_VALID_REQUIREMENT);
		
		int actualPoints =0;
		int maxPoints = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue= null;
		if(socialHistory != null)
		{
			if(!ApplicationUtil.isEmpty(socialHistory.getSmokingStatus()))
			{
				for ( CCDASmokingStatus smokingStatus : socialHistory.getSmokingStatus())
				{
					if(smokingStatus.getObservationTime() != null && ApplicationUtil.isEffectiveTimePresent(smokingStatus.getObservationTime()))
					{
						maxPoints++;
						if(ApplicationUtil.checkDateRange(birthDate, smokingStatus.getObservationTime()))
						{
							actualPoints++;
						}
						else
						{
							issue = new CCDAXmlSnippet();
							issue.setLineNumber(smokingStatus.getObservationTime().getLineNumber());
							issue.setXmlString(smokingStatus.getObservationTime().getXmlString());
							issuesList.add(issue);
						}
					}
				}
			}
			
			if(!ApplicationUtil.isEmpty(socialHistory.getTobaccoUse()))
			{
				for ( CCDATobaccoUse tobaccoUse : socialHistory.getTobaccoUse())
				{
					if(tobaccoUse.getTobaccoUseTime() != null && ApplicationUtil.isEffectiveTimePresent(tobaccoUse.getTobaccoUseTime()))
					{
						maxPoints++;
						if(ApplicationUtil.checkDateRange(birthDate, tobaccoUse.getTobaccoUseTime()))
						{
							actualPoints++;
						}
						else
						{
							issue = new CCDAXmlSnippet();
							issue.setLineNumber(tobaccoUse.getTobaccoUseTime().getLineNumber());
							issue.setXmlString(tobaccoUse.getTobaccoUseTime().getXmlString());
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
				validateTimeScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES.SOCIAL_HISTORY_SECTION.getIgReference());
			}
			else if(docType.equalsIgnoreCase("R1.1"))
			{
				validateTimeScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES_R1.SOCIAL_HISTORY_SECTION.getIgReference());
			}
			validateTimeScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_LINKS.SOCIALHISTORY.getTaskforceLink());
		}
		return validateTimeScore;
	}
	
	public  CCDAScoreCardRubrics getValidDisplayNameScoreCard(CCDASocialHistory socialHistory,String docType)
	{
		CCDAScoreCardRubrics validateDisplayNameScore = new CCDAScoreCardRubrics();
		validateDisplayNameScore.setRule(ApplicationConstants.CODE_DISPLAYNAME_REQUIREMENT);
		
		int maxPoints = 0;
		int actualPoints = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue= null;
		if(socialHistory != null)
		{
			if(socialHistory.getSectionCode()!= null && !ApplicationUtil.isEmpty(socialHistory.getSectionCode().getDisplayName())
													&& ApplicationUtil.isCodeSystemAvailable(socialHistory.getSectionCode().getCodeSystem()))
			{
				maxPoints++;
				if(ApplicationUtil.validateDisplayName(socialHistory.getSectionCode().getCode(), 
											socialHistory.getSectionCode().getCodeSystem(),
											socialHistory.getSectionCode().getDisplayName()))
				{
					actualPoints++;
				}
				else
				{
					issue = new CCDAXmlSnippet();
					issue.setLineNumber(socialHistory.getSectionCode().getLineNumber());
					issue.setXmlString(socialHistory.getSectionCode().getXmlString());
					issuesList.add(issue);
				}
			}
			
			if(!ApplicationUtil.isEmpty(socialHistory.getSmokingStatus()))
			{
				for (CCDASmokingStatus smokingStatus : socialHistory.getSmokingStatus())
				{
					if(smokingStatus.getSmokingStatusCode() != null && !ApplicationUtil.isEmpty(smokingStatus.getSmokingStatusCode().getDisplayName())
							&& ApplicationUtil.isCodeSystemAvailable(smokingStatus.getSmokingStatusCode().getCodeSystem()))
					{
						maxPoints++;
						if(ApplicationUtil.validateDisplayName(smokingStatus.getSmokingStatusCode().getCode(), 
																smokingStatus.getSmokingStatusCode().getCodeSystem(),
																	smokingStatus.getSmokingStatusCode().getDisplayName()))
						{
							actualPoints++;
						}
						else
						{
							issue = new CCDAXmlSnippet();
							issue.setLineNumber(smokingStatus.getSmokingStatusCode().getLineNumber());
							issue.setXmlString(smokingStatus.getSmokingStatusCode().getXmlString());
							issuesList.add(issue);
						}
					}
				}
			}
			
			if(!ApplicationUtil.isEmpty(socialHistory.getTobaccoUse()))
			{
				for (CCDATobaccoUse tobaccoUse : socialHistory.getTobaccoUse())
				{
					if(tobaccoUse.getTobaccoUseCode() != null && !ApplicationUtil.isEmpty(tobaccoUse.getTobaccoUseCode().getDisplayName())
							&& ApplicationUtil.isCodeSystemAvailable(tobaccoUse.getTobaccoUseCode().getCodeSystem()))
					{
						maxPoints++;
						if(ApplicationUtil.validateDisplayName(tobaccoUse.getTobaccoUseCode().getCode(), 
														tobaccoUse.getTobaccoUseCode().getCodeSystem(),
														tobaccoUse.getTobaccoUseCode().getDisplayName()))
						{
							actualPoints++;
						}
						else
						{
							issue = new CCDAXmlSnippet();
							issue.setLineNumber(tobaccoUse.getTobaccoUseCode().getLineNumber());
							issue.setXmlString(tobaccoUse.getTobaccoUseCode().getXmlString());
							issuesList.add(issue);
						}
					}
				}
			}
		}
		
		if(maxPoints==0)
		{
			actualPoints=1;
			maxPoints=1;
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
				validateDisplayNameScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES.SOCIAL_HISTORY_SECTION.getIgReference());
			}else if (docType.equalsIgnoreCase("R1.1"))
			{
				validateDisplayNameScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES_R1.SOCIAL_HISTORY_SECTION.getIgReference());
			}
			validateDisplayNameScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_LINKS.SOCIALHISTORY.getTaskforceLink());
		}
		return validateDisplayNameScore;
	}
	
	public  CCDAScoreCardRubrics getValidSmokingStatusScore(CCDASocialHistory socialHistory,String docType)
	{
		CCDAScoreCardRubrics validSmokingStausScore = new CCDAScoreCardRubrics();
		validSmokingStausScore.setRule(ApplicationConstants.SOCIALHISTORY_SMOKING_STATUS_REQUIREMENT);
		
		int actualPoints =0;
		int maxPoints = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue= null;
		if(socialHistory != null)
		{
			if(!ApplicationUtil.isEmpty(socialHistory.getSmokingStatus()))
			{
				for (CCDASmokingStatus smokingStatus : socialHistory.getSmokingStatus())
				{
					maxPoints++;
					if(smokingStatus.getSmokingStatusCode()!= null)
					{
						if(ApplicationConstants.SMOKING_STATUS_CODES.contains(smokingStatus.getSmokingStatusCode().getCode()))
						{
							actualPoints++;
						}
						else
						{
							issue = new CCDAXmlSnippet();
							issue.setLineNumber(smokingStatus.getSmokingStatusCode().getLineNumber());
							issue.setXmlString(smokingStatus.getSmokingStatusCode().getXmlString());
							issuesList.add(issue);
						}
					}
					else
					{
						issue = new CCDAXmlSnippet();
						issue.setLineNumber(smokingStatus.getLineNumber());
						issue.setXmlString(smokingStatus.getXmlString());
						issuesList.add(issue);
					}
				}
			}
		}
		else
		{
			issue = new CCDAXmlSnippet();
			issue.setLineNumber("Social History section not present");
			issue.setXmlString("Social History section not present");
			issuesList.add(issue);
		}
		
		validSmokingStausScore.setActualPoints(actualPoints);
		validSmokingStausScore.setMaxPoints(maxPoints);
		validSmokingStausScore.setRubricScore(ApplicationUtil.calculateRubricScore(maxPoints, actualPoints));
		validSmokingStausScore.setIssuesList(issuesList);
		validSmokingStausScore.setNumberOfIssues(issuesList.size());
		if(issuesList.size() > 0)
	    {
			validSmokingStausScore.setDescription("smoking status code  validation Rubric failed for Social History");
			if(docType.equalsIgnoreCase("") || docType.equalsIgnoreCase("R2.1"))
			{
				validSmokingStausScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES.SMOKING_STATUS.getIgReference());
			}else if (docType.equalsIgnoreCase("R1.1"))
			{
				validSmokingStausScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES_R1.SMOKING_STATUS.getIgReference());
			}
			validSmokingStausScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_LINKS.SOCIALHISTORY.getTaskforceLink());
		}
		return validSmokingStausScore;
	}
	
	public static CCDAScoreCardRubrics getValidSmokingStatuIdScore(CCDASocialHistory socialHistory,String docType)
	{
		CCDAScoreCardRubrics validSmokingStausIDScore = new CCDAScoreCardRubrics();
		validSmokingStausIDScore.setRule(ApplicationConstants.SOCIALHISTORY_SMOKING_STATUS_OBS_ID_REQUIREMENT);
		
		int actualPoints =0;
		int maxPoints = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue= null;
		if(socialHistory != null)
		{
			if(!ApplicationUtil.isEmpty(socialHistory.getSmokingStatus()))
			{
				for (CCDASmokingStatus smokingStatus : socialHistory.getSmokingStatus())
				{
					if(!ApplicationUtil.isEmpty(smokingStatus.getSmokingStatusTemplateIds()))
					{
						maxPoints = maxPoints + smokingStatus.getSmokingStatusTemplateIds().size();
						for (CCDAII templateId : smokingStatus.getSmokingStatusTemplateIds())
						{
							if(templateId.getRootValue() != null && templateId.getRootValue().equals(ApplicationConstants.SMOKING_STATUS_OBSERVATION_ID))
							{
								actualPoints++;
							}
							else
							{
								issue = new CCDAXmlSnippet();
								issue.setLineNumber(templateId.getLineNumber());
								issue.setXmlString(templateId.getXmlString());
								issuesList.add(issue);
							}
						}
					}
					else
					{
						maxPoints++;
						issue = new CCDAXmlSnippet();
						issue.setLineNumber(smokingStatus.getLineNumber());
						issue.setXmlString(smokingStatus.getXmlString());
						issuesList.add(issue);
					}
				}
			}
			else
			{
				issue = new CCDAXmlSnippet();
				issue.setLineNumber(socialHistory.getLineNumber());
				issue.setXmlString(socialHistory.getXmlString());
				issuesList.add(issue);
			}
		}
		else
		{
			issue = new CCDAXmlSnippet();
			issue.setLineNumber("Social History section not present");
			issue.setXmlString("Social History section not present");
			issuesList.add(issue);
		}
		
		validSmokingStausIDScore.setActualPoints(actualPoints);
		validSmokingStausIDScore.setMaxPoints(maxPoints);
		validSmokingStausIDScore.setRubricScore(ApplicationUtil.calculateRubricScore(maxPoints, actualPoints));
		validSmokingStausIDScore.setIssuesList(issuesList);
		validSmokingStausIDScore.setNumberOfIssues(issuesList.size());
		if(issuesList.size() > 0)
	    {
			validSmokingStausIDScore.setDescription("smoking status observation validation Rubric failed for Social History");
			if(docType.equalsIgnoreCase("") || docType.equalsIgnoreCase("R2.1"))
			{
				validSmokingStausIDScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES.SMOKING_STATUS.getIgReference());
			}
			else if (docType.equalsIgnoreCase("R1.1"))
			{
				validSmokingStausIDScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES_R1.SMOKING_STATUS.getIgReference());
			}
			validSmokingStausIDScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_LINKS.SOCIALHISTORY.getTaskforceLink());
		}
		return validSmokingStausIDScore;
	}
	
	public CCDAScoreCardRubrics getNarrativeStructureIdScore(CCDASocialHistory socialHistory,String docType)
	{
		CCDAScoreCardRubrics narrativeTextIdScore = new CCDAScoreCardRubrics();
		narrativeTextIdScore.setRule(ApplicationConstants.NARRATIVE_STRUCTURE_ID_REQ);
		
		int maxPoints = 0;
		int actualPoints = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue= null;
		if(socialHistory != null)
		{
			if(!ApplicationUtil.isEmpty(socialHistory.getSmokingStatus()))
			{
				for(CCDASmokingStatus smokingStatus : socialHistory.getSmokingStatus())
				{
					if(!ApplicationUtil.isEmpty(smokingStatus.getReferenceTexts()))
					{
						for(CCDADataElement referenceText : smokingStatus.getReferenceTexts())
						{
							maxPoints++;
							if(socialHistory.getReferenceLinks().contains(referenceText.getValue()))
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
			
			if(!ApplicationUtil.isEmpty(socialHistory.getTobaccoUse()))
			{
				for(CCDATobaccoUse tobaccoUse : socialHistory.getTobaccoUse())
				{
					if(!ApplicationUtil.isEmpty(tobaccoUse.getReferenceTexts()))
					{
						for(CCDADataElement referenceText : tobaccoUse.getReferenceTexts())
						{
							maxPoints++;
							if(socialHistory.getReferenceLinks().contains(referenceText.getValue()))
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
				maxPoints=1;
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
				narrativeTextIdScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES.SOCIAL_HISTORY_SECTION.getIgReference());
			}
			else if (docType.equalsIgnoreCase("R1.1"))
			{
				narrativeTextIdScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES_R1.SOCIAL_HISTORY_SECTION.getIgReference());
			}
			narrativeTextIdScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_LINKS.SOCIALHISTORY.getTaskforceLink());
		}
		
		return narrativeTextIdScore;
	}

}
