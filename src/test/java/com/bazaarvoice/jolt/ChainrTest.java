package com.bazaarvoice.jolt;

import org.testng.AssertJUnit;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChainrTest {

    private List<Map<String,Object>> newSpec() {
        return new ArrayList<Map<String, Object>>();
    }

    private Map<String, Object> newActivity( String opname ) {
        Map<String, Object> activity = new HashMap<String, Object>();
        activity.put( "operation", opname );
        return activity;
    }

    private Map<String, Object> newActivity( String operation, Object spec ) {
        Map<String, Object> activity = new HashMap<String, Object>();
        activity.put( "operation", operation );
        activity.put( "spec", spec );
        return activity;
    }

    private Map<String, Object> newFailActivity( String operation, Object spec ) {
        Map<String, Object> activity = new HashMap<String, Object>();
        activity.put( "operation", operation );
        activity.put( "tuna", spec );
        return activity;
    }

    private Map<String, Object> newDelegatrActivity(Class cls) {
        Map<String, Object> activity = new HashMap<String, Object>();
        activity.put( "operation", "java" );
        activity.put( "className", cls.getName() );
        return activity;
    }

    private List<Map<String,Object>> newShiftrSpec(Object shiftrSpec) {
        List<Map<String,Object>> retvalue = this.newSpec();
        retvalue.add( newActivity( "shift", shiftrSpec ) );
        return retvalue;
    }

    private List<Map<String,Object>> newDefaultrSpec(Object defaultrSpec) {
        List<Map<String,Object>> retvalue = this.newSpec();
        retvalue.add( newActivity( "default", defaultrSpec ) );
        return retvalue;
    }

    private List<Map<String,Object>> newRemovrSpec(Object removrSpec) {
        List<Map<String,Object>> retvalue = this.newSpec();
        retvalue.add( newActivity( "remove", removrSpec ) );
        return retvalue;
    }

    private List<Map<String,Object>> newSortrSpec(Object sortrSpec) {
        List<Map<String,Object>> retvalue = this.newSpec();
        retvalue.add( newActivity( "sort", sortrSpec ) );
        return retvalue;
    }

    @Test
    public void process_itCallsShiftr()
            throws IOException, JoltException {
        Object input = JsonUtils.jsonToObject( ChainrTest.class.getResourceAsStream( "/json/shiftr/queryMappingXform/input.json" ) );
        Object expected = JsonUtils.jsonToObject( ChainrTest.class.getResourceAsStream( "/json/shiftr/queryMappingXform/output.json" ) );
        Object shiftrSpec = JsonUtils.jsonToObject( ChainrTest.class.getResourceAsStream( "/json/shiftr/queryMappingXform/spec.json" ) );
        Object chainrSpec = this.newShiftrSpec( shiftrSpec );

        Chainr unit = new Chainr();
        Object actual = unit.process( input, chainrSpec );

        JoltTestUtil.runDiffy( "failed Shiftr call.", expected, actual );
    }

    @Test
    public void process_itCallsDefaultr()
            throws IOException, JoltException {
        Object input = JsonUtils.jsonToObject( ChainrTest.class.getResourceAsStream( "/json/defaultr/firstSample/input.json" ) );
        Object expected = JsonUtils.jsonToObject( ChainrTest.class.getResourceAsStream( "/json/defaultr/firstSample/output.json" ) );
        Object defaultrSpec = JsonUtils.jsonToObject( ChainrTest.class.getResourceAsStream( "/json/defaultr/firstSample/spec.json" ) );
        Object chainrSpec = this.newDefaultrSpec( defaultrSpec );

        Chainr unit = new Chainr();
        Object actual = unit.process( input, chainrSpec );

        JoltTestUtil.runDiffy( "failed Defaultr call.", expected, actual );
    }

    @Test
    public void process_itCallsRemover()
            throws IOException, JoltException {
        Object input = JsonUtils.jsonToObject( ChainrTest.class.getResourceAsStream( "/json/removr/firstSample/input.json" ) );
        Object expected = JsonUtils.jsonToObject( ChainrTest.class.getResourceAsStream( "/json/removr/firstSample/output.json" ) );
        Object removrSpec = JsonUtils.jsonToObject( ChainrTest.class.getResourceAsStream( "/json/removr/firstSample/spec.json" ) );
        Object chainrSpec = this.newRemovrSpec( removrSpec );

        Chainr unit = new Chainr();
        Object actual = unit.process( input, chainrSpec );

        JoltTestUtil.runDiffy( "failed Removr call.", expected, actual );
    }

    @Test
    public void process_itCallsSortr()
            throws IOException, JoltException {
        Object input = JsonUtils.jsonToObject( ChainrTest.class.getResourceAsStream( "/json/sortr/simple/input.json" ) );
        Object expected = JsonUtils.jsonToObject( ChainrTest.class.getResourceAsStream( "/json/sortr/simple/output.json" ) );
        Object sortrSpec = JsonUtils.jsonToObject( ChainrTest.class.getResourceAsStream( "/json/sortr/simple/spec.json" ) );
        Object chainrSpec = this.newSortrSpec( sortrSpec );

        Chainr unit = new Chainr();
        Object actual = unit.process( input, chainrSpec );

        JoltTestUtil.runDiffy( "failed Sortr call.", expected, actual );

        String orderErrorMessage = SortrTest.verifyOrder( actual, expected );
        AssertJUnit.assertNull( orderErrorMessage, orderErrorMessage );
    }

    @Test
    public void process_itCallsDelegatr()
            throws JoltException {
        List<Map<String,Object>> spec = this.newSpec();
        spec.add( this.newDelegatrActivity( GoodDelegate.class ) );
        Object input = new Object();

        Chainr unit = new Chainr();
        DelegationResult actual = (DelegationResult) unit.process( input, spec );

        AssertJUnit.assertEquals( input, actual.input );
        AssertJUnit.assertEquals( spec.get( 0 ), actual.pipelineEntry );
    }

    @DataProvider
    public Object[][] failureCases() {
        return new Object[][] {
                { null },
                { "foo" },
                { this.newActivity( null ) },
                { this.newActivity( "pants" ) },
                { this.newDelegatrActivity( ExplodingDelegate.class ) },
        };
    }

    @Test(dataProvider = "failureCases", expectedExceptions = JoltException.class)
    public void process_itBlowsUp(Object spec)
            throws JoltException {
        Object input = new Object();
        Chainr unit = new Chainr();
        unit.process( input, spec );
    }

    @DataProvider
    public Object[][] failureCases2() {
        return new Object[][] {
            { "shift" },
            { "remove" },
            { "default" },
            { "sort" }
        };
    }

    @Test(dataProvider = "failureCases2", expectedExceptions = JoltException.class)
    public void process_itBlowsUp_reachForTestCoverage(String operation)
            throws JoltException {
        Object input = new Object();
        Chainr unit = new Chainr();

        List<Map<String,Object>> retvalue = this.newSpec();
        retvalue.add( this.newFailActivity( operation, new Object() ) );

        unit.process( input, retvalue );
    }

    @DataProvider
    public Object[][] getTestCaseNames() {
        return new Object[][] {
            {"firstSample", true},
        };
    }

    @Test(dataProvider = "getTestCaseNames")
    public void runTestCases(String testCaseName, boolean sorted ) throws IOException, JoltException {

        String testPath = "/json/chainr/"+testCaseName;
        Object input = JsonUtils.jsonToObject( Shiftr.class.getResourceAsStream( testPath + "/input.json" ) );
        Object spec = JsonUtils.jsonToObject( Shiftr.class.getResourceAsStream( testPath + "/spec.json" ) );
        Object expected = JsonUtils.jsonToObject( Shiftr.class.getResourceAsStream( testPath + "/output.json" ) );

        Chainr chainr = new Chainr();
        Object actual = chainr.process( input, spec );

        JoltTestUtil.runDiffy( "failed case " + testPath, expected, actual );

        if ( sorted ) {
            // Make sure the sort actually worked.
            String orderErrorMessage = SortrTest.verifyOrder( actual, expected );
            AssertJUnit.assertNull( orderErrorMessage, orderErrorMessage );
        }
    }
}
