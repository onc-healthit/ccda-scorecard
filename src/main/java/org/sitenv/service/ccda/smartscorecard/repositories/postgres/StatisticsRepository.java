package org.sitenv.service.ccda.smartscorecard.repositories.postgres;

import org.sitenv.service.ccda.smartscorecard.entities.postgres.ScorecardStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@Repository
public interface StatisticsRepository extends JpaRepository<ScorecardStatistics, Integer>{
	
	
	@Transactional(readOnly = true)
	@Query("select count(*) from ScorecardStatistics")
	int findByCount();
	
	@Transactional(readOnly = true)
	@Query("SELECT AVG(docscore) FROM ScorecardStatistics")
	int findScoreAverage();
}
