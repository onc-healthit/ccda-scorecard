package org.sitenv.service.ccda.smartscorecard.configuration;

public class ApplicationConfiguration {
	
	/**
	 * True allows setting default scorecard.xml values externally
	 * If using this configuration, set this to true, otherwise, ensure it is false
	 */
	public static final boolean OVERRIDE_SCORECARD_XML_CONFIG = false;
	
	/**
	 * Options are defined in org.sitenv.service.ccda.smartscorecard.configuration.ApplicationConfiguration.Environment
	 */
	public static final Environment ENV = Environment.DOT_GOV_PROD;
	
	/**
	 * The following value is only looked at if OVERRIDE_SCORECARD_XML_CONFIG is true
	 * True allows for 'C-CDA IG Conformance Errors' results
	 */
	public static final boolean IG_CONFORMANCE_CALL = true;
	
	/**
	 * The following value is only looked at if OVERRIDE_SCORECARD_XML_CONFIG is true
	 * True allows for '2015 Edition Certification Feedback' results
	 */
	public static final boolean CERTIFICATION_RESULTS_CALL = true;		
		
	/**
	 * The cures update version of the Reference Validator is currently hosted on the C-CDA
	 * servers. True enables cures validation within the scorecard by switching
	 * servers and passing the curesUpdate form data parameter
	 */
	public static final boolean CURES_UPDATE = true;
	
	public static final String 
		// Basic server URL definitions
		DEFAULT_LOCAL_SCORECARD_SERVER_URL = "http://localhost:8000",
		DEFAULT_LOCAL_REF_VAL_SERVER_URL = "http://localhost:8080",		
		
		//CCDA_DEV_SERVER_URL = "http://34.195.107.72", // AHRQ DEV CCDA
		
		CCDA_DEV_SERVER_URL = "https://ccda.dev.sitenv.org", // AHRQ DEV CCDA
		CCDA_TEST_SERVER_URL = "http://34.236.48.201", // AHRQ TEST CCDA
		
		CCDA_GOV_PROD_SERVER_URL = "https://ccda.healthit.gov", // new ahrq dot gov prod
		CCDA_PROD_SERVER_URL = "https://prodccda.sitenv.org", // old aws prod
		
		/// Server URL Modifications
		// TTP URLs are set based on CURES_UPDATE which means they could end up as a CCDA server URL if CURES_UPDATE is true
		// CURRENT DEV AND TEST
		TTP_DEV_SERVER_URL = CURES_UPDATE ? CCDA_DEV_SERVER_URL : "http://52.44.175.145", // AHRQ DEV James
		TTP_TEST_SERVER_URL = CURES_UPDATE ? CCDA_TEST_SERVER_URL : "http://35.153.125.47", // AHRQ TEST James		
		// CURRENT PROD
		TTP_GOV_PROD_SERVER_URL = CURES_UPDATE ? CCDA_GOV_PROD_SERVER_URL : "https://james.healthit.gov",				
	    // OLD / UNUSED NOW
	    TTP_PROD_SERVER_URL = CURES_UPDATE ? CCDA_PROD_SERVER_URL : "https://ttpds.sitenv.org:8443",
		
		// Basic endpoint definitions	
		CODE_AND_DISPLAYNAME_IN_CODESYSTEM_SERVICE = "/referenceccdaservice/iscodeandisplaynameincodesystem",
		CODE_IN_VALUESET_SERVICE = "/referenceccdaservice/iscodeinvalueset",
		CODE_IN_CODESYSTEM_SERVICE = "/referenceccdaservice/iscodeincodesystem",
		REFERENCE_CCDA_SERVICE = "/referenceccdaservice/",
		CCDA_SCORECARD_SERVICE_APPLICATION_NAME = "ccdascorecardserviceinternal",
		CCDA_SCORECARD_SERVICE = "/scorecard/" + CCDA_SCORECARD_SERVICE_APPLICATION_NAME,
		SAVE_SCORECARD_SERVICE_BACKEND = "/scorecard/savescorecardservicebackend",
		SAVE_SCORECARD_SERVICE_BACKEND_SUMMARY = "/scorecard/savescorecardservicebackendsummary";	
	
