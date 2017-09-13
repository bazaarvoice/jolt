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

@SuppressWarnings( "deprecated" )
public class ListsTest extends AbstractTester {

    @DataProvider(parallel = true)
    public Iterator<Object[]> getTestCases() {
        List<Object[]> testCases = new LinkedList<>(  );

        Function FIRST_ELEMENT = new Lists.firstElement();
        Function LAST_ELEMENT = new Lists.lastElement();
        Function ELEMENT_AT = new Lists.elementAt();

        Function SIZE = new Objects.size();

        testCases.add( new Object[] {"first-empty-array", FIRST_ELEMENT, new Object[0], Optional.empty() } );
        testCases.add( new Object[] {"first-empty-list", FIRST_ELEMENT, Arrays.asList(  ), Optional.empty() } );

        testCases.add( new Object[] {"first-null", FIRST_ELEMENT, null, Optional.empty() } );
        testCases.add( new Object[] {"first-array", FIRST_ELEMENT, new Object[]{ 1, 2, 3 }, Optional.of( 1 ) } );
        testCases.add( new Object[] {"first-list", FIRST_ELEMENT, Arrays.asList( 1, 2, 3 ), Optional.of( 1 ) } );



        testCases.add( new Object[] {"last-empty-array", LAST_ELEMENT, new Object[0], Optional.empty() } );
        testCases.add( new Object[] {"last-empty-list", LAST_ELEMENT, Arrays.asList(  ), Optional.empty() } );

        testCases.add( new Object[] {"last-null", LAST_ELEMENT, null, Optional.empty() } );
        testCases.add( new Object[] {"last-array", LAST_ELEMENT, new Object[]{ 1, 2, 3 }, Optional.of( 3 ) } );
        testCases.add( new Object[] {"last-list", LAST_ELEMENT, Arrays.asList( 1, 2, 3 ), Optional.of( 3 ) } );



        testCases.add( new Object[] {"at-empty-array", ELEMENT_AT, new Object[] {5}, Optional.empty() } );
        testCases.add( new Object[] {"at-empty-list", ELEMENT_AT, Arrays.asList( 5 ), Optional.empty() } );
        testCases.add( new Object[] {"at-empty-null", ELEMENT_AT, new Object[] {null, 1}, Optional.empty() } );
        testCases.add( new Object[] {"at-empty-invalid", ELEMENT_AT, new Object(), Optional.empty() } );

        testCases.add( new Object[] {"at-array", ELEMENT_AT, new Object[]{ 1, 2, 3, 1 }, Optional.of( 3 ) } );
        testCases.add( new Object[] {"at-list", ELEMENT_AT, Arrays.asList( 1, 2, 3, 1 ), Optional.of( 3 ) } );

        testCases.add( new Object[] {"at-array-missing", ELEMENT_AT, new Object[]{ 5, 1, 2, 3 }, Optional.empty() } );
        testCases.add( new Object[] {"at-list-missing", ELEMENT_AT, Arrays.asList( 5, 1, 2, 3 ), Optional.empty() } );


        testCases.add( new Object[] {"size-list", SIZE, new Object[]{ 5, 1, 2, 3 }, Optional.of(4) } );
        testCases.add( new Object[] {"size-list-empty", SIZE, Arrays.asList( ), Optional.of(0) } );

        return testCases.iterator();
    }
}
