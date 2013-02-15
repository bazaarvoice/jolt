package com.bazaarvoice.jolt.shiftr.spec;

import com.bazaarvoice.jolt.exception.SpecException;
import com.bazaarvoice.jolt.shiftr.WalkedPath;
import com.bazaarvoice.jolt.shiftr.pathelement.AmpPathElement;
import com.bazaarvoice.jolt.shiftr.pathelement.ArrayPathElement;
import com.bazaarvoice.jolt.shiftr.pathelement.AtPathElement;
import com.bazaarvoice.jolt.shiftr.pathelement.DollarPathElement;
import com.bazaarvoice.jolt.shiftr.pathelement.LiteralPathElement;
import com.bazaarvoice.jolt.shiftr.pathelement.MatchablePathElement;
import com.bazaarvoice.jolt.shiftr.pathelement.PathElement;
import com.bazaarvoice.jolt.shiftr.pathelement.StarAllPathElement;
import com.bazaarvoice.jolt.shiftr.pathelement.StarRegexPathElement;
import com.bazaarvoice.jolt.shiftr.pathelement.StarSinglePathElement;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * A Spec Object represents a single line from the JSON Shiftr Spec.
 *
 * At a minimum a single Spec has :
 *   Raw LHS spec value
 *   Some kind of PathElement (based off that raw LHS value)
 *
 * Additionally there are 2 distinct subclasses of the base Spec
 *  LeafSpec : where the RHS is a String or Array of Strings, that specify an write path for the data from this level in the tree
 *  CompositeSpec : where the RHS is a map of children Specs
 *
 * Mapping of Json Shiftr Spec to Spec objects :
 * {
 *   rating-*" : {      // CompositeSpec with one child and a Star PathElement
 *     "&(1)" : {       // CompositeSpec with one child and a Reference PathElement
 *       "foo: {        // CompositeSpec with one child and a Literal PathElement
 *         "value" : "Rating-&1.value"  // OutputtingSpec with a Literal PathElement and one write path
 *       }
 *     }
 *   }
 * }
 *
 * The tree structure of formed by the CompositeSpecs is what is used during Shiftr transforms
 *  to do the parallel tree walk with the input data tree.
 *
 * During the parallel tree walk, a Path<Literal PathElements> is maintained, and used when
 *  a tree walk encounters an Outputting spec to evaluate the wildcards in the write DotNotationPath.
 */
public abstract class Spec {

    // The processed key from the Json config
    protected final MatchablePathElement pathElement;

    public Spec(String rawJsonKey) {
        List<PathElement> pathElements = parse( rawJsonKey );

        if ( pathElements.size() != 1 ){
            throw new SpecException( "Shiftr invalid LHS:" + rawJsonKey + " can not contain '.'" );
        }

        PathElement pe =  pathElements.get( 0 );
        if ( ! ( pe instanceof MatchablePathElement ) ) {
            throw new SpecException( "Spec LHS key=" + rawJsonKey + " is not a valid LHS key." );
        }

        this.pathElement = (MatchablePathElement) pe;
    }

    // TODO clean this up and then move PathElement and its subclasses up to a common package
    //  once all the shiftr specific logic is extracted.
    public static List<PathElement> parse( String key )  {

        if ( key.contains("@") ) {
            return Arrays.<PathElement>asList( new AtPathElement( key ) );
        }
        else if ( key.contains("$") ) {
            return Arrays.<PathElement>asList( new DollarPathElement( key ) );
        }
        else if ( key.contains("[") ) {

            if ( StringUtils.countMatches( key, "[" ) != 1 || StringUtils.countMatches( key, "]" ) != 1 ) {
                throw new SpecException( "Invalid key:" + key + " has too many [] references.");
            }

            // is canonical array?
            if ( key.charAt( 0 ) == '[' && key.charAt( key.length() - 1 ) == ']') {
                return Arrays.<PathElement>asList( new ArrayPathElement( key ) );
            }

            // Split syntactic sugar of "photos[]" --> [ "photos", "[]" ]
            //  or                      "bob-&(3,1)-smith[&0]" --> [ "bob-&(3,1)-smith", "[&(0,0)]" ]

            String canonicalKey = key.replace( "[", ".[" );
            String[] subkeys = canonicalKey.split( "\\." );

            List<PathElement> subElements = parse(  subkeys ); // at this point each sub key should be a valid key, so just recall parse

            for ( int index = 0; index < subElements.size() - 1; index++ ) {
                PathElement v = subElements.get( index );
                if ( v instanceof ArrayPathElement ) {
                    throw new SpecException( "Array [..] must be the last thing in the key, was:" + key );
                }
            }

            return subElements;
        }
        else if ( key.contains("&") ) {
            if ( key.contains("*") )
            {
                throw new SpecException("Can't mix * with & ) ");
            }
            return Arrays.<PathElement>asList( new AmpPathElement( key ) );
        }
        else if ( "*".equals( key ) ) {
            return Arrays.<PathElement>asList( new StarAllPathElement( key ) );
        }
        else if ( StringUtils.countMatches( key, "*" ) == 1 ) {
            return Arrays.<PathElement>asList( new StarSinglePathElement( key ) );
        }
        else if ( key.contains("*") ) {
            return Arrays.<PathElement>asList( new StarRegexPathElement( key ) );
        }
        else {
            return Arrays.<PathElement>asList( new LiteralPathElement( key ) );
        }
    }

    public static List<PathElement> parse( String[] keys ) {
        ArrayList<PathElement> paths = new ArrayList<PathElement>();

        for( String key: keys ) {
            List<PathElement> subPaths = parse( key );
            for ( PathElement path : subPaths ) {
                paths.add( path );
            }
        }

        return paths;
    }

    /**
     * This is the main recursive method of the Shiftr parallel "spec" and "input" tree walk.
     *
     * If should return true, if this Spec object was able to successfully apply itself given the
     *  inputKey and input object.
     *
     * In the context of the Shiftr parallel treewalk, if this method returns true, the assumption
     *  is that no other sibling Shiftr specs need to look at this particular input key.
     *
     * @return true if this this spec "handles" the inputkey such that no sibling specs need to see it
     */
    public abstract boolean apply( String inputKey, Object input, WalkedPath walkedPath, Map<String,Object> output );
}