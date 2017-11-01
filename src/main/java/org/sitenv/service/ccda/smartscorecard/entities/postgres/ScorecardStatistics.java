package org.sitenv.service.ccda.smartscorecard.entities.postgres;

import java.sql.Timestamp;

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
	
	@Column(name = "docname")
    private String docname;
	
	@Column(name = "docscore", nullable = false)
    private int docscore;
	
	@Column(name = "patientscore", nullable = false)
    private int patientScore;
	
	@Column(name = "allergiessectionscore", nullable = false)
    private int allergiesSectionScore;
	
	@Column(name = "encounterssectionscore", nullable = false)
    private int encountersSectionScore;
	
	@Column(name = "immunizationssectionscore", nullable = false)
    private int immunizationsSectionScore;
	
	@Column(name = "medicationssectionscore", nullable = false)
    private int medicationsSectionScore;
	
	@Column(name = "problemssectionscore", nullable = false)
    private int problemsSectionScore;
	
	@Column(name = "proceduressectionscore", nullable = false)
    private int proceduresSectionScore;
	
	@Column(name = "socialhistorysectionscore", nullable = false)
    private int socialhistorySectionScore;
	
	@Column(name = "vitalssectionscore", nullable = false)
    private int vitalsSectionScore;
	
	@Column(name = "resultssectionscore", nullable = false)
    private int resultsSectionScore;
	
	@Column(name = "miscscore", nullable = false)
    private int miscScore;
	
	@Column(name = "createtimestamp", nullable = false, updatable = false, insertable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
	Timestamp createTimestamp;
	
	@Column(name = "oneclickscorecard", nullable = true)
	private boolean oneClickScorecard;
	
	@Column(name = "patientissues")
    private Integer patientIssues;
	
	@Column(name = "allergiessectionissues")
    private Integer allergiesSectionIssues;
	
	@Column(name = "encounterssectionissues")
    private Integer encountersSectionIssues;
	
	@Column(name = "immunizationssectionissues")
    private Integer immunizationsSectionIssues;
	
	@Column(name = "medicationssectionissues")
    private Integer medicationsSectionIssues;
	
	@Column(name = "problemssectionissues")
    private Integer problemsSectionIssues;
	
	@Column(name = "proceduressectionissues")
    private Integer proceduresSectionIssues;
	
	@Column(name = "socialhistorysectionissues")
    private Integer socialhistorySectionIssues;
	
	@Column(name = "vitalssectionissues")
    private Integer vitalsSectionIssues;
	
	@Column(name = "resultssectionissues")
    private Integer resultsSectionIssues;
	
	@Column(name = "miscissues")
    private Integer miscIssues;
	
	@Column(name = "ccdadocumenttype")
	private String ccdaDocumentType;


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
	
	public String getDocname() {
		return docname;
	}

	public void setDocname(String docname) {
		this.docname = docname;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public int getPatientScore() {
		return patientScore;
	}

	public void setPatientScore(int patientScore) {
		this.patientScore = patientScore;
	}

	public int getAllergiesSectionScore() {
		return allergiesSectionScore;
	}

	public void setAllergiesSectionScore(int allergiesSectionScore) {
		this.allergiesSectionScore = allergiesSectionScore;
	}

	public int getEncountersSectionScore() {
		return encountersSectionScore;
	}

	public void setEncountersSectionScore(int encountersSectionScore) {
		this.encountersSectionScore = encountersSectionScore;
	}

	public int getImmunizationsSectionScore() {
		return immunizationsSectionScore;
	}

	public void setImmunizationsSectionScore(int immunizationsSectionScore) {
		this.immunizationsSectionScore = immunizationsSectionScore;
	}

	public int getMedicationsSectionScore() {
		return medicationsSectionScore;
	}

	public void setMedicationsSectionScore(int medicationsSectionScore) {
		this.medicationsSectionScore = medicationsSectionScore;
	}

	public int getProblemsSectionScore() {
		return problemsSectionScore;
	}

	public void setProblemsSectionScore(int problemsSectionScore) {
		this.problemsSectionScore = problemsSectionScore;
	}

	public int getProceduresSectionScore() {
		return proceduresSectionScore;
	}

	public void setProceduresSectionScore(int proceduresSectionScore) {
		this.proceduresSectionScore = proceduresSectionScore;
	}

	public int getSocialhistorySectionScore() {
		return socialhistorySectionScore;
	}

	public void setSocialhistorySectionScore(int socialhistorySectionScore) {
		this.socialhistorySectionScore = socialhistorySectionScore;
	}

	public int getVitalsSectionScore() {
		return vitalsSectionScore;
	}

	public void setVitalsSectionScore(int vitalsSectionScore) {
		this.vitalsSectionScore = vitalsSectionScore;
	}

	public int getResultsSectionScore() {
		return resultsSectionScore;
	}

	public void setResultsSectionScore(int resultsSectionScore) {
		this.resultsSectionScore = resultsSectionScore;
	}

	public int getMiscScore() {
		return miscScore;
	}

	public void setMiscScore(int miscScore) {
		this.miscScore = miscScore;
	}

	public Timestamp getCreateTimestamp() {
		return createTimestamp;
	}

	public void setCreateTimestamp(Timestamp createTimestamp) {
		this.createTimestamp = createTimestamp;
	}

	public boolean isOneClickScorecard() {
		return oneClickScorecard;
	}

	public void setOneClickScorecard(boolean oneClickScorecard) {
		this.oneClickScorecard = oneClickScorecard;
	}

	public Integer getPatientIssues() {
		return patientIssues;
	}

	public void setPatientIssues(Integer patientIssues) {
		this.patientIssues = patientIssues;
	}

	public Integer getAllergiesSectionIssues() {
		return allergiesSectionIssues;
	}

	public void setAllergiesSectionIssues(Integer allergiesSectionIssues) {
		this.allergiesSectionIssues = allergiesSectionIssues;
	}

	public Integer getEncountersSectionIssues() {
		return encountersSectionIssues;
	}

	public void setEncountersSectionIssues(Integer encountersSectionIssues) {
		this.encountersSectionIssues = encountersSectionIssues;
	}

	public Integer getImmunizationsSectionIssues() {
		return immunizationsSectionIssues;
	}

	public void setImmunizationsSectionIssues(Integer immunizationsSectionIssues) {
		this.immunizationsSectionIssues = immunizationsSectionIssues;
	}

	public Integer getMedicationsSectionIssues() {
		return medicationsSectionIssues;
	}

	public void setMedicationsSectionIssues(Integer medicationsSectionIssues) {
		this.medicationsSectionIssues = medicationsSectionIssues;
	}

	public Integer getProblemsSectionIssues() {
		return problemsSectionIssues;
	}

	public void setProblemsSectionIssues(Integer problemsSectionIssues) {
		this.problemsSectionIssues = problemsSectionIssues;
	}

	public Integer getProceduresSectionIssues() {
		return proceduresSectionIssues;
	}

	public void setProceduresSectionIssues(Integer proceduresSectionIssues) {
		this.proceduresSectionIssues = proceduresSectionIssues;
	}

	public Integer getSocialhistorySectionIssues() {
		return socialhistorySectionIssues;
	}

	public void setSocialhistorySectionIssues(Integer socialhistorySectionIssues) {
		this.socialhistorySectionIssues = socialhistorySectionIssues;
	}

	public Integer getVitalsSectionIssues() {
		return vitalsSectionIssues;
	}

	public void setVitalsSectionIssues(Integer vitalsSectionIssues) {
		this.vitalsSectionIssues = vitalsSectionIssues;
	}

	public Integer getResultsSectionIssues() {
		return resultsSectionIssues;
	}

	public void setResultsSectionIssues(Integer resultsSectionIssues) {
		this.resultsSectionIssues = resultsSectionIssues;
	}

	public Integer getMiscIssues() {
		return miscIssues;
	}

	public void setMiscIssues(Integer miscIssues) {
		this.miscIssues = miscIssues;
	}

	public String getCcdaDocumentType() {
		return ccdaDocumentType;
	}
	
	public void setCcdaDocumentType(String ccdaDocumentType) {
		this.ccdaDocumentType = ccdaDocumentType;
	}	
}
