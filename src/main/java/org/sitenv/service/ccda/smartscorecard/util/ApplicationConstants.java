package org.sitenv.service.ccda.smartscorecard.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

public class ApplicationConstants {
	
	//set this to false for production
	public static final boolean IN_DEVELOPMENT_MODE = false;
	 
	public static String FILEPATH = "C:/Projects/Dragon/CCDAParser/170.315_b1_toc_amb_ccd_r21_sample1_v1.xml";
	
	public static String FILEPATH1 = "C:/Projects/Dragon/CCDAParser/C-CDA_R2-1_CCD_withUSRH_EF_.xml";
	
	public static final String PATIENT_DOB_REQUIREMENT = "Patient DOB should be valid and properly precisioned";
	
	public static final String TIME_PRECISION_REQUIREMENT = "EffectiveDate/Time elements have the right time and timezone offsets";
	public static final String TIME_PRECISION_DESCRIPTION = "EffectiveTime elements in the section are expected to have timeoffsets along with the date and are "
			+ "typically nonzero timeoffsets. In addition they are expected to have the timezone information for proper interpretation.For e.g if the time is being "
			+ "defaulted to 000000 for hours, minutes and seconds for multiple entries it might be worth checking if the data was entered properly. "
			+ "Also if the time offsets are present without a timezone, the time may be interpreted incorrectly, hence timezones should be specified as part "
			+ "of the time element.";
	
	public static final String TIME_VALID_REQUIREMENT = "EffectiveDate/Times for all historical activities should be within the lifespan on the patient.";
	public static final String TIME_VALID_DESCRIPTION = " EffectiveDate/Times for historical events should be greater than the patient's date of "
			+ "birth and less than the earliest of current time or patient's date of death. ";
	
	public static final String CODE_DISPLAYNAME_REQUIREMENT = "The Display Names used by the structured data should match the Display "
											+ "Name (Preferred Name) within the Terminology";
	
	public static final String CODE_DISPLAYNAME_DESCRIPTION = "Each of the code systems, value sets specified by the C-CDA IG refers back to "
				+ "standard terminologies like SNOMED-CT, LOINC, RxNorm, ICD9, ICD10. When codes from these "
			+ "codesystems are used to represent structured data the display name corresponding to the code should be used as part of the document";
	
	public static final String UCUM_CODE_REQUIREMENT = "The units used for Physical Quantities are the ones recommended by HL7";
	public static final String UCUM_CODE_DESCRIPTION = "UCUM units used for Physical Quantities should match the recommendations for the LOINC code. "
			+ "So if the code element has a LOINC code then the recommended UCUM unit for the LOINC code should be used as part of the value element";
	
	
	
	public static final String VITAL_LOINC_REQUIREMENT = "The Vital Sign Observation entries should use LOINC codes to represent the type of vital sign being captured";
	
	public static final String VITAL_LOINC_DESCRIPTION = "Each of the vital sign observation present in the document should use the recommended "
			+ "LOINC codes to represent the vital sign.";
	public static final String VITAL_UCUM_REQUIREMENT = "Each of the Vital Sign Observation should use the recommended "
			+ "UCUM units to represent the vital sign measurement result.";
	public static final String VITAL_UCUM_DESCRIPTION = "The recommended UCUM units should be used to represent the Vital Sign result values as part of the observation.";
	public static final String VITAL_AAPR_DATE_REQUIREMENT = "The EffectiveDate/Time elements for the Vital Sign Organizer must encompass the underlying observations.";
	public static final String VITAL_AAPR_DATE_DESCRIPTION = "The EffectiveDate/Time elements of the Vital Signs Organizer cannot be out of sync with the "
			+ "Vital Signs Observation. Each of the Observation's EffectiveTime/low >= Organizer's EffectiveTime/low "
			+ "and Observation's EffectiveTime/high should be <= Organizer's EffectiveTime/high";
	public static final String PROBLEM_APR_STATUS_REQ = "Problem Concern status and Problem Observation status are consistent with each other.";
	public static final String PROBLEM_APR_STATUS_DESC = "A Problem Concern status of completed is compatible with a Problem Observation status of Resolved or Inactive."
			+ " A Problem Concern status of Active is compatible with a Problem Observation status of Active.";
	public static final String PROBLEM_APR_TIME_REQ = "Problem Concern effective times reflect the appropriate problem concern status.";
	public static final String PROBLEM_APR_TIME_DESC = "A Problem Concern of completed or suspended should have a Problem Concern effectiveTime/high value present."
                               + "Similarly a Problem Concern which is Active shall not have a Problem Concern effectiveTime/high value.";
	
