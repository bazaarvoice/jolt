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
package com.bazaarvoice.jolt.cardinality;

import com.bazaarvoice.jolt.common.Optional;
import com.bazaarvoice.jolt.common.pathelement.AtPathElement;
import com.bazaarvoice.jolt.common.pathelement.LiteralPathElement;
import com.bazaarvoice.jolt.common.pathelement.MatchablePathElement;
import com.bazaarvoice.jolt.common.pathelement.PathElement;
import com.bazaarvoice.jolt.common.pathelement.StarAllPathElement;
import com.bazaarvoice.jolt.common.pathelement.StarRegexPathElement;
import com.bazaarvoice.jolt.common.pathelement.StarSinglePathElement;
import com.bazaarvoice.jolt.common.spec.BaseSpec;
import com.bazaarvoice.jolt.common.tree.WalkedPath;
import com.bazaarvoice.jolt.exception.SpecException;
import com.bazaarvoice.jolt.utils.StringTools;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * A Spec Object represents a single line from the JSON Cardinality Spec.
 *
 * At a minimum a single Spec has :
 *   Raw LHS spec value
 *   Some kind of PathElement (based off that raw LHS value)
 *
 * Additionally there are 2 distinct subclasses of the base Spec
 *  CardinalityLeafSpec : where the RHS is either "ONE" or "MANY"
 *  CardinalityCompositeSpec : where the RHS is a map of children Specs
 *
 * The tree structure of formed by the CompositeSpecs is what is used during the transform
 *  to do the parallel tree walk with the input data tree.
 *
 * During the parallel tree walk, a Path<Literal PathElements> is maintained, and used when
 *  a tree walk encounters a leaf spec.
 */
public abstract class CardinalitySpec implements BaseSpec {

    private static final String STAR = "*";
    private static final String AT = "@";

    // The processed key from the JSON config
    protected final MatchablePathElement pathElement;

    public CardinalitySpec( String rawJsonKey ) {
        List<PathElement> pathElements = parse( rawJsonKey );

        if ( pathElements.size() != 1 ){
            throw new SpecException( "CardinalityTransform invalid LHS:" + rawJsonKey + " can not contain '.'" );
        }

        PathElement pe =  pathElements.get( 0 );
        if ( ! ( pe instanceof MatchablePathElement ) ) {
            throw new SpecException( "Spec LHS key=" + rawJsonKey + " is not a valid LHS key." );
        }

        this.pathElement = (MatchablePathElement) pe;
    }

    // once all the cardinalitytransform specific logic is extracted.
    public static List<PathElement> parse( String key )  {

        if ( key.contains(AT) ) {
            return Arrays.<PathElement>asList( new AtPathElement( key ) );
        }
        else if ( STAR.equals(key) ) {
            return Arrays.<PathElement>asList( new StarAllPathElement( key ) );
        }
        else if ( key.contains(STAR) ) {
            if ( StringTools.countMatches(key, STAR) == 1 ) {
                return Arrays.<PathElement>asList( new StarSinglePathElement( key ) );
            }
            else {
                return Arrays.<PathElement>asList( new StarRegexPathElement( key ) );
            }
        }
        else {
            return Arrays.<PathElement>asList( new LiteralPathElement( key ) );
        }
    }

    /**
     * This is the main recursive method of the CardinalityTransform parallel "spec" and "input" tree walk.
     *
     * It should return true if this Spec object was able to successfully apply itself given the
     *  inputKey and input object.
     *
     * In the context of the CardinalityTransform parallel treewalk, if this method returns a non-null Object,
     * the assumption is that no other sibling Cardinality specs need to look at this particular input key.
     *
     * @return true if this this spec "handles" the inputkey such that no sibling specs need to see it
     */
    public abstract boolean applyCardinality( String inputKey, Object input, WalkedPath walkedPath, Object parentContainer );

    @Override
    public boolean apply( final String inputKey, final Optional<Object> inputOptional, final WalkedPath walkedPath, final Map<String, Object> output, final Map<String, Object> context ) {
        return applyCardinality( inputKey, inputOptional.get(), walkedPath, output );
    }

    @Override
    public MatchablePathElement getPathElement() {
        return pathElement;
    }
}
