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

    public static final class concat extends Function.ListFunction {
        @Override
        protected Optional<Object> applyList( final List<Object> argList ) {
            StringBuilder sb = new StringBuilder(  );
            for(Object arg: argList ) {
                if ( arg != null ) {
                    sb.append(arg.toString() );
                }
            }
            return Optional.of(sb.toString());
        }
    }

    public static final class substring extends Function.ListFunction {

        @Override
        protected Optional<Object> applyList(List<Object> argList) {

            // There is only one path that leads to success and many
            //  ways for this to fail.   So using a do/while loop
            //  to make the bailing easy.
            do {

                // if argList is null or not the right size; bail
                if(argList == null || argList.size() != 3 ) {
                    break;
                }

                if ( ! ( argList.get(0) instanceof String &&
                         argList.get(1) instanceof Integer &&
                         argList.get(2) instanceof Integer ) ) {
                    break;
                }

                // If we get here, then all these casts should work.
                String tuna = (String) argList.get(0);
                int start = (Integer) argList.get(1);
                int end = (Integer) argList.get(2);

                // do start and end make sense?
                if ( start >= end || start < 0 || end < 1 || end > tuna.length() ) {
                    break;
                }

                return Optional.of(tuna.substring(start, end));

            } while( false );

            // if we got here, then return an Optional.empty.
            return Optional.empty();
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
            return Optional.of( sb.toString() );
        }
    }

    public static final class split extends Function.ArgDrivenSingleFunction<String, List> {
      @Override
      protected Optional<List> applySingle(final String separator, final Object source) {
        if (source == null || separator == null) {
          return Optional.empty();
        }
        else if ( source instanceof String ) {
          // only try to split input strings
          String inputString = (String) source;
          return Optional.of( Arrays.asList(inputString.split(separator)) );
        }
        else {
          return Optional.empty();
        }
      }
    }

    public static class pad extends Function.ArgDrivenListFunction<String>{
        @Override
        protected Optional<Object> applyList(String filler, List<Object> args) {

            if(filler == null || args == null) {
                return Optional.of("");
            } else {
                if(filler.length() == 1 && args.size() == 2) {
                    Object arg1 = args.get(0);
                    Object arg2 = args.get(1);
                    if(arg1 instanceof Integer && arg2 instanceof String) {
                        return leftPad(filler, args);
                    }
                    else if(arg1 instanceof String && arg2 instanceof Integer) {
                        args.set(0, arg2);
                        args.set(1, arg1);
                        return rightPad(filler, args);
                    }
                }
            }
            return Optional.of("");
        }

        Optional<Object> leftPad(String filler, List<Object> args) {
            if(filler == null || args == null) {
                return Optional.of("");
            } else {
                Object arg1 = args.get(0);
                Object arg2 = args.get(1);
                if(arg1 instanceof Integer && arg2 instanceof String) {
                    int width = (Integer) arg1;
                    String source = (String) arg2;
                    if(source.length() >= width) {
                        return Optional.of(source);
                    } else {
                        char[] sourceArray = source.toCharArray();
                        char[] destinationArray = new char[width];
                        int destIndex = width - 1;
                        for(int i = sourceArray.length - 1; i >= 0; i--) {
                            destinationArray[destIndex] = sourceArray[i];
                            destIndex--;
                        }
                        for(int i = destIndex; i >= 0; i--) {
                            destinationArray[i] = filler.charAt(0);
                        }
                        return Optional.of(new String(destinationArray));
                    }
                }
            }
            return Optional.of("");
        }

        Optional<Object> rightPad(String filler, List<Object> args) {
            if(filler == null || args == null) {
                return Optional.of("");
            } else {
                Object arg1 = args.get(0);
                Object arg2 = args.get(1);
                if(arg1 instanceof Integer && arg2 instanceof String) {
                    int width = (Integer) arg1;
                    String source = (String) arg2;
                    if(source.length() >= width) {
                        return Optional.of(source);
                    } else {
                        char[] sourceArray = source.toCharArray();
                        char[] destinationArray = new char[width];
                        int destIndex = 0;
                        for(int i = 0; i <= sourceArray.length - 1; i++) {
                            destinationArray[destIndex] = sourceArray[i];
                            destIndex++;
                        }
                        for(int i = destIndex; i <= width - 1; i++) {
                            destinationArray[i] = filler.charAt(0);
                        }
                        return Optional.of(new String(destinationArray));
                    }
                }
            }
            return Optional.of("");
        }
    }

    public static final class leftPad extends pad {
        @Override
        protected Optional<Object> applyList(String filler, List<Object> args) {
            return leftPad(filler, args);
        }
    }

    public static final class rightPad extends pad {
        @Override
        protected Optional<Object> applyList(String filler, List<Object> args) {
            return rightPad(filler, args);
        }
    }
}
