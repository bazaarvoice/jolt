/*
 * Copyright 2013-2023 Bazaarvoice, Inc.
 * Copyright 2025 Jolt Community
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
package io.joltcommunity.jolt.modifier.function;

import io.joltcommunity.jolt.common.Optional;

import java.util.Arrays;
import java.util.List;
import java.util.regex.PatternSyntaxException;

@SuppressWarnings("deprecated")
public class Strings {

    private static Optional<Object> padString(boolean leftPad, String source, List<Object> args) {

        // There is only one path that leads to success and many
        //  ways for this to fail.   So using a do/while loop
        //  to make the bailing easy.
        do {

            if (source == null || args == null) {
                break;
            }

            if (!(args.get(0) instanceof Integer width &&
                    args.get(1) instanceof String filler)) {
                break;
            }

            // if the width param is stupid; bail
            if (width <= 0 || width > 500) {
                break;
            }

            // filler can only be a single char
            //  otherwise the math becomes hard
            if (filler.length() != 1) {
                break;
            }

            char fillerChar = filler.charAt(0);

            // if the desired width of the overall padding is smaller than
            //  the source string, then just return the source string.
            if (width <= source.length()) {
                return Optional.of(source);
            }

            int padLength = width - source.length();
            char[] padArray = new char[padLength];

            Arrays.fill(padArray, fillerChar);

            StringBuilder sb = new StringBuilder();

            if (leftPad) {
                sb.append(padArray).append(source);
            } else {
                sb.append(source).append(padArray);
            }

            return Optional.of(sb.toString());

        } while (false);

        return Optional.empty();
    }

    public static final class toLowerCase extends Function.SingleFunction<String> {
        @Override
        protected Optional<String> applySingle(final Object arg) {

            if (!(arg instanceof String argString)) {
                return Optional.empty();
            }

            return Optional.of(argString.toLowerCase());
        }
    }

    public static final class toUpperCase extends Function.SingleFunction<String> {
        @Override
        protected Optional<String> applySingle(final Object arg) {

            if (!(arg instanceof String argString)) {
                return Optional.empty();
            }

            return Optional.of(argString.toUpperCase());
        }
    }

    public static final class trim extends Function.SingleFunction<String> {
        @Override
        protected Optional<String> applySingle(final Object arg) {

            if (!(arg instanceof String argString)) {
                return Optional.empty();
            }

            return Optional.of(argString.trim());
        }
    }

    public static final class concat extends Function.ListFunction {
        @Override
        protected Optional<Object> applyList(final List<Object> argList) {
            StringBuilder sb = new StringBuilder();
            for (Object arg : argList) {
                if (arg != null) {
                    sb.append(arg.toString());
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
                if (argList == null || argList.size() != 3) {
                    break;
                }

                if (!(argList.get(0) instanceof String tuna &&
                        argList.get(1) instanceof Integer &&
                        argList.get(2) instanceof Integer)) {
                    break;
                }

                // If we get here, then all these casts should work.
                int start = (Integer) argList.get(1);
                int end = (Integer) argList.get(2);

                // do start and end make sense?
                if (start >= end || start < 0 || end < 1 || end > tuna.length()) {
                    break;
                }

                return Optional.of(tuna.substring(start, end));

            } while (false);

            // if we got here, then return an Optional.empty.
            return Optional.empty();
        }
    }

    @SuppressWarnings("unchecked")
    public static final class join extends Function.ArgDrivenListFunction<String> {

        @Override
        protected Optional<Object> applyList(final String specialArg, final List<Object> args) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < args.size(); i++) {
                Object arg = args.get(i);
                if (arg != null) {
                    String argString = arg.toString();
                    if (!("".equals(argString))) {
                        sb.append(argString);
                        if (i < args.size() - 1) {
                            sb.append(specialArg);
                        }
                    }
                }
            }
            return Optional.of(sb.toString());
        }
    }

    public static final class split extends Function.ArgDrivenSingleFunction<String, List> {
        @Override
        protected Optional<List> applySingle(final String separator, final Object source) {
            if (source == null || separator == null) {
                return Optional.empty();
            } else if (source instanceof String inputString) {
                // only try to split input strings
                return Optional.of(Arrays.asList(inputString.split(separator)));
            } else {
                return Optional.empty();
            }
        }
    }

    public static final class leftPad extends Function.ArgDrivenListFunction<String> {
        @Override
        protected Optional<Object> applyList(String source, List<Object> args) {

            return padString(true, source, args);
        }
    }

    public static final class rightPad extends Function.ArgDrivenListFunction<String> {
        @Override
        protected Optional<Object> applyList(String source, List<Object> args) {

            return padString(false, source, args);
        }
    }

    public static final class replace extends Function.ListFunction {
        @Override
        protected Optional<Object> applyList(List<Object> args) {

            // There is only one path that leads to success and many
            //  ways for this to fail.   So using a do/while loop
            //  to make the bailing easy.
            do {

                if (args.size() != 3) {
                    break;
                }

                if (!(args.get(0) instanceof String source &&
                        args.get(1) instanceof String target &&
                        args.get(2) instanceof String replacement)) {
                    break;
                }

                return Optional.of(source.replace(target, replacement));

            } while (false);

            return Optional.empty();
        }
    }

    public static final class replaceAll extends Function.ListFunction {
        @Override
        protected Optional<Object> applyList(List<Object> args) {

            // There is only one path that leads to success and many
            //  ways for this to fail.   So using a do/while loop
            //  to make the bailing easy.
            do {

                if (args.size() != 3) {
                    break;
                }

                if (!(args.get(0) instanceof String source &&
                        args.get(1) instanceof String regex &&
                        args.get(2) instanceof String replacement)) {
                    break;
                }

                try {
                    return Optional.of(source.replaceAll(regex, replacement));
                } catch (PatternSyntaxException e) {
                    // if the regex is invalid, we just return an empty Optional
                    break;
                }

            } while (false);

            return Optional.empty();
        }
    }
}
