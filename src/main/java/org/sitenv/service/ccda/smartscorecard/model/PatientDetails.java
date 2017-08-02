package org.sitenv.service.ccda.smartscorecard.model;

public class PatientDetails {
	
	private String patientDob;
	private String patientDod;
	private boolean isDobValid;
	private boolean isDodValid;
	private boolean isDodPresent;
	
	public String getPatientDob() {
		return patientDob;
	}
	public void setPatientDob(String patientDob) {
		this.patientDob = patientDob;
	}
	public String getPatientDod() {
		return patientDod;
	}
	public void setPatientDod(String patientDod) {
		this.patientDod = patientDod;
	}
	public boolean isDobValid() {
		return isDobValid;
	}
	public void setDobValid(boolean isDobValid) {
		this.isDobValid = isDobValid;
	}
	public boolean isDodValid() {
		return isDodValid;
	}
	public void setDodValid(boolean isDodValid) {
		this.isDodValid = isDodValid;
	}
	public boolean isDodPresent() {
		return isDodPresent;
	}
	public void setDodPresent(boolean isDodPresent) {
		this.isDodPresent = isDodPresent;
	}
}
