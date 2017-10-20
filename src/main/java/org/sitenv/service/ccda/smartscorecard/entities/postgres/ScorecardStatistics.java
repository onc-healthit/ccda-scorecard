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
    private int patientIssues;
	
	@Column(name = "allergiessectionissues")
    private int allergiesSectionIssues;
	
	@Column(name = "encounterssectionissues")
    private int encountersSectionIssues;
	
	@Column(name = "immunizationssectionissues")
    private int immunizationsSectionIssues;
	
	@Column(name = "medicationssectionissues")
    private int medicationsSectionIssues;
	
	@Column(name = "problemssectionissues")
    private int problemsSectionIssues;
	
	@Column(name = "proceduressectionissues")
    private int proceduresSectionIssues;
	
	@Column(name = "socialhistorysectionissues")
    private int socialhistorySectionIssues;
	
	@Column(name = "vitalssectionissues")
    private int vitalsSectionIssues;
	
	@Column(name = "resultssectionissues")
    private int resultsSectionIssues;
	
	@Column(name = "miscissues")
    private int miscIssues;
	
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

	public int getPatientIssues() {
		return patientIssues;
	}

	public void setPatientIssues(int patientIssues) {
		this.patientIssues = patientIssues;
	}

	public int getAllergiesSectionIssues() {
		return allergiesSectionIssues;
	}

	public void setAllergiesSectionIssues(int allergiesSectionIssues) {
		this.allergiesSectionIssues = allergiesSectionIssues;
	}

	public int getEncountersSectionIssues() {
		return encountersSectionIssues;
	}

	public void setEncountersSectionIssues(int encountersSectionIssues) {
		this.encountersSectionIssues = encountersSectionIssues;
	}

	public int getImmunizationsSectionIssues() {
		return immunizationsSectionIssues;
	}

	public void setImmunizationsSectionIssues(int immunizationsSectionIssues) {
		this.immunizationsSectionIssues = immunizationsSectionIssues;
	}

	public int getMedicationsSectionIssues() {
		return medicationsSectionIssues;
	}

	public void setMedicationsSectionIssues(int medicationsSectionIssues) {
		this.medicationsSectionIssues = medicationsSectionIssues;
	}

	public int getProblemsSectionIssues() {
		return problemsSectionIssues;
	}

	public void setProblemsSectionIssues(int problemsSectionIssues) {
		this.problemsSectionIssues = problemsSectionIssues;
	}

	public int getProceduresSectionIssues() {
		return proceduresSectionIssues;
	}

	public void setProceduresSectionIssues(int proceduresSectionIssues) {
		this.proceduresSectionIssues = proceduresSectionIssues;
	}

	public int getSocialhistorySectionIssues() {
		return socialhistorySectionIssues;
	}

	public void setSocialhistorySectionIssues(int socialhistorySectionIssues) {
		this.socialhistorySectionIssues = socialhistorySectionIssues;
	}

	public int getVitalsSectionIssues() {
		return vitalsSectionIssues;
	}

	public void setVitalsSectionIssues(int vitalsSectionIssues) {
		this.vitalsSectionIssues = vitalsSectionIssues;
	}

	public int getResultsSectionIssues() {
		return resultsSectionIssues;
	}

	public void setResultsSectionIssues(int resultsSectionIssues) {
		this.resultsSectionIssues = resultsSectionIssues;
	}

	public int getMiscIssues() {
		return miscIssues;
	}

	public void setMiscIssues(int miscIssues) {
		this.miscIssues = miscIssues;
	}
	
	public String getCcdaDocumentType() {
		return ccdaDocumentType;
	}
	
	public void setCcdaDocumentType(String ccdaDocumentType) {
		this.ccdaDocumentType = ccdaDocumentType;
	}	
}
