package org.sitenv.service.ccda.smartscorecard.processor;

import java.util.ArrayList;
import java.util.List;

import org.sitenv.ccdaparsing.model.CCDACode;
import org.sitenv.ccdaparsing.model.CCDAII;
import org.sitenv.ccdaparsing.model.CCDAMedication;
import org.sitenv.ccdaparsing.model.CCDAMedicationActivity;
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
public class MedicationScorecard {
	
	@Autowired
	TemplateIdProcessor templateIdProcessor;
	
	public Category getMedicationCategory(CCDAMedication medications, PatientDetails patientDetails,String docType,List<SectionRule> sectionRules)
	{
		
		if(medications==null || medications.isSectionNullFlavourWithNI())
		{
			return new Category(ApplicationConstants.CATEGORIES.MEDICATIONS.getCategoryDesc(),true);
		}
		Category medicationCategory = new Category();
		medicationCategory.setCategoryName(ApplicationConstants.CATEGORIES.MEDICATIONS.getCategoryDesc());
		
		List<CCDAScoreCardRubrics> medicationScoreList = new ArrayList<CCDAScoreCardRubrics>();
		
		if (sectionRules==null || ApplicationUtil.isRuleEnabled(sectionRules, ApplicationConstants.TIME_PRECISION_REQUIREMENT)) {
			medicationScoreList.add(getTimePrecisionScore(medications, docType));
		}
		if (sectionRules==null || ApplicationUtil.isRuleEnabled(sectionRules, ApplicationConstants.TIME_VALID_REQUIREMENT)) {
			medicationScoreList.add(getValidDateTimeScore(medications, patientDetails, docType));
		}
		if (sectionRules==null || ApplicationUtil.isRuleEnabled(sectionRules, ApplicationConstants.CODE_DISPLAYNAME_REQUIREMENT)) {
			medicationScoreList.add(getValidDisplayNameScoreCard(medications, docType));
		}
		if (sectionRules==null || ApplicationUtil.isRuleEnabled(sectionRules, ApplicationConstants.MEDICATION_CODE_REQ)) {
			medicationScoreList.add(getValidMedicationCodeScoreCard(medications, docType));
		}
		// medicationScoreList.add(getValidMedActivityScore(medications,docType));
		if (sectionRules==null || ApplicationUtil.isRuleEnabled(sectionRules, ApplicationConstants.NARRATIVE_STRUCTURE_ID_REQ)) {
			medicationScoreList.add(getNarrativeStructureIdScore(medications, docType));
		}
		if (sectionRules==null || ApplicationUtil.isRuleEnabled(sectionRules, ApplicationConstants.MED_SIG_TEXT_REQ)) {
			medicationScoreList.add(getMedSubAdminScore(medications, docType));
		}
		if (sectionRules==null || ApplicationUtil.isRuleEnabled(sectionRules, ApplicationConstants.TEMPLATEID_DESC)) {
			medicationScoreList.add(getTemplateIdScore(medications, docType));
		}
		
		medicationCategory.setCategoryRubrics(medicationScoreList);
		ApplicationUtil.calculateSectionGradeAndIssues(medicationScoreList, medicationCategory);
		
		return medicationCategory;
		
	}
	
