package com.ecsteam.cloudfoundry.maven.indexer.util;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.w3c.dom.Document;

public class UnsuccessfulMockAnswer implements Answer<Document> {
	public static final HttpClientErrorException UNAUTH_EXCEPTION = new HttpClientErrorException(
			HttpStatus.UNAUTHORIZED);

	@Override
	public Document answer(InvocationOnMock invocation) throws Throwable {
		throw UNAUTH_EXCEPTION;
	}
}
