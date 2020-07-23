# C-CDA Scorecard
### <a href="#overview">Overview</a>
### <a href="#api">API</a>
### <a href="#setupInstructions">Setup Instructions</a>
#### &nbsp; <a href="#dependencies">Dependencies</a>
#### &nbsp; <a href="#database">Database</a>
#### &nbsp; <a href="#tomcat">Tomcat</a>
#### &nbsp; <a href="#rules">Rules</a>
#### &nbsp; <a href="#useConfig">XML Configuration Path (Using pre-built WAR)</a>
#### &nbsp; <a href="#noConfig">Override Path (Building WAR from source)</a>

<span id="overview"></span>
# Overview
* This application contains the C-CDA Scorecard service. The Service is implemented following the standards and promotes best practices in C-CDA implementation by assessing key aspects of the structured data found in individual documents. It is a tool designed to allow implementers to gain insight and information regarding industry best practice and usage overall. It also provides a rough quantitative assessment and highlights areas of improvement which can be made today to move the needle forward. The best practices and quantitative scoring criteria have been developed by HL7 through the HL7-ONC Cooperative agreement to improve the implementation of health care standards.

<span id="api"></span>
# API
* The Scorecard API is a POST RESTful service which takes a C-CDA document as input and returns JSON results. 
  * Input parameter name: ccdaFile
  * Input parameter Type: File.
  * Output parameter Type: JSON string.

* Below is an example Java Snippet to access the Scorecard service in your own applications.
```Java
public Void ccdascorecardservice(MultipartFile ccdaFile)
{
    LinkedMultiValueMap<String, Object> requestMap = new LinkedMultiValueMap<String, Object>();
	String response = "";
	try{
		File tempFile = File.createTempFile("ccda", "File");
		FileOutputStream out = new FileOutputStream(tempFile);
		IOUtils.copy(ccdaFile.getInputStream(), out);
		requestMap.add("ccdaFile", new FileSystemResource(tempFile));		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = 
									new HttpEntity<LinkedMultiValueMap<String, Object>>(requestMap, headers);
		RestTemplate restTemplate = new RestTemplate();
		FormHttpMessageConverter formConverter = new FormHttpMessageConverter();
		formConverter.setCharset(Charset.forName("UTF8"));
		restTemplate.getMessageConverters().add(formConverter);
		restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
		response = restTemplate.postForObject("http://sitenv.org/scorecard/ccdascorecardservice2", 
												requestEntity, String.class);
		tempFile.delete();
	}catch(Exception exc)
	{
		exc.printStackTrace();
	}
}
```

<span id="setupInstructions"></span>
# Setup Instructions

<span id="dependencies"></span>
* Dependencies
  * This project requires a prerequisite ccda parser dependency. You can build the jar from here https://github.com/onc-healthit/ccda-parser

<span id="database"></span>
* Database
  * Install the latest version of Postgresql. Create a user called scorecarduser with password as scorecarduser and create a DB called site_scorecard
  * Inside the site_scorecard DB run the following scripts to create scorecard_statistics table
```SQL
CREATE SEQUENCE public.scorecard_statistics_id_seq
    INCREMENT 1
    START 83
    MINVALUE 1
    MAXVALUE 2147483647
    CACHE 1;
ALTER SEQUENCE public.scorecard_statistics_id_seq
    OWNER TO scorecarduser;
```
```SQL
CREATE TABLE public.scorecard_statistics

(
    id integer NOT NULL DEFAULT nextval('scorecard_statistics_id_seq'::regclass),
    doctype character varying(100) COLLATE pg_catalog."default",
    docscore smallint NOT NULL,
    patientscore smallint NOT NULL,
    allergiessectionscore smallint NOT NULL,
    encounterssectionscore smallint NOT NULL,
    immunizationssectionscore smallint NOT NULL,
    medicationssectionscore smallint NOT NULL,
    problemssectionscore smallint NOT NULL,
    proceduressectionscore smallint NOT NULL,
    socialhistorysectionscore smallint NOT NULL,
    vitalssectionscore smallint NOT NULL,
    resultssectionscore smallint NOT NULL,
    miscscore smallint NOT NULL,
    docname character varying(500) COLLATE pg_catalog."default",
    createtimestamp timestamp without time zone DEFAULT now(),
    oneclickscorecard boolean NOT NULL DEFAULT false,
    patientissues smallint,
    allergiessectionissues smallint,
    encounterssectionissues smallint,
    immunizationssectionissues smallint,
    medicationssectionissues smallint,
    problemssectionissues smallint,
    proceduressectionissues smallint,
    socialhistorysectionissues smallint,
    vitalssectionissues smallint,
    resultssectionissues smallint,
    miscissues smallint,
    ccdadocumenttype character varying(100) COLLATE pg_catalog."default",
    directemailaddress character varying(100) COLLATE pg_catalog."default",
    CONSTRAINT scorecard_statistics_pkey PRIMARY KEY (id)
)

WITH (
    OIDS = FALSE
)

TABLESPACE pg_default;
ALTER TABLE public.scorecard_statistics
    OWNER to scorecarduser;
```

