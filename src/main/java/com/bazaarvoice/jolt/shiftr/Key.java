package com.bazaarvoice.jolt.shiftr;

import com.bazaarvoice.jolt.shiftr.Path.*;
import com.bazaarvoice.jolt.shiftr.PathElement.*;

import com.bazaarvoice.jolt.JsonUtils;
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

    private static final ComputedKeysComparator computedKeysComparator = new ComputedKeysComparator();

    // The processed key from the Json config
    protected PathElement pathElement;

    // The processed output specification
    protected List<OutputPath> outputPaths = new ArrayList<OutputPath>();

    /*
    Spec Example 1 :
    {
        "@" : [ "payload.original", "payload.secondCopy" ]   //  @ : specialKey      [ "payload.original", "payload.secondCopy" ] : outputPaths

        "rating": {                                          //  rating : literalChild     outputPaths are empty
            "primary": {
                "value": "Rating",
                "max": "RatingRange"
            },
            "*": {                                           // * : computedChild
                "value": "SecondaryRatings.&1.Value",
                "max": "SecondaryRatings.&1.Range",
                "&": "SecondaryRatings.&1.Id"                // & with no children : specialKey
            }
        }
    }

    Spec Example 2 :
    {
        rating-*" : {                                        // rating-* : computedChild
            "&(1)" : {                                       // &(1) has children : computedChild, aka comptute the key, and see if there are any values that match
                "value" : "Rating-&1.value"
            }
        }
    }
    Sample Data for Spec 2 :
    {
        rating-primary : {
            primary : {
                value : 4      <-- want this value
            },
            randomNoise : {
                value : 5      <-- not this one
            }
        }
    }
    */


    // The value of the key, either children or a literal value
    protected List<Key> specialChildren = new ArrayList<Key>(3);
    protected Map<String, Key> literalChildren = new HashMap<String, Key>();
    protected List<Key> computedChildren = new ArrayList<Key>();

    protected boolean hasChildren = false;

    public Key( String rawJsonKey, Object spec ) {

        pathElement = PathElement.parse( rawJsonKey );

        if ( spec instanceof Map ) {
            List<Key> children = processSpec( (Map<String, Object>) spec );

            for ( Key child : children ) {
                if ( child.pathElement instanceof LiteralPathElement ) {
                    literalChildren.put( child.pathElement.getRawKey(), child );
                }
                else if ( child.pathElement instanceof AtPathElement || (  // special if it is an "@"
                     child.pathElement instanceof ReferencePathElement && ! child.hasChildren ) ) {   // special if it is a "&" and it has no children
                    specialChildren.add( child );
                }
                else {   // star || (& with children)
                    computedChildren.add( child );
                }
            }

            // Only the computed children need to be sorted
            Collections.sort( computedChildren, computedKeysComparator );

            hasChildren = true;
        }
        else if ( spec instanceof String ) {
            // leaf level so spec is an dot notation output path
            outputPaths.add( parseOutputDotNotation( spec ) );
        }
        else if ( spec instanceof List ) {
            // leaf level list
            // Spec : "foo": ["a", "b"] : Shift the value of "foo" to both "a" and "b"
            for ( Object dotNotation : (List) spec ) {
                outputPaths.add( parseOutputDotNotation( dotNotation ) );
            }
        }
        else {
            throw new IllegalArgumentException( "Invalid Shiftr spec RHS.  Should be map, string, or array of strings.  Key in question : " + spec );
        }

        // self check
        if ( pathElement instanceof AtPathElement && hasChildren ) {
            throw new IllegalArgumentException( "@ Shiftr key, can not have children." );
        }
    }

    private static OutputPath parseOutputDotNotation( Object rawObj ) {

        if ( ! ( rawObj instanceof String ) ) {
            throw new IllegalArgumentException( "Invalid Shiftr spec RHS.  Should be a string or array of Strings.   Value in question : " + rawObj );
        }

        String outputPathStr = (String) rawObj;
        return OutputPath.parseDotNotation(outputPathStr);
    }

    /**
     * Recursively walk the spec input tree.
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

        return result;
    }


    /**
     * This is the main "recursive" method.
     */
    public boolean applyChildren( String inputKey, Object input, LiteralPath parentWalkedPath, Map<String,Object> output ) {

        LiteralPathElement thisLevel = pathElement.matchInput(inputKey, parentWalkedPath);
        if ( thisLevel == null ) {
            return false;
        }

        LiteralPath walkedPath = new LiteralPath( parentWalkedPath, thisLevel );

        //// 1. If I have no children, just output
        if ( ! hasChildren ) {

            for ( OutputPath outputPath : outputPaths) {
                StringPath stringPath = outputPath.build( walkedPath );
                putInOutput( input, stringPath, output );
            }
            return true;
        }

        //// 2. Handle any special / key based children first, but don't have them block anything
        for( Key subKey : specialChildren ) {
            if ( subKey.pathElement instanceof ReferencePathElement ) {
                ReferencePathElement subRef = (ReferencePathElement) subKey.pathElement;
                String refOutputData = subRef.evaluateAsOutputKey( walkedPath );

                // Use the comptued key as the input data
                subKey.applyChildren( refOutputData, refOutputData, walkedPath, output );
            }
            if ( subKey.pathElement instanceof AtPathElement ) {
                for ( OutputPath outputPath : subKey.outputPaths) {
                    StringPath stringPath = outputPath.build( walkedPath );

                    // put this potentially large input tree of data, into the output
                    putInOutput( input, stringPath, output );
                }
            }
        }

        //// 3. For each input key value, see if it matches any literal Keys or Computed keys, in that order
        if ( input instanceof Map) {

            Map<String,Object> inputMap = (Map<String, Object>) input;

            for( String subKeyStr : inputMap.keySet() ) {
                Object subInput = inputMap.get( subKeyStr );

                applyToLiteralAndComputedChildren( subKeyStr, subInput, walkedPath, output );
            }
        }
        else if ( input instanceof List) {

            List inputList = (List) input;

            for (int index = 0; index < inputList.size(); index++) {
                Object subInput = inputList.get( index );
                String subKeyStr = Integer.toString( index );

                applyToLiteralAndComputedChildren( subKeyStr, subInput, walkedPath, output );
            }
        }

        return true;
    }

    private void applyToLiteralAndComputedChildren( String subKeyStr, Object subInput, LiteralPath walkedPath, Map<String, Object> output ) {

        Key literalChild = literalChildren.get( subKeyStr );
        if ( literalChild != null ) {
            literalChild.applyChildren( subKeyStr, subInput, walkedPath, output );
        }
        else {
            for ( Key computedChild : computedChildren ) {
                // if the computed key does not match it will quickly return false
                if ( computedChild.applyChildren( subKeyStr, subInput, walkedPath, output ) ) {
                    break;
                }
            }
        }
    }


    public static class ComputedKeysComparator implements Comparator<Key> {

        private static HashMap<Class, Integer> orderMap = new HashMap<Class, Integer>();

        static {
            orderMap.put( PathElement.ReferencePathElement.class, 1 );
            orderMap.put( PathElement.StarPathElement.class, 2 );
        }

        @Override
        public int compare( Key a, Key b ) {

            PathElement ape = a.pathElement;
            PathElement bpe = b.pathElement;

            int aa = orderMap.get( ape.getClass() );
            int bb = orderMap.get( bpe.getClass() );

            int elementsEqual =  aa < bb ? -1 : aa == bb ? 0 : 1;

            if ( elementsEqual != 0 ) {
                return elementsEqual;
            }

            // At this point we have two PathElements of the same type.
            String acf = ape.getCanonicalForm();
            String bcf = bpe.getCanonicalForm();

            int alen = acf.length();
            int blen = bcf.length();

            // Sort them by length, with the longest (most specific) being first
            //  aka rating-range-* needs to be evaled before rating-*, or else rating-* will catch too much
            // If the lengths are equal, sort alphabetically as the last deterministic behavior
            return alen > blen ? -1 : alen == blen ? acf.compareTo( bcf ) : 1;
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

        // defensive clone, in case the spec points to a map or list in the input doc
        value = JsonUtils.cloneJson( value );

        // we're going to drill down into the output via the path specified in the where argument
        // current is the variable that holds our current location in the output
        Map<String, Object> current = output;               // we start at the overall output

        boolean atBottom = false;
        for ( int index = 0; index < outputPath.size(); index++ ) {

            if ( index == outputPath.size() -1 ) {
                atBottom = true;
            }

            // figure out key name from paths
            Object[] arrayKey = splitArrayKey( outputPath.elementAt( index ) );
            String keyName = (String) arrayKey[0];
            Integer arrayIndex = (Integer) arrayKey[1];

            Object next = current.get( keyName );               // grab the next value in the path

            if ( arrayIndex == null ) { // MAP

                if ( atBottom ){

                    if ( next == null ) {                             // nothing there
                        current.put( keyName, value );                      // just put the value
                    } else if ( next instanceof List ) {                // there's a list there
                        ( (List) next ).add( value );               // add the value
                    } else {                                                  // there's a non-list there
                        List toPut = new ArrayList();                       // make one to put there
                        toPut.add( next );                          // add what's already there
                        toPut.add( value );                                 // add our new value
                        current.put( keyName, toPut );                      // put the list in place
                    }
                }
                else {
                    // make sure there's a map there and drill down
                    // TODO handle the case where next is a list/value better
                    if ( ( next == null ) || !( next instanceof Map ) ) {     // we expect it to be there and a map
                        next = new HashMap<String, Object>();           // make the missing map
                        current.put( keyName, next );                   // put it in the output
                    }

                    current = (Map<String, Object>) next;               // drill down the next level
                }
            } else {  // LIST

                if ( atBottom ) {

                    if ( next == null || !(next instanceof List)) {        // nothing or wrong thing there
                        next = new ArrayList();
                        current.put( keyName, next );                      // just put the value
                    }

                    ensureArraySize( arrayIndex, (List) next );

                    Object nextNext = ((List) next).get( arrayIndex );
                    if ( nextNext == null ) {                             // nothing there
                        ((List) next).set( arrayIndex, value );                      // just put the value
                    } else if ( nextNext instanceof List ) {                // there's a list there
                        ( (List) nextNext ).add( value );               // add the value
                    } else {                                                  // there's a non-list there
                        List toPut = new ArrayList();                       // make one to put there
                        toPut.add( nextNext );                          // add what's already there
                        toPut.add( value );                                 // add our new value
                        ((List) next).set( arrayIndex, toPut );                      // put the list in place
                    }

                }
                else {

                    // make sure there's an Array there and drill down
                    // TODO handle the case where next is a list/value better
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

                    current = nextNext;
                }
            }
        }
    }

    private static void ensureArraySize( Integer upperIndex, List list ) {
        for ( int sizing = list.size(); sizing <= upperIndex; sizing++ ) {
            list.add( null );
        }
    }
}
