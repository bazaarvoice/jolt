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

/**
 * Interface to allow the guts of the Transform class loading logic to be swapped out.
 * This primarily exists to allow clients of Jolt to load their own custom Java Transforms
 *  via Guice or other dependency injection systems.
 */
public interface ChainrInstantiator {

    /**
     * Instantiate the Transform class specified by the ChainrEntry.
     */
    public JoltTransform hydrateTransform( ChainrEntry entry );
}
