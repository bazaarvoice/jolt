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

import com.bazaarvoice.jolt.Transform;

import java.util.Map;

/**
 * This class wants a Guice injected param, but didn't mark it's constructor with @Inject.
 */
public class GuiceTransformMissingInjectAnnotation implements Transform {

    public static class BadGuiceConfig {
        private final String key, value;

        public BadGuiceConfig( String key, String value ) {
            this.key = key;
            this.value = value;
        }
    }

    private final BadGuiceConfig config;

    public GuiceTransformMissingInjectAnnotation( BadGuiceConfig config ) {
        this.config = config;
    }

    @Override
    public Object transform( Object input ) {

        ((Map) input).put( config.key, config.value );
        return input;
    }
}
