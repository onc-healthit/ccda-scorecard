package org.sitenv.service.ccda.smartscorecard.entities.inmemory;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name = "VITALS")
public class Vitals {
	
	@Id
    @Column(name = "ID")
    private Integer Id;

    @Column(name = "CODE")
    private String code;

    @Column(name = "DISPLAYNAME")
    private String displayName;

    
    @Column(name = "UCUMCODE")
    private String UCUMCode;


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


	public String getUCUMCode() {
		return UCUMCode;
	}


	public void setUCUMCode(String uCUMCode) {
		UCUMCode = uCUMCode;
	}

}
