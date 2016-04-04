package org.sitenv.service.ccda.smartscorecard.util;

import java.util.HashMap;
import java.util.Map;

public class ApplicationConstants {
	
	public static String FILEPATH = "C:/Projects/Dragon/CCDAParser/170.315_b1_toc_amb_ccd_r21_sample1_v1.xml";
	
	public static String PATIENT_CONTACT_REQUIREMENT = "Patient in CCDA should contain proper address and telecom information";
	public static String PATIENT_DOB_REQUIREMENT = "Patient in CCDA should contain DOB";
	public static String PATIENT_LANG_IND_REQUIREMENT = "Patient in CCDA should contain language indicator value as true or false";
	
	
	
	public static String ENCOUNTER_SECTION_REQUIREMENT = "CCDA document should contain encounter section";
	public static String ENCOUNTER_SDL_REQUIREMENT = "Service delivery location should be present within each encounter activity";
	public static String ENCOUNTER_INDICATION_DESC = "Indication entry should be present within each encounter activity";
	
	
	public static String MEDICATION_SECTION_REQUIREMENT = "CCDA document should contain encounter section";
	
	public static String PROBLEMS_SECTION_REQUIREMENT = "CCDA document should contain problem section";
	
	public static String IMMUNIZATION_SECTION_REQUIREMENT = "CCDA document should contain immunization section";
	
	public static String LABRESULTS_SECTION_REQUIREMENT = "CCDA document should contain lab results section";
	
	public static String VITALS_SECTION_REQUIREMENT = "CCDA document should contain vitals section";
	
	public static String ALLERGIES_SECTION_REQUIREMENT = "CCDA document should contain allergies section";
	
	public static String PROCEDURES_SECTION_REQUIREMENT = "CCDA document should contain procedures section";
	
	
	
	
	public static final Map<Integer , String> PATIENT_CONTACT_POINTS = new HashMap<Integer , String>() {/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

	{
	    put(0, "Address and telecom are not present");
	    put(1, "Either address or telecom present");
	    put(2, "Both address and telecom present");
	}};
	
	public static final Map<Integer , String> PATIENT_DOB_POINTS = new HashMap<Integer , String>() {/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

	{
	    put(0, "DOB value not present");
	    put(1, "DOB value  present");
	}};
	
	public static final Map<Integer , String> PATIENT_LANG_IND_POINTS = new HashMap<Integer , String>() {/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

	{
	    put(0, "Language indicator value not present");
	    put(1, "Language indicator value present");
	}};
	
	
	public static final Map<Integer , String> ENCOUNTER_SECTION_POINTS = new HashMap<Integer , String>() {/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

	{
	    put(0, "Encounter section is absent");
	    put(1, "Encounter section is present");
	}};
	
	public static final Map<Integer , String> ENCOUNTER_SDL_POINTS = new HashMap<Integer , String>() {/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

	{
	    put(0, "None of the encunter activity contains SDL element");
	    put(1, "> 0% and < 25% encounter activities have SDL element");
	    put(2, "> 25% and < 50% encounter activities have SDL element");
	    put(3, "> 50% and < 75% encounter activities have SDL element");
	    put(4, "> 75% and < 100% encounter activities have SDL element");
	    put(5, "All encounter activities contain SDL element");
	}};
	
	
	public static final Map<Integer , String> ENCOUNTER_INDICATION_POINTS = new HashMap<Integer , String>() {/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

	{
	    put(0, "Indication entry absent within encounter activity");
	    put(1, "Indication entry present within encounter activity");
	}};
	
	public static final Map<Integer , String> PROBLEMS_SECTION_POINTS = new HashMap<Integer , String>() {/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

	{
	    put(0, "Problmes section is absent");
	    put(1, "Problems section is present");
	}};
	
	public static final Map<Integer , String> IMMUNIZATION_SECTION_POINTS = new HashMap<Integer , String>() {/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

	{
	    put(0, "Immunization section is absent");
	    put(1, "Immunization section is present");
	}};
	
	public static final Map<Integer , String> MEDICATION_SECTION_POINTS = new HashMap<Integer , String>() {/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

	{
	    put(0, "Medication section is absent");
	    put(1, "Medication section is present");
	}};
	
	public static final Map<Integer , String> LABRESULTS_SECTION_POINTS = new HashMap<Integer , String>() {/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

	{
	    put(0, "Lab results section is absent");
	    put(1, "Lab results section is present");
	}};
	
	public static final Map<Integer , String> VITALS_SECTION_POINTS = new HashMap<Integer , String>() {/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

	{
	    put(0, "Vitals section is absent");
	    put(1, "Vitals section is present");
	}};
	
	public static final Map<Integer , String> ALLERGIES_SECTION_POINTS = new HashMap<Integer , String>() {/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

	{
	    put(0, "Allergies section is absent");
	    put(1, "Allergies section is present");
	}};
	
	public static final Map<Integer , String> PROCEDURES_SECTION_POINTS = new HashMap<Integer , String>() {/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

	{
	    put(0, "Procedures section is absent");
	    put(1, "Procedures section is present");
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
		PATIENT_CONTACT("Contact", 2),
		PATIENT_LANG_IND("LanguageIndicator",1), 
		PATIENT_DOB("DOB",1),
		PATIENT_CTM("CareTeamMembers",1),
		SECTION("Section",1),
		ENCOUNTER_SDL("Service Delivery location",5),
		ENCOUNTER_INDICATION("Indication",1);

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

}
