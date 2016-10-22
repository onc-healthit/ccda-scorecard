package org.sitenv.service.ccda.smartscorecard.model;

import java.util.List;

public class Category {
	
	// scorecard properties
	private String categoryName;
	private String categoryGrade;
	private int categoryNumericalScore;
	private List<CCDAScoreCardRubrics> categoryRubrics;
	private int numberOfIssues;	
	
	// referenceccdavalidator properties
	private boolean isFailingConformance;
	// e.g. description, xPath, Line Number, etc.
	// TODO: this is convenient here but we don't NEED it but it makes for less front-end processing
	// Options:
	// 1. We populate this (referenceErrors)
	// 2. We override categoryRubrics with the related reference results
	// 3. We do not populate this, and delete it from the class. Instead, we have the front end 
	// loop the results in the current ReferenceResult instance and match the section name to populate 
	private List<ReferenceError> referenceErrors;
	
	public String getCategoryName() {
		return categoryName;
	}
	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}
	public String getCategoryGrade() {
		return categoryGrade;
	}
	public void setCategoryGrade(String categoryGrade) {
		this.categoryGrade = categoryGrade;
	}
	public List<CCDAScoreCardRubrics> getCategoryRubrics() {
		return categoryRubrics;
	}
	public void setCategoryRubrics(List<CCDAScoreCardRubrics> categoryRubrics) {
		this.categoryRubrics = categoryRubrics;
	}
	public int getNumberOfIssues() {
		return numberOfIssues;
	}
	public void setNumberOfIssues(int numberOfIssues) {
		this.numberOfIssues = numberOfIssues;
	}
	public int getCategoryNumericalScore() {
		return categoryNumericalScore;
	}
	public void setCategoryNumericalScore(int categoryNumericalScore) {
		this.categoryNumericalScore = categoryNumericalScore;
	}
	public boolean isFailingConformance() {
		return isFailingConformance;
	}
	public void setFailingConformance(boolean isFailingConformance) {
		this.isFailingConformance = isFailingConformance;
	}
	public List<ReferenceError> getReferenceErrors() {
		return referenceErrors;
	}
	public void setReferenceErrors(List<ReferenceError> referenceErrors) {
		this.referenceErrors = referenceErrors;
	}	
	
}
