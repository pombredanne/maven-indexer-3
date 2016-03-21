package com.ecsteam.cloudfoundry.maven.indexer.util;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;

import com.ecsteam.cloudfoundry.maven.indexer.EnableMavenIndexer;

@EnableAutoConfiguration
@Configuration
@EnableMavenIndexer
public class TestApp {
	public static void main(String[] args) {
		SpringApplication.run(TestApp.class, args);
	}
}
