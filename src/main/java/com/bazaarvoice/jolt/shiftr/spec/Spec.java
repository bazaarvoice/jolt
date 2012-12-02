package com.bazaarvoice.jolt.shiftr.spec;

import com.bazaarvoice.jolt.exception.SpecException;
import com.bazaarvoice.jolt.shiftr.WalkedPath;
import com.bazaarvoice.jolt.shiftr.pathelement.PathElement;

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
    protected final PathElement pathElement;

    public Spec(String rawJsonKey) {
        List<PathElement> pathElements = PathElement.parse( rawJsonKey );

        if ( pathElements.size() != 1 ){
            throw new SpecException( "Shiftr invalid LHS:" + rawJsonKey + " can not contain '.'" );
        }

        this.pathElement = pathElements.get( 0 );
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