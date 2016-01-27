package com.bazaarvoice.jolt.convertr;

import com.bazaarvoice.jolt.exception.TypeConversionException;

public enum TYPE {
	
	STRING, FLOAT, INT, BOOLEAN;
	
	public static TYPE parse(String typeString) {
		if ("STRING".equals(typeString)) {
			return STRING;
		}
		if ("FLOAT".equals(typeString)) {
			return FLOAT;
		}
		if ("INT".equals(typeString)) {
			return INT;
		}
		if ("BOOLEAN".equals(typeString)) {
			return BOOLEAN;
		}
		throw new TypeConversionException("Unknown type: " + typeString);
	}
	
	public static Object convert(Object convertee, TYPE to) {
		if (convertee instanceof String) {
			switch (to) {
				case STRING:
					return convertee;
				case FLOAT:
					return Double.parseDouble((String)convertee);
				case INT:
					return Integer.parseInt((String)convertee);
				case BOOLEAN:
					return Boolean.parseBoolean((String)convertee);
			}
		}
		else if (convertee instanceof Integer) {
			switch (to) {
				case STRING:
					return String.valueOf(convertee);
				case FLOAT:
					return (double)convertee;
				case INT:
					return convertee;
				case BOOLEAN:
					throw new TypeConversionException("Cannot convert from " + INT + " to " + BOOLEAN);
			}
		}
		else if (convertee instanceof Float || convertee instanceof Double) {
			switch (to) {
				case STRING:
					return String.valueOf(convertee);
				case FLOAT:
					return (double)convertee;
				case INT:
					return (int)convertee;
				case BOOLEAN:
					throw new TypeConversionException("Cannot convert from " + FLOAT + " to " + BOOLEAN);
			}
		}
		else if (convertee instanceof Boolean) {
			switch (to) {
				case STRING:
					return String.valueOf(convertee);
				case FLOAT:
					throw new TypeConversionException("Cannot convert from " + BOOLEAN + " to " + FLOAT);
				case INT:
					throw new TypeConversionException("Cannot convert from " + BOOLEAN + " to " + INT);
				case BOOLEAN:
					return convertee;
			}
		}
		throw new TypeConversionException(String.valueOf(convertee) + " does not belong to " + STRING + ", " + FLOAT + ", " + INT + " or " + BOOLEAN);
	}

}
