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
package com.sonoport.freesound.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.Map;

import mockit.Deencapsulation;

import org.junit.Test;

/**
 * Unit tests to ensure the correct operation of {@link TextSearch}.
 */
public class TextSearchTest {

	/** Search string to use in tests. */
	private static final String SEARCH_STRING = "cars";

	/** Sort order to use in tests. */
	private static final SortOrder SORT_ORDER = SortOrder.CREATED_ASCENDING;

	/** 'Group by Pack' value to use in tests. */
	private static final boolean GROUP_BY_PACK = true;

	/**
	 * Test that the constructor taking the search string as a parameter correctly populates the member variable.
	 */
	@Test
	public void searchStringConstrcutor() {
		final TextSearch query = new TextSearch(SEARCH_STRING);

		assertEquals(SEARCH_STRING, Deencapsulation.getField(query, "searchString"));
	}

	/**
	 * Test that setting the search string using the Fluent API correctly populates the member variable.
	 */
	@Test
	public void searchStringFluentAPI() {
		final TextSearch originalQuery = new TextSearch();
		final TextSearch updatedQuery = originalQuery.searchString(SEARCH_STRING);

		assertSame(originalQuery, updatedQuery);
		assertEquals(SEARCH_STRING, Deencapsulation.getField(originalQuery, "searchString"));
	}

	/**
	 * Test that setting the sort order using the Fluent API correctly populates the member variable.
	 */
	@Test
	public void sortOrderFluentAPI() {
		final TextSearch originalQuery = new TextSearch();
		final TextSearch updatedQuery = originalQuery.sortOrder(SORT_ORDER);

		assertSame(originalQuery, updatedQuery);
		assertEquals(SORT_ORDER, Deencapsulation.getField(originalQuery, "sortOrder"));
	}

	/**
	 * Test that setting 'group by pack' using the Fluent API correctly populates the member variable.
	 */
	@Test
	public void groupByPackFluentAPI() {
		final TextSearch originalQuery = new TextSearch();
		final TextSearch updatedQuery = originalQuery.groupByPack(GROUP_BY_PACK);

		assertSame(originalQuery, updatedQuery);
		assertEquals(GROUP_BY_PACK, Deencapsulation.getField(originalQuery, "groupByPack"));
	}

	/**
	 * Test that the query parameter names and values are correctly provided when requested.
	 */
	@Test
	public void correctQueryParametersCreated() {
		final TextSearch query = new TextSearch(SEARCH_STRING).sortOrder(SORT_ORDER).groupByPack(GROUP_BY_PACK);
		final Map<String, Object> queryParameters = query.getQueryParameters();

		assertEquals(SEARCH_STRING, queryParameters.get("query"));
		assertEquals(SORT_ORDER.getParameterValue(), queryParameters.get("sort"));

		final String groupByValue = GROUP_BY_PACK ? "1" : "0";
		assertEquals(groupByValue, queryParameters.get("group_by_pack"));
	}
}
