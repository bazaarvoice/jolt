/*
 * Copyright 2013 Bazaarvoice, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bazaarvoice.jolt.convertr;

import com.bazaarvoice.jolt.exception.TypeConversionException;

public enum TYPE {
	
	STRING, FLOAT, INT, BOOLEAN;
	
	public static TYPE parse(String typeString) {
		if ("STRING".equalsIgnoreCase(typeString)) {
			return STRING;
		}
		if ("FLOAT".equalsIgnoreCase(typeString)) {
			return FLOAT;
		}
		if ("INT".equalsIgnoreCase(typeString)) {
			return INT;
		}
		if ("BOOLEAN".equalsIgnoreCase(typeString)) {
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
					return Long.parseLong((String)convertee);
				case BOOLEAN:
					return Boolean.parseBoolean((String)convertee);
			}
		}
		else if (convertee instanceof Integer || convertee instanceof Long) {
			switch (to) {
				case STRING:
					return String.valueOf(convertee);
				case FLOAT:
					return (double)convertee;
				case INT:
					return (long)convertee;
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
					return (long)convertee;
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
