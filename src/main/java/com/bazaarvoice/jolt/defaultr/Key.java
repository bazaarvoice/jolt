package com.bazaarvoice.jolt.defaultr;

import com.bazaarvoice.jolt.Defaultr;
import static com.bazaarvoice.jolt.defaultr.OPS.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class Key<T> {

    /**
     * Factory-ish method that recursively processes a Map<String, Object> into a Map<DefaultrKey, Object>.
     *
     * @param spec Simple Jackson default Map<String,Object> input
     * @return processed spec
     */
    public static Map<Key, Object> parseSpec( Map<String, Object> spec ) {
        return processSpec( false, spec );
    }

    /**
     * Recursively walk the spec input tree.  Handle arrays by telling DefaultrKeys if they need to be ArrayKeys, and
     *  to find the max default array length.
     */
    private static Map<Key, Object> processSpec( boolean parentIsArray, Map<String, Object> spec ) {

        Map<Key, Object> result = new LinkedHashMap<Key, Object>();

        Key dk = null;
        for ( String key : spec.keySet() ) {
            if ( parentIsArray ) {
                dk = new ArrayKey( key );
            }
            else {
                dk = new MapKey( key );
            }

            Object obj = spec.get( key );
            // Spec is String -> Map or String -> Literal only
            if ( obj instanceof Map ) {
                Map<Key, Object> children = processSpec( dk.isArrayOutput(), (Map<String, Object>) obj );
                result.put( dk, children );

                if ( dk.isArrayOutput() ) {
                    // loop over children and find the max literal value
                    for( Key childKey : children.keySet() ) {
                        int childValue = childKey.getLiteralIntKey();
                        if ( childValue > dk.maxChildrenLiteralKey ) {
                            dk.maxChildrenLiteralKey = childValue;
                        }
                    }
                }

                // TODO : Ensure there is only one STAR entry
            }
            else {
                // literal such as String, number, or Json array
                result.put( dk, obj );
            }
        }

        return result;
    }


    public static final String OR_INPUT_REGEX = "\\" + Defaultr.WildCards.OR;

    // Am I supposed to be parent of an array?  If so I need to make sure that I inform
    //  my children they need to be ArrayKeys, and I need to make sure that the output array
    //  I will write to is big enough.
    protected boolean isArrayOutput = false;

    protected String rawKey;
    protected OPS op = null;
    protected int orCount = 0;
    protected int maxChildrenLiteralKey = -1;
    protected List<String> keyStrings;

    protected abstract Collection<T> getKeyValues();
    public abstract int getLiteralIntKey();
    public abstract Collection<T> findMatchingDefaulteeKeys( Object defaultee );

        protected void init( String rawJsonKey ) {

        rawKey = rawJsonKey;
        if ( rawJsonKey.endsWith( Defaultr.WildCards.ARRAY ) ) {
            isArrayOutput = true;
            rawKey = rawKey.replace( Defaultr.WildCards.ARRAY, "" );
        }

        op = OPS.parse( rawKey );

        switch( op ){
            case OR :
                keyStrings = Arrays.asList( rawKey.split( Key.OR_INPUT_REGEX ) );
                orCount = keyStrings.size();
                break;
            case LITERAL:
                keyStrings = Arrays.asList( rawKey );
                break;
            case STAR:
                keyStrings = Collections.emptyList();
                break;
            default :
                throw new IllegalStateException( "Someone has added an op type without changing this method." );
        }
    }

    public int getOrCount() {
        return orCount;
    }

    public boolean isArrayOutput() {
        return isArrayOutput;
    }

    public OPS getOp() {
        return op;
    }

    public int getMaxChildrenLiteralKey() {
        return maxChildrenLiteralKey;
    }

    public Object createDefaultContainerObject() {
        if ( isArrayOutput() ) {
            return new ArrayList<Object>();
        } else {
            return new LinkedHashMap<String, Object>();
        }
    }


    public static class KeyPrecedenceComparator implements Comparator<Key> {

        private OPS.OpsPrecedenceComparator opsComparator = new OPS.OpsPrecedenceComparator();

        @Override
        public int compare(Key a, Key b) {

            int opsEqual = opsComparator.compare(a.getOp(), b.getOp() );

            if ( opsEqual == 0 && OR == a.getOp() && OR == b.getOp() )
            {
                // For deterministic behavior, sub sort on the specificity of the OR
                //   aka as an Or, the more star like, the higher your value
                return (a.getOrCount() < b.getOrCount() ? -1 : (a.getOrCount() == b.getOrCount() ? 0 : 1));

                // TODO : if the orCounts are the same, make more deterministic?
            }

            return opsEqual;
        }
    }

}
