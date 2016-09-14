package org.sitenv.service.ccda.smartscorecard.controller;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TryMeController {

	private static final String OUTPUT_FILE_NAME = "170.315_b1_toc_amb_ccd_r21_sample1_v5.xml";
	private static final String FILE_PATH = "/" + OUTPUT_FILE_NAME;
	private static final String MEDIA_TYPE = "text/xml";
	private static final boolean FORCE_OUTPUT_FILE_NAME = false;

	@RequestMapping(value = "/downloadtrymefileservice", method = RequestMethod.GET)
	public ResponseEntity<byte[]> downloadtrymefileservice() {
		return downloadLocalFile(FILE_PATH, MEDIA_TYPE,
				FORCE_OUTPUT_FILE_NAME ? OUTPUT_FILE_NAME : null);
	}

	public static ResponseEntity<byte[]> downloadLocalFile(
			final String filePath, final String mediaType,
			final String outputFilename) {

		InputStream xmlFileStream = null;
		byte[] fileBytes = null;
		ResponseEntity<byte[]> response = null;

		try {
			TryMeController.class.getResourceAsStream(FILE_PATH);
			xmlFileStream = TryMeController.class.getResourceAsStream(filePath);
			ApplicationUtil.debugLog("xmlFileStream", xmlFileStream.toString());

			fileBytes = IOUtils.toByteArray(xmlFileStream);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.parseMediaType(mediaType));
			if (outputFilename != null) {
				headers.setContentDispositionFormData(outputFilename,
						outputFilename);
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
