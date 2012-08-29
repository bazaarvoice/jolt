package com.bazaarvoice.jolt.defaultr;

import com.bazaarvoice.jolt.Defaultr;
import static com.bazaarvoice.jolt.defaultr.DefaultrOPS.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DefaultrKey {

    /**
     * Factory-ish method that recursively processes a Map<String, Object> into a Map<DefaultrKey, Object>.
     *
     * @param spec Simple Jackson default Map<String,Object> input
     * @return processed spec
     */
    public static Map<DefaultrKey, Object> parseSpec( Map<String, Object> spec ) {
        return processSpec( false, spec );
    }

    /**
     * Recursively walk the spec input tree.  Handle arrays by telling DefaultrKeys if they need to be ArrayKeys, and
     *  to find the max default array length.
     */
    private static Map<DefaultrKey, Object> processSpec( boolean parentIsArray, Map<String, Object> spec ) {

        Map<DefaultrKey, Object> result = new LinkedHashMap<DefaultrKey, Object>();

        for ( String key : spec.keySet() ) {
            DefaultrKey dk = new DefaultrKey( parentIsArray, key );

            Object obj = spec.get( key );
            // Spec is String -> Map or String -> Literal only
            if ( obj instanceof Map ) {
                Map<DefaultrKey, Object> children = processSpec( dk.isArrayOutput, (Map<String, Object>) obj );
                result.put( dk, children );

                if ( dk.isArrayOutput ) {
                    // loop over children and find the max literal value
                    for( DefaultrKey dKey : children.keySet() ) {
                        dk.maxLiteralKey = dKey.getMaxValue( dk.maxLiteralKey );
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


    private Collection keyValues;
    private int keyInt = -1;

    public DefaultrOPS op = null;
    // Am I supposed to be a key in a Json array?  If so I need to speak Int key values.
    public boolean isArrayKey = false;

    // Am I supposed to be parent of an array?  If so I need to make sure that I inform
    //  my children they need to be ArrayKeys, and I need to make sure that the output array
    //  I will write to is big enough.
    public boolean isArrayOutput = false;

    public int orCount = 0;
    public int maxLiteralKey = -1;

    private static final String OR_INPUT_REGEX = "\\" + Defaultr.WildCards.OR;

    public DefaultrKey( boolean isArrayKey, String jsonKey ) {

        String literalValue = jsonKey;
        this.isArrayKey = isArrayKey;

        if ( literalValue.endsWith( Defaultr.WildCards.ARRAY ) ) {
            isArrayOutput = true;
            literalValue = literalValue.replace( Defaultr.WildCards.ARRAY, "" );
        }

        //// FIGURE OUT WHAT THE keyValues ARE
        op = DefaultrOPS.parse( literalValue );
        if ( op == OR ) {
            List<String> keyStrings = Arrays.asList( literalValue.split( OR_INPUT_REGEX ) );
            orCount = keyStrings.size();

            if ( isArrayKey ) {
                keyValues = new ArrayList<Integer>();
                for( String orLiteral : keyStrings ) {
                    int orInt = Integer.parseInt( orLiteral );
                    keyValues.add( orInt );
                }
            }
            else {
                keyValues = keyStrings;
            }
        }

        if ( op == LITERAL ) {
            if ( isArrayKey ) {
                keyInt = Integer.parseInt( literalValue );
                keyValues = Arrays.asList( keyInt );
            }
            else {
                keyValues = Arrays.asList( literalValue );
            }
        }

        if ( keyValues == null ) {
            keyValues = Collections.emptyList();
        }
    }

    public Collection getKeyValues() {
        return keyValues;
    }

    private int getMaxValue( int currentMaxValue ) {

        // only force the defaultee array size to grow if we are literal Key and we are going to default beyond the current max
        if ( op == LITERAL && keyInt > currentMaxValue ) {
            return keyInt;
        }
        return currentMaxValue;
    }

    public static class DefaultrKeyComparator implements Comparator<DefaultrKey> {

        private DefaultrOPS.OpsPrecedenceComparator opsComparator = new DefaultrOPS.OpsPrecedenceComparator();

        @Override
        public int compare(DefaultrKey a, DefaultrKey b) {

            int opsEqual = opsComparator.compare(a.op, b.op );

            if ( opsEqual == 0 && OR == a.op && OR == b.op )
            {
                // For deterministic behavior, sub sort on the specificity of the OR
                //   aka as an Or, the more star like, the higher your value
                return (a.orCount < b.orCount ? -1 : (a.orCount == b.orCount ? 0 : 1));

                // TODO : if the orCounts are the same, make more deterministic?
            }

            return opsEqual;
        }
    }

}
