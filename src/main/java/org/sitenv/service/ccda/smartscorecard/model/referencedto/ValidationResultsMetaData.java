package org.sitenv.service.ccda.smartscorecard.model.referencedto;



import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ValidationResultsMetaData {
	private String ccdaDocumentType;
	private boolean serviceError;
	private String serviceErrorMessage;
	private String ccdaFileName;
	private String ccdaFileContents;
	private List<ResultMetaData> resultMetaData;

	public String getCcdaDocumentType() {
		return ccdaDocumentType;
	}

	public void setCcdaDocumentType(String ccdaDocumentType) {
		this.ccdaDocumentType = ccdaDocumentType;
	}

	public boolean isServiceError() {
		return serviceError;
	}

	public void setServiceError(boolean serviceError) {
		this.serviceError = serviceError;
	}

	public String getServiceErrorMessage() {
		return serviceErrorMessage;
	}

	public void setServiceErrorMessage(String serviceErrorMessage) {
		this.serviceErrorMessage = serviceErrorMessage;
	}

	public List<ResultMetaData> getResultMetaData() {
		return resultMetaData;
	}

	public void setResultMetaData(List<ResultMetaData> resultMetaData) {
		this.resultMetaData = resultMetaData;
	}

	public String getCcdaFileName() {
		return ccdaFileName;
	}

	public void setCcdaFileName(String ccdaFileName) {
		this.ccdaFileName = ccdaFileName;
	}

	public String getCcdaFileContents() {
		return ccdaFileContents;
	}

	public void setCcdaFileContents(String ccdaFileContents) {
		this.ccdaFileContents = ccdaFileContents;
	}
}
