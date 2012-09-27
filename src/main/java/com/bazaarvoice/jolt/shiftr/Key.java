package com.bazaarvoice.jolt.shiftr;

import com.bazaarvoice.jolt.shiftr.Path.*;
import com.bazaarvoice.jolt.shiftr.PathElement.*;

import com.bazaarvoice.jolt.JsonUtils;
import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Key {

    /**
     * Factory-ish method that recursively processes a Map<String, Object> into a Set<Key> objects.
     *
     * @param spec Simple Jackson default Map<String,Object> input
     * @return Set of Keys from this level in the spec
     */
    public static List<Key> parseSpec( Map<String, Object> spec ) {
        return processSpec( spec );
    }

    /**
     * Recursively walk the spec input tree.  Handle arrays by telling DefaultrKeys if they need to be ArrayKeys, and
     * to find the max default array length.
     */
    private static List<Key> processSpec( Map<String, Object> spec ) {

        List<Key> result = new ArrayList<Key>();
        Set<String> actualKeys = new HashSet<String>();

        for ( String rawKeyStr : spec.keySet() ) {

            String[] keyStrings = rawKeyStr.split( "\\|" ); // unwrap the syntactic sugar of the OR
            for ( String keyString : keyStrings ) {

                // Duplicate the "spec" for each key in the "|"
                // this will recursively call processSpec if needed
                Key key = new Key(keyString, spec.get(rawKeyStr));

                String canonicalString = key.pathElement.getCanonicalForm();

                if ( actualKeys.contains( canonicalString ) ) {
                    throw new IllegalArgumentException( "Duplicate canonical Shiftr key found : " + canonicalString );
                }

                actualKeys.add( canonicalString );

                result.add(key);
            }
        }

        // Sort the children before returning
        Collections.sort( result, keyComparator );

        return result;
    }

    private static final KeyPrecedenceComparator keyComparator = new KeyPrecedenceComparator();

    // The key of the spec
    protected PathElement pathElement;

    // The value of the key, either children or a literal value
    protected List<Key> children = null;

    protected List<OutputPath> outputPaths = new ArrayList<OutputPath>();

    public Key( String rawJsonKey, Object spec ) {

        pathElement = PathElement.parse( rawJsonKey );

        // Spec is String -> Map   or   String -> Literal only
        if ( spec instanceof Map ) {
            children = processSpec( (Map<String, Object>) spec );
        }
        else if ( spec instanceof String ) {
            // literal such as String, number, or Json array
            String rawValue = (String) spec;
            outputPaths.add(OutputPath.parseDotNotation(rawValue));
        }
        // Spec : "foo": ["a", "b"] : Shift the value of "foo" to both "a" and "b"
        else if ( spec instanceof List ) {
            try {
                List<String> outputs = (List<String>) spec;
                for ( String outputPathStr : outputs ) {
                    outputPaths.add(OutputPath.parseDotNotation(outputPathStr));
                }
            }
            catch ( ClassCastException cce ) {
                throw new IllegalArgumentException( "Invalid Shiftr spec RHS.  Should be array of strings.  Key in question : " + spec );
            }
        }
        else {
            throw new IllegalArgumentException( "Invalid Shiftr spec RHS.  Should be map, string, or array of strings.  Key in question : " + spec );
        }

        if ( pathElement instanceof AtPathElement && children != null ) {
            throw new IllegalArgumentException( "@ Shiftr key, can not have children." );
        }
    }

    public List<Key> getChildren() {
        return Collections.unmodifiableList( children );
    }


    public LiteralPath apply( String inputKey, LiteralPath walkedPath ) {

        LiteralPathElement lpe = pathElement.matchInput(inputKey, walkedPath);

        if ( lpe != null ) {  // only output if
            return new LiteralPath( walkedPath, lpe );
        }

        return null;
    }

    /**
     * This is the main "recursive" method.
     *
     *
     "rating": {
         "primary": {
             "value": "Rating",
             "max": "RatingRange"
         },
         "*": {
             "value": "SecondaryRatings.&1.Value",
             "max": "SecondaryRatings.&1.Range",
             "&": "SecondaryRatings.&1.Id"
         }
     }
     */


    public boolean applyChildren( String inputKey, Object input, LiteralPath walkedPath, Map<String,Object> output ) {

        if ( pathElement instanceof AtPathElement ) {
            return false;
        }

        LiteralPathElement thisLevel = pathElement.matchInput(inputKey, walkedPath);
        if ( thisLevel == null ) {
            return false;
        }

        LiteralPath newWalkedPath = new LiteralPath( walkedPath, thisLevel );

        // make sure any output-only reference path children get a chance to output
        if ( children != null ) {
            for ( Key subKey : children ) {
                if ( subKey.pathElement instanceof ReferencePathElement && subKey.children == null ) {
                    ReferencePathElement subRef = (ReferencePathElement) subKey.pathElement;
                    String refOutputData = subRef.evaluateAsOutputKey( newWalkedPath );

                    subKey.applyChildren( refOutputData, refOutputData, newWalkedPath, output );
                }
                if ( subKey.pathElement instanceof AtPathElement && subKey.children == null ) {
                    for ( OutputPath outputPath : subKey.outputPaths) {
                        StringPath stringPath = outputPath.build( newWalkedPath );
                        putInOutput( input, stringPath, output );
                    }
                }
            }
        }

        if ( children == null ) { // leaf node of spec
            for ( OutputPath outputPath : outputPaths) {
                StringPath stringPath = outputPath.build( newWalkedPath );
                putInOutput( input, stringPath, output );
            }
            // the whole job of the @ key is to copy a whole subtree of input data to the output
            //  and allow other targeted shifts to occur by _not_ stopping the shiftr logic
            if ( pathElement instanceof AtPathElement ) {
                return false;
            }
            return true;
        }

        else if ( children != null && input instanceof Map) {

            Map<String,Object> inputMap = (Map<String, Object>) input;

            for( String subKeyStr : inputMap.keySet() ) {
                for ( Key subKey : children ) {
                    if ( subKey.applyChildren( subKeyStr, inputMap.get( subKeyStr ), newWalkedPath, output ) ) {
                        break;
                    }
                }
            }
        }
        else if ( children != null && input instanceof List) {

            List inputList = (List) input;

            for (int index = 0; index < inputList.size(); index++) {
                for ( Key subKey : children ) {
                    Object subInput = inputList.get( index );
                    String key = Integer.toString( index );
                    if ( subKey.applyChildren( key, subInput, newWalkedPath, output ) ) {
                        break;
                    }
                }
            }
        }

        return true;
    }






    public static class KeyPrecedenceComparator implements Comparator<Key> {

        private static HashMap<Class, Integer> orderMap = new HashMap<Class, Integer>();

        static {
            orderMap.put( PathElement.AtPathElement.class, 1 );
            orderMap.put( PathElement.LiteralPathElement.class, 2 );
            orderMap.put( PathElement.ReferencePathElement.class, 3 );
            orderMap.put( PathElement.StarPathElement.class, 4 );
        }

        @Override
        public int compare( Key a, Key b ) {

            int aa = orderMap.get( a.pathElement.getClass() );
            int bb = orderMap.get( b.pathElement.getClass() );

            int elementsEqual =  aa < bb ? -1 : aa == bb ? 0 : 1;

            if ( elementsEqual != 0 ) {
                return elementsEqual;
            }

            // Sort the star elements by lenght with the longest (most specific) being first
            //  aka rating-range-* needs to be evaled before rating-*, or else rating-* will catch too much
            if ( a.pathElement instanceof StarPathElement ) {
                int alen = a.pathElement.rawKey.length();
                int blen = b.pathElement.rawKey.length();

                return alen > blen ? -1 : alen == blen ? 0 : 1;
            }
            return elementsEqual;
        }
    }


    public static Pattern arrayKeyPattern = Pattern.compile( "^(.*?)\\[(\\d)\\]$" );

    public static Object[] splitArrayKey( String arrayKey ) {

        if ( arrayKey.contains( "[" ) && StringUtils.countMatches( arrayKey, "[" ) == 1 ) {

            Matcher matcher = Key.arrayKeyPattern.matcher( arrayKey );

            if ( matcher.find() ) {
                Object[] retValue = new Object[2];
                retValue[0] = matcher.group( 1 );
                retValue[1] = Integer.parseInt( matcher.group( 2 ) );
                return retValue;
            }
        }
        return new Object[] {arrayKey, null};
    }

    public static void putInOutput(Object value, StringPath outputPath, Map<String, Object> output) {

        // TODO defense

        // we're going to drill down into the output via the path specified in the where argument
        // current is the variable that holds our current location in the output
        Map<String, Object> current = output;               // we start at the overall output

        // drill down for each item in the path above the last
        for ( int index = 0; index < outputPath.size() - 1; index++ ) {

            // figure out key name from paths
            Object[] arrayKey = splitArrayKey( outputPath.elementAt( index ) );
            String keyName = (String) arrayKey[0];
            Integer arrayIndex = (Integer) arrayKey[1];

            Object next;
            if ( arrayIndex == null ) {

                // make sure there's a map there and drill down
                // TODO handle the case where next is a list/value better
                next = current.get( keyName );               // grab the next value in the path
                if ( ( next == null ) || !( next instanceof Map ) ) {     // we expect it to be there and a map
                    next = new HashMap<String, Object>();           // make the missing map
                    current.put( keyName, next );                   // put it in the output
                }
            } else {

                // make sure there's an Array there and drill down
                // TODO handle the case where next is a list/value better
                next = current.get( keyName );               // grab the next value in the path
                if ( ( next == null ) || !( next instanceof List ) ) {     // we expect it to be there and a map
                    next = new ArrayList<Object>();           // make the missing list
                    current.put( keyName, next );                   // put it in the output
                }

                ensureArraySize( arrayIndex, (List) next );
                HashMap<String, Object> nextNext = (HashMap<String, Object>) ((List) next).get( arrayIndex );
                if ( nextNext == null ) {
                    nextNext = new HashMap<String, Object>();
                    ( (List) next ).set( arrayIndex, nextNext );
                }

                next = nextNext;
            }

            current = (Map<String, Object>) next;               // drill down the next level
        }

        // defensive clone, in case the spec points to a map or list in the input doc
        value = JsonUtils.cloneJson( value );

        // now we're at the very bottom of our path.
        // time to insert our value

        // figure out the last keyname
        Object[] arrayKey = splitArrayKey( outputPath.lastElement() );
        String keyName = (String) arrayKey[0];
        Integer arrayIndex = (Integer) arrayKey[1];

        Object alreadyThere = current.get( keyName );           // see if it's occupied

        if ( arrayIndex == null ) {
            if ( alreadyThere == null ) {                             // nothing there
                current.put( keyName, value );                      // just put the value
            } else if ( alreadyThere instanceof List ) {                // there's a list there
                ( (List) alreadyThere ).add( value );               // add the value
            } else {                                                  // there's a non-list there
                List toPut = new ArrayList();                       // make one to put there
                toPut.add( alreadyThere );                          // add what's already there
                toPut.add( value );                                 // add our new value
                current.put( keyName, toPut );                      // put the list in place
            }
        } else {

            if ( alreadyThere == null || !(alreadyThere instanceof List)) {                             // nothing or wrong thing there
                alreadyThere = new ArrayList();
                current.put( keyName, alreadyThere );                      // just put the value
            }

            ensureArraySize( arrayIndex, (List) alreadyThere );


            Object nextNext = ((List) alreadyThere).get( arrayIndex );
            if ( nextNext == null ) {                             // nothing there
                ((List) alreadyThere).set( arrayIndex, value );                      // just put the value
            } else if ( nextNext instanceof List ) {                // there's a list there
                ( (List) nextNext ).add( value );               // add the value
            } else {                                                  // there's a non-list there
                List toPut = new ArrayList();                       // make one to put there
                toPut.add( nextNext );                          // add what's already there
                toPut.add( value );                                 // add our new value
                ((List) alreadyThere).set( arrayIndex, toPut );                      // put the list in place
            }
        }
    }

    private static void ensureArraySize( Integer upperIndex, List list ) {
        for ( int sizing = list.size(); sizing <= upperIndex; sizing++ ) {
            list.add( null );
        }
    }
}
