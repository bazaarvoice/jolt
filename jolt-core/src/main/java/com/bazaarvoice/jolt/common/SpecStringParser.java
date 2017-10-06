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

package com.bazaarvoice.jolt.common;

import com.bazaarvoice.jolt.exception.SpecException;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Static utility methods for handling specStrings such that we can process them into
 * usable formats for further processing into PathElement objects
 */
public class SpecStringParser {


    private SpecStringParser() {}

    /**
     * Method that recursively parses a dotNotation String based on an iterator.
     *
     * This method will call out to parseAtPathElement
     *
     * @param pathStrings List to store parsed Strings that each represent a PathElement
     * @param iter the iterator to pull characters from
     * @param dotNotationRef the original dotNotation string used for error messages
     * @return evaluated List<String> from dot notation string spec
     */
    public static List<String> parseDotNotation( List<String> pathStrings, Iterator<Character> iter,
                                                 String dotNotationRef ) {

        if ( ! iter.hasNext() ) {
            return pathStrings;
        }

        // Leave the forward slashes, unless it precedes a "."
        // The way this works is always suppress the forward slashes, but add them back in if the next char is not a "."

        boolean prevIsEscape = false;
        boolean currIsEscape = false;
        StringBuilder sb = new StringBuilder();

        char c;
        while( iter.hasNext() ) {

            c = iter.next();

            currIsEscape = false;
            if ( c == '\\' && ! prevIsEscape ) {
                // current is Escape only if the char is escape, or
                //  it is an Escape and the prior char was, then don't consider this one an escape
                currIsEscape = true;
            }

            if ( prevIsEscape && c != '.' && c != '\\') {
                sb.append( '\\' );
                sb.append( c );
            }
            else if( c == '@' ) {
                sb.append( '@' );
                sb.append( parseAtPathElement( iter, dotNotationRef ) );

                //                      there was a "[" seen       but no "]"
                boolean isPartOfArray = sb.indexOf( "[" ) != -1 && sb.indexOf( "]" ) == -1;
                if ( ! isPartOfArray ) {
                    pathStrings.add( sb.toString() );
                    sb = new StringBuilder();
                }
            }
            else if ( c == '.' ) {

                if ( prevIsEscape ) {
                    sb.append( '.' );
                }
                else {
                    if ( sb.length() != 0 ) {
                        pathStrings.add( sb.toString() );
                    }
                    return parseDotNotation( pathStrings, iter, dotNotationRef );
                }
            }
            else if ( ! currIsEscape ) {
                sb.append( c );
            }

            prevIsEscape = currIsEscape;
        }

        if ( sb.length() != 0 ) {
            pathStrings.add( sb.toString() );
        }
        return pathStrings;
    }

