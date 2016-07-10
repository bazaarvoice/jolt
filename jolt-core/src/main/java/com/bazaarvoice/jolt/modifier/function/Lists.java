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

import java.util.Arrays;
import java.util.List;

@SuppressWarnings( "deprecated" )
public class Lists {

    public static final class firstElement implements Function {
        @Override
        public Optional<Object> apply( final Object... args ) {
            if(args.length == 1 && args[0] instanceof List) {
                List input = (List)args[0];
                if(input.size() > 0) {
                    return Optional.of( input.get( 0 ) );
                }
            }
            return Optional.empty();
        }
    }

    public static final class lastElement implements Function {
        @Override
        public Optional<Object> apply( final Object... args ) {
            if(args.length == 1 && args[0] instanceof List) {
                List input = (List)args[0];
                if(input.size() > 0) {
                    return Optional.of( input.get( input.size() - 1 ) );
                }
            }
            return Optional.empty();
        }
    }

    public static final class elementAt implements Function {
        @Override
        public Optional<Object> apply( final Object... args ) {
            if ( args.length == 2 && args[0] instanceof List && args[1] instanceof Integer ) {
                int index = (int) args[1];
                List input = (List) args[0];
                if ( input.size() > index ) {
                    return Optional.of( input.get( index ) );
                }
            }
            return Optional.empty();
        }
    }

    public static final class toList implements Function {
        @Override
        public Optional<Object> apply( final Object... args ) {
            if(args.length == 0) {
                return Optional.empty();
            }
            else {
                return Optional.<Object>of( Arrays.asList( args ));
            }
        }
    }
}
