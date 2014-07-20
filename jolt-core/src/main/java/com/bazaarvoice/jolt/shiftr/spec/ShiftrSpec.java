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
package com.bazaarvoice.jolt.shiftr.spec;

import com.bazaarvoice.jolt.common.pathelement.*;
import com.bazaarvoice.jolt.exception.SpecException;
import com.bazaarvoice.jolt.common.WalkedPath;
import com.bazaarvoice.jolt.utils.StringTools;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;

/**
 * A Spec Object represents a single line from the JSON Shiftr Spec.
 *
 * At a minimum a single Spec has :
 *   Raw LHS spec value
 *   Some kind of PathElement (based off that raw LHS value)
 *
 * Additionally there are 2 distinct subclasses of the base Spec
 *  LeafSpec : where the RHS is a String or Array of Strings, that specify an write path for the data from this level in the tree
 *  CompositeSpec : where the RHS is a map of children Specs
 *
 * Mapping of JSON Shiftr Spec to Spec objects :
 * {
 *   rating-*" : {      // CompositeSpec with one child and a Star PathElement
 *     "&(1)" : {       // CompositeSpec with one child and a Reference PathElement
 *       "foo: {        // CompositeSpec with one child and a Literal PathElement
 *         "value" : "Rating-&1.value"  // OutputtingSpec with a Literal PathElement and one write path
 *       }
 *     }
 *   }
 * }
 *
 * The tree structure of formed by the CompositeSpecs is what is used during Shiftr transforms
 *  to do the parallel tree walk with the input data tree.
 *
 * During the parallel tree walk, a Path<Literal PathElements> is maintained, and used when
 *  a tree walk encounters an Outputting spec to evaluate the wildcards in the write DotNotationPath.
 */
public abstract class ShiftrSpec {

    // The processed key from the JSON config
    protected final MatchablePathElement pathElement;

    public ShiftrSpec(String rawJsonKey) {

        PathElement pe = parseSingleKeyLHS( rawJsonKey );

        if ( ! ( pe instanceof MatchablePathElement ) ) {
            throw new SpecException( "Spec LHS key=" + rawJsonKey + " is not a valid LHS key." );
        }

        this.pathElement = (MatchablePathElement) pe;
    }

    /**
     * Visible for Testing.
     *
     * Inspects the key in a particular order to determine the correct sublass of
     *  PathElement to create.
     *
     * @param key String that should represent a single PathElement
     * @return a concrete implementation of PathElement
     */
    public static PathElement parseSingleKeyLHS( String key )  {

        if ( "@".equals( key ) ) {
            return new AtPathElement( key );
        }
        else if ( key.startsWith("@") ) {
            return new TransposePathElement( key );
        }
        else if ( key.contains( "@" ) ) {
            throw new SpecException( "Invalid key:" + key  + " can not have an @ other than at the front." );
        }
        else if ( key.contains("$") ) {
            return new DollarPathElement( key );
        }
        else if ( key.contains("[") ) {

            if ( StringTools.countMatches(key, "[") != 1 || StringTools.countMatches(key, "]") != 1 ) {
                throw new SpecException( "Invalid key:" + key + " has too many [] references.");
            }

            return new ArrayPathElement( key );
        }
        else if ( key.contains( "&" ) ) {

            if ( key.contains("*") )
            {
                throw new SpecException("Can't mix * with & ) ");
            }
            return new AmpPathElement( key );
        }
        else if ( "*".equals( key ) ) {
            return new StarAllPathElement( key );
        }
        else if (key.contains("*" ) ) {

            int numOfStars = StringTools.countMatches(key, "*");

            if(numOfStars == 1){
                return new StarSinglePathElement( key );
            }
            else if(numOfStars == 2){
                return new StarDoublePathElement( key );
            }
            else {
                return new StarRegexPathElement( key );
            }
        }
        else {
            return new LiteralPathElement( key );
        }
    }


