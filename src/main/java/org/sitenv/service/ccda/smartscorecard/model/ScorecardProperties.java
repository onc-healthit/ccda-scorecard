package org.sitenv.service.ccda.smartscorecard.model;

public class ScorecardProperties {
	
	private Boolean igConformanceCall;
	private Boolean certificationResultsCall;
	private String igConformanceURL;
	private String certificatinResultsURL;
	
	private String tokenEndpoint;
	private String clientId;
	private String clientSecret;
	
	public Boolean getIgConformanceCall() {
		return igConformanceCall;
	}
	public void setIgConformanceCall(Boolean igConformanceCall) {
		this.igConformanceCall = igConformanceCall;
	}
	public Boolean getCertificationResultsCall() {
		return certificationResultsCall;
	}
	public void setCertificationResultsCall(Boolean certificationResultsCall) {
		this.certificationResultsCall = certificationResultsCall;
	}
	public String getIgConformanceURL() {
		return igConformanceURL;
	}
	public void setIgConformanceURL(String igConformanceURL) {
		this.igConformanceURL = igConformanceURL;
	}
	public String getCertificatinResultsURL() {
		return certificatinResultsURL;
	}
	public void setCertificatinResultsURL(String certificatinResultsURL) {
		this.certificatinResultsURL = certificatinResultsURL;
	}
	
	public String getTokenEndpoint() {
		return tokenEndpoint;
	}
	public void setTokenEndpoint(String tokenEndpoint) {
		this.tokenEndpoint = tokenEndpoint;
	}
	public String getClientId() {
		return clientId;
	}
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	public String getClientSecret() {
		return clientSecret;
	}
	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}	
	
}