	// Resolves final full endpoints based on flags
	// -Reference C-CDA Validator endpoints
	private static String server = ENV.server(EndpointType.RefVal);
	public static final String 
		CODE_DISPLAYNAME_VALIDATION_URL = server + CODE_AND_DISPLAYNAME_IN_CODESYSTEM_SERVICE,
		CODE_VALUSET_VALIDATION_URL = server + CODE_IN_VALUESET_SERVICE,
		CODE_CODESYSTEM_VALIDATION_URL = server + CODE_IN_CODESYSTEM_SERVICE,
		REFERENCE_VALIDATOR_URL = server + REFERENCE_CCDA_SERVICE;
	// -Scorecard endpoints (updating the same server variable to reuse programatically for next config)
	static {
		server = ENV.server(EndpointType.Scorecard);
	}
	public static final String 
		SAVESCORECARDSERVICEBACKEND_URL = server + SAVE_SCORECARD_SERVICE_BACKEND,
		SAVESCORECARDSERVICEBACKENDSUMMARY_URL = server + SAVE_SCORECARD_SERVICE_BACKEND,
		CCDASCORECARDSERVICE_URL = server + CCDA_SCORECARD_SERVICE;
	
	public enum EndpointType {
		RefVal, Scorecard
	}
	
	public enum Environment {
		LOCAL_OR_CUSTOM {
			@Override
			public String server(EndpointType type) {
				return type == EndpointType.RefVal ? DEFAULT_LOCAL_REF_VAL_SERVER_URL
						: DEFAULT_LOCAL_SCORECARD_SERVER_URL;
			}
		}, 
		DEV {
			@Override
			public String server(EndpointType type) {
				return type == EndpointType.RefVal ? TTP_DEV_SERVER_URL
						: CCDA_DEV_SERVER_URL;
			}
		},
		TEST {
			@Override
			public String server(EndpointType type) {
				return type == EndpointType.RefVal ? TTP_TEST_SERVER_URL
						: CCDA_TEST_SERVER_URL;
			}
		},
	    // OLD / UNUSED NOW
		PROD {
			@Override
			public String server(EndpointType type) {
				return type == EndpointType.RefVal ? TTP_PROD_SERVER_URL
						: CCDA_PROD_SERVER_URL;
			}
		},
		// CURRENT PROD
		DOT_GOV_PROD {
			@Override
			public String server(EndpointType type) {
				return type == EndpointType.RefVal ? TTP_GOV_PROD_SERVER_URL
						: CCDA_GOV_PROD_SERVER_URL;
			}
		},
		PROD_REF_VAL_WITH_LOCAL_OR_CUSTOM_SCORECARD {
			@Override
			public String server(EndpointType type) {
				return type == EndpointType.RefVal ? TTP_PROD_SERVER_URL
						: DEFAULT_LOCAL_SCORECARD_SERVER_URL;
			}
		},
		DOR_GOV_PROD_REF_VAL_WITH_LOCAL_OR_CUSTOM_SCORECARD {
			@Override
			public String server(EndpointType type) {
				return type == EndpointType.RefVal ? TTP_GOV_PROD_SERVER_URL
						: DEFAULT_LOCAL_SCORECARD_SERVER_URL;
			}
		};		
		
		public abstract String server(EndpointType type);
		
		public boolean isProduction() {
			return this == PROD || this == DOT_GOV_PROD;
		}
		
		public boolean isDevLocalOrCustom() {
			return !isProduction();
		}
	}
	
	// Other Configurations
	public static final int CORE_POOL_SIZE = 200;
	public static final int MAX_POOL_SIZE = 500;
	public static final int QUEUE_CAPACITY = 10;
}
