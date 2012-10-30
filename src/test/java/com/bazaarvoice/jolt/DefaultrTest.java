package com.bazaarvoice.jolt;

import org.testng.AssertJUnit;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DefaultrTest {

    @DataProvider
    public Object[][] getTestCaseNames() {
        return new Object[][] {
            {"firstSample"},
            {"identity"},
            {"expansionOnly"},
            {"photosArray"},
            {"arrayMismatch1"},
            {"arrayMismatch2"},
            {"starsOfStars"},
            {"topLevelIsArray"},
            {"orOrdering"}
        };
    }

    @Test(dataProvider = "getTestCaseNames")
    public void runTestCases(String testCaseName)
            throws IOException {
        if ("".equals( testCaseName )) {
            return;
        }
        String testPath = "/json/defaultr/" + testCaseName;
        Object input = JsonUtils.jsonToObject( Defaultr.class.getResourceAsStream( testPath + "/input.json" ) );
        Object spec = JsonUtils.jsonToObject( Defaultr.class.getResourceAsStream( testPath + "/spec.json" ) );
        Object expected = JsonUtils.jsonToObject( Defaultr.class.getResourceAsStream( testPath + "/output.json" ) );

        Defaultr defaultr = new Defaultr();
        Object actual = defaultr.defaultr( spec, input );

        JoltTestUtil.runDiffy( "failed case " + testPath, expected, actual );
    }

    @Test
    public void deepCopyTest() throws IOException {

        Object spec = JsonUtils.jsonToObject( Defaultr.class.getResourceAsStream( "/json/defaultr/deepCopy/spec.json" ) );
        Defaultr defaultr = new Defaultr();

        {
            Object input = JsonUtils.jsonToObject( Defaultr.class.getResourceAsStream( "/json/defaultr/deepCopy/input.json" ) );
            Map<String, Object> fiddle = (Map<String, Object>) defaultr.defaultr( spec, input );

            List array = (List) fiddle.get( "array" );
            array.add("a");

            Map<String,Object> subMap = (Map<String,Object>) fiddle.get( "map" );
            subMap.put("c", "c");
        }
        {
            Object input = JsonUtils.jsonToObject( Defaultr.class.getResourceAsStream( "/json/defaultr/deepCopy/input.json" ) );
            Object expected = JsonUtils.jsonToObject( Defaultr.class.getResourceAsStream( "/json/defaultr/deepCopy/output.json" ) );

            Object actual = defaultr.defaultr( spec, input );
            JoltTestUtil.runDiffy( "Same spec deepcopy fail.", expected, actual );
        }
    }

    @Test
    public void throwExceptionOnNullDefaultee() throws IOException {
        Object spec = JsonUtils.jsonToMap( "{ \"tuna\": \"marlin\" }" );

        Defaultr defaultr = new Defaultr();
        try {
            defaultr.defaultr( spec, null );
            AssertJUnit.fail("expected illegal argument exception");
        }
        catch( IllegalArgumentException iae )
        { }
        catch( Exception e ){
            AssertJUnit.fail("expected illegal argument exception, but got something else");
        }
    }

    @Test
    public void throwExceptionOnBadSpec() throws IOException {
        Object spec = JsonUtils.jsonToMap( "{ \"tuna*\": \"marlin\" }" );

        Defaultr defaultr = new Defaultr();
        try {
            defaultr.defaultr( spec, new LinkedHashMap() );
            AssertJUnit.fail("expected illegal argument exception");
        }
        catch( IllegalArgumentException iae )
        { }
        catch( Exception e ){
            AssertJUnit.fail("expected illegal argument exception, but got something else");
        }
    }
}
