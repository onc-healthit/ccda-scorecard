package org.sitenv.service.ccda.smartscorecard.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.sitenv.ccdaparsing.model.CCDAII;
import org.sitenv.ccdaparsing.model.CCDAProcActProc;
import org.sitenv.ccdaparsing.model.CCDAProcedure;
import org.sitenv.ccdaparsing.model.CCDAServiceDeliveryLoc;
import org.sitenv.ccdaparsing.model.CCDAXmlSnippet;
import org.sitenv.service.ccda.smartscorecard.configuration.SectionRule;
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
public class ProceduresScorecard {
	
	private static final Logger logger = Logger.getLogger(ProceduresScorecard.class);
	
	@Autowired
	TemplateIdProcessor templateIdProcessor;
	
	@Autowired
	ReferenceValidatorService referenceValidatorService;
	
	@Async()
	public Future<Category> getProceduresCategory(CCDAProcedure procedures, PatientDetails patientDetails,String ccdaVersion,List<SectionRule> sectionRules)
	{
		long startTime = System.currentTimeMillis();
		
		logger.info("Procedures Start time:"+ startTime);
		
		if(procedures==null || procedures.isSectionNullFlavourWithNI())
		{
			return new AsyncResult<Category>(new Category(ApplicationConstants.CATEGORIES.PROCEDURES.getCategoryDesc(),true));
		}
		Category procedureCategory = new Category();
		procedureCategory.setCategoryName(ApplicationConstants.CATEGORIES.PROCEDURES.getCategoryDesc());
		
		List<CCDAScoreCardRubrics> procedureScoreList = new ArrayList<CCDAScoreCardRubrics>();
		
		if (sectionRules==null || ApplicationUtil.isRuleEnabled(sectionRules, ApplicationConstants.RULE_IDS.O1)) {
			procedureScoreList.add(getValidDisplayNameScoreCard(procedures, ccdaVersion));
		}
		if (sectionRules==null || ApplicationUtil.isRuleEnabled(sectionRules, ApplicationConstants.RULE_IDS.O2)) {
			procedureScoreList.add(getNarrativeStructureIdScore(procedures, ccdaVersion));
		}
		if (sectionRules==null || ApplicationUtil.isRuleEnabled(sectionRules, ApplicationConstants.RULE_IDS.O3)) {
			procedureScoreList.add(getTemplateIdScore(procedures, ccdaVersion));
		}
		
		procedureCategory.setCategoryRubrics(procedureScoreList);
		ApplicationUtil.calculateSectionGradeAndIssues(procedureScoreList, procedureCategory);
		ApplicationUtil.calculateNumberOfChecksAndFailedRubrics(procedureScoreList, procedureCategory);
		logger.info("Procedures End time:"+ (System.currentTimeMillis() - startTime));
		return new AsyncResult<Category>(procedureCategory);
	}
	
	
	
