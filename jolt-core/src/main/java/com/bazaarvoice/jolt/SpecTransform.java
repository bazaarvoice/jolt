package com.bazaarvoice.jolt;

/**
 * Marker interface for Jolt transforms that are based off a "spec".
 *
 * Implementations of this interface are expected to have a single arg Constructor,
 *  which takes an Object that is the spec for the constructed instance.
 * Chainr leverages this to instantiate SpecTransform object correctly.
 *
 * Ideally, calls to the transform method are expected to be stateless and multi-thread safe.
 */
public interface SpecTransform extends Transform {
}
