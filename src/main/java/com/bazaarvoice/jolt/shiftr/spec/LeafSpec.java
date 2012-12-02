package com.bazaarvoice.jolt.shiftr.spec;

import com.bazaarvoice.jolt.Shiftr;
import org.apache.commons.lang.StringUtils;
import com.bazaarvoice.jolt.exception.SpecException;
import com.bazaarvoice.jolt.shiftr.OutputWriter;
import com.bazaarvoice.jolt.shiftr.WalkedPath;
import com.bazaarvoice.jolt.shiftr.pathelement.AtPathElement;
import com.bazaarvoice.jolt.shiftr.pathelement.DollarPathElement;
import com.bazaarvoice.jolt.shiftr.pathelement.LiteralPathElement;

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
public class LeafSpec extends Spec {

    // List of the processed version of the "write specifications"
    protected final List<OutputWriter> outputWriters;

    public LeafSpec( String rawKey, Object rhs ) {
        super( rawKey );

        List<OutputWriter> oP;
        if ( rhs instanceof String ) {
            // leaf level so spec is an dot notation write path
            oP = Arrays.asList( parseOutputDotNotation( rhs ) );
        }
        else if ( rhs instanceof List ) {
            // leaf level list
            // Spec : "foo": ["a", "b"] : Shift the value of "foo" to both "a" and "b"
            List<Object> rhsList = (List<Object>) rhs;
            oP = new ArrayList<OutputWriter>( rhsList.size() );
            for ( Object dotNotation : rhsList ) {
                oP.add( parseOutputDotNotation( dotNotation ) );
            }
        }
        else {
            throw new SpecException( "Invalid Shiftr spec RHS.  Should be map, string, or array of strings.  Spec in question : " + rhs );
        }

        outputWriters = Collections.unmodifiableList( oP );
    }

    private static OutputWriter parseOutputDotNotation( Object rawObj ) {

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
        else
        {
            outputPathStr = Shiftr.ROOT_KEY + "." + outputPathStr;
        }

        return new OutputWriter(outputPathStr);
    }

    /**
     * If this Spec matches the inputkey, then do the work of outputting data and return true.
     *
     * @return true if this this spec "handles" the inputkey such that no sibling specs need to see it
     */
    public boolean apply( String inputKey, Object input, WalkedPath walkedPath, Map<String,Object> output ){

        LiteralPathElement thisLevel = pathElement.match( inputKey, walkedPath );
        if ( thisLevel == null ) {
            return false;
        }

        Object data;
        Boolean returnValue;

        if ( this.pathElement instanceof DollarPathElement ) {
            DollarPathElement subRef = (DollarPathElement) this.pathElement;

            // The data is the parent key, so evaluate against the parent's path
            data = subRef.evaluate( walkedPath );
            returnValue = false;  // don't block further Shiftr matches
        }
        else if ( this.pathElement instanceof AtPathElement ) {

            // The data is our parent's data
            data = input;
            returnValue = false;  // don't block further Shiftr matches
        }
        else {
            // the data is the input
            data = input;
            // tell our parent that we matched and no further processing for this inputKey should be done
            returnValue = true;
        }

        // Add our the LiteralPathElement for this level, so that write path References can use it as &(0,0)
        walkedPath.add( thisLevel );

        // Place the data in the write
        for ( OutputWriter outputPath : outputWriters ) {
            outputPath.write( data, output, walkedPath );
        }

        walkedPath.removeLast();

        return returnValue;
    }
}
