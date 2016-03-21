/**
 * Copyright 2016 ECS Team, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.ecsteam.cloudfoundry.maven.indexer;

import java.util.Comparator;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MavenIndexerConfiguration {
	
	@Bean
	@ConditionalOnMissingBean(MavenIndexerController.class)
	public MavenIndexerController mavenIndexController() {
		return new MavenIndexerController();
	}
	
	@Bean
	@ConditionalOnMissingBean(XmlUtility.class)
	public XmlUtility xmlUtility() {
		return new XmlUtility();
	}
	
	@Bean(name = "versionOrdering")
	@ConditionalOnMissingBean(name = "versionOrdering")
	public Comparator<String> versionOrdering() {
		return String.CASE_INSENSITIVE_ORDER.reversed();
	}
}
