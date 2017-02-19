package com.bazaarvoice.jolt.callr;

import com.bazaarvoice.jolt.common.pathelement.*;
import com.bazaarvoice.jolt.common.tree.MatchedElement;
import com.bazaarvoice.jolt.common.tree.WalkedPath;
import com.bazaarvoice.jolt.exception.SpecException;

import java.util.*;

public class CallrCompositeSpec extends CallrSpec {

    private static final ComputedKeysComparator computedKeysComparator = new ComputedKeysComparator();

    private final Map<String, CallrSpec> literalChildren;
    private final List<CallrSpec> computedChildren;

    public CallrCompositeSpec( String rawKey, Map<String, Object> spec ) {
        super( rawKey );

        Map<String, CallrSpec> literals = new HashMap<>();
        ArrayList<CallrSpec> computed = new ArrayList<>();

        // self check
        if ( pathElement instanceof AtPathElement ) {
            throw new SpecException( "@ CallrSpec key, cannot have children." );
        }

        List<CallrSpec> children = createChildren( spec );

        if ( children.isEmpty() ) {
            throw new SpecException( "CallrSpec format error: the spec cannot be empty." );
        }

        for ( CallrSpec child : children ) {
            literals.put( child.pathElement.getRawKey(), child );

            if ( child.pathElement instanceof LiteralPathElement ) {
                literals.put( child.pathElement.getRawKey(), child );
            } else if ( child.pathElement instanceof AtPathElement ) {
                throw new SpecException( "Callr does not accept @" );

            } else {   // star
                computed.add( child );
            }
        }

        Collections.sort( computed, computedKeysComparator );

        computed.trimToSize();
        literalChildren = Collections.unmodifiableMap( literals );
        computedChildren = Collections.unmodifiableList( computed );
    }


    /**
     * Recursively walk the spec input tree.
     */
    private List<CallrSpec> createChildren( Map<String, Object> rawSpec ) {

        List<CallrSpec> children = new ArrayList<>();
        Set<String> actualKeys = new HashSet<>();

        for ( String keyString : rawSpec.keySet() ) {

            Object rawRhs = rawSpec.get( keyString );

            CallrSpec childSpec;
            if ( rawRhs instanceof Map ) {
                if ( isLeaf( (Map<String, Object>) rawRhs ) ) {
                    childSpec = new CallrLeafSpec( keyString, (Map<String, Object>) rawRhs );
                } else {
                    childSpec = new CallrCompositeSpec( keyString, (Map<String, Object>) rawRhs );
                }
            } else {
                throw new SpecException( "Callr must contain a map defining a method and args." );
            }

            String childCanonicalString = childSpec.pathElement.getCanonicalForm();

            if ( actualKeys.contains( childCanonicalString ) ) {
                throw new IllegalArgumentException( "Duplicate canonical Callr key found : " + childCanonicalString );
            }

            actualKeys.add( childCanonicalString );

            children.add( childSpec );
        }

        return children;
    }

    public boolean apply( String inputKey, Object input, WalkedPath walkedPath, Object parentContainer ) {
        MatchedElement thisLevel = pathElement.match( inputKey, walkedPath );
        if ( thisLevel == null ) {
            return false;
        }

        walkedPath.add( input, thisLevel );

        process( input, walkedPath );

        walkedPath.removeLast();
        return true;
    }

    @SuppressWarnings( "unchecked" )
    private void process( Object input, WalkedPath walkedPath ) {

        if ( input instanceof Map ) {

            // Iterate over the whole entrySet rather than the keyset with follow on gets of the values
            Set<Map.Entry<String, Object>> entrySet = new HashSet<>( ( (Map<String, Object>) input ).entrySet() );
            for ( Map.Entry<String, Object> inputEntry : entrySet ) {
                applyKeyToLiteralAndComputed( this, inputEntry.getKey(), inputEntry.getValue(), walkedPath, input );
            }
        } else if ( input instanceof List ) {

            for ( int index = 0; index < ( (List<Object>) input ).size(); index++ ) {
                Object subInput = ( (List<Object>) input ).get( index );
                String subKeyStr = Integer.toString( index );

                applyKeyToLiteralAndComputed( this, subKeyStr, subInput, walkedPath, input );
            }
        } else if ( input != null ) {

            // if not a map or list, must be a scalar
            String scalarInput = input.toString();
            applyKeyToLiteralAndComputed( this, scalarInput, null, walkedPath, scalarInput );
        }
    }

    /**
     * This method implements the Cardinality matching behavior
     * when we have both literal and computed children.
     * <p/>
     * For each input key, we see if it matches a literal, and it not, try to match the key with every computed child.
     */
    private static void applyKeyToLiteralAndComputed( CallrCompositeSpec spec, String subKeyStr, Object subInput, WalkedPath walkedPath, Object input ) {

        CallrSpec literalChild = spec.literalChildren.get( subKeyStr );

        // if the subKeyStr found a literalChild, then we do not have to try to match any of the computed ones
        if ( literalChild != null ) {
            literalChild.apply( subKeyStr, subInput, walkedPath, input );
        } else {
            // If no literal spec key matched, iterate through all the computedChildren

            // Iterate through all the computedChildren until we find a match
            // This relies upon the computedChildren having already been sorted in priority order
            for ( CallrSpec computedChild : spec.computedChildren ) {
                // if the computed key does not match it will quickly return false
                if ( computedChild.apply( subKeyStr, subInput, walkedPath, input ) ) {
                    break;
                }
            }
        }
    }

    public static class ComputedKeysComparator implements Comparator<CallrSpec> {

        private static final HashMap<Class, Integer> orderMap = new HashMap<>();

        static {
            orderMap.put( AmpPathElement.class, 1 );
            orderMap.put( StarPathElement.class, 2 );
        }

        @Override
        public int compare( CallrSpec a, CallrSpec b ) {

            PathElement ape = a.pathElement;
            PathElement bpe = b.pathElement;

            int aa = orderMap.get( ape.getClass() );
            int bb = orderMap.get( bpe.getClass() );

            int elementsEqual = aa < bb ? -1 : aa == bb ? 0 : 1;

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
