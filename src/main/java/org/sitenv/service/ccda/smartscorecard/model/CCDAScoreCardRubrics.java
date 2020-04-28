package org.sitenv.service.ccda.smartscorecard.model;

import java.util.ArrayList;
import java.util.List;

import org.sitenv.ccdaparsing.model.CCDAXmlSnippet;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class CCDAScoreCardRubrics {
	
	private String rule;
	private int numberOfIssues;
	private List<CCDAXmlSnippet> issuesList;
	private List<String> exampleTaskForceLinks;
	private List<String> igReferences;
	private String description;
	@JsonIgnore
	private int actualPoints;
	@JsonIgnore
	private int maxPoints;
	@JsonIgnore
	private float rubricScore;
	@JsonIgnore
	private int numberOfChecks;
	
	public String getRule() {
		return rule;
	}
	public void setRule(String rule) {
		this.rule = rule;
	}
	public int getNumberOfIssues() {
		return numberOfIssues;
	}
	public void setNumberOfIssues(int numberOfIssues) {
		this.numberOfIssues = numberOfIssues;
	}
	public List<CCDAXmlSnippet> getIssuesList() {
		return issuesList;
	}
	public void setIssuesList(List<CCDAXmlSnippet> issuesList) {
		this.issuesList = issuesList;
	}
	public List<String> getExampleTaskForceLinks() {
		if(exampleTaskForceLinks == null)
		{
			return exampleTaskForceLinks = new ArrayList<String>();
		}else
			return exampleTaskForceLinks;
	}
	public void setExampleTaskForceLinks(List<String> exampleTaskForceLinks) {
		this.exampleTaskForceLinks = exampleTaskForceLinks;
	}
	public List<String> getIgReferences() {
		if(igReferences == null)
		{
			return igReferences = new ArrayList<String>();
		}else
			return igReferences;
	}
	public void setIgReferences(List<String> igReferences) {
		this.igReferences = igReferences;
	}
	public int getActualPoints() {
		return actualPoints;
	}
	public void setActualPoints(int actualPoints) {
		this.actualPoints = actualPoints;
	}
	public int getMaxPoints() {
		return maxPoints;
	}
	public void setMaxPoints(int maxPoints) {
		this.maxPoints = maxPoints;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public float getRubricScore() {
		return rubricScore;
	}
	public void setRubricScore(float rubricScore) {
		this.rubricScore = rubricScore;
	}
	public int getNumberOfChecks() {
		return numberOfChecks;
	}
	public void setNumberOfChecks(int numberOfChecks) {
		this.numberOfChecks = numberOfChecks;
	}
}
