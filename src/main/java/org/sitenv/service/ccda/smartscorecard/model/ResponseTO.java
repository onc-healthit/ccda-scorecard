package org.sitenv.service.ccda.smartscorecard.model;

public class ResponseTO {
	
	private boolean isSuccess;
	private Results results;
	public boolean isSuccess() {
		return isSuccess;
	}
	public void setSuccess(boolean isSuccess) {
		this.isSuccess = isSuccess;
	}
	public Results getResults() {
		return results;
	}
	public void setResults(Results results) {
		this.results = results;
	}
	
}
