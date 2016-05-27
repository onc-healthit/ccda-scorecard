package org.sitenv.service.ccda.smartscorecard.processor;

import java.util.ArrayList;
import java.util.List;

import org.sitenv.ccdaparsing.model.CCDAPatient;
import org.sitenv.ccdaparsing.model.CCDAXmlSnippet;
import org.sitenv.service.ccda.smartscorecard.model.CCDAScoreCardRubrics;
import org.sitenv.service.ccda.smartscorecard.model.Category;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationConstants;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationUtil;
import org.springframework.stereotype.Service;

@Service
public class PatientScorecard {
	
	public Category getPatientCategory(CCDAPatient patient)
	{
		
		Category patientCategory = new Category();
		patientCategory.setCategoryName("Patient Demographics");
		List<CCDAScoreCardRubrics> patientScoreList = new ArrayList<CCDAScoreCardRubrics>();
		patientScoreList.add(getDOBTimePrecisionScore(patient));
		
		patientCategory.setCategoryRubrics(patientScoreList);
		ApplicationUtil.calculateSectionGradeAndIssues(patientScoreList, patientCategory);
		
		return patientCategory;
		
	}
	
	
	public static CCDAScoreCardRubrics getDOBTimePrecisionScore(CCDAPatient patient)
	{
		CCDAScoreCardRubrics timePrecisionScore = new CCDAScoreCardRubrics();
		timePrecisionScore.setRule(ApplicationConstants.PATIENT_DOB_REQUIREMENT);
		
		int actualPoints = 0;
		int maxPoints = 1;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue= null;
		if(patient != null)
		{
			if(patient.getDob()!=null)
			{
				if(ApplicationUtil.validateDayFormat(patient.getDob().getValue()) && ApplicationUtil.validateDate(patient.getDob().getValue()))
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
		if(issuesList.size() > 0)
		{
			timePrecisionScore.setDescription("Time precision Rubric failed for Patient DOB");
			timePrecisionScore.getIgReferences().add(ApplicationConstants.IG_SECTION_REFERENCES);
			timePrecisionScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_URL);
		}else 
		{
		    timePrecisionScore.setDescription("Time precision Rubric executed successfully for Patient DOB");
		}
		return timePrecisionScore;
	}

}
