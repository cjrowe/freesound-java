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
import java.io.InputStream;
import java.util.Map.Entry;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.GetRequest;
import com.mashape.unirest.request.HttpRequest;
import com.mashape.unirest.request.HttpRequestWithBody;
import com.sonoport.freesound.query.BinaryResponseQuery;
import com.sonoport.freesound.query.JSONResponseQuery;
import com.sonoport.freesound.query.OAuthQuery;
import com.sonoport.freesound.query.PagingQuery;
import com.sonoport.freesound.query.Query;
import com.sonoport.freesound.query.oauth2.AccessTokenQuery;
import com.sonoport.freesound.query.oauth2.OAuth2AccessTokenRequest;
import com.sonoport.freesound.query.oauth2.RefreshOAuth2AccessTokenRequest;
import com.sonoport.freesound.response.AccessTokenDetails;

/**
 * Client used to make calls to the freesound.org API (v2).
 *
 * Users of this library must first register their application with Freesound (http://www.freesound.org/apiv2/apply).
 * The credentials generated (Client ID & Client Secret/API Key) are then used to construct an instance of this class.
 */
public class FreesoundClient {

	/** Base address for all calls to the freesound.org APIv2. */
	protected static final String API_ENDPOINT = "https://www.freesound.org/apiv2";

	/** Name of the HTTP Header specifying the user agent string. */
	protected static final String HTTP_USER_AGENT_HEADER = "User-Agent";

	/** The default User-Agent string to pass with requests, if user does not specify their own. */
	protected static final String DEFAULT_USER_AGENT_STRING =
			"Sonoport-freesound-java/0.5.0 (https://github.com/Sonoport/freesound-java)";

	/** Name of the HTTP Header specifying the content types to be accepted. */
	protected static final String HTTP_ACCEPT_HEADER = "Accept";

	/** The content types the library will accept. */
	protected static final String CONTENT_TYPES_TO_ACCEPT = "application/json, application/octet-stream";

	/** The Client ID created by freesound.org for the application. */
	private final String clientId;

	/** The Client Secret/API Key generated by freesound.org for the application. */
	private final String clientSecret;

	/**
	 * @param clientId Client ID for application
	 * @param clientSecret Client Secret (API Key) for application
	 */
	public FreesoundClient(final String clientId, final String clientSecret) {
		this(clientId, clientSecret, null);
	}

	/**
	 * @param clientId Client ID for application
	 * @param clientSecret Client Secret (API Key) for application
	 * @param userAgentString The User-Agent string to send with all requests
	 */
	public FreesoundClient(final String clientId, final String clientSecret, final String userAgentString) {
		this.clientId = clientId;
		this.clientSecret = clientSecret;

		Unirest.setDefaultHeader(HTTP_ACCEPT_HEADER, CONTENT_TYPES_TO_ACCEPT);

		if (userAgentString != null) {
			Unirest.setDefaultHeader(HTTP_USER_AGENT_HEADER, userAgentString);
		} else {
			Unirest.setDefaultHeader(HTTP_USER_AGENT_HEADER, DEFAULT_USER_AGENT_STRING);
		}
	}

	/**
	 * Execute a given query (synchronously) against the freesound API.
	 *
	 * @param query The query to execute
	 *
	 * @throws FreesoundClientException Any errors encountered when performing API call
	 */
	public void executeQuery(final Query<?, ?> query) throws FreesoundClientException {
		final HttpRequest request = buildHTTPRequest(query);
		final String credential = buildAuthorisationCredential(query);

		if (credential != null) {
			request.header("Authorization", credential);
		}

		if (query instanceof JSONResponseQuery) {
			executeQuery(request, (JSONResponseQuery<?>) query);
		} else if (query instanceof BinaryResponseQuery) {
			executeQuery(request, (BinaryResponseQuery) query);
		} else {
			throw new FreesoundClientException(String.format("Unknown request type: %s", query.getClass()));
		}
	}

	/**
	 * Execute a query that we expect to return a JSON object as a response.
	 *
	 * @param request The HTTP request to send
	 * @param jsonResponseQuery The query the request has been constructed from
	 *
	 * @throws FreesoundClientException Any errors encountered
	 */
	private void executeQuery(
			final HttpRequest request, final JSONResponseQuery<?> jsonResponseQuery) throws FreesoundClientException {
		try {
			final HttpResponse<JsonNode> jsonResponse = request.asJson();
			jsonResponseQuery.setResponse(jsonResponse.getCode(), jsonResponse.getBody().getObject());
		} catch (final UnirestException e) {
			throw new FreesoundClientException("Error when attempting to make API call", e);
		}
	}

