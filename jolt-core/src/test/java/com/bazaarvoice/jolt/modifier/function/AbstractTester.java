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
import org.testng.annotations.Test;

import java.util.Iterator;
import java.util.List;

import static org.testng.Assert.assertEquals;

@SuppressWarnings( "deprecated" )
public abstract class AbstractTester {

    @SuppressWarnings( "unused" )
    public abstract Iterator<Object[]> getTestCases();

    @Test(dataProvider = "getTestCases")
    public void testFunctions(String name, Function function, Object args, Optional<Object> expected) {
        Optional<Object> actual;
        if(args instanceof List) {
            actual = function.apply( (List) args );
        }
        else if (args instanceof Object[]){
            actual = function.apply( (Object[]) args );
        }
        else {
            actual = function.apply( args );
        }
        assertEquals( actual.isPresent(), expected.isPresent(), "actual and expected should both be present or not" );
        if ( actual.isPresent() ) {
            assertEquals( actual.get(), expected.get(), name + " failed");
        }
    }
}
