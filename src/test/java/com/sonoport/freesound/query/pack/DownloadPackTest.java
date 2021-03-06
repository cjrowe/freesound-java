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
package com.sonoport.freesound.query.pack;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.sonoport.freesound.query.BinaryResponseQueryTest;

/**
 * Unit tests to ensure the correct operation of {@link DownloadPack}.
 */
public class DownloadPackTest extends BinaryResponseQueryTest<DownloadPack> {

	/** Pack Id to use in tests. */
	private static final int PACK_ID = 1234;

	/** OAuth2 token to use in tests. */
	private static final String OAUTH_TOKEN = "abc123def";

	/**
	 * Ensure that instances of {@link DownloadPack} are constructed correctly.
	 */
	@Test
	public void checkDownloadQueryConstructedCorrectly() {
		final DownloadPack downloadPack = newQueryInstance();

		assertTrue(downloadPack.getRouteParameters().size() == 1);
		assertEquals(
				String.valueOf(PACK_ID), downloadPack.getRouteParameters().get(DownloadPack.PACK_ID_ROUTE_PARAMETER));

		assertEquals(OAUTH_TOKEN, downloadPack.getOauthToken());
	}

	@Override
	protected DownloadPack newQueryInstance() {
		return new DownloadPack(PACK_ID, OAUTH_TOKEN);
	}

}