    /**
     * Helper method to turn a String into an Iterator<Character>
     */
    public static Iterator<Character> stringIterator(final String string) {
        // Ensure the error is found as soon as possible.
        if (string == null)
            throw new NullPointerException();

        return new Iterator<Character>() {
            private int index = 0;

            public boolean hasNext() {
                return index < string.length();
            }

            public Character next() {

                // Throw NoSuchElementException as defined by the Iterator contract,
                // not IndexOutOfBoundsException.
                if (!hasNext())
                    throw new NoSuchElementException();
                return string.charAt(index++);
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * Given a dotNotation style outputPath like "data[2].&(1,1)", this method fixes the syntactic sugar
     * of "data[2]" --> "data.[2]"
     *
     * This makes all the rest of the String processing easier once we know that we can always
     * split on the '.' character.
     *
     * @param dotNotaton Output path dot notation
     * @return
     */
    // TODO Unit Test this
    public static String fixLeadingBracketSugar( String dotNotaton ) {

        if ( dotNotaton == null || dotNotaton.length() == 0 ) {
            return "";
        }

        char prev = dotNotaton.charAt( 0 );
        StringBuilder sb = new StringBuilder();
        sb.append( prev );

        for ( int index = 1; index < dotNotaton.length(); index++ ) {
            char curr =  dotNotaton.charAt( index );

            if ( curr == '[' && prev != '\\') {
                if ( prev == '@' || prev == '.' ) {
                    // no need to add an extra '.'
                }
                else {
                    sb.append( '.' );
                }
            }

            sb.append( curr );
            prev = curr;
        }

        return sb.toString();
    }

    /**
     * Parse RHS Transpose @ logic.
     * "@(a.b)"  --> pulls "(a.b)" off the iterator
     * "@a.b"    --> pulls just "a" off the iterator
     *
     * This method expects that the the '@' character has already been seen.
     *
     * @param iter iterator to pull data from
     * @param dotNotationRef the original dotNotation string used for error messages
     */
    // TODO Unit Test this
    public static String parseAtPathElement( Iterator<Character> iter, String dotNotationRef ) {

        if ( ! iter.hasNext() ) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        // Strategy here is to walk thru the string looking for matching parenthesis.
        // '(' increments the count, while ')' decrements it
        // If we ever get negative there is a problem.
        boolean isParensAt = false;
        int atParensCount = 0;

        char c = iter.next();
        if ( c == '(' ) {
            isParensAt = true;
            atParensCount++;
        }
        else if ( c == '.' ) {
            throw new SpecException( "Unable to parse dotNotation, invalid TransposePathElement : " + dotNotationRef );
        }

        sb.append( c );

        while( iter.hasNext() ) {
            c = iter.next();
            sb.append( c );

            // Parsing "@(a.b.[&2])"
            if ( isParensAt ) {
                if ( c == '(' ) {
                    throw new SpecException( "Unable to parse dotNotation, too many open parens '(' : " + dotNotationRef );
                }
                else if ( c == ')' ) {
                    atParensCount--;
                }

                if ( atParensCount == 0 ) {
                    return sb.toString();
                }
                else if ( atParensCount < 0 ) {
                    throw new SpecException( "Unable to parse dotNotation, specifically the '@()' part : " + dotNotationRef );
                }
            }
            // Parsing "@abc.def, return a canonical form of "@(abc)" and leave the "def" in the iterator
            else if ( c == '.' ) {
                return "(" + sb.toString().substring( 0, sb.length() - 1 ) + ")";
            }
        }

        // if we got to the end of the String and we have mismatched parenthesis throw an exception.
        if ( isParensAt && atParensCount != 0 ) {
            throw new SpecException( "Invalid @() pathElement from : " + dotNotationRef );
        }
        // Parsing "@abc"
        return sb.toString();
    }

    // Visible for Testing
    // given "\@pants" -> "pants"                 starts with escape
    // given "rating-\&pants" -> "rating-pants"   escape in the middle
    // given "rating\\pants" -> "ratingpants"     escape the escape char
    public static String removeEscapedValues(String origKey) {
        StringBuilder sb = new StringBuilder();

        boolean prevWasEscape = false;
        for ( char c : origKey.toCharArray() ) {
            if ( '\\' == c ) {
                if ( prevWasEscape ) {
                    prevWasEscape = false;
                }
                else {
                    prevWasEscape = true;
                }
            }
            else {
                if ( ! prevWasEscape ) {
                    sb.append( c );
                }
                prevWasEscape = false;
            }
        }

        return sb.toString();
    }

    // Visible for Testing
    // given "\@pants" -> "@pants"                 starts with escape
    // given "rating-\&pants" -> "rating-&pants"   escape in the middle
    // given "rating\\pants" -> "rating\pants"     escape the escape char
    public static String removeEscapeChars( String origKey ) {
        StringBuilder sb = new StringBuilder();

        boolean prevWasEscape = false;
        for ( char c : origKey.toCharArray() ) {
            if ( '\\' == c ) {
                if ( prevWasEscape ) {
                    sb.append( c );
                    prevWasEscape = false;
                }
                else {
                    prevWasEscape = true;
                }
            }
            else {
                sb.append( c );
                prevWasEscape = false;
            }
        }

        return sb.toString();
    }

    public static List<String> parseFunctionArgs(String argString) {
        List<String> argsList = new LinkedList<>(  );
        int firstBracket = argString.indexOf( '(' );

        String className = argString.substring( 0, firstBracket );
        argsList.add( className );

        // drop the first and last ( )
        argString = argString.substring( firstBracket + 1, argString.length() - 1 );

        StringBuilder sb = new StringBuilder( );
        boolean inBetweenBrackets = false;
        boolean inBetweenQuotes = false;
        for (int i = 0; i < argString.length(); i++){
            char c = argString.charAt(i);
            switch ( c ) {
                case '(':
                    if (!inBetweenQuotes) {
                        inBetweenBrackets = true;
                    }
                    sb.append( c );
                    break;
                case ')':
                    if (!inBetweenQuotes) {
                        inBetweenBrackets = false;
                    }
                    sb.append( c );
                    break;
                case '\'':
                    inBetweenQuotes = !inBetweenQuotes;
                    sb.append( c );
                    break;
                case ',':
                    if ( !inBetweenBrackets && !inBetweenQuotes ) {
                        argsList.add( sb.toString().trim() );
                        sb = new StringBuilder();
                        break;
                    }
                default:
                    sb.append( c );
                    break;
            }
        }

        argsList.add( sb.toString().trim() );
        return argsList;
    }
}
