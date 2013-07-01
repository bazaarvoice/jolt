/*
 * Copyright 2013 Bazaarvoice, Inc.

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *     http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bazaarvoice.jolt.shiftr.spec;

import com.bazaarvoice.jolt.Shiftr;
import com.bazaarvoice.jolt.shiftr.ShiftrWriter;
import org.apache.commons.lang.StringUtils;
import com.bazaarvoice.jolt.exception.SpecException;
import com.bazaarvoice.jolt.common.WalkedPath;
import com.bazaarvoice.jolt.common.pathelement.AtPathElement;
import com.bazaarvoice.jolt.common.pathelement.DollarPathElement;
import com.bazaarvoice.jolt.common.pathelement.LiteralPathElement;

import java.util.Arrays;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Leaf level Spec object.
 *
 * If this Spec's PathElement matches the input (successful parallel tree walk)
 *  this Spec has the information needed to write the given data to the output object.
 */
public class ShiftrLeafSpec extends ShiftrSpec {

    // List of the processed version of the "write specifications"
    private final List<ShiftrWriter> shiftrWriters;

    public ShiftrLeafSpec( String rawKey, Object rhs ) {
        super( rawKey );

        List<ShiftrWriter> writers;
        if ( rhs instanceof String ) {
            // leaf level so spec is an dot notation write path
            writers = Arrays.asList( parseOutputDotNotation( rhs ) );
        }
        else if ( rhs instanceof List ) {
            // leaf level list
            // Spec : "foo": ["a", "b"] : Shift the value of "foo" to both "a" and "b"
            List<Object> rhsList = (List<Object>) rhs;
            writers = new ArrayList<ShiftrWriter>( rhsList.size() );
            for ( Object dotNotation : rhsList ) {
                writers.add( parseOutputDotNotation( dotNotation ) );
            }
        }
        else {
            throw new SpecException( "Invalid Shiftr spec RHS.  Should be map, string, or array of strings.  Spec in question : " + rhs );
        }

        shiftrWriters = Collections.unmodifiableList( writers );
    }

    private static ShiftrWriter parseOutputDotNotation( Object rawObj ) {

        if ( ! ( rawObj instanceof String ) ) {
            throw new SpecException( "Invalid Shiftr spec RHS.  Should be a string or array of Strings.   Value in question : " + rawObj );
        }

        // Prepend "root" to each output path.
        // This is needed for the "identity" transform, eg if we are just supposed to put the input into the output
        //  what key do we put it under?
        String outputPathStr = (String) rawObj;
        if ( StringUtils.isBlank( outputPathStr ) ) {
            outputPathStr = Shiftr.ROOT_KEY;
        }
        else {
            outputPathStr = Shiftr.ROOT_KEY + "." + outputPathStr;
        }

        return new ShiftrWriter(outputPathStr);
    }

    /**
     * If this Spec matches the inputkey, then do the work of outputting data and return true.
     *
     * @return true if this this spec "handles" the inputkey such that no sibling specs need to see it
     */
    @Override
    public boolean apply( String inputKey, Object input, WalkedPath walkedPath, Map<String,Object> output ){

        LiteralPathElement thisLevel = pathElement.match( inputKey, walkedPath );
        if ( thisLevel == null ) {
            return false;
        }

        Object data;
        boolean realChild;

        if ( this.pathElement instanceof DollarPathElement ) {
            DollarPathElement subRef = (DollarPathElement) this.pathElement;

            // The data is the parent key, so evaluate against the parent's path
            data = subRef.evaluate( walkedPath );
            realChild = false;  // don't block further Shiftr matches
        }
        else if ( this.pathElement instanceof AtPathElement ) {

            // The data is our parent's data
            data = input;
            realChild = false;  // don't block further Shiftr matches
        }
        else {
            // the data is the input
            data = input;
            // tell our parent that we matched and no further processing for this inputKey should be done
            realChild = true;
        }

        // Add our the LiteralPathElement for this level, so that write path References can use it as &(0,0)
        walkedPath.add( thisLevel );

        // Write out the data
        for ( ShiftrWriter outputPath : shiftrWriters ) {
            outputPath.write( data, output, walkedPath );
        }

        walkedPath.removeLast();

        if ( realChild ) {
            // we were a "real" child, so increment the matchCount of our parent
            walkedPath.lastElement().incrementHashCount();
        }

        return realChild;
    }
}