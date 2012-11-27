package com.bazaarvoice.jolt.exception;

/**
 * Exception thrown by JOLT transforms.  Should only be thrown from methods processing data.
 */
public class TransformException extends JoltException {

    public TransformException( String msg ) {
        super(msg);
    }

    public TransformException( String msg, Exception ex ) {
        super(msg, ex);
    }
}
