package com.bazaarvoice.jolt.common;

import com.bazaarvoice.jolt.JsonUtils;
import com.bazaarvoice.jolt.shiftr.Path.*;
import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for "placing" data in a Json output, given a fully evaluated path (StringPath).
 * "Evaluated" in this case mean all "&(1,1)" style references have been "evaluated" and a literal value
 *   has been computed.   Thus the job of this class is just to put the supplied data value, in the right place.
 *
 * Note, Placr will create an Array in the output if two values are written to the same "location" twice.
 * Aka, write the value "foo" to the path "bar.baz" once and you get :
 * {
 *   "bar" : {
 *       "baz" : "foo"
 *   }
 * }
 *
 * Write "tuna" to "bar.baz" as well, and you get :
 * {
 *   "bar" : {
 *       "baz" : [ "foo", "tuna" ]     // "foo" was removed, put into an array with "tuna", and then re-added to the output.
 *   }
 * }
 *
 * Note you can explicitly declare you want "baz" part of "bar.baz" to be an array, using this syntax : "bar.baz[]".
 * This is how you would get "foo" into an array by itself if you wanted.
 *
 * Example : Write the value "foo" to "bar.baz[]" would product output :
 * {
 *   "bar" : {
 *       "baz" : [ "foo" ]
 *   }
 * }
 *
 * Note on the Array Syntax.
 *
 *   "foo.bar[2].baz[]" is the current syntax and it is bad.  Its fine as syntactic sugar, but it really should be
 *   "foo.bar.[2].baz.[]" as the canonical form.
 *
 *   The logic of Placr would be simpler, as it would then always be one step, right now it is overly complex because
 *     it has to handle single step transitions "foo" to "bar", and multi-step transitions, "bar" to "[2]" to "baz".
 */
public class Placr {

    public static String OUTPUT_PREFIX_KEY = "output";

