package org.sitenv.service.ccda.smartscorecard.processor;

import java.util.ArrayList;
import java.util.List;

import org.sitenv.ccdaparsing.model.CCDADataElement;
import org.sitenv.ccdaparsing.model.CCDAII;
import org.sitenv.ccdaparsing.model.CCDAProcActProc;
import org.sitenv.ccdaparsing.model.CCDAProcedure;
import org.sitenv.ccdaparsing.model.CCDAServiceDeliveryLoc;
import org.sitenv.ccdaparsing.model.CCDAXmlSnippet;
import org.sitenv.service.ccda.smartscorecard.model.CCDAScoreCardRubrics;
import org.sitenv.service.ccda.smartscorecard.model.Category;
import org.sitenv.service.ccda.smartscorecard.model.PatientDetails;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationConstants;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProceduresScorecard {
	
	@Autowired
	TemplateIdProcessor templateIdProcessor;
	
	public Category getProceduresCategory(CCDAProcedure procedures, PatientDetails patientDetails,String docType)
	{
		if(procedures==null || procedures.isSectionNullFlavourWithNI())
		{
			return new Category(ApplicationConstants.CATEGORIES.PROCEDURES.getCategoryDesc(),true);
		}
		Category procedureCategory = new Category();
		procedureCategory.setCategoryName(ApplicationConstants.CATEGORIES.PROCEDURES.getCategoryDesc());
		
		List<CCDAScoreCardRubrics> procedureScoreList = new ArrayList<CCDAScoreCardRubrics>();
		procedureScoreList.add(getValidDisplayNameScoreCard(procedures,docType));
		procedureScoreList.add(getNarrativeStructureIdScore(procedures,docType));
		procedureScoreList.add(getTemplateIdScore(procedures, docType));
		
		procedureCategory.setCategoryRubrics(procedureScoreList);
		ApplicationUtil.calculateSectionGradeAndIssues(procedureScoreList, procedureCategory);
		return procedureCategory;
	}
	
	
	
	public CCDAScoreCardRubrics getValidDisplayNameScoreCard(CCDAProcedure procedures,String docType)
	{
		CCDAScoreCardRubrics validateDisplayNameScore = new CCDAScoreCardRubrics();
		validateDisplayNameScore.setRule(ApplicationConstants.CODE_DISPLAYNAME_REQUIREMENT);
		
		int maxPoints = 0;
		int actualPoints = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue= null;
		if(procedures != null)
		{
			if(procedures.getSectionCode()!= null && !ApplicationUtil.isEmpty(procedures.getSectionCode().getDisplayName())
												&& ApplicationUtil.isCodeSystemAvailable(procedures.getSectionCode().getCodeSystem()))
			{
				maxPoints++;
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
			
			if(!ApplicationUtil.isEmpty(procedures.getProcActsProcs()))
			{
				for (CCDAProcActProc procAct : procedures.getProcActsProcs())
				{
					if(procAct.getProcCode() != null && !ApplicationUtil.isEmpty(procAct.getProcCode().getDisplayName())
							&& ApplicationUtil.isCodeSystemAvailable(procAct.getProcCode().getCodeSystem()))
					{
						maxPoints++;
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
				}
			}
		}
		if(maxPoints==0)
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
				validateDisplayNameScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES.PROCEDURE_SECTION.getIgReference());
			}
			else if (docType.equalsIgnoreCase("R1.1"))
			{
				validateDisplayNameScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES_R1.PROCEDURE_SECTION.getIgReference());
			}
			validateDisplayNameScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_LINKS.PROCEDURES.getTaskforceLink());
		}
		return validateDisplayNameScore;
	}
	
	
	public CCDAScoreCardRubrics getNarrativeStructureIdScore(CCDAProcedure procedures,String docType)
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
			if(docType.equalsIgnoreCase("") || docType.equalsIgnoreCase("R2.1"))
			{
				narrativeTextIdScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES.PROCEDURE_SECTION.getIgReference());
			}
			else if(docType.equalsIgnoreCase("R1.1"))
			{
				narrativeTextIdScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES_R1.PROCEDURE_SECTION.getIgReference());
			}
			narrativeTextIdScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_LINKS.PROCEDURES.getTaskforceLink());
		}
		
		return narrativeTextIdScore;
	}
	
	
	public CCDAScoreCardRubrics getTemplateIdScore(CCDAProcedure procedures,String docType)
	{
		CCDAScoreCardRubrics templateIdScore = new CCDAScoreCardRubrics();
		templateIdScore.setRule(ApplicationConstants.TEMPLATEID_DESC);
		
		int maxPoints = 0;
		int actualPoints = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		
		if(procedures!=null)
		{
			if(!ApplicationUtil.isEmpty(procedures.getSectionTemplateId()))
			{
				for (CCDAII templateId : procedures.getSectionTemplateId())
				{
					maxPoints = maxPoints++;
					templateIdProcessor.scoreTemplateId(templateId,actualPoints,issuesList,docType);
				}
			}
			
			if(!ApplicationUtil.isEmpty(procedures.getProcActsProcs()))
			{
				for(CCDAProcActProc procAct : procedures.getProcActsProcs())
				{
					if(!ApplicationUtil.isEmpty(procAct.getSectionTemplateId()))
					{
						for (CCDAII templateId : procAct.getSectionTemplateId())
						{
							maxPoints = maxPoints++;
							templateIdProcessor.scoreTemplateId(templateId,actualPoints,issuesList,docType);
						}
					}
					
					if(!ApplicationUtil.isEmpty(procAct.getSdLocs()))
					{
						for(CCDAServiceDeliveryLoc sdLoc : procAct.getSdLocs())
						{
							if(!ApplicationUtil.isEmpty(sdLoc.getTemplateId()))
							{
								for (CCDAII templateId : sdLoc.getTemplateId())
								{
									maxPoints = maxPoints++;
									templateIdProcessor.scoreTemplateId(templateId,actualPoints,issuesList,docType);
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
