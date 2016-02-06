package com.ecsteam.cloudfoundry.maven.indexer;

import java.io.ByteArrayInputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
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

	@Autowired(required = false)
	private RestTemplate client;

	@RequestMapping("/{groupId}/{artifactId}/index.yml")
	public ResponseEntity<String> renderMavenRepositoryAsYaml(@PathVariable String groupId,
			@PathVariable String artifactId) throws Exception {
		String fixedGroupId = groupId.replace(".", "/");

		String baseUrl = String.format("%s/%s/%s", mavenBaseUrl, fixedGroupId, artifactId);

		Document metadataXml = null;
		try {
			String metadataUrl = baseUrl + "/maven-metadata.xml";
			metadataXml = loadXmlFromUrl(metadataUrl);
		} catch (RestClientException e) {
			if (e instanceof HttpStatusCodeException) {
				HttpStatusCodeException hce = (HttpStatusCodeException) e;
				return new ResponseEntity<String>(hce.getResponseBodyAsString(), hce.getStatusCode());
			}

			throw e;
		}

		NodeList versions = metadataXml.getElementsByTagName("version");

		Map<String, String> results = new LinkedHashMap<>(versions.getLength());
		for (int i = 0; i < versions.getLength(); ++i) {
			String version = versions.item(i).getTextContent();
			results.put(version, getArtifactUrl(artifactId, baseUrl, version));
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

	private RestTemplate getClient() {
		if (client == null) {
			client = new RestTemplate();
		}

		return client;
	}

	private String getArtifactUrl(String artifactId, String baseUrl, String version) {
		String pomUrl = String.format("%1$s/%2$s/%3$s-%2$s.pom", baseUrl, version, artifactId);

		String packaging = "jar";
		try {
			Document pomDocument = loadXmlFromUrl(pomUrl);
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

	private Document loadXmlFromUrl(String url) {
		String xml = getClient().getForObject(url, String.class);
		try {
			DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = builderFactory.newDocumentBuilder();
			return builder.parse(new ByteArrayInputStream(xml.getBytes("UTF-8")));
		} catch (Exception t) {
			throw new RuntimeException(t);
		}
	}
}
