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

    /**
     * Given a list, return the first element
     */
    public static final class firstElement extends Function.ListFunction {

        @Override
        protected Optional applyList( final List argList ) {
            return argList.size() > 0 ?
                    Optional.of( argList.get( 0 ) ) :
                    Optional.empty();
        }
    }

    /**
     * Given a list, return the last element
     */
    public static final class lastElement extends Function.ListFunction {

        @Override
        protected Optional applyList( final List argList ) {
            return argList.size() > 0 ?
                    Optional.of( argList.get( argList.size() - 1 ) ) :
                    Optional.empty();
        }
    }

    /**
     * Given an index at arg[0], and a list at arg[1] or args[1...N], return element at index of list or array
     */
    public static final class elementAt extends Function.ArgDrivenListFunction<Integer> {

        @Override
        protected Optional<Object> applyList( final Integer specialArg, final List<Object> args ) {
            if ( specialArg != null && args != null && args.size() > specialArg ) {
                return Optional.of( args.get( specialArg ) );
            }
            return Optional.empty();
        }
    }

    /**
     * Given an arbitrary number of arguments, return them as list
     */
    public static final class toList extends Function.BaseFunction<List> {
        @Override
        protected Optional<Object> applyList( final List input ) {
            return Optional.<Object>of( input );
        }

        @Override
        protected Optional<List> applySingle( final Object arg ) {
            return Optional.<List>of( Arrays.asList( arg ) );
        }
    }

    /**
     * Given an arbitrary list of items, returns a new array of them in sorted state
     */
    public static final class sort extends Function.BaseFunction {

        @Override
        protected Optional applyList( final List argList ) {
            try {
                Object[] dest = argList.toArray();
                Arrays.sort( dest );
                return Optional.<Object>of( dest );
            }
            // if any of the elements are not Comparable<?> it'll throw a ClassCastException
            catch(Exception ignored) {
                return Optional.empty();
            }
        }

        @Override
        protected Optional applySingle( final Object arg ) {
            return Optional.of( arg );
        }
    }
}
