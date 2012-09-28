package com.bazaarvoice.jolt.common;

import com.bazaarvoice.jolt.JsonUtils;
import com.bazaarvoice.jolt.shiftr.Path.*;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for "placing" data in a Json output, given a fully evaluated path (StringPath).
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
            Object[] arrayKey = splitArrayKey( outputPath.elementAt( index ) );
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
                        next = new HashMap<String, Object>();              // make the missing map
                        current.put( keyName, next );                      // put it in the output
                    }

                    current = (Map<String, Object>) next;                  // drill down the next level
                }
            } else {  // LIST

                if ( atBottom ) {

                    if ( next == null || !( next instanceof List ) ) {        // nothing or wrong thing there
                        next = new ArrayList();                            // make a new list
                        current.put( keyName, next );                      // place it in the output
                    }

                    ensureArraySize( arrayIndex, (List) next );            // next is now a list, make sure it is big enough

                    Object arrayNext = ( (List) next ).get( arrayIndex );    // See if there is any data in our target spot
                    if ( arrayNext == null ) {                             // nothing there
                        ( (List) next ).set( arrayIndex, value );            // just put the value
                    } else if ( arrayNext instanceof List ) {              // there's a list there
                        ( (List) arrayNext ).add( value );                 // add the value
                    } else {                                               // there's a non-list there
                        List toPut = new ArrayList();                      // make one to put there   Aka "foo" : "a", want to add "b" -> "foo" : [ "a", "b" ]
                        toPut.add( arrayNext );                            // add what's already there
                        toPut.add( value );                                // add our new value
                        ( (List) next ).set( arrayIndex, toPut );            // put the list in place
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

                    ensureArraySize( arrayIndex, (List) next );             // next is now a list, make sure it is big enough

                    HashMap<String, Object> arrayNext = (HashMap<String, Object>) ( (List) next ).get( arrayIndex );
                    // See if there is any data in our target spot
                    if ( arrayNext == null ) {                              // nothing there
                        arrayNext = new HashMap<String, Object>();          // make a new map
                        ( (List) next ).set( arrayIndex, arrayNext );       // put it in the output
                    }

                    current = arrayNext;                                    // return the map to continue drilling
                }
            }
        }
    }

    private static void ensureArraySize( Integer upperIndex, List list ) {
        for ( int sizing = list.size(); sizing <= upperIndex; sizing++ ) {
            list.add( null );
        }
    }

    static Pattern arrayKeyPattern = Pattern.compile( "^(.*?)\\[(\\d)\\]$" );  // photos or photos[2]

    private static Object[] splitArrayKey( String arrayKey ) {

        if ( arrayKey.contains( "[" ) && StringUtils.countMatches( arrayKey, "[" ) == 1 ) {

            Matcher matcher = arrayKeyPattern.matcher( arrayKey );

            if ( matcher.find() ) {
                Object[] retValue = new Object[2];
                retValue[0] = matcher.group( 1 );
                retValue[1] = Integer.parseInt( matcher.group( 2 ) );
                return retValue;
            }
        }
        return new Object[] {arrayKey, null};
    }
}