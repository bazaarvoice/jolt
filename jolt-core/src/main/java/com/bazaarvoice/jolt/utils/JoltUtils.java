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
package com.bazaarvoice.jolt.utils;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * Handy utilities that do NOT depend on JsonUtil lives here!
 */
public class JoltUtils {

    /**
     * Removes a key recursively from anywhere in a JSON document.
     * NOTE: mutates its input.
     *
     * @param json        the Jackson Object version of the JSON document
     *                    (contents changed by this call)
     * @param keyToRemove the key to remove from the document
     */
    public static void removeRecursive( Object json, String keyToRemove ) {
        if ( ( json == null ) || ( keyToRemove == null ) ) {
            return;
        }
        if ( json instanceof Map ) {
            Map<String, Object> jsonMap = cast(json);

            // If this level of the tree has the key we are looking for, remove it
            if ( jsonMap.containsKey( keyToRemove ) ) {
                jsonMap.remove( keyToRemove );
            }

            // regardless, recurse down the tree
            for ( String subKey : jsonMap.keySet() ) {
                Object value = jsonMap.get( subKey );
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
     * Navigate inside a json object in quick and dirty way.
     *
     * @param source the source json object
     * @param paths the paths array to travel
     * @return the object of Type <T> at final destination
     * @throws NullPointerException if the source is null
     * @throws UnsupportedOperationException if the source is not Map or List
     */
    public static <T> T navigate(final Object source, final Object... paths) {
        Object destination = source;
        for (Object path : paths) {
            if(path == null || destination == null) {
                throw new NullPointerException("source or path is null");
            }
            if(destination instanceof Map) {
                destination = ((Map) destination).get(path);
            }
            else if(path instanceof Integer && destination instanceof List) {
                destination = ((List) destination).get((Integer)path);
            }
            else {
                throw new UnsupportedOperationException("Navigation supports only Map and List source types and non-null String and Integer path types");
            }
        }
        return cast(destination);
    }

    /**
     * Navigate inside a json object in quick and "dirtier" way, i.e. returns a default value NPE
     * or out-of-index errors encountered
     *
     * @param source the source
     * @param paths the paths array
     * @return the object of Type <T> at final destination or defaultValue if non existent
     * @throws UnsupportedOperationException the unsupported operation exception
     */
    public static <T> T navigateSafe(final T defaultValue, final Object source, final Object... paths) {
        Object destination = source;
        for (Object path : paths) {
            if(path == null || destination == null) {
                return defaultValue;
            }
            if(destination instanceof Map) {
                Map destinationMap = (Map) destination;
                if(!destinationMap.containsKey(path)) {
                    return defaultValue;
                }
                else {
                    destination = destinationMap.get(path);
                }
            }
            else if(path instanceof Integer && destination instanceof List) {
                List destinationList = (List) destination;
                if(destinationList.size() < (Integer) path) {
                    return defaultValue;
                }
                else {
                    destination = destinationList.get((Integer)path);
                }
            }
            else {
                throw new UnsupportedOperationException("Navigation supports only Map and List source types and non-null String and Integer path types");
            }
        }
        return cast(destination);
    }

    /**
     * Given a json document, checks if it has any "leaf" values, can handle deep nesting of lists and maps
     *
     * i.e. { "a": [ "x": {}, "y": [] ], "b": { "p": [], "q": {} }} ==> is empty
     *
     * @param obj source
     * @return true if its an empty json, can have deep nesting, false otherwise
     */
    public static boolean isEmptyJson(final Object obj) {
        Collection values = null;
        if(obj instanceof Collection) {
            if(((Collection) obj).size() == 0) {
                return true;
            }
            values = (Collection) obj;
        }
        if(obj instanceof Map) {
            if(((Map) obj).size() == 0) {
                return true;
            }
            values = ((Map) obj).values();
        }
        int processedEmpty = 0;
        if(values != null) {
            for (Object value: values) {
                if(!isEmptyJson(value)) {
                    return false;
                }
                processedEmpty++;
            }
            if(processedEmpty == values.size()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Given a json document, finds out absolute path to every leaf element
     *
     * i.e. { "a": [ "x": { "y": "alpha" }], "b": { "p": [ "beta", "gamma" ], "q": {} }} will yield
     *
     * 1) "a",0,"x","y" -> to "alpha"
     * 2) "b","p", 0 -> to "beta"
     * 3) "b", "p", 1 -> to "gamma"
     * 4) "b","q" -> to {} (empty Map)
     *
     * @param source json
     * @return list of Object[] representing path to every leaf element
     */
    public static List<Object[]> listKeyChains(final Object source) {

        List<Object[]> keyChainList = new LinkedList<>();

        if(source instanceof Map) {
            Map sourceMap = (Map) source;
            for (Object key: sourceMap.keySet()) {
                keyChainList.addAll(listKeyChains(key, sourceMap.get(key)));
            }
        }
        else if(source instanceof List) {
            List sourceList = (List) source;
            for(int i=0; i<sourceList.size(); i++) {
                keyChainList.addAll(listKeyChains(i, sourceList.get(i)));
            }
        }
        else {
            return Collections.emptyList();
        }

        return keyChainList;
    }

    /**
     * Helper/overridden method for listKeyChain(source), it accepts a key-value pair for convenience
     * note: "key": value (an item in map) and [value] (an item in list) is generalized here
     *       as [value] is interpreted in json path as 1: value
     *
     * @param key
     * @param value
     * @return list of Object[] representing path to every leaf element starting with provided root key
     */
    public static List<Object[]> listKeyChains(final Object key, final Object value) {
        List<Object[]> keyChainList = new LinkedList<>();
        List<Object[]> childKeyChainList = listKeyChains(value);
        if(childKeyChainList.size() > 0) {
            for(Object[] childKeyChain: childKeyChainList) {
                Object[] keyChain = new Object[childKeyChain.length + 1];
                keyChain[0] = key;
                System.arraycopy(childKeyChain, 0, keyChain, 1, childKeyChain.length);
                keyChainList.add(keyChain);
            }
        }
        else {
            keyChainList.add(new Object[] {key});
        }
        return keyChainList;
    }

    /**
     * Converts a standard json path to human readable SimpleTraversr compatible path
     *
     * @param paths the path array of objects
     * @return string representation of the path, human readable and SimpleTraversr friendly
     */
    public static String toSimpleTraversrPath(Object[] paths) {
        StringBuilder pathBuilder = new StringBuilder();
        for(int i=0; i<paths.length; i++) {
            Object path = paths[i];
            if(path instanceof Integer) {
                pathBuilder.append("[").append(((Integer) path).intValue()).append("]");
            }
            else if(path instanceof String) {
                pathBuilder.append(path.toString());
            }
            else{
                throw new UnsupportedOperationException("Only Strings and Integers are supported as path element");
            }
            if(!(i+1 == paths.length)) {
                pathBuilder.append(".");
            }
        }
        return pathBuilder.toString();
    }

    /**
     * Type casts an input object to class indicated by TypeToken
     *
     * @param object the input object to cast
     * @return cast object of type T
     */
    @SuppressWarnings("unchecked")
    public static <T> T cast(Object object) {
        return (T) (object);
    }

    /**
     * Type cast to array E[]
     *
     * @param object the input object to cast
     * @return casted array of type E[]
     */
    @SuppressWarnings("unchecked")
    public static <E> E[] cast(Object[] object) {
        return (E[])(object);
    }
}