	public CCDAScoreCardRubrics getValidDisplayNameScoreCard(CCDAProcedure procedures,String ccdaVersion)
	{
		CCDAScoreCardRubrics validateDisplayNameScore = new CCDAScoreCardRubrics();
		validateDisplayNameScore.setRule(ApplicationConstants.CODE_DISPLAYNAME_REQUIREMENT);
		
		int maxPoints = 0;
		int actualPoints = 0;
		int numberOfChecks = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue= null;
		if(procedures != null)
		{
			if(procedures.getSectionCode()!= null && !ApplicationUtil.isEmpty(procedures.getSectionCode().getDisplayName())
												&& ApplicationUtil.isCodeSystemAvailable(procedures.getSectionCode().getCodeSystem()))
			{
				maxPoints++;
				numberOfChecks++;
				if(referenceValidatorService.validateDisplayName(procedures.getSectionCode().getCode(), 
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
						numberOfChecks++;
						if(referenceValidatorService.validateDisplayName(procAct.getProcCode().getCode(), 
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
		validateDisplayNameScore.setNumberOfChecks(numberOfChecks);
		if(issuesList.size() > 0)
		{
			validateDisplayNameScore.setDescription(ApplicationConstants.CODE_DISPLAYNAME_DESCRIPTION);
			if(ccdaVersion.equals("") || ccdaVersion.equals(ApplicationConstants.CCDAVersion.R21.getVersion()))
			{
				validateDisplayNameScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES.PROCEDURE_SECTION.getIgReference());
			}
			else if (ccdaVersion.equals(ApplicationConstants.CCDAVersion.R11.getVersion()))
			{
				validateDisplayNameScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES_R1.PROCEDURE_SECTION.getIgReference());
			}
			validateDisplayNameScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_LINKS.PROCEDURES.getTaskforceLink());
		}
		return validateDisplayNameScore;
	}
	
	
	public CCDAScoreCardRubrics getNarrativeStructureIdScore(CCDAProcedure procedures,String ccdaVersion)
	{
		CCDAScoreCardRubrics narrativeTextIdScore = new CCDAScoreCardRubrics();
		narrativeTextIdScore.setRule(ApplicationConstants.NARRATIVE_STRUCTURE_ID_REQ);
		
		int maxPoints = 0;
		int actualPoints = 0;
		int numberOfChecks = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue= null;
		if(procedures != null)
		{
			if(!ApplicationUtil.isEmpty(procedures.getProcActsProcs()))
			{
				for(CCDAProcActProc procAct : procedures.getProcActsProcs())
				{
					maxPoints++;
					numberOfChecks++;
					if(procAct.getReferenceText()!= null)
					{
						if(procedures.getReferenceLinks()!= null && procedures.getReferenceLinks().contains(procAct.getReferenceText().getValue()))
						{
							actualPoints++;
						}
						else
						{
							issue = new CCDAXmlSnippet();
							issue.setLineNumber(procAct.getReferenceText().getLineNumber());
							issue.setXmlString(procAct.getReferenceText().getXmlString());
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
		narrativeTextIdScore.setNumberOfChecks(numberOfChecks);
		if(issuesList.size() > 0)
		{
			narrativeTextIdScore.setDescription(ApplicationConstants.NARRATIVE_STRUCTURE_ID_DESC);
			if(ccdaVersion.equals("") || ccdaVersion.equals(ApplicationConstants.CCDAVersion.R21.getVersion()))
			{
				narrativeTextIdScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES.PROCEDURE_SECTION.getIgReference());
			}
			else if(ccdaVersion.equals(ApplicationConstants.CCDAVersion.R11.getVersion()))
			{
				narrativeTextIdScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES_R1.PROCEDURE_SECTION.getIgReference());
			}
			narrativeTextIdScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_LINKS.PROCEDURES.getTaskforceLink());
		}
		
		return narrativeTextIdScore;
	}
	
	
	public CCDAScoreCardRubrics getTemplateIdScore(CCDAProcedure procedures,String ccdaVersion)
	{
		CCDAScoreCardRubrics templateIdScore = new CCDAScoreCardRubrics();
		templateIdScore.setRule(ApplicationConstants.TEMPLATEID_DESC);
		
		int maxPoints = 0;
		int actualPoints = 0;
		int numberOfChecks = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		
		if(procedures!=null)
		{
			if(!ApplicationUtil.isEmpty(procedures.getSectionTemplateId()))
			{
				for (CCDAII templateId : procedures.getSectionTemplateId())
				{
					maxPoints++;
					numberOfChecks++;
					actualPoints =  actualPoints + templateIdProcessor.scoreTemplateId(templateId, issuesList, ccdaVersion);
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
							maxPoints++;
							numberOfChecks++;
							actualPoints =  actualPoints + templateIdProcessor.scoreTemplateId(templateId, issuesList, ccdaVersion);
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
									maxPoints++;
									numberOfChecks++;
									actualPoints =  actualPoints + templateIdProcessor.scoreTemplateId(templateId, issuesList, ccdaVersion);
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
		templateIdScore.setNumberOfChecks(numberOfChecks);
		if(issuesList.size() > 0)
		{
			templateIdScore.setDescription(ApplicationConstants.TEMPLATEID_REQ);
			if(ccdaVersion.equals("") || ccdaVersion.equals(ApplicationConstants.CCDAVersion.R21.getVersion()))
			{
				templateIdScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES.ALLERGY_SECTION.getIgReference());
			}
			else if (ccdaVersion.equals(ApplicationConstants.CCDAVersion.R11.getVersion()))
			{
				templateIdScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES_R1.ALLERGY_SECTION.getIgReference());
			}
			templateIdScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_LINKS.ALLERGIES.getTaskforceLink());
		}
		
		return templateIdScore;
	}
}
