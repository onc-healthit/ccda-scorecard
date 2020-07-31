package org.sitenv.service.ccda.smartscorecard.cofiguration;

public class ApplicationConfiguration {
	
	/**
	 * True allows setting default scorecard.xml values externally
	 * If using this configuration, set this to true, otherwise, ensure it is false
	 */
	public static final boolean OVERRIDE_SCORECARD_XML_CONFIG = false;
	
	/**
	 * Sets the environment for deployment - only relevant if OVERRIDE_SCORECARD_XML_CONFIG is true
	 * Options are defined in org.sitenv.service.ccda.smartscorecard.cofiguration.ApplicationConfiguration.Environment
	 */
	public static final Environment ENV = Environment.PROD_REF_VAL_WITH_LOCAL_OR_CUSTOM_SCORECARD;
	
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
	
	// Resolves final full endpoints based on flags
	// -Reference C-CDA Validator endpoints
	private static String server = ENV.server(EndpointType.RefVal);
	public static final String 
		CODE_DISPLAYNAME_VALIDATION_URL = server + CODE_AND_DISPLAYNAME_IN_CODESYSTEM_SERVICE,
		CODE_VALUSET_VALIDATION_URL = server + CODE_IN_VALUESET_SERVICE,
		CODE_CODESYSTEM_VALIDATION_URL = server + CODE_IN_CODESYSTEM_SERVICE,
		REFERENCE_VALIDATOR_URL = server + REFERENCE_CCDA_SERVICE;
	// -Scorecard endpoints
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
		PROD {
			@Override
			public String server(EndpointType type) {
				return type == EndpointType.RefVal ? TTP_PROD_SERVER_URL
						: CCDA_PROD_SERVER_URL;
			}
		}, 
		DOT_GOV_DEV {
			@Override
			public String server(EndpointType type) {
				throw new UnsupportedOperationException(
						"The DOT_GOV_DEV environment is not yet implemented. "
						+ "It will be implemented once the transfer is complete");
			}
		}, 
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