    public static void placeInOutput( Object value, StringPath realOutputPath, Map<String, Object> output ) {

        // TODO defense

        // To handle the case where we are trying to output at the top level, we need to add a fake
        //  output root key.
        StringPath outputPath = realOutputPath.prepend( OUTPUT_PREFIX_KEY );

        // defensive clone, in case the spec points to a map or list in the input doc
        value = JsonUtils.cloneJson( value );

        // We're going to drill down into the output via the specified outputPath.
        // Current is the variable that holds our current location in the output
        Map<String, Object> current = output;  // we start at the overall output

        boolean atBottom = false;
        for ( int index = 0; index < outputPath.size(); index++ ) {

            if ( index == outputPath.size() - 1 ) {
                atBottom = true;
            }

            // figure out keyName and arrayIndex from the paths
            Object[] arrayKey = parsePathElement( outputPath.elementAt( index ) );
            String keyName = (String) arrayKey[0];
            Integer arrayIndex = (Integer) arrayKey[1];

            Object next = current.get( keyName );                         // grab the next value in the path

            if ( arrayIndex == null ) { // MAP

                if ( atBottom ) {

                    if ( next == null ) {                                 // nothing there
                        current.put( keyName, value );                    // just put the value
                    } else if ( next instanceof List ) {                  // there's a list there
                        ( (List) next ).add( value );                     // add the value
                    } else {                                              // there's a non-list there
                        List toPut = new ArrayList();                     // make one to put there
                        toPut.add( next );                                // add what's already there
                        toPut.add( value );                               // add our new value
                        current.put( keyName, toPut );                    // put the list in place
                    }
                } else {
                    // make sure there's a map there and drill down
                    // TODO Warn or Fail if next != Map
                    if ( ( next == null ) || !( next instanceof Map ) ) {  // we expect it to be there and a map
                        next = new LinkedHashMap<String, Object>();              // make the missing map
                        current.put( keyName, next );                      // put it in the output
                    }

                    current = (Map<String, Object>) next;                  // drill down the next level
                }
            } else {  // LIST

                if ( atBottom ) {

                    if ( next == null || !( next instanceof List ) ) {     // nothing or wrong thing there
                        next = new ArrayList();                            // make a new list
                        current.put( keyName, next );                      // place it in the output
                    }

                    ensureArraySize( arrayIndex, (List) next );            // next is now a list, make sure it is big enough

                    Object arrayNext = null;
                    if ( arrayIndex != -1 ) {
                        arrayNext = ( (List<Object>) next ).get( arrayIndex );  // See if there is any data in our target spot
                    }
                    else {
                        arrayNext = next;                                       // We don't have an explicit target spot, but we want an array
                    }

                    if ( arrayNext == null ) {                             // nothing there
                        ( (List<Object>) next ).set( arrayIndex, value );  // just put the value
                    } else if ( arrayNext instanceof List ) {              // there's a list there
                        ( (List<Object>) arrayNext ).add( value );         // add the value
                    } else {                                               // there's a non-list there
                        List<Object> toPut = new ArrayList<Object>();      // make one to put there   Aka "foo" : "a", want to add "b" -> "foo" : [ "a", "b" ]
                        toPut.add( arrayNext );                            // add what's already there
                        toPut.add( value );                                // add our new value
                        ( (List<Object>) next ).set( arrayIndex, toPut );  // put the list in place
                    }

                } else {
                    // For array based output keys "photos[2]", the "drill down" is a two step process
                    // First, "map" drill down from current to "photos", and get back a List
                    // Second, make sure there is a map at [2] in the list
                    // Lastly, return the map at [2] for continued drilling

                    // make sure there's an Array there and drill down
                    // TODO Warn or Fail if next != List
                    if ( ( next == null ) || !( next instanceof List ) ) {  // we expect it to be there and a map
                        next = new ArrayList<Object>();                     // make the missing list
                        current.put( keyName, next );                       // put it in the output
                    }

                    ensureArraySize( arrayIndex, (List<Object>) next );             // next is now a list, make sure it is big enough

                    Map<String, Object> arrayNext = null;
                    if ( arrayIndex != -1 ) {
                        arrayNext = (Map<String, Object>) ( (List<Object>) next ).get( arrayIndex );
                    }

                    // See if there is any data in our target spot
                    if ( arrayNext == null ) {                              // nothing there
                        arrayNext = new LinkedHashMap<String, Object>();          // make a new map
                        if ( arrayIndex != -1 ) {
                            ( (List<Object>) next ).set( arrayIndex, arrayNext );   // put it in the output at the explicit array index
                        }
                        else {
                            ( (List<Object>) next ).add( arrayNext );               // put it in the output as an add
                        }
                    }

                    current = arrayNext;                                    // return the map to continue drilling
                }
            }
        }
    }

    private static void ensureArraySize( Integer upperIndex, List<Object> list ) {
        for ( int sizing = list.size(); sizing <= upperIndex; sizing++ ) {
            list.add( null );
        }
    }

    @VisibleForTesting
    static Pattern arrayKeyPattern = Pattern.compile( "^(.*?)\\[(\\d)?\\]$" );  // photos or photos[2] or photos[]

    /**
     * Given a String path like "foo.bar[2].baz[]" this method works on individual path elements to extract
     *   key name and array information.
     *
     * In the case of "foo.bar[2].baz[]" :
     *  "foo"    -> [ "foo", null ]         // null means not an array
     *  "bar[2]" -> [ "bar", 2 ]            // 0 to infinity means a legit array value
     *  "baz[]"  -> [ "baz", -1 ]           // -1 means we want array and we don't care what index we put data in.
     *
     * @param pathElement one entry in a StringPath, like "foo", "bar[2]", or "baz[]"
     * @return a 2 element Object[] where the first element is the key, and the 2nd element is array information
     */
    private static Object[] parsePathElement( String pathElement ) {

        if ( pathElement.contains( "[" ) && StringUtils.countMatches( pathElement, "[" ) == 1 ) {

            Matcher matcher = arrayKeyPattern.matcher( pathElement );

            if ( matcher.find() ) {
                Object[] retValue = new Object[2];
                retValue[0] = matcher.group( 1 );

                String arrayIndex = matcher.group( 2 );
                if ( StringUtils.isBlank( arrayIndex ) ) {
                    retValue[1] = -1;  // -1 : we were passed "[]" which means, just make sure there is an array there, and add to it
                }
                else {
                    retValue[1] = Integer.parseInt( arrayIndex );
                }
                return retValue;
            }
        }
        return new Object[] {pathElement, null};
    }
}