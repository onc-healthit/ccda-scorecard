package org.sitenv.service.ccda.smartscorecard.processor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.sitenv.ccdaparsing.model.CCDAPatient;
import org.sitenv.ccdaparsing.model.CCDAPatientName;
import org.sitenv.ccdaparsing.model.CCDAPatientNameElement;
import org.sitenv.ccdaparsing.model.CCDAXmlSnippet;
import org.sitenv.service.ccda.smartscorecard.configuration.SectionRule;
import org.sitenv.service.ccda.smartscorecard.model.CCDAScoreCardRubrics;
import org.sitenv.service.ccda.smartscorecard.model.Category;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationConstants;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationUtil;
import org.springframework.stereotype.Service;

@Service
public class PatientScorecard {
	
	public static final List<String> patientNameUseValues = new ArrayList<>(Arrays.asList("ABC","A","ASGN","IDE","I","L","C","PHON","P","R","SRCH","SNDX","SYL"));
	
	public static final List<String> patientNameElementQualifierValues = new ArrayList<>(Arrays.asList("AC","AD","BR","CL","IN","NB","PR","SP","TITLE","VV"));
	
	public Category getPatientCategory(CCDAPatient patient,String ccdaVersion,List<SectionRule> sectionRules)
	{
		
		Category patientCategory = new Category();
		patientCategory.setCategoryName(ApplicationConstants.CATEGORIES.PATIENT.getCategoryDesc());
		
		List<CCDAScoreCardRubrics> patientScoreList = new ArrayList<CCDAScoreCardRubrics>();
		
		if (sectionRules==null || ApplicationUtil.isRuleEnabled(sectionRules, ApplicationConstants.RULE_IDS.P1)) {
			patientScoreList.add(getDOBTimePrecisionScore(patient, ccdaVersion));
		}
		if (sectionRules==null || ApplicationUtil.isRuleEnabled(sectionRules, ApplicationConstants.RULE_IDS.P2)) {
			patientScoreList.add(getPatientNameScore(patient, ccdaVersion));
			patientScoreList.add(getPatientNameElementScore(patient, ccdaVersion));
		}
		
		patientCategory.setCategoryRubrics(patientScoreList);
		ApplicationUtil.calculateSectionGradeAndIssues(patientScoreList, patientCategory);
		ApplicationUtil.calculateNumberOfChecksAndFailedRubrics(patientScoreList, patientCategory);
		
		return patientCategory;
		
	}
	
	
	public static CCDAScoreCardRubrics getDOBTimePrecisionScore(CCDAPatient patient, String ccdaVersion)
	{
		CCDAScoreCardRubrics timePrecisionScore = new CCDAScoreCardRubrics();
		timePrecisionScore.setRule(ApplicationConstants.PATIENT_DOB_REQUIREMENT);
		
		int actualPoints = 0;
		int maxPoints = 1;
		int numberOfChecks = 1;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue= null;
		if(patient != null)
		{
			if(patient.getDob()!=null)
			{
				if((ApplicationUtil.validateDayFormat(patient.getDob().getValue()) || 
						ApplicationUtil.validateMinuteFormat(patient.getDob().getValue()) || 
						ApplicationUtil.validateSecondFormat(patient.getDob().getValue()))
						&& ApplicationUtil.validateBirthDate(patient.getDob().getValue()))
				{
					actualPoints++;
				}
				else
				{
					issue = new CCDAXmlSnippet();
					issue.setLineNumber(patient.getDob().getLineNumber());
					issue.setXmlString(patient.getDob().getXmlString());
					issuesList.add(issue);
				}
			}
			else
			{
				issue = new CCDAXmlSnippet();
				issue.setLineNumber(patient.getLineNumber());
				issue.setXmlString(patient.getXmlString());
				issuesList.add(issue);
			}
		}
		else
		{
			issue = new CCDAXmlSnippet();
			issue.setLineNumber("Patient section not present");
			issue.setXmlString("Patient section not present");
			issuesList.add(issue);
		}
		
		timePrecisionScore.setActualPoints(actualPoints);
		timePrecisionScore.setMaxPoints(maxPoints);
		timePrecisionScore.setRubricScore(ApplicationUtil.calculateRubricScore(maxPoints, actualPoints));
		timePrecisionScore.setIssuesList(issuesList);
		timePrecisionScore.setNumberOfIssues(issuesList.size());
		timePrecisionScore.setNumberOfChecks(numberOfChecks);
		if(issuesList.size() > 0)
		{
			timePrecisionScore.setDescription("Time precision Rubric failed for Patient DOB");
			if(ccdaVersion.equals("") || ccdaVersion.equals(ApplicationConstants.CCDAVersion.R21.getVersion()))
			{
				timePrecisionScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES.RECORD_TARGET.getIgReference());
			}
			else if(ccdaVersion.equals(ApplicationConstants.CCDAVersion.R11.getVersion()))
			{
				timePrecisionScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES_R1.RECORD_TARGET.getIgReference());
			}
			timePrecisionScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_LINKS.PATIENT.getTaskforceLink());
		}
		return timePrecisionScore;
	}
	
	public static CCDAScoreCardRubrics getLegalNameScore(CCDAPatient patient, String ccdaVersion)
	{
		CCDAScoreCardRubrics legalNameScore = new CCDAScoreCardRubrics();
		legalNameScore.setRule(ApplicationConstants.PATIENT_LEGAL_NAME_REQUIREMENT);
		
		int actualPoints = 0;
		int maxPoints = 0;
		int numberOfChecks = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue= null;
		if(patient != null)
		{
			if(patient.getPatientLegalNameElement()!=null)
			{
				maxPoints++;
				numberOfChecks++;
				if(!(patient.getGivenNameElementList().size() > 2 || patient.isGivenNameContainsQualifier()))
				{
					actualPoints++;
				}
				else
				{
					issue = new CCDAXmlSnippet();
					issue.setLineNumber(patient.getPatientLegalNameElement().getLineNumber());
					issue.setXmlString(patient.getPatientLegalNameElement().getXmlString());
					issuesList.add(issue);
				}
			}
		}
		
		if(maxPoints ==0)
		{
			maxPoints =1;
			actualPoints =1;
		}
		
		legalNameScore.setActualPoints(actualPoints);
		legalNameScore.setMaxPoints(maxPoints);
		legalNameScore.setRubricScore(ApplicationUtil.calculateRubricScore(maxPoints, actualPoints));
		legalNameScore.setIssuesList(issuesList);
		legalNameScore.setNumberOfIssues(issuesList.size());
		legalNameScore.setNumberOfChecks(numberOfChecks);
		if(issuesList.size() > 0)
		{
			legalNameScore.setDescription(ApplicationConstants.PATIENT_LEGAL_NAME_DESCRIPTION);
			if(ccdaVersion.equals("") || ccdaVersion.equals(ApplicationConstants.CCDAVersion.R21.getVersion()))
			{
				legalNameScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES.RECORD_TARGET.getIgReference());
			}
			else if(ccdaVersion.equals(ApplicationConstants.CCDAVersion.R11.getVersion()))
			{
				legalNameScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES_R1.RECORD_TARGET.getIgReference());
			}
			legalNameScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_LINKS.PATIENT.getTaskforceLink());
		}
		return legalNameScore;
	}
	
	public static CCDAScoreCardRubrics getPatientNameScore(CCDAPatient patient, String ccdaVersion)
	{
		CCDAScoreCardRubrics patientNameScore = new CCDAScoreCardRubrics();
		patientNameScore.setRule(ApplicationConstants.PATIENT_LEGAL_NAME_REQUIREMENT);
		
		int actualPoints = 0;
		int maxPoints = 0;
		int numberOfChecks = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue= null;
		
		if(patient != null)
		{
			if(patient.getPatientNames()!=null && patient.getPatientNames().size()>1)
			{
				maxPoints++;
				numberOfChecks++;
				Boolean isPatientLegalNamePresent = false;
				for(CCDAPatientName patientName : patient.getPatientNames()) {
					if(patientName.getUseContext()!=null && patientName.getUseContext().equalsIgnoreCase("L")) {
						isPatientLegalNamePresent = true;
					}
				}
				
				if(isPatientLegalNamePresent) {
					actualPoints++;
				}
				else
				{
					for(CCDAPatientName patientName : patient.getPatientNames()) {
						issue = new CCDAXmlSnippet();
						issue.setLineNumber(patientName.getLineNumber());
						issue.setXmlString(patientName.getXmlString());
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
		
		patientNameScore.setActualPoints(actualPoints);
		patientNameScore.setMaxPoints(maxPoints);
		patientNameScore.setRubricScore(ApplicationUtil.calculateRubricScore(maxPoints, actualPoints));
		patientNameScore.setIssuesList(issuesList);
		patientNameScore.setNumberOfIssues(issuesList.size());
		patientNameScore.setNumberOfChecks(numberOfChecks);
		if(issuesList.size() > 0)
		{
			patientNameScore.setDescription(ApplicationConstants.PATIENT_LEGAL_NAME_DESCRIPTION);
			if(ccdaVersion.equals("") || ccdaVersion.equals(ApplicationConstants.CCDAVersion.R21.getVersion()))
			{
				patientNameScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES.RECORD_TARGET.getIgReference());
			}
			else if(ccdaVersion.equals(ApplicationConstants.CCDAVersion.R11.getVersion()))
			{
				patientNameScore.getIgReferences().add(ApplicationConstants.IG_REFERENCES_R1.RECORD_TARGET.getIgReference());
			}
			patientNameScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_LINKS.PATIENT.getTaskforceLink());
		}
		return patientNameScore;
	}
	
	public static CCDAScoreCardRubrics getPatientNameElementScore(CCDAPatient patient, String ccdaVersion) {
		CCDAScoreCardRubrics patientNameElementScore = new CCDAScoreCardRubrics();
		patientNameElementScore.setRule(ApplicationConstants.PATIENT_LEGAL_NAME_REQUIREMENT);

		int actualPoints = 0;
		int maxPoints = 0;
		int numberOfChecks = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue = null;

		if (patient != null && patient.getLastName() != null && patient.getFirstName() != null) {
			if (patient.getPatientNames() != null && patient.getPatientNames().size() > 1) {
				maxPoints++;
				numberOfChecks++;
				for (CCDAPatientName patientName : patient.getPatientNames()) {
					if (!patientName.getUseContext().equalsIgnoreCase("L")) {
						if (!patientNameUseValues.contains(patientName.getUseContext())) {
							if (patientName.getFamilyName() != null) {
								for (CCDAPatientNameElement patientNameElement : patientName.getFamilyName()) {
									if (!patientNameElement.getValue()
											.equalsIgnoreCase(patient.getLastName().getValue())
											&& !patientNameElementQualifierValues
													.contains(patientNameElement.getQualifierValue())) {
										issue = new CCDAXmlSnippet();
										issue.setLineNumber(patientNameElement.getLineNumber());
										issue.setXmlString(patientNameElement.getXmlString());
										issuesList.add(issue);
									}
								}
							}
							if (patientName.getGivenName() != null) {
								for (CCDAPatientNameElement patientNameElement : patientName.getGivenName()) {
									if (!patientNameElement.getValue()
											.equalsIgnoreCase(patient.getFirstName().getValue())
											&& !patientNameElement.getValue()
													.equalsIgnoreCase(patient.getMiddleName().getValue())
											&& !patientNameElementQualifierValues
													.contains(patientNameElement.getQualifierValue())) {
										issue = new CCDAXmlSnippet();
										issue.setLineNumber(patientNameElement.getLineNumber());
										issue.setXmlString(patientNameElement.getXmlString());
										issuesList.add(issue);

									}
								}
							}
						}
					}
				}
				if (issuesList.size() == 0) {
					actualPoints++;
				}
			}
		}

		if (maxPoints == 0) {
			maxPoints = 1;
			actualPoints = 1;
		}

		patientNameElementScore.setActualPoints(actualPoints);
		patientNameElementScore.setMaxPoints(maxPoints);
		patientNameElementScore.setRubricScore(ApplicationUtil.calculateRubricScore(maxPoints, actualPoints));
		patientNameElementScore.setIssuesList(issuesList);
		patientNameElementScore.setNumberOfIssues(issuesList.size());
		patientNameElementScore.setNumberOfChecks(numberOfChecks);
		if (issuesList.size() > 0) {
			patientNameElementScore.setDescription(ApplicationConstants.PATIENT_LEGAL_NAME_DESCRIPTION);
			if (ccdaVersion.equals("") || ccdaVersion.equals(ApplicationConstants.CCDAVersion.R21.getVersion())) {
				patientNameElementScore.getIgReferences()
						.add(ApplicationConstants.IG_REFERENCES.RECORD_TARGET.getIgReference());
			} else if (ccdaVersion.equals(ApplicationConstants.CCDAVersion.R11.getVersion())) {
				patientNameElementScore.getIgReferences()
						.add(ApplicationConstants.IG_REFERENCES_R1.RECORD_TARGET.getIgReference());
			}
			patientNameElementScore.getExampleTaskForceLinks()
					.add(ApplicationConstants.TASKFORCE_LINKS.PATIENT.getTaskforceLink());
		}
		return patientNameElementScore;
	}
}
