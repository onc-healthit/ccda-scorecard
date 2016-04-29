package org.sitenv.service.ccda.smartscorecard.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "LOINC")
public class Loinc {
    @Id
    @Column(name = "ID")
    private Integer Id;

    @Column(name = "CODE")
    private String code;

    @Column(name = "DISPLAYNAME")
    private String displayName;

    @Column(name = "CODESYSTEM")
    private String codeSystem;
    
    @Column(name = "EXAMPLEUCUM")
    private String exampleUCUM;
    
    @Column(name = "EXAMPLEUCUMDISPLAY")
    private String exampleUCUMDisplay;
    
    
    public Integer getId() {
        return Id;
    }

    public void setId(Integer id) {
        Id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getCodeSystem() {
        return codeSystem;
    }

    public void setCodeSystem(String codeSystem) {
        this.codeSystem = codeSystem;
    }

	public String getExampleUCUM() {
		return exampleUCUM;
	}

	public void setExampleUCUM(String exampleUCUM) {
		this.exampleUCUM = exampleUCUM;
	}

	public String getExampleUCUMDisplay() {
		return exampleUCUMDisplay;
	}

	public void setExampleUCUMDisplay(String exampleUCUMDisplay) {
		this.exampleUCUMDisplay = exampleUCUMDisplay;
	}
}