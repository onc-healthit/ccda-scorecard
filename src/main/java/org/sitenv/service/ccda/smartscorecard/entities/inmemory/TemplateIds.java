package org.sitenv.service.ccda.smartscorecard.entities.inmemory;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "TEMPLATEIDS")
public class TemplateIds {
	
	
	@Id
    @Column(name = "ID")
    private Integer Id;
	
	@Column(name = "TEMPLATETITLE")
	private String templateTitle;
	
	@Column(name = "TEMPLATETYPE")
	private String templateType;
	
	@Column(name = "TEMPLATEID")
	private String templateId;

	public Integer getId() {
		return Id;
	}

	public void setId(Integer id) {
		Id = id;
	}

	public String getTemplateTitle() {
		return templateTitle;
	}

	public void setTemplateTitle(String templateTitle) {
		this.templateTitle = templateTitle;
	}

	public String getTemplateType() {
		return templateType;
	}

	public void setTemplateType(String templateType) {
		this.templateType = templateType;
	}

	public String getTemplateId() {
		return templateId;
	}

	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}
}
