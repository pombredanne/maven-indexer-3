package com.ecsteam.cloudfoundry.maven.indexer;

import java.io.ByteArrayInputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.yaml.snakeyaml.Yaml;

@RestController
public class MavenIndexerController {

	@Value("${maven.repository.base:https://repo1.maven.org/maven2}")
	private String mavenBaseUrl;

	@Autowired(required = false)
	private RestTemplate client;

	private XPath xPath;

	private XPathExpression versionExpression;

	public MavenIndexerController() {
		xPath = XPathFactory.newInstance().newXPath();

		String versionsXPath = "/metadata/versioning/versions/version";
		try {
			versionExpression = xPath.compile(versionsXPath);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		
		String packagingXPath = "/project/packaging";
		try {
			versionExpression = xPath.compile(packagingXPath);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
	}

	@RequestMapping("/index/{groupId}/{artifactId}/index.yml")
	public ResponseEntity<String> renderMavenRepositoryAsYaml(@PathVariable String groupId,
			@PathVariable String artifactId) throws Exception {
		String fixedGroupId = groupId.replace(".", "/");

		String baseUrl = String.format("%s/%s/%s", mavenBaseUrl, fixedGroupId, artifactId);

		String xml;
		try {
			xml = getClient().getForObject(baseUrl + "/maven-metadata.xml", String.class);
		} catch (RestClientException e) {
			if (e instanceof HttpStatusCodeException) {
				HttpStatusCodeException hce = (HttpStatusCodeException) e;
				return new ResponseEntity<String>(hce.getResponseBodyAsString(), hce.getStatusCode());
			}

			throw e;
		}

		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = builderFactory.newDocumentBuilder();
		Document xmlDocument = builder.parse(new ByteArrayInputStream(xml.getBytes()));

		NodeList versions = (NodeList) versionExpression.evaluate(xmlDocument, XPathConstants.NODESET);

		Map<String, String> results = new LinkedHashMap<>(versions.getLength());
		for (int i = 0; i < versions.getLength(); ++i) {
			String version = versions.item(i).getTextContent();
			results.put(version, getArtifactUrl(artifactId, baseUrl, version));
		}

		Yaml yaml = new Yaml();
		String resultYaml = yaml.dump(results);

		return new ResponseEntity<String>(resultYaml, HttpStatus.OK);
	}

	private RestTemplate getClient() {
		if (client == null) {
			client = new RestTemplate();
		}

		return client;
	}

	private String getArtifactUrl(String artifactId, String baseUrl, String version) {
		String pomUrl = String.format("%s/%s-%s.pom", baseUrl, artifactId, version);
		
		return pomUrl;
	}
}
