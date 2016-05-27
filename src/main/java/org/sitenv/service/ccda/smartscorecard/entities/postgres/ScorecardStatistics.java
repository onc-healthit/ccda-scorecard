package org.sitenv.service.ccda.smartscorecard.entities.postgres;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "scorecard_statistics")
public class ScorecardStatistics {
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SCORECARD_STATISTICS_SEQ")
	@SequenceGenerator(name = "SCORECARD_STATISTICS_SEQ", sequenceName = "scorecard_statistics_id_seq", allocationSize=1)
	@Column(name = "id")
	Long id;
	
	@Column(name = "doctype")
    private String doctype;
	
	@Column(name = "docscore", nullable = false)
    private int docscore;

	public String getDoctype() {
		return doctype;
	}

	public void setDoctype(String doctype) {
		this.doctype = doctype;
	}

	public int getDocscore() {
		return docscore;
	}

	public void setDocscore(int docscore) {
		this.docscore = docscore;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
}
