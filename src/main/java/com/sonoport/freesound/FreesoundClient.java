/*
 * Copyright 2014 Sonoport (Asia) Pte Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sonoport.freesound;

import java.io.IOException;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.sonoport.freesound.query.Query;

/**
 * Client used to make calls to the freesound.org API (v2).
 *
 * Users of this library must first register their application with Freesound (http://www.freesound.org/apiv2/apply).
 * The credentials generated (Client ID & Client Secret/API Key) are then used to construct an instance of this class.
 */
public class FreesoundClient {

	/** Base address for all calls to the freesound.org APIv2. */
	private static final String API_ENDPOINT = "https://www.freesound.org/apiv2";

	/** The Client ID created by freesound.org for the application. */
	private final String clientId;

	/** The Client Secret/API Key generated by freesound.org for the application. */
	private final String clientSecret;

	/**
	 * @param clientId Client ID for application
	 * @param clientSecret Client Secret (API Key) for application
	 */
	public FreesoundClient(final String clientId, final String clientSecret) {
		this.clientId = clientId;
		this.clientSecret = clientSecret;
	}

	/**
	 * Execute a given query (synchronously) against the freesound API.
	 *
	 * @param query The query to execute
	 */
	public void executeQuery(final Query<?> query) {
		final String url = API_ENDPOINT + query.getPath();
		final String token = String.format("Token %s", clientSecret);

		try {
			final HttpResponse<JsonNode> httpResponse =
					Unirest.get(url).fields(query.getQueryParameters()).header("Authorization", token).asJson();
			query.setResponse(httpResponse);
		} catch (final UnirestException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Shutdown the client. Ensures that all background processes are properly terminated so that application can be
	 * shutdown cleanly.
	 *
	 * @throws FreesoundClientException Any errors encountered performing shutdown
	 */
	public void shutdown() throws FreesoundClientException {
		try {
			Unirest.shutdown();
		} catch (final IOException e) {
			throw new FreesoundClientException("Error shutting down background Unirest service", e);
		}
	}
}
