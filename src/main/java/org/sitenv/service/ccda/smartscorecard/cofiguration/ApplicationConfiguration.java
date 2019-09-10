package org.sitenv.service.ccda.smartscorecard.cofiguration;

public class ApplicationConfiguration {

	/**
	 * True allows switching the various service URLs from the prod to the dev server and enables local logs
	 * Note: Never commit true as to ensure this is always set to false for production
	 */
	public static final boolean IN_DEVELOPMENT_MODE = false;
	/**
	 * True allows setting default scorecard.xml values externally
	 */
	public static final boolean OVERRIDE_SCORECARD_XML_CONFIG = true;
	/**
	 * The following value is only looked at if OVERRIDE_SCORECARD_XML_CONFIG == true
	 * When overridden, the URL property is set by ApplicationConstants.REFERENCE_VALIDATOR_URL
	 * True allows for 'C-CDA IG Conformance Errors' results
	 */
	public static final boolean IG_CONFORMANCE_CALL = true;
	/**
	 * The following value is only looked at if OVERRIDE_SCORECARD_XML_CONFIG == true
	 * When overridden, the URL property is set by ApplicationConstants.REFERENCE_VALIDATOR_URL
	 * True allows for '2015 Edition Certification Feedback' results
	 */
	public static final boolean CERTIFICATION_RESULTS_CALL = true;
	
	// set DEFAULT_LOCAL_SERVER_URL according to local tomcat URL
	public static final String DEFAULT_LOCAL_SERVER_URL = "http://localhost:8000",
			CCDA_DEV_SERVER_URL = "https://devccda.sitenv.org", // TODO: update this url to the HHS server we will be using
			CCDA_PROD_SERVER_URL = "https://prodccda.sitenv.org",
			TTP_DEV_SERVER_URL = "52.44.175.145/referenceccdaservice", // HHS dev James
			TTP_PROD_SERVER_URL = "https://ttpds.sitenv.org:8443",
			CODE_AND_DISPLAYNAME_IN_CODESYSTEM_SERVICE = "/referenceccdaservice/iscodeandisplaynameincodesystem",
			CODE_IN_VALUESET_SERVICE = "/referenceccdaservice/iscodeinvalueset",
			CODE_IN_CODESYSTEM_SERVICE = "/referenceccdaservice/iscodeincodesystem",
			REFERENCE_CCDA_SERVICE = "/referenceccdaservice/",
			CCDA_SCORECARD_SERVICE = "/scorecard/ccdascorecardservice2",
			SAVE_SCORECARD_SERVICE_BACKEND = "/scorecard/savescorecardservicebackend",
			SAVE_SCORECARD_SERVICE_BACKEND_SUMMARY = "/scorecard/savescorecardservicebackendsummary";
	// ensure when WAR is headed to the dev server that DEFAULT_LOCAL_SERVER_URL
	// is replaced with CCDA_DEV_SERVER_URL in the following Strings
	public static final String CODE_DISPLAYNAME_VALIDATION_URL = (IN_DEVELOPMENT_MODE ? TTP_DEV_SERVER_URL
			: TTP_PROD_SERVER_URL)
			+ CODE_AND_DISPLAYNAME_IN_CODESYSTEM_SERVICE;
	public static final String CODE_VALUSET_VALIDATION_URL = (IN_DEVELOPMENT_MODE ? TTP_DEV_SERVER_URL
			: TTP_PROD_SERVER_URL)
			+ CODE_IN_VALUESET_SERVICE;
	public static final String CODE_CODESYSTEM_VALIDATION_URL = (IN_DEVELOPMENT_MODE ? TTP_DEV_SERVER_URL
			: TTP_PROD_SERVER_URL)
			+ CODE_IN_CODESYSTEM_SERVICE;
	public static final String REFERENCE_VALIDATOR_URL = (IN_DEVELOPMENT_MODE ? TTP_DEV_SERVER_URL
			: TTP_PROD_SERVER_URL)
			+ REFERENCE_CCDA_SERVICE;
	public static final String SAVESCORECARDSERVICEBACKEND_URL = (IN_DEVELOPMENT_MODE ? CCDA_DEV_SERVER_URL
			: CCDA_PROD_SERVER_URL)
			+ SAVE_SCORECARD_SERVICE_BACKEND;
	public static final String SAVESCORECARDSERVICEBACKENDSUMMARY_URL = (IN_DEVELOPMENT_MODE ? CCDA_DEV_SERVER_URL
			: CCDA_PROD_SERVER_URL)
			+ SAVE_SCORECARD_SERVICE_BACKEND;
	public static final String CCDASCORECARDSERVICE_URL = (IN_DEVELOPMENT_MODE ? CCDA_DEV_SERVER_URL
			: CCDA_PROD_SERVER_URL)
			+ CCDA_SCORECARD_SERVICE;	
	
	public static final int CORE_POOL_SIZE = 200;
	public static final int MAX_POOL_SIZE = 500;
	public static final int QUEUE_CAPACITY = 10;
	
}
