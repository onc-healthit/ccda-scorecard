package org.sitenv.service.ccda.smartscorecard.processor;

import org.sitenv.service.ccda.smartscorecard.entities.postgres.ScorecardStatistics;
import org.sitenv.service.ccda.smartscorecard.repositories.postgres.StatisticsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class ScoreCardStatisticProcessor {
	
	@Autowired
	StatisticsRepository statisticsRepository;
	
	public void saveDetails(int docScore)
	{
		ScorecardStatistics scorecardStatistics = new ScorecardStatistics();
		scorecardStatistics.setDoctype("R2.1");
		scorecardStatistics.setDocscore(docScore);
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

}
