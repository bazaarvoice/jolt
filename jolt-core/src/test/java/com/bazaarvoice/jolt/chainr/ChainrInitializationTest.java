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
import com.bazaarvoice.jolt.JsonUtils;
import com.bazaarvoice.jolt.chainr.transforms.TransformTestResult;
import com.bazaarvoice.jolt.exception.TransformException;
import org.testng.AssertJUnit;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;

public class ChainrInitializationTest {

    @DataProvider
    public Object[][] badTransforms() throws IOException {
        return new Object[][] {
            {JsonUtils.jsonToObject( ChainrInitializationTest.class.getResourceAsStream( "/json/chainr/transforms/bad_transform_loadsExplodingTransform.json" ) )}
        };
    }

    @Test(dataProvider = "badTransforms", expectedExceptions = TransformException.class )
    public void testBadTransforms(Object chainrSpec) {
        Chainr unit = Chainr.fromSpec( chainrSpec );
        unit.transform( new HashMap(), null );// should fail here
        AssertJUnit.fail( "Should not have gotten here" );
    }

    @DataProvider
    public Object[][] passingTestCases() throws IOException {
        return new Object[][] {
            {new Object(), JsonUtils.jsonToObject( ChainrInitializationTest.class.getResourceAsStream( "/json/chainr/transforms/loadsGoodTransform.json" ) )}
        };
    }

    @Test(dataProvider = "passingTestCases" )
    public void testPassing(Object input, Object spec) {
        Chainr unit = Chainr.fromSpec( spec );
        TransformTestResult actual = null;
        actual = (TransformTestResult) unit.transform( input, null );

        AssertJUnit.assertEquals( input, actual.input );
        AssertJUnit.assertNotNull( actual.spec );
    }

    @Test( expectedExceptions = RuntimeException.class )
    public void chainrBuilderFailsOnNullLoader() throws IOException {

        Object validSpec = JsonUtils.jsonToObject( ChainrInitializationTest.class.getResourceAsStream( "/json/chainr/transforms/loadsGoodTransform.json" ) );
        new ChainrBuilder( validSpec ).loader( null );
    }
}
