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
import com.bazaarvoice.jolt.chainr.instantiator.GuiceChainrInstantiator;
import com.bazaarvoice.jolt.chainr.transforms.GuiceSpecDrivenTransform;
import com.bazaarvoice.jolt.chainr.transforms.GuiceTransform;
import com.bazaarvoice.jolt.chainr.transforms.GuiceTransformMissingInjectAnnotation;
import com.bazaarvoice.jolt.exception.SpecException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.Provides;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Map;

public class GuicedChainrTest {

    @Test
    public void successTestCase() throws IOException {

        String testPath = "/json/chainr/guice_spec.json";
        Map<String, Object> testUnit = JsonUtils.classpathToMap( testPath );

        Object input = testUnit.get( "input" );
        Object spec = testUnit.get( "spec" );
        Object expected = testUnit.get( "expected" );

        Module parentModule = new AbstractModule() {
            @Override
            protected void configure() {
            }

            @Provides
            public GuiceTransform.GuiceConfig getConfigC() {
                return new GuiceTransform.GuiceConfig( "c", "cc" );
            }

            @Provides
            public GuiceSpecDrivenTransform.GuiceConfig getConfigD() {
                return new GuiceSpecDrivenTransform.GuiceConfig( "dd" );
            }
        };

        Chainr unit = Chainr.fromSpec( spec, new GuiceChainrInstantiator( parentModule ) );

        Assert.assertFalse( unit.hasContextualTransforms() );
        Assert.assertEquals( unit.getContextualTransforms().size(), 0 );

        Object actual = unit.transform( input, null );

        JoltTestUtil.runDiffy( "failed case " + testPath, expected, actual );
    }


    @Test( expectedExceptions = SpecException.class )
    public void itBlowsUpForMissingProviderStockTransform() throws IOException
    {
        String testPath = "/json/chainr/guice_spec.json";
        Map<String, Object> testUnit = JsonUtils.classpathToMap( testPath );

        Object spec = testUnit.get( "spec" );

        Module parentModule = new AbstractModule() {
            @Override
            protected void configure() {
            }

            @Provides
            public GuiceSpecDrivenTransform.GuiceConfig getConfigD() {
                return new GuiceSpecDrivenTransform.GuiceConfig( "dd" );
            }
        };

        Chainr.fromSpec( spec, new GuiceChainrInstantiator( parentModule ) );
    }

    @Test( expectedExceptions = SpecException.class )
    public void itBlowsUpForMissingProviderSpecTransform() throws IOException
    {
        String testPath = "/json/chainr/guice_spec.json";
        Map<String, Object> testUnit = JsonUtils.classpathToMap( testPath );

        Object spec = testUnit.get( "spec" );

        Module parentModule = new AbstractModule() {
            @Override
            protected void configure() {
            }

            @Provides
            public GuiceTransform.GuiceConfig getConfigC() {
                return new GuiceTransform.GuiceConfig( "c", "cc" );
            }
        };

        Chainr.fromSpec( spec, new GuiceChainrInstantiator( parentModule ) );
    }

    @Test( expectedExceptions = SpecException.class )
    public void itBlowsUpForBadGuiceTransform() {
        Chainr.fromSpec( ImmutableList.of( ImmutableMap.of( "operator", "com.bazaarvoice.jolt.chainr.transforms.GuiceTransformMissingInjectAnnotation" ) ),
                new GuiceChainrInstantiator( new AbstractModule() {
                    @Override
                    protected void configure() {
                    }

                    @Provides
                    public GuiceTransformMissingInjectAnnotation.BadGuiceConfig getConfigC() {
                        return new GuiceTransformMissingInjectAnnotation.BadGuiceConfig( "b:", "bad" );
                    }
                } ) );
    }
}
