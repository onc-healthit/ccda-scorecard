package org.sitenv.service.ccda.smartscorecard.tests;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.sitenv.service.ccda.smartscorecard.cofiguration.ApplicationConfiguration;
import org.sitenv.service.ccda.smartscorecard.controller.SaveReportController;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationUtil;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

public class SaveReportControllerTest {

	private static final String CCDA_TEST_FILE = "/Scorecard-PassesCert.xml";

	@Test
	public void testSavescorecardservicebackend() {

		File localFile = new File(getClass().getResource(CCDA_TEST_FILE)
				.getPath());
		ApplicationUtil.debugLog("localFile Path", localFile.getPath());
		FileInputStream localFileStream = null;
		byte[] pdfBytes = null;

		try {
			localFileStream = new FileInputStream(localFile);
			ApplicationUtil.debugLog("localFileStream",
					localFileStream.toString());

			MockMultipartFile ccdaFile = new MockMultipartFile(
					localFile.getName(), localFileStream);
			ApplicationUtil.debugLog("ccdaFile.getName()",
					(ccdaFile.getName() != null) ? ccdaFile.getName() : "null");

			pdfBytes = getSavescorecardservicebackendPdfResultsStream(ccdaFile);
			ApplicationUtil.debugLog("pdfBytes",
					pdfBytes != null ? pdfBytes.toString() : "null");
			ApplicationUtil.debugLog("ccdaFile.getName() 2nd time",
					(ccdaFile.getName() != null) ? ccdaFile.getName() : "null");

			Assert.assertNotNull(
					"The test failed when calling savescorecardservicebackend. The response stream is null.",
					pdfBytes);

			File savedPdfFile = new File(localFile.getName() + ".pdf");
			try (FileOutputStream fop = new FileOutputStream(savedPdfFile)) {
				if (!savedPdfFile.exists()) {
					savedPdfFile.createNewFile();
				}
				fop.write(pdfBytes);
			} catch (IOException e) {
				TestUtil.convertStackTraceToStringAndAssertFailWithIt(e);
			}
			Assert.assertNotNull(
					"The scorecard pdf report was not able to be saved.",
					savedPdfFile);
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
			ApplicationUtil
					.debugLog("testSavescorecardservicebackend completed routine");
		}

	}

	private static byte[] getSavescorecardservicebackendPdfResultsStream(
			MultipartFile ccdaFile) {

		LinkedMultiValueMap<String, Object> requestMap = new LinkedMultiValueMap<>();
		byte[] pdfBytes = null;
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
			HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(
					requestMap, headers);

			FormHttpMessageConverter formConverter = new FormHttpMessageConverter();
			formConverter.setCharset(Charset
					.forName(SaveReportController.SAVE_REPORT_CHARSET_NAME));
			ByteArrayHttpMessageConverter pdfConverter = new ByteArrayHttpMessageConverter();
			List<MediaType> mediaTypes = new ArrayList<MediaType>();
			mediaTypes.add(new MediaType("application", "pdf"));
			pdfConverter.setSupportedMediaTypes(mediaTypes);

			List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();
			messageConverters.add(pdfConverter);
			messageConverters.add(formConverter);
			messageConverters.add(new MappingJackson2HttpMessageConverter());

			RestTemplate restTemplate = new RestTemplate();
			restTemplate.setMessageConverters(messageConverters);

			pdfBytes = restTemplate.postForObject(
					ApplicationConfiguration.SAVESCORECARDSERVICEBACKEND_URL,
					requestEntity, byte[].class);
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

		return pdfBytes;

	}

}
