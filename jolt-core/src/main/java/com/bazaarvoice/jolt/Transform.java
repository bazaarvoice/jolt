package com.bazaarvoice.jolt;

/**
 * Base interface for all Jolt Transforms.
 *
 * Transforms should be stateless, in that multiple threads should be able to
 * call the transform method simultaneously.
 */
public interface Transform {

    /**
     * Execute a transform on some input JSON and return the result.
     *
     * @param input the JSON object to transform in plain vanilla Jackson Map<String, Object> style
     * @return the results of the transformation
     * @throws com.bazaarvoice.jolt.exception.TransformException if there are issues with the transform
     */
    Object transform( Object input );
}
