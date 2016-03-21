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

import java.net.URI;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.Yaml;

@RestController
@RequestMapping(value = "/maven", method = RequestMethod.GET)
public class MavenIndexerController {

	@Value("${maven.repository.base:https://repo1.maven.org/maven2}")
	private String mavenBaseUrl;

	@Autowired
	private XmlUtility xmlClient;
	
	@Autowired
	@Qualifier("versionOrdering")
	private Comparator<String> versionOrdering;

	@RequestMapping("/{groupId}/{artifactId}/index.yml")
	public ResponseEntity<String> renderMavenRepositoryAsYaml(@PathVariable String groupId,
			@PathVariable String artifactId, @RequestHeader HttpHeaders requestHeaders) throws Exception {
		String fixedGroupId = groupId.replace(".", "/");

		String baseUrl = String.format("%s/%s/%s", mavenBaseUrl, fixedGroupId, artifactId);

		Document metadataXml = null;
		try {
			String metadataUrl = baseUrl + "/maven-metadata.xml";
			metadataXml = xmlClient.getXml(requestHeaders, new URI(metadataUrl));
		} catch (RestClientException e) {
			if (e instanceof HttpStatusCodeException) {
				HttpStatusCodeException hce = (HttpStatusCodeException) e;
				return new ResponseEntity<String>(hce.getResponseBodyAsString(), hce.getResponseHeaders(),
						hce.getStatusCode());
			}

			throw e;
		}

		NodeList versions = metadataXml.getElementsByTagName("version");

		Map<String, String> results = new TreeMap<>(versionOrdering);
		for (int i = 0; i < versions.getLength(); ++i) {
			String version = versions.item(i).getTextContent();
			results.put(version, getArtifactUrl(artifactId, baseUrl, version, requestHeaders));
		}

		DumperOptions options = new DumperOptions();
		options.setDefaultFlowStyle(FlowStyle.BLOCK);
		options.setExplicitStart(true);

		Yaml yaml = new Yaml(options);
		String resultYaml = yaml.dump(results);
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE);

		return new ResponseEntity<String>(resultYaml, headers, HttpStatus.OK);
	}

	private String getArtifactUrl(String artifactId, String baseUrl, String version, HttpHeaders requestHeaders) {
		String pomUrl = String.format("%1$s/%2$s/%3$s-%2$s.pom", baseUrl, version, artifactId);

		String packaging = "jar";
		try {
			Document pomDocument = xmlClient.getXml(requestHeaders, new URI(pomUrl));
			NodeList packagingNodes = pomDocument.getElementsByTagName("packaging");

			if (packagingNodes.getLength() > 0) {
				Node packageNode = packagingNodes.item(0);
				packaging = packageNode.getTextContent();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return String.format("%1$s/%2$s/%3$s-%2$s.%4$s", baseUrl, version, artifactId, packaging);
	}
}
