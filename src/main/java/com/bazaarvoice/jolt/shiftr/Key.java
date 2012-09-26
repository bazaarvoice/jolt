package com.bazaarvoice.jolt.shiftr;

import com.bazaarvoice.jolt.shiftr.Path.*;
import com.bazaarvoice.jolt.shiftr.PathElement.*;

import com.bazaarvoice.jolt.JsonUtils;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

        for ( String keyStr : spec.keySet() ) {
            Object subSpec = spec.get( keyStr );
            result.add( new Key( keyStr, subSpec ) ); // this will recursively call processSpec if needed
        }

        // Sort the children b4 returning
        Collections.sort( result, keyComparator );

        return result;
    }

    private static final KeyPrecedenceComparator keyComparator = new KeyPrecedenceComparator();

    // The key of the spec
    protected String rawKey;
    protected PathElement pathElement;

    // The value of the key, either children or a literal value
    protected List<Key> children = null;

    protected String rawValue = null;
    protected OutputPath outputPath = null;

    public Key( String rawJsonKey, Object spec ) {

        rawKey = rawJsonKey;
        pathElement = PathElement.parse( rawJsonKey );

        // Spec is String -> Map   or   String -> Literal only
        if ( spec instanceof Map ) {
            children = processSpec( (Map<String, Object>) spec );

        }
        else if ( spec instanceof String ) {
            // literal such as String, number, or Json array
            rawValue = (String) spec;
            outputPath = OutputPath.parseDotNotation(rawValue);
        }
        else {
            throw new IllegalArgumentException( "Shiftr spec can not have non-String RHS values." );
        }
    }

    public List<Key> getChildren() {
        return children;
    }

    public LiteralPath apply( String inputKey, Object input, LiteralPath walkedPath ) {

        LiteralPathElement lpe = pathElement.matchInput(inputKey, walkedPath);

        if ( lpe != null ) {  // only output if
            return new LiteralPath( walkedPath, lpe );
        }

        return null;
    }

    /**
     * This is the main "recursive" method.
     */
    public boolean applyChildren( String inputKey, Object input, LiteralPath walkedPath, Map<String,Object> output ) {

        LiteralPath newWalkedPath = apply( inputKey, input, walkedPath);

        if ( newWalkedPath == null ){
            return false;
        }

        if ( children == null && input instanceof String ) { // leaf node of spec
            StringPath stringPath = outputPath.build( newWalkedPath );
            putInOutput( input, stringPath, output );
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

            for (int index=0; index<inputList.size(); index++) {
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
            orderMap.put( PathElement.OrPathElement.class, 4 );
            orderMap.put( PathElement.StarPathElement.class, 5 );
        }

        //private OpsPrecedenceComparator opsComparator = new OpsPrecedenceComparator();

        @Override
        public int compare( Key a, Key b ) {

            int aa = orderMap.get( a.pathElement.getClass() );
            int bb = orderMap.get( b.pathElement.getClass() );

            // TODO more deterministic sort
            return aa < bb ? -1 : aa == bb ? 0 : 1;
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
