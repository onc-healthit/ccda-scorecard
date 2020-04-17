package org.sitenv.service.ccda.smartscorecard.repositories.postgres;

import java.sql.Timestamp;
import java.util.List;

import org.sitenv.service.ccda.smartscorecard.entities.postgres.ScorecardStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@Repository
public interface StatisticsRepository extends JpaRepository<ScorecardStatistics, Integer> {
	
	@Transactional(readOnly = true)
	@Query("SELECT count(*) FROM ScorecardStatistics s where s.oneClickScorecard = :isOneClickScorecard")
	long findByCount(@Param("isOneClickScorecard")boolean isOneClickScorecard);
	
	@Transactional(readOnly = true)
	@Query("SELECT AVG(docscore) FROM ScorecardStatistics s where s.oneClickScorecard = :isOneClickScorecard")
	int findScoreAverage(@Param("isOneClickScorecard")boolean isOneClickScorecard);
	
	@Transactional(readOnly = true)
	@Query("SELECT COUNT(ccdadocumenttype) FROM ScorecardStatistics "
			+ "WHERE ccdadocumenttype = :ccdaDocumentTypeBeingSearchedFor AND oneClickScorecard = :isOneClickScorecard")
	long findCountOfDocsScoredPerCcdaDocumentType(@Param("ccdaDocumentTypeBeingSearchedFor")String ccdaDocumentTypeBeingSearchedFor,
			@Param("isOneClickScorecard")boolean isOneClickScorecard);
	
	@Transactional(readOnly = true)
	@Query("SELECT AVG(docscore) FROM ScorecardStatistics "
			+ "WHERE ccdadocumenttype = :ccdaDocumentTypeBeingSearchedFor AND oneClickScorecard = :isOneClickScorecard")
	int findAverageOfScoresForCcdaDocumentType(@Param("ccdaDocumentTypeBeingSearchedFor")String ccdaDocumentTypeBeingSearchedFor,
			@Param("isOneClickScorecard")boolean isOneClickScorecard);
	
	@Transactional(readOnly = true)
	@Query("select s from ScorecardStatistics s WHERE createTimestamp >= :fromDate AND createTimestamp <= :toDate order by createTimestamp desc")
	List<ScorecardStatistics> findByDateRange(@Param("fromDate")Timestamp fromDate,
			@Param("toDate")Timestamp toDate);
	
	@Transactional(readOnly = true)
	@Query("SELECT docscore FROM ScorecardStatistics")
	List<Integer> getAllDocScores();
	
}
