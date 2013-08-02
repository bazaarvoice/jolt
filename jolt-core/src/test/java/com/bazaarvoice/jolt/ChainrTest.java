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

import com.bazaarvoice.jolt.chainr.transforms.TransformTestResult;
import com.bazaarvoice.jolt.chainr.spec.ChainrEntry;
import com.bazaarvoice.jolt.chainr.transforms.GoodTestTransform;
import com.bazaarvoice.jolt.exception.SpecException;
import com.bazaarvoice.jolt.exception.TransformException;
import com.bazaarvoice.jolt.chainr.transforms.ExplodingTestTransform;
import org.testng.Assert;
import org.testng.AssertJUnit;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChainrTest {

    private List<Map<String,Object>> newChainrSpec() {
        return new ArrayList<Map<String, Object>>();
    }

    private Map<String, Object> newActivity( String opname ) {
        Map<String, Object> activity = new HashMap<String, Object>();
        activity.put( ChainrEntry.OPERATION_KEY, opname );
        return activity;
    }

    private Map<String, Object> newActivity( String operation, Object spec ) {
        Map<String, Object> activity = new HashMap<String, Object>();
        activity.put( ChainrEntry.OPERATION_KEY, operation );
        if ( spec != null ) {
            activity.put( ChainrEntry.SPEC_KEY, spec );
        }
        return activity;
    }

    private Map<String, Object> newCustomJavaActivity( Class cls, Object spec ) {
        Map<String, Object> activity = new HashMap<String, Object>();
        activity.put( ChainrEntry.OPERATION_KEY, cls.getName() );
        if ( spec != null ) {
            activity.put( ChainrEntry.SPEC_KEY, spec );
        }

        return activity;
    }

    private List<Map<String,Object>> newCustomJavaChainrSpec( Class cls, Object delegateSpec )
    {
        List<Map<String,Object>> retvalue = this.newChainrSpec();
        retvalue.add( newCustomJavaActivity( cls, delegateSpec ));
        return retvalue;
    }

    private List<Map<String,Object>> newShiftrChainrSpec( Object shiftrSpec ) {
        List<Map<String,Object>> retvalue = this.newChainrSpec();
        retvalue.add( newActivity( "shift", shiftrSpec ) );
        return retvalue;
    }

    private List<Map<String,Object>> newShiftrDefaultrSpec( Object defaultrSpec ) {
        List<Map<String,Object>> retvalue = this.newChainrSpec();
        retvalue.add( newActivity( "default", defaultrSpec ) );
        return retvalue;
    }

    private List<Map<String,Object>> newShiftrRemovrSpec( Object removrSpec ) {
        List<Map<String,Object>> retvalue = this.newChainrSpec();
        retvalue.add( newActivity( "remove", removrSpec ) );
        return retvalue;
    }

    private List<Map<String,Object>> newShiftrSortrSpec( Object sortrSpec ) {
        List<Map<String,Object>> retvalue = this.newChainrSpec();
        retvalue.add( newActivity( "sort", sortrSpec ) );
        return retvalue;
    }

    @Test
    public void process_itCallsShiftr() throws IOException {
        Map<String, Object> testUnit = JsonUtils.jsonToMap( ChainrTest.class.getResourceAsStream( "/json/shiftr/queryMappingXform.json" ) );

        Object input = testUnit.get( "input" );
        Object shiftrSpec = testUnit.get( "spec" );
        Object expected = testUnit.get( "expected" );

        Object chainrSpec = this.newShiftrChainrSpec( shiftrSpec );

        Chainr unit = Chainr.fromSpec( chainrSpec );
        Object actual = unit.transform( input, null );

        JoltTestUtil.runDiffy( "failed Shiftr call.", expected, actual );
    }

    @Test
    public void process_itCallsDefaultr() throws IOException {
        Map<String, Object> testUnit = JsonUtils.jsonToMap( ChainrTest.class.getResourceAsStream( "/json/defaultr/firstSample.json" ) );

        Object input = testUnit.get( "input" );
        Object defaultrSpec = testUnit.get( "spec" );
        Object expected = testUnit.get( "expected" );

        Object chainrSpec = this.newShiftrDefaultrSpec( defaultrSpec );

        Chainr unit = Chainr.fromSpec( chainrSpec );
        Object actual = unit.transform( input, null );

        JoltTestUtil.runDiffy( "failed Defaultr call.", expected, actual );
    }

    @Test
    public void process_itCallsRemover() throws IOException {
        Map<String, Object> testUnit = JsonUtils.jsonToMap( ChainrTest.class.getResourceAsStream( "/json/removr/firstSample.json" ) );

        Object input = testUnit.get( "input" );
        Object removrSpec = testUnit.get( "spec" );
        Object expected = testUnit.get( "expected" );

        Object chainrSpec = this.newShiftrRemovrSpec( removrSpec );

        Chainr unit = Chainr.fromSpec( chainrSpec );
        Object actual = unit.transform( input, null );

        JoltTestUtil.runDiffy( "failed Removr call.", expected, actual );
    }

    @Test
    public void process_itCallsSortr() throws IOException {
        Object input = JsonUtils.jsonToObject( ChainrTest.class.getResourceAsStream( "/json/sortr/simple/input.json" ) );
        Object expected = JsonUtils.jsonToObject( ChainrTest.class.getResourceAsStream( "/json/sortr/simple/output.json" ) );
        Object chainrSpec = this.newShiftrSortrSpec( null );

        Chainr unit = Chainr.fromSpec( chainrSpec );
        Object actual = unit.transform( input, null );

        JoltTestUtil.runDiffy( "failed Sortr call.", expected, actual );

        String orderErrorMessage = SortrTest.verifyOrder( actual, expected );
        AssertJUnit.assertNull( orderErrorMessage, orderErrorMessage );
    }

    @Test
    public void process_itCallsCustomJavaTransform() {
        List<Map<String,Object>> spec = this.newChainrSpec();
        Object delegateSpec = new HashMap();
        spec.add( this.newCustomJavaActivity( GoodTestTransform.class, delegateSpec ) );
        Object input = new Object();

        Chainr unit = Chainr.fromSpec( spec );
        TransformTestResult actual = (TransformTestResult) unit.transform( input, null );

        AssertJUnit.assertEquals( input, actual.input );
        AssertJUnit.assertEquals( delegateSpec, actual.spec );
    }

    @DataProvider
    public Object[][] failureSpecCases() {
        return new Object[][] {
                { null },
                { "foo" },
                { this.newActivity( null ) },
                { this.newActivity( "pants" ) },
        };
    }

    @Test(dataProvider = "failureSpecCases", expectedExceptions = SpecException.class)
    public void process_itBlowsUp_fromSpec(Object spec) {
        Chainr.fromSpec( spec );
        AssertJUnit.fail("Should have failed during spec initialization.");
    }

    @DataProvider
    public Object[][] failureTransformCases() {
        return new Object[][] {
            { this.newCustomJavaChainrSpec( ExplodingTestTransform.class, null ) }
        };
    }

    @Test(dataProvider = "failureTransformCases", expectedExceptions = TransformException.class)
    public void process_itBlowsUp_fromTransform(Object spec) {
        Chainr unit = Chainr.fromSpec( spec );
        unit.transform( new HashMap(), null );
        AssertJUnit.fail("Should have failed during transform.");
    }




    @DataProvider
    public Object[][] getTestCaseNames() {
        return new Object[][] {
            {"firstSample", true},
        };
    }

    @Test(dataProvider = "getTestCaseNames")
    public void runTestCases(String testCaseName, boolean sorted ) throws IOException {
        String testPath = "/json/chainr/integration/" + testCaseName;
        Map<String, Object> testUnit = JsonUtils.jsonToMap( Defaultr.class.getResourceAsStream( testPath + ".json" ) );

        Object input = testUnit.get( "input" );
        Object spec = testUnit.get( "spec" );
        Object expected = testUnit.get( "expected" );

        Chainr unit = Chainr.fromSpec( spec );

        Assert.assertFalse( unit.hasContextualTransforms() );
        Assert.assertEquals( unit.getContextualTransforms().size(), 0 );

        Object actual = unit.transform( input, null );

        JoltTestUtil.runDiffy( "failed case " + testPath, expected, actual );

        if ( sorted ) {
            // Make sure the sort actually worked.
            String orderErrorMessage = SortrTest.verifyOrder( actual, expected );
            AssertJUnit.assertNull( orderErrorMessage, orderErrorMessage );
        }
    }
}
