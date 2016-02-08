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
	
	STRING, DOUBLE, INT, BOOLEAN, STRICT_STRING, STRICT_DOUBLE, STRICT_INT, STRICT_BOOLEAN;
	
	public static TYPE parse(String typeString) {
		if ("STRING".equalsIgnoreCase(typeString)) {
			return STRING;
		}
		if ("DOUBLE".equalsIgnoreCase(typeString)) {
			return DOUBLE;
		}
		if ("INT".equalsIgnoreCase(typeString)) {
			return INT;
		}
		if ("BOOLEAN".equalsIgnoreCase(typeString)) {
			return BOOLEAN;
		}
		if ("STRICT_STRING".equalsIgnoreCase(typeString)) {
            return STRICT_STRING;
        }
        if ("STRICT_DOUBLE".equalsIgnoreCase(typeString)) {
            return STRICT_DOUBLE;
        }
        if ("STRICT_INT".equalsIgnoreCase(typeString)) {
            return STRICT_INT;
        }
        if ("STRICT_BOOLEAN".equalsIgnoreCase(typeString)) {
            return STRICT_BOOLEAN;
        }
		throw new TypeConversionException("Unknown type: " + typeString);
	}
	
	public static Object convert(Object convertee, TYPE to) {
		if (convertee instanceof String) {
            convertee = ((String)convertee).trim();
			switch (to) {
				case DOUBLE:
                case STRICT_DOUBLE:
                    try {
                        return Double.parseDouble((String) convertee);
                    } catch (NumberFormatException e) {
                        if (to == DOUBLE)
                            return convertee;
                    }
                    break;
				case INT:
                case STRICT_INT:
                    try {
                        return Long.parseLong((String) convertee);
                    } catch (NumberFormatException e) {
                        if (to == INT)
                            return convertee;
                    }
                    break;
				case BOOLEAN:
                    return Boolean.parseBoolean((String)convertee);
                case STRICT_BOOLEAN:
                    if ("true".equalsIgnoreCase((String)convertee) || "false".equalsIgnoreCase((String)convertee))
                        return Boolean.parseBoolean((String)convertee);
                    break;
                default:
                    return convertee;
			}
		}
		else if (convertee instanceof Integer || convertee instanceof Long) {
			switch (to) {
				case STRING:
                case STRICT_STRING:
					return String.valueOf(convertee);
				case DOUBLE:
                case STRICT_DOUBLE:
					return (double)convertee;
				case INT:
                case STRICT_INT:
					return (long)convertee;
                case BOOLEAN:
                    if ((long)convertee > 0)
                        return true;
                    return false;
                case STRICT_BOOLEAN:
                    break;
                default:
                    return convertee;
			}
		}
		else if (convertee instanceof Float || convertee instanceof Double) {
			switch (to) {
				case STRING:
                case STRICT_STRING:
                    return String.valueOf(convertee);
				case DOUBLE:
                case STRICT_DOUBLE:
					return (double)convertee;
				case INT:
                case STRICT_INT:
					return (long)convertee;
                case BOOLEAN:
                    if ((double)convertee > 0)
                        return true;
                    return false;
                case STRICT_BOOLEAN:
					break;
                default:
                    return convertee;
			}
		}
		else if (convertee instanceof Boolean) {
			switch (to) {
				case STRING:
                case STRICT_STRING:
					return String.valueOf(convertee);
                case STRICT_DOUBLE:
				case STRICT_INT:
					break;
                default:
                    return convertee;
			}
		}

        if (to == STRICT_BOOLEAN || to == STRICT_DOUBLE || to == STRICT_INT || to == STRICT_STRING) {
            throw new TypeConversionException("Cannot convert " + String.valueOf(convertee) + " to " + to);
        }
        return convertee;
	}

}
