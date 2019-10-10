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
package com.bazaarvoice.jolt;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Static method convenience wrappers for a JsonUtil configured with a minimal ObjectMapper.
 *
 * The ObjectMapper use is configured to :
 *   Allow comments in the JSON strings,
 *   Hydrates all JSON Maps into LinkedHashMaps.
 */
public class JsonUtils {

    private static final JsonUtil util = new JsonUtilImpl();

    /**
     * Construct a JsonUtil with a Jackson ObjectMapper that has been preconfigured with custom
     *  Modules or Mixins.
     */
    public static JsonUtil customJsonUtil( ObjectMapper mapper ) {
        return new JsonUtilImpl( mapper );
    }

    /**
     * Removes a key recursively from anywhere in a JSON document.
     * NOTE: mutates its input.
     *
     * Deprecated: use JoltUtils instead
     *
     * @param json        the Jackson Object version of the JSON document
     *                    (contents changed by this call)
     * @param keyToRemove the key to remove from the document
     */
    @Deprecated
    public static void removeRecursive( Object json, String keyToRemove ) {
        if ( ( json == null ) || ( keyToRemove == null ) ) {
            return;
        }
        if ( json instanceof Map ) {
            @SuppressWarnings("unchecked")
            Map<String, Object> jsonMap = (Map<String, Object>) json;

            // If this level of the tree has the key we are looking for, remove it
            // Do the lookup instead of just the remove to avoid un-necessarily
            //  dying on ImmutableMaps.
            if ( jsonMap.containsKey( keyToRemove ) ) {
                jsonMap.remove( keyToRemove );
            }

            // regardless, recurse down the tree
            for ( Object value : jsonMap.values() ) {
                removeRecursive( value, keyToRemove );
            }
        }
        if ( json instanceof List ) {
            for ( Object value : (List) json ) {
                removeRecursive( value, keyToRemove );
            }
        }
    }

    /**
     * Utility for test classes, so that they can inline json in a test class.
     * Does a character level replacement of apostrophe (') with double quote (").
     *
     * This means you can express a snippit of JSON without having to forward
     * slash escape everything.
     *
     * This is character based, so don't have any apostrophes (') in your test
     * data.
     *
     * @param javason JSON-ish string you want to turn into Maps-of-Maps
     * @return Maps-of-Maps
     */
    public static Map<String, Object> javason( String javason ) {

        String json = javason.replace( '\'', '"' );

        return jsonToMap( new ByteArrayInputStream( json.getBytes() ) );
    }

    public static JsonUtil getDefaultJsonUtil() {
        return util;
    }

    //// All the methods listed below are static passthrus to the JsonUtil interface
    public static Object jsonToObject( String json ) {
        return util.jsonToObject( json );
    }
    
    public static Object jsonToObject( String json, String charset ) {
        return util.jsonToObject( json, charset );
    }

    public static Object jsonToObject( InputStream in ) {
        return util.jsonToObject( in );
    }

    public static Map<String, Object> jsonToMap( String json ) {
        return util.jsonToMap( json );
    }

    public static Map<String, Object> jsonToMap( String json, String charset ) {
        return util.jsonToMap( json, charset );
    }

    public static Map<String, Object> jsonToMap( InputStream in ) {
        return util.jsonToMap( in );
    }

    public static List<Object> jsonToList( String json ) {
        return util.jsonToList( json );
    }

    public static List<Object> jsonToList( String json, String charset ) {
        return util.jsonToList( json, charset );
    }

    public static List<Object> jsonToList( InputStream in ) {
        return util.jsonToList( in );
    }

    public static Object filepathToObject( String filePath ) {
        return util.filepathToObject( filePath );
    }

    public static Map<String, Object> filepathToMap( String filePath ) {
        return util.filepathToMap( filePath );
    }

    public static List<Object> filepathToList( String filePath ) {
        return util.filepathToList( filePath );
    }

    public static Object classpathToObject( String classPath ) {
        return util.classpathToObject( classPath );
    }

    public static Map<String, Object> classpathToMap( String classPath ) {
        return util.classpathToMap( classPath );
    }

    public static List<Object> classpathToList( String classPath ) {
        return util.classpathToList( classPath );
    }

    public static <T> T classpathToType( String classPath, TypeReference<T> typeRef ) {
        return util.classpathToType( classPath, typeRef );
    }

    public static <T> T classpathToType( String classPath, Class<T> aClass ) {
        return util.classpathToType( classPath, aClass );
    }

    public static <T> T stringToType ( String json, TypeReference<T> typeRef ) {
        return util.stringToType( json, typeRef );
    }

    public static <T> T stringToType( String json, Class<T> aClass ) {
        return util.stringToType( json, aClass );
    }

    public static <T> T fileToType ( String filePath, TypeReference<T> typeRef ) {
        return util.fileToType( filePath, typeRef );
    }
    public static <T> T fileToType ( String filePath, Class<T> aClass ) {
        return util.fileToType( filePath, aClass );
    }

    public static <T> T streamToType( InputStream in, TypeReference<T> typeRef ) {
        return util.streamToType( in, typeRef );
    }
    public static <T> T streamToType( InputStream in, Class<T> aClass ) {
        return util.streamToType( in, aClass );
    }

    /**
     * Use the stringToType method instead.
     */
    @Deprecated
    public static <T> T jsonTo( String json, TypeReference<T> typeRef ) {
        return util.stringToType( json, typeRef );
    }

    /**
     * Use the streamToType method instead.
     */
    @Deprecated
    public static <T> T jsonTo( InputStream in, TypeReference<T> typeRef ) {
        return util.streamToType( in, typeRef );
    }

    public static String toJsonString( Object obj ) {
        return util.toJsonString( obj );
    }

    public static String toPrettyJsonString( Object obj ) {
        return util.toPrettyJsonString( obj );
    }


    /**
     * Makes a deep copy of a Map<String, Object> object by converting it to a String and then
     * back onto stock JSON objects.
     *
     * @param obj object tree to copy
     * @return deep copy of the incoming obj
     */
    public static Object cloneJson( Object obj ) {
        // use the "configured" util for the serialize to String part
        return util.cloneJson( obj );
    }

    /**
     * Navigate inside a json object in quick and dirty way.
     *
     * Deprecated: use JoltUtils instead
     *
     * @param source the source json object
     * @param paths the paths array to travel
     * @return the object of Type <T> at final destination
     * @throws NullPointerException if the source is null
     * @throws UnsupportedOperationException if the source is not Map or List
     */
    @SuppressWarnings("unchecked")
    @Deprecated
    public static <T> T navigate(Object source, Object... paths) throws NullPointerException, UnsupportedOperationException {
        Object destination = source;
        for (Object path : paths) {
            if(destination == null) throw new NullPointerException("Navigation not possible on null object");
            if(destination instanceof Map) destination = ((Map) destination).get(path);
            else if(path instanceof Integer && destination instanceof List) destination = ((List) destination).get((Integer)path);
            else throw new UnsupportedOperationException("Navigation supports only Map and List source types and non-null String and Integer path types");
        }
        return (T) destination;
    }
}
