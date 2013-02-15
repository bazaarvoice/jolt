package com.bazaarvoice.jolt.exception;

/**
 * Base Jolt Exception
 */
public class JoltException extends RuntimeException {

    public JoltException( String msg ) {
        super(msg);
    }

    public JoltException( String msg, Exception ex ) {
        super(msg, ex);
    }
}
