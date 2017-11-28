package org.sitenv.service.ccda.smartscorecard.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.sitenv.ccdaparsing.model.CCDAII;
import org.sitenv.ccdaparsing.model.CCDASmokingStatus;
import org.sitenv.ccdaparsing.model.CCDASocialHistory;
import org.sitenv.ccdaparsing.model.CCDATobaccoUse;
import org.sitenv.ccdaparsing.model.CCDAXmlSnippet;
import org.sitenv.service.ccda.smartscorecard.cofiguration.SectionRule;
import org.sitenv.service.ccda.smartscorecard.model.CCDAScoreCardRubrics;
import org.sitenv.service.ccda.smartscorecard.model.Category;
import org.sitenv.service.ccda.smartscorecard.model.PatientDetails;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationConstants;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

@Service
public class SocialHistoryScorecard {
	
	private static final Logger logger = Logger.getLogger(SocialHistoryScorecard.class);
	
	@Autowired
	TemplateIdProcessor templateIdProcessor;
	@Async()
	public Future<Category> getSocialHistoryCategory(CCDASocialHistory socialHistory, PatientDetails patientDetails,String docType,List<SectionRule> sectionRules)
	{
		long startTime = System.currentTimeMillis();
		logger.info("SocailHistory Start time:"+ startTime);
		if(socialHistory==null || socialHistory.isSectionNullFlavourWithNI())
		{
			return new AsyncResult<Category>(new Category(ApplicationConstants.CATEGORIES.SOCIALHISTORY.getCategoryDesc(),true));
		}
		Category socialHistoryCategory = new Category();
		socialHistoryCategory.setCategoryName(ApplicationConstants.CATEGORIES.SOCIALHISTORY.getCategoryDesc());
		
		List<CCDAScoreCardRubrics> socialHistoryScoreList = new ArrayList<CCDAScoreCardRubrics>();
		
		if (sectionRules==null || ApplicationUtil.isRuleEnabled(sectionRules, ApplicationConstants.RULE_IDS.S1)) {
			socialHistoryScoreList.add(getTimePrecisionScore(socialHistory, docType));
		}
		if (sectionRules==null || ApplicationUtil.isRuleEnabled(sectionRules, ApplicationConstants.RULE_IDS.S2)) {
			socialHistoryScoreList.add(getValidDateTimeScore(socialHistory, patientDetails, docType));
		}
		if (sectionRules==null || ApplicationUtil.isRuleEnabled(sectionRules, ApplicationConstants.RULE_IDS.S3)) {
			socialHistoryScoreList.add(getValidDisplayNameScoreCard(socialHistory, docType));
		}
		if (sectionRules==null || ApplicationUtil.isRuleEnabled(sectionRules, ApplicationConstants.RULE_IDS.S4)) {
			socialHistoryScoreList.add(getValidSmokingStatusScore(socialHistory, docType));
		}
		if (sectionRules==null || ApplicationUtil.isRuleEnabled(sectionRules, ApplicationConstants.RULE_IDS.S5)) {
			socialHistoryScoreList.add(getValidSmokingStatuIdScore(socialHistory, docType));
		}
		if (sectionRules==null || ApplicationUtil.isRuleEnabled(sectionRules, ApplicationConstants.RULE_IDS.S6)) {
			socialHistoryScoreList.add(getValidGenderObsScore(socialHistory, docType));
		}
		if (sectionRules==null || ApplicationUtil.isRuleEnabled(sectionRules, ApplicationConstants.RULE_IDS.S7)) {
			socialHistoryScoreList.add(getNarrativeStructureIdScore(socialHistory, docType));
		}
		if (sectionRules==null || ApplicationUtil.isRuleEnabled(sectionRules, ApplicationConstants.RULE_IDS.S8)) {
			socialHistoryScoreList.add(getTemplateIdScore(socialHistory, docType));
		}
		
		socialHistoryCategory.setCategoryRubrics(socialHistoryScoreList);
		ApplicationUtil.calculateSectionGradeAndIssues(socialHistoryScoreList, socialHistoryCategory);
		logger.info("SocailHistory End time:"+ (System.currentTimeMillis() - startTime));
		return new AsyncResult<Category>(socialHistoryCategory);
		
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
								ApplicationUtil.validateMonthFormat(smokingStatus.getObservationTime()) ||
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
								ApplicationUtil.validateMonthFormat(tobaccoUse.getTobaccoUseTime()) ||
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
	
	public  CCDAScoreCardRubrics getValidDateTimeScore(CCDASocialHistory socialHistory, PatientDetails patientDetails,String docType)
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
						if(ApplicationUtil.checkDateRange(patientDetails, smokingStatus.getObservationTime()))
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
						if(ApplicationUtil.checkDateRange(patientDetails, tobaccoUse.getTobaccoUseTime()))
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
	
	public static CCDAScoreCardRubrics getValidGenderObsScore(CCDASocialHistory socialHistory,String docType)
	{
		CCDAScoreCardRubrics validGenderObsIDScore = new CCDAScoreCardRubrics();
		validGenderObsIDScore.setRule(ApplicationConstants.SOCIALHISTORY_GENDER_OBS_REQUIREMENT);
		
		int actualPoints =0;
		int maxPoints = 1;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue= null;
		if(socialHistory != null)
		{
			if(socialHistory.getSocialHistoryGenderObs()!=null)
			{
				if(socialHistory.getSocialHistoryGenderObs().getGenderValue()!=null)
				{
					if(socialHistory.getSocialHistoryGenderObs().getGenderValue().getCode()!= null)
					{
						actualPoints++;
					}
					else
					{
						issue = new CCDAXmlSnippet();
						issue.setLineNumber(socialHistory.getSocialHistoryGenderObs().getGenderValue().getLineNumber());
						issue.setXmlString(socialHistory.getSocialHistoryGenderObs().getGenderValue().getXmlString());
						issuesList.add(issue);
					}
				}
				else
				{
					issue = new CCDAXmlSnippet();
					issue.setLineNumber(socialHistory.getSocialHistoryGenderObs().getLineNumber());
					issue.setXmlString(socialHistory.getSocialHistoryGenderObs().getXmlString());
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
		}
		else
		{
			issue = new CCDAXmlSnippet();
			issue.setLineNumber("Social History section not present");
			issue.setXmlString("Social History section not present");
			issuesList.add(issue);
		}
		
		validGenderObsIDScore.setActualPoints(actualPoints);
		validGenderObsIDScore.setMaxPoints(maxPoints);
		validGenderObsIDScore.setRubricScore(ApplicationUtil.calculateRubricScore(maxPoints, actualPoints));
		validGenderObsIDScore.setIssuesList(issuesList);
		validGenderObsIDScore.setNumberOfIssues(issuesList.size());
		if(issuesList.size() > 0)
	    {
			validGenderObsIDScore.setDescription(ApplicationConstants.SOCIALHISTORY_GENDER_OBS_DESC);
			if(docType.equalsIgnoreCase("") || docType.equalsIgnoreCase("R2.1"))
			{
				validGenderObsIDScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES.SOCIAL_HISTORY_OBSERVATION.getIgReference());
			}
			else if (docType.equalsIgnoreCase("R1.1"))
			{
				validGenderObsIDScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES_R1.SOCIAL_HISTORY_OBSERVATION.getIgReference());
			}
			validGenderObsIDScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_LINKS.SOCIALHISTORY.getTaskforceLink());
		}
		return validGenderObsIDScore;
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
					maxPoints++;
					if(smokingStatus.getReferenceText()!=null)
					{
						if(socialHistory.getReferenceLinks()!= null && socialHistory.getReferenceLinks().contains(smokingStatus.getReferenceText().getValue()))
						{
							actualPoints++;
						}
						else
						{
							issue = new CCDAXmlSnippet();
							issue.setLineNumber(smokingStatus.getReferenceText().getLineNumber());
							issue.setXmlString(smokingStatus.getReferenceText().getXmlString());
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
				for(CCDATobaccoUse tobaccoUse : socialHistory.getTobaccoUse())
				{
					maxPoints++;
					if(tobaccoUse.getReferenceText()!=null)
					{
						if(socialHistory.getReferenceLinks()!= null && socialHistory.getReferenceLinks().contains(tobaccoUse.getReferenceText().getValue()))
						{
							actualPoints++;
						}
						else
						{
							issue = new CCDAXmlSnippet();
							issue.setLineNumber(tobaccoUse.getReferenceText().getLineNumber());
							issue.setXmlString(tobaccoUse.getReferenceText().getXmlString());
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
			
			if(socialHistory.getSocialHistoryGenderObs()!=null)
			{
				maxPoints++;
				if(socialHistory.getSocialHistoryGenderObs().getReferenceText()!=null)
				{
					if(socialHistory.getReferenceLinks()!= null && socialHistory.getReferenceLinks().contains(socialHistory.getSocialHistoryGenderObs().getReferenceText().getValue()))
					{
						actualPoints++;
					}
					else
					{
						issue = new CCDAXmlSnippet();
						issue.setLineNumber(socialHistory.getSocialHistoryGenderObs().getReferenceText().getLineNumber());
						issue.setXmlString(socialHistory.getSocialHistoryGenderObs().getReferenceText().getXmlString());
						issuesList.add(issue);
					}
				}
				else
				{
					issue = new CCDAXmlSnippet();
					issue.setLineNumber(socialHistory.getSocialHistoryGenderObs().getLineNumber());
					issue.setXmlString(socialHistory.getSocialHistoryGenderObs().getXmlString());
					issuesList.add(issue);
				}
			}
		}
		
		if(maxPoints ==0)
		{
			maxPoints=1;
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
	
	public CCDAScoreCardRubrics getTemplateIdScore(CCDASocialHistory socialHistory,String docType)
	{
		CCDAScoreCardRubrics templateIdScore = new CCDAScoreCardRubrics();
		templateIdScore.setRule(ApplicationConstants.TEMPLATEID_DESC);
		
		int maxPoints = 0;
		int actualPoints = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		
		if(socialHistory!=null)
		{
			if(!ApplicationUtil.isEmpty(socialHistory.getSectionTemplateIds()))
			{
				for (CCDAII templateId : socialHistory.getSectionTemplateIds())
				{
					maxPoints = maxPoints++;
					templateIdProcessor.scoreTemplateId(templateId,actualPoints,issuesList,docType);
				}
			}
			
			if(!ApplicationUtil.isEmpty(socialHistory.getSmokingStatus()))
			{
				for(CCDASmokingStatus smokingStatus : socialHistory.getSmokingStatus())
				{
					if(!ApplicationUtil.isEmpty(smokingStatus.getSmokingStatusTemplateIds()))
					{
						for (CCDAII templateId : smokingStatus.getSmokingStatusTemplateIds())
						{
							maxPoints = maxPoints++;
							templateIdProcessor.scoreTemplateId(templateId,actualPoints,issuesList,docType);
						}
					}
				}
			}
			
			if(!ApplicationUtil.isEmpty(socialHistory.getTobaccoUse()))
			{
				for(CCDATobaccoUse tobaccoUse : socialHistory.getTobaccoUse())
				{
					if(!ApplicationUtil.isEmpty(tobaccoUse.getTobaccoUseTemplateIds()))
					{
						for (CCDAII templateId : tobaccoUse.getTobaccoUseTemplateIds())
						{
							maxPoints = maxPoints++;
							templateIdProcessor.scoreTemplateId(templateId,actualPoints,issuesList,docType);
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
