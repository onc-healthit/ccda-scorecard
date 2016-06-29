package org.sitenv.service.ccda.smartscorecard.processor;

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
	
	public void saveDetails(Results results,String docname)
	{
		ScorecardStatistics scorecardStatistics = new ScorecardStatistics();
		scorecardStatistics.setDoctype(results.getDocType());
		scorecardStatistics.setDocscore(results.getFinalNumericalGrade());
		scorecardStatistics.setDocname(docname);
		for (Category category : results.getCategoryList())
		{
			if(category.getCategoryName().equalsIgnoreCase(ApplicationConstants.CATEGORIES.PATIENT.getCategoryDesc()))
			{
				scorecardStatistics.setPatientScore(category.getCategoryNumericalScore() );
			}
			else if(category.getCategoryName().equalsIgnoreCase(ApplicationConstants.CATEGORIES.ALLERGIES.getCategoryDesc()))
			{
				scorecardStatistics.setAllergiesSectionScore(category.getCategoryNumericalScore());
			}
			else if(category.getCategoryName().equalsIgnoreCase(ApplicationConstants.CATEGORIES.ENCOUNTERS.getCategoryDesc()))
			{
				scorecardStatistics.setEncountersSectionScore(category.getCategoryNumericalScore());
			}
			else if(category.getCategoryName().equalsIgnoreCase(ApplicationConstants.CATEGORIES.MEDICATIONS.getCategoryDesc()))
			{
				scorecardStatistics.setMedicationsSectionScore(category.getCategoryNumericalScore());
			}
			else if(category.getCategoryName().equalsIgnoreCase(ApplicationConstants.CATEGORIES.IMMUNIZATIONS.getCategoryDesc()))
			{
				scorecardStatistics.setImmunizationsSectionScore(category.getCategoryNumericalScore());
			}
			else if(category.getCategoryName().equalsIgnoreCase(ApplicationConstants.CATEGORIES.PROBLEMS.getCategoryDesc()))
			{
				scorecardStatistics.setProblemsSectionScore(category.getCategoryNumericalScore());
			}
			else if(category.getCategoryName().equalsIgnoreCase(ApplicationConstants.CATEGORIES.PROCEDURES.getCategoryDesc()))
			{
				scorecardStatistics.setProceduresSectionScore(category.getCategoryNumericalScore());
			}
			else if(category.getCategoryName().equalsIgnoreCase(ApplicationConstants.CATEGORIES.SOCIALHISTORY.getCategoryDesc()))
			{
				scorecardStatistics.setSocialhistorySectionScore(category.getCategoryNumericalScore());
			}
			else if(category.getCategoryName().equalsIgnoreCase(ApplicationConstants.CATEGORIES.VITALS.getCategoryDesc()))
			{
				scorecardStatistics.setVitalsSectionScore(category.getCategoryNumericalScore());
			}
			else if(category.getCategoryName().equalsIgnoreCase(ApplicationConstants.CATEGORIES.RESULTS.getCategoryDesc()))
			{
				scorecardStatistics.setResultsSectionScore(category.getCategoryNumericalScore());
			}
			else if(category.getCategoryName().equalsIgnoreCase(ApplicationConstants.CATEGORIES.MISC.getCategoryDesc()))
			{
				scorecardStatistics.setMiscScore(category.getCategoryNumericalScore());
			}
		}
		statisticsRepository.save(scorecardStatistics);
	}
	
	public int calculateIndustryAverage()
	{
		if(statisticsRepository.count() > 5 )
		{
			return statisticsRepository.findScoreAverage();
		}else
			return 0;
	}
	
	public long numberOfDocsScored()
	{
		return statisticsRepository.count();
	}

}
