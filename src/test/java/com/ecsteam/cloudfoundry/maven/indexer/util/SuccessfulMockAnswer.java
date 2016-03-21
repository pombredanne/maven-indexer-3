package com.ecsteam.cloudfoundry.maven.indexer.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import lombok.AllArgsConstructor;
import lombok.Getter;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.core.env.ConfigurablePropertyResolver;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySourcesPropertyResolver;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;

public class SuccessfulMockAnswer implements Answer<Document> {

	private Environment env;

	public SuccessfulMockAnswer(Environment env) {
		this.env = env;
	}

	@Override
	public Document answer(InvocationOnMock invocation) throws Throwable {
		Object[] args = invocation.getArguments();
		URI uri = (URI) args[1];

		return loadXml(env, uri.toString());
	}

	private String getClassPathLocationFromUrl(Environment env, String url, GavInfo info) {
		if (StringUtils.isEmpty(url)) {
			return null;
		}

		StringBuilder location = new StringBuilder();
		if (url.endsWith("maven-metadata.xml")) {
			location.append("metadata/").append(info.getGroupId()).append(".xml");
		} else if (url.endsWith(".pom")) {
			location.append("pom/template.pom");
		}

		return location.toString();
	}

	private GavInfo getGavInfo(Environment env, String url) {
		String groupId = null;
		String artifactId = null;
		String version = null;

		String base = env.getProperty("maven.repository.base", "http://localhost/");
		if (!url.startsWith(base)) {
			throw new IllegalArgumentException();
		}

		String gav = url.substring(base.length());
		if (gav.startsWith("/")) {
			gav = gav.substring(1);
		}

		String[] parts = gav.split("/");

		if (url.endsWith("maven-metadata.xml")) {
			groupId = StringUtils.collectionToDelimitedString(Arrays.asList(parts).subList(0, parts.length - 2), ".");
			artifactId = parts[parts.length - 2];
		} else if (url.endsWith(".pom")) {
			groupId = StringUtils.collectionToDelimitedString(Arrays.asList(parts).subList(0, parts.length - 3), ".");
			artifactId = parts[parts.length - 3];
			version = parts[parts.length - 2];
		}

		return new GavInfo(groupId, artifactId, version);
	}

	private Document loadXml(Environment env, String url) {
		GavInfo info = getGavInfo(env, url);

		String resourceUri = getClassPathLocationFromUrl(env, url, info);
		try {
			Resource resource = new ClassPathResource(resourceUri);
			DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = builderFactory.newDocumentBuilder();

			String xml = convertStreamToString(resource.getInputStream());

			MutablePropertySources sources = new MutablePropertySources();
			sources.addFirst(new MapPropertySource("gav", info.toMap()));
			ConfigurablePropertyResolver resolver = new PropertySourcesPropertyResolver(sources);

			String resolvedXml = resolver.resolvePlaceholders(xml);

			return builder.parse(new ByteArrayInputStream(resolvedXml.getBytes("UTF-8")));
		} catch (Exception t) {
			throw new RuntimeException(t);
		}
	}

	// adapted from http://stackoverflow.com/questions/309424/read-convert-an-inputstream-to-a-string
	private String convertStreamToString(InputStream is) throws IOException {
		if (is == null) {
			return null;
		}

		Scanner s = new Scanner(is);
		s.useDelimiter("\\A");
		String rv = s.hasNext() ? s.next() : "";

		s.close();

		return rv;
	}

	@Getter
	@AllArgsConstructor
	public static class GavInfo {
		private String groupId;
		private String artifactId;
		private String version;

		public Map<String, Object> toMap() {
			Map<String, Object> map = new HashMap<>();
			map.put("TEST.groupId", groupId);
			map.put("TEST.artifactId", artifactId);
			map.put("TEST.version", version);

			return map;
		}
	}

}
