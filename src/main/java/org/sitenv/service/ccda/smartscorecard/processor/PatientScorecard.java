package org.sitenv.service.ccda.smartscorecard.processor;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.sitenv.ccdaparsing.model.CCDAPL;
import org.sitenv.ccdaparsing.model.CCDAPatient;
import org.sitenv.service.ccda.smartscorecard.model.CCDAScoreCardRubrics;
import org.sitenv.service.ccda.smartscorecard.model.Category;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationConstants;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationUtil;

public class PatientScorecard {
	
	public static Category getPatientCategory(CCDAPatient patient)
	{
		
		Category patientCategory = new Category();
		patientCategory.setCategoryName("Patient");
		List<CCDAScoreCardRubrics> patientScoreList = new ArrayList<CCDAScoreCardRubrics>();
		patientScoreList.add(getContactScore(patient));
		patientScoreList.add(getDOBScore(patient));
		patientScoreList.add(getLangIndScore(patient));
		patientScoreList.add(getTimePrecisionScore(patient));
		
		patientCategory.setCategoryRubrics(patientScoreList);
		patientCategory.setCategoryGrade("B");
		
		return patientCategory;
		
	}
	
	public static CCDAScoreCardRubrics getContactScore(CCDAPatient patient)
	{
		CCDAScoreCardRubrics contactScore = new CCDAScoreCardRubrics();
		contactScore.setPoints(ApplicationConstants.PATIENT_CONTACT_POINTS);
		contactScore.setRequirement(ApplicationConstants.PATIENT_CONTACT_REQUIREMENT);
		contactScore.setSubCategory(ApplicationConstants.SUBCATEGORIES.PATIENT_CONTACT.getSubcategory());
		contactScore.setMaxPoints(ApplicationConstants.SUBCATEGORIES.PATIENT_CONTACT.getMaxPoints());
		int actualPoints = 0;
		if(!ApplicationUtil.isEmpty(patient.getAddresses()))
		{
			actualPoints++;
		}
		
		if(!ApplicationUtil.isEmpty(patient.getTelecom()))
		{
			actualPoints++;
		}
		
		if(ApplicationConstants.SUBCATEGORIES.PATIENT_CONTACT.getMaxPoints() == actualPoints)
		{
			contactScore.setComment("Patient element has both address and telecom details");
		}else if(actualPoints == 1)
		{
			contactScore.setComment("Either Telecom or address is missing for Patient element");
		}else
		{
			contactScore.setComment("Both Telecom and address are missing for Patient element");
		}
		
		contactScore.setActualPoints(actualPoints);
		
		return contactScore;
	}
	
	public static CCDAScoreCardRubrics getDOBScore(CCDAPatient patient)
	{
		CCDAScoreCardRubrics dobScore = new CCDAScoreCardRubrics();
		dobScore.setPoints(ApplicationConstants.PATIENT_DOB_POINTS);
		dobScore.setRequirement(ApplicationConstants.PATIENT_DOB_REQUIREMENT);
		dobScore.setSubCategory(ApplicationConstants.SUBCATEGORIES.PATIENT_DOB.getSubcategory());
		dobScore.setMaxPoints(ApplicationConstants.SUBCATEGORIES.PATIENT_DOB.getMaxPoints());
		int actualPoints =1;
		if(patient.getDob() == null || ApplicationUtil.isValueEmpty(patient.getDob()))
		{
			actualPoints =0;
		}
		
		if(ApplicationConstants.SUBCATEGORIES.PATIENT_DOB.getMaxPoints() == actualPoints)
		{
			dobScore.setComment("Patient element contains Date of birth");
		}else
		{
			dobScore.setComment("Patient element doesnt contains Date of birth");
		}
		
		dobScore.setActualPoints(actualPoints);
		return dobScore;
	}
	
	public static CCDAScoreCardRubrics getLangIndScore(CCDAPatient patient)
	{
		CCDAScoreCardRubrics langIndScore = new CCDAScoreCardRubrics();
		langIndScore.setPoints(ApplicationConstants.PATIENT_LANG_IND_POINTS);
		langIndScore.setRequirement(ApplicationConstants.PATIENT_LANG_IND_REQUIREMENT);
		langIndScore.setSubCategory(ApplicationConstants.SUBCATEGORIES.PATIENT_LANG_IND.getSubcategory());
		langIndScore.setMaxPoints(ApplicationConstants.SUBCATEGORIES.PATIENT_LANG_IND.getMaxPoints());
		
		int actualPoints =1;
		for( CCDAPL language : patient.getLanguageCommunication())
		{
			
			if(language.getPreferenceInd() == null || ApplicationUtil.isValueEmpty(language.getPreferenceInd()))
			{
				actualPoints = 0;
			}
		}
		
		if(ApplicationConstants.SUBCATEGORIES.PATIENT_LANG_IND.getMaxPoints() == actualPoints)
		{
			langIndScore.setComment("Preferred Language attributes specified for patient");
		}else
		{
			langIndScore.setComment("Preferred Language attributes not specified for patient");
		}
		langIndScore.setActualPoints(actualPoints);
		return langIndScore;
	}
	
	public static CCDAScoreCardRubrics getTimePrecisionScore(CCDAPatient patient)
	{
		CCDAScoreCardRubrics timePrecisionScore = new CCDAScoreCardRubrics();
		timePrecisionScore.setPoints(ApplicationConstants.PATIENT_EFF_TIME_POINTS);
		timePrecisionScore.setRequirement(ApplicationConstants.PATIENT_EFF_TIME_REQUIREMENT);
		timePrecisionScore.setSubCategory(ApplicationConstants.SUBCATEGORIES.TIME_PRECISION.getSubcategory());
		timePrecisionScore.setMaxPoints(ApplicationConstants.SUBCATEGORIES.TIME_PRECISION.getMaxPoints());
		
		int actualPoints =1;
		try
		{
			ApplicationUtil.convertStringToDate(patient.getDob().getValue(), ApplicationConstants.DAY_FORMAT);

		}catch(ParseException e)
		{
			actualPoints =0;
		}
		
		if(ApplicationConstants.SUBCATEGORIES.TIME_PRECISION.getMaxPoints() == actualPoints)
		{
			timePrecisionScore.setComment("All the time elememts under patient sections has proper precision");
		}else
		{
			timePrecisionScore.setComment("Birthdate under patient sections should be precisioned to day");
		}
		
		timePrecisionScore.setActualPoints(actualPoints);
		return timePrecisionScore;
	}

}