<span id="tomcat"></span>
* Tomcat
  * Inside latest version of tomcat and add the following snippet under the <GlobalNamingResources> tag in server.xml
```XML
<Resource auth="Container" 
	  driverClassName="org.postgresql.Driver" 
	  maxActive="100" 
	  maxIdle="30" 
	  maxWait="10000" 
	  name="jdbc/site_scorecard" 
	  password="scorecarduser" 
	  type="javax.sql.DataSource" 
	  url="jdbc:postgresql://localhost/site_scorecard" 
	  username="scorecarduser"/>
```
  * Add the following snippet to context.xml
```XML
<ResourceLink global="jdbc/site_scorecard" 
    name="jdbc/site_scorecard"
    type="javax.sql.DataSource">
</ResourceLink>
```

<span id="rules"></span>
* Rules
  * The execution of rules in the Scorecard is controlled by an external configuration file, scorecardConfig.xml. The file controls what rules to execute. Please follow the steps below to configure scorecardConfig.xml
    * Download scorecardConfig.xml which is available under src/main/resources
    * By default scorecardConfig.xml is configured to run all of the Scorecard rules. Make the necessary changes to disable/enable any specific rules.
    * /var/opt/sitenv/scorecard/config/scorecardConfig.xml is the default path configured in /src/main/resources/config.properties file. Make sure to create a default path for scorecardConfig.xml. If you decide to create different path than specified, update config.properties appropriately. If you have the project cloned, an easy custom path to use would be within the source itself, such as Drive:/Users/Username/git/thisProjectName/src/main/resources/

**Note: <a href="#noConfig">If building the WAR yourself</a> vs using an appropriate local WAR from the releases page, you have the option to <a href="#noConfig">skip configuration via scorecard.xml</a>. Otherwise, if using a pre-built WAR, you will need to configure with scorecard.xml as described next**

<span id="useConfig"></span>
* **Continued instructions for using a pre-built release WAR:**
  * Download scorecard.xml from https://github.com/onc-healthit/ccda-scorecard/blob/master/src/main/resources/scorecard.xml.
  * Update parameter values accordingly. 
      * scorecard.igConformanceCall - Indicates whether conformance check need to run or not. 
      * scorecard.certificatinResultsCall - Indicates whether certification Result check need to happen or not. 
      * scorecard.igConformanceUrl - URL for igConformanceCall
      * scorecard.certificationResultsUrl - URL for certification result call.
      * scorecard.configFile - Path for scorecardConfig.xml file which controls the execution of scorecard rules. This can be downloaded from https://github.com/onc-healthit/ccda-scorecard/blob/master/src/main/resources/scorecardConfig.xml
  * Place a copy of scorecard.xml in $CATALINA_BASE/conf/[enginename]/[hostname]/. For example, ~/apache-tomcat-7.0.57/conf/Catalina/localhost
  * Copy the WAR file to the Apache Tomcat webapps folder
  * Start Tomcat
 
 * Below is an example of the scorecard.xml configuration which uses **local** referenceccdaservice URLs. We have used default port (8080) as reference. It can be changed to any port
 ```XML
 <Context reloadable="true">
    <Parameter name="scorecard.igConformanceCall" value="true" override="true"/>
    <Parameter name="scorecard.certificatinResultsCall" value="true" override="true"/>
    <Parameter name="scorecard.igConformanceUrl" value="http://localhost:8080/referenceccdaservice/" override="true"/>
    <Parameter name="scorecard.certificationResultsUrl" value="http://localhost:8080/referenceccdaservice/" override="true"/>
    <Parameter name="scorecard.configFile" value="//path to scorecardConfig.xml" override="true"/>
</Context>
```
* Below is an example of the scorecard.xml configuration which uses **production** referenceccdaservice URLs
```XML
<Context reloadable="true">
    <Parameter name="scorecard.igConformanceCall" value="true" override="true"/>
    <Parameter name="scorecard.certificatinResultsCall" value="true" override="true"/>
    <Parameter name="scorecard.igConformanceUrl" value="https://prodccda.sitenv.org/referenceccdaservice/" override="true"/>
    <Parameter name="scorecard.certificationResultsUrl" value="https://prodccda.sitenv.org/referenceccdaservice/" override="true"/>
    <Parameter name="scorecard.configFile" value="//path to scorecardConfig.xml" override="true"/>
</Context>
```
* When using the production validator API, you might encounter a security exception when scorecard tries to contact the API. To overcome this exception you need to add the
  validator's public certificate into your local java keystore.
