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

package com.bazaarvoice.jolt.modifier.function;

import com.bazaarvoice.jolt.common.Optional;

@SuppressWarnings( "deprecated" )
public class Strings {

    public static final class toLowerCase implements Function {
        @Override
        public Optional<Object> apply( final Object... args ) {
            if(args.length == 0) {
                return Optional.empty();
            }
            Object arg = args[0];
            if(arg != null) {
                return Optional.<Object>of( args[0].toString().toLowerCase());
            }
            else {
                return Optional.of( null );
            }
        }
    }

    public static final class toUpperCase implements Function {
        @Override
        public Optional<Object> apply( final Object... args ) {
            if(args.length == 0) {
                return Optional.empty();
            }
            Object arg = args[0];
            if(arg != null) {
                return Optional.<Object>of(args[0].toString().toUpperCase());
            }
            else {
                return Optional.of( null );
            }
        }
    }

    public static final class concat implements Function {
        @Override
        public Optional<Object> apply( final Object... args ) {
            if(args.length == 0) {
                return Optional.empty();
            }
            StringBuilder sb = new StringBuilder(  );
            for(Object arg: args) {
                if ( arg != null ) {
                    sb.append(arg.toString() );
                }
            }
            return Optional.<Object>of(sb.toString());
        }
    }
}
