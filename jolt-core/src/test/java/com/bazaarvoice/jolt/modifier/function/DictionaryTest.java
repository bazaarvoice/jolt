/*
 * Copyright 2019 Infovista.
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

package com.bazaarvoice.jolt.modifier.function;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.testng.annotations.DataProvider;

import com.bazaarvoice.jolt.common.Optional;
import com.bazaarvoice.jolt.dictionary.Dictionaries;

@SuppressWarnings("deprecated")
public class DictionaryTest extends AbstractTester {

	@DataProvider(parallel = true)
	public Iterator<Object[]> getTestCases() {

		List<Object[]> testCases = new LinkedList<>();

		Function convert = new Dictionary.convert();
		// declare some fake dictionaries to use them
		Map<String, String> dict = new HashMap();
		dict.put("A", "1");
		dict.put("B", "2");
		dict.put("C", "3");
		Dictionaries.setDictionary("test", dict);
		testCases.add(new Object[] { "convert-simple1", convert, new Object[] { "test", "A" }, Optional.of("1") });
		testCases.add(new Object[] { "convert-simple2", convert, new Object[] { "test", "B" }, Optional.of("2") });
		testCases.add(new Object[] { "convert-simple3", convert, new Object[] { "test", "C" }, Optional.of("3") });
		testCases.add(new Object[] { "convert-novalue", convert, new Object[] { "test", "D" }, Optional.empty() });
		testCases
				.add(new Object[] { "convert-nodictionary", convert, new Object[] { "dummy", "A" }, Optional.empty() });

		return testCases.iterator();
	}
}