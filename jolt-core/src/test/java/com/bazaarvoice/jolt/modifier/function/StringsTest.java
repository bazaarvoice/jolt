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

import com.bazaarvoice.jolt.common.Optional;
import org.testng.annotations.DataProvider;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("deprecated")
public class StringsTest extends AbstractTester {

  @DataProvider(parallel = true)
  public Iterator<Object[]> getTestCases() {
    List<Object[]> testCases = new LinkedList<>(  );

    Function SPLIT = new Strings.split();

    testCases.add( new Object[] {"split-invalid-null", SPLIT, null, Optional.empty() } );
    testCases.add( new Object[] {"split-invalid-string", SPLIT, "", Optional.empty() } );

    testCases.add( new Object[] {"split-null-string", SPLIT, new Object[] {",", null}, Optional.empty() } );
    testCases.add( new Object[] {"split-null-separator", SPLIT, new Object[] {null, "test"}, Optional.empty() } );

    testCases.add( new Object[] {"split-empty-string", SPLIT, new Object[] {",", ""}, Optional.of( Arrays.asList("") ) } );
    testCases.add( new Object[] {"split-single-token-string", SPLIT, new Object[] {",", "test"}, Optional.of( Arrays.asList("test") )} );

    testCases.add( new Object[] {"split-double-token-string", SPLIT, new Object[] {",", "test,TEST"}, Optional.of( Arrays.asList("test", "TEST") )} );
    testCases.add( new Object[] {"split-multi-token-string", SPLIT, new Object[] {",", "test,TEST,Test,TeSt"}, Optional.of( Arrays.asList("test", "TEST", "Test", "TeSt") )} );
    testCases.add( new Object[] {"split-spaced-token-string", SPLIT, new Object[] {",", "test, TEST"}, Optional.of( Arrays.asList("test", " TEST") )} );
    testCases.add( new Object[] {"split-long-separator-spaced-token-string", SPLIT, new Object[] {", ", "test, TEST"}, Optional.of( Arrays.asList("test", "TEST") )} );

    testCases.add( new Object[] {"split-regex-token-string", SPLIT, new Object[] {"[eE]", "test,TEST"}, Optional.of( Arrays.asList("t", "st,T", "ST") )} );
    testCases.add( new Object[] {"split-regex2-token-string", SPLIT, new Object[] {"\\s+", "test TEST  Test    TeSt"}, Optional.of( Arrays.asList("test", "TEST", "Test", "TeSt") )} );

    return testCases.iterator();
  }
}
