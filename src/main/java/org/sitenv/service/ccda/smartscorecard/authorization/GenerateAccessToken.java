package org.sitenv.service.ccda.smartscorecard.authorization;

import java.util.Objects;

import org.json.JSONObject;
import org.sitenv.service.ccda.smartscorecard.model.ScorecardProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class GenerateAccessToken {
	
	private static final Logger logger = LoggerFactory.getLogger(GenerateAccessToken.class);
	
	@Autowired
	@Qualifier("scorecardProperties")
	ScorecardProperties scorecardProperties;

	public String getAccessToken() {
		String accessToken = null;
		try {
			RestTemplate restTemplate = new RestTemplate();

			String tokenEndpoint = scorecardProperties.getTokenEndpoint();
			logger.info("tokenEndpoint: " + tokenEndpoint);
			String clientId = scorecardProperties.getClientId();
			logger.info("clientId: " + clientId);
			String clientSecret = scorecardProperties.getClientSecret();

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

			MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
			map.add("grant_type", "client_credentials");
			map.add("client_secret", clientSecret);
			map.add("client_id", clientId);
			HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
			ResponseEntity<KeyCloakResponse> response = restTemplate.postForEntity(tokenEndpoint, request,
					KeyCloakResponse.class);

			JSONObject jsonObj = new JSONObject(Objects.requireNonNull(response.getBody()));
			accessToken = jsonObj.getString("access_token");
			logger.info("getAccessToken accessToken  status :::::::" + response.getStatusCode());
		} catch (HttpClientErrorException clienterror) {
			logger.error("HttpClientErrorException  :::::::" + clienterror.getMessage());
			clienterror.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return accessToken;
	}
	
}