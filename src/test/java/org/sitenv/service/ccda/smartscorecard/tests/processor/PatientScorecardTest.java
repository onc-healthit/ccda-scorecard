package org.sitenv.service.ccda.smartscorecard.tests.processor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.sitenv.service.ccda.smartscorecard.configuration.ApplicationConfiguration;
import org.sitenv.service.ccda.smartscorecard.model.ResponseTO;
import org.sitenv.service.ccda.smartscorecard.processor.ScorecardProcessor;
import org.sitenv.service.ccda.smartscorecard.tests.TestUtil;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationConstants;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

/**
 * These tests require a live server connection and thus are ignored for the sake of maven builds
 */
@Ignore
public class PatientScorecardTest {

	@Autowired
	ScorecardProcessor scorecardProcessor;

	private static final String CCDA_2_NAMES_NO_LEGAL_USE_ON_EITHER_NAME = "Patient_2Names_NoLegalUseOnEitherName.xml";
	private static final String CCDA_2_NAMES_HAS_LEGAL_USE_ON_1ST_NAME = "Patient_2Names_HasLegalUseOn1stName.xml";
	private static final String CCDA_1_NAME_HAS_LEGAL_USE = "Patient_1Name_HasLegalUse.xml";
	private static final String CCDA_1_NAME_NO_LEGAL_USE = "Patient_1Name_NoLegalUse.xml";

	/*
	 The following Regression Tests were created to cover SITE-3092, 
	 "Scorecard backend rubric processing bug when processing Patient header data with multiple names or no legal name"
	 Before the fix, due to NPEs
	 An Exception was thrown if:
	  -There were 2 names and at least one was not of legal use type
	 An Exception was not thrown if:
	  -There were 2 names and one was a legal use type
	  -There was one name only with a legal use type
	  -There was one name only without a legal use type	  
	  Note: The NPE is swallowed by processCCDAFile so we can't check for it in tests. 
	  But, we can check the side-effects of that consumption (success == false && errorMessage == generic)
	*/

	@Test
	public void testTwoPatientNameElementsWhereNeitherIsLegalUseType() {
		// expect pass due to NPE check now in place
		ResponseTO response = callScorecardService(CCDA_2_NAMES_NO_LEGAL_USE_ON_EITHER_NAME);
		run3092PatientNameAssertions(response);
	}

	@Test
	public void testTwoPatientNameElementsWhereFirstIsLegalUseType() {
		// expect pass all along
		ResponseTO response = callScorecardService(CCDA_2_NAMES_HAS_LEGAL_USE_ON_1ST_NAME);
		run3092PatientNameAssertions(response);
	}

	@Test
	public void testOnePatientNameElementAndIsLegalUseType() {
		// expect pass all along
		ResponseTO response = callScorecardService(CCDA_1_NAME_HAS_LEGAL_USE);
		run3092PatientNameAssertions(response);
	}

	@Test
	public void testOnePatientNameElementAndIsNotLegalUseType() {
		// expect pass all along
		ResponseTO response = callScorecardService(CCDA_1_NAME_NO_LEGAL_USE);
		run3092PatientNameAssertions(response);
	}

	private static void run3092PatientNameAssertions(ResponseTO response) {
		Assert.assertNotNull(response);
		Assert.assertTrue(response.isSuccess());
		Assert.assertNull(response.getErrorMessage());
		Assert.assertNotEquals(response.getErrorMessage(), ApplicationConstants.ErrorMessages.GENERIC_WITH_CONTACT);
	}

	private ResponseTO callScorecardService(String ccdaFilename) throws NullPointerException {
		ResponseTO response = null;
		File localFile = new File(getClass().getResource("/" + ccdaFilename).getPath());
		ApplicationUtil.debugLog("localFile Path", localFile.getPath());
		FileInputStream localFileStream = null;

		try {
			localFileStream = new FileInputStream(localFile);
			ApplicationUtil.debugLog("localFileStream", localFileStream.toString());
			MockMultipartFile ccdaFile = new MockMultipartFile(localFile.getName(), localFileStream);
			ApplicationUtil.debugLog("ccdaFile.getName()", (ccdaFile.getName() != null) ? ccdaFile.getName() : "null");

			response = getScorecardResponse(ApplicationConfiguration.CCDASCORECARDSERVICE_URL, ccdaFile);
		} catch (Exception e) {
			TestUtil.convertStackTraceToStringAndAssertFailWithIt(e);
		} finally {
			try {
				if (localFileStream != null) {
					localFileStream.close();
				}
			} catch (IOException e) {
				TestUtil.convertStackTraceToStringAndAssertFailWithIt(e);
			}
		}
		return response;
	}

	private static ResponseTO getScorecardResponse(String endpoint, MultipartFile ccdaFile) {
		ResponseTO response = new ResponseTO();

		LinkedMultiValueMap<String, Object> requestMap = new LinkedMultiValueMap<>();
		FileOutputStream tempFileOutputStream = null;
		File tempFile = null;

		try {
			final String tempCcdaFileName = "ccdaFile";
			tempFile = File.createTempFile(tempCcdaFileName, "xml");
			tempFileOutputStream = new FileOutputStream(tempFile);
			IOUtils.copy(ccdaFile.getInputStream(), tempFileOutputStream);
			requestMap.add(tempCcdaFileName, new FileSystemResource(tempFile));

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.MULTIPART_FORM_DATA);
			HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(requestMap, headers);

			FormHttpMessageConverter formConverter = new FormHttpMessageConverter();
			formConverter.setCharset(Charset.forName("UTF8"));

			List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();
			messageConverters.add(formConverter);
			messageConverters.add(new MappingJackson2HttpMessageConverter());

			RestTemplate restTemplate = new RestTemplate();
			restTemplate.setMessageConverters(messageConverters);

			response = restTemplate.postForObject(endpoint, requestEntity, ResponseTO.class);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (tempFileOutputStream != null) {
				try {
					tempFileOutputStream.flush();
					tempFileOutputStream.close();
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
			if (tempFile != null && tempFile.isFile()) {
				tempFile.delete();
			}
		}

		return response;
	}

}
