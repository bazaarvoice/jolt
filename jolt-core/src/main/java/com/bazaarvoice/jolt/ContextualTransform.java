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

import java.util.Map;

/**
 * Interface for Jolt Transforms that can incorporate context information along with the
 * source input JSON.
 *
 * These Jolt Transforms should be stateless, thus allowing multiple threads to
 * call the transform method simultaneously.
 */
public interface ContextualTransform extends JoltTransform {

    /**
     * Execute a transform on some input JSON with optionally provided "context" and return the result.
     *
     * The "context" allows transforms to tweak their behavior based upon criteria outside of the input JSON object.
     *
     * The canonical example for the need to have Transforms consider "context" is a Transform that creates
     * urls based upon input data.  Should it generate "http" or "https" urls?
     *
     * Most likely the input JSON data does not provide any guidance. This is what the "context" is for.
     * It allows the consumer of the Transform to specialize itself based on data outside the scope of the input JSON.
     *
     * Without the "context" notion you would instead create a HttpUrlTransform and a HttpsUrlTransform.
     * This creates problems when you want to used them as part of a larger Chainr Transform, as you
     * would need to create two Chainrs that are almost the same.   The number of Chainrs needed grows
     * exponentially as you add other context sensitive transforms.
     *
     * @param input the JSON object to transform in plain vanilla Jackson Map<String, Object> style
     * @param context information outside of the input JSON that needs to be taken into account when doing the transform
     * @return the results of the transformation
     * @throws com.bazaarvoice.jolt.exception.TransformException if there are issues with the transform
     */
    Object transform( Object input, Map<String, Object> context );
}
