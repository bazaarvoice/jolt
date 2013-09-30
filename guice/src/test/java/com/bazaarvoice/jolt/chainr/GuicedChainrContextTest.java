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
import com.bazaarvoice.jolt.chainr.transforms.GuiceContextDrivenTransform;
import com.bazaarvoice.jolt.chainr.transforms.GuiceSpecAndContextDrivenTransform;
import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.Provides;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class GuicedChainrContextTest {

    @DataProvider
    public Iterator<Object[]> getCases() throws IOException {

        String testPath = "/json/chainr/guice_spec_with_context.json";
        Map<String, Object> testSuite = JsonUtils.classpathToMap( testPath );

        Object spec = testSuite.get( "spec" );
        List<Map> tests = (List<Map>) testSuite.get( "tests" );

        List<Object[]> accum = Lists.newLinkedList();

        for ( Map testCase : tests ) {

            String testCaseName = (String) testCase.get( "testCaseName" );
            Object input = testCase.get( "input" );
            Map<String,Object> context = (Map<String,Object>) testCase.get( "context" );
            Object expected = testCase.get( "expected" );

            accum.add( new Object[] { testCaseName, spec, input, context, expected } );
        }

        return accum.iterator();
    }

    @Test( dataProvider = "getCases")
    public void successCases( String testCaseName, Object spec, Object input, Map<String,Object> context, Object expected ) throws IOException {

        Module parentModule = new AbstractModule() {
            @Override
            protected void configure() {
            }

            @Provides
            public GuiceContextDrivenTransform.GuiceConfig getConfigC() {
                return new GuiceContextDrivenTransform.GuiceConfig( "c", "cc" );
            }

            @Provides
            public GuiceSpecAndContextDrivenTransform.GuiceConfig getConfigD() {
                return new GuiceSpecAndContextDrivenTransform.GuiceConfig( "dd" );
            }
        };

        Chainr unit = Chainr.fromSpec( spec, new GuiceChainrInstantiator( parentModule ) );

        Assert.assertTrue( unit.hasContextualTransforms() );
        Assert.assertEquals( unit.getContextualTransforms().size(), 2 );

        Object actual = unit.transform( input, context );

        JoltTestUtil.runDiffy( "failed case " + testCaseName, expected, actual );
    }
}
