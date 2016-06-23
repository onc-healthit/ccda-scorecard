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
	
	public Category getSocialHistoryCategory(CCDASocialHistory socialHistory, String birthDate)
	{
		
		Category socialHistoryCategory = new Category();
		socialHistoryCategory.setCategoryName(ApplicationConstants.CATEGORIES.SOCIALHISTORY.getCategoryDesc());
		
		List<CCDAScoreCardRubrics> socialHistoryScoreList = new ArrayList<CCDAScoreCardRubrics>();
		socialHistoryScoreList.add(getTimePrecisionScore(socialHistory));
		socialHistoryScoreList.add(getValidDateTimeScore(socialHistory,birthDate));
		socialHistoryScoreList.add(getValidDisplayNameScoreCard(socialHistory));
		socialHistoryScoreList.add(getValidSmokingStatusScore(socialHistory));
		socialHistoryScoreList.add(getValidSmokingStatuIdScore(socialHistory));
		socialHistoryScoreList.add(getNarrativeStructureIdScore(socialHistory));
		
		socialHistoryCategory.setCategoryRubrics(socialHistoryScoreList);
		ApplicationUtil.calculateSectionGradeAndIssues(socialHistoryScoreList, socialHistoryCategory);
		
		return socialHistoryCategory;
		
	}
	
	public  CCDAScoreCardRubrics getTimePrecisionScore(CCDASocialHistory socialHistory)
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
						if(ApplicationUtil.validateDayFormat(smokingStatus.getObservationTime().getValue()))
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
						if(tobaccoUse.getTobaccoUseTime().getLow() != null)
						{
							maxPoints++;
							if(ApplicationUtil.validateDayFormat(tobaccoUse.getTobaccoUseTime().getLow().getValue()))
							{
								actualPoints++;
							}
							else
							{
								issue = new CCDAXmlSnippet();
								issue.setLineNumber(tobaccoUse.getTobaccoUseTime().getLow().getLineNumber());
								issue.setXmlString(tobaccoUse.getTobaccoUseTime().getLow().getXmlString());
								issuesList.add(issue);
							}
						}
						if(tobaccoUse.getTobaccoUseTime().getHigh() != null)
						{
							maxPoints++;
							if(ApplicationUtil.validateDayFormat(tobaccoUse.getTobaccoUseTime().getHigh().getValue()))
							{
								actualPoints++;
							}
							else
							{
								issue = new CCDAXmlSnippet();
								issue.setLineNumber(tobaccoUse.getTobaccoUseTime().getHigh().getLineNumber());
								issue.setXmlString(tobaccoUse.getTobaccoUseTime().getHigh().getXmlString());
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
			timePrecisionScore.getIgReferences().add(ApplicationConstants.IG_SECTION_REFERENCES);
			timePrecisionScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_URL);
		}
		return timePrecisionScore;
	}
	
	public  CCDAScoreCardRubrics getValidDateTimeScore(CCDASocialHistory socialHistory, String birthDate)
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
					maxPoints++;
					if(smokingStatus.getObservationTime() != null)
					{
						if(ApplicationUtil.checkDateRange(birthDate, smokingStatus.getObservationTime().getValue()))
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
						if(tobaccoUse.getTobaccoUseTime().getLow() != null)
						{
							maxPoints++;
							if(ApplicationUtil.checkDateRange(birthDate, tobaccoUse.getTobaccoUseTime().getLow().getValue()))
							{
								actualPoints++;
							}
							else
							{
								issue = new CCDAXmlSnippet();
								issue.setLineNumber(tobaccoUse.getTobaccoUseTime().getLow().getLineNumber());
								issue.setXmlString(tobaccoUse.getTobaccoUseTime().getLow().getXmlString());
								issuesList.add(issue);
							}
						}
						if(tobaccoUse.getTobaccoUseTime().getHigh() != null)
						{
							maxPoints++;
							if(ApplicationUtil.checkDateRange(birthDate, tobaccoUse.getTobaccoUseTime().getHigh().getValue()))
							{
								actualPoints++;
							}
							else
							{
								issue = new CCDAXmlSnippet();
								issue.setLineNumber(tobaccoUse.getTobaccoUseTime().getHigh().getLineNumber());
								issue.setXmlString(tobaccoUse.getTobaccoUseTime().getHigh().getXmlString());
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
			issue.setLineNumber("Social History section not present");
			issue.setXmlString("Social History section not present");
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
	
	public  CCDAScoreCardRubrics getValidDisplayNameScoreCard(CCDASocialHistory socialHistory)
	{
		CCDAScoreCardRubrics validateDisplayNameScore = new CCDAScoreCardRubrics();
		validateDisplayNameScore.setRule(ApplicationConstants.CODE_DISPLAYNAME_REQUIREMENT);
		
		int maxPoints = 0;
		int actualPoints = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue= null;
		if(socialHistory != null)
		{
			maxPoints++;
			if(socialHistory.getSectionCode()!= null)
			{
				if(ApplicationUtil.validateDisplayName(socialHistory.getSectionCode().getCode(), 
						ApplicationConstants.CODE_SYSTEM_MAP.get(socialHistory.getSectionCode().getCodeSystem()),
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
			else
			{
				issue = new CCDAXmlSnippet();
				issue.setLineNumber(socialHistory.getLineNumber());
				issue.setXmlString(socialHistory.getXmlString());
				issuesList.add(issue);
			}
			
			if(!ApplicationUtil.isEmpty(socialHistory.getSmokingStatus()))
			{
				for (CCDASmokingStatus smokingStatus : socialHistory.getSmokingStatus())
				{
					maxPoints++;
					if(smokingStatus.getSmokingStatusCode() != null)
					{
						if(ApplicationUtil.validateDisplayName(smokingStatus.getSmokingStatusCode().getCode(), 
								ApplicationConstants.CODE_SYSTEM_MAP.get(smokingStatus.getSmokingStatusCode().getCodeSystem()),
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
				for (CCDATobaccoUse tobaccoUse : socialHistory.getTobaccoUse())
				{
					maxPoints++;
					if(tobaccoUse.getTobaccoUseCode() != null)
					{
						if(ApplicationUtil.validateDisplayName(tobaccoUse.getTobaccoUseCode().getCode(), 
								ApplicationConstants.CODE_SYSTEM_MAP.get(tobaccoUse.getTobaccoUseCode().getCodeSystem()),
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
					else
					{
						issue = new CCDAXmlSnippet();
						issue.setLineNumber(tobaccoUse.getLineNumber());
						issue.setXmlString(tobaccoUse.getXmlString());
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
	
	public  CCDAScoreCardRubrics getValidSmokingStatusScore(CCDASocialHistory socialHistory)
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
			validSmokingStausScore.getIgReferences().add(ApplicationConstants.IG_SECTION_REFERENCES);
			validSmokingStausScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_URL);
		}
		return validSmokingStausScore;
	}
	
	public static CCDAScoreCardRubrics getValidSmokingStatuIdScore(CCDASocialHistory socialHistory)
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
								issue.setLineNumber(smokingStatus.getLineNumber());
								issue.setXmlString(smokingStatus.getXmlString());
								issuesList.add(issue);
							}
						}
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
			validSmokingStausIDScore.getIgReferences().add(ApplicationConstants.IG_SECTION_REFERENCES);
			validSmokingStausIDScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_URL);
		}
		return validSmokingStausIDScore;
	}
	
	public CCDAScoreCardRubrics getNarrativeStructureIdScore(CCDASocialHistory socialHistory)
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
