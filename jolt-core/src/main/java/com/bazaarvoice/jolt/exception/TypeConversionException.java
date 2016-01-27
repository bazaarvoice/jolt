package com.bazaarvoice.jolt.exception;

import com.bazaarvoice.jolt.exception.JoltException;

/**
 * Exception thrown by JOLT Convertr.
 */
public class TypeConversionException extends JoltException {
	public TypeConversionException(String message) {
		super(message);
	}

	public TypeConversionException(String message, Throwable t) {
		super(message, t);
	}
}
