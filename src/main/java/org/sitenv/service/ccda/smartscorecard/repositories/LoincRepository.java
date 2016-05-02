package org.sitenv.service.ccda.smartscorecard.repositories;

import org.sitenv.service.ccda.smartscorecard.entities.Loinc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Transactional(readOnly = true)
@Repository
public interface LoincRepository extends JpaRepository<Loinc, Integer> {
      
	
	@Transactional(readOnly = true)
	@Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Loinc c WHERE c.code = :code")
	boolean findByCode(String code);
    
	Loinc findByDisplayName(String displayName);
  
    @Transactional(readOnly = true)
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Loinc c WHERE c.codeSystem in (:codesystems)")
    boolean foundCodesystems(@Param("codesystems")List<String> codesystems);

    @Transactional(readOnly = true)
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Loinc c WHERE c.code = :code and c.displayName = :displayName and c.codeSystem in (:codesystems)")
    boolean foundCodeAndDisplayNameInCodesystem(@Param("code")String code, @Param("displayName")String displayName, @Param("codesystems")List<String> codesystems);

    @Transactional(readOnly = true)
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Loinc c WHERE c.code = :code and c.codeSystem in (:codesystems)")
    boolean foundCodeInCodesystems(@Param("code")String code, @Param("codesystems")List<String> codesystems);

    @Transactional(readOnly = true)
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Loinc c WHERE c.displayName = :displayName and c.codeSystem in (:codesystems)")
    boolean foundDisplayNameInCodesystems(@Param("displayName")String displayName, @Param("codesystems")List<String> codesystems);
    
    @Transactional(readOnly = true)
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Loinc c WHERE c.code = :loincCode and c.exampleUCUMDisplay = :ucumCode")
    boolean foundUCUMUnitsForLoincCode(@Param("loincCode")String loincCode, @Param("ucumCode")String ucumCode);

}
