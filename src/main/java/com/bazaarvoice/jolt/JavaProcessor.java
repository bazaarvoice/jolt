package com.bazaarvoice.jolt;

import java.util.Map;

public class JavaProcessor implements Chainable {

    @Override
    public Object process( Object input, Map<String, Object> joltPipelineEntry )
            throws JoltException {
        String className = joltPipelineEntry.get( "className" ).toString();
        if (className == null) {
            throw new JoltException( "JOLT JavaProcessor requires a 'className' parameter." );
        }
        try {
            Class cls = Class.forName( className );
            if (JavaProcessor.class.isAssignableFrom( cls )) {
                throw new JoltException( "Attempted infinite loop: "+className+" is a JavaProcessor." );
            }
            Object instance = cls.newInstance();
            if (!(instance instanceof Chainable )) {
                throw new JoltException( "JOLT JavaProcessor className must point to a class that implements JoltProcessor ("+className+" does not)." );
            }
            Chainable processor = (Chainable) instance;
            return processor.process( input, joltPipelineEntry );
        } catch ( ClassNotFoundException e ) {
            throw new JoltException( "JOLT JavaProcessor could not find class "+className, e );
        } catch ( InstantiationException e ) {
            throw new JoltException( "JOLT JavaProcessor could not construct an instance of class "+className, e );
        } catch ( IllegalAccessException e ) {
            throw new JoltException( "JOLT JavaProcessor could not construct an instance of class "+className, e );
        }
    }

}
