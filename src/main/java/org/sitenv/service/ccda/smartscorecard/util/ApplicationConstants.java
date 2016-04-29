package org.sitenv.service.ccda.smartscorecard.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class ApplicationConstants {
	
	 
	public static String FILEPATH = "C:/Projects/Dragon/CCDAParser/170.315_b1_toc_amb_ccd_r21_sample1_v1.xml";
	
	public static final String PATIENT_DOB_REQUIREMENT = "Patient DOB should be valid and properly precisioned";
	
	public static final String ENCOUNTER_TIME_PRECISION_REQUIREMENT = "All effective time elements under Encounters section should contain "
																		+ "proper precision and format with correct offset";
	public static final String ENCOUNTER_TIMEDATE_VALID_REQUIREMENT = "All effective time elements under Encounters section should contain "
																		+ "valid date and time value within human life span";
	public static final String ENCOUNTER_CODE_DISPLAYNAME_REQUIREMENT = "All code elements under encounter section should contain valid display names";	
	
	public static final String MEDICATION_TIME_PRECISION_REQUIREMENT = "All effective time elements under Medication section should contain "
																			+ "proper precision and format with correct offset";
	public static final String MEDICATION_TIMEDATE_VALID_REQUIREMENT = "All effective time elements under Medication section should contain "
																		+ "valid date and time value within human life span";
	public static final String MEDICATION_CODE_DISPLAYNAME_REQUIREMENT = "All code elements under Medication section should contain valid display names";
	public static final String MEDICATION_ACTIVITY_VALID_REQUIREMENT = "All Immunizations should be under Immunization section and not in Medication section";
	
	public static final String PROBLEMS_TIME_PRECISION_REQUIREMENT = "All effective time elements under Problems section should contain "
																		+ "proper precision and format with correct offset";
	public static final String PROBLEMS_TIMEDATE_VALID_REQUIREMENT = "All effective time elements under Problems section should contain "
																	+ "valid date and time value within human life span";
	public static final String PROBLEMS_CODE_DISPLAYNAME_REQUIREMENT = "All code elements under Problems section should contain valid display names";	
	public static final String PROBLEMS_ACT_STATUS_CODE_REQUIREMENT = "All problem concern act status code should align with effective time values";
	
	public static final String IMMUNIZATION_TIME_PRECISION_REQUIREMENT = "All effective time elements under Immunization section should contain "
																	+ "proper precision and format with correct offset";
	public static final String IMMUNIZATION_TIMEDATE_VALID_REQUIREMENT = "All effective time elements under Immunization section should contain "
																		+ "valid date and time value within human life span";	
	public static final String IMMUNIZATION_CODE_DISPLAYNAME_REQUIREMENT = "All code elements under Immunization section should contain valid display names";	
	
	public static final String LABRESULTS_TIME_PRECISION_REQUIREMENT = "All effective time elements under Lab Results section should contain "
																		+ "proper precision and format with correct offset";
	public static final String LABRESULTS_TIMEDATE_VALID_REQUIREMENT = "All effective time elements under Lab Results section should contain "
																		+ "valid date and time value within human life span";
	public static final String LABRESULTS_CODE_DISPLAYNAME_REQUIREMENT = "All code elements under Results section should contain valid display names";
	public static final String LABRESULTS_UCUM_REQUIREMENT = "All LOINC codes under Results section should contain valid UCUM units";
	
	public static final String VITALS_TIME_PRECISION_REQUIREMENT = "All effective time elements under Vitals section should contain "
																	+ "proper precision and format with correct offset";
	public static final String VITALS_TIMEDATE_VALID_REQUIREMENT = "All effective time elements under Vitals section should contain "
																	+ "valid date and time value within human life span";	
	public static final String VITALS_CODE_DISPLAYNAME_REQUIREMENT = "All code elements under Vitals section should contain valid display names";
	public static final String VITALS_UCUM_REQUIREMENT = "All LOINC codes under Vitals section should contain valid UCUM units";
	public static final String VITALS_ORG_DATE_ALIGN = "Vitals observation effective should align with vitals organizer effetive time";
	
	public static final String ALLERGIES_TIME_PRECISION_REQUIREMENT = "All effective time elements under allergies section should contain "
																		+ "proper precision and format with correct offset";
	public static final String ALLERGIES_TIMEDATE_VALID_REQUIREMENT = "All effective time elements under allergies section should contain "
																		+ "valid date and time value within human life span";
	public static final String ALLERGIES_CODE_DISPLAYNAME_REQUIREMENT = "All code elements under allergies section should contain valid display names";		

	
	public static final String PROCEDURES_SECTION_REQUIREMENT = "CCDA document should contain procedures section";
	
	
	public static final String SOCIALHISTORY_TIME_PRECISION_REQUIREMENT = "All effective time elements under Social History section should contain "
																			+ "proper precision and format with correct offset";
	public static final String SOCIALHISTORY_TIMEDATE_VALID_REQUIREMENT = "All effective time elements under Social History section should contain "
																		+ "valid date and time value within human life span";
	public static final String SOCIALHISTORY_CODE_DISPLAYNAME_REQUIREMENT = "All code elements under Social History section should contain valid display names";	
	public static final String SOCIALHISTORY_SMOKING_STATUS_REQUIREMENT = "Smoking status code value should be valid";
	public static final String SOCIALHISTORY_SMOKING_STATUS_OBS_ID_REQUIREMENT = "Smoking status observation Template Id should be valid";
	
	public static final String DAY_FORMAT = "yyyyMMdd";
	public static final String DAY_PATTERN = "\\d{8}";
	
	public static final String MINUTE_FORMAT = "yyyyMMddHHmmZ";
	public static final String MINUTE_PATTERN = "\\d{12}-\\d{4}";
	
	public static final String SECOND_FORMAT = "yyyyMMddHHmmssZ";
	public static final String SECOND_PATTERN = "\\d{8}-\\d{4}";
	
	public static final String CODE_DISPLAYNAME_VALIDATION_URL = "http://54.200.51.225:8080/referenceccdaservice/iscodeandisplaynameincodesystem"; 
	
	public static final ArrayList<String> SMOKING_STATUS_CODES = new ArrayList<String>(
		    Arrays.asList("449868002", "428041000124106", "8517006","266919005","77176002","266927001","428071000124103","428061000124105"));
	
	public static final String SMOKING_STATUS_OBSERVATION_ID = "2.16.840.1.113883.10.20.22.4.78"; 
	
	public static final String IMMUNIZATION_ACTIVITY_ID = "2.16.840.1.113883.10.20.22.4.52"; 
	
	public static final String SNOMEDCT_CODE_NAME = "SNOMED-CT";
	public static final String SNOMEDCT_CODE_SYSTEM = "2.16.840.1.113883.6.96";
	
	public static final String LOINC_CODE_NAME = "LOINC";
	public static final String LOINC_CODE_SYSTEM = "2.16.840.1.113883.6.1";
	
	public static final String ICD9CM_DIAGNOSIS_CODE_NAME = "ICD9CM_DX";
	public static final String ICD9CM_DIAGNOSIS_CODE_SYSTEM = "2.16.840.1.113883.6.103";
	
	public static final String ICD9CM_PROCEDURE_CODE_NAME = "ICD9CM_SG";
	public static final String ICD9CM_PROCEDURE_CODE_SYSTEM = "2.16.840.1.113883.6.104";
	
	public static final String ICD10CM_CODE_NAME = "ICD10CM";
	public static final String ICD10CM_CODE_SYSTEM = "2.16.840.1.113883.6.90";
	
	public static final String ICD10PCS_CODE_NAME = "ICD10PCS";
	public static final String ICD10PCS_CODE_SYSTEM = "2.16.840.1.113883.6.4";
	
	public static final String RXNORM_CODE_NAME = "RXNORM";
	public static final String RXNORM_CODE_SYSTEM = "2.16.840.1.113883.6.88";
	
	public static final String CPT4_CODE_NAME = "CPT-4";
	public static final String CPT4_CODE_SYSTEM = "2.16.840.1.113883.6.12";
	
	public static final String VSAC_VALUESET_NAME = "VSAC";
	
	public static final Map<String, String> CODE_SYSTEM_MAP = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
	
	static {
		CODE_SYSTEM_MAP.put(SNOMEDCT_CODE_SYSTEM, SNOMEDCT_CODE_NAME);
		CODE_SYSTEM_MAP.put(LOINC_CODE_SYSTEM, LOINC_CODE_NAME);
		CODE_SYSTEM_MAP.put(ICD9CM_DIAGNOSIS_CODE_SYSTEM, ICD9CM_DIAGNOSIS_CODE_NAME);
		CODE_SYSTEM_MAP.put(ICD9CM_PROCEDURE_CODE_SYSTEM, ICD9CM_PROCEDURE_CODE_NAME);
		CODE_SYSTEM_MAP.put(ICD10CM_CODE_SYSTEM, ICD10CM_CODE_NAME);
		CODE_SYSTEM_MAP.put(ICD10PCS_CODE_SYSTEM, ICD10PCS_CODE_NAME);
		CODE_SYSTEM_MAP.put(RXNORM_CODE_SYSTEM, RXNORM_CODE_NAME);
		CODE_SYSTEM_MAP.put(CPT4_CODE_SYSTEM, CPT4_CODE_NAME);
	}
	
	
	
	
	public static final Map<Integer , String> PATIENT_DOB_POINTS = new HashMap<Integer , String>() {/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

	{
		put(0, "DOB is not valid or not precisioned properly");
	    put(1, "DOB is valid and precisioned properly");
	}};
	
	public static final Map<Integer , String> TIME_PRECISION_POINTS = new HashMap<Integer , String>() {/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

	{
		put(0, "All effective time elements are not properly precisioned or formatted or doesn't contain proper offset.");
	    put(1, "More than 25% of effective time elements are properly precisioned, formatted and has correct offset");
	    put(2, "More than 50% of effective time elements are properly precisioned, formatted and has correct offset");
	    put(3, "More than 75% of effective time elements are properly precisioned, formatted and has correct offset");
	    put(4, "All effective time elements are properly precisioned, formatted and has correct offset");
	}};
	
	public static final Map<Integer , String> VALID_TIME_POINTS = new HashMap<Integer , String>() {/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

	{
		put(0, "None of the effective time element is valid.");
	    put(1, "More than 25% of effective time elements are valid and within human lifespan");
	    put(2, "More than 50% of effective time elements are valid and within human lifespan");
	    put(3, "More than 75% of effective time elements are valid and within human lifespan");
	    put(4, "All effective time elements are are valid and within human lifespan");
	}};
	
	public static final Map<Integer , String> VALID_CODE_DISPLAYNAME_POINTS = new HashMap<Integer , String>() {/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

	{
		put(0, "None of the code element has correct display Name");
	    put(1, "More than 25% of code elemeents are having correct display name");
	    put(2, "More than 50% of code elements are having correct display name");
	    put(3, "More than 75% of code elements are having correct display name");
	    put(4, "All code elements are having correct display name");
	}};
	
	public static final Map<Integer , String> VALID_UCUM_CODE_POINTS = new HashMap<Integer , String>() {/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

	{
		put(0, "None of the LOINC codes has correct UCUM units");
	    put(1, "More than 25% of LOINC codes are having correct UCUM units");
	    put(2, "More than 50% of LOINC codes are having correct UCUM units");
	    put(3, "More than 75% of LOINC codes are having correct UCUM units");
	    put(4, "All LOINC codes are having valid UCUM units");
	}};
	
	public static final Map<Integer , String> VITALS_APPR_TIME_POINTS = new HashMap<Integer , String>() {/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

	{
		put(0, "None of the vitals observation effective time is aligned with Vitals organizer effective time");
	    put(1, "More than 25% of vitals observation effective time is aligned with Vitals organizer effective time");
	    put(2, "More than 50% of vital observation effective time is aligned with Vitals organizer effective time");
	    put(3, "More than 75% of vital observation effective time is aligned with Vitals organizer effective time");
	    put(4, "All Vital observations effective ae aligned with Vital organizer effective time");
	}};
	
	public static final Map<Integer , String> PROBLEM_ACT_STATUS_CNST_SCORE = new HashMap<Integer , String>() {/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

	{
		put(0, "None of the concern act status code is aligned with effective time values");
	    put(1, "More than 25% of concern act status code are aligned with effective time values");
	    put(2, "More than 50% of concern act status code are aligned with effective time values");
	    put(3, "More than 75% of concern act status code are aligned with effective time values");
	    put(4, "All concern act status code are aligned with effective time values");
	}};
	
	public static final Map<Integer , String> VALID_MEDICATIONS_POINTS = new HashMap<Integer , String>() {/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

	{
	    put(0, "No Immunizations under Medication section");
	    put(1, "Immunizations should be under Immunization section and not in Medication section");
	}};
	
	public static final Map<Integer , String> VALID_SMOKING_STATUS_POINTS = new HashMap<Integer , String>() {/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

	{
	    put(0, "Smoking status observation value code is Invalid");
	    put(1, "smoking status observation value code is valid");
	}};
	
	public static final Map<Integer , String> VALID_SMOKING_STATUS_ID_POINTS = new HashMap<Integer , String>() {/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

	{
	    put(0, "Smoking status observation Template ID is Invalid");
	    put(1, "Smoking status observation Template ID is valid");
	}};
	
	
	public static enum CATEGORIES
	{
		PATIENT("Patient",10),
		GENERAL("General",10), 
		ENCOUNTER("Encounter",10),
		Medication("Medication",10);

		private String category;
		private int totalDataElements;

		private CATEGORIES(final String category, final int totalDataElements)
		{
			this.category = category;
			this.totalDataElements = totalDataElements;
		}

		public String getCategory()
		{
			return category;
		}
		
		public int gettotalDataElements()
		{
			return totalDataElements;
		}

	}
	
	public static enum SUBCATEGORIES
	{
		PATINET_DOB("Patient DOB",1),
		TIME_PRECISION("TimePrecision",1),
		TIME_VALIDATION("ValidDateTime",1),
		CODE_DISPLAYNAME_VALIDATION("Valid Display Name",1),
		UCUM_VALIDATION("UCUM validation",1),
		TIME_ALIGN("Time Alignment",1),
		SMOKING_STATUS("Valid Smoking status",1),
		SMOKING_STATUS_ID("Valid Smoking status ID",1),
		PROBLEM_STATUSCODE("Problem status code",1),
		MEDICATION_VALID("Valid Medication Activities",1);

		private String subCategory;
		private int maxPoints;

		private SUBCATEGORIES(final String subCategory , final int maxPoints)
		{
			this.subCategory = subCategory;
			this.maxPoints = maxPoints;
		}

		public String getSubcategory()
		{
			return subCategory;
		}
		
		public int getMaxPoints()
		{
			return maxPoints;
		}

	}
	
	public static enum PROBLEMACT_STATUS
	{
		COMPLETED("Completed"),
		ACTIVE("Active"), 
		ABORTED("Aborted"),
		SUSPENDED("Suspended");

		private String status;

		private PROBLEMACT_STATUS(final String status)
		{
			this.status = status;
		}

		public String getstatus()
		{
			return status;
		}

	}
	
	public static enum PROBLEM_STATUS
	{
		ACTIVE("Active"), 
		INACTIVE("Inactive"),
		RESOLVED("Resolved");

		private String status;

		private PROBLEM_STATUS(final String status)
		{
			this.status = status;
		}

		public String getstatus()
		{
			return status;
		}

	}

}
