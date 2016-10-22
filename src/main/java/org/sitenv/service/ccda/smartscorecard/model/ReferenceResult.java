package org.sitenv.service.ccda.smartscorecard.model;

import java.util.List;

import org.sitenv.service.ccda.smartscorecard.model.ReferenceTypes.ReferenceInstanceType;

public class ReferenceResult {
	
	private ReferenceInstanceType type;
	// e.g. Add all instance specific errors found + vocab
	private int totalErrorCount;
	// e.g. description, xPath, Line Number, etc.
	private List<ReferenceError> referenceErrors;

	public ReferenceInstanceType getType() {
		return type;
	}

	public void setType(ReferenceInstanceType type) {
		this.type = type;
	}

	public int getTotalErrorCount() {
		return totalErrorCount;
	}

	public void setTotalErrorCount(int totalErrorCount) {
		this.totalErrorCount = totalErrorCount;
	}

	public List<ReferenceError> getReferenceErrors() {
		return referenceErrors;
	}

	public void setReferenceErrors(List<ReferenceError> referenceErrors) {
		this.referenceErrors = referenceErrors;
	}

}
