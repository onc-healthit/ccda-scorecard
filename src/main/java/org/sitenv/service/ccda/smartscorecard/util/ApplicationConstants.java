package org.sitenv.service.ccda.smartscorecard.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
	
	public static final String TEMPLATEID_DESC = "All Template Ids should be Valid";
	public static final String TEMPLATEID_REQ = "All Template Ids should be correct";
	
	public static final String UNIQUEID_DESC = "Instance Identifiers should be unique";
	public static final String UNIQUEID_REQ = "Generally, the identifiers found within a CDA document should be unique and non-reoccurring within the same document.";
	
	
	public static final String IMMU_CODE_REQ = "Immunizations coded with CVX codes";
	public static final String IMMU_CODE_DESC = "Each Immunization code should be validated aganist CVX Vaccines Administered valueset";
	
	public static final String MEDICATION_CODE_REQ = "Medications coded with RxNorm SCD, SBD, GPCK, or BPCPK codes";
	public static final String MEDICATION_CODE_DESC = "C-CDA medication lists should contain medications coded as RxNorm Semantic Clinical Drugs, "
							+ "Semantic Branded Drugs, and packs. This means prescribable products on the level of 'loratadine 10mg oral tablet'.";
	
	
	
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
	
	public static final String ALLERGIES_CONCERN_DATE_ALIGN_REQ = "Allergies observation effective should align with Allergies concern act effective time";
	public static final String ALLERGIES_CONCERN_DATE_ALIGN_DESC ="Allergies observation effective should align with Allergies concern act effective time";
	
	public static final String PROCEDURES_CODE_DISPLAYNAME_REQUIREMENT = "All code elements under Procedures section should contain valid display names";	
	
	
	public static final String SOCIALHISTORY_TIME_PRECISION_REQUIREMENT = "All effective time elements under Social History section should contain "
																			+ "proper precision and format with correct offset";
	public static final String SOCIALHISTORY_TIMEDATE_VALID_REQUIREMENT = "All effective time elements under Social History section should contain "
																		+ "valid date and time value within human life span";
	public static final String SOCIALHISTORY_CODE_DISPLAYNAME_REQUIREMENT = "All code elements under Social History section should contain valid display names";	
	public static final String SOCIALHISTORY_SMOKING_STATUS_REQUIREMENT = "Smoking status code value should be valid";
	public static final String SOCIALHISTORY_SMOKING_STATUS_OBS_ID_REQUIREMENT = "Smoking status observation Template Id should be valid";
	
	public static final String NARRATIVE_STRUCTURE_ID_REQ = "Each entry has to be linked to related narrative text";
	public static final String NARRATIVE_STRUCTURE_ID_DESC = "Each entry has to be linked to related narrative text";
	
	public static final String EMPTY_DOC_ERROR_MESSAGE = "Given C-CDA document is empty or invalid. Please upload a valid C-CDA Document.";
	public static final String EXCEPTION_ERROR_MESSAGE = "Our system experienced some unexpected error, please try after some time or email your issue to TestingServices@sitenv.org";
	
	public static final String MONTH_FORMAT = "yyyyMM";
	public static final String MONTH_PATTERN = "\\d{6}";
	
	public static final String DAY_FORMAT = "yyyyMMdd";
	public static final String DAY_PATTERN = "\\d{8}";
	
	public static final String MINUTE_FORMAT = "yyyyMMddHHmmZ";
	public static final String MINUTE_PATTERN = "\\d{12}-\\d{4}";
	
	public static final String SECOND_FORMAT = "yyyyMMddHHmmssZ";
	public static final String SECOND_PATTERN = "\\d{14}-\\d{4}";
	
	// set DEFAULT_LOCAL_SERVER_URL according to local tomcat URL
	public static final String DEFAULT_LOCAL_SERVER_URL = "http://localhost:8000",
			CCDA_DEV_SERVER_URL = "https://devccda.sitenv.org",
			CCDA_PROD_SERVER_URL = "https://prodccda.sitenv.org",
			CODE_AND_DISPLAYNAME_IN_CODESYSTEM_SERVICE = "/referenceccdaservice/iscodeandisplaynameincodesystem",
			CODE_IN_VALUESET_SERVICE = "/referenceccdaservice/iscodeinvalueset",
			CODE_IN_CODESYSTEM_SERVICE = "/referenceccdaservice/iscodeincodesystem",
			REFERENCE_CCDA_SERVICE = "/referenceccdaservice/",
			CCDA_SCORECARD_SERVICE = "/scorecard/ccdascorecardservice2",
			SAVE_SCORECARD_SERVICE_BACKEND = "/scorecard/savescorecardservicebackend";
	// ensure when WAR is headed to the dev server that DEFAULT_LOCAL_SERVER_URL
	// is replaced with CCDA_DEV_SERVER_URL in the following Strings
	public static final String CODE_DISPLAYNAME_VALIDATION_URL = (IN_DEVELOPMENT_MODE ? CCDA_DEV_SERVER_URL
			: CCDA_PROD_SERVER_URL)
			+ CODE_AND_DISPLAYNAME_IN_CODESYSTEM_SERVICE;
	public static final String CODE_VALUSET_VALIDATION_URL = (IN_DEVELOPMENT_MODE ? CCDA_DEV_SERVER_URL
			: CCDA_PROD_SERVER_URL)
			+ CODE_IN_VALUESET_SERVICE;
	public static final String CODE_CODESYSTEM_VALIDATION_URL = (IN_DEVELOPMENT_MODE ? CCDA_DEV_SERVER_URL
			: CCDA_PROD_SERVER_URL)
			+ CODE_IN_CODESYSTEM_SERVICE;
	public static final String REFERENCE_VALIDATOR_URL = (IN_DEVELOPMENT_MODE ? CCDA_DEV_SERVER_URL
			: CCDA_PROD_SERVER_URL)
			+ REFERENCE_CCDA_SERVICE;
	public static final String SAVESCORECARDSERVICEBACKEND_URL = (IN_DEVELOPMENT_MODE ? DEFAULT_LOCAL_SERVER_URL
			: CCDA_PROD_SERVER_URL)
			+ SAVE_SCORECARD_SERVICE_BACKEND;
	public static final String CCDASCORECARDSERVICE_URL = (IN_DEVELOPMENT_MODE ? DEFAULT_LOCAL_SERVER_URL
			: CCDA_PROD_SERVER_URL)
			+ CCDA_SCORECARD_SERVICE;
	
	public static final ArrayList<String> SMOKING_STATUS_CODES = new ArrayList<String>(
		    Arrays.asList("449868002", "428041000124106", "8517006","266919005","77176002","266927001","428071000124103","428061000124105"));
	
	public static final String SMOKING_STATUS_OBSERVATION_ID = "2.16.840.1.113883.10.20.22.4.78"; 
	public static final String IMMUNIZATION_ACTIVITY_ID = "2.16.840.1.113883.10.20.22.4.52"; 
	public static final String MEDICATION_ACTIVITY_ID = "2.16.840.1.113883.10.20.22.4.16";
	public static final String HITSP_VITAL_VALUESET_OID = "2.16.840.1.113883.3.88.12.80.62";
	public static final String PROBLEM_TYPE_VALUESET_OID = "2.16.840.1.113883.3.88.12.3221.7.2";
	public static final String CVX_CODES_VALUSET_OID = "2.16.840.1.113762.1.4.1010.6";
	public static final String MEDICATION_CLINICAL_DRUG_VALUSET_OID = "2.16.840.1.113762.1.4.1010.4";
	
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
	
	public static final String TEMPLATEID_XPATH = "./templateId";
	
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
	
	public static final Map<String, String> SECTION_TEMPLATEID_MAP = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
	
	static {
		SECTION_TEMPLATEID_MAP.put("2.16.840.1.113883.10.20.22.2.6.1", CATEGORIES.ALLERGIES.getCategoryDesc());
		SECTION_TEMPLATEID_MAP.put("2.16.840.1.113883.10.20.22.2.22.1", CATEGORIES.ENCOUNTERS.getCategoryDesc());
		SECTION_TEMPLATEID_MAP.put("2.16.840.1.113883.10.20.22.2.2.1", CATEGORIES.IMMUNIZATIONS.getCategoryDesc());
		SECTION_TEMPLATEID_MAP.put("2.16.840.1.113883.10.20.22.2.3.1", CATEGORIES.RESULTS.getCategoryDesc());
		SECTION_TEMPLATEID_MAP.put("2.16.840.1.113883.10.20.22.2.1.1", CATEGORIES.MEDICATIONS.getCategoryDesc());
		SECTION_TEMPLATEID_MAP.put("2.16.840.1.113883.10.20.22.2.5.1", CATEGORIES.PROBLEMS.getCategoryDesc());
		SECTION_TEMPLATEID_MAP.put("2.16.840.1.113883.10.20.22.2.7.1", CATEGORIES.PROCEDURES.getCategoryDesc());
		SECTION_TEMPLATEID_MAP.put("2.16.840.1.113883.10.20.22.2.17", CATEGORIES.SOCIALHISTORY.getCategoryDesc());
		SECTION_TEMPLATEID_MAP.put("2.16.840.1.113883.10.20.22.2.4.1", CATEGORIES.VITALS.getCategoryDesc());
		SECTION_TEMPLATEID_MAP.put(CATEGORIES.PATIENT.getCategoryDesc(), CATEGORIES.PATIENT.getCategoryDesc());
	}
	
	
	public static final List<String> referenceValidatorErrorList = new ArrayList<>(Arrays.asList("C-CDA MDHT Conformance Error", 
																			"ONC 2015 S&CC Vocabulary Validation Conformance Error",
																			"ONC 2015 S&CC Reference C-CDA Validation Error"));

	
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
	
	public static class ErrorMessages {
		public static final String CONTACT = "Please report this issue to TestingServices@sitenv.org.";
		public static final String GENERIC = "An Unknown error has occurred. ";
		public static final String GENERIC_WITH_CONTACT = "An Unknown error has occurred. "
				+ CONTACT;
		public static final String GENERIC_DIFFERENT_FILE_OR_TIME = "Please try a different file or try again at another time.";
		public static final String JSON_TO_JAVA_JACKSON = "An error occurred while converting the Scorecard service JSON response to a Java object via the Jackson API.";
		public static final String RESULTS_ARE_NULL = "Note for the developers: The ResponseTO results are null.";
		public static final String IS_SUCCESS_FALSE = "Note for the developers: isSuccess is equal to false.";
		public static final String NULL_RESULT_ON_SAVESCORECARDSERVICEBACKEND_CALL = "Error: savescorecardservicebackend did not receive any results (null) from ccdascorecardservice."
				+ " " + GENERIC_DIFFERENT_FILE_OR_TIME;
		public static final String UNSTRUCTURED_DOCUMENT = "The supplied C-CDA XML document has been identified as an Unstructured Document "
				+ "urn:hl7ii:2.16.840.1.113883.10.20.22.1.10. The C-CDA Scorecard tool does not score this document type. "
				+ "Please try submitting another document type for review such as a Continuity of Care Document, Care Plan, etc.";
		public static final String SCHEMA_ERRORS_GENERIC = "Schema errors must be addressed before a score can be provided.";
	}

	public static enum IG_REFERENCES
	{
		RECORD_TARGET("Section 1.1.1.2: recordTarget"),
		ENCOUNTER_ACTIVITY("Section 3.23: Encounter Actvity"),
		ENCOUNTER_SECTION("Section 2.16: Encounters"),
		ALLERGY_CONCERN("Section 3.5: Allergy Concern Act"),
		ALLERGY_SECTION("Section 2.4.1: Allergies and Intolerances Section"),
		PROBLEM_OBSERVATION("Section 3.79: Problem Observation"), 
		PROBLEM_SECTION("Section 2.53.1: Problem Section"),
		PROBLEM_CONCERN_ACT("Section 3.78: Problem Concern Act"),
		MEDICATION_SECTION("Section 2.39.1: Medications Section"), 
		MEDICATION_ACTIVITY("Section 3.48 Medication Activity"),
		IMMUNIZATION_ACTIVITY("Section 3.41: Immunization Activity"), 
		IMMUNIZATION_SECTION("Section 2.32.1: Immunizations Section"),
		SOCIAL_HISTORY_SECTION("Section 2.66: Social History Section"), 
		SMOKING_STATUS("Section 3.100: Smoking Status - Meaningful Use"),
		SOCIAL_HISTORY_OBSERVATION("Section 3.101: Social History Observation"),
		TOBACCO_USE("Section 3.107: Tobacco Use"),
		RESULT_SECTION("Section 2.64.1: Results Section"),
		RESULT_ORGANIZER("Section 3.93: Result Organizer"),
		RESULT_OBSERVATION("Section 3.92: Result Observation"),
		VITAL_SIGN_ORGANIZER("Section 3.109: Vital Signs Organizer"),
		VITAL_SIGN_OBSERVATION("Section 3.108: Vital Sign Observation"), 
		VITAL_SIGN_SECTION("Section 2.70.1: Vital Signs Section"),
		PROCEDURE_SECTION("Section 2.61.1 :Procedures Section"),
		PROCEDURE_ACTIVITY_OBSERVATION("Section 3.82 Procedure Activity Observation"),
		TEMPLATE_IDS("Section 5.0 TEMPLATE IDS IN THIS GUIDE");
		
		
		private String igReferences; 
		
		
		private IG_REFERENCES(final String igReferences)
		{
			this.igReferences = igReferences;
		}

		public String getIgReference()
		{
			return igReferences;
		}
	}
	
	
	public static enum IG_REFERENCES_R1
	{
		RECORD_TARGET("Section 2.2.1 RecordTarget"),
		ENCOUNTER_ACTIVITY("Section 5.21 Encounter Activities"),
		ENCOUNTER_SECTION("Section 4.11 Encounters"),
		ALLERGY_CONCERN("Section 5.5 Allergy Problem Act"),
		ALLERGY_SECTION("Section 4.2 Allergies"),
		PROBLEM_OBSERVATION("Section 5.59	Problem Observation"), 
		PROBLEM_SECTION("Section 4.44 Problem Section"),
		PROBLEM_CONCERN_ACT("Section 5.58 Problem Concern Act"),
		MEDICATION_SECTION("Section 4.33 Medications Section "), 
		MEDICATION_ACTIVITY("Section 5.39 Medication Activity"),
		IMMUNIZATION_ACTIVITY("Section 5.34 Immunization Activity"), 
		IMMUNIZATION_SECTION("Section 4.27 Immunizations Section"),
		SOCIAL_HISTORY_SECTION("Section 4.57 Social History Section"), 
		SMOKING_STATUS("Section 5.75 Smoking Status Observation"),
		SOCIAL_HISTORY_OBSERVATION("Section 5.76 Social History Observation"),
		TOBACCO_USE("Section 5.80 Tobacco Use"),
		RESULT_SECTION("Section 4.55 Results Section"),
		RESULT_ORGANIZER("Section 5.71 Result Organizer"),
		RESULT_OBSERVATION("Section 5.70 Result Observation"),
		VITAL_SIGN_ORGANIZER("Section 5.82 Vital Signs Organizer "),
		VITAL_SIGN_OBSERVATION("Section 5.81 Vital Sign Observation"), 
		VITAL_SIGN_SECTION("Section 4.60 Vital Sign Section"),
		PROCEDURE_SECTION("Section 4.52 Procedures Section"),
		PROCEDURE_ACTIVITY_OBSERVATION("Section 5.62 Procedure Activity Observation"),
		TEMPLATE_IDS("Appendix C : Template ID's in this Guide");
		
		
		private String igReferences; 
		
		
		private IG_REFERENCES_R1(final String igReferences)
		{
			this.igReferences = igReferences;
		}

		public String getIgReference()
		{
			return igReferences;
		}
	}
	
	public static enum TASKFORCE_LINKS
	{
		
		PATIENT("https://github.com/jddamore/HL7-Task-Force-Examples/blob/master/DEMO_Record_Target_Example.xml"),
		ENCOUNTERS("https://github.com/jddamore/HL7-Task-Force-Examples/blob/master/ENC_Encounter_hospitalization_with_diagnoses.xml"),
		ALLERGIES("https://github.com/brettmarquard/HL7-C-CDA-Task-Force-Examples/blob/master/No_Known_Allergies_Status_with_Author_Timestamp.xml"),
		PROBLEMS("https://github.com/brettmarquard/HL7-C-CDA-Task-Force-Examples/blob/master/No_Known_Problems_Section_20140226.xml"),
		MEDICATIONS("https://github.com/jddamore/HL7-Task-Force-Examples/blob/master/MED_Oral_Med_QID_with_PRN.xml"),
		IMMUNIZATIONS("https://github.com/brettmarquard/HL7-C-CDA-Task-Force-Examples/blob/master/Unknown_Immunization_Status_R2.xml"),
		SOCIALHISTORY("https://github.com/jddamore/HL7-Task-Force-Examples/blob/master/SMOKING_Former_Smoker.xml"),
		RESULTS("https://github.com/jddamore/HL7-Task-Force-Examples/blob/master/RESULT_Value_Less_Than_Physical_Quantity.xml"),
		RESULTS_UCUM("https://github.com/jddamore/HL7-Task-Force-Examples/blob/master/RESULT_Value_UCUM_Translation.xml"),
		VITALSIGNS("https://github.com/jddamore/HL7-Task-Force-Examples/blob/master/VITALS_Complete_Panel_Metric.xml"),
		PROCEDURES("https://github.com/jddamore/HL7-Task-Force-Examples/blob/master/PROCEDURE_Observation_Example.xml");
		
		private String taskforceLink;
		
		private TASKFORCE_LINKS(final String taskforceLink)
		{
			this.taskforceLink = taskforceLink;
		}

		public String getTaskforceLink() {
			return taskforceLink;
		}
	}
	
	public static enum VALIDATION_OBJECTIVES
	{
		CCDA_IG_PLUS_VOCAB("CCDA IG Plus Vocab"),
		CERTIFICATION_B1_CCD_DS_RN_OBJECTIVE("170_315_b1_ToC_Amb"),
		CERTIFICATION_B9_CP_OBJECTIVE("170_315_b9_CP_Amb");
		
		
		private String validationObjective; 
		
		
		private VALIDATION_OBJECTIVES(final String validationObjective)
		{
			this.validationObjective = validationObjective;
		}

		public String getValidationObjective()
		{
			return validationObjective;
		}
	}
}
