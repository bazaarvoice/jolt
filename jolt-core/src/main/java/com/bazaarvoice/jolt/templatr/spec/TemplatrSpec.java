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

package com.bazaarvoice.jolt.templatr.spec;

import com.bazaarvoice.jolt.common.PathEvaluatingTraversal;
import com.bazaarvoice.jolt.common.TransposeReader;
import com.bazaarvoice.jolt.common.TraversalBuilder;
import com.bazaarvoice.jolt.common.pathelement.ArrayPathElement;
import com.bazaarvoice.jolt.common.pathelement.LiteralPathElement;
import com.bazaarvoice.jolt.common.pathelement.MatchablePathElement;
import com.bazaarvoice.jolt.common.pathelement.StarPathElement;
import com.bazaarvoice.jolt.common.spec.BaseSpec;
import com.bazaarvoice.jolt.common.tree.ArrayMatchedElement;
import com.bazaarvoice.jolt.common.tree.MatchedElement;
import com.bazaarvoice.jolt.common.tree.WalkedPath;
import com.bazaarvoice.jolt.exception.SpecException;
import com.bazaarvoice.jolt.exception.TransformException;
import com.bazaarvoice.jolt.templatr.OpMode;

import java.util.List;
import java.util.Map;

import static com.bazaarvoice.jolt.common.PathElementBuilder.buildMatchablePathElement;

/**
 * Base Templatr spec
 */
public abstract class TemplatrSpec implements BaseSpec {

    // traversal builder that uses a TransposeReader to create a PathEvaluatingTraversal
    protected  static final TraversalBuilder TRAVERSAL_BUILDER = new TraversalBuilder() {
        @Override
        @SuppressWarnings( "unchecked" )
        public <T extends PathEvaluatingTraversal> T buildFromPath( final String path ) {
            return (T) new TransposeReader( path );
        }
    };

    protected final OpMode opMode;
    protected final MatchablePathElement pathElement;

    /**
     * Builds LHS pathElement and validates to specification
     */
    protected TemplatrSpec(String rawJsonKey, OpMode opMode) {
        this.pathElement = buildMatchablePathElement( rawJsonKey );
        if(pathElement instanceof StarPathElement || pathElement instanceof LiteralPathElement || pathElement instanceof ArrayPathElement) {
            this.opMode = opMode;
        }
        else {
            throw new SpecException( opMode.name() + " cannot have " + pathElement.getClass().getSimpleName() + " RHS" );
        }
    }

    @Override
    public MatchablePathElement getPathElement() {
        return pathElement;
    }

    @Override
    public boolean apply( final String inputKey, final Object input, final WalkedPath walkedPath, final Map<String, Object> output, final Map<String, Object> context ) {
        if(output != null) {
            throw new TransformException( "Expected a null output" );
        }

        MatchedElement thisLevel = pathElement.match( inputKey, walkedPath );
        if ( thisLevel == null ) {
            return false;
        }

        return applyElement( inputKey, input, thisLevel, walkedPath, context );
    }

    /**
     * Templatr specific override that is used in BaseSpec#apply(...)
     * The name is changed for easy identification during debugging
     */
    protected abstract boolean applyElement( final String key, final Object input, final MatchedElement thisLevel, final WalkedPath walkedPath, final Map<String, Object> context );

    /**
     * Static utility method for facilitating writes on input object
     *
     * @param parent the source object
     * @param matchedElement the current spec (leaf) element that was matched with input
     * @param value to write
     * @param opMode to determine if write is applicable
     */
    @SuppressWarnings( "unchecked" )
    protected static void setData(Object parent, MatchedElement matchedElement, Object value, OpMode opMode) {
        if(parent instanceof Map) {
            Map source = (Map) parent;
            String key = matchedElement.getRawKey();
            if(opMode.isApplicable( source, key )) {
                source.put( key, value );
            }
        }
        else if (parent instanceof List && matchedElement instanceof ArrayMatchedElement ) {
            List source = (List) parent;
            int origSize = ( (ArrayMatchedElement) matchedElement ).getOrigSize();
            int reqIndex = ( (ArrayMatchedElement) matchedElement ).getRawIndex();
            if(opMode.isApplicable( source, reqIndex, origSize )) {
                source.set( reqIndex, value );
            }
        }
        else {
            throw new RuntimeException( "Should not come here!" );
        }
    }
}
