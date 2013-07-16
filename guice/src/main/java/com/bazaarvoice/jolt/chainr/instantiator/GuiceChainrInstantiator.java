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

import com.bazaarvoice.jolt.JoltTransform;
import com.bazaarvoice.jolt.chainr.spec.ChainrEntry;
import com.bazaarvoice.jolt.exception.SpecException;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

/**
 * This class allows Transforms specified in Chainr spec files to be loaded via Guice.
 * This is primarily used for Custom Java Transforms that need additional information.
 * <p/>
 * It requires a GuiceModule, because it creates an Injector for each SpecTransform it
 * processes.
 * <p/>
 * If your app is using Guice, you can either pass in your "existing" Module (if you can),
 * or create a Guice Module with just the dependencies that the Transforms require.
 * The latter approach keeps scope contained, and make it easier to unit test your Transforms.
 * <p/>
 * Note this does require that you "know" what Transforms you are using and what they depend on.
 */
public class GuiceChainrInstantiator implements ChainrInstantiator {

    private final Module parentModule;
    private final Injector nonSpecInjector;

    /**
     * @param parentModule Guice module that will be used to create an injector to instantiate Transform classes
     */
    public GuiceChainrInstantiator( Module parentModule ) {
        this.parentModule = parentModule;
        this.nonSpecInjector = Guice.createInjector( parentModule );
    }

    @Override
    public JoltTransform hydrateTransform( ChainrEntry entry ) {

        final Class<? extends JoltTransform> transformClass = entry.getJoltTransformClass();
        final Object transformSpec = entry.getSpec();

        try {

            if ( entry.isSpecDriven() ) {

                // In order to inject an "Object" into the constructor of a SpecTransform, we create an Injector just for this class.
                Injector injector;
                injector = Guice.createInjector( new AbstractModule() {
                    @Override
                    protected void configure() {

                        // install the parent module so that Custom Java Transforms or Templates can have @Injected properties filled in
                        install( parentModule );

                        // Bind the "spec" for the transform
                        bind( Object.class ).toInstance( transformSpec );
                    }
                } );

                return injector.getInstance( transformClass );

            } else {
                // else normal no-op constructor OR non-spec constructor with @Inject annotation
                return nonSpecInjector.getInstance( transformClass );
            }

        }
        catch ( Exception creationException ) {
            throw new SpecException( "Exception using Guice to initialize class:" + transformClass.getCanonicalName() + entry.getErrorMessageIndexSuffix(), creationException );
        }
    }
}
