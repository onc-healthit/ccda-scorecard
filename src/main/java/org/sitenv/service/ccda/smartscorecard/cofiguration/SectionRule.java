package org.sitenv.service.ccda.smartscorecard.cofiguration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "sectionRule")
@XmlAccessorType(XmlAccessType.FIELD)
public class SectionRule {
	
	@XmlAttribute(name="ruleName")
    private String ruleName;
	
	@XmlAttribute(name="ruleId")
    private String ruleId;
	
	@XmlAttribute(name="ruleEnabled")
    private boolean ruleEnabled;
	
	public String getRuleId() {
		return ruleId;
	}

	public void setRuleId(String ruleId) {
		this.ruleId = ruleId;
	}

	public String getRuleName() {
		return ruleName;
	}

	public void setRuleName(String ruleName) {
		this.ruleName = ruleName;
	}

	public boolean isRuleEnabled() {
		return ruleEnabled;
	}

	public void setRuleEnabled(boolean ruleEnabled) {
		this.ruleEnabled = ruleEnabled;
	}
}
