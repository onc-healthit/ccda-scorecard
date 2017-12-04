package org.sitenv.service.ccda.smartscorecard.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.sitenv.ccdaparsing.model.CCDAII;
import org.sitenv.ccdaparsing.model.CCDAVitalObs;
import org.sitenv.ccdaparsing.model.CCDAVitalOrg;
import org.sitenv.ccdaparsing.model.CCDAVitalSigns;
import org.sitenv.ccdaparsing.model.CCDAXmlSnippet;
import org.sitenv.service.ccda.smartscorecard.cofiguration.SectionRule;
import org.sitenv.service.ccda.smartscorecard.model.CCDAScoreCardRubrics;
import org.sitenv.service.ccda.smartscorecard.model.Category;
import org.sitenv.service.ccda.smartscorecard.model.PatientDetails;
import org.sitenv.service.ccda.smartscorecard.repositories.inmemory.VitalsRepository;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationConstants;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

@Service
public class VitalsScorecard {
	
	private static final Logger logger = Logger.getLogger(VitalsScorecard.class);
	
	@Autowired
	VitalsRepository vitalsRepository;
	
	@Autowired
	TemplateIdProcessor templateIdProcessor;
	
	@Autowired
	ReferenceValidatorService referenceValidatorService;
	
	@Async()
	public Future<Category> getVitalsCategory(CCDAVitalSigns vitals, PatientDetails patientDetails,String ccdaVersion,List<SectionRule> sectionRules)
	{
		long startTime = System.currentTimeMillis();
		logger.info("Vitals Start time:"+ startTime);
		if(vitals==null || vitals.isSectionNullFlavourWithNI())
		{
			return new AsyncResult<Category>(new Category(ApplicationConstants.CATEGORIES.VITALS.getCategoryDesc(),true));
		}
		Category vitalsCategory = new Category();
		vitalsCategory.setCategoryName(ApplicationConstants.CATEGORIES.VITALS.getCategoryDesc());
		
		List<CCDAScoreCardRubrics> vitalsScoreList = new ArrayList<CCDAScoreCardRubrics>();
		
		if (sectionRules== null || ApplicationUtil.isRuleEnabled(sectionRules, ApplicationConstants.RULE_IDS.V1)) {
			vitalsScoreList.add(getTimePrecisionScore(vitals, ccdaVersion));
		}
		if (sectionRules== null || ApplicationUtil.isRuleEnabled(sectionRules, ApplicationConstants.RULE_IDS.V2)) {
			vitalsScoreList.add(getValidDateTimeScore(vitals, patientDetails, ccdaVersion));
		}
		if (sectionRules== null || ApplicationUtil.isRuleEnabled(sectionRules, ApplicationConstants.RULE_IDS.V3)) {
			vitalsScoreList.add(getValidDisplayNameScoreCard(vitals, ccdaVersion));
		}
		if (sectionRules== null || ApplicationUtil.isRuleEnabled(sectionRules, ApplicationConstants.RULE_IDS.V4)) {
			vitalsScoreList.add(getValidLoincCodesScore(vitals, ccdaVersion));
		}
		if (sectionRules== null || ApplicationUtil.isRuleEnabled(sectionRules, ApplicationConstants.RULE_IDS.V5)) {
			vitalsScoreList.add(getValidUCUMScore(vitals, ccdaVersion));
		}
		if (sectionRules== null || ApplicationUtil.isRuleEnabled(sectionRules, ApplicationConstants.RULE_IDS.V6)) {
			vitalsScoreList.add(getApprEffectivetimeScore(vitals, ccdaVersion));
		}
		if (sectionRules== null || ApplicationUtil.isRuleEnabled(sectionRules, ApplicationConstants.RULE_IDS.V7)) {
			vitalsScoreList.add(getNarrativeStructureIdScore(vitals, ccdaVersion));
		}
		if (sectionRules== null || ApplicationUtil.isRuleEnabled(sectionRules, ApplicationConstants.RULE_IDS.V8)) {
			vitalsScoreList.add(getTemplateIdScore(vitals, ccdaVersion));
		}
		
		vitalsCategory.setCategoryRubrics(vitalsScoreList);
		ApplicationUtil.calculateSectionGradeAndIssues(vitalsScoreList, vitalsCategory);
		logger.info("Vitals End time:"+ (System.currentTimeMillis() - startTime));
		return new AsyncResult<Category>(vitalsCategory);
		
	}
	
	
	public CCDAScoreCardRubrics getTimePrecisionScore(CCDAVitalSigns vitals,String ccdaVersion)
	{
		CCDAScoreCardRubrics timePrecisionScore = new CCDAScoreCardRubrics();
		timePrecisionScore.setRule(ApplicationConstants.TIME_PRECISION_REQUIREMENT);
		
		int actualPoints = 0;
		int maxPoints = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue= null;
		if(vitals != null)
		{
			if(!ApplicationUtil.isEmpty(vitals.getVitalsOrg()))
			{
				for (CCDAVitalOrg vitalOrg : vitals.getVitalsOrg())
				{
					maxPoints++;
					if(vitalOrg.getEffTime() != null)
					{
						if(ApplicationUtil.validateDayFormat(vitalOrg.getEffTime()) ||
								ApplicationUtil.validateMinuteFormat(vitalOrg.getEffTime()) ||
								ApplicationUtil.validateSecondFormat(vitalOrg.getEffTime()))
						{
							actualPoints++;
						}
						else
						{
							issue = new CCDAXmlSnippet();
							issue.setLineNumber(vitalOrg.getEffTime().getLineNumber());
							issue.setXmlString(vitalOrg.getEffTime().getXmlString());
							issuesList.add(issue);
						}
					}
					else
					{
						issue = new CCDAXmlSnippet();
						issue.setLineNumber(vitalOrg.getLineNumber());
						issue.setXmlString(vitalOrg.getXmlString());
						issuesList.add(issue);
					}
					
					if(!ApplicationUtil.isEmpty(vitalOrg.getVitalObs()))
					{
						for (CCDAVitalObs vitalObs : vitalOrg.getVitalObs() )
						{
							maxPoints++;
							if(vitalObs.getMeasurementTime() != null)
							{
								if(ApplicationUtil.validateDayFormat(vitalObs.getMeasurementTime())||
										ApplicationUtil.validateMinuteFormat(vitalObs.getMeasurementTime()) ||
										ApplicationUtil.validateSecondFormat(vitalObs.getMeasurementTime()))
								{
									actualPoints++;
								}
								else
								{
									issue = new CCDAXmlSnippet();
									issue.setLineNumber(vitalObs.getMeasurementTime().getLineNumber());
									issue.setXmlString(vitalObs.getMeasurementTime().getXmlString());
									issuesList.add(issue);
								}
							}
							else
							{
								issue = new CCDAXmlSnippet();
								issue.setLineNumber(vitalObs.getLineNumber());
								issue.setXmlString(vitalObs.getXmlString());
								issuesList.add(issue);
							}
						}
					}
				}
			}
			else
			{
				issue = new CCDAXmlSnippet();
				issue.setLineNumber(vitals.getLineNumber());
				issue.setXmlString(vitals.getXmlString());
				issuesList.add(issue);
			}
		}
		else
		{
			issue = new CCDAXmlSnippet();
			issue.setLineNumber("Vitals section not present");
			issue.setXmlString("Vitals section not present");
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
			if(ccdaVersion.equals("") || ccdaVersion.equals(ApplicationConstants.CCDAVersion.R21.getVersion()))
			{
				timePrecisionScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES.VITAL_SIGN_ORGANIZER.getIgReference());
			}else if (ccdaVersion.equals(ApplicationConstants.CCDAVersion.R11.getVersion()))
			{
				timePrecisionScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES_R1.VITAL_SIGN_ORGANIZER.getIgReference());
			}
			timePrecisionScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_LINKS.VITALSIGNS.getTaskforceLink());
		}
		return timePrecisionScore;
	}
	
	
	public CCDAScoreCardRubrics getValidDateTimeScore(CCDAVitalSigns vitals, PatientDetails patientDetails,String ccdaVersion)
	{
		CCDAScoreCardRubrics validateTimeScore = new CCDAScoreCardRubrics();
		validateTimeScore.setRule(ApplicationConstants.TIME_VALID_REQUIREMENT);
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue= null;
		int actualPoints = 0;
		int maxPoints = 0;
		
		if(vitals != null)
		{
			if(!ApplicationUtil.isEmpty(vitals.getVitalsOrg()))
			{
				for (CCDAVitalOrg vitalOrg : vitals.getVitalsOrg())
				{
					if(vitalOrg.getEffTime() != null && ApplicationUtil.isEffectiveTimePresent(vitalOrg.getEffTime()))
					{
						maxPoints++;
						if(ApplicationUtil.checkDateRange(patientDetails, vitalOrg.getEffTime()))
						{
							actualPoints++;
						}
						else
						{
							issue = new CCDAXmlSnippet();
							issue.setLineNumber(vitalOrg.getEffTime().getLineNumber());
							issue.setXmlString(vitalOrg.getEffTime().getXmlString());
							issuesList.add(issue);
						}
					}
					
					if(!ApplicationUtil.isEmpty(vitalOrg.getVitalObs()))
					{
						for (CCDAVitalObs vitalObs : vitalOrg.getVitalObs() )
						{
							if(vitalObs.getMeasurementTime() != null && ApplicationUtil.isEffectiveTimePresent(vitalObs.getMeasurementTime()))
							{
								maxPoints++;
								if(ApplicationUtil.checkDateRange(patientDetails, vitalObs.getMeasurementTime()))
								{
									actualPoints++;
								}
								else
								{
									issue = new CCDAXmlSnippet();
									issue.setLineNumber(vitalObs.getMeasurementTime().getLineNumber());
									issue.setXmlString(vitalObs.getMeasurementTime().getXmlString());
									issuesList.add(issue);
								}
							}
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
			if(ccdaVersion.equals("") || ccdaVersion.equals(ApplicationConstants.CCDAVersion.R21.getVersion()))
			{
				validateTimeScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES.VITAL_SIGN_ORGANIZER.getIgReference());
			}
			else if (ccdaVersion.equals(ApplicationConstants.CCDAVersion.R11.getVersion()))
			{
				validateTimeScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES_R1.VITAL_SIGN_ORGANIZER.getIgReference());
			}
			validateTimeScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_LINKS.VITALSIGNS.getTaskforceLink());
		}
		return validateTimeScore;
	}
	
	public CCDAScoreCardRubrics getValidDisplayNameScoreCard(CCDAVitalSigns vitals,String ccdaVersion)
	{
		CCDAScoreCardRubrics validateDisplayNameScore = new CCDAScoreCardRubrics();
		validateDisplayNameScore.setRule(ApplicationConstants.CODE_DISPLAYNAME_REQUIREMENT);
		
		int maxPoints = 0;
		int actualPoints = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue= null;
		if(vitals != null)
		{
			if(vitals.getSectionCode()!= null && !ApplicationUtil.isEmpty(vitals.getSectionCode().getDisplayName())
												&& ApplicationUtil.isCodeSystemAvailable(vitals.getSectionCode().getCodeSystem()))
			{
				maxPoints++;
				if(referenceValidatorService.validateDisplayName(vitals.getSectionCode().getCode(), 
														vitals.getSectionCode().getCodeSystem(),
														vitals.getSectionCode().getDisplayName()))
				{
					actualPoints++;
				}
				else
				{
					issue = new CCDAXmlSnippet();
					issue.setLineNumber(vitals.getSectionCode().getLineNumber());
					issue.setXmlString(vitals.getSectionCode().getXmlString());
					issuesList.add(issue);
				}
			}
			if(!ApplicationUtil.isEmpty(vitals.getVitalsOrg()))
			{
				for (CCDAVitalOrg vitalsOrg : vitals.getVitalsOrg())
				{
					if(vitalsOrg.getOrgCode() != null && !ApplicationUtil.isEmpty(vitalsOrg.getOrgCode().getDisplayName())
							&& ApplicationUtil.isCodeSystemAvailable(vitalsOrg.getOrgCode().getCodeSystem()))
					{
						maxPoints++;
						if(referenceValidatorService.validateDisplayName(vitalsOrg.getOrgCode().getCode(), 
																vitalsOrg.getOrgCode().getCodeSystem(),
																		vitalsOrg.getOrgCode().getDisplayName()))
						{
							actualPoints++;
						}
						else
						{
							issue = new CCDAXmlSnippet();
							issue.setLineNumber(vitalsOrg.getOrgCode().getLineNumber());
							issue.setXmlString(vitalsOrg.getOrgCode().getXmlString());
							issuesList.add(issue);
						}
					}
					
					if(!ApplicationUtil.isEmpty(vitalsOrg.getVitalObs()))
					{
						for(CCDAVitalObs vitalsObs : vitalsOrg.getVitalObs())
						{
							if(vitalsObs.getVsCode() != null && !ApplicationUtil.isEmpty(vitalsObs.getVsCode().getDisplayName())
									&& ApplicationUtil.isCodeSystemAvailable(vitalsObs.getVsCode().getCodeSystem()))
							{
								maxPoints++;
								if(referenceValidatorService.validateDisplayName(vitalsObs.getVsCode().getCode(), 
																	vitalsObs.getVsCode().getCodeSystem(),
																	vitalsObs.getVsCode().getDisplayName()))
								{
									actualPoints++;
								}
								else
								{
									issue = new CCDAXmlSnippet();
									issue.setLineNumber(vitalsObs.getVsCode().getLineNumber());
									issue.setXmlString(vitalsObs.getVsCode().getXmlString());
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
			if(ccdaVersion.equals("") || ccdaVersion.equals(ApplicationConstants.CCDAVersion.R21.getVersion()))
			{
				validateDisplayNameScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES.VITAL_SIGN_SECTION.getIgReference());
			}
			else if (ccdaVersion.equals(ApplicationConstants.CCDAVersion.R11.getVersion()))
			{
				validateDisplayNameScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES_R1.VITAL_SIGN_SECTION.getIgReference());
			}
			validateDisplayNameScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_LINKS.VITALSIGNS.getTaskforceLink());
		}
		return validateDisplayNameScore;
	}
	
	public CCDAScoreCardRubrics getValidUCUMScore(CCDAVitalSigns vitals,String ccdaVersion)
	{
		CCDAScoreCardRubrics validateUCUMScore = new CCDAScoreCardRubrics();
		validateUCUMScore.setRule(ApplicationConstants.VITAL_UCUM_REQUIREMENT);
		
		int maxPoints = 0;
		int actualPoints = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue= null;
		if(vitals != null)
		{
			if(!ApplicationUtil.isEmpty(vitals.getVitalsOrg()))
			{
				for(CCDAVitalOrg vitalOrg : vitals.getVitalsOrg())
				{
					if(!ApplicationUtil.isEmpty(vitalOrg.getVitalObs()))
					{
						for(CCDAVitalObs vitalObs : vitalOrg.getVitalObs())
						{
							maxPoints++;
							if(vitalObs.getVsResult() != null && vitalObs.getVsResult().getXsiType().equalsIgnoreCase("PQ"))
							{
								if(vitalObs.getVsCode()!= null)
								{
									if(vitalsRepository.isUCUMCodeValidForVitalCode(vitalObs.getVsCode().getCode(),vitalObs.getVsResult().getUnits()))
									{
										actualPoints++;
									}
									else
									{
										issue = new CCDAXmlSnippet();
										issue.setLineNumber(vitalObs.getVsResult().getLineNumber());
										issue.setXmlString(vitalObs.getVsResult().getXmlString());
										issuesList.add(issue);
									}
								}
								else 
								{
									issue = new CCDAXmlSnippet();
									issue.setLineNumber(vitalObs.getVsResult().getLineNumber());
									issue.setXmlString(vitalObs.getVsResult().getXmlString());
									issuesList.add(issue);
								}
							}
							else if(vitalObs.getVsResult() == null)
							{
								maxPoints++;
								issue = new CCDAXmlSnippet();
								issue.setLineNumber(vitalObs.getLineNumber());
								issue.setXmlString(vitalObs.getXmlString());
								issuesList.add(issue);
							}
						}
					}
				}
			}
			else
			{
				issue = new CCDAXmlSnippet();
				issue.setLineNumber(vitals.getLineNumber());
				issue.setXmlString(vitals.getXmlString());
				issuesList.add(issue);
			}
		}
		else
		{
			issue = new CCDAXmlSnippet();
			issue.setLineNumber("Vitals section not present");
			issue.setXmlString("Vitals section not present");
			issuesList.add(issue);
		}
		
		validateUCUMScore.setActualPoints(actualPoints);
		validateUCUMScore.setMaxPoints(maxPoints);
		validateUCUMScore.setRubricScore(ApplicationUtil.calculateRubricScore(maxPoints, actualPoints));
		validateUCUMScore.setIssuesList(issuesList);
		validateUCUMScore.setNumberOfIssues(issuesList.size());
		if(issuesList.size() > 0)
		{
			validateUCUMScore.setDescription(ApplicationConstants.VITAL_UCUM_DESCRIPTION);
			if(ccdaVersion.equals("") || ccdaVersion.equals(ApplicationConstants.CCDAVersion.R21.getVersion()))
			{
				validateUCUMScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES.VITAL_SIGN_OBSERVATION.getIgReference());
			}
			else if (ccdaVersion.equals(ApplicationConstants.CCDAVersion.R11.getVersion()))
			{
				validateUCUMScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES_R1.VITAL_SIGN_OBSERVATION.getIgReference());
			}
			validateUCUMScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_LINKS.VITALSIGNS.getTaskforceLink());
		}
		return validateUCUMScore;
	}
	
	public CCDAScoreCardRubrics getValidLoincCodesScore(CCDAVitalSigns vitals,String ccdaVersion)
	{
		CCDAScoreCardRubrics validatLoincCodeScore = new CCDAScoreCardRubrics();
		validatLoincCodeScore.setRule(ApplicationConstants.VITAL_LOINC_REQUIREMENT);
		
		int maxPoints = 0;
		int actualPoints = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue= null;
		if(vitals != null)
		{
			if( !ApplicationUtil.isEmpty(vitals.getVitalsOrg()))
			{
				for(CCDAVitalOrg vitalOrg : vitals.getVitalsOrg())
				{
				   if(!ApplicationUtil.isEmpty(vitalOrg.getVitalObs()))
				   {
					   for(CCDAVitalObs vitalObs : vitalOrg.getVitalObs())
					   {
						   maxPoints++;
						   if(vitalObs.getVsCode() != null)
						   {
							   if(referenceValidatorService.validateCodeForValueset(vitalObs.getVsCode().getCode(), ApplicationConstants.HITSP_VITAL_VALUESET_OID))
							   {
								   actualPoints++;
							   }
							   else 
							   {
								   issue = new CCDAXmlSnippet();
								   issue.setLineNumber(vitalObs.getVsCode().getLineNumber());
								   issue.setXmlString(vitalObs.getVsCode().getXmlString());
								   issuesList.add(issue);
							   }
						   }
						   else 
						   {
							   issue = new CCDAXmlSnippet();
							   issue.setLineNumber(vitalObs.getLineNumber());
							   issue.setXmlString(vitalObs.getXmlString());
							   issuesList.add(issue);
						   }
					   }
				   }
				}
			}
			else
			{
				issue = new CCDAXmlSnippet();
				issue.setLineNumber(vitals.getLineNumber());
				issue.setXmlString(vitals.getXmlString());
				issuesList.add(issue);
			}
		}
		else
		{
			issue = new CCDAXmlSnippet();
			issue.setLineNumber("Vitals section not present");
			issue.setXmlString("Vitals section not present");
			issuesList.add(issue);
		}
		
		validatLoincCodeScore.setActualPoints(actualPoints);
		validatLoincCodeScore.setMaxPoints(maxPoints);
		validatLoincCodeScore.setRubricScore(ApplicationUtil.calculateRubricScore(maxPoints, actualPoints));
		validatLoincCodeScore.setIssuesList(issuesList);
		validatLoincCodeScore.setNumberOfIssues(issuesList.size());
		if(issuesList.size() > 0)
		{
			validatLoincCodeScore.setDescription(ApplicationConstants.VITAL_LOINC_DESCRIPTION);
			if(ccdaVersion.equals("") || ccdaVersion.equals(ApplicationConstants.CCDAVersion.R21.getVersion()))
			{
				validatLoincCodeScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES.VITAL_SIGN_OBSERVATION.getIgReference());
			}
			else if (ccdaVersion.equals(ApplicationConstants.CCDAVersion.R11.getVersion()))
			{
				validatLoincCodeScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES_R1.VITAL_SIGN_OBSERVATION.getIgReference());
			}
			validatLoincCodeScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_LINKS.VITALSIGNS.getTaskforceLink());
		}
		return validatLoincCodeScore;
		
	}
	
	public CCDAScoreCardRubrics getApprEffectivetimeScore(CCDAVitalSigns vitals, String ccdaVersion)
	{
		CCDAScoreCardRubrics validateApprEffectiveTimeScore = new CCDAScoreCardRubrics();
		validateApprEffectiveTimeScore.setRule(ApplicationConstants.VITAL_AAPR_DATE_REQUIREMENT);
		
		int maxPoints = 0;
		int actualPoints = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue= null;
		if(vitals != null)
		{
			if(!ApplicationUtil.isEmpty(vitals.getVitalsOrg()))
			{
				for(CCDAVitalOrg vitalOrg : vitals.getVitalsOrg())
				{
					if(vitalOrg.getEffTime()!= null)
					{
						if(!ApplicationUtil.isEmpty(vitalOrg.getVitalObs()))
						{
							for(CCDAVitalObs vitalObs : vitalOrg.getVitalObs())
							{
								if(vitalObs.getMeasurementTime() != null && ApplicationUtil.isEffectiveTimePresent(vitalObs.getMeasurementTime()))
								{
									maxPoints++;
									if(ApplicationUtil.checkDateRange(vitalObs.getMeasurementTime(), vitalOrg.getEffTime()))
									{
										actualPoints++;
									}
									else 
									{
										issue = new CCDAXmlSnippet();
										issue.setLineNumber(vitalObs.getMeasurementTime().getLineNumber());
										issue.setXmlString(vitalObs.getMeasurementTime().getXmlString());
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
			maxPoints =1;
			actualPoints =1;
		}
		
		validateApprEffectiveTimeScore.setActualPoints(actualPoints);
		validateApprEffectiveTimeScore.setMaxPoints(maxPoints);
		validateApprEffectiveTimeScore.setRubricScore(ApplicationUtil.calculateRubricScore(maxPoints, actualPoints));
		validateApprEffectiveTimeScore.setIssuesList(issuesList);
		validateApprEffectiveTimeScore.setNumberOfIssues(issuesList.size());
		if(issuesList.size() > 0)
		{
			validateApprEffectiveTimeScore.setDescription(ApplicationConstants.VITAL_AAPR_DATE_DESCRIPTION);
			if(ccdaVersion.equals("") || ccdaVersion.equals(ApplicationConstants.CCDAVersion.R21.getVersion()))
			{
				validateApprEffectiveTimeScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES.VITAL_SIGN_ORGANIZER.getIgReference());
			}
			else if (ccdaVersion.equals(ApplicationConstants.CCDAVersion.R11.getVersion()))
			{
				validateApprEffectiveTimeScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES_R1.VITAL_SIGN_ORGANIZER.getIgReference());
			}
			validateApprEffectiveTimeScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_LINKS.VITALSIGNS.getTaskforceLink());
		}
		return validateApprEffectiveTimeScore;
	}
	
	public CCDAScoreCardRubrics getNarrativeStructureIdScore(CCDAVitalSigns vitals,String ccdaVersion)
	{
		CCDAScoreCardRubrics narrativeTextIdScore = new CCDAScoreCardRubrics();
		narrativeTextIdScore.setRule(ApplicationConstants.NARRATIVE_STRUCTURE_ID_REQ);
		
		int maxPoints = 0;
		int actualPoints = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue= null;
		if(vitals != null)
		{
			if(!ApplicationUtil.isEmpty(vitals.getVitalsOrg()))
			{
				for(CCDAVitalOrg vitalOrg : vitals.getVitalsOrg())
				{
					if(vitalOrg.getVitalObs()!=null)
					{
						for(CCDAVitalObs vitalObs : vitalOrg.getVitalObs())
						{
							maxPoints++;
							if(vitalObs.getReferenceText()!= null)
							{
								if(vitals.getReferenceLinks()!= null && vitals.getReferenceLinks().contains(vitalObs.getReferenceText().getValue()))
								{
									actualPoints++;
								}
								else
								{
									issue = new CCDAXmlSnippet();
									issue.setLineNumber(vitalObs.getReferenceText().getLineNumber());
									issue.setXmlString(vitalObs.getReferenceText().getXmlString());
									issuesList.add(issue);
								}
							}
							else
							{
								issue = new CCDAXmlSnippet();
								issue.setLineNumber(vitalObs.getLineNumber());
								issue.setXmlString(vitalObs.getXmlString());
								issuesList.add(issue);
							}
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
		
		
		narrativeTextIdScore.setActualPoints(actualPoints);
		narrativeTextIdScore.setMaxPoints(maxPoints);
		narrativeTextIdScore.setRubricScore(ApplicationUtil.calculateRubricScore(maxPoints, actualPoints));
		narrativeTextIdScore.setIssuesList(issuesList);
		narrativeTextIdScore.setNumberOfIssues(issuesList.size());
		if(issuesList.size() > 0)
		{
			narrativeTextIdScore.setDescription(ApplicationConstants.NARRATIVE_STRUCTURE_ID_DESC);
			if(ccdaVersion.equals("") || ccdaVersion.equals(ApplicationConstants.CCDAVersion.R21.getVersion()))
			{
				narrativeTextIdScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES.VITAL_SIGN_SECTION.getIgReference());
			}
			else if (ccdaVersion.equals(ApplicationConstants.CCDAVersion.R11.getVersion()))
			{
				narrativeTextIdScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES_R1.VITAL_SIGN_SECTION.getIgReference());
			}
			narrativeTextIdScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_LINKS.VITALSIGNS.getTaskforceLink());
		}
		
		return narrativeTextIdScore;
	}
	
	public CCDAScoreCardRubrics getTemplateIdScore(CCDAVitalSigns vitals, String ccdaVersion)
	{
		CCDAScoreCardRubrics templateIdScore = new CCDAScoreCardRubrics();
		templateIdScore.setRule(ApplicationConstants.TEMPLATEID_DESC);
		
		int maxPoints = 0;
		int actualPoints = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		
		if(vitals!= null)
		{
			if(!ApplicationUtil.isEmpty(vitals.getTemplateIds()))
			{
				for (CCDAII templateId : vitals.getTemplateIds())
				{
					maxPoints = maxPoints++;
					templateIdProcessor.scoreTemplateId(templateId,actualPoints,issuesList,ccdaVersion);
				}
			}
			
			if(!ApplicationUtil.isEmpty(vitals.getVitalsOrg()))
			{
				for(CCDAVitalOrg vitalOrg :vitals.getVitalsOrg())
				{
					if(!ApplicationUtil.isEmpty(vitalOrg.getTemplateIds()))
					{
						for (CCDAII templateId : vitalOrg.getTemplateIds())
						{
							maxPoints = maxPoints++;
							templateIdProcessor.scoreTemplateId(templateId,actualPoints,issuesList,ccdaVersion);
						}
					}
					
					if(!ApplicationUtil.isEmpty(vitalOrg.getVitalObs()))
					{
						for(CCDAVitalObs vitalObs : vitalOrg.getVitalObs())
						{
							if(!ApplicationUtil.isEmpty(vitalObs.getTemplateIds()))
							{
								for (CCDAII templateId : vitalObs.getTemplateIds())
								{
									maxPoints = maxPoints++;
									templateIdProcessor.scoreTemplateId(templateId,actualPoints,issuesList,ccdaVersion);
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
