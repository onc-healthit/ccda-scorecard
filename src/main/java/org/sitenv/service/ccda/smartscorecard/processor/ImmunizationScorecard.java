package org.sitenv.service.ccda.smartscorecard.processor;

import java.util.ArrayList;
import java.util.List;

import org.sitenv.ccdaparsing.model.CCDACode;
import org.sitenv.ccdaparsing.model.CCDAII;
import org.sitenv.ccdaparsing.model.CCDAImmunization;
import org.sitenv.ccdaparsing.model.CCDAImmunizationActivity;
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
public class ImmunizationScorecard {
	
	@Autowired
	TemplateIdProcessor templateIdProcessor;
	
	public Category getImmunizationCategory(CCDAImmunization immunizations, PatientDetails patientDetails,String docType,List<SectionRule>sectionRules)
	{
		if(immunizations == null || immunizations.isSectionNullFlavourWithNI())
		{
			return new Category(ApplicationConstants.CATEGORIES.IMMUNIZATIONS.getCategoryDesc(),true);
		}
		Category immunizationCategory = new Category();
		immunizationCategory.setCategoryName(ApplicationConstants.CATEGORIES.IMMUNIZATIONS.getCategoryDesc());
		
		List<CCDAScoreCardRubrics> immunizationScoreList = new ArrayList<CCDAScoreCardRubrics>();
		
		if (sectionRules==null || ApplicationUtil.isRuleEnabled(sectionRules, ApplicationConstants.RULE_IDS.I1)) {
			immunizationScoreList.add(getTimePrecisionScore(immunizations, docType));
		}
		if (sectionRules==null || ApplicationUtil.isRuleEnabled(sectionRules, ApplicationConstants.RULE_IDS.I2)) {
			immunizationScoreList.add(getValidDateTimeScore(immunizations, patientDetails, docType));
		}
		if (sectionRules==null || ApplicationUtil.isRuleEnabled(sectionRules, ApplicationConstants.RULE_IDS.I3)) {
			immunizationScoreList.add(getValidDisplayNameScoreCard(immunizations, docType));
		}
		if (sectionRules==null || ApplicationUtil.isRuleEnabled(sectionRules, ApplicationConstants.RULE_IDS.I4)) {
			immunizationScoreList.add(getValidImmunizationCodeScoreCard(immunizations, docType));
		}
		if (sectionRules==null || ApplicationUtil.isRuleEnabled(sectionRules, ApplicationConstants.RULE_IDS.I5)) {
			immunizationScoreList.add(getNarrativeStructureIdScore(immunizations, docType));
		}
		if (sectionRules==null || ApplicationUtil.isRuleEnabled(sectionRules, ApplicationConstants.RULE_IDS.I6)) {
			immunizationScoreList.add(getTemplateIdScore(immunizations, docType));
		}
		
		immunizationCategory.setCategoryRubrics(immunizationScoreList);
		ApplicationUtil.calculateSectionGradeAndIssues(immunizationScoreList, immunizationCategory);
		
		return immunizationCategory;
		
	}
	
