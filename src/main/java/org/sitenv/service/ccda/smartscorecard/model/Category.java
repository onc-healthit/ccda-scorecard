package org.sitenv.service.ccda.smartscorecard.model;

import java.util.List;

public class Category {
	
	private String categoryName;
	private String categoryGrade;
	private int categoryNumericalScore;
	private List<CCDAScoreCardRubrics> categoryRubrics;
	private int numberOfIssues;
	
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
}
