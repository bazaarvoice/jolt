/*
 * Copyright 2013 Bazaarvoice, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bazaarvoice.jolt.defaultr;

import com.bazaarvoice.jolt.Defaultr;
import com.bazaarvoice.jolt.exception.TransformException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.bazaarvoice.jolt.defaultr.OPS.OR;

public abstract class Key {

    /**
     * Factory-ish method that recursively processes a Map<String, Object> into a Set<Key> objects.
     *
     * @param spec Simple Jackson default Map<String,Object> input
     * @return Set of Keys from this level in the spec
     */
    public static Set<Key> parseSpec( Map<String, Object> spec ) {
        return processSpec( false, spec );
    }

    /**
     * Recursively walk the spec input tree.  Handle arrays by telling DefaultrKeys if they need to be ArrayKeys, and
     *  to find the max default array length.
     */
    private static Set<Key> processSpec( boolean parentIsArray, Map<String, Object> spec ) {

        // TODO switch to List<Key> and sort before returning

        Set<Key> result = new HashSet<>();

        for ( String key : spec.keySet() ) {
            Object subSpec = spec.get( key );
            if ( parentIsArray ) {
                result.add( new ArrayKey( key, subSpec ) ); // this will recursively call processSpec if needed
            }
            else {
                result.add( new MapKey( key, subSpec ) ); // this will recursively call processSpec if needed
            }
        }

        return result;
    }


    private static final String OR_INPUT_REGEX = "\\" + Defaultr.WildCards.OR;
    private static final Key.KeyPrecedenceComparator keyComparator = new Key.KeyPrecedenceComparator();

    // Am I supposed to be parent of an array?  If so I need to make sure that I inform
    //  my children they need to be ArrayKeys, and I need to make sure that the output array
    //  I will write to is big enough.
    private boolean isArrayOutput = false;
    private OPS op = null;
    private int orCount = 0;
    private int outputArraySize = -1;

    protected Set<Key> children = null;
    protected Object literalValue = null;

    protected String rawKey;
    protected List<String> keyStrings;

    public Key( String rawJsonKey, Object spec ) {

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

        // Spec is String -> Map   or   String -> Literal only
        if ( spec instanceof Map ) {
            children = processSpec( isArrayOutput(), (Map<String, Object>) spec );

            if ( isArrayOutput() ) {
                // loop over children and find the max literal value
                for( Key childKey : children ) {
                    int childValue = childKey.getLiteralIntKey();
                    if ( childValue > outputArraySize ) {
                        outputArraySize = childValue;
                    }
                }
            }
        }
        else {
            // literal such as String, number, or JSON array
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
            throw new TransformException( "Defaultee should never be null when " +
                    "passed to the applyChildren method." );
        }

        // This has nothing to do with this being an ArrayKey or MapKey, instead this is about
        //  this key being the parent of an Array in the output.
        if ( isArrayOutput() && defaultee instanceof List) {

            @SuppressWarnings( "unchecked" )
            List<Object> defaultList = (List<Object>) defaultee;

            // Extend the defaultee list if needed
            for ( int index = defaultList.size() - 1; index < getOutputArraySize(); index++ ) {
                defaultList.add( null );
            }
        }

        // Find and sort the children DefaultrKeys by precedence: literals, |, then *
        ArrayList<Key> sortedChildren = new ArrayList<>();
        sortedChildren.addAll( children );
        Collections.sort( sortedChildren, keyComparator );

        for ( Key childKey : sortedChildren ) {
            childKey.applyChild( defaultee );
        }
    }

    protected abstract int getLiteralIntKey();

    /**
     * Apply this Key to the defaultee.
     *
     * If this Key is a WildCard key, this may apply to many entries in the container.
     */
    protected abstract void applyChild( Object container );

    public int getOrCount() {
       return orCount;
    }

    public boolean isArrayOutput() {
        return isArrayOutput;
    }

    public OPS getOp() {
        return op;
    }

    public int getOutputArraySize() {
        return outputArraySize;
    }

    public Object createOutputContainerObject() {
        if ( isArrayOutput() ) {
            return new ArrayList<>();
        } else {
            return new LinkedHashMap<String, Object>();
        }
    }

    public static class KeyPrecedenceComparator implements Comparator<Key> {

        private final OPS.OpsPrecedenceComparator opsComparator = new OPS.OpsPrecedenceComparator();

        @Override
        public int compare(Key a, Key b) {

            int opsEqual = opsComparator.compare(a.getOp(), b.getOp() );

            if ( opsEqual == 0 && OR == a.getOp() && OR == b.getOp() )
            {
                // For deterministic behavior, sub sort on the specificity of the OR and then alphabetically on the rawKey
                //   For the Or, the more star like, the higher your value
                //   If the or count matches, fall back to alphabetical on the rawKey from the spec file
                return (a.getOrCount() < b.getOrCount() ? -1 : (a.getOrCount() == b.getOrCount() ? a.rawKey.compareTo( b.rawKey ) : 1 ) );
            }

            return opsEqual;
        }
    }

}
