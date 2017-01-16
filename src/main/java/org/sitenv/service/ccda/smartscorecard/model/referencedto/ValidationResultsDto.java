package org.sitenv.service.ccda.smartscorecard.model.referencedto;

import org.sitenv.service.ccda.smartscorecard.model.ReferenceError;

import java.util.ArrayList;
import java.util.List;

public class ValidationResultsDto {
	private ValidationResultsMetaData resultsMetaData;
	private List<ReferenceError> ccdaValidationResults;

	public ValidationResultsDto() {}
	
	public ValidationResultsDto(ValidationResultsDto resultsToCopy) {
		this.resultsMetaData = new ValidationResultsMetaData(
				resultsToCopy.getResultsMetaData());
		this.ccdaValidationResults = new ArrayList<ReferenceError>(
				resultsToCopy.getCcdaValidationResults());
	}	
	
	public List<ReferenceError> getCcdaValidationResults() {
		return ccdaValidationResults;
	}

	public void setCcdaValidationResults(List<ReferenceError> ccdaValidationResults) {
		this.ccdaValidationResults = ccdaValidationResults;
	}

	public ValidationResultsMetaData getResultsMetaData() {
		return resultsMetaData;
	}

	public void setResultsMetaData(ValidationResultsMetaData resultsMetaData) {
		this.resultsMetaData = resultsMetaData;
	}

}
