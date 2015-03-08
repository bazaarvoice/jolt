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

import java.lang.reflect.Constructor;

/**
 * Loads classes via Java Reflection APIs.
 */
public class DefaultChainrInstantiator implements ChainrInstantiator {

    @Override
    public JoltTransform hydrateTransform( ChainrEntry entry ) {

        Object spec = entry.getSpec();
        Class<? extends JoltTransform> transformClass = entry.getJoltTransformClass();

        try {
            // If the transform class is a SpecTransform, we try to construct it with the provided spec.
            if ( entry.isSpecDriven() ) {

                try {
                    // Lookup a Constructor with a Single "Object" arg.
                    Constructor constructor = transformClass.getConstructor( Object.class );

                    return (JoltTransform) constructor.newInstance( spec );

                } catch ( NoSuchMethodException nsme ) {
                    // This means the transform class "violated" the SpecTransform marker interface
                    throw new SpecException( "JOLT Chainr encountered an exception constructing SpecTransform className:" + transformClass.getCanonicalName() +
                            ".  Specifically, no single arg constructor found" + entry.getErrorMessageIndexSuffix(), nsme );
                }
            }
            else {
                // The opClass is just a Transform, so just create a newInstance of it.
                return transformClass.newInstance();
            }
        } catch ( Exception e ) {
            // FYI 3 exceptions are known to be thrown here
            // IllegalAccessException, InvocationTargetException, InstantiationException
            throw new SpecException( "JOLT Chainr encountered an exception constructing Transform className:"
                    + transformClass.getCanonicalName() + entry.getErrorMessageIndexSuffix(), e );
        }
    }
}
