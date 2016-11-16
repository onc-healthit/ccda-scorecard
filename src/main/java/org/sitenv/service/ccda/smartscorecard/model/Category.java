package org.sitenv.service.ccda.smartscorecard.model;

import java.util.ArrayList;
import java.util.List;

public class Category {	
	
	public Category(boolean isFailingConformance, String categoryName)
	{
		this.isFailingConformance = isFailingConformance;
		this.categoryName = categoryName;
		this.categoryRubrics = new ArrayList<CCDAScoreCardRubrics>();
		if(isFailingConformance) {
			//does not affect overall score since failed conformance
			this.categoryGrade = "F";
			this.categoryNumericalScore = -1;
		}
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
	
	// referenceccdavalidator properties
	private boolean isFailingConformance;
	
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
	
}
