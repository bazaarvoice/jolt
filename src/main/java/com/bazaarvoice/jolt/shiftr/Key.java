package com.bazaarvoice.jolt.shiftr;

import com.bazaarvoice.jolt.common.Placr;
import com.bazaarvoice.jolt.shiftr.Path.*;
import com.bazaarvoice.jolt.shiftr.PathElement.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class Key {

    /**
     * Factory method that recursively processes a Map<String, Object> into a Set<Key> objects.
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

    // The processed output specification for spec keys that do not have children
    protected List<DotNotationPath> outputPaths = new ArrayList<DotNotationPath>();

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
                "&": "SecondaryRatings.&1.Id"                // & with no children : specialKey : Means use the text value of the key as the input
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


    // Three different buckets for the children of a node in a the Shiftr spec
    protected List<Key> specialChildren = new ArrayList<Key>(3);    // children that aren't actually triggered off the input data
    protected Map<String, Key> literalChildren = new HashMap<String, Key>();  // children that are simple exact matches against the input data
    protected List<Key> computedChildren = new ArrayList<Key>();    // children that are regex matches against the input data

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

    private static DotNotationPath parseOutputDotNotation( Object rawObj ) {

        if ( ! ( rawObj instanceof String ) ) {
            throw new IllegalArgumentException( "Invalid Shiftr spec RHS.  Should be a string or array of Strings.   Value in question : " + rawObj );
        }

        String outputPathStr = (String) rawObj;
        return DotNotationPath.parseDotNotation( outputPathStr );
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

        LiteralPathElement thisLevel = pathElement.match( inputKey, parentWalkedPath );
        if ( thisLevel == null ) {
            return false;
        }

        LiteralPath walkedPath = parentWalkedPath.append( thisLevel );

        //// 1. If I have no children, just output
        if ( ! hasChildren ) {

            for ( DotNotationPath outputPath : outputPaths) {
                StringPath stringPath = outputPath.evaluate( walkedPath );
                Placr.placeInOutput( input, stringPath, output );
            }
            return true;
        }

        //// 2. Handle any special / key based children first, but don't have them block anything
        for( Key subKey : specialChildren ) {
            if ( subKey.pathElement instanceof ReferencePathElement ) {
                ReferencePathElement subRef = (ReferencePathElement) subKey.pathElement;
                String refOutputData = subRef.evaluate( walkedPath );

                // Use the computed key as the input data
                // We call applyChildren not just placeInOutput, so that Reference computation will work correctly
                subKey.applyChildren( refOutputData, refOutputData, walkedPath, output );
            }
            if ( subKey.pathElement instanceof AtPathElement ) {

                // Pass down our input directly into our At child
                subKey.applyChildren( inputKey, input, walkedPath, output );
            }
        }

        //// 3. For each input key value, see if it matches any literal Keys or Computed keys, in that order
        // Numeric Array indexes are used / matched as Strings, here we handle the special casing between
        //  Map and List here to get a String "subKeyStr" and subInput
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
            // Quickly match a literal spec key if possible
            literalChild.applyChildren( subKeyStr, subInput, walkedPath, output );
        }
        else {
            // If no literal spec key matched, iterate thru the computedChildren until we find a match
            // This relies upon the computedChildren having already been sorted in priority order
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
            //  aka "rating-range-*" needs to be evaluated before "rating-*", or else "rating-*" will catch too much
            // If the lengths are equal, sort alphabetically as the last ditch deterministic behavior
            return alen > blen ? -1 : alen == blen ? acf.compareTo( bcf ) : 1;
        }
    }
}
