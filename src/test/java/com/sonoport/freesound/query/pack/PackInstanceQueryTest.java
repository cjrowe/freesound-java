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

/**
 * Ensure that {@link PackInstanceQuery} objects are created and operate correctly.
 */
public class PackInstanceQueryTest {

	/** Pack identifier to use in tests. */
	private static final int PACK_ID = 1234;

	/**
	 * Test to ensure that {@link PackInstanceQuery} objects are correctly created.
	 */
	@Test
	public void checkPackInstanceQueryCreatedCorrectly() {
		final PackInstanceQuery query = new PackInstanceQuery(PACK_ID);

		assertTrue(query.getRouteParameters().size() == 1);
		assertEquals(
				String.valueOf(PACK_ID), query.getRouteParameters().get(PackInstanceQuery.PACK_IDENTIFIER_PARAMETER));

		assertTrue(query.getQueryParameters().isEmpty());
	}
}