	public CCDAScoreCardRubrics getTimePrecisionScore(CCDAImmunization immunizatons,String docType)
	{
		CCDAScoreCardRubrics timePrecisionScore = new CCDAScoreCardRubrics();
		timePrecisionScore.setRule(ApplicationConstants.TIME_PRECISION_REQUIREMENT);
		
		int actualPoints =0;
		int maxPoints = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue= null;
		if(immunizatons != null)
		{
			if(!ApplicationUtil.isEmpty(immunizatons.getImmActivity()))
			{
				for (CCDAImmunizationActivity immunizationActivity : immunizatons.getImmActivity())
				{
					maxPoints++;
					if(immunizationActivity.getTime() != null)
					{
						if(ApplicationUtil.validateDayFormat(immunizationActivity.getTime()) ||
								ApplicationUtil.validateMinuteFormat(immunizationActivity.getTime()) ||
								ApplicationUtil.validateSecondFormat(immunizationActivity.getTime()))
						{
							actualPoints++;
						}
						else
						{
							issue = new CCDAXmlSnippet();
							issue.setLineNumber(immunizationActivity.getTime().getLineNumber());
							issue.setXmlString(immunizationActivity.getTime().getXmlString());
							issuesList.add(issue);
						}
					}
					else
					{
						issue = new CCDAXmlSnippet();
						issue.setLineNumber(immunizationActivity.getLineNumber());
						issue.setXmlString(immunizationActivity.getXmlString());
						issuesList.add(issue);
					}
				}
			}
			else
			{
				issue = new CCDAXmlSnippet();
				issue.setLineNumber(immunizatons.getLineNumber());
				issue.setXmlString(immunizatons.getXmlString());
				issuesList.add(issue);
			}
		}
		else
		{
			issue = new CCDAXmlSnippet();
			issue.setLineNumber("Immunization section not present");
			issue.setXmlString("Immunization section not present");
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
				timePrecisionScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES.IMMUNIZATION_ACTIVITY.getIgReference());
			}else if (docType.equalsIgnoreCase("R1.1"))
			{
				timePrecisionScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES_R1.IMMUNIZATION_ACTIVITY.getIgReference());
			}
			timePrecisionScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_LINKS.IMMUNIZATIONS.getTaskforceLink());
		}
		return timePrecisionScore;
	}
	
	
	public CCDAScoreCardRubrics getValidDateTimeScore(CCDAImmunization immunizatons, PatientDetails patientDetails,String docType)
	{
		CCDAScoreCardRubrics validateTimeScore = new CCDAScoreCardRubrics();
		validateTimeScore.setRule(ApplicationConstants.TIME_VALID_REQUIREMENT);
		
		int actualPoints =0;
		int maxPoints = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue= null;
		if(immunizatons != null)
		{
			if(!ApplicationUtil.isEmpty(immunizatons.getImmActivity()))
			{
				for (CCDAImmunizationActivity immunizationActivity : immunizatons.getImmActivity())
				{
					if(immunizationActivity.getTime() != null && ApplicationUtil.isEffectiveTimePresent(immunizationActivity.getTime()))
					{
						maxPoints++;
						if(ApplicationUtil.checkDateRange(patientDetails, immunizationActivity.getTime()))
						{
							actualPoints++;
						}
						else
						{
							issue = new CCDAXmlSnippet();
							issue.setLineNumber(immunizationActivity.getTime().getLineNumber());
							issue.setXmlString(immunizationActivity.getTime().getXmlString());
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
				validateTimeScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES.IMMUNIZATION_ACTIVITY.getIgReference());
			}
			else if (docType.equalsIgnoreCase("R1.1"))
			{
				validateTimeScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES_R1.IMMUNIZATION_ACTIVITY.getIgReference());
			}
			validateTimeScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_LINKS.IMMUNIZATIONS.getTaskforceLink());
		}
		return validateTimeScore;
	}
	
	
	public CCDAScoreCardRubrics getValidDisplayNameScoreCard(CCDAImmunization immunizatons,String docType)
	{
		CCDAScoreCardRubrics validateDisplayNameScore = new CCDAScoreCardRubrics();
		validateDisplayNameScore.setRule(ApplicationConstants.CODE_DISPLAYNAME_REQUIREMENT);
		
		int maxPoints = 0;
		int actualPoints = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue= null;
		if(immunizatons != null)
		{
			if(immunizatons.getSectionCode()!= null && !ApplicationUtil.isEmpty(immunizatons.getSectionCode().getDisplayName()) 
													&& ApplicationUtil.isCodeSystemAvailable(immunizatons.getSectionCode().getCodeSystem()))
			{
				maxPoints++;
				if(ApplicationUtil.validateDisplayName(immunizatons.getSectionCode().getCode(), 
														immunizatons.getSectionCode().getCodeSystem(),
														immunizatons.getSectionCode().getDisplayName()))
				{
					actualPoints++;
				}
				else
				{
					issue = new CCDAXmlSnippet();
					issue.setLineNumber(immunizatons.getSectionCode().getLineNumber());
					issue.setXmlString(immunizatons.getSectionCode().getXmlString());
					issuesList.add(issue);
				}
			}
			if(!ApplicationUtil.isEmpty(immunizatons.getImmActivity()))
			{
				for (CCDAImmunizationActivity immuActivity : immunizatons.getImmActivity())
				{
					if(immuActivity.getApproachSiteCode() != null && !ApplicationUtil.isEmpty(immuActivity.getApproachSiteCode().getDisplayName())
																&& ApplicationUtil.isCodeSystemAvailable(immuActivity.getApproachSiteCode().getCodeSystem()))
					{
						maxPoints++;
						if(ApplicationUtil.validateDisplayName(immuActivity.getApproachSiteCode().getCode(), 
															   immuActivity.getApproachSiteCode().getCodeSystem(),
															   immuActivity.getApproachSiteCode().getDisplayName()))
						{
							actualPoints++;
						}
						else
						{
							issue = new CCDAXmlSnippet();
							issue.setLineNumber(immuActivity.getApproachSiteCode().getLineNumber());
							issue.setXmlString(immuActivity.getApproachSiteCode().getXmlString());
							issuesList.add(issue);
						}
					}
					if(immuActivity.getConsumable() != null)
					{
						if(!ApplicationUtil.isEmpty(immuActivity.getConsumable().getTranslations()))
						{
							for (CCDACode translationCode : immuActivity.getConsumable().getTranslations())
							{
								if(!ApplicationUtil.isEmpty(translationCode.getDisplayName()) && ApplicationUtil.isCodeSystemAvailable(translationCode.getCodeSystem()))
								{
									maxPoints++;
									if(ApplicationUtil.validateDisplayName(translationCode.getCode(), 
													translationCode.getCodeSystem().toUpperCase(),
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

		if(maxPoints ==0)
		{
			maxPoints=1;
			actualPoints=1;
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
				validateDisplayNameScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES.IMMUNIZATION_SECTION.getIgReference());
			}else if (docType.equalsIgnoreCase("R1.1"))
			{
				validateDisplayNameScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES_R1.IMMUNIZATION_SECTION.getIgReference());
			}
			validateDisplayNameScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_LINKS.IMMUNIZATIONS.getTaskforceLink());
		}
		return validateDisplayNameScore;
	}
	
	public CCDAScoreCardRubrics getValidImmunizationCodeScoreCard(CCDAImmunization immunizations,String docType)
	{
		CCDAScoreCardRubrics validateImmuCodeScore = new CCDAScoreCardRubrics();
		validateImmuCodeScore.setRule(ApplicationConstants.IMMU_CODE_REQ);
		
		int maxPoints = 0;
		int actualPoints = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue= null;
		if(immunizations != null)
		{
			if(!ApplicationUtil.isEmpty(immunizations.getImmActivity()))
			{
				for (CCDAImmunizationActivity immuAct : immunizations.getImmActivity())
				{
					maxPoints++;
					if(immuAct.getConsumable()!=null)
					{
						if(immuAct.getConsumable().getMedcode()!=null)
						{
							if(ApplicationUtil.validateCodeForValueset(immuAct.getConsumable().getMedcode().getCode(), 
																			ApplicationConstants.CVX_CODES_VALUSET_OID))
							{
								actualPoints++;
							}
							else
							{
								issue = new CCDAXmlSnippet();
								issue.setLineNumber(immuAct.getConsumable().getMedcode().getLineNumber());
								issue.setXmlString(immuAct.getConsumable().getMedcode().getXmlString());
								issuesList.add(issue);
							}
						}
						else
						{
							issue = new CCDAXmlSnippet();
							issue.setLineNumber(immuAct.getConsumable().getLineNumber());
							issue.setXmlString(immuAct.getConsumable().getXmlString());
							issuesList.add(issue);
						}
					}
					else
					{
						issue = new CCDAXmlSnippet();
						issue.setLineNumber(immuAct.getLineNumber());
						issue.setXmlString(immuAct.getXmlString());
						issuesList.add(issue);
					}
				}
			}
			else
			{
				issue = new CCDAXmlSnippet();
				issue.setLineNumber(immunizations.getLineNumber());
				issue.setXmlString(immunizations.getXmlString());
				issuesList.add(issue);
			}
		}
		else
		{
			issue = new CCDAXmlSnippet();
			issue.setLineNumber("Immunization section not present");
			issue.setXmlString("Immunization section not present");
			issuesList.add(issue);
		}
		validateImmuCodeScore.setActualPoints(actualPoints);
		validateImmuCodeScore.setMaxPoints(maxPoints);
		validateImmuCodeScore.setRubricScore(ApplicationUtil.calculateRubricScore(maxPoints, actualPoints));
		validateImmuCodeScore.setIssuesList(issuesList);
		validateImmuCodeScore.setNumberOfIssues(issuesList.size());
		if(issuesList.size() > 0)
		{
			validateImmuCodeScore.setDescription(ApplicationConstants.IMMU_CODE_DESC);
			if(docType.equalsIgnoreCase("") || docType.equalsIgnoreCase("R2.1"))
			{
				validateImmuCodeScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES.IMMUNIZATION_ACTIVITY.getIgReference());
			}
			else if (docType.equalsIgnoreCase("R1.1"))
			{
				validateImmuCodeScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES_R1.IMMUNIZATION_ACTIVITY.getIgReference());
			}
			validateImmuCodeScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_LINKS.IMMUNIZATIONS.getTaskforceLink());
		}
		return validateImmuCodeScore;
	}
	
	public CCDAScoreCardRubrics getNarrativeStructureIdScore(CCDAImmunization immunizations,String docType)
	{
		CCDAScoreCardRubrics narrativeTextIdScore = new CCDAScoreCardRubrics();
		narrativeTextIdScore.setRule(ApplicationConstants.NARRATIVE_STRUCTURE_ID_REQ);
		
		int maxPoints = 0;
		int actualPoints = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue= null;
		if(immunizations != null)
		{
			if(!ApplicationUtil.isEmpty(immunizations.getImmActivity()))
			{
				for(CCDAImmunizationActivity immuAct : immunizations.getImmActivity())
				{
					maxPoints++;
					if(immuAct.getReferenceText()!= null)
					{
						if(immunizations.getReferenceLinks()!= null && immunizations.getReferenceLinks().contains(immuAct.getReferenceText().getValue()))
						{
							actualPoints++;
						}
						else
						{
							issue = new CCDAXmlSnippet();
							issue.setLineNumber(immuAct.getReferenceText().getLineNumber());
							issue.setXmlString(immuAct.getReferenceText().getXmlString());
							issuesList.add(issue);
						}
					}
					else
					{
						issue = new CCDAXmlSnippet();
						issue.setLineNumber(immuAct.getLineNumber());
						issue.setXmlString(immuAct.getXmlString());
						issuesList.add(issue);
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
				narrativeTextIdScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES.IMMUNIZATION_SECTION.getIgReference());
			}else if (docType.equalsIgnoreCase("R1.1"))
			{
				narrativeTextIdScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES_R1.IMMUNIZATION_SECTION.getIgReference());
			}
			narrativeTextIdScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_LINKS.IMMUNIZATIONS.getTaskforceLink());
		}
		
		return narrativeTextIdScore;
	}
	
	
	public CCDAScoreCardRubrics getTemplateIdScore(CCDAImmunization immunizations,String docType)
	{
		CCDAScoreCardRubrics templateIdScore = new CCDAScoreCardRubrics();
		templateIdScore.setRule(ApplicationConstants.TEMPLATEID_DESC);
		
		int maxPoints = 0;
		int actualPoints = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		
		
		if(immunizations!= null)
		{
			if(!ApplicationUtil.isEmpty(immunizations.getTemplateIds()))
			{
				for (CCDAII templateId : immunizations.getTemplateIds())
				{
					maxPoints = maxPoints++;
					templateIdProcessor.scoreTemplateId(templateId,actualPoints,issuesList,docType);
				}
			}
			
			if(!ApplicationUtil.isEmpty(immunizations.getImmActivity()))
			{
				for(CCDAImmunizationActivity immuActivity :  immunizations.getImmActivity())
				{
					if(!ApplicationUtil.isEmpty(immuActivity.getTemplateIds()))
					{
						for (CCDAII templateId : immuActivity.getTemplateIds())
						{
							maxPoints = maxPoints++;
							templateIdProcessor.scoreTemplateId(templateId,actualPoints,issuesList,docType);
						}
					}
					
					if((immuActivity.getConsumable()!=null))
					{
						if(!ApplicationUtil.isEmpty(immuActivity.getConsumable().getTemplateIds()))
						{
							for (CCDAII templateId : immuActivity.getConsumable().getTemplateIds())
							{
								maxPoints = maxPoints++;
								templateIdProcessor.scoreTemplateId(templateId,actualPoints,issuesList,docType);
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
