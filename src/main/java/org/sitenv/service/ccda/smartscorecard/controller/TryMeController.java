package org.sitenv.service.ccda.smartscorecard.controller;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TryMeController {
	
	private static final String MEDIA_TYPE = "text/xml";
	private static final boolean FORCE_OUTPUT_FILE = false;
	private static final String FORCED_OUTPUT_FILE_NAME_WITH_EXTENSION = "overridden-filename.xml";

	@RequestMapping(value = "/downloadtrymefileservice", method = RequestMethod.POST)
	public ResponseEntity<byte[]> downloadtrymefileservice(@RequestBody String filenameWithExtension) {
		String filePath = "/" + (FORCE_OUTPUT_FILE ? FORCED_OUTPUT_FILE_NAME_WITH_EXTENSION : filenameWithExtension);
		return FORCE_OUTPUT_FILE 
				? downloadLocalFile(filePath, MEDIA_TYPE, FORCED_OUTPUT_FILE_NAME_WITH_EXTENSION) 
				: downloadLocalFile(filePath, MEDIA_TYPE);		
	}
	
	public static ResponseEntity<byte[]> downloadLocalFile(
			final String filePath, final String mediaType) {
		return downloadLocalFile(filePath, mediaType, null);
	}

	public static ResponseEntity<byte[]> downloadLocalFile(
			final String filePath, final String mediaType,
			final String forcedOutputFilename) {

		InputStream xmlFileStream = null;
		byte[] fileBytes = null;
		ResponseEntity<byte[]> response = null;

		try {
			TryMeController.class.getResourceAsStream(filePath);
			xmlFileStream = TryMeController.class.getResourceAsStream(filePath);
			ApplicationUtil.debugLog("xmlFileStream", xmlFileStream.toString());

			fileBytes = IOUtils.toByteArray(xmlFileStream);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.parseMediaType(mediaType));
			if (forcedOutputFilename != null) {
				headers.setContentDispositionFormData(forcedOutputFilename,
						forcedOutputFilename);
			}
			headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

			response = new ResponseEntity<byte[]>(fileBytes, headers,
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (xmlFileStream != null) {
				try {
					if (xmlFileStream != null) {
						xmlFileStream.close();
					}
				} catch (IOException e2) {
					e2.printStackTrace();
				}
			}
		}

		return response;
	}

}
