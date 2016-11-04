package org.sitenv.service.ccda.smartscorecard.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.Charset;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.IOUtils;
import org.sitenv.ccdaparsing.util.PositionalXMLReader;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationConstants;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;

@RestController
public class ScorecardController {
	
	@RequestMapping(value="/ccdascorecardservice2", method= RequestMethod.POST)
	public @ResponseBody String ccdavalidatorservice(@RequestParam("ccdaFile") MultipartFile ccdaFile, @RequestParam("validationObjective")String validationObjective,
													 @RequestParam("referenceFileName")String referenceFileName, @RequestParam("debug_mode")String debug_mode){
		LinkedMultiValueMap<String, Object> requestMap = new LinkedMultiValueMap<String, Object>();
		XPath xPath = XPathFactory.newInstance().newXPath();
		String response = "";
		try{
			Document doc = PositionalXMLReader.readXML(ccdaFile.getInputStream());
			xPath.compile("./patient/birthplace/place/addr[not(@nullFlavor)]").
			evaluate(doc, XPathConstants.NODE);
			File tempFile = File.createTempFile("ccda", "File");
			FileOutputStream out = new FileOutputStream(tempFile);
			IOUtils.copy(ccdaFile.getInputStream(), out);
			requestMap.add("ccdaFile", new FileSystemResource(tempFile));
			requestMap.add("validationObjective", validationObjective);
			requestMap.add("referenceFileName", referenceFileName);
			requestMap.add("debug_mode", debug_mode);
			
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.MULTIPART_FORM_DATA);
	
			HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<LinkedMultiValueMap<String, Object>>(
																					requestMap, headers);
			RestTemplate restTemplate = new RestTemplate();
		    FormHttpMessageConverter formConverter = new FormHttpMessageConverter();
		    formConverter.setCharset(Charset.forName("UTF8"));
		    restTemplate.getMessageConverters().add(formConverter);
		    restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
		    response = restTemplate.postForObject(ApplicationConstants.REFERENCE_VALIDATOR_URL, requestEntity, String.class);
		    tempFile.delete();
		}catch(Exception exc)
		{
			exc.printStackTrace();
		}
		
	    return response;
	}

}
