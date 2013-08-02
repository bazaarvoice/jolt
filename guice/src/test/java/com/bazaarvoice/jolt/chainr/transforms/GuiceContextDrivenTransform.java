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

import javax.inject.Inject;
import java.util.Map;

public class GuiceContextDrivenTransform implements ContextualTransform {

    public static final String CONTEXT_KEY = "suffix";

    private final GuiceConfig guiceConfig;   // Value we get form Guice

    public static class GuiceConfig {
        private final String key, value;

        public GuiceConfig( String key, String value ) {
            this.key = key;
            this.value = value;
        }
    }

    @Inject
    public GuiceContextDrivenTransform( GuiceConfig guiceConfig ) {
        this.guiceConfig = guiceConfig;
    }

    @Override
    public Object transform( Object input, Map<String, Object> context ) {

        String suffix = (String) context.get( CONTEXT_KEY );

        ( (Map) input ).put( guiceConfig.key, guiceConfig.value + suffix );

        return input;
    }
}
