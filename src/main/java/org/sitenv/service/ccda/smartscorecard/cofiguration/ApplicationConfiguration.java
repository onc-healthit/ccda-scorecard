package org.sitenv.service.ccda.smartscorecard.cofiguration;

public class ApplicationConfiguration {
	
	/**
	 * True allows setting default scorecard.xml values externally
	 * If using this configuration, set this to true, otherwise, ensure it is false
	 */
	public static final boolean OVERRIDE_SCORECARD_XML_CONFIG = true;
	
	/**
	 * True allows switching the various service URLs from the prod to the dev server and enables local logs
	 * Note: Never commit true as to ensure this is always set to false for production
	 */
	public static final boolean IN_DEVELOPMENT_MODE = false;
	
	/**
	 * True allows using local or custom servers
	 * Note: Never commit true as to ensure this is always set to false for production
	 */	
	public static final boolean IN_LOCAL_MODE = false;	
	
	/**
	 * The following value is only looked at if OVERRIDE_SCORECARD_XML_CONFIG == true
	 * When overridden, the URL property is set by ApplicationConstants.REFERENCE_VALIDATOR_URL
	 * True allows for 'C-CDA IG Conformance Errors' results
	 */
	public static final boolean IG_CONFORMANCE_CALL = false;
	
	/**
	 * The following value is only looked at if OVERRIDE_SCORECARD_XML_CONFIG == true
	 * When overridden, the URL property is set by ApplicationConstants.REFERENCE_VALIDATOR_URL
	 * True allows for '2015 Edition Certification Feedback' results
	 */
	public static final boolean CERTIFICATION_RESULTS_CALL = false;
	
	/**
	 * If not in dev mode, true uses healthit.gov URLS and servers for the service
	 * False uses sitenv.org
	 */	
	private static final boolean IS_PRODUCTION_DOT_GOV = false;	
	
	/**
	 * The cures update version of the ref val is currently hosted on the ccda
	 * servers. True enables cures validation within the scorecard by switching
	 * servers and passing the curesUpdate form data parameter
	 */
	public static final boolean CURES_UPDATE = true;
	
	public static final String 
			// Basic server URL definitions
			DEFAULT_LOCAL_SCORECARD_SERVER_URL = "http://localhost:8000",
			DEFAULT_LOCAL_REF_VAL_SERVER_URL = "http://localhost:8080",
			CCDA_DEV_SERVER_URL = "https://ccda.test.sitenv.org", // AHRQ TEST CCDA
			CCDA_PROD_SERVER_URL = "https://prodccda.sitenv.org",
			CCDA_GOV_PROD_SERVER_URL = "https://ccda.healthit.gov",
			TTP_DEV_SERVER_URL = "http://35.153.125.47", // AHRQ TEST James
			TTP_PROD_SERVER_URL = CURES_UPDATE ? CCDA_PROD_SERVER_URL : "https://ttpds.sitenv.org:8443",
			TTP_GOV_PROD_SERVER_URL = CURES_UPDATE ? CCDA_GOV_PROD_SERVER_URL : "https://james.healthit.gov",
			// Basic endpoint definitions	
			CODE_AND_DISPLAYNAME_IN_CODESYSTEM_SERVICE = "/referenceccdaservice/iscodeandisplaynameincodesystem",
			CODE_IN_VALUESET_SERVICE = "/referenceccdaservice/iscodeinvalueset",
			CODE_IN_CODESYSTEM_SERVICE = "/referenceccdaservice/iscodeincodesystem",
			REFERENCE_CCDA_SERVICE = "/referenceccdaservice/",
			CCDA_SCORECARD_SERVICE = "/scorecard/ccdascorecardservice2",
			SAVE_SCORECARD_SERVICE_BACKEND = "/scorecard/savescorecardservicebackend",
			SAVE_SCORECARD_SERVICE_BACKEND_SUMMARY = "/scorecard/savescorecardservicebackendsummary";
	
	// Final full endpoints based on flags
	// -Reference C-CDA Validator endpoints
	public static final String CODE_DISPLAYNAME_VALIDATION_URL = IN_LOCAL_MODE ? DEFAULT_LOCAL_REF_VAL_SERVER_URL
			: (IN_DEVELOPMENT_MODE ? TTP_DEV_SERVER_URL
					: (IS_PRODUCTION_DOT_GOV ? TTP_GOV_PROD_SERVER_URL : TTP_PROD_SERVER_URL))
					+ CODE_AND_DISPLAYNAME_IN_CODESYSTEM_SERVICE;
	public static final String CODE_VALUSET_VALIDATION_URL = IN_LOCAL_MODE ? DEFAULT_LOCAL_REF_VAL_SERVER_URL
			: (IN_DEVELOPMENT_MODE ? TTP_DEV_SERVER_URL
					: (IS_PRODUCTION_DOT_GOV ? TTP_GOV_PROD_SERVER_URL : TTP_PROD_SERVER_URL))
					+ CODE_IN_VALUESET_SERVICE;
	public static final String CODE_CODESYSTEM_VALIDATION_URL = IN_LOCAL_MODE ? DEFAULT_LOCAL_REF_VAL_SERVER_URL
			: (IN_DEVELOPMENT_MODE ? TTP_DEV_SERVER_URL
					: (IS_PRODUCTION_DOT_GOV ? TTP_GOV_PROD_SERVER_URL : TTP_PROD_SERVER_URL))
					+ CODE_IN_CODESYSTEM_SERVICE;
	public static final String REFERENCE_VALIDATOR_URL = IN_LOCAL_MODE ? DEFAULT_LOCAL_REF_VAL_SERVER_URL
			: (IN_DEVELOPMENT_MODE ? TTP_DEV_SERVER_URL
					: (IS_PRODUCTION_DOT_GOV ? TTP_GOV_PROD_SERVER_URL : TTP_PROD_SERVER_URL)) + REFERENCE_CCDA_SERVICE;
	// -Scorecard endpoints
	public static final String SAVESCORECARDSERVICEBACKEND_URL = IN_LOCAL_MODE ? DEFAULT_LOCAL_SCORECARD_SERVER_URL
			: (IN_DEVELOPMENT_MODE ? CCDA_DEV_SERVER_URL
					: (IS_PRODUCTION_DOT_GOV ? CCDA_GOV_PROD_SERVER_URL : CCDA_PROD_SERVER_URL))
					+ SAVE_SCORECARD_SERVICE_BACKEND;
	public static final String SAVESCORECARDSERVICEBACKENDSUMMARY_URL = SAVESCORECARDSERVICEBACKEND_URL;
	public static final String CCDASCORECARDSERVICE_URL = IN_LOCAL_MODE ? DEFAULT_LOCAL_SCORECARD_SERVER_URL
			: (IN_DEVELOPMENT_MODE ? CCDA_DEV_SERVER_URL
					: (IS_PRODUCTION_DOT_GOV ? CCDA_GOV_PROD_SERVER_URL : CCDA_PROD_SERVER_URL))
					+ CCDA_SCORECARD_SERVICE;
	
	public static final int CORE_POOL_SIZE = 200;
	public static final int MAX_POOL_SIZE = 500;
	public static final int QUEUE_CAPACITY = 10;
	
}
