package com.bazaarvoice.jolt;

import com.bazaarvoice.jolt.exception.SpecException;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
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

    public final static String JAVA_DELEGATE_IDENTIFIER = "java";

    public final static String OPERATION_KEY = "operation";
    public final static String CLASSNAME_KEY = "className";
    public final static String SPEC_KEY = "spec";

    /**
     * Maps operation names to the classes that handle them
     */
    private static final Map<String, Class> CHAINABLES;

    static {
        HashMap<String, Class> temp = new HashMap<String, Class>();
        temp.put( "shift", Shiftr.class );
        temp.put( "default", Defaultr.class );
        temp.put( "remove", Removr.class );
        temp.put( "sort", Sortr.class );
        CHAINABLES = Collections.unmodifiableMap( temp );
    }

    private final List<Transform> transforms;

    public Chainr( Object chainrSpec ) {
        if ( !( chainrSpec instanceof List ) ) {
            throw new SpecException(  "JOLT Chainr expects a JSON array of objects - Malformed spec." );
        }

        transforms = new LinkedList<Transform>();

        List<Object> operations = (List<Object>) chainrSpec;
        for ( int index = 0; index < operations.size(); index++ ) {

            Object chainrEntryObj = operations.get( index );
            if ( ! (chainrEntryObj instanceof Map) ) {
                throw new SpecException( "JOLT Chainr expects a JSON array of objects - Malformed spec at index:" + index );
            }

            Transform transform = getTransform( (Map<String,Object>) chainrEntryObj, index );

            transforms.add( transform );
        }

        if ( transforms.isEmpty() ) {
            throw new SpecException( "JOLT Chainr passed an empty JSON array.");
        }
    }

    private Transform getTransform( Map<String,Object> chainrEntry, int chainrIndex ) {

        Object opNameObj = chainrEntry.get( OPERATION_KEY );
        if ( opNameObj == null ) {
            throw new SpecException( "JOLT Chainr needs a specified operation, Chainr spec index:" + chainrIndex );
        }

        String opName = opNameObj.toString().toLowerCase();
        Object specObj = chainrEntry.get( SPEC_KEY );
        Object classNameObj = chainrEntry.get( CLASSNAME_KEY );

        Class opClass;
        if ( JAVA_DELEGATE_IDENTIFIER.equals( opName ) ) {

            if ((classNameObj == null) || !(classNameObj instanceof String)) {
                throw new SpecException( "JOLT 'java' operation requires a 'className' parameter.  Chainr spec index:" + chainrIndex );
            }

            String className = (String) classNameObj;
            try {
                opClass = Class.forName( className );
                if (Chainr.class.isAssignableFrom( opClass )) {
                    throw new SpecException( "Attempt to nest Chainr inside itself at Chainr spec index:" + chainrIndex );
                }

            } catch ( ClassNotFoundException e ) {
                throw new SpecException( "JOLT Chainr could not find custome tranform class :"+className + ".  Chainr spec index:" + chainrIndex, e );
            }
        } else {
            opClass = CHAINABLES.get( opName );
        }

        if ( opClass == null ) {
            throw new SpecException( "JOLT Chainr does not support operation: " + opName + ".  Chainr spec index:" + chainrIndex);
        }

        if ( ! Transform.class.isAssignableFrom( opClass ) ) {
            throw new SpecException( "JOLT Chainr operation:" + opNameObj + " does not implement Transform.  Chainr spec index:" + chainrIndex );
        }

        if ( SpecTransform.class.isAssignableFrom( opClass ) ) {

            if ( specObj == null ){
                throw new SpecException( "JOLT Chainr - operation:" + opName + " implemented by className:" + opClass.getCanonicalName() + " requires a spec." );
            }

            try {
                // Lookup a Constructor with a Single "Object" arg.
                Constructor constructor = opClass.getConstructor( new Class[] { Object.class } );

                return (Transform) constructor.newInstance( specObj );
            }
            catch ( NoSuchMethodException nsme )
            {
                throw new SpecException( "JOLT Chainr encountered an exception constructing SpecTransform className:" + opClass.getCanonicalName() + ".  Specifically, no single arg constructor found.", nsme );
            }
            catch( Exception e ) {
                // FYI 3 exceptions are known to be thrown here
                // IllegalAccessException, InvocationTargetException, InstantiationException
                throw new SpecException( "JOLT Chainr encountered an exception constructing SpecTransform className:" + opClass.getCanonicalName(), e );
            }
        } else {
            try {
                return (Transform) opClass.newInstance();
            } catch ( Exception e ) {
                throw new SpecException( "JOLT Chainr encountered an exception instantiating Transform className:" + opClass.getCanonicalName(), e );
            }
        }
    }

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
}
