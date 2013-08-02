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
package com.bazaarvoice.jolt.chainr.transforms;

import com.bazaarvoice.jolt.SpecDriven;
import com.bazaarvoice.jolt.Transform;

import java.util.Map;

/**
 * Chainr should barf on this class, as it is a SpecTransform without a single arg constructor.
 * This class is reference from a JSON test fixture.
 */
public class BadSpecTransform implements SpecDriven, Transform {

    @Override
    public Object transform( Object input ) {
        return input;
    }
}
