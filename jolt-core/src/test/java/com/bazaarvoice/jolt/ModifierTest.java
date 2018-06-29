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

import com.bazaarvoice.jolt.common.Optional;
import com.bazaarvoice.jolt.common.SpecStringParser;
import com.bazaarvoice.jolt.exception.SpecException;
import com.bazaarvoice.jolt.modifier.function.Function;
import com.google.common.collect.Lists;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings( "deprecated" )
public class ModifierTest {

    enum TemplatrTestCase {
        OVERWRITR {
            @Override
            Modifier getTemplatr( final Object spec ) {
                return new Modifier.Overwritr( spec );
            }
        },
        DEFAULTR {
            @Override
            Modifier getTemplatr( final Object spec ) {
                return new Modifier.Defaultr(spec);
            }
        },
        DEFINR {
            @Override
            Modifier getTemplatr( final Object spec ) {
                return new Modifier.Definr( spec );
            }
        };

        abstract Modifier getTemplatr(Object spec);
    }

    @BeforeClass
    @SuppressWarnings( "unchecked" )
    public void setup() throws Exception {
        // accessing built ins such that we can test a custom impl of function
        // this is a special test case, and not a recommended approach of using function
        Field f = Modifier.class.getDeclaredField("STOCK_FUNCTIONS");
        f.setAccessible( true );
        Map<String, Function> BUILT_INS  = (Map<String, Function>) f.get( null );
        BUILT_INS.put( "minLabelComputation", new MinLabelComputation() );
        BUILT_INS.put( "maxLabelComputation", new MaxLabelComputation() );
    }

    @DataProvider
    public Iterator<Object[]> getTestCases() {
        List<Object[]> testCases = Lists.newLinkedList();

        testCases.add( new Object[]{"/json/modifier/mapLiteral.json"} );
        testCases.add( new Object[]{"/json/modifier/mapLiteralWithNullInput.json"} );
        testCases.add( new Object[]{"/json/modifier/mapLiteralWithMissingInput.json"} );
        testCases.add( new Object[]{"/json/modifier/mapLiteralWithEmptyInput.json"} );

        testCases.add( new Object[]{"/json/modifier/arrayElementAt.json"} );

        testCases.add( new Object[]{"/json/modifier/arrayLiteral.json"} );
        testCases.add( new Object[]{"/json/modifier/arrayLiteralWithNullInput.json"} );
        testCases.add( new Object[]{"/json/modifier/arrayLiteralWithEmptyInput.json"} );
        testCases.add( new Object[]{"/json/modifier/arrayLiteralWithMissingInput.json"} );

        testCases.add( new Object[]{"/json/modifier/simple.json"} );
        testCases.add( new Object[]{"/json/modifier/simpleArray.json"} );
        testCases.add( new Object[]{"/json/modifier/arrayObject.json"} );

        testCases.add( new Object[]{"/json/modifier/simpleMapNullToArray.json"} );
        testCases.add( new Object[]{"/json/modifier/simpleMapRuntimeNull.json"} );

        testCases.add( new Object[]{"/json/modifier/simpleLookup.json"} );
        testCases.add( new Object[]{"/json/modifier/complexLookup.json"} );

        testCases.add( new Object[]{"/json/modifier/simpleArrayLookup.json"} );
        testCases.add( new Object[]{"/json/modifier/complexArrayLookup.json"} );

        testCases.add( new Object[]{"/json/modifier/valueCheckSimpleArray.json"} );
        testCases.add( new Object[]{"/json/modifier/valueCheckSimpleArrayNullInput.json"} );
        testCases.add( new Object[]{"/json/modifier/valueCheckSimpleArrayEmptyInput.json"} );

        testCases.add( new Object[]{"/json/modifier/valueCheckSimpleMap.json"} );
        testCases.add( new Object[]{"/json/modifier/valueCheckSimpleMapNullInput.json"} );
        testCases.add( new Object[]{"/json/modifier/valueCheckSimpleMapEmptyInput.json"} );

        testCases.add( new Object[]{"/json/modifier/simpleMapOpOverride.json"} );
        testCases.add( new Object[]{"/json/modifier/simpleArrayOpOverride.json"} );

        testCases.add( new Object[]{"/json/modifier/testListOfFunction.json"} );

        return testCases.iterator();
    }