    /**
     * Helper method to turn a String into an Iterator<Character>
     */
    private static Iterator<Character> stringIterator(final String string) {
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
    private static String fixLeadingBracketSugar( String dotNotaton ) {

        if ( dotNotaton == null || dotNotaton.length() == 0 ) {
            return "";
        }

        char prev = dotNotaton.charAt( 0 );
        StringBuilder sb = new StringBuilder();
        sb.append( prev );

        for ( int index = 1; index < dotNotaton.length(); index++ ) {
            char curr =  dotNotaton.charAt( index );

            if ( curr == '[' ) {
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
     * "@(a.b)" or
     * "@a.b
     *
     * This method expects that the the '@' character has already been seen.
     *
     * @param iter iterator to pull data from
     * @param dotNotationRef the original dotNotation string used for error messages
     */
    private static String parseAtPathElement( Iterator<Character> iter, String dotNotationRef ) {

        if ( ! iter.hasNext() ) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        boolean isParensAt = false;
        int atParensCount = 0;

        char c = iter.next();
        if ( c == '(' ) {
            isParensAt = true;
            atParensCount++;
        }
        else if ( c == '.' ) {
            return "";
        }

        sb.append( c );

        while( iter.hasNext() ) {
            c = iter.next();
            sb.append( c );

            // Parsing "@(a.b.[&2])"
            if ( isParensAt ) {
                if ( c == '(' ) {
                    atParensCount++;
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
            // Parsing "@abc.def
            else if ( c == '.' ) {
                return sb.toString();
            }
        }

        // if we got to the end of the String and we have mismatched parenthesis throw an exception.
        if ( isParensAt && atParensCount != 0 ) {
            throw new SpecException( "Invalid @() pathElement from : " + dotNotationRef );
        }
        // Parsing "@abc"
        return sb.toString();
    }

    /**
     * Method that recursively parses a dotNotation String based on an iterator.
     *
     * This method will call out to parseAtPathElement
     *
     * @param pathStrings List to store parsed Strings that each represent a PathElement
     * @param iter the iterator to pull characters from
     * @param dotNotationRef the original dotNotation string used for error messages
     * @return
     */
    private static List<String> parseDotNotation( List<String> pathStrings, Iterator<Character> iter, String dotNotationRef ) {

        if ( ! iter.hasNext() ) {
            return pathStrings;
        }

        StringBuilder sb = new StringBuilder();

        char c;
        while( iter.hasNext() ) {

            c = iter.next();

            if( c == '@' ) {
                sb.append( '@' );
                sb.append( parseAtPathElement( iter, dotNotationRef ) );
                pathStrings.add( sb.toString() );
                sb = new StringBuilder();
            }
            else {
                if ( c == '.' ) {
                    if ( sb.length() != 0 ) {
                        pathStrings.add( sb.toString() );
                    }
                    return parseDotNotation( pathStrings, iter, dotNotationRef );
                }

                sb.append( c );
            }
        }

        if ( sb.length() != 0 ) {
            pathStrings.add( sb.toString() );
        }
        return pathStrings;
    }

    /**
     * @param refDotNotation the original dotNotation string used for error messages
     * @return List of PathElements based on the provided List<String> keys
     */
    private static List<PathElement> parseList( List<String> keys, String refDotNotation ) {
        ArrayList<PathElement> paths = new ArrayList<PathElement>();

        for( String key: keys ) {
            PathElement path = parseSingleKeyLHS( key );
            if ( path instanceof AtPathElement ) {
                throw new SpecException( "'.@.' is not valid on the RHS: " + refDotNotation );
            }
            paths.add( path );
        }

        return paths;
    }

    /**
     * Parse the dotNotation of the RHS.
     */
    public static List<PathElement> parseDotNotationRHS( String dotNotation ) {
        String fixedNotation = fixLeadingBracketSugar( dotNotation );
        List<String> pathStrs = parseDotNotation( new LinkedList<String>(), stringIterator( fixedNotation ), dotNotation );

        return parseList( pathStrs, dotNotation );
    }


    /**
     * This is the main recursive method of the Shiftr parallel "spec" and "input" tree walk.
     *
     * It should return true if this Spec object was able to successfully apply itself given the
     *  inputKey and input object.
     *
     * In the context of the Shiftr parallel treewalk, if this method returns true, the assumption
     *  is that no other sibling Shiftr specs need to look at this particular input key.
     *
     * @return true if this this spec "handles" the inputkey such that no sibling specs need to see it
     */
    public abstract boolean apply( String inputKey, Object input, WalkedPath walkedPath, Map<String,Object> output );
}
