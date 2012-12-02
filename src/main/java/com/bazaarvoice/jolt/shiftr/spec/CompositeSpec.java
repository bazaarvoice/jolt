package com.bazaarvoice.jolt.shiftr.spec;

import com.bazaarvoice.jolt.exception.SpecException;
import com.bazaarvoice.jolt.shiftr.WalkedPath;
import com.bazaarvoice.jolt.shiftr.pathelement.AmpPathElement;
import com.bazaarvoice.jolt.shiftr.pathelement.AtPathElement;
import com.bazaarvoice.jolt.shiftr.pathelement.DollarPathElement;
import com.bazaarvoice.jolt.shiftr.pathelement.LiteralPathElement;
import com.bazaarvoice.jolt.shiftr.pathelement.PathElement;
import com.bazaarvoice.jolt.shiftr.pathelement.StarPathElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Spec that has children, which it builds and then manages during Transforms.
 */
public class CompositeSpec extends Spec {

  /*
    Example of how a Spec gets parsed into Composite and LeafSpec objects :

    {                                                        //  "implicit" root CompositeSpec, with one specialChild ("@") and one literalChild ("rating")
        "@" : [ "payload.original", "payload.secondCopy" ]   //  LeafSpec with an AtPathElement and outputWriters [ "payload.original", "payload.secondCopy" ]

        "rating": {                                          //  CompositeSpec with 1 literalChild ("rating") and one computedChild ("*")
            "primary": {
                "value": "Rating",
                "max": "RatingRange"
            },
            "*": {
                "value": "SecondaryRatings.&1.Value",        // LeafSpec with a LiteralPathElement and one outputWriter [ "SecondaryRatings.&1.Value" ]
                "max": "SecondaryRatings.&1.Range",
                "&": "SecondaryRatings.&1.Id"                // & with no children : specialKey : Means use the text value of the key as the input
            }
        }
    }
    */

    private static final ComputedKeysComparator computedKeysComparator = new ComputedKeysComparator();

    // Three different buckets for the children of this CompositeSpec
    protected final List<Spec> specialChildren;         // children that aren't actually triggered off the input data
    protected final Map<String, Spec> literalChildren;  // children that are simple exact matches against the input data
    protected final List<Spec> computedChildren;        // children that are regex matches against the input data

    public CompositeSpec(String rawKey, Map<String, Object> spec ) {
        super( rawKey );

        ArrayList<Spec> sC = new ArrayList<Spec>();
        Map<String, Spec> lC = new HashMap<String, Spec>();
        ArrayList<Spec> cC = new ArrayList<Spec>();

        // self check
        if ( pathElement instanceof AtPathElement ) {
            throw new SpecException( "@ Shiftr key, can not have children." );
        }
        if ( pathElement instanceof DollarPathElement ) {
            throw new SpecException( "$ Shiftr key, can not have children." );
        }

        List<Spec> children = createChildren( spec );

        if ( children.isEmpty() ) {
            throw new SpecException( "Shift Spec format error : Spec line with empty {} as value is not valid." );
        }

        for ( Spec child : children ) {
            if ( child.pathElement instanceof LiteralPathElement ) {
                lC.put( child.pathElement.getRawKey(), child );
            }
            // special is it is "@" or "$"
            else if ( child.pathElement instanceof AtPathElement || (
                      child.pathElement instanceof DollarPathElement ) ) {
                sC.add( child );
            }
            else {   // star || (& with children)
                cC.add( child );
            }
        }

        // Only the computed children need to be sorted
        Collections.sort(cC, computedKeysComparator);

        sC.trimToSize();
        cC.trimToSize();

        specialChildren = Collections.unmodifiableList( sC );
        literalChildren = Collections.unmodifiableMap( lC );
        computedChildren = Collections.unmodifiableList( cC );
    }

    /**
     * Recursively walk the spec input tree.
     */
    private static List<Spec> createChildren( Map<String, Object> rawSpec ) {

        List<Spec> result = new ArrayList<Spec>();
        Set<String> actualKeys = new HashSet<String>();

        for ( String rawLhsStr : rawSpec.keySet() ) {

            Object rawRhs = rawSpec.get( rawLhsStr );
            String[] keyStrings = rawLhsStr.split( "\\|" ); // unwrap the syntactic sugar of the OR
            for ( String keyString : keyStrings ) {

                Spec childSpec;
                if( rawRhs instanceof Map ) {
                    childSpec = new CompositeSpec(keyString, (Map<String, Object>) rawRhs );
                }
                else {
                    childSpec = new LeafSpec(keyString, rawRhs );
                }

                String childCanonicalString = childSpec.pathElement.getCanonicalForm();

                if ( actualKeys.contains( childCanonicalString ) ) {
                    throw new IllegalArgumentException( "Duplicate canonical Shiftr key found : " + childCanonicalString );
                }

                actualKeys.add( childCanonicalString );

                result.add(childSpec);
            }
        }

        return result;
    }

    /**
     * If this Spec matches the inputkey, then perform one step in the Shiftr parallel treewalk.
     *
     * Step one level down the input "tree" by carefully handling the List/Map nature the input to
     *  get the "one level down" data.
     *
     * Step one level down the Spec tree by carefully and efficiently applying our children to the
     *  "one level down" data.
     *
     * @return true if this this spec "handles" the inputkey such that no sibling specs need to see it
     */
    public boolean apply( String inputKey, Object input, WalkedPath walkedPath, Map<String,Object> output )
    {
        LiteralPathElement thisLevel = pathElement.match( inputKey, walkedPath );
        if ( thisLevel == null ) {
            return false;
        }

        walkedPath.add( thisLevel );

        //// 1. Handle any special / key based children first, but don't have them block anything
        for( Spec subSpec : specialChildren ) {
            subSpec.apply( inputKey, input, walkedPath, output );
        }

        //// 2. For each input key value, see if it matches any literal Keys or Computed keys, in that order
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

            List<Object> inputList = (List<Object>) input;

            for (int index = 0; index < inputList.size(); index++) {
                Object subInput = inputList.get( index );
                String subKeyStr = Integer.toString( index );

                applyToLiteralAndComputedChildren( subKeyStr, subInput, walkedPath, output );
            }
        }

        walkedPath.removeLast();

        return true;
    }

    private void applyToLiteralAndComputedChildren( String subKeyStr, Object subInput, WalkedPath walkedPath, Map<String, Object> output ) {

        // TODO : Perf Improvement
        //  If there are no computedChildren, we could iterate across the literalChildren key set, instead of the input keyset, which is potentially large
        //  At spec parse time, determine a execution strategy, so we don't have to do n number of .get calls to an emptySet.

        Spec literalChild = literalChildren.get( subKeyStr );
        if ( literalChild != null ) {
            // Quickly match a literal spec key if possible
            literalChild.apply( subKeyStr, subInput, walkedPath, output );
        }
        else {
            // If no literal spec key matched, iterate thru the computedChildren until we find a match
            // This relies upon the computedChildren having already been sorted in priority order
            for ( Spec computedChild : computedChildren ) {
                // if the computed key does not match it will quickly return false
                if ( computedChild.apply( subKeyStr, subInput, walkedPath, output ) ) {
                    break;
                }
            }
        }
    }


    public static class ComputedKeysComparator implements Comparator<Spec> {

        private static HashMap<Class, Integer> orderMap = new HashMap<Class, Integer>();

        static {
            orderMap.put( AmpPathElement.class, 1 );
            orderMap.put( StarPathElement.class, 2 );
        }

        @Override
        public int compare( Spec a, Spec b ) {

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
