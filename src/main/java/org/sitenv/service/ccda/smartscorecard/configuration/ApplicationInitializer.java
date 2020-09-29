package org.sitenv.service.ccda.smartscorecard.configuration;

import org.springframework.core.annotation.Order;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

@Order(1)
public class ApplicationInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

	@Override
	protected Class<?>[] getRootConfigClasses() {
		return new Class<?>[] { 
				MvcConfiguration.class,PersistanceConfiguration.class,PersistanceConfigurationPostGres.class};
	}

	@Override
	protected Class<?>[] getServletConfigClasses() {
		return new Class<?>[] {};
	}

	@Override
	protected String[] getServletMappings() {
		return new String[] { "/" };
	}

}