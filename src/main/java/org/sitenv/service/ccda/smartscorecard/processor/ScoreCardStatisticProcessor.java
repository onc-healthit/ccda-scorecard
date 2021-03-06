package org.sitenv.service.ccda.smartscorecard.processor;

import java.sql.Timestamp;
import java.util.List;

import org.sitenv.service.ccda.smartscorecard.entities.postgres.ScorecardStatistics;
import org.sitenv.service.ccda.smartscorecard.model.Category;
import org.sitenv.service.ccda.smartscorecard.model.Results;
import org.sitenv.service.ccda.smartscorecard.repositories.postgres.StatisticsRepository;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class ScoreCardStatisticProcessor {
	
	@Autowired
	StatisticsRepository statisticsRepository;
	
	public void saveDetails(Results results,String docname,boolean isOneClickScorecard) 
	{
		saveDetails(results,docname,isOneClickScorecard,"Unknown","ccdascorecardservice");
	}
	
	public void saveDetails(Results results,String docname,boolean isOneClickScorecard,String ccdaDocumentType,
			String directEmailAddress)
	{
		ScorecardStatistics scorecardStatistics = new ScorecardStatistics();
		scorecardStatistics.setDoctype(results.getCcdaVersion());
		scorecardStatistics.setDocscore(results.getFinalNumericalGrade());
		scorecardStatistics.setDocname(docname);
		scorecardStatistics.setOneClickScorecard(isOneClickScorecard);
		scorecardStatistics.setCcdaDocumentType(ccdaDocumentType);
		scorecardStatistics.setDirectEmailAddress(directEmailAddress);
		for (Category category : results.getCategoryList())
		{
			if(category.getCategoryName().equalsIgnoreCase(ApplicationConstants.CATEGORIES.PATIENT.getCategoryDesc()))
			{
				scorecardStatistics.setPatientScore(category.getCategoryNumericalScore() );
				scorecardStatistics.setPatientIssues(category.getNumberOfIssues());
			}
			else if(category.getCategoryName().equalsIgnoreCase(ApplicationConstants.CATEGORIES.ALLERGIES.getCategoryDesc()))
			{
				scorecardStatistics.setAllergiesSectionScore(category.getCategoryNumericalScore());
				scorecardStatistics.setAllergiesSectionIssues(category.getNumberOfIssues());
			}
			else if(category.getCategoryName().equalsIgnoreCase(ApplicationConstants.CATEGORIES.ENCOUNTERS.getCategoryDesc()))
			{
				scorecardStatistics.setEncountersSectionScore(category.getCategoryNumericalScore());
				scorecardStatistics.setEncountersSectionIssues(category.getNumberOfIssues());
			}
			else if(category.getCategoryName().equalsIgnoreCase(ApplicationConstants.CATEGORIES.MEDICATIONS.getCategoryDesc()))
			{
				scorecardStatistics.setMedicationsSectionScore(category.getCategoryNumericalScore());
				scorecardStatistics.setMedicationsSectionIssues(category.getNumberOfIssues());
			}
			else if(category.getCategoryName().equalsIgnoreCase(ApplicationConstants.CATEGORIES.IMMUNIZATIONS.getCategoryDesc()))
			{
				scorecardStatistics.setImmunizationsSectionScore(category.getCategoryNumericalScore());
				scorecardStatistics.setImmunizationsSectionIssues(category.getNumberOfIssues());
			}
			else if(category.getCategoryName().equalsIgnoreCase(ApplicationConstants.CATEGORIES.PROBLEMS.getCategoryDesc()))
			{
				scorecardStatistics.setProblemsSectionScore(category.getCategoryNumericalScore());
				scorecardStatistics.setProblemsSectionIssues(category.getNumberOfIssues());
			}
			else if(category.getCategoryName().equalsIgnoreCase(ApplicationConstants.CATEGORIES.PROCEDURES.getCategoryDesc()))
			{
				scorecardStatistics.setProceduresSectionScore(category.getCategoryNumericalScore());
				scorecardStatistics.setProceduresSectionIssues(category.getNumberOfIssues());
			}
			else if(category.getCategoryName().equalsIgnoreCase(ApplicationConstants.CATEGORIES.SOCIALHISTORY.getCategoryDesc()))
			{
				scorecardStatistics.setSocialhistorySectionScore(category.getCategoryNumericalScore());
				scorecardStatistics.setSocialhistorySectionIssues(category.getNumberOfIssues());
			}
			else if(category.getCategoryName().equalsIgnoreCase(ApplicationConstants.CATEGORIES.VITALS.getCategoryDesc()))
			{
				scorecardStatistics.setVitalsSectionScore(category.getCategoryNumericalScore());
				scorecardStatistics.setVitalsSectionIssues(category.getNumberOfIssues());
			}
			else if(category.getCategoryName().equalsIgnoreCase(ApplicationConstants.CATEGORIES.RESULTS.getCategoryDesc()))
			{
				scorecardStatistics.setResultsSectionScore(category.getCategoryNumericalScore());
				scorecardStatistics.setResultsSectionIssues(category.getNumberOfIssues());
			}
			else if(category.getCategoryName().equalsIgnoreCase(ApplicationConstants.CATEGORIES.MISC.getCategoryDesc()))
			{
				scorecardStatistics.setMiscScore(category.getCategoryNumericalScore());
				scorecardStatistics.setMiscIssues(category.getNumberOfIssues());
			}
		}
		statisticsRepository.save(scorecardStatistics);
	}
	
	public int calculateIndustryAverage(boolean isOneClickScorecard)
	{
		if(statisticsRepository.findByCount(isOneClickScorecard) > 5 )
		{
			return statisticsRepository.findScoreAverage(isOneClickScorecard);
		}else
			return 0;
	}
	
	public int calculateIndustryAverageScoreForCcdaDocumentType(String ccdaDocumentType, boolean isOneClickScorecard) 
	{
		return statisticsRepository.findAverageOfScoresForCcdaDocumentType(ccdaDocumentType, isOneClickScorecard);
	}
	
	public long numberOfDocsScored(boolean isOneClickScorecard)
	{
		return statisticsRepository.findByCount(isOneClickScorecard);
	}
	
	public long numberOfDocsScoredPerCcdaDocumentType(String ccdaDocumentType, boolean isOneClickScorecard)
	{
		return statisticsRepository.findCountOfDocsScoredPerCcdaDocumentType(ccdaDocumentType, isOneClickScorecard);
	}
	
	public List<ScorecardStatistics> getAllRecords()
	{
		return statisticsRepository.findAll();
	}
	
	public List<ScorecardStatistics> getAllRecordsForDateRange(Timestamp fromDate, Timestamp toDate)
	{
		return statisticsRepository.findByDateRange(fromDate,toDate);
	}

}
