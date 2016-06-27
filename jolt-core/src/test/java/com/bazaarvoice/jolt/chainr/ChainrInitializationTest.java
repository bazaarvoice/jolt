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
import com.bazaarvoice.jolt.ContextualTransform;
import com.bazaarvoice.jolt.JoltTransform;
import com.bazaarvoice.jolt.JsonUtils;
import com.bazaarvoice.jolt.Transform;
import com.bazaarvoice.jolt.chainr.transforms.TransformTestResult;
import com.bazaarvoice.jolt.exception.SpecException;
import com.bazaarvoice.jolt.exception.TransformException;
import com.beust.jcommander.internal.Lists;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChainrInitializationTest {

    @DataProvider
    public Object[][] badTransforms() {
        return new Object[][] {
            {JsonUtils.classpathToObject(  "/json/chainr/transforms/bad_transform_loadsExplodingTransform.json" )}
        };
    }

    @Test(dataProvider = "badTransforms", expectedExceptions = TransformException.class )
    public void testBadTransforms(Object chainrSpec) {
        Chainr unit = Chainr.fromSpec( chainrSpec );
        unit.transform( new HashMap(), null );// should fail here
        Assert.fail( "Should not have gotten here" );
    }

    @DataProvider
    public Object[][] passingTestCases() {
        return new Object[][] {
            {new Object(), JsonUtils.classpathToObject( "/json/chainr/transforms/loadsGoodTransform.json" )}
        };
    }

    @Test(dataProvider = "passingTestCases" )
    public void testPassing(Object input, Object spec) {
        Chainr unit = Chainr.fromSpec( spec );
        TransformTestResult actual = (TransformTestResult) unit.transform( input, null );

        Assert.assertEquals( input, actual.input );
        Assert.assertNotNull( actual.spec );
    }

    @Test( expectedExceptions = IllegalArgumentException.class )
    public void chainrBuilderFailsOnNullLoader() {

        Object validSpec = JsonUtils.classpathToObject( "/json/chainr/transforms/loadsGoodTransform.json" );
        new ChainrBuilder( validSpec ).loader( null );
    }

    @Test( expectedExceptions = IllegalArgumentException.class )
    public void failsOnNullListOfJoltTransforms() {
        new Chainr( null );
    }

    @Test( expectedExceptions = SpecException.class )
    public void failsOnStupidTransform() {
        List<JoltTransform> badSpec = Lists.newArrayList();

        // Stupid JoltTransform that implements the base interface, and not one of the useful ones
        badSpec.add( new JoltTransform() {} );

        new Chainr( badSpec );
    }

    @Test( expectedExceptions = SpecException.class )
    public void failsOnOverEagerTransform() {
        List<JoltTransform> badSpec = Lists.newArrayList();

        // Stupid JoltTransform that implements both "real" interfaces
        badSpec.add( new OverEagerTransform() );

        new Chainr( badSpec );
    }

    private static class OverEagerTransform implements Transform, ContextualTransform {
        @Override
        public Object transform( Object input, Map<String, Object> context ) {
            return null;
        }

        @Override
        public Object transform( Object input ) {
            return null;
        }
    }
}
