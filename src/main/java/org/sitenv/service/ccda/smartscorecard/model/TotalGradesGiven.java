package org.sitenv.service.ccda.smartscorecard.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TotalGradesGiven {
	
	private int aPlusGrades;
	private int aMinusGrades;
	private int bPlusGrades;
	private int bMinusGrades;
	private int cGrades;
	private int dGrades;
	
	@JsonProperty("aPlusGrades")
	public int getAPlusGrades() {
		return aPlusGrades;
	}
	
	public void setAPlusGrades(int aPlusGrades) {
		this.aPlusGrades = aPlusGrades;
	}
	
	@JsonProperty("aMinusGrades")
	public int getAMinusGrades() {
		return aMinusGrades;
	}
	
	public void setAMinusGrades(int aMinusGrades) {
		this.aMinusGrades = aMinusGrades;
	}
	
	@JsonProperty("bPlusGrades")
	public int getBPlusGrades() {
		return bPlusGrades;
	}
	
	public void setBPlusGrades(int bPlusGrades) {
		this.bPlusGrades = bPlusGrades;
	}
	
	@JsonProperty("bMinusGrades")
	public int getBMinusGrades() {
		return bMinusGrades;
	}
	
	public void setBMinusGrades(int bMinusGrades) {
		this.bMinusGrades = bMinusGrades;
	}
	
	@JsonProperty("cGrades")
	public int getCGrades() {
		return cGrades;
	}
	
	public void setCGrades(int cGrades) {
		this.cGrades = cGrades;
	}
	
	@JsonProperty("dGrades")
	public int getDGrades() {
		return dGrades;
	}
	
	public void setDGrades(int dGrades) {
		this.dGrades = dGrades;
	}
	
}
