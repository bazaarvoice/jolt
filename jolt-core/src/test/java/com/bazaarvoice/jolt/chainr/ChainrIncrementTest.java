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
package com.bazaarvoice.jolt.chainr;

import com.bazaarvoice.jolt.Chainr;
import com.bazaarvoice.jolt.JoltTestUtil;
import com.bazaarvoice.jolt.JsonUtils;
import com.bazaarvoice.jolt.exception.TransformException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;

public class ChainrIncrementTest {

    @DataProvider
    public Object[][] fromToTests() {

        Object chainrSpec = JsonUtils.classpathToObject( "/json/chainr/increments/spec.json" );

        return new Object[][] {
            {chainrSpec, 0, 1},
            {chainrSpec, 0, 3},
            {chainrSpec, 1, 3},
            {chainrSpec, 1, 4}
        };
    }

    @Test( dataProvider = "fromToTests")
    public void testChainrIncrementsFromTo( Object chainrSpec, int start, int end ) throws IOException {
        Chainr chainr = Chainr.fromSpec( chainrSpec );

        Object expected = JsonUtils.classpathToObject( "/json/chainr/increments/" + start + "-" + end + ".json" );

        Object actual = chainr.transform( start, end, new HashMap() );

        JoltTestUtil.runDiffy( "failed incremental From-To Chainr", expected, actual );
    }


    @DataProvider
    public Object[][] toTests() {

        Object chainrSpec = JsonUtils.classpathToObject(  "/json/chainr/increments/spec.json" );

        return new Object[][] {
                {chainrSpec, 1},
                {chainrSpec, 3}
        };
    }

    @Test( dataProvider = "toTests")
    public void testChainrIncrementsTo( Object chainrSpec, int end  ) throws IOException {

        Chainr chainr = Chainr.fromSpec( chainrSpec );

        Object expected = JsonUtils.classpathToObject( "/json/chainr/increments/0-" + end + ".json" );

        Object actual = chainr.transform( end, new HashMap() );

        JoltTestUtil.runDiffy( "failed incremental To Chainr", expected, actual );
    }

    @DataProvider
    public Object[][] failTests() {

        Object chainrSpec = JsonUtils.classpathToObject( "/json/chainr/increments/spec.json" );

        return new Object[][] {
                {chainrSpec, 0, 0},
                {chainrSpec, -2, 2},
                {chainrSpec, 0, -2},
                {chainrSpec, 1, 10000}
        };
    }

    @Test( dataProvider = "failTests", expectedExceptions = TransformException.class)
    public void testFails( Object chainrSpec, int start, int end  ) throws IOException {
        Chainr chainr = Chainr.fromSpec( chainrSpec );
        chainr.transform( start, end, new HashMap());
    }
}
