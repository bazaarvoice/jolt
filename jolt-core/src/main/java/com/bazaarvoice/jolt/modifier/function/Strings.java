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
import java.util.OptionalInt;

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

    public static final class split extends Function.ArgDrivenSingleFunction<String, List> {
      @Override
      protected Optional<List> applySingle(final String separator, final Object source) {
        if (source == null || separator == null) {
          return Optional.empty();
        } else {
          return Optional.<List>of( Arrays.asList(source.toString().split(separator)) );
        }
      }
    }

    public static final class pad extends Function.ArgDrivenListFunction<String>{
        @Override
        protected Optional<Object> applyList(String filler, List<Object> args) {

            if(filler == null || args == null) {
                return Optional.of("");
            } else {
                if(filler.length() == 1 && args.size() == 2) {
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
                    } else if(arg1 instanceof String && arg2 instanceof Integer) {
                        String source = (String) arg1;
                        int width = (Integer) arg2;
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
            }
            return Optional.of("");
        }
    }
}