	public static final String PROBLEM_TIME_CNST_REQ = "The EffectiveDate/Time elements for the Problem Concern Act must encompass the underlying observations.";
	public static final String PROBLEM_TIME_CNST_DESC = "The EffectiveDate/Time elements of the Problem Concern Act cannot be out of sync with the Problem Observation. "
			+ "Each of the Observation's EffectiveTime/low >= Problem Concern's EffectiveTime/low and Observation's EffectiveTime/high should be <= Problem Concern's EffectiveTime/high";
	public static final String IMMU_NOTIN_MED_REQ = "Immunizations should be represented in the appropriate section.";
	public static final String IMMU_NOTIN_MED_DESC = "Immunizations should be recorded using the Section Code '2.16.840.1.113883.10.20.22.2.2.1' within the document.";
	
	
	public static final String RESULTS_UCUM_REQ = "All Lab Results should use UCUM units to express the result values.";
	public static final String RESULTS_UCUM_DESC = "The recommended UCUM units should be used to represent the Lab Result values as part of the observation.";
	
	public static final String LABRESULTS_APR_TIME_REQ = "The EffectiveDate/Time elements for the Result Organizer must encompass the underlying observations.";
	public static final String LABRESULTS_APR_TIME_DESC = "The EffectiveDate/Time elements of the Results Organizer cannot be out of sync with the Result Observation. "
			+ "Each of the Observation's EffectiveTime/low >= Organizer's EffectiveTime/low and Observation's EffectiveTime/high should be <= Organizer's "
			+ "EffectiveTime/high";
	
	public static final String TEMPLATEID_DESC = "";
	public static final String TEMPLATEID_REQ = "All Template Ids should be correct";
	
	
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
	public static final String PROBLEMS_CODE_LOINC_REQUIREMENT = "All problem codes are should express with core subset of SNOMED codes";
	public static final String PROBLEMS_CONCERN_DATE_ALIGN = "Problems observation effective time should align with Problems concern act effective time";
	public static final String PROBLEMS_CONCERN_STATUS_ALIGN = "Problems observation status should align with Problems concern act status";
	
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
	public static final String LABRESULTS_LOIN_CODE_REQ = "Lab results should be expressed with LOINC codes";
	public static final String LABRESULTS_UCUM_REQUIREMENT = "All LOINC codes under Results section should contain valid UCUM units";
	public static final String LABRESULTS_ORG_DATE_ALIGN = "Results observation effective should align with Results organizer effective time";
	
	public static final String VITALS_TIME_PRECISION_REQUIREMENT = "All effective time elements under Vitals section should contain "
																	+ "proper precision and format with correct offset";
	public static final String VITALS_TIMEDATE_VALID_REQUIREMENT = "All effective time elements under Vitals section should contain "
																	+ "valid date and time value within human life span";	
	public static final String VITALS_CODE_DISPLAYNAME_REQUIREMENT = "All code elements under Vitals section should contain valid display names";
	public static final String VITALS_UCUM_REQUIREMENT = "All LOINC codes under Vitals section should contain valid UCUM units";
	public static final String VITALS_ORG_DATE_ALIGN = "Vitals observation effective should align with vitals organizer effective time";
	public static final String VITALS_LOIN_CODE_REQ = "Vitals observations should be expressed with LOINC codes";
	
