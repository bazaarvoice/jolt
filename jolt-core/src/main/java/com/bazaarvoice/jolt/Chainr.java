package com.bazaarvoice.jolt;

import com.bazaarvoice.jolt.exception.SpecException;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Chainr is the JOLT mechanism for chaining transforms together. Any of the built-in JOLT
 * transform types can be called directly from Chainr. Any custom-written Java transforms
 * can be adapted in by using the Transform or SpecTransform interfaces.
 *
 * A Chainr spec should be an array of objects in order that look like this:
 *
 * [
 *     {
 *         "operation": "[operation-name]",
 *         // stuff that the specific transform needs go here
 *     },
 *     ...
 * ]
 *
 * Each operation is called in the order that it is specified within the array. The original
 * input to Chainr is passed into the first operation, with its output passed into the next,
 * and so on. The output of the final operation is returned from Chainr.
 *
 * Currently, [operation-name] can be any of the following:
 *
 * - shift: (Shiftr) a tool for moving parts of an input JSON document to a new output document
 * - default: (Defaultr) a tool for applying default values to the provided JSON document
 * - remove: (Removr) a tool for removing specific values from the provided JSON document
 * - sort: (Sortr) sort the JSON document, purely for human readability / debugging
 * - java: passes control to whatever Java class you specify as long as it implements the Transform interface
 *
 * Shift, default, and remove operation all require a "spec", while sort does not.
 *
 * [
 *     {
 *         "operation": "shift",
 *         "spec" : { // shiftr spec }
 *     },
 *     {
 *         "operation": "sort"  // sort does not need a spec
 *     },
 *     ...
 * ]
 *
 * Custom Java classes that implement Tranform or SpecTransform can be loaded by specifying the full
 *  className to load.   Additionally, if upon reflection of the class we see that it is an instance of a
 *  SpecTransform, then we will construct it with a the supplied "spec" object.
 *
 * [
 *     {
 *         "operation": "java",
 *         "className" : "com.bazaarvoice.tuna.TunaTransform"
 *
 *         // "spec" : { ..  } // optional spec to use to construct a TunaTransform if it has the SpecTransform marker interface.
 *     },
 *     ...
 * ]
 */
public class Chainr implements SpecTransform {

    public final static String CUSTOM_TRANSFORM_IDENTIFIER = "java";

    public final static String OPERATION_KEY = "operation";
    public final static String CLASSNAME_KEY = "className";
    public final static String SPEC_KEY = "spec";

    /**
     * Maps operation names to the classes that handle them
     */
    private static final Map<String, Class<? extends Transform>> STOCK_TRANSFORMS;
    static {
        HashMap<String, Class<? extends Transform>> temp = new HashMap<String, Class<? extends Transform>>();
        temp.put( "shift", Shiftr.class );
        temp.put( "default", Defaultr.class );
        temp.put( "remove", Removr.class );
        temp.put( "sort", Sortr.class );
        temp.put( "cardinality", CardinalityTransform.class );
        STOCK_TRANSFORMS = Collections.unmodifiableMap( temp );
    }

    private final List<Transform> transforms;

    /**
     * Runs a spec on some input calling each specified operation in turn.
     *
     * @param input a JSON (Jackson-parsed) maps-of-maps object to transform
     * @return an object representing the JSON resulting from the transform
     * @throws com.bazaarvoice.jolt.exception.TransformException if the specification is malformed, an operation is not
     *                       found, or if one of the specified transforms throws an exception.
     */
    @Override
    public Object transform( Object input ) {

        Object intermediate = input;
        for ( Transform transform : transforms ) {
            intermediate = transform.transform( intermediate );
        }
        return intermediate;
    }


    /**
     * Initialize a Chainr to run a list of Transforms.
     * This is the constructor most "production" usages of Chainr should use.
     *
     * @param chainrSpec List of transforms to run
     */
    public Chainr( Object chainrSpec ) {
        this( chainrSpec, /* ignored */ -1 , /* ignored */ -1, true );
    }


    /**
     * Initialize a Chainr to run only a subset of the transforms in it's spec.
     *
     * Useful for testing and debugging.
     *
     * @param chainrSpec List of transforms to run
     * @param to transform from the chainrSpec to start with: 0 based index inclusive
     */
    public Chainr( Object chainrSpec, int to ) {
        this( chainrSpec, 0, to, false);
    }

    /**
     * Initialize a Chainr to run only a subset of the transforms in it's spec.
     *
     * Useful for testing and debugging.
     *
     * @param chainrSpec List of transforms to run
     * @param from transform from the chainrSpec to start with: 0 based index
     * @param to transform from the chainrSpec to end with: 0 based index inclusive
     */
    public Chainr( Object chainrSpec, int from, int to ) {
        this( chainrSpec, from, to, false);
    }


    /**
     * Private constructor.
     *
     * @param chainrSpec List of transforms to run
     * @param from transform from the chainrSpec to start with: 0 based index
     * @param to transform from the chainrSpec to end with: 0 based index inclusive
     * @param all if true, "from" and "to" parameters are ignored, instead from=0 and to=chainrSpec.size()
     */
    private Chainr( Object chainrSpec, int from, int to, boolean all ) {

        if ( !( chainrSpec instanceof List ) ) {
            throw new SpecException(  "JOLT Chainr expects a JSON array of objects - Malformed spec." );
        }

        List<Object> operations = (List<Object>) chainrSpec;

        int start, end;
        if ( all ) {
            start = 0;
            end = operations.size();
        }
        else
        {
            start = from;
            end = to + 1;
        }

        if ( (start < 0 ) || (end > operations.size() ||  end <= start ) ) {
            throw new SpecException(  "JOLT Chainr : invalid from and to parameters.  from=" + from + " to=" + to );
        }

        transforms = Collections.unmodifiableList( getTransforms( operations, start, end ) );
    }


    private List<Transform> getTransforms( List<Object> operations, int start, int end ) {

        if ( operations.isEmpty() ) {
            throw new SpecException( "JOLT Chainr passed an empty JSON array.");
        }

        List<Transform> transformList = new ArrayList<Transform>(operations.size());

        for ( int index = start; index < end; index++ ) {

            Object chainrEntryObj = operations.get( index );

            ChainrEntry entry = processChainrEntry( index, chainrEntryObj );

            transformList.add( entry.getTransform() );
        }

        return transformList;
    }

    /**
     * Process an element from the Chainr Spec into a ChainrEntry class.
     * This method tries to validate the syntax of the Chainr spec, where
     *  as the ChainrEntry deals more with Transform instantiation.
     *
     * @param chainrEntryObj the unknown Object from the Chainr list
     * @param index the index of the chainrEntryObj, used in reporting errors
     * @return an initialized ChanirEntry
     */
    private ChainrEntry processChainrEntry( int index, Object chainrEntryObj ) {

        if ( ! (chainrEntryObj instanceof Map ) ) {
            throw new SpecException( "JOLT Chainr expects a JSON array of objects - Malformed spec at index:" + index );
        }

        Map<String,Object> chainrEntryMap = (Map<String, Object>) chainrEntryObj;

        Object opNameObj = chainrEntryMap.get( OPERATION_KEY );
        if ( opNameObj == null || !(opNameObj instanceof String)) {
            throw new SpecException( "JOLT Chainr needs a 'operation' of type String, spec index:" + index );
        }

        String operation = opNameObj.toString().toLowerCase();
        Object specObj = chainrEntryMap.get( SPEC_KEY );
        String className = null;

        if ( CUSTOM_TRANSFORM_IDENTIFIER.equals( operation ) ) {

            Object classNameObj = chainrEntryMap.get( CLASSNAME_KEY );
            if ((classNameObj == null) || !(classNameObj instanceof String)) {
                throw new SpecException( "JOLT 'java' operation requires a 'className' parameter.  Chainr spec index:" + index );
            }

            className = (String) classNameObj;
        }
        else if ( ! STOCK_TRANSFORMS.containsKey( operation ) ) {
            throw new SpecException( "JOLT Chainr does not know/support operation: " + operation + ".  Chainr spec index:" + index);
        }

        return new ChainrEntry( index, operation, specObj, className );
    }


    /**
     * Helper class that encapsulates the Java specific instantiation logic need to create and initialize
     *  Transform objects.
     *
     * If I didn't want to keep Jackson from being a dependency, this would be the type of class that
     *  I would have just had Jackson load for me.
     */
    private static class ChainrEntry {
        private final int index;
        private final String operation;
        private final Object spec;
        private final String className;

        private ChainrEntry( int index, String operation, Object spec, String className ) {
            this.index = index;
            this.operation = operation;
            this.spec = spec;
            this.className = className;
        }

        public Transform getTransform() {
            return initializeTransform( getTransformClass() );
        }

        private Class<? extends Transform> getTransformClass() {

            Class<? extends Transform> opClass;
            if ( CUSTOM_TRANSFORM_IDENTIFIER.equals( operation ) ) {
                opClass = getCustomTransformClass();
            } else {
                opClass = STOCK_TRANSFORMS.get( operation );
            }

            if ( opClass == null ) {
                throw new SpecException( "JOLT Chainr does not support operation: " + operation + ".  Chainr spec index:" + index);
            }

            if ( ! Transform.class.isAssignableFrom( opClass ) ) {
                throw new SpecException( "JOLT Chainr class:" + className + " does not implement Transform.  Chainr spec index:" + index );
            }

            return opClass;
        }

        private Class<Transform> getCustomTransformClass() {

            Class opClass;
            try {
                opClass = Class.forName( className );
                if (Chainr.class.isAssignableFrom( opClass )) {
                    throw new SpecException( "Attempt to nest Chainr inside itself at Chainr spec index:" + index );
                }

            } catch ( ClassNotFoundException e ) {
                throw new SpecException( "JOLT Chainr could not find custom transform class :"+className + ".  Chainr spec index:" + index, e );
            }

            if ( Transform.class.isAssignableFrom( opClass ) ) {
                return (Class<Transform>) opClass;
            }
            else {
                throw new SpecException( "Custom transform class :"+className + " does not implement the Transform interface.  Chainr spec index:" + index );
            }
        }

        private Transform initializeTransform( Class<? extends Transform> transformClass ) {

            try {
                // If the opClass is a SpecTransform, we try to construct it with the provided spec.
                if ( SpecTransform.class.isAssignableFrom( transformClass ) ) {

                    if ( spec == null ) {
                        throw new SpecException( "JOLT Chainr - operation:" + operation + " implemented by className:" + transformClass.getCanonicalName() + " requires a spec." );
                    }

                    try {
                        // Lookup a Constructor with a Single "Object" arg.
                        Constructor<? extends Transform> constructor = transformClass.getConstructor( new Class[] {Object.class} );

                        return constructor.newInstance( spec );
                    } catch ( NoSuchMethodException nsme ) {
                        // This means the transform class "violated" the marker interface
                        throw new SpecException( "JOLT Chainr encountered an exception constructing SpecTransform className:" + transformClass.getCanonicalName() + ".  Specifically, no single arg constructor found.", nsme );
                    }
                }
                else {
                    // The opClass is just a Transform, so just create a newInstance of it.
                    return transformClass.newInstance();
                }
            } catch ( Exception e ) {
                // FYI 3 exceptions are known to be thrown here
                // IllegalAccessException, InvocationTargetException, InstantiationException
                throw new SpecException( "JOLT Chainr encountered an exception constructing Transform className:" + transformClass.getCanonicalName(), e );
            }
        }

    }
}
