package com.bazaarvoice.jolt.shiftr.spec;

import com.bazaarvoice.jolt.JsonUtils;
import com.bazaarvoice.jolt.Shiftr;
import org.testng.AssertJUnit;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class KeyOrderingTest {

    @DataProvider
    public Object[][] shiftrKeyOrderingTestCases() throws IOException {
        return new Object[][] {
            {
                "Simple * and &",
                JsonUtils.jsonToMap( "{ \"*\" : { \"a\" : \"b\" }, \"&\" : { \"a\" : \"b\" } }" ),
                Arrays.asList( "&(0,0)", "*" )
            },
            {
                "2* and 2&",
                JsonUtils.jsonToMap( "{ \"rating-*\" : { \"a\" : \"b\" }, \"rating-range-*\" : { \"a\" : \"b\" }, \"&\" : { \"a\" : \"b\" }, \"tuna-&(0)\" : { \"a\" : \"b\" } }" ),
                Arrays.asList( "tuna-&(0,0)", "&(0,0)", "rating-range-*", "rating-*" )
            },
            {
                "2& alpha-number based fallback",
                JsonUtils.jsonToMap( "{ \"&\" : { \"a\" : \"b\" }, \"&(0,1)\" : { \"a\" : \"b\" } }" ),
                Arrays.asList( "&(0,0)", "&(0,1)" )
            },
            {
                "2* and 2& alpha fallback",
                JsonUtils.jsonToMap( "{ \"aaaa-*\" : { \"a\" : \"b\" }, \"bbbb-*\" : { \"a\" : \"b\" }, \"aaaa-&\" : { \"a\" : \"b\" }, \"bbbb-&(0)\" : { \"a\" : \"b\" } }" ),
                Arrays.asList( "aaaa-&(0,0)", "bbbb-&(0,0)", "aaaa-*", "bbbb-*" )
            }
        };
    }

    @Test(dataProvider = "shiftrKeyOrderingTestCases" )
    public void testKeyOrdering( String testName, Map<String,Object> spec, List<String> expectedOrder ) {

        CompositeSpec root = new CompositeSpec( Shiftr.ROOT_KEY, spec );

        for ( int index = 0; index < expectedOrder.size(); index++) {
            String expected = expectedOrder.get( index );
            AssertJUnit.assertEquals( testName, expected, root.getComputedChildren().get( index ).pathElement.getCanonicalForm() );
        }
    }
}
