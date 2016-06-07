# CCDA-Score-CARD
This application contains ccda score card service. The Service is implemented following the standards and promotes best practices in C-CDA implementation by assessing key aspects of the structured data found in individual documents. It is a tool designed to allow implementers to gain insight and information regarding industry best practice and usage overall. It also provides a rough quantitative assessment and highlights areas of improvement which can be made today to move the needle forward. The best practices and quantitative scoring criteria have been developed by HL7 through the HL7-ONC Cooperative agreement to improve the implementation of health care standards.

Below is the Java Snippet to access Score-CARD service in your own applications.

Score card API is POST restful services which takes CCDA document as input and gives JSON results. 
* Input parameter name: ccdaFile
* Input parameter Type: File.
* Output parameter Type: JSON string.

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
		response = restTemplate.postForObject("http://sitenv.org/ccda-smart-scorecard/ccdascorecardservice", 
												requestEntity, String.class);
		tempFile.delete();
	}catch(Exception exc)
	{
		exc.printStackTrace();
	}
}
```
