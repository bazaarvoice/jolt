package com.bazaarvoice.jolt.shiftr;

import com.bazaarvoice.jolt.JsonUtils;

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
     * Factory-ish method that recursively processes a Map<String, Object> into a Set<Key> objects.
     *
     * @param spec Simple Jackson default Map<String,Object> input
     * @return Set of Keys from this level in the spec
     */
    public static Set<Key> parseSpec( Map<String, Object> spec ) {
        return processSpec( spec );
    }

    /**
     * Recursively walk the spec input tree.  Handle arrays by telling DefaultrKeys if they need to be ArrayKeys, and
     *  to find the max default array length.
     */
    private static Set<Key> processSpec( Map<String, Object> spec ) {

        Set<Key> result = new HashSet<Key>();

        for ( String key : spec.keySet() ) {
            Object subSpec = spec.get( key );

            result.add( new Key( key, subSpec ) ); // this will recursively call processSpec if needed
        }

        return result;
    }

    private static final Key.KeyPrecedenceComparator keyComparator = new Key.KeyPrecedenceComparator();

    protected PathElement pathElement;
    protected Set<Key> children = null;

    protected String rawKey;
    protected Object literalValue = null;

    public Key(String rawJsonKey, Object spec) {

        rawKey = rawJsonKey;

        pathElement = PathElement.parse( rawJsonKey );

        // Spec is String -> Map   or   String -> Literal only
        if ( spec instanceof Map ) {
            children = processSpec( (Map<String, Object>) spec );

        }
        else {
            // literal such as String, number, or Json array
            literalValue = spec;
        }
    }

    /**
     * This is the main "recursive" method.   The defaultee should never be null, because
     *  the defaultee wasn't null, it was null and we created it, OR there was
     *  a mismatch between the Defaultr Spec and the input, and we didn't recurse.
     */
    public void applyChildren( Object defaultee ) {

        if ( defaultee == null ) {
            throw new IllegalArgumentException( "Defaultee should never be null when " +
                    "passed to the applyChildren method." );
        }

        // Find and sort the children DefaultrKeys by precedence: literals, |, then *
        ArrayList<Key> sortedChildren = new ArrayList<Key>();
        sortedChildren.addAll( children );
        Collections.sort( sortedChildren, keyComparator );

        for ( Key childKey : sortedChildren ) {
//            childKey.applyChild( defaultee );
        }
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
        public int compare(Key a, Key b) {

            int aa = orderMap.get( a.pathElement.getClass() );
            int bb = orderMap.get(b.pathElement.getClass());

            // TODO more deterministic sort
            return aa < bb ? -1 : aa == bb ? 0 : 1;
        }
    }

}
