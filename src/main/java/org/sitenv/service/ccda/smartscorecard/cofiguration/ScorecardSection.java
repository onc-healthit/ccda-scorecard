package org.sitenv.service.ccda.smartscorecard.cofiguration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;


@XmlRootElement(name = "scorecardSection")
@XmlAccessorType(XmlAccessType.FIELD)
public class ScorecardSection {
    
	@XmlAttribute(name="sectionName")
    private String sectionName;
	
	@XmlElement(name="sectionRule")
    private List<SectionRule> sectionRules;

	public String getSectionName() {
		return sectionName;
	}

	public void setSectionName(String sectionName) {
		this.sectionName = sectionName;
	}

	public List<SectionRule> getSectionRules() {
		return sectionRules;
	}

	public void setSectionRules(List<SectionRule> sectionRules) {
		this.sectionRules = sectionRules;
	}

}
