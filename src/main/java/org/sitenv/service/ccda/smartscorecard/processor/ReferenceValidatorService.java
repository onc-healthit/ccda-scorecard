package org.sitenv.service.ccda.smartscorecard.processor;

import org.apache.log4j.Logger;
import org.sitenv.service.ccda.smartscorecard.configuration.ApplicationConfiguration;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationConstants;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class ReferenceValidatorService {
	
	private static final Logger logger = Logger.getLogger(ReferenceValidatorService.class);
	
	@Autowired
	@Qualifier("refValRestTemplate")
	private RestTemplate restTemplate;
	
	public boolean validateDisplayName(String code, String codeSystem, String displayName )
	{
		boolean result = false;
		try {
			if(!ApplicationUtil.isEmpty(code) && !ApplicationUtil.isEmpty(codeSystem) && !ApplicationUtil.isEmpty(displayName))
			{
				UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(ApplicationConfiguration.CODE_DISPLAYNAME_VALIDATION_URL)
						.queryParam("code", code)
						.queryParam("codeSystems", ApplicationConstants.CODE_SYSTEM_MAP.get(codeSystem)==null ? "\"\"" : ApplicationConstants.CODE_SYSTEM_MAP.get(codeSystem))
						.queryParam("displayName", displayName.toUpperCase());
				
				result = restTemplate.getForObject(builder.build().encode().toUri(), Boolean.class);
			}
		}catch(Exception exc) {
			logger.info("Exception occured while calling ref val validate display name end point:" + exc.getLocalizedMessage());
		}
		return result;
	}
	
	public boolean validateCodeForValueset(String code, String valuesetId)
	{
		boolean result = false;
		try {
			if(!ApplicationUtil.isEmpty(code) && !ApplicationUtil.isEmpty(valuesetId))
			{
				UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(ApplicationConfiguration.CODE_VALUSET_VALIDATION_URL)
						.queryParam("code", code)
						.queryParam("valuesetOids", valuesetId);
			
				result = restTemplate.getForObject(builder.build().encode().toUri(), Boolean.class);
			}
		}catch (Exception exc) {
			logger.info("Exception occured while calling ref val validate code for value set name end point:" + exc.getLocalizedMessage());
		}
		
		return result;
		
	}
	
	public boolean validateCodeForCodeSystem(String code, String codeSystem)
	{
		boolean result = false;
		try {
			if(!ApplicationUtil.isEmpty(code) && !ApplicationUtil.isEmpty(codeSystem))
			{
				UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(ApplicationConfiguration.CODE_CODESYSTEM_VALIDATION_URL)
				        .queryParam("code",code)
				        .queryParam("codeSystems", ApplicationConstants.CODE_SYSTEM_MAP.get(codeSystem)==null ? "\"\"" : ApplicationConstants.CODE_SYSTEM_MAP.get(codeSystem));
				
			    result = restTemplate.getForObject(builder.build().encode().toUri(), Boolean.class);
			}
		}catch (Exception exc) {
			logger.info("Exception occured while calling ref val validate code for code system end point:" + exc.getLocalizedMessage());
		}
		return result;
		
	}

}
