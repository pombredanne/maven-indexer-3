package com.ecsteam.cloudfoundry.maven.indexer.util;

import static org.mockito.AdditionalMatchers.not;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;

import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;

import com.ecsteam.cloudfoundry.maven.indexer.XmlUtility;

public abstract class TestMocks {
	public static final HttpHeaders GOOD_AUTH_HEADERS = new HttpHeaders();
	public static final HttpHeaders BAD_AUTH_HEADERS = new HttpHeaders();
	public static final HttpHeaders NO_AUTH_HEADERS = new HttpHeaders();
	

	static {
		GOOD_AUTH_HEADERS.add(HttpHeaders.AUTHORIZATION, "Basic abcd");
		BAD_AUTH_HEADERS.add(HttpHeaders.AUTHORIZATION, "Basic wxyz");
	}

	public static XmlUtility getAuthenticatedXmlMock(Environment env) throws Exception {		
		return getMockWithRequiredHeaders(env, GOOD_AUTH_HEADERS);
	}

	public static XmlUtility getUnauthenticatedXmlMock(Environment env) throws Exception {
		return getMockWithRequiredHeaders(env, null);
	}

	private static XmlUtility getMockWithRequiredHeaders(Environment env, HttpHeaders goodHeaders)
			throws Exception {
		
		XmlUtility utility = mock(XmlUtility.class);
		if (goodHeaders != null) {
			when(utility.getXml(eq(goodHeaders), any(URI.class))).thenAnswer(new SuccessfulMockAnswer(env));
			when(utility.getXml(not(eq(goodHeaders)), any(URI.class))).thenAnswer(new UnsuccessfulMockAnswer());
		} else {
			when(utility.getXml(any(HttpHeaders.class), any(URI.class))).thenAnswer(new SuccessfulMockAnswer(env));
		}

		return utility;
	}
}
