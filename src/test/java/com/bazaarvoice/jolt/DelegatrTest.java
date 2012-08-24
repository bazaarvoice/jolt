package com.bazaarvoice.jolt;

import org.testng.AssertJUnit;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Map;

public class DelegatrTest {

    @DataProvider
    public Object[][] testCases()
            throws IOException {
        return new Object[][] {
                {null, JsonUtils.jsonToObject( DelegatrTest.class.getResourceAsStream( "/json/delegatr/empty.json" ) )},
                {null, JsonUtils.jsonToObject( DelegatrTest.class.getResourceAsStream( "/json/delegatr/arrayClassName.json" ) )},
                {null, JsonUtils.jsonToObject( DelegatrTest.class.getResourceAsStream( "/json/delegatr/badClassName.json" ) )},
                {null, JsonUtils.jsonToObject( DelegatrTest.class.getResourceAsStream( "/json/delegatr/loadsDelegatr.json" ) )},
                {null, JsonUtils.jsonToObject( DelegatrTest.class.getResourceAsStream( "/json/delegatr/loadsString.json" ) )},
                {null, JsonUtils.jsonToObject( DelegatrTest.class.getResourceAsStream( "/json/delegatr/loadsExplodingDelegate.json" ) )},
                {new Object(), JsonUtils.jsonToObject( DelegatrTest.class.getResourceAsStream( "/json/delegatr/loadsGoodDelegate.json" ) )}
        };
    }

    @Test(dataProvider = "testCases")
    public void testProcess(Object input, Map<String, Object> pipelineEntry)
            throws JoltException {
        Delegatr unit = new Delegatr();
        DelegationResult actual = null;
        try {
            actual = (DelegationResult) unit.process( input, pipelineEntry );
            AssertJUnit.assertEquals( input, actual.input );
            AssertJUnit.assertEquals( pipelineEntry, actual.pipelineEntry );
        } catch ( JoltException e ) {
            if (input != null) {
                throw e;
            }
            // else ok
        }
    }
}

class ExplodingDelegate implements Chainable {

    @Override
    public Object process( Object input, Map<String, Object> pipelineEntry )
            throws JoltException {
        throw new JoltException( "kaboom" );
    }
}

class GoodDelegate implements Chainable {

    @Override
    public Object process( Object input, Map<String, Object> pipelineEntry )
            throws JoltException {
        return new DelegationResult( input, pipelineEntry );
    }
}

class DelegationResult {
    Object input;
    Map<String, Object> pipelineEntry;
    DelegationResult(Object input, Map<String, Object> pipelineEntry) {
        this.input = input;
        this.pipelineEntry = pipelineEntry;
    }
}
