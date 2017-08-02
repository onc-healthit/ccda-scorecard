package org.sitenv.service.ccda.smartscorecard.repositories.inmemory;

import org.sitenv.service.ccda.smartscorecard.entities.inmemory.TemplateIdsR21;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@Repository
public interface TemplateIdRepository21 extends JpaRepository<TemplateIdsR21, Integer>{

	
	@Transactional(readOnly = true)
	@Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM TemplateIdsR21 c WHERE c.templateId = :templateId")
	boolean findByTemplateId(@Param("templateId")String templateId);
	
	
	@Transactional(readOnly = true)
	@Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM TemplateIdsR21 c WHERE c.templateId = :templateId and c.extension = :extension")
	boolean findByTemplateIdAndExtension(@Param("templateId")String templateId, @Param("extension")String extension);
}
