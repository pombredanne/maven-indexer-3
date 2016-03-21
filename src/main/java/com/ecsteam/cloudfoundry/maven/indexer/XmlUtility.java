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

import java.io.ByteArrayInputStream;
import java.net.URI;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;

@Component
public class XmlUtility {

	@Autowired(required = false)
	private RestTemplate client;

	public Document getXml(MultiValueMap<String, String> headers, URI uri) {
		RequestEntity<Void> requestEntity = new RequestEntity<Void>(headers, HttpMethod.GET, uri);
		ResponseEntity<String> response = getClient().exchange(requestEntity, String.class);

		try {
			String xml = response.getBody();
			DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = builderFactory.newDocumentBuilder();

			return builder.parse(new ByteArrayInputStream(xml.getBytes("UTF-8")));
		} catch (Exception t) {
			throw new RuntimeException(t);
		}

	}

	private RestTemplate getClient() {
		if (client == null) {
			client = new RestTemplate();
		}

		return client;
	}
}
