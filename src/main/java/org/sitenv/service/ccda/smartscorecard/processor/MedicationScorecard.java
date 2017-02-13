package org.sitenv.service.ccda.smartscorecard.processor;

import java.util.ArrayList;
import java.util.List;

import org.sitenv.ccdaparsing.model.CCDACode;
import org.sitenv.ccdaparsing.model.CCDADataElement;
import org.sitenv.ccdaparsing.model.CCDAII;
import org.sitenv.ccdaparsing.model.CCDAMedication;
import org.sitenv.ccdaparsing.model.CCDAMedicationActivity;
import org.sitenv.ccdaparsing.model.CCDAXmlSnippet;
import org.sitenv.service.ccda.smartscorecard.model.CCDAScoreCardRubrics;
import org.sitenv.service.ccda.smartscorecard.model.Category;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationConstants;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationUtil;
import org.springframework.stereotype.Service;

@Service
public class MedicationScorecard {
	
	public Category getMedicationCategory(CCDAMedication medications, String birthDate,String docType)
	{
		
		if(medications==null || medications.isSectionNullFlavourWithNI())
		{
			return new Category(ApplicationConstants.CATEGORIES.MEDICATIONS.getCategoryDesc(),true);
		}
		Category medicationCategory = new Category();
		medicationCategory.setCategoryName(ApplicationConstants.CATEGORIES.MEDICATIONS.getCategoryDesc());
		
		List<CCDAScoreCardRubrics> medicationScoreList = new ArrayList<CCDAScoreCardRubrics>();
		medicationScoreList.add(getTimePrecisionScore(medications,docType));
		medicationScoreList.add(getValidDateTimeScore(medications, birthDate,docType));
		medicationScoreList.add(getValidDisplayNameScoreCard(medications,docType));
		medicationScoreList.add(getValidMedicationCodeScoreCard(medications,docType));
		medicationScoreList.add(getValidMedActivityScore(medications,docType));
		medicationScoreList.add(getNarrativeStructureIdScore(medications,docType));
		
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
						if(ApplicationUtil.validateMinuteFormat(medActivity.getDuration()) ||
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
		else
		{
			issue = new CCDAXmlSnippet();
			issue.setLineNumber("Medications section not present");
			issue.setXmlString("Medications section not present");
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
				timePrecisionScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES.MEDICATION_ACTIVITY.getIgReference());
			}else if (docType.equalsIgnoreCase("R1.1"))
			{
				timePrecisionScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES_R1.MEDICATION_ACTIVITY.getIgReference());
			}
			timePrecisionScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_LINKS.MEDICATIONS.getTaskforceLink());
		}
		return timePrecisionScore;
	}
	
	
	public CCDAScoreCardRubrics getValidDateTimeScore(CCDAMedication medications, String birthDate,String docType)
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
					maxPoints++;
					if(medActivity.getDuration() != null)
					{
						if(ApplicationUtil.checkDateRange(birthDate, medActivity.getDuration()))
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
		else
		{
			issue = new CCDAXmlSnippet();
			issue.setLineNumber("Medications section not present");
			issue.setXmlString("Medications section not present");
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
			validateImmuCodeScore.setDescription(ApplicationConstants.MEDICATION_CODE_DISPLAYNAME_REQUIREMENT);
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
					if(!ApplicationUtil.isEmpty(medAct.getReferenceTexts()))
					{
						for(CCDADataElement referenceText : medAct.getReferenceTexts())
						{
							maxPoints++;
							if(medications.getReferenceLinks().contains(referenceText.getValue()))
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
}
