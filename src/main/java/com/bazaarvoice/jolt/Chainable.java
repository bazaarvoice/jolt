package com.bazaarvoice.jolt;

import java.util.Map;

/**
 * The interface by which JOLT transform operations are passed their input.
 */
public interface Chainable {

    /**
     * Execute a transform on some input JSON.
     *
     * @param input the JSON object to transform
     * @param operationEntry the JSON object from the Chainr spec containing
     *  the rest of the details necessary to carry out the transform
     * @return the results of the transformation
     * @throws JoltException if issues occur during the transform
     */
    Object process(Object input, Map<String, Object> operationEntry)
            throws JoltException;

}
