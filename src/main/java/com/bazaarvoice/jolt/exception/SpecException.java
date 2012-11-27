package com.bazaarvoice.jolt.exception;

/**
 * Exception thrown by JOLT SpecTransforms during initialization.
 */
public class SpecException extends JoltException {

    public SpecException(String msg) {
        super(msg);
    }

    public SpecException(String msg, Exception ex) {
        super(msg, ex);
    }
}
