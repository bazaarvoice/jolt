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
package com.bazaarvoice.jolt.shiftr.spec;

import com.bazaarvoice.jolt.common.ComputedKeysComparator;
import com.bazaarvoice.jolt.common.ExecutionStrategy;
import com.bazaarvoice.jolt.common.Optional;
import com.bazaarvoice.jolt.common.pathelement.AmpPathElement;
import com.bazaarvoice.jolt.common.pathelement.AtPathElement;
import com.bazaarvoice.jolt.common.pathelement.DollarPathElement;
import com.bazaarvoice.jolt.common.pathelement.HashPathElement;
import com.bazaarvoice.jolt.common.pathelement.LiteralPathElement;
import com.bazaarvoice.jolt.common.pathelement.StarAllPathElement;
import com.bazaarvoice.jolt.common.pathelement.StarDoublePathElement;
import com.bazaarvoice.jolt.common.pathelement.StarPathElement;
import com.bazaarvoice.jolt.common.pathelement.StarRegexPathElement;
import com.bazaarvoice.jolt.common.pathelement.StarSinglePathElement;
import com.bazaarvoice.jolt.common.pathelement.TransposePathElement;
import com.bazaarvoice.jolt.common.spec.BaseSpec;
import com.bazaarvoice.jolt.common.spec.OrderedCompositeSpec;
import com.bazaarvoice.jolt.common.spec.SpecBuilder;
import com.bazaarvoice.jolt.common.tree.MatchedElement;
import com.bazaarvoice.jolt.common.tree.WalkedPath;
import com.bazaarvoice.jolt.exception.SpecException;
import com.bazaarvoice.jolt.shiftr.ShiftrSpecBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Spec that has children, which it builds and then manages during Transforms.
 */
public class ShiftrCompositeSpec extends ShiftrSpec implements OrderedCompositeSpec {

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

    private static final HashMap<Class, Integer> orderMap;
    private static final ComputedKeysComparator computedKeysComparator;
    private static final SpecBuilder<ShiftrSpec> specBuilder;

    static {
        orderMap = new HashMap<>();
        orderMap.put( AmpPathElement.class, 1 );
        orderMap.put( StarRegexPathElement.class, 2 );
        orderMap.put( StarDoublePathElement.class, 3 );
        orderMap.put( StarSinglePathElement.class, 4 );
        orderMap.put( StarAllPathElement.class, 5 );
        computedKeysComparator = ComputedKeysComparator.fromOrder( orderMap );
        specBuilder = new ShiftrSpecBuilder();
    }

    // Three different buckets for the children of this CompositeSpec
    private final List<ShiftrSpec> specialChildren;         // children that aren't actually triggered off the input data
    private final Map<String, ShiftrSpec> literalChildren;  // children that are simple exact matches against the input data
    private final List<ShiftrSpec> computedChildren;        // children that are regex matches against the input data
    private final ExecutionStrategy executionStrategy;

    public ShiftrCompositeSpec(String rawKey, Map<String, Object> spec ) {
        super( rawKey );

        ArrayList<ShiftrSpec> special = new ArrayList<>();
        Map<String, ShiftrSpec> literals = new LinkedHashMap<>();
        ArrayList<ShiftrSpec> computed = new ArrayList<>();

        // self check
        if ( pathElement instanceof AtPathElement ) {
            throw new SpecException( "@ Shiftr key, can not have children." );
        }
        if ( pathElement instanceof DollarPathElement ) {
            throw new SpecException( "$ Shiftr key, can not have children." );
        }

        List<ShiftrSpec> children = specBuilder.createSpec( spec );

        if ( children.isEmpty() ) {
            throw new SpecException( "Shift ShiftrSpec format error : ShiftrSpec line with empty {} as value is not valid." );
        }

        for ( ShiftrSpec child : children ) {
            if ( child.pathElement instanceof LiteralPathElement ) {
                literals.put( child.pathElement.getRawKey(), child );
            }
            // special is it is "@" or "$"
            else if ( child.pathElement instanceof AtPathElement ||
                      child.pathElement instanceof HashPathElement ||
                      child.pathElement instanceof DollarPathElement ||
                      child.pathElement instanceof TransposePathElement ) {
                special.add( child );
            }
            else {   // star || (& with children)
                computed.add( child );
            }
        }

        // Only the computed children need to be sorted
        Collections.sort( computed, computedKeysComparator );

        special.trimToSize();
        computed.trimToSize();

        specialChildren = Collections.unmodifiableList( special );
        literalChildren = Collections.unmodifiableMap( literals );
        computedChildren = Collections.unmodifiableList( computed );

        executionStrategy = determineExecutionStrategy();
    }


    @Override
    public Map<String, ShiftrSpec> getLiteralChildren() {
        return literalChildren;
    }

    @Override
    public List<ShiftrSpec> getComputedChildren() {
        return computedChildren;
    }

    @Override
    public ExecutionStrategy determineExecutionStrategy() {
        if ( computedChildren.isEmpty() ) {
            return ExecutionStrategy.AVAILABLE_LITERALS;
        }
        else if ( literalChildren.isEmpty() ) {
            return ExecutionStrategy.COMPUTED;
        }

        for ( BaseSpec computed : computedChildren ) {
            if ( ! ( computed.getPathElement() instanceof StarPathElement ) ) {
                return ExecutionStrategy.CONFLICT;
            }

            StarPathElement starPathElement = (StarPathElement) computed.getPathElement();

            for ( String literal : literalChildren.keySet() ) {
                if ( starPathElement.stringMatch( literal ) ) {
                    return ExecutionStrategy.CONFLICT;
                }
            }
        }

        return ExecutionStrategy.AVAILABLE_LITERALS_WITH_COMPUTED;
    }

    /**
     * If this Spec matches the inputKey, then perform one step in the Shiftr parallel treewalk.
     *
     * Step one level down the input "tree" by carefully handling the List/Map nature the input to
     *  get the "one level down" data.
     *
     * Step one level down the Spec tree by carefully and efficiently applying our children to the
     *  "one level down" data.
     *
     * @return true if this this spec "handles" the inputKey such that no sibling specs need to see it
     */
    @Override
    public boolean apply( String inputKey, Optional<Object> inputOptional, WalkedPath walkedPath, Map<String,Object> output, Map<String, Object> context )
    {
        MatchedElement thisLevel = pathElement.match( inputKey, walkedPath );
        if ( thisLevel == null ) {
            return false;
        }

        // If we are a TransposePathElement, try to swap the "input" with what we lookup from the Transpose
        if ( pathElement instanceof TransposePathElement ) {

            TransposePathElement tpe = (TransposePathElement) this.pathElement;

            // Note the data found may not be a String, thus we have to call the special objectEvaluate
            // Optional, because the input data could have been a valid null.
            Optional<Object> optional = tpe.objectEvaluate( walkedPath );
            if ( !optional.isPresent() ) {
                return false;
            }
            inputOptional = optional;
        }

        // add ourselves to the path, so that our children can reference us
        walkedPath.add( inputOptional.get(), thisLevel );

        // Handle any special / key based children first, but don't have them block anything
        for( ShiftrSpec subSpec : specialChildren ) {
            subSpec.apply( inputKey, inputOptional, walkedPath, output, context );
        }

        // Handle the rest of the children
        executionStrategy.process( this, inputOptional, walkedPath, output, context );

        // We are done, so remove ourselves from the walkedPath
        walkedPath.removeLast();

        // we matched so increment the matchCount of our parent
        walkedPath.lastElement().getMatchedElement().incrementHashCount();

        return true;
    }
}
