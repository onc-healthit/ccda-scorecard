package org.sitenv.service.ccda.smartscorecard.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.sitenv.service.ccda.smartscorecard.model.ResponseTO;
import org.sitenv.service.ccda.smartscorecard.processor.ScorecardProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


@RestController
public class ScorecardController {
	
	@Autowired
	ScorecardProcessor scorecardProcessor;
	
	@RequestMapping(value="/ccdascorecardservice2", method= RequestMethod.POST)
	public @ResponseBody ResponseTO ccdascorecardservice(@RequestParam("ccdaFile") MultipartFile ccdaFile){
		return scorecardProcessor.processCCDAFile(ccdaFile,false);
	}
	
	@RequestMapping(value = "/exportscorecarddatatoexcel", method = RequestMethod.GET)
	public ResponseEntity<InputStreamResource> genrateScorecardData(@RequestParam(value="fromDate", required=false) String fromDate,
			@RequestParam(value="toDate", required=false) String toDate)throws Exception {
		
		File file=null;
		HSSFWorkbook workBook = null;
		String fileName = "scorecardData.xls";
		try {
			
			workBook = scorecardProcessor.generateScorecardData(fromDate, toDate);
			file = new File(fileName);
			workBook.write(file);
			
		} catch (IOException ioe) {
			ioe.printStackTrace();
			throw ioe;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		return ResponseEntity.ok()
				.contentLength(file.length())
	            .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
	            .header("Content-Disposition","attachment; filename=" + fileName )
	            .body(new InputStreamResource(new FileInputStream(file)));
			
	}

}
