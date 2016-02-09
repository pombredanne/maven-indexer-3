package com.ecsteam.cloudfoundry.maven.indexer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = MavenIndexerApplicationTests.TestApp.class)
@WebAppConfiguration
public class MavenIndexerApplicationTests {

	@Test
	public void contextLoads() {
	}

	@EnableAutoConfiguration
	@Configuration
	@EnableMavenIndexer
	public static class TestApp {
		public static void main(String[] args) {
			SpringApplication.run(TestApp.class, args);
		}
	}
}
