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

import java.util.List;

@SuppressWarnings( "deprecated" )
public class Strings {

    public static final class toLowerCase extends Function.SingleFunction<String> {
        @Override
        protected Optional<String> applySingle( final Object arg ) {
            return arg == null ? Optional.<String>of( null ) : Optional.of( arg.toString().toLowerCase() );
        }
    }

    public static final class toUpperCase extends Function.SingleFunction<String> {
        @Override
        protected Optional<String> applySingle( final Object arg ) {
            return arg == null ? Optional.<String>of( null ): Optional.of( arg.toString().toUpperCase() );
        }
    }
    public static final class trim extends Function.SingleFunction<String> {
        @Override
        protected Optional<String> applySingle( final Object arg ) {
            return arg == null ? Optional.<String>of( null ): Optional.of( arg.toString().trim());
        }
    }

    public static final class concat extends Function.ListFunction {
        @Override
        protected Optional<Object> applyList( final List<Object> argList ) {
            StringBuilder sb = new StringBuilder(  );
            for(Object arg: argList ) {
                if ( arg != null ) {
                    sb.append(arg.toString() );
                }
            }
            return Optional.<Object>of(sb.toString());
        }
    }

    @SuppressWarnings( "unchecked" )
    public static final class join extends Function.ArgDrivenListFunction<String> {

        @Override
        protected Optional<Object> applyList( final String specialArg, final List<Object> args ) {
            StringBuilder sb = new StringBuilder(  );
            for(int i=0; i < args.size(); i++) {
                Object arg = args.get(i);
                if (arg != null ) {
                    String argString = arg.toString();
                    if( !("".equals( argString ))) {
                        sb.append( argString );
                        if ( i < args.size() - 1 ) {
                            sb.append( specialArg );
                        }
                    }
                }
            }
            return Optional.<Object>of( sb.toString() );
        }
    }

    public static final class remove extends Function.ArgDrivenSingleFunction<String,String> {

        @Override
        protected Optional<String> applySingle( String specialArg, Object arg )
        {

            return arg == null ? Optional.<String>of( null ) : Optional.of( arg.toString().replace( specialArg,"" ));
        }
    }

    public static final class replace extends Function.ArgDrivenSingleFunction<String,String> {

        @Override
        protected Optional<String> applySingle( String specialArg, Object arg )
        {

            if (specialArg != null && !specialArg.isEmpty()) {
                String[] args = specialArg.split( "(?<!\\\\)\\|" );
                if (args != null && args.length >1) {
                    return Optional.of( arg.toString().replace( args[0],args[1] ) );
                }
            }
            return Optional.<String>of( null );
        }
    }

//    public static final class replace extends Function.ArgDrivenSingleFunction<List<String>,String> {
//
//        @Override
//        protected Optional<String> applySingle( List<String> specialArg, Object arg )
//        {
//
//            if (arg == null) {
//                return Optional.of( null );
//
//            } else {
//                 String _args = arg.toString();
//                 if (!_args.isEmpty()) {
//                     if (!specialArg.isEmpty()) {
//                         String one = specialArg.get( 0 );
//                         String two = "";
//                         if (specialArg.size() > 1) {
//                             two = specialArg.get( 1 );
//                         }
//                         return Optional.of( _args.replace( one,two ));
//                     }
//
//                 } else {
//                     return Optional.of( "" );
//                 }
//
//            }
//
//            return Optional.of( null );
//        }
//
//    }
}
