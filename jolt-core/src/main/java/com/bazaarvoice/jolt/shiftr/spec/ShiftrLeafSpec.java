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

import com.bazaarvoice.jolt.common.Optional;
import com.bazaarvoice.jolt.common.PathEvaluatingTraversal;
import com.bazaarvoice.jolt.common.TraversalBuilder;
import com.bazaarvoice.jolt.common.pathelement.AtPathElement;
import com.bazaarvoice.jolt.common.pathelement.DollarPathElement;
import com.bazaarvoice.jolt.common.pathelement.HashPathElement;
import com.bazaarvoice.jolt.common.pathelement.TransposePathElement;
import com.bazaarvoice.jolt.common.tree.MatchedElement;
import com.bazaarvoice.jolt.common.tree.WalkedPath;
import com.bazaarvoice.jolt.exception.SpecException;
import com.bazaarvoice.jolt.shiftr.ShiftrWriter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Leaf level Spec object.
 *
 * If this Spec's PathElement matches the input (successful parallel tree walk)
 *  this Spec has the information needed to write the given data to the output object.
 */
public class ShiftrLeafSpec extends ShiftrSpec {

    // traversal builder that uses a ShifterWriter to create a PathEvaluatingTraversal
    private static final TraversalBuilder TRAVERSAL_BUILDER = new TraversalBuilder() {
        @Override
        @SuppressWarnings( "unchecked" )
        public <T extends PathEvaluatingTraversal> T buildFromPath( final String path ) {
            return (T) new ShiftrWriter( path );
        }
    };

    // List of the processed version of the "write specifications"
    private final List<? extends PathEvaluatingTraversal> shiftrWriters;

    public ShiftrLeafSpec( String rawKey, Object rhs ) {
        super( rawKey );

        List<PathEvaluatingTraversal> writers;
        if ( rhs instanceof String ) {
            // leaf level so spec is an dot notation write path
            writers = Arrays.asList( TRAVERSAL_BUILDER.build( rhs ) );
        }
        else if ( rhs instanceof List ) {
            // leaf level list
            // Spec : "foo": ["a", "b"] : Shift the value of "foo" to both "a" and "b"
            @SuppressWarnings( "unchecked" )
            List<Object> rhsList = (List<Object>) rhs;
            writers = new ArrayList<>( rhsList.size() );
            for ( Object dotNotation : rhsList ) {
                writers.add( TRAVERSAL_BUILDER.build( dotNotation ) );
            }
        }
        else if ( rhs == null ) {
            // this means someone wanted to match something, but not send it anywhere.  Basically like a removal.
            writers = Collections.emptyList();
        }
        else {
            throw new SpecException( "Invalid Shiftr spec RHS.  Should be map, string, or array of strings.  Spec in question : " + rhs );
        }

        shiftrWriters = Collections.unmodifiableList( writers );
    }

    /**
     * If this Spec matches the inputkey, then do the work of outputting data and return true.
     *
     * @return true if this this spec "handles" the inputkey such that no sibling specs need to see it
     */
    @Override
    public boolean apply( String inputKey, Optional<Object> inputOptional, WalkedPath walkedPath, Map<String,Object> output, Map<String, Object> context){

        Object input = inputOptional.get();
        MatchedElement thisLevel = pathElement.match( inputKey, walkedPath );
        if ( thisLevel == null ) {
            return false;
        }

        Object data;
        boolean realChild = false;  // by default don't block further Shiftr matches

        if ( this.pathElement instanceof DollarPathElement ||
             this.pathElement instanceof HashPathElement ) {

            // The data is already encoded in the thisLevel object created by the pathElement.match called above
            data = thisLevel.getCanonicalForm();
        }
        else if ( this.pathElement instanceof AtPathElement ) {

            // The data is our parent's data
            data = input;
        }
        else if ( this.pathElement instanceof TransposePathElement ) {
            // We try to walk down the tree to find the value / data we want
            TransposePathElement tpe = (TransposePathElement) this.pathElement;

            // Note the data found may not be a String, thus we have to call the special objectEvaluate
            Optional<Object> evaledData = tpe.objectEvaluate( walkedPath );
            if ( evaledData.isPresent() ) {
                data = evaledData.get();
            }
            else {
                // if we could not find the value we want looking down the tree, bail
                return false;
            }
        }
        else {
            // the data is the input
            data = input;
            // tell our parent that we matched and no further processing for this inputKey should be done
            realChild = true;
        }

        // Add our the LiteralPathElement for this level, so that write path References can use it as &(0,0)
        walkedPath.add( input, thisLevel );

        // Write out the data
        for ( PathEvaluatingTraversal outputPath : shiftrWriters ) {
            outputPath.write( data, output, walkedPath );
        }

        walkedPath.removeLast();

        if ( realChild ) {
            // we were a "real" child, so increment the matchCount of our parent
            walkedPath.lastElement().getMatchedElement().incrementHashCount();
        }

        return realChild;
    }
}
