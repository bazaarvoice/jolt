package com.bazaarvoice.jolt.traversr;

import com.bazaarvoice.jolt.traversr.traversal.ArrayTraversal;
import com.bazaarvoice.jolt.traversr.traversal.AutoExpandArrayTraversal;
import com.bazaarvoice.jolt.traversr.traversal.MapTraversal;
import com.bazaarvoice.jolt.traversr.traversal.Traversal;
import com.bazaarvoice.jolt.traversr.traversal.Traversal.Operation;

import java.util.List;

/**
 * Traversr allows you to walk JSON tree structures of data, and to GET and SET operations.
 *
 * Corner cases that arise during tree walk, are handled by subclasses.
 * Ex: If no data exists mid tree walk quit or insert a new container?
 *     Or if there is data but it is the wrong type : overwrite or skip?
 *
 * Traversr analyzes the path path to be traversed and creates a "linked list" of Traversal objects.
 *
 * Then that list of Traversals can be used many times to write data into different JSON tree structures
 *  with different key values.
 *
 * For example given a Shiftr output path of :  "tuna[&1].bob.&3[]" some of the keys are known,
 *  "tuna" and "bob", but other keys will only be known later.
 *
 * However, the structure of the output path will not change, which means we can do some work before
 *  the keys are known.
 *
 * First the output path is turned into its canonical form : "tuna.[4].[&1].bob.&3.[]".
 * Then, a series of Traversals is created.
 *  tuna -> MapTraversal
 *  [&1] -> ArrayTraversal
 *  bob  -> MapTraversal
 *  &3   -> MapTraversal
 *  []   -> AutoExpandArrayTraversal
 *
 * Later, a list of keys can then be provided, such as
 *  [ "tuna", "2", "bob", "smith", "[]" ], and they can be quickly used without having to build or
 *  parse any more objects.
 *
 * The list of keys are all Strings, which ArrayTraversals will convert to Integers as needed.
 */
public abstract class Traversr {

    private final Traversal root;
    private final int traversalLength;

    public Traversr ( String humanPath ) {

        String intermediatePath = humanPath.replace( "[", ".[" );
        // given this replace and split strategy, we can end up with double dots, "..", which will generate an empty path element.
        // so remove any ".."  ;)
        intermediatePath = intermediatePath.replace( "..", "." );

        String[] paths = intermediatePath.split( "\\." );

        Traversal rooty = null;
        for ( int index = paths.length -1 ; index >= 0; index--) {
            rooty = makePathElement( paths[index], rooty );
        }
        traversalLength = paths.length;
        root = rooty;
    }

    private Traversal makePathElement(String path, Traversal child) {

        if ( "[]".equals( path ) ) {
            return new AutoExpandArrayTraversal( this, child );
        }
        else if ( path.startsWith( "[" ) && path.endsWith( "]" ) ) {
            return new ArrayTraversal( this, child );
        }
        else {
            return new MapTraversal( this, child );
        }
    }

    /**
     * Note : Calling this method may modify the tree object by adding new Maps and Lists as needed
     *  for the traversal.  This is determined by the behavior of the implementations of the
     *  abstract methods of this class.
     *
     * @return null or data
     */
    public Object get( Object tree, List<String> keys ) {

        if ( keys.size() != traversalLength ) {
            throw new TraversrException( "Traversal Path and number of keys mismatch, traversalLength:" + traversalLength + " numKeys:" + keys.size() );
        }

        return root.traverse( tree, Traversal.Operation.GET, keys.iterator(), null );
    }

    public void set( Object tree, List<String> keys, Object data ) {

        if ( keys.size() != traversalLength ) {
            throw new TraversrException( "Traversal Path and number of keys mismatch, traversalLength:" + traversalLength + " numKeys:" + keys.size() );
        }

        root.traverse( tree, Traversal.Operation.SET, keys.iterator(), data );
    }

    // TODO extract these methods to an interface, and then sublasses of Traverser like ShiftrTraversr can do the
    //  Swing style "I implement the interface and pass myself down" trick.
    //  Means we can still can have a ShiftrTraversr, but less of a an explicit dependency inversion going
    //   on between the Traversr and its Traversals.
    /**
     * Allow subclasses to control how "sets" are done, if/once the traversal has made it to the the last element.
     *
     * Overwrite existing data?   List-ize existing data with new data?
     *
     * @return the data object if the set was successful, or null if not
     */
    public abstract Object handleFinalSet( Traversal traversal, Object tree, String key, Object data );

    /**
     * Allow subclasses to control how gets are handled for intermediate traversals.
     *
     * Example: we are a MapTraversal and out key is "foo".
     *   We simply  do a 'tree.get( "foo" )'.  However, if we get a null back, or we get back
     *   a data type incompatible with our child Traversal, what do we do?
     *
     * Overwrite or just return?
     *
     * @return null or a container object (Map/List) for our child Traversal to use
     */
    public abstract Object handleIntermediateGet( Traversal traversal, Object tree, String key, Operation op );
}
