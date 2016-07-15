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
	
	public Category getMedicationCategory(CCDAMedication medications, String birthDate)
	{
		
		Category medicationCategory = new Category();
		medicationCategory.setCategoryName(ApplicationConstants.CATEGORIES.MEDICATIONS.getCategoryDesc());
		
		List<CCDAScoreCardRubrics> medicationScoreList = new ArrayList<CCDAScoreCardRubrics>();
		medicationScoreList.add(getTimePrecisionScore(medications));
		medicationScoreList.add(getValidDateTimeScore(medications, birthDate));
		medicationScoreList.add(getValidDisplayNameScoreCard(medications));
		medicationScoreList.add(getValidMedActivityScore(medications));
		medicationScoreList.add(getNarrativeStructureIdScore(medications));
		
		medicationCategory.setCategoryRubrics(medicationScoreList);
		ApplicationUtil.calculateSectionGradeAndIssues(medicationScoreList, medicationCategory);
		
		return medicationCategory;
		
	}
	
	public CCDAScoreCardRubrics getTimePrecisionScore(CCDAMedication medications)
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
						if(medActivity.getDuration().getSingleAdministration() != null)
						{
							if(ApplicationUtil.validateMinuteFormat(medActivity.getDuration().getSingleAdministration()))
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
						else if(medActivity.getDuration().getLow() != null)
						{
							if(ApplicationUtil.validateDayFormat(medActivity.getDuration().getLow().getValue()) ||
									ApplicationUtil.validateMonthFormat(medActivity.getDuration().getLow().getValue()))
							{
								actualPoints++;
							}else
							{
								issue = new CCDAXmlSnippet();
								issue.setLineNumber(medActivity.getDuration().getLow().getLineNumber());
								issue.setXmlString(medActivity.getDuration().getLow().getXmlString());
								issuesList.add(issue);
							}
							if(medActivity.getDuration().getHigh() != null)
							{
								maxPoints++;
								if(ApplicationUtil.validateDayFormat(medActivity.getDuration().getHigh().getValue()) ||
										ApplicationUtil.validateMonthFormat(medActivity.getDuration().getHigh().getValue()))
								{
									actualPoints++;
								}
								else
								{
									issue = new CCDAXmlSnippet();
									issue.setLineNumber(medActivity.getDuration().getHigh().getLineNumber());
									issue.setXmlString(medActivity.getDuration().getHigh().getXmlString());
									issuesList.add(issue);
								}
							}
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
			timePrecisionScore.getIgReferences().add(ApplicationConstants.IG_SECTION_REFERENCES);
			timePrecisionScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_URL);
		}
		return timePrecisionScore;
	}
	
	
	public CCDAScoreCardRubrics getValidDateTimeScore(CCDAMedication medications, String birthDate)
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
						if(medActivity.getDuration().getSingleAdministration() != null)
						{
							if(ApplicationUtil.checkDateRange(birthDate, medActivity.getDuration().getSingleAdministration()))
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
						else if(medActivity.getDuration().getLow() != null)
						{
							if(ApplicationUtil.checkDateRange(birthDate, medActivity.getDuration().getLow().getValue()))
							{
								actualPoints++;
							}
							else
							{
								issue = new CCDAXmlSnippet();
								issue.setLineNumber(medActivity.getDuration().getLow().getLineNumber());
								issue.setXmlString(medActivity.getDuration().getLow().getXmlString());
								issuesList.add(issue);
							}
							if(medActivity.getDuration().getHigh() != null)
							{
								maxPoints++;
								if(ApplicationUtil.checkDateRange(birthDate, medActivity.getDuration().getHigh().getValue()))
								{
									actualPoints++;
								}
								else
								{
									issue = new CCDAXmlSnippet();
									issue.setLineNumber(medActivity.getDuration().getHigh().getLineNumber());
									issue.setXmlString(medActivity.getDuration().getHigh().getXmlString());
									issuesList.add(issue);
								}
							}
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
			validateTimeScore.getIgReferences().add(ApplicationConstants.IG_SECTION_REFERENCES);
			validateTimeScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_URL);
		}
		return validateTimeScore;
	}
	
	
	public CCDAScoreCardRubrics getValidDisplayNameScoreCard(CCDAMedication medications)
	{
		CCDAScoreCardRubrics validateDisplayNameScore = new CCDAScoreCardRubrics();
		validateDisplayNameScore.setRule(ApplicationConstants.CODE_DISPLAYNAME_REQUIREMENT);
		
		int maxPoints = 0;
		int actualPoints = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue= null;
		if(medications != null)
		{
			maxPoints++;
			if(medications.getSectionCode()!= null)
			{
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
			else
			{
				issue = new CCDAXmlSnippet();
				issue.setLineNumber(medications.getLineNumber());
				issue.setXmlString(medications.getXmlString());
				issuesList.add(issue);
			}
			
			if(!ApplicationUtil.isEmpty(medications.getMedActivities()))
			{
				for (CCDAMedicationActivity medActivity : medications.getMedActivities())
				{
					maxPoints++;
					if(medActivity.getApproachSiteCode()!= null)
					{
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
					else
					{
						issue = new CCDAXmlSnippet();
						issue.setLineNumber(medActivity.getLineNumber());
						issue.setXmlString(medActivity.getXmlString());
						issuesList.add(issue);
					}
					
					if(medActivity.getConsumable() != null)
					{
						if(!ApplicationUtil.isEmpty(medActivity.getConsumable().getTranslations()))
						{
							for (CCDACode translationCode : medActivity.getConsumable().getTranslations())
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
		else
		{
			issue = new CCDAXmlSnippet();
			issue.setLineNumber("Medications section not present");
			issue.setXmlString("Medications section not present");
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
	
	public CCDAScoreCardRubrics getValidMedActivityScore(CCDAMedication medications)
	{
		CCDAScoreCardRubrics validateMedActivityScore = new CCDAScoreCardRubrics();
		validateMedActivityScore.setRule(ApplicationConstants.IMMU_NOTIN_MED_REQ);
		
		int actualPoints =1;
		int maxPoints = 1;
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
						for (CCDAII templateId : medAct.getTemplateIds())
						{
							if(templateId.getValue() != null && templateId.getValue().equals(ApplicationConstants.IMMUNIZATION_ACTIVITY_ID))
							{
								actualPoints = 0;
								issue = new CCDAXmlSnippet();
								issue.setLineNumber(medAct.getLineNumber());
								issue.setXmlString(medAct.getXmlString());
								issuesList.add(issue);
							}
						}
					}
				}
			}
		}
		
		validateMedActivityScore.setActualPoints(actualPoints);
		validateMedActivityScore.setMaxPoints(maxPoints);
		validateMedActivityScore.setRubricScore(ApplicationUtil.calculateRubricScore(maxPoints, actualPoints));
		validateMedActivityScore.setIssuesList(issuesList);
		validateMedActivityScore.setNumberOfIssues(issuesList.size());
		if(issuesList.size() > 0)
		{
			validateMedActivityScore.setDescription(ApplicationConstants.IMMU_NOTIN_MED_DESC);
			validateMedActivityScore.getIgReferences().add(ApplicationConstants.IG_SECTION_REFERENCES);
			validateMedActivityScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_URL);
		}
		return validateMedActivityScore;
	}
	
	public CCDAScoreCardRubrics getNarrativeStructureIdScore(CCDAMedication medications)
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
		}
		
		if(maxPoints==0)
		{
			maxPoints = 1;
			actualPoints = 1;
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