* Navigate to JAVA_HOME/jre/lib/security and run the following script
```
keytool -importcert $CERT -alias $ALIAS -keystore cacerts -storepass changeit
```

* Deploy the WAR file to Tomcat and start Tomcat. You should be able to see the Scorecard UI by navigating to this URL: http://localhost:8080/scorecard/
  * Note: 8080 is just an example of what your Tomcat port might be. Please replace 8080 with your actual port if it differs
  * *IF you've reached this point in the instructions, you have chosen to configure with scorecard.xml and are done.*

<span id="noConfig"></span>
* **Continued instructions if building the WAR yourself:**
  * From this point one can either follow the prior <a href="#useConfig">instructions for using a pre-built release WAR and build the WAR instead of downloading it before deploying</a>, or, use the override options in src/main/java/org/sitenv/service/ccda/smartscorecard/cofiguration/ApplicationConfiguration.java, explained ahead:

* Navigate to ApplicationConfiguration.java and set OVERRIDE_SCORECARD_XML_CONFIG to true
```Java
/**
 * True allows setting default scorecard.xml values externally
 */
public static final boolean OVERRIDE_SCORECARD_XML_CONFIG = true;
```

* Decide which server you would like the service to contact for Reference C-CDA Validation. Whether it be a development server, production server, or a custom (such as local) specified server. One can also control the server of the Scorecard itself if desired. These outcomes are based on the Environment set
```
/**
 * Sets the environment for deployment - only relevant if OVERRIDE_SCORECARD_XML_CONFIG is true
 * Options are defined in org.sitenv.service.ccda.smartscorecard.cofiguration.ApplicationConfiguration.Environment
 */
public static final Environment ENV = Environment.ENTER_DESIRED_ENVIRONMENT_FROM_ENUM_HERE;
```

* To use the **production** servers, set set ENV to Environment.PROD, and skip to final "Build the Scorecard project and deploy the WAR file" step
```Java
public static final Environment ENV = Environment.PROD;;
```

* For example, to use the **development** servers, set ENV to Environment.DEV, and "Build the Scorecard project and deploy the WAR file" step
```Java
public static final Environment ENV = Environment.DEV;
```

* To use a **local** or custom server, set ENV to Environment.LOCAL_OR_CUSTOM, and continue through the remaining instructions
```Java
public static final Environment ENV = Environment.LOCAL_OR_CUSTOM;
```
  * If the Scorecard is hosted on a different port than 8000, update the port in DEFAULT_LOCAL_SCORECARD_SERVER_URL to whatever Tomcat is configured to for your local Scorecard instance (8080 is a common default but default for this is 8000 since the Reference C-CDA Validator may already be configured on 8080). To use a custom non-local server, replace the entire URL as desired, however, there probably isn't a good reason to this for this particular URL
  ```Java
  public static final String DEFAULT_LOCAL_SCORECARD_SERVER_URL = "http://localhost:XXXX",
  ```
  * If the Reference C-CDA Validator is hosted on a different port than 8080, update the port in DEFAULT_LOCAL_REF_VAL_SERVER_URL to whatever Tomcat is configured to for your local Reference C-CDA Validator instance. To use a custom non-local server, replace the entire URL as desired
  ```Java
  public static final String DEFAULT_LOCAL_REF_VAL_SERVER_URL = "http://localhost:XXXX",
  ```

* Build the Scorecard project and deploy the WAR file to Tomcat and start Tomcat. You should be able to see the Scorecard UI by navigating to this URL: http://localhost:8000/scorecard/
  * Note: 8000 is just an example of what your Tomcat port might be. Please replace 8000 with your actual port if it differs. For example, it might be 8080.
  * Note: If there is an issue with the maven build due to tests failing, plase post a bug on the Scorecard issues ticket and try building with the following to continue regardless:
    ```Bash
    mvn clean install -D skipTests
    ```
