package org.sitenv.service.ccda.smartscorecard.controller;

import org.sitenv.service.ccda.smartscorecard.model.ResponseTO;
import org.sitenv.service.ccda.smartscorecard.processor.ScorecardProcessor;
import org.springframework.beans.factory.annotation.Autowired;
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

}
