package org.sitenv.service.ccda.smartscorecard.model;

import java.util.Map;

public class CCDAScoreCardRubrics {
	
	private String requirement;
	private String issue;
	private String detail;
	private String comment;
	private String subCategory;
	private Map<Integer, String> points;
	private int maxPoints;
	private int actualPoints;
	
	public String getRequirement() {
		return requirement;
	}
	public void setRequirement(String requirement) {
		this.requirement = requirement;
	}
	public String getIssue() {
		return issue;
	}
	public void setIssue(String issue) {
		this.issue = issue;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public String getDetail() {
		return detail;
	}
	public void setDetail(String detail) {
		this.detail = detail;
	}
	public String getSubCategory() {
		return subCategory;
	}
	public void setSubCategory(String subCategory) {
		this.subCategory = subCategory;
	}
	public Map<Integer, String> getPoints() {
		return points;
	}
	public void setPoints(Map<Integer, String> points) {
		this.points = points;
	}
	public int getMaxPoints() {
		return maxPoints;
	}
	public void setMaxPoints(int maxPoints) {
		this.maxPoints = maxPoints;
	}
	public int getActualPoints() {
		return actualPoints;
	}
	public void setActualPoints(int actualPoints) {
		this.actualPoints = actualPoints;
	}
}
