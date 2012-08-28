package com.bazaarvoice.jolt;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Chainr is the JOLT mechanism for chaining transforms together. Any of the built-in JOLT
 * transform types can be called directly from Chainr. Any custom-written Java transforms
 * can be adapted in by using the Chainable interface.
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
 * - java: (Delegatr) passes control to a Java instance of Chainable for processing
 *
 * (with 'default' and 'calculate' coming soon.
 *
 */
public class Chainr {

    /**
     * Maps operation names to the classes that handle them
     */
    private static final Map<String, Chainable> CHAINABLES;
    static {
        HashMap<String, Chainable> temp = new HashMap<String, Chainable>();
        temp.put( "shift", new Shiftr() );
        temp.put( "java", new Delegatr() );
        // TODO default
        CHAINABLES = Collections.unmodifiableMap( temp );
    }

    /**
     * Runs a spec on some input calling each specified operation in turn.
     *
     * @param input a JSON (Jackson-parsed) maps-of-maps object to transform
     * @param spec a JSON array of objects that specifies the list of transforms to execute
     * @return an object representing the JSON resulting from the transform
     * @throws JoltException if the specification is malformed, an operation is not
     *  found, or if one of the specified transforms throws an exception.
     */
    public Object process(Object input, Object spec)
            throws JoltException {
        if (!(spec instanceof List)) {
            this.throwMalformedSpecException( spec );
        }
        List operations = (List) spec;
        Object intermediate = input;
        for (Object entry: operations) {
            if (!(entry instanceof Map)) {
                this.throwMalformedSpecException( spec );
            }
            Map operation = (Map) entry;
            Object opname = operation.get( "operation" );
            if (opname == null) {
                throw new JoltException( "JOLT Chainr does not support operation: "+opname );
            }
            Chainable op = CHAINABLES.get( opname.toString().toLowerCase() );
            if (op == null) {
                throw new JoltException( "JOLT Chainr does not support operation: "+opname );
            }
            try {
            intermediate = op.process( intermediate, operation );
            }
            catch (Exception ex) {
                if (ex instanceof JoltException) {
                    throw (JoltException) ex;
                }
                throw new JoltException( "JOLT Chainr encountered an exception while processing.", ex );
            }
        }
        return intermediate;
    }

    /**
     * Composes and throws a JoltException for a malformed spec,
     * @param spec the malformed spec in question
     * @throws JoltException always
     */
    private void throwMalformedSpecException(Object spec)
            throws JoltException {
        String msg = "JOLT Chainr expects a JSON array of objects. Malformed spec: ";
        try {
            msg += JsonUtils.toJsonString( spec );
        } catch ( IOException e ) {
            msg += "could not transform to JSON string: "+e.toString();
        }
        throw new JoltException( msg );
    }
}
