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

import com.bazaarvoice.jolt.chainr.DelegationResult;
import com.bazaarvoice.jolt.exception.SpecException;
import com.bazaarvoice.jolt.exception.TransformException;
import org.testng.AssertJUnit;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;

public class ChainrInitializationTest {

    @DataProvider
    public Object[][] badSpecs() throws IOException {
        return new Object[][] {
            {JsonUtils.jsonToObject( ChainrInitializationTest.class.getResourceAsStream( "/json/chainrInitialization/bad_spec_arrayClassName.json" ) )},
            {JsonUtils.jsonToObject( ChainrInitializationTest.class.getResourceAsStream( "/json/chainrInitialization/bad_spec_ClassName.json" ) )},
            {JsonUtils.jsonToObject( ChainrInitializationTest.class.getResourceAsStream( "/json/chainrInitialization/bad_spec_NonTransformClass.json" ) )},
            {JsonUtils.jsonToObject( ChainrInitializationTest.class.getResourceAsStream( "/json/chainrInitialization/bad_spec_empty.json" ) )},
            {JsonUtils.jsonToObject( ChainrInitializationTest.class.getResourceAsStream( "/json/chainrInitialization/bad_spec_SpecTransform.json" ) )}
        };
    }

    @Test(dataProvider = "badSpecs", expectedExceptions = SpecException.class )
    public void testBadSpecs(Object chainrSpec) {
        new Chainr( chainrSpec ); // should fail when parsing spec
        AssertJUnit.fail( "Should not have gotten here" );
    }

    @DataProvider
    public Object[][] badTransforms() throws IOException {
        return new Object[][] {
            {JsonUtils.jsonToObject( ChainrInitializationTest.class.getResourceAsStream( "/json/chainrInitialization/bad_transform_loadsExplodingDelegate.json" ) )}
        };
    }

    @Test(dataProvider = "badTransforms", expectedExceptions = TransformException.class )
    public void testBadTransforms(Object chainrSpec) {
        Chainr unit = new Chainr( chainrSpec );
        unit.transform( new HashMap() );// should fail here
        AssertJUnit.fail( "Should not have gotten here" );
    }

    @DataProvider
    public Object[][] passingTestCases() throws IOException {
        return new Object[][] {
            {new Object(), JsonUtils.jsonToObject( ChainrInitializationTest.class.getResourceAsStream( "/json/chainrInitialization/loadsGoodDelegate.json" ) )}
        };
    }

    @Test(dataProvider = "passingTestCases" )
    public void testPassing(Object input, Object spec) {
        Chainr unit = new Chainr( spec );
        DelegationResult actual = null;
        actual = (DelegationResult) unit.transform( input );

        AssertJUnit.assertEquals( input, actual.input );
        AssertJUnit.assertNotNull( actual.spec );
    }
}
