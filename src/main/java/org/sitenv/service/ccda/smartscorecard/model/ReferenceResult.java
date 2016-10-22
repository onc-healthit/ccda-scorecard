package org.sitenv.service.ccda.smartscorecard.model;

import java.util.List;

public class ReferenceResult {

	// e.g. 'C-CDA IG Conformance', '2015 Certification', future types...
	private String type;
	// e.g. Add all instance specific errors found + vocab
	private int totalErrorCount;
	// e.g. description, xPath, Line Number, etc.
	private List<ReferenceError> referenceErrors;

	public String getType() {
		return type;
	}

	public void setType(String type) {
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