	public CCDAScoreCardRubrics getTimePrecisionScore(CCDAMedication medications,String docType)
	{
		CCDAScoreCardRubrics timePrecisionScore = new CCDAScoreCardRubrics();
		timePrecisionScore.setRule(ApplicationConstants.TIME_PRECISION_REQUIREMENT);
		
		int actualPoints =0;
		int maxPoints = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue= null;
		if(medications != null)
		{
			if(!ApplicationUtil.isEmpty(medications.getMedActivities()))
			{
				for (CCDAMedicationActivity medActivity : medications.getMedActivities())
				{
					maxPoints++;
					if(medActivity.getDuration() != null)
					{
						if(ApplicationUtil.validateDayFormat(medActivity.getDuration()) ||
								ApplicationUtil.validateMinuteFormat(medActivity.getDuration()) ||
									ApplicationUtil.validateSecondFormat(medActivity.getDuration()))
						{
							actualPoints++;
						}
						else
						{
							issue = new CCDAXmlSnippet();
							issue.setLineNumber(medActivity.getDuration().getLineNumber());
							issue.setXmlString(medActivity.getDuration().getXmlString());
							issuesList.add(issue);
						}
					}
					else
					{
						issue = new CCDAXmlSnippet();
						issue.setLineNumber(medActivity.getLineNumber());
						issue.setXmlString(medActivity.getXmlString());
						issuesList.add(issue);
					}
				}
			}
			else
			{
				issue = new CCDAXmlSnippet();
				issue.setLineNumber(medications.getLineNumber());
				issue.setXmlString(medications.getXmlString());
				issuesList.add(issue);
			}
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
				timePrecisionScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES.MEDICATION_ACTIVITY.getIgReference());
			}else if (docType.equalsIgnoreCase("R1.1"))
			{
				timePrecisionScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES_R1.MEDICATION_ACTIVITY.getIgReference());
			}
			timePrecisionScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_LINKS.MEDICATIONS.getTaskforceLink());
		}
		return timePrecisionScore;
	}
	
	
	public CCDAScoreCardRubrics getValidDateTimeScore(CCDAMedication medications, PatientDetails patientDetails,String docType)
	{
		CCDAScoreCardRubrics validateTimeScore = new CCDAScoreCardRubrics();
		validateTimeScore.setRule(ApplicationConstants.TIME_VALID_REQUIREMENT);
		
		int actualPoints =0;
		int maxPoints = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue= null;
		if(medications != null)
		{
			if(!ApplicationUtil.isEmpty(medications.getMedActivities()))
			{
				for (CCDAMedicationActivity medActivity : medications.getMedActivities())
				{
					if(medActivity.getDuration() != null && ApplicationUtil.isEffectiveTimePresent(medActivity.getDuration()))
					{
						maxPoints++;
						if(ApplicationUtil.checkDateRange(patientDetails, medActivity.getDuration()))
						{
							actualPoints++;
						}
						else
						{
							issue = new CCDAXmlSnippet();
							issue.setLineNumber(medActivity.getDuration().getLineNumber());
							issue.setXmlString(medActivity.getDuration().getXmlString());
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
				validateTimeScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES.MEDICATION_ACTIVITY.getIgReference());
			}else if (docType.equalsIgnoreCase("R1.1"))
			{
				validateTimeScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES_R1.MEDICATION_ACTIVITY.getIgReference());
			}
			validateTimeScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_LINKS.MEDICATIONS.getTaskforceLink());
		}
		return validateTimeScore;
	}
	
	
	public CCDAScoreCardRubrics getValidDisplayNameScoreCard(CCDAMedication medications,String docType)
	{
		CCDAScoreCardRubrics validateDisplayNameScore = new CCDAScoreCardRubrics();
		validateDisplayNameScore.setRule(ApplicationConstants.CODE_DISPLAYNAME_REQUIREMENT);
		
		int maxPoints = 0;
		int actualPoints = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue= null;
		if(medications != null)
		{
			if(medications.getSectionCode()!= null && !ApplicationUtil.isEmpty(medications.getSectionCode().getDisplayName())
													&& ApplicationUtil.isCodeSystemAvailable(medications.getSectionCode().getCodeSystem()))
			{
				maxPoints++;
				if(ApplicationUtil.validateDisplayName(medications.getSectionCode().getCode(), 
							medications.getSectionCode().getCodeSystem(),
						medications.getSectionCode().getDisplayName()))
				{
					actualPoints++;
				}
				else
				{
					issue = new CCDAXmlSnippet();
					issue.setLineNumber(medications.getSectionCode().getLineNumber());
					issue.setXmlString(medications.getSectionCode().getXmlString());
					issuesList.add(issue);
				}
			}
			
			if(!ApplicationUtil.isEmpty(medications.getMedActivities()))
			{
				for (CCDAMedicationActivity medActivity : medications.getMedActivities())
				{
					if(medActivity.getApproachSiteCode()!= null && !ApplicationUtil.isEmpty(medActivity.getApproachSiteCode().getDisplayName())
																&& ApplicationUtil.isCodeSystemAvailable(medActivity.getApproachSiteCode().getCodeSystem()))
					{
						maxPoints++;
						if(ApplicationUtil.validateDisplayName(medActivity.getApproachSiteCode().getCode(), 
																medActivity.getApproachSiteCode().getCodeSystem(),
																medActivity.getApproachSiteCode().getDisplayName()))
						{
							actualPoints++;
						}
						else
						{
							issue = new CCDAXmlSnippet();
							issue.setLineNumber(medActivity.getApproachSiteCode().getLineNumber());
							issue.setXmlString(medActivity.getApproachSiteCode().getXmlString());
							issuesList.add(issue);
						}
					}
					if(medActivity.getConsumable() != null)
					{
						if(!ApplicationUtil.isEmpty(medActivity.getConsumable().getTranslations()))
						{
							for (CCDACode translationCode : medActivity.getConsumable().getTranslations())
							{
								if(!ApplicationUtil.isEmpty(translationCode.getDisplayName()) && ApplicationUtil.isCodeSystemAvailable(translationCode.getCodeSystem()))
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
		
		if(maxPoints==0)
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
				validateDisplayNameScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES.MEDICATION_SECTION.getIgReference());
			}else if (docType.equalsIgnoreCase("R1.1"))
			{
				validateDisplayNameScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES_R1.MEDICATION_SECTION.getIgReference());
			}
			validateDisplayNameScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_LINKS.MEDICATIONS.getTaskforceLink());
		}
		return validateDisplayNameScore;
	}
	
	public CCDAScoreCardRubrics getValidMedicationCodeScoreCard(CCDAMedication medications,String docType)
	{
		CCDAScoreCardRubrics validateImmuCodeScore = new CCDAScoreCardRubrics();
		validateImmuCodeScore.setRule(ApplicationConstants.MEDICATION_CODE_REQ);
		
		int maxPoints = 0;
		int actualPoints = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue= null;
		if(medications != null)
		{
			if(!ApplicationUtil.isEmpty(medications.getMedActivities()))
			{
				for (CCDAMedicationActivity medAct : medications.getMedActivities())
				{
					maxPoints++;
					if(medAct.getConsumable()!=null)
					{
						if(medAct.getConsumable().getMedcode()!=null)
						{
							if(ApplicationUtil.validateCodeForValueset(medAct.getConsumable().getMedcode().getCode(), 
																			ApplicationConstants.MEDICATION_CLINICAL_DRUG_VALUSET_OID))
							{
								actualPoints++;
							}
							else
							{
								issue = new CCDAXmlSnippet();
								issue.setLineNumber(medAct.getConsumable().getMedcode().getLineNumber());
								issue.setXmlString(medAct.getConsumable().getMedcode().getXmlString());
								issuesList.add(issue);
							}
						}
						else
						{
							issue = new CCDAXmlSnippet();
							issue.setLineNumber(medAct.getConsumable().getLineNumber());
							issue.setXmlString(medAct.getConsumable().getXmlString());
							issuesList.add(issue);
						}
					}
					else
					{
						issue = new CCDAXmlSnippet();
						issue.setLineNumber(medAct.getLineNumber());
						issue.setXmlString(medAct.getXmlString());
						issuesList.add(issue);
					}
				}
			}
			else
			{
				issue = new CCDAXmlSnippet();
				issue.setLineNumber(medications.getLineNumber());
				issue.setXmlString(medications.getXmlString());
				issuesList.add(issue);
			}
		}
		else
		{
			issue = new CCDAXmlSnippet();
			issue.setLineNumber("Medications section not present");
			issue.setXmlString("Medications section not present");
			issuesList.add(issue);
		}
		validateImmuCodeScore.setActualPoints(actualPoints);
		validateImmuCodeScore.setMaxPoints(maxPoints);
		validateImmuCodeScore.setRubricScore(ApplicationUtil.calculateRubricScore(maxPoints, actualPoints));
		validateImmuCodeScore.setIssuesList(issuesList);
		validateImmuCodeScore.setNumberOfIssues(issuesList.size());
		if(issuesList.size() > 0)
		{
			validateImmuCodeScore.setDescription(ApplicationConstants.MEDICATION_CODE_DESC);
			if(docType.equalsIgnoreCase("") || docType.equalsIgnoreCase("R2.1"))
			{
				validateImmuCodeScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES.MEDICATION_ACTIVITY.getIgReference());
			}else if (docType.equalsIgnoreCase("R1.1"))
			{
				validateImmuCodeScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES_R1.MEDICATION_ACTIVITY.getIgReference());
			}
			validateImmuCodeScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_LINKS.MEDICATIONS.getTaskforceLink());
		}
		return validateImmuCodeScore;
	}
	
	public CCDAScoreCardRubrics getValidMedActivityScore(CCDAMedication medications,String docType)
	{
		CCDAScoreCardRubrics validateMedActivityScore = new CCDAScoreCardRubrics();
		validateMedActivityScore.setRule(ApplicationConstants.IMMU_NOTIN_MED_REQ);
		
		int actualPoints =0;
		int maxPoints = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue= null;
		if(medications != null)
		{
			if(!ApplicationUtil.isEmpty(medications.getMedActivities()))
			{
				for (CCDAMedicationActivity medAct : medications.getMedActivities())
				{
					if(!ApplicationUtil.isEmpty(medAct.getTemplateIds()))
					{
						maxPoints = maxPoints + medAct.getTemplateIds().size();
						for (CCDAII templateId : medAct.getTemplateIds())
						{
							if(templateId.getRootValue() != null && templateId.getRootValue().equals(ApplicationConstants.MEDICATION_ACTIVITY_ID))
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
						issue.setLineNumber(medAct.getLineNumber());
						issue.setXmlString(medAct.getXmlString());
						issuesList.add(issue);
					}
				}
			}
			else
			{
				issue = new CCDAXmlSnippet();
				issue.setLineNumber(medications.getLineNumber());
				issue.setXmlString(medications.getXmlString());
				issuesList.add(issue);
			}
		}
		else
		{
			issue = new CCDAXmlSnippet();
			issue.setLineNumber("Medications section not present");
			issue.setXmlString("Medications section not present");
			issuesList.add(issue);
		}
		
		validateMedActivityScore.setActualPoints(actualPoints);
		validateMedActivityScore.setMaxPoints(maxPoints);
		validateMedActivityScore.setRubricScore(ApplicationUtil.calculateRubricScore(maxPoints, actualPoints));
		validateMedActivityScore.setIssuesList(issuesList);
		validateMedActivityScore.setNumberOfIssues(issuesList.size());
		if(issuesList.size() > 0)
		{
			validateMedActivityScore.setDescription(ApplicationConstants.IMMU_NOTIN_MED_DESC);
			if(docType.equalsIgnoreCase("") || docType.equalsIgnoreCase("R2.1"))
			{
				validateMedActivityScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES.MEDICATION_ACTIVITY.getIgReference());
			}
			else if(docType.equalsIgnoreCase("R1.1"))
			{
				validateMedActivityScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES_R1.MEDICATION_ACTIVITY.getIgReference());
			}
			validateMedActivityScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_LINKS.MEDICATIONS.getTaskforceLink());
		}
		return validateMedActivityScore;
	}
	
	public CCDAScoreCardRubrics getNarrativeStructureIdScore(CCDAMedication medications,String docType)
	{
		CCDAScoreCardRubrics narrativeTextIdScore = new CCDAScoreCardRubrics();
		narrativeTextIdScore.setRule(ApplicationConstants.NARRATIVE_STRUCTURE_ID_REQ);
		
		int maxPoints = 0;
		int actualPoints = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue= null;
		if(medications != null)
		{
			if(!ApplicationUtil.isEmpty(medications.getMedActivities()))
			{
				for(CCDAMedicationActivity medAct : medications.getMedActivities())
				{
					maxPoints++;
					if(medAct.getReferenceText()!= null)
					{
						if(medications.getReferenceLinks()!= null && medications.getReferenceLinks().contains(medAct.getReferenceText().getValue()))
						{
							actualPoints++;
						}
						else
						{
							issue = new CCDAXmlSnippet();
							issue.setLineNumber(medAct.getReferenceText().getLineNumber());
							issue.setXmlString(medAct.getReferenceText().getXmlString());
							issuesList.add(issue);
						}
					}
					else
					{
						issue = new CCDAXmlSnippet();
						issue.setLineNumber(medAct.getLineNumber());
						issue.setXmlString(medAct.getXmlString());
						issuesList.add(issue);
					}
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
				narrativeTextIdScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES.MEDICATION_SECTION.getIgReference());
			}
			else if (docType.equalsIgnoreCase("R1.1"))
			{
				narrativeTextIdScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES_R1.MEDICATION_SECTION.getIgReference());
			}
			narrativeTextIdScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_LINKS.MEDICATIONS.getTaskforceLink());
		}
		
		return narrativeTextIdScore;
	}
	
	public CCDAScoreCardRubrics getMedSubAdminScore(CCDAMedication medications,String docType)
	{
		CCDAScoreCardRubrics medSubAdminScore = new CCDAScoreCardRubrics();
		medSubAdminScore.setRule(ApplicationConstants.MED_SIG_TEXT_REQ);
		
		int maxPoints = 0;
		int actualPoints = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue= null;
		if(medications != null)
		{
			if(!ApplicationUtil.isEmpty(medications.getMedActivities()))
			{
				for (CCDAMedicationActivity medAct : medications.getMedActivities())
				{
					maxPoints++;
					if(medAct.getMedSubAdmin()!=null)
					{
						if(medAct.getMedSubAdmin().getReferenceText()!= null)
						{
							if(medications.getReferenceLinks()!= null && medications.getReferenceLinks().contains(medAct.getMedSubAdmin().getReferenceText().getValue()))
							{
								actualPoints++;
							}
							else
							{
								issue = new CCDAXmlSnippet();
								issue.setLineNumber(medAct.getMedSubAdmin().getReferenceText().getLineNumber());
								issue.setXmlString(medAct.getMedSubAdmin().getReferenceText().getXmlString());
								issuesList.add(issue);
							}
						}
						else
						{
							issue = new CCDAXmlSnippet();
							issue.setLineNumber(medAct.getMedSubAdmin().getLineNumber());
							issue.setXmlString(medAct.getMedSubAdmin().getXmlString());
							issuesList.add(issue);
						}
					}
					else
					{
						issue = new CCDAXmlSnippet();
						issue.setLineNumber(medAct.getLineNumber());
						issue.setXmlString(medAct.getXmlString());
						issuesList.add(issue);
					}
				}
			}
		}
		
		if(maxPoints==0)
		{
			maxPoints = 1;
			actualPoints = 1;
		}
		
		medSubAdminScore.setActualPoints(actualPoints);
		medSubAdminScore.setMaxPoints(maxPoints);
		medSubAdminScore.setRubricScore(ApplicationUtil.calculateRubricScore(maxPoints, actualPoints));
		medSubAdminScore.setIssuesList(issuesList);
		medSubAdminScore.setNumberOfIssues(issuesList.size());
		if(issuesList.size() > 0)
		{
			medSubAdminScore.setDescription(ApplicationConstants.MED_SIG_TEXT_DESC);
			if(docType.equalsIgnoreCase("") || docType.equalsIgnoreCase("R2.1"))
			{
				medSubAdminScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES.MEDICATION_SECTION.getIgReference());
			}
			else if (docType.equalsIgnoreCase("R1.1"))
			{
				medSubAdminScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES_R1.MEDICATION_SECTION.getIgReference());
			}
			medSubAdminScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_LINKS.MEDICATIONS.getTaskforceLink());
		}
		
		return medSubAdminScore;
	}
	
	public CCDAScoreCardRubrics getTemplateIdScore(CCDAMedication medications,String docType)
	{
		CCDAScoreCardRubrics templateIdScore = new CCDAScoreCardRubrics();
		templateIdScore.setRule(ApplicationConstants.TEMPLATEID_DESC);
		
		int maxPoints = 0;
		int actualPoints = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		
		if(medications!=null)
		{
			if(!ApplicationUtil.isEmpty(medications.getTemplateIds()))
			{
				for (CCDAII templateId : medications.getTemplateIds())
				{
					maxPoints = maxPoints++;
					templateIdProcessor.scoreTemplateId(templateId,actualPoints,issuesList,docType);
				}
			}
			
			if(!ApplicationUtil.isEmpty(medications.getMedActivities()))
			{
				for(CCDAMedicationActivity medAct : medications.getMedActivities())
				{
					if(!ApplicationUtil.isEmpty(medAct.getTemplateIds()))
					{
						for (CCDAII templateId : medAct.getTemplateIds())
						{
							maxPoints = maxPoints++;
							templateIdProcessor.scoreTemplateId(templateId,actualPoints,issuesList,docType);
						}
					}
					
					if(medAct.getConsumable()!=null)
					{
						if(!ApplicationUtil.isEmpty(medAct.getConsumable().getTemplateIds()))
						{
							for (CCDAII templateId : medAct.getConsumable().getTemplateIds())
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
