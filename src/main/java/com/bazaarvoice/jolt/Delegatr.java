package com.bazaarvoice.jolt;

import java.util.Map;

/**
 * Delegates to an instance of a custom-written Java transform. The Chainr entry expected
 * by Delegatr looks like this:
 *
 * {
 *     "operation": "java",                                 // tells Chainr to call Delegatr
 *     "className": "com.bazaarvoice.package.ClassName",    // fully qualified class name of a Chainable implementation
 *     // other stuff necessary for the transformation goes here
 * }
 *
 * Given proper input, the Delegatr loads the specified class, constructs an instance with
 * its no-argument constructor, and calls the Chainable process method on it.
 */
public class Delegatr implements Chainable {

    /**
     * Delegates a transform operation to an instance of a custom Chainable.
     *
     * @param input the JSON object to transform
     * @param operationEntry the JSON object from the Chainr spec containing
     *  the rest of the details necessary to carry out the transform
     * @return the results of the transform
     * @throws JoltException if no class name specified, if the class could not be
     * loaded or instantiated, if the class is a Delegatr, or if an issue occurs
     * during the transformation
     */
    @Override
    public Object process( Object input, Map<String, Object> operationEntry )
            throws JoltException {
        Object classNameObj = operationEntry.get( "className" );
        if ((classNameObj == null) || !(classNameObj instanceof String)) {
            throw new JoltException( "JOLT Delegatr requires a 'className' parameter." );
        }
        String className = (String) classNameObj;
        try {
            Class cls = Class.forName( className );
            if (Delegatr.class.isAssignableFrom( cls )) {
                throw new JoltException( "Attempted infinite loop: "+className+" is a JavaProcessor." );
            }
            Object instance = cls.newInstance();
            if (!(instance instanceof Chainable )) {
                throw new JoltException( "JOLT Delegatr className must point to a class that implements Chainable ("+className+" does not)." );
            }
            Chainable processor = (Chainable) instance;
            return processor.process( input, operationEntry );
        } catch ( ClassNotFoundException e ) {
            throw new JoltException( "JOLT Delegatr could not find class "+className, e );
        } catch ( InstantiationException e ) {
            throw new JoltException( "JOLT Delegatr could not construct an instance of class "+className, e );
        } catch ( IllegalAccessException e ) {
            throw new JoltException( "JOLT Delegatr could not construct an instance of class "+className, e );
        }
    }

}
