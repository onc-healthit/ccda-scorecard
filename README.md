# C-CDA Scorecard
This application contains the C-CDA Scorecard service. The Service is implemented following the standards and promotes best practices in C-CDA implementation by assessing key aspects of the structured data found in individual documents. It is a tool designed to allow implementers to gain insight and information regarding industry best practice and usage overall. It also provides a rough quantitative assessment and highlights areas of improvement which can be made today to move the needle forward. The best practices and quantitative scoring criteria have been developed by HL7 through the HL7-ONC Cooperative agreement to improve the implementation of health care standards.

The Scorecard API is a POST RESTful service which takes a C-CDA document as input and returns JSON results. 
* Input parameter name: ccdaFile
* Input parameter Type: File.
* Output parameter Type: JSON string.

Below is the Java Snippet to access the Scorecard service in your own applications.

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

# Setup Instructions
* This project requires a prerequisite ccda parser dependency. You can build the jar from here https://github.com/onc-healthit/ccda-parser 
* Install the latest version of Postgresql. Create a user called scorecarduser with password as scorecarduser and create a DB called site_scorecard
* Inside the site_scorecard DB run the following scripts to create scorecard_statistics table
```
CREATE SEQUENCE public.scorecard_statistics_id_seq
    INCREMENT 1
    START 83
    MINVALUE 1
    MAXVALUE 2147483647
    CACHE 1;
ALTER SEQUENCE public.scorecard_statistics_id_seq
    OWNER TO scorecarduser;
```
```
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
* Inside latest version of tomcat and add the following snippet under the <GlobalNamingResources> tag in server.xml
```
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
```
<ResourceLink global="jdbc/site_scorecard" 
    name="jdbc/site_scorecard"
    type="javax.sql.DataSource">
</ResourceLink>
```

* Rules execution in scorecard is controlled using an external config file. scorecardConfig.xml controls what rules to execute. Please follow the steps below to configure scorecardConfig.xml.
  * Download scorecardConfig.xml which is available under src/main/resources
  * By default scorecardConfig.xml is configured to run all the scorecard rules. Make the necessary changes to disable/enable any specific rules.
  * /var/opt/sitenv/scorecard/config/scorecardConfig.xml is the default path configured in /src/main/resources/config.properties file. Make sure to create default path for scorecardConfig.xml. If you decide to create different path then update config.properties appropriately
 
* Build the scorecard project and deploy the war file to tomcat and start tomcat. You should be able to see scorecard UI by navigating to this URL - http://localhost:8080/scorecard/
* Note: 8080 is just an example of what your Tomcat port might be. Please replace 8080 with your actual port if it differs.
