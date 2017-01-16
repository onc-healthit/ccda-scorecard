package org.sitenv.service.ccda.smartscorecard.model;


import org.sitenv.service.ccda.smartscorecard.model.ReferenceTypes.ValidationResultType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public class ReferenceError {

	private String description;
	private ValidationResultType type;
	private String xPath;
	private String validatorConfiguredXpath;
	private String documentLineNumber;
	private String actualCode;
	private String actualCodeSystem;
	private String actualCodeSystemName;
	private String actualDisplayName;
	private Boolean schemaError;
	private Boolean dataTypeSchemaError;
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
	public String getSectionName() {
		return sectionName;
	}
	public void setSectionName(String sectionName) {
		this.sectionName = sectionName;
	}
	
}