	/**
	 * Execute a query that we expect to return a binary payload.
	 *
	 * @param request The HTTP request to send
	 * @param binaryResponseQuery The query the request has been constructed from
	 *
	 * @throws FreesoundClientException Any errors encountered
	 */
	private void executeQuery(
			final HttpRequest request, final BinaryResponseQuery binaryResponseQuery)
					throws FreesoundClientException {
		try {
			final HttpResponse<InputStream> binaryResponse = request.asBinary();
			binaryResponseQuery.setResponse(binaryResponse.getCode(), binaryResponse.getBody());
		} catch (final UnirestException e) {
			throw new FreesoundClientException("Error when attempting to make API call", e);
		}
	}

	/**
	 * Build the Unirest {@link HttpRequest} that will be used to make the call to the API.
	 *
	 * @param query The query to be made
	 * @return Properly configured {@link HttpRequest} representing query
	 */
	private HttpRequest buildHTTPRequest(final Query<?, ?> query) {
		final String url = API_ENDPOINT + query.getPath();

		HttpRequest request;
		switch (query.getHttpRequestMethod()) {
			case GET:
				request = Unirest.get(url);

				if ((query.getQueryParameters() != null) && !query.getQueryParameters().isEmpty()) {
					((GetRequest) request).fields(query.getQueryParameters());
				}

				break;

			case POST:
				request = Unirest.post(url);

				if ((query.getQueryParameters() != null) && !query.getQueryParameters().isEmpty()) {
					((HttpRequestWithBody) request).fields(query.getQueryParameters());
				}

				break;

			default:
				request = Unirest.get(url);
		}

		/*
		 * Add any named route parameters to the request (i.e. elements used to build the URI, such as
		 * '/sound/{sound_id}' would have a parameter named 'sound_id').
		 */
		if ((query.getRouteParameters() != null) && !query.getRouteParameters().isEmpty()) {
			for (final Entry<String, String> routeParameter : query.getRouteParameters().entrySet()) {
				request.routeParam(routeParameter.getKey(), routeParameter.getValue());
			}
		}

		return request;
	}

	/**
	 * Build the credential that will be passed in the 'Authorization' HTTP header as part of the API call. The nature
	 * of the credential will depend on the query being made.
	 *
	 * @param query The query being made
	 * @return The string to pass in the Authorization header (or null if none)
	 */
	private String buildAuthorisationCredential(final Query<?, ?> query) {
		String credential = null;
		if (query instanceof OAuthQuery) {
			final String oauthToken = ((OAuthQuery) query).getOauthToken();
			credential = String.format("Bearer %s", oauthToken);
		} else if (query instanceof AccessTokenQuery) {
			// Don't set the Authorization header
		} else {
			credential = String.format("Token %s", clientSecret);
		}

		return credential;
	}

	/**
	 * Retrieve the next page of results for a {@link PagingQuery}.
	 *
	 * @param query The {@link PagingQuery} being run
	 * @throws FreesoundClientException If it is not possible to retrieve the next page
	 */
	public void nextPage(final PagingQuery<?, ?> query) throws FreesoundClientException {
		if (query.hasNextPage()) {
			final int currentPage = query.getPage();
			query.setPage(currentPage + 1);

			executeQuery(query);
		} else {
			throw new FreesoundClientException("No more pages of results");
		}
	}

	/**
	 * Retrieve the previous page of results for a {@link PagingQuery}.
	 *
	 * @param query The {@link PagingQuery} being run
	 * @throws FreesoundClientException If it is not possible to retrieve the previous page
	 */
	public void previousPage(final PagingQuery<?, ?> query) throws FreesoundClientException {
		if (query.hasPreviousPage()) {
			final int currentPage = query.getPage();
			query.setPage(currentPage - 1);

			executeQuery(query);
		} else {
			throw new FreesoundClientException("At first page of results");
		}
	}

	/**
	 * Redeem an authorisation code received from freesound.org for an access token that can be used to make calls to
	 * OAuth2 protected resources.
	 *
	 * @param authorisationCode The authorisation code received
	 * @return Details of the access token returned
	 *
	 * @throws FreesoundClientException Any exception thrown during call
	 */
	public AccessTokenDetails redeemAuthorisationCodeForAccessToken(final String authorisationCode)
			throws FreesoundClientException {
		final OAuth2AccessTokenRequest tokenRequest =
				new OAuth2AccessTokenRequest(clientId, clientSecret, authorisationCode);

		executeQuery(tokenRequest);

		return tokenRequest.getResults();
	}

	/**
	 * Retrieve a new OAuth2 access token using a refresh token.
	 *
	 * @param refreshToken The refresh token to present
	 * @return Details of the access token returned
	 * @throws FreesoundClientException Any exception thrown during call
	 */
	public AccessTokenDetails refreshAccessToken(final String refreshToken) throws FreesoundClientException {
		final RefreshOAuth2AccessTokenRequest tokenRequest =
				new RefreshOAuth2AccessTokenRequest(clientId, clientSecret, refreshToken);

		executeQuery(tokenRequest);

		return tokenRequest.getResults();
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
