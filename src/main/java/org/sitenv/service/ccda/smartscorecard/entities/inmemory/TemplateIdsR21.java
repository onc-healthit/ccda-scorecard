package org.sitenv.service.ccda.smartscorecard.entities.inmemory;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "TEMPLATEIDSR21")
public class TemplateIdsR21 {
	
	
	@Id
    @Column(name = "ID")
    private Integer Id;
	
	@Column(name = "TEMPLATETITLE")
	private String templateTitle;
	
	@Column(name = "TEMPLATETYPE")
	private String templateType;
	
	@Column(name = "TEMPLATEID")
	private String templateId;
	
	@Column(name = "EXTENSION")
	private String extension;

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

	public String getExtension() {
		return extension;
	}

	public void setExtension(String extension) {
		this.extension = extension;
	}
}
