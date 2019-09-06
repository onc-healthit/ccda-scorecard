package org.sitenv.service.ccda.smartscorecard.model;

import java.util.ArrayList;
import java.util.List;

public class Category implements Comparable<Category> {	
	
	public Category(boolean isFailingConformance, String categoryName)
	{
		this.isFailingConformance = isFailingConformance;
		this.categoryName = categoryName;
		this.categoryRubrics = new ArrayList<CCDAScoreCardRubrics>();
	}
	
	public Category(String categoryName, boolean isNullFlavorNI)
	{
		this.isNullFlavorNI = isNullFlavorNI;
		this.categoryName = categoryName;
		this.categoryRubrics = new ArrayList<CCDAScoreCardRubrics>();
	}
	
	public Category()
	{
		
	}
	
	// scorecard properties
	private String categoryName;
	private String categoryGrade;
	private int categoryNumericalScore;
	private List<CCDAScoreCardRubrics> categoryRubrics;
	private int numberOfIssues;	
	private int numberOfChecks;
	private int numberOfFailedRubrics;
	
	// referenceccdavalidator properties
	private boolean isFailingConformance;
	private boolean certificationFeedback;	
	private boolean isNullFlavorNI;
	
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
	public boolean isCertificationFeedback() {
		return certificationFeedback;
	}
	public void setCertificationFeedback(boolean certificationFeedback) {
		this.certificationFeedback = certificationFeedback;
	}
	public boolean isNullFlavorNI() {
		return isNullFlavorNI;
	}
	public void setNullFlavorNI(boolean isNullFlavorNI) {
		this.isNullFlavorNI = isNullFlavorNI;
	}
	
	public int getNumberOfFailedRubrics() {
		return numberOfFailedRubrics;
	}

	public void setNumberOfFailedRubrics(int numberOfFailedRubrics) {
		this.numberOfFailedRubrics = numberOfFailedRubrics;
	}
	
	public int getNumberOfChecks() {
		return numberOfChecks;
	}

	public void setNumberOfChecks(int numberOfChecks) {
		this.numberOfChecks = numberOfChecks;
	}

	@Override
	public int compareTo(Category other) {
		if(this.categoryNumericalScore < other.categoryNumericalScore) {
			return -1;
		} else if(this.categoryNumericalScore > other.categoryNumericalScore) {
			return 1;
		}
		return 0;
	}
	
}
