package com.bazaarvoice.jolt.shiftr;

import com.bazaarvoice.jolt.common.WalkedPath;
import com.bazaarvoice.jolt.exception.SpecException;
import com.bazaarvoice.jolt.common.pathelement.EvaluatablePathElement;
import com.bazaarvoice.jolt.common.pathelement.PathElement;
import com.bazaarvoice.jolt.shiftr.spec.ShiftrSpec;
import com.bazaarvoice.jolt.traversr.Traversr;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Convenience class for path based off a single dot notation String,
 *  like "rating.&1(2).&.value".
 *
 * This processes the dot notation path into internal data structures, so
 *  that the String processing only happens once.
 */
public class ShiftrWriter {

    private final List<EvaluatablePathElement> elements;
    private final Traversr traversr;

    public ShiftrWriter( String dotNotation ) {

        if ( dotNotation.contains("@") || dotNotation.contains("*") || dotNotation.contains("$")) {
            throw new SpecException("DotNotation (write key) can not contain '@', '*', or '$'.");
        }

        List<PathElement> paths;
        Traversr trav;

        if ( StringUtils.isNotBlank( dotNotation ) ) {
            String[] split = dotNotation.split( "\\." );

            paths = ShiftrSpec.parse( split );
            trav = new ShiftrTraversr( dotNotation );
        }
        else {
            paths = Collections.emptyList();
            trav = new ShiftrTraversr( "" );
        }

        List<EvaluatablePathElement> evalPaths = new ArrayList<EvaluatablePathElement>( paths.size() );
        for( PathElement pe : paths ) {
            if ( ! ( pe instanceof EvaluatablePathElement ) ) {
                throw new SpecException( "RHS key=" + pe.getRawKey() + " is not a valid RHS key." );
            }

            evalPaths.add( (EvaluatablePathElement) pe );
        }

        this.elements = Collections.unmodifiableList( evalPaths );
        this.traversr = trav;
    }

    /**
     * Use the supplied WalkedPath, in the evaluation of each of our PathElements to
     *  build a concrete output path.  Then use that output path to write the given
     *  data to the output.
     *
     * @param data data to write
     * @param output data structure we are going to write the data to
     * @param walkedPath reference used to lookup reference values like "&1(2)"
     */
    public void write( Object data, Map<String, Object> output, WalkedPath walkedPath ) {
        traversr.set( output, evaluate( walkedPath ), data );
    }

    /**
     * Use the supplied WalkedPath, in the evaluation of each of our PathElements
     * @param walkedPath used to lookup/evaluate PathElement references values like "&1(2)"
     * @return fully evaluated Strings, possibly with concrete array references like "photos.[3]"
     */
    // Visible for testing
    List<String> evaluate( WalkedPath walkedPath ) {

        List<String> strings = new ArrayList<String>(elements.size());
        for ( EvaluatablePathElement pathElement : elements ) {
            String evaledLeafOutput = pathElement.evaluate( walkedPath );
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
