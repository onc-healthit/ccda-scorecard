package org.sitenv.service.ccda.smartscorecard.model.referencedto;

import java.util.List;

import org.sitenv.service.ccda.smartscorecard.model.ReferenceError;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ValidationResultsDto {
	private ValidationResultsMetaData resultsMetaData;
	private List<ReferenceError> ccdaValidationResults;

	public List<ReferenceError> getCcdaValidationResults() {
		return ccdaValidationResults;
	}

	public void setCcdaValidationResults(
			List<ReferenceError> ccdaValidationResults) {
		this.ccdaValidationResults = ccdaValidationResults;
	}

	public ValidationResultsMetaData getResultsMetaData() {
		return resultsMetaData;
	}

	public void setResultsMetaData(ValidationResultsMetaData resultsMetaData) {
		this.resultsMetaData = resultsMetaData;
	}

}
