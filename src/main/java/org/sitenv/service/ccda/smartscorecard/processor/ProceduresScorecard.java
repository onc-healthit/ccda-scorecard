package org.sitenv.service.ccda.smartscorecard.processor;

import java.util.ArrayList;
import java.util.List;

import org.sitenv.ccdaparsing.model.CCDADataElement;
import org.sitenv.ccdaparsing.model.CCDAProcActProc;
import org.sitenv.ccdaparsing.model.CCDAProcedure;
import org.sitenv.ccdaparsing.model.CCDAXmlSnippet;
import org.sitenv.service.ccda.smartscorecard.model.CCDAScoreCardRubrics;
import org.sitenv.service.ccda.smartscorecard.model.Category;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationConstants;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationUtil;
import org.springframework.stereotype.Service;

@Service
public class ProceduresScorecard {
	
	public Category getProceduresCategory(CCDAProcedure procedures, String birthDate)
	{
		
		Category procedureCategory = new Category();
		procedureCategory.setCategoryName(ApplicationConstants.CATEGORIES.PROCEDURES.getCategoryDesc());
		
		List<CCDAScoreCardRubrics> procedureScoreList = new ArrayList<CCDAScoreCardRubrics>();
		procedureScoreList.add(getValidDisplayNameScoreCard(procedures));
		procedureScoreList.add(getNarrativeStructureIdScore(procedures));
		
		procedureCategory.setCategoryRubrics(procedureScoreList);
		ApplicationUtil.calculateSectionGradeAndIssues(procedureScoreList, procedureCategory);
		return procedureCategory;
	}
	
	
	
	public CCDAScoreCardRubrics getValidDisplayNameScoreCard(CCDAProcedure procedures)
	{
		CCDAScoreCardRubrics validateDisplayNameScore = new CCDAScoreCardRubrics();
		validateDisplayNameScore.setRule(ApplicationConstants.CODE_DISPLAYNAME_REQUIREMENT);
		
		int maxPoints = 0;
		int actualPoints = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue= null;
		if(procedures != null)
		{
			maxPoints++;
			if(procedures.getSectionCode()!= null)
			{
				if(ApplicationUtil.validateDisplayName(procedures.getSectionCode().getCode(), 
									procedures.getSectionCode().getCodeSystem(),
									procedures.getSectionCode().getDisplayName()))
				{
					actualPoints++;
				}
				else
				{
					issue = new CCDAXmlSnippet();
					issue.setLineNumber(procedures.getSectionCode().getLineNumber());
					issue.setXmlString(procedures.getSectionCode().getXmlString());
					issuesList.add(issue);
				}
			}
			else
			{
				issue = new CCDAXmlSnippet();
				issue.setLineNumber(procedures.getLineNumber());
				issue.setXmlString(procedures.getXmlString());
				issuesList.add(issue);
			}
			
			if(!ApplicationUtil.isEmpty(procedures.getProcActsProcs()))
			{
				for (CCDAProcActProc procAct : procedures.getProcActsProcs())
				{
					maxPoints++;
					if(procAct.getProcCode() != null)
					{
						if(ApplicationUtil.validateDisplayName(procAct.getProcCode().getCode(), 
												procAct.getProcCode().getCodeSystem(),
												procAct.getProcCode().getDisplayName()))
						{
							actualPoints++;
						}
						else
						{
							issue = new CCDAXmlSnippet();
							issue.setLineNumber(procAct.getProcCode().getLineNumber());
							issue.setXmlString(procAct.getProcCode().getXmlString());
							issuesList.add(issue);
						}
					}
					else
					{
						issue = new CCDAXmlSnippet();
						issue.setLineNumber(procAct.getLineNumber());
						issue.setXmlString(procAct.getXmlString());
						issuesList.add(issue);
					}
				}
			}
		}
		else
		{
			issue = new CCDAXmlSnippet();
			issue.setLineNumber("Procedures section not present");
			issue.setXmlString("Procedures section not present");
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
	
	
	public CCDAScoreCardRubrics getNarrativeStructureIdScore(CCDAProcedure procedures)
	{
		CCDAScoreCardRubrics narrativeTextIdScore = new CCDAScoreCardRubrics();
		narrativeTextIdScore.setRule(ApplicationConstants.NARRATIVE_STRUCTURE_ID_REQ);
		
		int maxPoints = 0;
		int actualPoints = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue= null;
		if(procedures != null)
		{
			if(!ApplicationUtil.isEmpty(procedures.getProcActsProcs()))
			{
				for(CCDAProcActProc procAct : procedures.getProcActsProcs())
				{
					if(!ApplicationUtil.isEmpty(procAct.getReferenceTexts()))
					{
						for(CCDADataElement referenceText : procAct.getReferenceTexts())
						{
							maxPoints++;
							if(procedures.getReferenceLinks().contains(referenceText.getValue()))
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
			narrativeTextIdScore.getIgReferences().add(ApplicationConstants.IG_SECTION_REFERENCES);
			narrativeTextIdScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_URL);
		}
		
		return narrativeTextIdScore;
	}
}
