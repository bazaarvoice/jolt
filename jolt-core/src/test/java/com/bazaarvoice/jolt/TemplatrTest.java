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

package com.bazaarvoice.jolt;

import com.bazaarvoice.jolt.exception.SpecException;
import com.google.common.collect.Lists;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class TemplatrTest {

    enum TemplatrTestCase {
        OVERWRITE {
            @Override
            Templatr getTemplatr( final Object spec ) {
                return new Templatr.Overwritr( spec );
            }
        },
        DEFAULTR {
            @Override
            Templatr getTemplatr( final Object spec ) {
                return new Templatr.Defaultr(spec);
            }
        },
        DEFINR {
            @Override
            Templatr getTemplatr( final Object spec ) {
                return new Templatr.Definr( spec );
            }
        };

        abstract Templatr getTemplatr(Object spec);
    }

    @DataProvider
    public Iterator<Object[]> getTestCases() {
        List<Object[]> testCases = Lists.newLinkedList();

        testCases.add( new Object[]{"/json/templatr/mapLiteral.json"} );
        testCases.add( new Object[]{"/json/templatr/mapLiteralWithNullInput.json"} );
        testCases.add( new Object[]{"/json/templatr/mapLiteralWithMissingInput.json"} );
        testCases.add( new Object[]{"/json/templatr/mapLiteralWithEmptyInput.json"} );
        testCases.add( new Object[]{"/json/templatr/arrayLiteralWithNullInput.json"} );
        testCases.add( new Object[]{"/json/templatr/arrayLiteral.json"} );
        testCases.add( new Object[]{"/json/templatr/arrayLiteralWithEmptyInput.json"} );
        testCases.add( new Object[]{"/json/templatr/arrayLiteralWithMissingInput.json"} );
        testCases.add( new Object[]{"/json/templatr/arrayObject.json"} );
        testCases.add( new Object[]{"/json/templatr/simple.json"} );
        testCases.add( new Object[]{"/json/templatr/simpleMapNullToArray.json"} );
        testCases.add( new Object[]{"/json/templatr/simpleMapRuntimeNull.json"} );
        testCases.add( new Object[]{"/json/templatr/simpleLookup.json"} );
        testCases.add( new Object[]{"/json/templatr/complexLookup.json"} );
        testCases.add( new Object[]{"/json/templatr/simpleArray.json"} );
        testCases.add( new Object[]{"/json/templatr/simpleArrayLookup.json"} );
        testCases.add( new Object[]{"/json/templatr/complexArrayLookup.json"} );

        return testCases.iterator();
    }

    @Test (dataProvider = "getTestCases")
    public void testOverwritrTransform(String testFile) throws Exception {
        doTest( testFile, TemplatrTestCase.OVERWRITE );
    }

    @Test (dataProvider = "getTestCases")
    public void testDefaultrTransform(String testFile) throws Exception {
        doTest( testFile, TemplatrTestCase.DEFAULTR );
    }

    @Test (dataProvider = "getTestCases")
    public void testDefinrTransform(String testFile) throws Exception {
        doTest( testFile, TemplatrTestCase.DEFINR );
    }

    public void doTest(String testFile, TemplatrTestCase testCase) throws Exception {
        Map<String, Object> testUnit = JsonUtils.classpathToMap( testFile );
        Object input = testUnit.get( "input" );
        Object spec = testUnit.get( "spec" );
        Object context = testUnit.get( "context" );
        Object expected = testUnit.get( testCase.name() );
        if(expected != null) {
            Templatr templatr = testCase.getTemplatr( spec );
            Object actual = templatr.transform( input, (Map<String, Object>) context );
            JoltTestUtil.runArrayOrderObliviousDiffy( testCase.name() + " failed case " + testFile, expected, actual );
        }
    }

    @DataProvider
    public Iterator<Object[]> getSpecValidationTestCases() {
        List<Object[]> testCases = Lists.newLinkedList();
        List<Object> testObjects = JsonUtils.classpathToList( "/json/templatr/validation/specThatShouldFail.json" );

        for(TemplatrTestCase testCase: TemplatrTestCase.values()) {
            for(Object specObj: testObjects) {
                testCases.add( new Object[] {testCase, specObj} );
            }
        }

        return testCases.iterator();
    }

    @Test(expectedExceptions = SpecException.class, dataProvider = "getSpecValidationTestCases")
    public void testInvalidSpecs(TemplatrTestCase testCase, Object spec) {
        testCase.getTemplatr( spec );
    }
}
