package com.bazaarvoice.jolt;

/**
 * The exception thrown by JOLT processors and operations.
 */
public class JoltException extends Exception {

    public JoltException(String msg) {
        super(msg);
    }

    public JoltException(String msg, Exception ex) {
        super(msg, ex);
    }

    public JoltException(Exception ex) {
        super(ex);
    }
}
