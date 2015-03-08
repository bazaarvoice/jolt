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

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Utility methods for getting JSON content loaded from
 *  the filesystem, the classpath, or in memory Strings.
 *
 * Also has methods to serialize Java object to JSON strings.
 *
 * Implementations of this interface can specify their own
 * Jackson ObjectMapper so that Domain specific Java Objects
 * can successfully be serialized and de-serialized.
 */
public interface JsonUtil {

    // DE-SERIALIZATION
    Object jsonToObject( String json );
    Object jsonToObject( String json , String charset );
    Object jsonToObject( InputStream in );

    Map<String, Object> jsonToMap( String json );
    Map<String, Object> jsonToMap( String json, String charset );
    Map<String, Object> jsonToMap( InputStream in );

    List<Object> jsonToList( String json);
    List<Object> jsonToList( String json , String charset );
    List<Object> jsonToList( InputStream in );

    Object           filepathToObject( String filePath );
    Map<String, Object> filepathToMap( String filePath );
    List<Object>       filepathToList( String filePath );

    Object           classpathToObject( String classPath );
    Map<String, Object> classpathToMap( String classPath );
    List<Object>       classpathToList( String classPath );

    /**
     * Use the stringToType method instead.
     */
    @Deprecated
    <T> T jsonTo(    String json, TypeReference<T> typeRef );

    /**
     * Use the streamToType method instead.
     */
    @Deprecated
    <T> T jsonTo( InputStream in, TypeReference<T> typeRef );

    <T> T stringToType   (String json, TypeReference<T> typeRef );
    <T> T stringToType   (String json, Class<T> aClass );

    <T> T classpathToType(String classPath, TypeReference<T> typeRef );
    <T> T classpathToType(String classPath, Class<T> aClass );

    <T> T fileToType     (String filePath, TypeReference<T> typeRef );
    <T> T fileToType     (String filePath, Class<T> aClass );

    <T> T streamToType   ( InputStream in, TypeReference<T> typeRef );
    <T> T streamToType   ( InputStream in, Class<T> aClass );

    String toJsonString( Object obj );
    String toPrettyJsonString( Object obj );

    /**
     * Makes a deep copy of a Map<String, Object> object by converting it to a String and then
     * back onto stock JSON objects.
     *
     * Leverages Serialization
     *
     * @param obj object tree to copy
     * @return deep copy of the incoming obj
     */
    Object cloneJson( Object obj );
}
