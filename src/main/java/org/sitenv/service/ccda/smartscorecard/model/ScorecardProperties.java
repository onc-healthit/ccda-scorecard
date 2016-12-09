package org.sitenv.service.ccda.smartscorecard.model;

/**
 * @author mouni
 *
 */
public class ScorecardProperties {
	
	private Boolean igConformanceCall;
	private Boolean certificationResultsCall;
	private String igConformanceURL;
	private String certificatinResultsURL;
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
	
}
	
