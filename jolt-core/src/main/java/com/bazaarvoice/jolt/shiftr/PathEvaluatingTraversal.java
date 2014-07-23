/*
 * Copyright 2014 Bazaarvoice, Inc.
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
package com.bazaarvoice.jolt.shiftr;

import com.bazaarvoice.jolt.common.WalkedPath;
import com.bazaarvoice.jolt.common.pathelement.TransposePathElement;
import com.bazaarvoice.jolt.common.pathelement.EvaluatablePathElement;
import com.bazaarvoice.jolt.common.pathelement.PathElement;
import com.bazaarvoice.jolt.exception.SpecException;
import com.bazaarvoice.jolt.shiftr.spec.ShiftrSpec;
import com.bazaarvoice.jolt.traversr.Traversr;
import com.bazaarvoice.jolt.utils.StringTools;

import java.util.*;

/**
 * Combines a Traversr with the ability to evaluate References against a WalkedPath.
 *
 * Convenience class for path based off a single dot notation String,
 *  like "rating.&1(2).&.value".
 *
 * This processes the dot notation path into internal data structures, so
 *  that the String processing only happens once.
 */
public abstract class PathEvaluatingTraversal {

    private final List<PathElement> elements;
    private final Traversr traversr;

    public PathEvaluatingTraversal( String dotNotation ) {

        if ( dotNotation.contains("*") || dotNotation.contains("$")) {
            throw new SpecException("DotNotation (write key) can not contain '*' or '$'.");
        }

        List<PathElement> paths;
        Traversr trav;

        if ( StringTools.isNotBlank( dotNotation ) ) {

            // Compute the path elements.
            paths = ShiftrSpec.parseDotNotationRHS( dotNotation );

            // Use the canonical versions of the path elements to create the Traversr
            List<String> traversrPaths = new ArrayList<String>( paths.size() );
            for ( PathElement pe : paths ) {
                if ( pe instanceof TransposePathElement ) {
                    // tell the traversr that we will provide it with a literal String eventually
                    // Aka to be a MapStep
                    traversrPaths.add( "DownwardTransposePathElement" );
                }
                else {
                    traversrPaths.add( pe.getCanonicalForm() );
                }
            }
            trav = createTraversr( traversrPaths );
        }
        else {
            paths = Collections.emptyList();
            trav = createTraversr( Arrays.asList( "" ) );
        }

        List<PathElement> evalPaths = new ArrayList<PathElement>( paths.size() );
        for( PathElement pe : paths ) {
            if ( ! ( pe instanceof EvaluatablePathElement || pe instanceof TransposePathElement) ) {
                throw new SpecException( "RHS key=" + pe.getRawKey() + " is not a valid RHS key." );
            }

            evalPaths.add( pe );
        }

        this.elements = Collections.unmodifiableList( evalPaths );
        this.traversr = trav;
    }

    protected abstract Traversr createTraversr(List<String> paths);

    /**
     * Use the supplied WalkedPath, in the evaluation of each of our PathElements to
     *  build a concrete output path.  Then use that output path to write the given
     *  data to the output.
     *
     * @param data data to write
     * @param output data structure we are going to write the data to
     * @param walkedPath reference used to lookup reference values like "&1(2)"
     */
    public void write( Object input, Object data, Map<String, Object> output, WalkedPath walkedPath ) {
        List<String> evaledPaths = evaluate( input, walkedPath );
        if ( evaledPaths != null ) {
            traversr.set( output, evaledPaths, data );
        }
    }

    public Object read( Object data, WalkedPath walkedPath ) {
        List<String> evaledPaths = evaluate( null, walkedPath );
        if ( evaledPaths == null ) {
            return null;
        }
        return traversr.get( data, evaledPaths );
    }

    /**
     * Use the supplied WalkedPath, in the evaluation of each of our PathElements.
     *
     * If our PathElements contained a TransposePathElement, we may return null.
     *
     * @param input
     * @param walkedPath used to lookup/evaluate PathElement references values like "&1(2)"
     * @return null or fully evaluated Strings, possibly with concrete array references like "photos.[3]"
     */
    // Visible for testing
    List<String> evaluate( Object input, WalkedPath walkedPath ) {

        List<String> strings = new ArrayList<String>(elements.size());
        for ( PathElement pathElement : elements ) {

            String evaledLeafOutput;
            if ( pathElement instanceof TransposePathElement ) {
                TransposePathElement ptpe = (TransposePathElement)  pathElement;

                Object dataFromTranspose = ptpe.getSubPathReader().read( input, walkedPath );
                if ( dataFromTranspose == null || ! ( dataFromTranspose instanceof String ) ) {

                    // If this output path has a TransposePathElement, and when we evaluate it
                    //  it does not resolve to a String, then return null
                    return null;
                }

                evaledLeafOutput = (String) ptpe.getSubPathReader().read( input, walkedPath );
            }
            else {
                EvaluatablePathElement pte = (EvaluatablePathElement) pathElement;
                evaledLeafOutput = pte.evaluate( walkedPath );
            }

            strings.add( evaledLeafOutput );
        }

        return strings;
    }

    public int size() {
        return elements.size();
    }

    public PathElement get( int index ) {
        return elements.get( index );
    }

    /**
     * Testing method.
     */
    public String getCanonicalForm() {
        StringBuilder buf = new StringBuilder();

        for ( PathElement pe : elements ) {
            buf.append( "." ).append( pe.getCanonicalForm() );
        }

        return buf.substring( 1 ); // strip the leading "."
    }
}
