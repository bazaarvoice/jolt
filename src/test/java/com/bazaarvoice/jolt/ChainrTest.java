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

    private Map<String, Object> newShiftrActivity(Object shiftrSpec ) {
        Map<String, Object> activity = new HashMap<String, Object>();
        activity.put( "operation", "shift" );
        activity.put( "spec", shiftrSpec );
        return activity;
    }

    private Map<String, Object> newDefaultrActivity(Object defaultrSpec ) {
        Map<String, Object> activity = new HashMap<String, Object>();
        activity.put( "operation", "default" );
        activity.put( "spec", defaultrSpec );
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
        retvalue.add( newShiftrActivity( shiftrSpec ) );
        return retvalue;
    }

    private List<Map<String,Object>> newDefaultrSpec(Object defaultrSpec) {
        List<Map<String,Object>> retvalue = this.newSpec();
        retvalue.add( newDefaultrActivity( defaultrSpec ) );
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

        ShiftrTest.ArrayDisorderDiffy diffy = new ShiftrTest.ArrayDisorderDiffy();
        Diffy.Result result = diffy.diff( expected, actual );
        if (!result.isEmpty()) {
            AssertJUnit.fail( "failed shiftr call.\nhere is a diff:\nexpected: " + JsonUtils.toJsonString( result.expected ) + "\nactual: " + JsonUtils.toJsonString( result.actual ) );
        }
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

        ShiftrTest.ArrayDisorderDiffy diffy = new ShiftrTest.ArrayDisorderDiffy();
        Diffy.Result result = diffy.diff( expected, actual );
        if (!result.isEmpty()) {
            AssertJUnit.fail( "failed Defaultr call.\nhere is a diff:\nexpected: " + JsonUtils.toJsonString( result.expected ) + "\nactual: " + JsonUtils.toJsonString( result.actual ) );
        }
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
    private void process_itBlowsUp(Object spec)
            throws JoltException {
        Object input = new Object();
        Chainr unit = new Chainr();
        unit.process( input, spec );
    }
}