	public static final String ALLERGIES_TIME_PRECISION_REQUIREMENT = "All effective time elements under allergies section should contain "
																		+ "proper precision and format with correct offset";
	public static final String ALLERGIES_TIMEDATE_VALID_REQUIREMENT = "All effective time elements under allergies section should contain "
																		+ "valid date and time value within human life span";
	public static final String ALLERGIES_CODE_DISPLAYNAME_REQUIREMENT = "All code elements under allergies section should contain valid display names";	
	public static final String ALLERGIES_CONCERN_DATE_ALIGN = "Allergies observation effective should align with Allergies concern act effective time";

	
	public static final String PROCEDURES_CODE_DISPLAYNAME_REQUIREMENT = "All code elements under Procedures section should contain valid display names";	
	
	
	public static final String SOCIALHISTORY_TIME_PRECISION_REQUIREMENT = "All effective time elements under Social History section should contain "
																			+ "proper precision and format with correct offset";
	public static final String SOCIALHISTORY_TIMEDATE_VALID_REQUIREMENT = "All effective time elements under Social History section should contain "
																		+ "valid date and time value within human life span";
	public static final String SOCIALHISTORY_CODE_DISPLAYNAME_REQUIREMENT = "All code elements under Social History section should contain valid display names";	
	public static final String SOCIALHISTORY_SMOKING_STATUS_REQUIREMENT = "Smoking status code value should be valid";
	public static final String SOCIALHISTORY_SMOKING_STATUS_OBS_ID_REQUIREMENT = "Smoking status observation Template Id should be valid";
	
	public static final String MONTH_FORMAT = "yyyyMM";
	public static final String MONTH_PATTERN = "\\d{6}";
	
	public static final String DAY_FORMAT = "yyyyMMdd";
	public static final String DAY_PATTERN = "\\d{8}";
	
	public static final String MINUTE_FORMAT = "yyyyMMddHHmmZ";
	public static final String MINUTE_PATTERN = "\\d{12}-\\d{4}";
	
	public static final String SECOND_FORMAT = "yyyyMMddHHmmssZ";
	public static final String SECOND_PATTERN = "\\d{8}-\\d{4}";
	
	private static final String CCDA_DEV_SERVER_URL = "http://54.200.51.225:8080",
			LOCAL_HOST_URL = "http://localhost:8080",
			CODE_AND_DISPLAYNAME_IN_CODESYSTEM_SERVICE = "/referenceccdaservice/iscodeandisplaynameincodesystem",
			CODE_IN_VALUESET_SERVICE = "/referenceccdaservice/iscodeinvalueset",
			CODE_IN_CODESYSTEM_SERVICE = "/referenceccdaservice/iscodeincodesystem",
			REFERENCE_CCDA_SERVICE = "/referenceccdaservice/";	
	public static final String CODE_DISPLAYNAME_VALIDATION_URL = (IN_DEVELOPMENT_MODE ? CCDA_DEV_SERVER_URL
			: LOCAL_HOST_URL) + CODE_AND_DISPLAYNAME_IN_CODESYSTEM_SERVICE;
	public static final String CODE_VALUSET_VALIDATION_URL = (IN_DEVELOPMENT_MODE ? CCDA_DEV_SERVER_URL
			: LOCAL_HOST_URL) + CODE_IN_VALUESET_SERVICE;	
	public static final String CODE_CODESYSTEM_VALIDATION_URL = (IN_DEVELOPMENT_MODE ? CCDA_DEV_SERVER_URL
			: LOCAL_HOST_URL) + CODE_IN_CODESYSTEM_SERVICE;	
	public static final String REFERENCE_VALIDATOR_URL = (IN_DEVELOPMENT_MODE ? CCDA_DEV_SERVER_URL
			: LOCAL_HOST_URL) + REFERENCE_CCDA_SERVICE;
	
	public static final ArrayList<String> SMOKING_STATUS_CODES = new ArrayList<String>(
		    Arrays.asList("449868002", "428041000124106", "8517006","266919005","77176002","266927001","428071000124103","428061000124105"));
	
	public static final String SMOKING_STATUS_OBSERVATION_ID = "2.16.840.1.113883.10.20.22.4.78"; 
	public static final String IMMUNIZATION_ACTIVITY_ID = "2.16.840.1.113883.10.20.22.4.52"; 
	public static final String HITSP_VITAL_VALUESET_OID = "2.16.840.1.113883.3.88.12.80.62";
	public static final String PROBLEM_TYPE_VALUESET_OID = "2.16.840.1.113883.3.88.12.3221.7.2";
	
	public static final String SNOMEDCT_CODE_SYSTEM_NAME = "SNOMED-CT";
	public static final String SNOMEDCT_CODE_SYSTEM = "2.16.840.1.113883.6.96";
	
	public static final String LOINC_CODE_SYSTEM_NAME = "LOINC";
	public static final String LOINC_CODE_SYSTEM = "2.16.840.1.113883.6.1";
	
