package com.bazaarvoice.jolt;

import com.bazaarvoice.jolt.exception.SpecException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class DefaultrTest {

    @DataProvider
    public Object[][] getTestCaseNames() {
        return new Object[][] {
            {"defaultNulls"},
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
    public void runTestCases(String testCaseName) throws IOException {

        String testPath = "/json/defaultr/" + testCaseName;
        Map<String, Object> testUnit = (Map<String, Object>) JsonUtils.jsonToObject( Defaultr.class.getResourceAsStream( testPath + ".json" ) );

        Object input = testUnit.get( "input" );
        Object spec = testUnit.get( "spec" );
        Object expected = testUnit.get( "expected" );

        Defaultr defaultr = new Defaultr(spec);
        Object actual = defaultr.transform( input );

        JoltTestUtil.runDiffy( "failed case " + testPath, expected, actual );
    }

    @Test
    public void deepCopyTest() throws IOException {
        Map<String, Object> testUnit = (Map<String, Object>) JsonUtils.jsonToObject( Defaultr.class.getResourceAsStream( "/json/defaultr/deepCopy.json" ) );

        Object spec = testUnit.get( "spec" );

        Defaultr defaultr = new Defaultr(spec);
        {
            Object input = testUnit.get( "input" );
            Map<String, Object> fiddle = (Map<String, Object>) defaultr.transform( input );

            List array = (List) fiddle.get( "array" );
            array.add("a");

            Map<String,Object> subMap = (Map<String,Object>) fiddle.get( "map" );
            subMap.put("c", "c");
        }
        {
            Map<String, Object> testUnit2 = (Map<String, Object>) JsonUtils.jsonToObject( Defaultr.class.getResourceAsStream( "/json/defaultr/deepCopy.json" ) );

            Object input = testUnit2.get( "input" );
            Object expected = testUnit2.get( "expected" );

            Object actual = defaultr.transform( input );
            JoltTestUtil.runDiffy( "Same spec deepcopy fail.", expected, actual );
        }
    }

    @Test(expectedExceptions = SpecException.class)
    public void throwExceptionOnBadSpec() throws IOException {
        Object spec = JsonUtils.jsonToMap( "{ \"tuna*\": \"marlin\" }" );
        new Defaultr( spec );
    }
}
