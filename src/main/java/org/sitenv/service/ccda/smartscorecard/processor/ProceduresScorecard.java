package org.sitenv.service.ccda.smartscorecard.processor;

import java.util.ArrayList;
import java.util.List;

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
									ApplicationConstants.CODE_SYSTEM_MAP.get(procedures.getSectionCode().getCodeSystem()),
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
												ApplicationConstants.CODE_SYSTEM_MAP.get(procAct.getProcCode().getCodeSystem()),
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
}
