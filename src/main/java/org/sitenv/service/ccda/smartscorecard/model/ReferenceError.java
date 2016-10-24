package org.sitenv.service.ccda.smartscorecard.model;

import org.sitenv.service.ccda.smartscorecard.model.ReferenceTypes.ValidationResultType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * This is a limited version of
 * referenceccdavalidator/src/main/java/org/sitenv/referenceccda
 * /validators/RefCCDAValidationResult.java with the exception that it also has
 * a sectionName String
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReferenceError {

	private String description;
	// not required for scorecard results display but are useful for sorting
	private ValidationResultType type;
	private String xPath;
	// this is a String vs an int the referenceccdavalidator so matching that
	private String documentLineNumber;
	// extracted from the validation message or by following the xPath if
	// required
	private String sectionName;

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public ValidationResultType getType() {
		return type;
	}

	public void setType(ValidationResultType type) {
		this.type = type;
	}

	public String getxPath() {
		return xPath;
	}

	public void setxPath(String xPath) {
		this.xPath = xPath;
	}

	public String getDocumentLineNumber() {
		return documentLineNumber;
	}

	public void setDocumentLineNumber(String documentLineNumber) {
		this.documentLineNumber = documentLineNumber;
	}

	public String getSectionName() {
		return sectionName;
	}

	public void setSectionName(String sectionName) {
		this.sectionName = sectionName;
	}

}
