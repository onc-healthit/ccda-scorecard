package org.sitenv.service.ccda.smartscorecard.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created by Brian on 2/10/2016.
 */
@XmlRootElement(name = "configurations")
@XmlAccessorType(XmlAccessType.FIELD)
public class Configurations {
    
	@XmlElement(name="scorecardSection")
    private List<ScorecardSection> scorecardSections = null;

	public List<ScorecardSection> getScorecardSections() {
		return scorecardSections;
	}

	public void setScorecardSections(List<ScorecardSection> scorecardSections) {
		this.scorecardSections = scorecardSections;
	}
    
}
