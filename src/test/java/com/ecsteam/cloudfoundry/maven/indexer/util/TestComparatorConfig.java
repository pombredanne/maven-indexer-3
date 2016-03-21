package com.ecsteam.cloudfoundry.maven.indexer.util;

import java.util.Comparator;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestComparatorConfig {
	
	@Bean(name = "versionOrdering")
	public Comparator<String> customComparator() {
		return String.CASE_INSENSITIVE_ORDER;
	}
}
