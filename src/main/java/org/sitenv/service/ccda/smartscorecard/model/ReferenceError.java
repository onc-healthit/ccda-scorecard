package org.sitenv.service.ccda.smartscorecard.model;

public class ReferenceError {

	private String description;
	private String xPath;
	// This is a String vs an int the referenceccdavalidator so matching that
	private String documentLineNumber;
	// extracted from the validation message or by following the xPath if required
	private String sectionName;

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
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
