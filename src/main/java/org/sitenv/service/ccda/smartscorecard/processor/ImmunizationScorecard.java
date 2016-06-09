package org.sitenv.service.ccda.smartscorecard.processor;

import java.util.ArrayList;
import java.util.List;

import org.sitenv.ccdaparsing.model.CCDACode;
import org.sitenv.ccdaparsing.model.CCDAImmunization;
import org.sitenv.ccdaparsing.model.CCDAImmunizationActivity;
import org.sitenv.ccdaparsing.model.CCDAXmlSnippet;
import org.sitenv.service.ccda.smartscorecard.model.CCDAScoreCardRubrics;
import org.sitenv.service.ccda.smartscorecard.model.Category;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationConstants;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationUtil;
import org.springframework.stereotype.Service;

@Service
public class ImmunizationScorecard {
	
	public Category getImmunizationCategory(CCDAImmunization immunizatons, String birthDate)
	{
		
		Category immunizationCategory = new Category();
		immunizationCategory.setCategoryName("Immunizations");
		
		List<CCDAScoreCardRubrics> immunizationScoreList = new ArrayList<CCDAScoreCardRubrics>();
		immunizationScoreList.add(getTimePrecisionScore(immunizatons));
		immunizationScoreList.add(getValidDateTimeScore(immunizatons,birthDate));
		immunizationScoreList.add(getValidDisplayNameScoreCard(immunizatons));
		
		immunizationCategory.setCategoryRubrics(immunizationScoreList);
		ApplicationUtil.calculateSectionGradeAndIssues(immunizationScoreList, immunizationCategory);
		
		return immunizationCategory;
		
	}
	
	public CCDAScoreCardRubrics getTimePrecisionScore(CCDAImmunization immunizatons)
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
						if(ApplicationUtil.validateDayFormat(immunizationActivity.getTime().getValue()) ||
								ApplicationUtil.validateMonthFormat(immunizationActivity.getTime().getValue()))
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
			timePrecisionScore.getIgReferences().add(ApplicationConstants.IG_SECTION_REFERENCES);
			timePrecisionScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_URL);
		}
		return timePrecisionScore;
	}
	
	
	public CCDAScoreCardRubrics getValidDateTimeScore(CCDAImmunization immunizatons, String birthDate)
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
					maxPoints++;
					if(immunizationActivity.getTime() != null)
					{
						if(ApplicationUtil.checkDateRange(birthDate, immunizationActivity.getTime().getValue()))
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
	
	
	public CCDAScoreCardRubrics getValidDisplayNameScoreCard(CCDAImmunization immunizatons)
	{
		CCDAScoreCardRubrics validateDisplayNameScore = new CCDAScoreCardRubrics();
		validateDisplayNameScore.setRule(ApplicationConstants.CODE_DISPLAYNAME_REQUIREMENT);
		
		int maxPoints = 0;
		int actualPoints = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue= null;
		if(immunizatons != null)
		{
			maxPoints++;
			if(immunizatons.getSectionCode()!= null)
			{
				if(ApplicationUtil.validateDisplayName(immunizatons.getSectionCode().getCode(), 
						ApplicationConstants.CODE_SYSTEM_MAP.get(immunizatons.getSectionCode().getCodeSystem()),
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
			else
			{
				issue = new CCDAXmlSnippet();
				issue.setLineNumber(immunizatons.getLineNumber());
				issue.setXmlString(immunizatons.getXmlString());
				issuesList.add(issue);
			}
			
			if(!ApplicationUtil.isEmpty(immunizatons.getImmActivity()))
			{
				for (CCDAImmunizationActivity immuActivity : immunizatons.getImmActivity())
				{
					maxPoints++;
					if(immuActivity.getApproachSiteCode() != null)
					{
						if(ApplicationUtil.validateDisplayName(immuActivity.getApproachSiteCode().getCode(), 
								ApplicationConstants.CODE_SYSTEM_MAP.get(immuActivity.getApproachSiteCode().getCodeSystem()),
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
					else
					{
						issue = new CCDAXmlSnippet();
						issue.setLineNumber(immuActivity.getLineNumber());
						issue.setXmlString(immuActivity.getXmlString());
						issuesList.add(issue);
					}
					
					if(immuActivity.getConsumable() != null)
					{
						if(!ApplicationUtil.isEmpty(immuActivity.getConsumable().getTranslations()))
						{
							for (CCDACode translationCode : immuActivity.getConsumable().getTranslations())
							{
								maxPoints++;
								if(ApplicationUtil.validateDisplayName(translationCode.getCode(), 
													ApplicationConstants.CODE_SYSTEM_MAP.get(translationCode.getCodeSystem().toUpperCase()),
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
			issue.setLineNumber("Immunization section not present");
			issue.setXmlString("Immunization section not present");
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
}
