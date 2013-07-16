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

import com.bazaarvoice.jolt.ContextualTransform;
import com.bazaarvoice.jolt.SpecDriven;

import javax.inject.Inject;
import java.util.Map;

public class GoodSpecAndContextDrivenTransform implements SpecDriven, ContextualTransform {

    public static final String CONTEXT_KEY = "test_context_key_2";

    private static final String SPEC_DRIVEN_KEY = "KEY_TO_ADD";

    private final String specKeyValue;

    @Inject
    public GoodSpecAndContextDrivenTransform( Object spec ) {
        specKeyValue = (String) ((Map) spec).get( SPEC_DRIVEN_KEY );
    }

    @Override
    public Object transform( Object input, Map<String, Object> context ) {

        String contextValue = (String) context.get( CONTEXT_KEY );

        ((Map) input).put( specKeyValue, contextValue );

        return input;
    }
}
