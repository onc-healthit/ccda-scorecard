package org.sitenv.service.ccda.smartscorecard.repositories.inmemory;

import org.sitenv.service.ccda.smartscorecard.entities.inmemory.Vitals;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@Repository
public interface VitalsRepository extends JpaRepository<Vitals, Integer> {
	
	@Transactional(readOnly = true)
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Vitals c WHERE c.code = :vitalCode and c.UCUMCode = :ucumCode")
    boolean isUCUMCodeValidForVitalCode(@Param("vitalCode")String vitalCode, @Param("ucumCode")String ucumCode);

}
