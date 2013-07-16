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
package com.bazaarvoice.jolt.chainr.instantiator;

import com.bazaarvoice.jolt.Transform;
import com.bazaarvoice.jolt.chainr.spec.ChainrEntry;
import com.bazaarvoice.jolt.exception.SpecException;
import com.google.inject.AbstractModule;
import com.google.inject.CreationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

/**
 * This class allows Transforms specifed in Chainr spec files to be loaded via Guice.
 * This is primarily used for Custom Java Transforms that need additional information.
 *
 * It requires a GuiceModule, because it creates an Injector foreach SpecTransform it
 * processes.
 *
 * If your app is using Guice, you probably have a rather large dependency graph.  The
 * idea is that clients would create a Guice Module with just the dependencies that
 * the Transform requires, so as to keep scope contained, and make it easier to
 * unit test the transforms.
 */
public class GuiceChainrInstantiator implements ChainrInstantiator {

    private final Module parentModule;
    private final Injector nonSpecInjector;

    public GuiceChainrInstantiator ( Module parentModule ) {
        this.parentModule = parentModule;
        this.nonSpecInjector = Guice.createInjector( parentModule );
    }

    public Transform hydrateTransform( ChainrEntry entry ) {

        final Class<? extends Transform> transformClass = entry.getTransformClass();
        final Object transformSpec = entry.getSpec();

        if ( entry.isSpecDriven() ) {

            // In order to inject an "Object" into the constructor of a SpecTransform, we create an Injector just for this class.
            Injector injector;
            try {
                injector = Guice.createInjector( new AbstractModule() {
                    @Override
                    protected void configure() {

                        // install the parent module so that Custom Java Transforms or Templates can have @Injected properties filled in
                        install( parentModule );

                        // Bind the "spec" for the transform
                        bind( Object.class ).toInstance( transformSpec );
                    }
                } );
            } catch ( CreationException creationException ) {
                throw new SpecException( "Exception using Guice to initialize class:" + transformClass.getCanonicalName(), creationException );
            }

            return injector.getInstance( transformClass );

        } else {
            // else normal no-op constructor OR non-spec constructor with @Inject annotation
            return nonSpecInjector.getInstance( transformClass );
        }
    }
}