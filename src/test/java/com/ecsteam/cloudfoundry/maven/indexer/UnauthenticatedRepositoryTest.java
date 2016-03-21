package com.ecsteam.cloudfoundry.maven.indexer;

import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Comparator;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.yaml.snakeyaml.Yaml;

import com.ecsteam.cloudfoundry.maven.indexer.util.TestApp;
import com.ecsteam.cloudfoundry.maven.indexer.util.TestComparatorConfig;
import com.ecsteam.cloudfoundry.maven.indexer.util.TestUnauthenticatedConfig;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { TestComparatorConfig.class, TestUnauthenticatedConfig.class, TestApp.class })
@WebAppConfiguration
@TestPropertySource(properties = { "maven.repository.base=http://localhost/maven" })
public class UnauthenticatedRepositoryTest {

	@Autowired
	private WebApplicationContext webApplicationContext;

	@Autowired
	@Qualifier("versionOrdering")
	private Comparator<String> comparator;

	private MockMvc mockMvc;

	@Before
	public void init() {
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).dispatchOptions(true).build();
	}

	@Test
	public void testUnauthenticatedMavenRepository() throws Exception {
		MvcResult result = mockMvc.perform(get("/maven/unauthid/artifactid/index.yml")).andDo(print())
				.andExpect(status().isOk()).andReturn();

		String yamlString = result.getResponse().getContentAsString();
		
		@SuppressWarnings("unchecked")
		Map<String, Object> map = (new Yaml()).loadAs(yamlString, Map.class);

		String prevKey = null;
		for (String key : map.keySet()) {
			if (prevKey == null) {
				prevKey = key;
				continue;
			}

			assertTrue("Keys are out of order", comparator.compare(prevKey, key) <= 0);
			prevKey = key;
		}
	}
}
