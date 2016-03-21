package com.ecsteam.cloudfoundry.maven.indexer;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.ecsteam.cloudfoundry.maven.indexer.util.TestApp;
import com.ecsteam.cloudfoundry.maven.indexer.util.TestAuthenticatedConfig;
import com.ecsteam.cloudfoundry.maven.indexer.util.TestMocks;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { TestAuthenticatedConfig.class, TestApp.class })
@WebAppConfiguration
@TestPropertySource(properties = { "maven.repository.base=http://localhost/maven" })
public class AuthenticatedRepositoryTest {

	@Autowired
	private WebApplicationContext webApplicationContext;

	private MockMvc mockMvc;

	@Before
	public void init() {
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).dispatchOptions(true).build();
	}

	@Test
	public void testAuthenticatedMavenRepository() throws Exception {
		mockMvc.perform(get("/maven/authid/artifactid/index.yml").headers(TestMocks.GOOD_AUTH_HEADERS)).andDo(print())
				.andExpect(status().isOk());
	}

	@Test
	public void testBadAuthenticatedMavenRepository() throws Exception {
		mockMvc.perform(get("/maven/authid/artifactid/index.yml").headers(TestMocks.BAD_AUTH_HEADERS)).andDo(print())
				.andExpect(status().isUnauthorized());
	}

	@Test
	public void testUnauthenticatedMavenRepository() throws Exception {
		mockMvc.perform(get("/maven/authid/artifactid/index.yml")).andDo(print()).andExpect(status().isUnauthorized());
	}
}
