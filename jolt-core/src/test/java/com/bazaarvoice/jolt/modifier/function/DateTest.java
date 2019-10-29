/*
 * Copyright 2013 Bazaarvoice, Inc.
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

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.testng.annotations.DataProvider;

import com.bazaarvoice.jolt.common.Optional;

@SuppressWarnings("deprecation")
public class DateTest extends AbstractTester {

	@DataProvider(parallel = true)
	public Iterator<Object[]> getTestCases() {
		List<Object[]> testCases = new LinkedList<>(  );
		
		Function TO_DATE = new Objects.toDate();
		
		testCases.add( new Object[] {"null-inputs", TO_DATE, null, Optional.empty() } );
		testCases.add( new Object[] {"empty-inputs", TO_DATE, Collections.emptyList(), Optional.empty() } );
		testCases.add( new Object[] {"missing-format-input", TO_DATE, Arrays.asList(1572353915824L), Optional.empty() } );
		testCases.add( new Object[] {"extra-inputs", TO_DATE, Arrays.asList(1572353915824L,"yyyy-MM-dd HH:mm:ss.SSS", "UTC", 1000), Optional.empty() } );
		testCases.add( new Object[] {"invalid-first-type", TO_DATE, Arrays.asList("yyyy-MM-dd HH:mm:ss.SSS","yyyy-MM-dd HH:mm:ss.SSS"), Optional.empty() } );
		testCases.add( new Object[] {"invalid-second-type", TO_DATE, Arrays.asList(1572353915824L,1572353915824L), Optional.empty() } );
		testCases.add( new Object[] {"invalid-second-format", TO_DATE, Arrays.asList(1572353915824L,"yyyy-yyyy-aa bb-cc-dd"), Optional.empty() } );
		testCases.add( new Object[] {"invalid-third-format", TO_DATE, Arrays.asList(1572353915824L,"yyyy-MM-dd HH:mm:ss.SSS", 100), Optional.empty() } );
		testCases.add( new Object[] {"invalid-third-format", TO_DATE, Arrays.asList(1572353915824L,"yyyy-MM-dd HH:mm:ss.SSS", "U T C"), Optional.empty() } );
		testCases.add( new Object[] {"invalid-first-int", TO_DATE, Arrays.asList(157235,"yyyy-MM-dd HH:mm:ss.SSS"), Optional.of("1970-01-01 00:02:37.235") } );
		testCases.add( new Object[] {"valid-without-timezone", TO_DATE, Arrays.asList(1572353915824L,"yyyy-MM-dd HH:mm:ss.SSS"), Optional.of("2019-10-29 12:58:35.824") } );	
		testCases.add( new Object[] {"valid-with-timezone-Z", TO_DATE, Arrays.asList(1572353915824L,"yyyy-MM-dd HH:mm:ss.SSS", "Z"), Optional.of("2019-10-29 12:58:35.824") } );	
		testCases.add( new Object[] {"valid-with-timezone-UTC", TO_DATE, Arrays.asList(1572353915824L,"yyyy-MM-dd HH:mm:ss.SSS", "UTC"), Optional.of("2019-10-29 12:58:35.824") } );
		testCases.add( new Object[] {"valid-with-timezone-GMT", TO_DATE, Arrays.asList(1572353915824L,"yyyy-MM-dd HH:mm:ss.SSS", "GMT"), Optional.of("2019-10-29 12:58:35.824") } );
		testCases.add( new Object[] {"valid-with-timezone-GMT+1", TO_DATE, Arrays.asList(1572353915824L,"yyyy-MM-dd HH:mm:ss.SSS", "GMT+1"), Optional.of("2019-10-29 13:58:35.824") } );
		testCases.add( new Object[] {"valid-with-timezone-GMT-10", TO_DATE, Arrays.asList(1572353915824L,"yyyy-MM-dd HH:mm:ss.SSS", "GMT-10"), Optional.of("2019-10-29 02:58:35.824") } );
		testCases.add( new Object[] {"valid-with-timezone-GMT+03:30", TO_DATE, Arrays.asList(1572353915824L,"yyyy-MM-dd HH:mm:ss.SSS", "GMT+03:30"), Optional.of("2019-10-29 16:28:35.824") } );
		testCases.add( new Object[] {"valid-with-timezone-UTC+02:00", TO_DATE, Arrays.asList(1572353915824L,"yyyy-MM-dd HH:mm:ss.SSS", "UTC+02:00"), Optional.of("2019-10-29 14:58:35.824") } );
		testCases.add( new Object[] {"valid-with-timezone-UTC+02:00", TO_DATE, Arrays.asList(1572353915824L,"yyyy-MM-dd HH:mm:ss.SSS", "UTC-02:00"), Optional.of("2019-10-29 10:58:35.824") } );
		testCases.add( new Object[] {"valid-with-timezone-+04", TO_DATE, Arrays.asList(1572353915824L,"yyyy-MM-dd HH:mm:ss.SSS", "+04"), Optional.of("2019-10-29 16:58:35.824") } );
		testCases.add( new Object[] {"valid-with-timezone-+05:00", TO_DATE, Arrays.asList(1572353915824L,"yyyy-MM-dd HH:mm:ss.SSS", "+05:00"), Optional.of("2019-10-29 17:58:35.824") } );
		testCases.add( new Object[] {"valid-with-timezone--05:00", TO_DATE, Arrays.asList(1572353915824L,"yyyy-MM-dd HH:mm:ss.SSS", "-05:00"), Optional.of("2019-10-29 07:58:35.824") } );
		
		return testCases.iterator();
	}

}
