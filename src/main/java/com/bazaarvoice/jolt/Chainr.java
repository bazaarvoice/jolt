package com.bazaarvoice.jolt;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JOLT, or JSON Object Language for Transformations is a framework for building JSON-to-JSON object
 * transformations as well as a toolkit for running them and piping them together.
 *
 * A JOLT spec should be an array of objects in order that look like this
 *
 * [
 *     {
 *         "operation": "[operation-name]",
 *         // stuff that the specific processor needs go here
 *     },
 *     ...
 * ]
 *
 * Currently, [operation-name] can be any of the following:
 *
 * - shift: (Shiftr) a tool for moving parts of an input JSON document to a new output document
 * - java: (JavaProcessor) passes control to a Java instance of JoltProcessor for processing
 *
 */
public class Chainr {

    private static final Map<String, Chainable> CHAINABLES;
    static {
        HashMap<String, Chainable> temp = new HashMap<String, Chainable>();
        temp.put( "shift", new Shiftr() );
        temp.put( "java", new JavaProcessor() );
        // TODO default
        CHAINABLES = Collections.unmodifiableMap( temp );
    }

    public Object process(Object input, Object spec)
            throws JoltException {
        if (!(spec instanceof List)) {
            throw new IllegalArgumentException( "bad spec" ); // TODO better
        }
        List<Map<String, Object>> pipeline = (List<Map<String, Object>>) spec;
        Object intermediate = input;
        for (Map<String, Object> pipelineEntry: pipeline) {
            Object opname = pipelineEntry.get( "operation" );
            if (opname == null) {
                throw new IllegalArgumentException( "bad opname: "+opname ); // TODO better
            }
            Chainable op = CHAINABLES.get( opname.toString().toLowerCase() );
            if (op == null) {
                throw new IllegalArgumentException( "unknown opname: "+opname ); // TODO better
            }
            intermediate = op.process( intermediate, pipelineEntry );
        }
        return intermediate;
    }
}
