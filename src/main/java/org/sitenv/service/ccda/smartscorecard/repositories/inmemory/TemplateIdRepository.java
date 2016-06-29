package org.sitenv.service.ccda.smartscorecard.repositories.inmemory;

import org.sitenv.service.ccda.smartscorecard.entities.inmemory.TemplateIds;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@Repository
public interface TemplateIdRepository extends JpaRepository<TemplateIds, Integer>{

	
	@Transactional(readOnly = true)
	@Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM TemplateIds c WHERE c.templateId = :templateId")
	boolean findByTemplateId(@Param("templateId")String templateId);
}
