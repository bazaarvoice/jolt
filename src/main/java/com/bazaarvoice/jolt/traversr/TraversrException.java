package com.bazaarvoice.jolt.traversr;

public class TraversrException extends RuntimeException{

    public TraversrException( String message ) {
        super(message);
    }

    public TraversrException( String message, Exception e) {
        super( message, e );
    }
}