	public static final String ICD9CM_DIAGNOSIS_CODE_SYSTEM_NAME = "ICD9CM_DX";
	public static final String ICD9CM_DIAGNOSIS_CODE_SYSTEM = "2.16.840.1.113883.6.103";
	
	public static final String ICD9CM_PROCEDURE_CODE_SYSTEM_NAME = "ICD9CM_SG";
	public static final String ICD9CM_PROCEDURE_CODE_SYSTEM = "2.16.840.1.113883.6.104";
	
	public static final String ICD10CM_CODE_SYSTEM_NAME = "ICD10CM";
	public static final String ICD10CM_CODE_SYSTEM = "2.16.840.1.113883.6.90";
	
	public static final String ICD10PCS_CODE_SYSTEM_NAME = "ICD10PCS";
	public static final String ICD10PCS_CODE_SYSTEM = "2.16.840.1.113883.6.4";
	
	public static final String RXNORM_CODE_SYSTEM_NAME = "RXNORM";
	public static final String RXNORM_CODE_SYSTEM = "2.16.840.1.113883.6.88";
	
	public static final String CPT4_CODE_SYSTEM_NAME = "CPT-4";
	public static final String CPT4_CODE_SYSTEM = "2.16.840.1.113883.6.12";
	
	public static final String CVX_CODE_SYSTEM_NAME = "CVX";
	public static final String CVX_CODE_SYSTEM = "2.16.840.1.113883.12.292";
	
	public static final String VSAC_VALUESET_NAME = "VSAC";
	
	public static final String IG_URL = "http://www.hl7.org/implement/standards/product_brief.cfm?product_id=379";
	public static final String IG_SECTION_REFERENCES = "Section #.#.# CONF:ABC";
	public static final String TASKFORCE_URL = "http://wiki.hl7.org/index.php?title=CDA_Example_Task_Force";
	
	public static final Map<String, String> CODE_SYSTEM_MAP = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
	
	static {
		CODE_SYSTEM_MAP.put(SNOMEDCT_CODE_SYSTEM, SNOMEDCT_CODE_SYSTEM_NAME);
		CODE_SYSTEM_MAP.put(LOINC_CODE_SYSTEM, LOINC_CODE_SYSTEM_NAME);
		CODE_SYSTEM_MAP.put(ICD9CM_DIAGNOSIS_CODE_SYSTEM, ICD9CM_DIAGNOSIS_CODE_SYSTEM_NAME);
		CODE_SYSTEM_MAP.put(ICD9CM_PROCEDURE_CODE_SYSTEM, ICD9CM_PROCEDURE_CODE_SYSTEM_NAME);
		CODE_SYSTEM_MAP.put(ICD10CM_CODE_SYSTEM, ICD10CM_CODE_SYSTEM_NAME);
		CODE_SYSTEM_MAP.put(ICD10PCS_CODE_SYSTEM, ICD10PCS_CODE_SYSTEM_NAME);
		CODE_SYSTEM_MAP.put(RXNORM_CODE_SYSTEM, RXNORM_CODE_SYSTEM_NAME);
		CODE_SYSTEM_MAP.put(CPT4_CODE_SYSTEM, CPT4_CODE_SYSTEM_NAME);
		CODE_SYSTEM_MAP.put(CVX_CODE_SYSTEM, CVX_CODE_SYSTEM_NAME);
	}
	
	public static enum CATEGORIES
	{
		ALLERGIES("Allergies"),
		ENCOUNTERS("Encounters"), 
		IMMUNIZATIONS("Immunizations"),
		RESULTS("Laboratory Tests and Results"),
		MEDICATIONS("Medications"),
		MISC("Miscellaneous"),
		PATIENT("Patient Demographics"),
		PROBLEMS("Problems"),
		PROCEDURES("Procedures"),
		SOCIALHISTORY("Social History"),
		VITALS("Vital Signs");

		private String categoryDesc;

		private CATEGORIES(final String categoryDesc)
		{
			this.categoryDesc = categoryDesc;
		}

		public String getCategoryDesc()
		{
			return categoryDesc;
		}

	}
	
	
	public static enum CONCERNACT_STATUS
	{
		COMPLETED("Completed"),
		ACTIVE("Active"), 
		ABORTED("Aborted"),
		SUSPENDED("Suspended");

		private String status;

		private CONCERNACT_STATUS(final String status)
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
		RESOLVED("Resolved"),
		COMPLETED("Completed");

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
