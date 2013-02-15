package com.bazaarvoice.jolt;

import com.bazaarvoice.jolt.exception.SpecException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;

public class ChainrIncrementTest {

    @DataProvider
    public Object[][] fromToTests() throws IOException {

        Object chainrSpec = JsonUtils.jsonToObject( ChainrIncrementTest.class.getResourceAsStream( "/json/chainrIncrements/spec.json" ) );

        return new Object[][] {
            {chainrSpec, 0, 0},
            {chainrSpec, 0, 2},
            {chainrSpec, 1, 2},
            {chainrSpec, 1, 3}
        };
    }

    @Test( dataProvider = "fromToTests")
    public void testChainrIncrementsFromTo( Object chainrSpec, int start, int end ) throws IOException {
        Chainr chainr = new Chainr( chainrSpec, start, end );

        Object expected = JsonUtils.jsonToObject( ChainrIncrementTest.class.getResourceAsStream( "/json/chainrIncrements/" + start + "-" + end + ".json" ) );

        Object actual = chainr.transform( new HashMap() );

        JoltTestUtil.runDiffy( "failed incremental From-To Chainr", expected, actual );
    }


    @DataProvider
    public Object[][] toTests() throws IOException {

        Object chainrSpec = JsonUtils.jsonToObject( ChainrIncrementTest.class.getResourceAsStream( "/json/chainrIncrements/spec.json" ) );

        return new Object[][] {
                {chainrSpec, 0},
                {chainrSpec, 2}
        };
    }

    @Test( dataProvider = "toTests")
    public void testChainrIncrementsTo( Object chainrSpec, int end  ) throws IOException {

        Chainr chainr = new Chainr( chainrSpec, end );

        Object expected = JsonUtils.jsonToObject( ChainrIncrementTest.class.getResourceAsStream( "/json/chainrIncrements/0-" + end + ".json" ) );

        Object actual = chainr.transform( new HashMap() );

        JoltTestUtil.runDiffy( "failed incremental To Chainr", expected, actual );
    }

    @DataProvider
    public Object[][] failTests() throws IOException {

        Object chainrSpec = JsonUtils.jsonToObject( ChainrIncrementTest.class.getResourceAsStream( "/json/chainrIncrements/spec.json" ) );

        return new Object[][] {
                {chainrSpec, -2, 2},
                {chainrSpec, 0, -2},
                {chainrSpec, 1, 10000}
        };
    }

    @Test( dataProvider = "failTests", expectedExceptions = SpecException.class)
    public void testFails( Object chainrSpec, int start, int end  ) throws IOException {
        new Chainr( chainrSpec, start, end );
    }
}
