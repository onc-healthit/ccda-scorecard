package org.sitenv.service.ccda.smartscorecard.model;

import java.util.ArrayList;
import java.util.List;

public class ResponseTO {
	
	private boolean isSuccess;
	private String errorMessage;
	private String filename;
	// scorecard results
	private Results results;
	// multiple instances of referenceccdavalidator results 
	// (first requirement is 2 instances)
	private List<ReferenceResult> referenceResults;
	
	private List<ReferenceError> schemaErrorList;
	
	private boolean schemaErrors;
	
	// scorecard accessor methods
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
	public String getErrorMessage() {
		return errorMessage;
	}
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	
	// referenceccdavalidator accessor methods
	public List<ReferenceResult> getReferenceResults() {
		if(this.referenceResults == null)
		{
			this.referenceResults = new ArrayList<ReferenceResult>();
		}
		return referenceResults;
	}
	public void setReferenceResults(List<ReferenceResult> referenceResults) {
		this.referenceResults = referenceResults;
	}
	public List<ReferenceError> getSchemaErrorList() {
		return schemaErrorList;
	}
	public void setSchemaErrorList(List<ReferenceError> schemaErrorList) {
		this.schemaErrorList = schemaErrorList;
	}
	public boolean isSchemaErrors() {
		return schemaErrors;
	}
	public void setSchemaErrors(boolean schemaErrors) {
		this.schemaErrors = schemaErrors;
	}
}
