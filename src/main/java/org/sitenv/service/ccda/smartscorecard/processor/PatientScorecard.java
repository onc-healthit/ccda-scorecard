package org.sitenv.service.ccda.smartscorecard.processor;

import java.util.ArrayList;
import java.util.List;

import org.sitenv.ccdaparsing.model.CCDAPatient;
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
		patientCategory.setCategoryName("Patient Information");
		List<CCDAScoreCardRubrics> patientScoreList = new ArrayList<CCDAScoreCardRubrics>();
		patientScoreList.add(getDOBTimePrecisionScore(patient));
		
		patientCategory.setCategoryRubrics(patientScoreList);
		patientCategory.setCategoryGrade(calculateSectionGrade(patientScoreList));
		
		return patientCategory;
		
	}
	
	public static String calculateSectionGrade(List<CCDAScoreCardRubrics> rubricsList)
	{
		int actualPoints=0;
		int maxPoints = 0;
		float percentage ;
		for(CCDAScoreCardRubrics rubrics : rubricsList)
		{
			actualPoints = actualPoints + rubrics.getActualPoints();
			maxPoints = maxPoints + rubrics.getMaxPoints();
		}
		
		percentage = (actualPoints * 100)/maxPoints;
		
		if(percentage <= 35)
		{
			return "C";
		}else if(percentage >= 35 && percentage <=70)
		{
			return "B";
		}else if(percentage >=70 && percentage <=100)
		{
			return "A";
		}else 
			return "UNKNOWN GRADE";
	}
	
	
	public static CCDAScoreCardRubrics getDOBTimePrecisionScore(CCDAPatient patient)
	{
		CCDAScoreCardRubrics timePrecisionScore = new CCDAScoreCardRubrics();
		timePrecisionScore.setPoints(ApplicationConstants.PATIENT_DOB_POINTS);
		timePrecisionScore.setRequirement(ApplicationConstants.PATIENT_DOB_REQUIREMENT);
		timePrecisionScore.setSubCategory(ApplicationConstants.SUBCATEGORIES.PATINET_DOB.getSubcategory());
		
		int actualPoints = 0;
		int maxPoints = 1;
		
		if(patient != null)
		{
			maxPoints++;
			if(patient.getDob()!=null)
			{
				if(ApplicationUtil.validateDayFormat(patient.getDob().getValue()) && ApplicationUtil.validateDate(patient.getDob().getValue()));
				{
					actualPoints++;
				}
			}
		}
		
		if(maxPoints == actualPoints)
		{
			timePrecisionScore.setComment("Patient DOB is properly precisioned with valid date");
		}else
		{
			timePrecisionScore.setComment("Patient DOB is not propelry precisioned or not a valid date");
		}
		
		timePrecisionScore.setActualPoints(actualPoints);
		timePrecisionScore.setMaxPoints(maxPoints);
		return timePrecisionScore;
	}

}
