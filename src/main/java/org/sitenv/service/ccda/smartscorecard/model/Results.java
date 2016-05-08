package org.sitenv.service.ccda.smartscorecard.model;

import java.util.List;

public class Results {
	
	private String finalGrade;
	private int finalNumericalGrade;
	private List<Category> categoryList;
	public String getFinalGrade() {
		return finalGrade;
	}
	public void setFinalGrade(String finalGrade) {
		this.finalGrade = finalGrade;
	}
	public List<Category> getCategoryList() {
		return categoryList;
	}
	public void setCategoryList(List<Category> categoryList) {
		this.categoryList = categoryList;
	}
	public int getFinalNumericalGrade() {
		return finalNumericalGrade;
	}
	public void setFinalNumericalGrade(int finalNumericalGrade) {
		this.finalNumericalGrade = finalNumericalGrade;
	}
}
