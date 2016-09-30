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
import com.bazaarvoice.jolt.JoltTransform;
import com.bazaarvoice.jolt.chainr.instantiator.ChainrInstantiator;
import com.bazaarvoice.jolt.chainr.instantiator.DefaultChainrInstantiator;
import com.bazaarvoice.jolt.chainr.spec.ChainrEntry;
import com.bazaarvoice.jolt.chainr.spec.ChainrSpec;

import java.util.ArrayList;
import java.util.List;

public class ChainrBuilder {

    private final Object chainrSpecObj;
    protected ChainrInstantiator chainrInstantiator = new DefaultChainrInstantiator();
    private ClassLoader classLoader = ChainrBuilder.class.getClassLoader();

    /**
     * Initialize a Chainr to run a list of Transforms.
     * This is the constructor most "production" usages of Chainr should use.
     *
     * @param chainrSpecObj List of transforms to run
     */
    public ChainrBuilder( Object chainrSpecObj ) {
        this.chainrSpecObj = chainrSpecObj;
    }

    /**
     * Set a ChainrInstantiator to use when instantiating Transform Objects.
     * If one is not set, defaults to DefaultChainrInstantiator;
     *
     * @param loader ChainrInstantiator to use load Transforms
     */
    public ChainrBuilder loader( ChainrInstantiator loader ) {

        if ( loader == null ) {
            throw new IllegalArgumentException( "ChainrBuilder requires a non-null loader." );
        }

        this.chainrInstantiator = loader;
        return this;
    }

    public ChainrBuilder withClassLoader( ClassLoader classLoader ) {
        if ( classLoader == null ) {
            throw new IllegalArgumentException( "ChainrBuilder requires a non-null classLoader." );
        }
        this.classLoader = classLoader;
        return this;
    }

    public Chainr build() {
        ChainrSpec chainrSpec = new ChainrSpec( chainrSpecObj, classLoader );
        List<JoltTransform> transforms = new ArrayList<>( chainrSpec.getChainrEntries().size() );
        for ( ChainrEntry entry : chainrSpec.getChainrEntries() ) {

            JoltTransform transform = chainrInstantiator.hydrateTransform( entry );
            transforms.add( transform );
        }

        return new Chainr( transforms );
    }
}
