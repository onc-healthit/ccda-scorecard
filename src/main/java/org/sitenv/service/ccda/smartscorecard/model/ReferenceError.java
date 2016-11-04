package org.sitenv.service.ccda.smartscorecard.model;


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
	private String type;
	private String xPath;
	private String validatorConfiguredXpath;
	private String documentLineNumber;
	private String actualCode;
	private String actualCodeSystem;
	private String actualCodeSystemName;
	private String actualDisplayName;
	private Boolean schemaError;
	private Boolean dataTypeSchemaError;
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getxPath() {
		return xPath;
	}
	public void setxPath(String xPath) {
		this.xPath = xPath;
	}
	public String getValidatorConfiguredXpath() {
		return validatorConfiguredXpath;
	}
	public void setValidatorConfiguredXpath(String validatorConfiguredXpath) {
		this.validatorConfiguredXpath = validatorConfiguredXpath;
	}
	public String getDocumentLineNumber() {
		return documentLineNumber;
	}
	public void setDocumentLineNumber(String documentLineNumber) {
		this.documentLineNumber = documentLineNumber;
	}
	public String getActualCode() {
		return actualCode;
	}
	public void setActualCode(String actualCode) {
		this.actualCode = actualCode;
	}
	public String getActualCodeSystem() {
		return actualCodeSystem;
	}
	public void setActualCodeSystem(String actualCodeSystem) {
		this.actualCodeSystem = actualCodeSystem;
	}
	public String getActualCodeSystemName() {
		return actualCodeSystemName;
	}
	public void setActualCodeSystemName(String actualCodeSystemName) {
		this.actualCodeSystemName = actualCodeSystemName;
	}
	public String getActualDisplayName() {
		return actualDisplayName;
	}
	public void setActualDisplayName(String actualDisplayName) {
		this.actualDisplayName = actualDisplayName;
	}
	public Boolean getSchemaError() {
		return schemaError;
	}
	public void setSchemaError(Boolean schemaError) {
		this.schemaError = schemaError;
	}
	public Boolean getDataTypeSchemaError() {
		return dataTypeSchemaError;
	}
	public void setDataTypeSchemaError(Boolean dataTypeSchemaError) {
		this.dataTypeSchemaError = dataTypeSchemaError;
	}
}
