package com.ecsteam.cloudfoundry.maven.indexer.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import com.ecsteam.cloudfoundry.maven.indexer.XmlUtility;

@Configuration
public class TestAuthenticatedConfig {
	@Autowired
	private Environment env;
	
	@Bean
	public XmlUtility xmlUtility() throws Exception {
		return TestMocks.getAuthenticatedXmlMock(env);
	}
}
