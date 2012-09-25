package com.bazaarvoice.jolt.shiftr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.bazaarvoice.jolt.shiftr.OPS.OpsPrecedenceComparator;

public class Key {

    public interface WildCards {
        public static final String STAR = "*";
        public static final String OR = "|";
        public static final String AT = "@";
        public static final String ARRAY = "[";
    }


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


    private static final String OR_INPUT_REGEX = "\\" + WildCards.OR;
    private static final Key.KeyPrecedenceComparator keyComparator = new Key.KeyPrecedenceComparator();

    // Am I supposed to be parent of an array?  If so I need to make sure that I inform
    //  my children they need to be ArrayKeys, and I need to make sure that the output array
    //  I will write to is big enough.
    private OPS op = null;

    protected Set<Key> children = null;
    protected Object literalValue = null;

    protected String rawKey;
    protected List<String> keyStrings;

    public Key(String rawJsonKey, Object spec) {

        rawKey = rawJsonKey;

        op = OPS.parse( rawKey );

        switch( op ){
            case OR :
                keyStrings = Arrays.asList( rawKey.split( Key.OR_INPUT_REGEX ) );
                break;
            case LITERAL:
                keyStrings = Arrays.asList( rawKey );
                break;
            case STAR:
                keyStrings = Collections.emptyList();
                break;
            default :
                throw new IllegalArgumentException( "Someone has added an op type without changing this method." );
        }

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

    public OPS getOp() {
        return op;
    }


    public static class KeyPrecedenceComparator implements Comparator<Key> {

        private OpsPrecedenceComparator opsComparator = new OpsPrecedenceComparator();

        @Override
        public int compare(Key a, Key b) {

            int opsEqual = opsComparator.compare(a.getOp(), b.getOp() );

//            if ( opsEqual == 0 && OR == a.getOp() && OR == b.getOp() )
//            {
//                // For deterministic behavior, sub sort on the specificity of the OR and then alphabetically on the rawKey
//                //   For the Or, the more star like, the higher your value
//                //   If the or count matches, fall back to alphabetical on the rawKey from the spec file
//                return (a.getOrCount() < b.getOrCount() ? -1 : (a.getOrCount() == b.getOrCount() ? a.rawKey.compareTo( b.rawKey ) : 1 ) );
//            }

            return opsEqual;
        }
    }

}