    @Test (dataProvider = "getTestCases")
    public void testOverwritrTransform(String testFile) throws Exception {
        doTest( testFile, TemplatrTestCase.OVERWRITR );
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
            Modifier modifier = testCase.getTemplatr( spec );
            Object actual = modifier.transform( input, (Map<String, Object>) context );
            JoltTestUtil.runArrayOrderObliviousDiffy( testCase.name() + " failed case " + testFile, expected, actual );
        }
    }

    @DataProvider
    public Iterator<Object[]> getSpecValidationTestCases() {
        List<Object[]> testCases = Lists.newLinkedList();
        List<Object> testObjects = JsonUtils.classpathToList( "/json/modifier/validation/specThatShouldFail.json" );

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

    @DataProvider
    public Iterator<Object[]> getFunctionTests() {
        List<Object[]> testCases = Lists.newLinkedList();

        testCases.add( new Object[]{"/json/modifier/functions/stringsSplitTest.json", TemplatrTestCase.OVERWRITR});
        testCases.add( new Object[]{"/json/modifier/functions/padStringsTest.json", TemplatrTestCase.OVERWRITR});
        testCases.add( new Object[]{"/json/modifier/functions/stringsTests.json", TemplatrTestCase.OVERWRITR});
        testCases.add( new Object[]{"/json/modifier/functions/mathTests.json", TemplatrTestCase.OVERWRITR} );
        testCases.add( new Object[]{"/json/modifier/functions/arrayTests.json", TemplatrTestCase.OVERWRITR} );
        testCases.add( new Object[]{"/json/modifier/functions/sizeTests.json", TemplatrTestCase.OVERWRITR} );
        testCases.add( new Object[]{"/json/modifier/functions/labelsLookupTest.json", TemplatrTestCase.DEFAULTR} );
        testCases.add( new Object[]{"/json/modifier/functions/valueTests.json", TemplatrTestCase.OVERWRITR }  );
        testCases.add( new Object[]{"/json/modifier/functions/squashNullsTests.json", TemplatrTestCase.OVERWRITR }  );

        return testCases.iterator();
    }


    @Test (dataProvider = "getFunctionTests")
    public void testFunctions(String testFile, TemplatrTestCase testCase) throws Exception {
        doTest( testFile, testCase);
    }

    @DataProvider
    public Iterator<Object[]> fnArgParseTestCases(){
        List<Object[]> testCases = Lists.newLinkedList();

        testCases.add( new Object[] {"fn(abc,efg,pqr)", new String[] {"fn", "abc", "efg", "pqr"} } );
        testCases.add( new Object[] {"fn(abc,@(1,2),pqr)", new String[] {"fn", "abc", "@(1,2)", "pqr"} } );
        testCases.add( new Object[] {"fn(abc,efg,pqr,)", new String[] {"fn", "abc", "efg", "pqr", ""} } );
        testCases.add( new Object[] {"fn(abc,,@(1,,2),,pqr,,)", new String[] {"fn", "abc", "","@(1,,2)","", "pqr", "", ""} } );
        testCases.add( new Object[] {"fn(abc,'e,f,g',pqr)", new String[] {"fn", "abc", "'e,f,g'", "pqr"} } );
        testCases.add( new Object[] {"fn(abc,'e(,f,)g',pqr)", new String[] {"fn", "abc", "'e(,f,)g'", "pqr"} } );

        return testCases.iterator();
    }

    @Test( dataProvider = "fnArgParseTestCases")
    public void testFunctionArgParse(String argString, String[] expected) throws Exception {
        List<String> actual = SpecStringParser.parseFunctionArgs( argString );
        JoltTestUtil.runArrayOrderObliviousDiffy(" failed case " + argString, expected, actual );
    }

    @Test
    public void testModifierFirstElementArray() throws IOException {
        Map<String, Object> input = new HashMap<String, Object>() {{
            put("input", new Integer[]{5, 4});
        }};

        Map<String, Object> spec = new HashMap<String, Object>() {{
            put("first", "=firstElement(@(1,input))");
        }};

        Map<String, Object> expected = new HashMap<String, Object>() {{
            put("input", new Integer[]{5, 4});
            put("first", 5);
        }};

        Modifier modifier = new Modifier.Overwritr( spec );
        Object actual = modifier.transform( input, null );
        JoltTestUtil.runArrayOrderObliviousDiffy( "failed modifierFirstElementArray", expected, actual );
    }

    @SuppressWarnings( "unused" )
    public static final class MinLabelComputation implements Function {
        @Override
        @SuppressWarnings( "unchecked" )
        public Optional<Object> apply( final Object... args ) {
            Map<String, String> valueLabels = (Map<String, String>) args[0];
            Integer min = Integer.MAX_VALUE;
            Set<String> valueLabelKeys = valueLabels.keySet();
            for (String labelKey: valueLabelKeys ) {
                Integer val = null;
                try {
                    val = Integer.parseInt( labelKey );
                }
                catch(Exception ignored) {}
                if(val != null) {
                    min = Math.min( val, min );
                }
            }
            return Optional.<Object>of( valueLabels.get( min.toString() ) );
        }
    }

    @SuppressWarnings( "unused" )
    public static final class MaxLabelComputation implements Function {
        @Override
        @SuppressWarnings( "unchecked" )
        public Optional<Object> apply( final Object... args ) {
            Map<String, String> valueLabels = (Map<String, String>) args[0];
            Integer max = Integer.MIN_VALUE;
            Set<String> valueLabelKeys = valueLabels.keySet();
            for (String labelKey: valueLabelKeys ) {
                Integer val = null;
                try {
                    val = Integer.parseInt( labelKey );
                }
                catch(Exception ignored) {}
                if(val != null) {
                    max = Math.max( val, max );
                }
            }
            return Optional.<Object>of( valueLabels.get( max.toString() ) );
        }
    }
}