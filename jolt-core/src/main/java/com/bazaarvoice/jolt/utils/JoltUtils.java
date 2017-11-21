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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Handy utilities that do NOT depend on JsonUtil / Jackson live here
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
     * Replaces a value recursively from anywhere in a JSON document.
     * NOTE: mutates its input.
     *
     * @param json        the Jackson Object version of the JSON document
     *                    (contents changed by this call)
     * @param key         the key where the value should be replaced.
     * @param valueMappings  map that contains the values to replace the key on json and map should be the same
     */
    public static void replaceRecursive( Object json, String key, Map<String,Object> valueMappings) {
        if ( json == null || key == null || valueMappings == null) {
            return;
        }
        if ( json instanceof Map ) {
            @SuppressWarnings("unchecked")
            Map<String, Object> jsonMap = (Map<String, Object>) json;

            if ( jsonMap.containsKey( key ) ) {
                Object keyToReplace = jsonMap.get(key);
                Object valueToReplace = valueMappings.containsKey(keyToReplace)
                        ? valueMappings.get(keyToReplace) : valueMappings.get("*");
                if(valueToReplace != null){
                    jsonMap.replace(key, valueToReplace);
                }
                return;
            }
            // regardless, recurse down the tree
            for ( Object node : jsonMap.values() ) {
                replaceRecursive( node, key, valueMappings);
            }
        }
        if ( json instanceof List ) {
            for ( Object node : (List) json ) {
                replaceRecursive( node, key, valueMappings);
            }
        }
    }

    /**
     * Receives a Map with the keys and values to replace in a Jackson Object
     * every key in the params should match with the key that want to replace
     * value on the Jackson Object.
     *
     * @param json              The jackson object where values will be replaced.
     * @param mappingPaths      It's a map wich contains the keys to find and the fullPaths on the
     *                          jackson object where the values will be replaced.
     * @param valuesToReplace   It's a map which contains the keys with 'oldValues' and 'newValues' to be replaced.
     *
     * Example json:
     *
     * {
         "L1_A" : {
            "L2_A" : {
                "L3_A" : "Good",
                "L3_B" : "ReplaceThis"
            },
            "L2_B" : "l2_b"
        },
        "L1_B" : "l1_b",

        "L3_B" : "This not should be replaced"
       }
     *
     * Example mappingPaths:
     *
     * { "L3_B" : ["L1_A.L2_A.L3_B"] }
     *
     * Example valuesToReplace:
     *
     * {
         "L3_B" : {
            "ReplaceThis" : "This has been replaced.",
            "AnotherValueToReplace" : "Another new value"
         }
       }
     */
    public static void replaceValues(Object json, Map<String, Object> mappingPaths, Map<String, Object> valuesToReplace){
        if(valuesToReplace != null){
            for(Map.Entry<String, Object> param : mappingPaths.entrySet()){
                List<String> paths = (List<String>) param.getValue();
                Map<String,Object> mappings = (Map<String, Object>) valuesToReplace.get(param.getKey());
                replaceValuesInPath(json, paths, mappings);
            }

        }
    }

    /**
     *  This method verify if the main object is an Array, then the replaceValuesInPath will be applied each element.
     *
     * @param raw       The original json object
     * @param paths     a list of paths where the replacement will take effect, the last element of each path
     *                  is the key of the element to be replaced.
     * @param valueMappings the value mappings is a map which contains the { "oldValueToReplace" : "newValueWhenOldMatch" }
     */
    private static void replaceValuesInPath(Object raw, List<String> paths, Map<String,Object> valueMappings){
        if(raw == null || paths == null || valueMappings == null){
            return;
        }

        if(raw instanceof List){
            List<Map<String, Object>> rawList = (List<Map<String, Object>>)raw;
            for(Map<String, Object> element : rawList){
                replaceValuesInPath(element, paths, valueMappings);
            }
        }
        if(raw instanceof Map){
            replaceValuesInPath((Map<String, Object>)raw, paths, valueMappings);
        }
    }


    /**
     * This method navigates in the method till find the parent node where the values will be replaced a path
     * @param raw       The original json object
     * @param paths     a list of paths where the replacement will take effect, the last element of each path
     *                  is the key of the element to be replaced.
     * @param valueMappings the value mappings is a map which contains the { "oldValueToReplace" : "newValueWhenOldMatch" }
     */
    private static void replaceValuesInPath(Map<String, Object> raw, List<String> paths, Map<String,Object> valueMappings){
        for(String fullPath : paths){
            String[] splittedPath = fullPath.split("\\.");
            int length = splittedPath.length;
            String key = splittedPath[length-1]; // the last element of the path is the key to be replaced
            Object parentNode = raw;
            if(length > 1) {
                for (int i = 0; i < length - 1; i++) {
                    if (parentNode instanceof List) {
                        break;
                    }
                    // The 'parentNode' is the node where the replace recursive will going to start.
                    parentNode = ((Map<String, Object>) parentNode).get(splittedPath[i]);
                }
            }
            replaceRecursive(parentNode, key, valueMappings);
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
                if ( (Integer) path >= destinationList.size() ) {
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
     * Deprecated: use isVacantJson() instead
     */
    @Deprecated
    public static boolean isEmptyJson(final Object obj) {
        return isVacantJson(obj);
    }
    /**
     * Vacant implies there are empty placeholders, i.e. a vacant hotel
     * Given a json document, checks if it has any "leaf" values, can handle deep nesting of lists and maps
     *
     * i.e. { "a": [ "x": {}, "y": [] ], "b": { "p": [], "q": {} }} ==> is empty
     *
     * @param obj source
     * @return true if its an empty json, can have deep nesting, false otherwise
     */
    public static boolean isVacantJson(final Object obj) {
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
                if(!isVacantJson(value)) {
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
     * Given a json document checks if its jst blank doc, i.e. [] or {}
     *
     * @param obj source
     * @return true if the json doc is [] or {}
     */
    public static boolean isBlankJson(final Object obj) {
        if (obj == null) {
            return true;
        }
        if(obj instanceof Collection) {
            return (((Collection) obj).size() == 0);
        }
        if(obj instanceof Map) {
            return (((Map) obj).size() == 0);
        }
        throw new UnsupportedOperationException("map or list is supported, got ${obj?obj.getClass():null}");
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

    /**
     * Given a 'fluffy' json document, it recursively removes all null elements
     * to compact the json document
     *
     * Warning: mutates the doc, destroys array order
     *
     * @param source
     * @return mutated source where all null elements are nuked
     */
    @SuppressWarnings("unchecked")
    public static Object compactJson(Object source) {
        if (source == null) return null;

        if (source instanceof List) {
            for (Object item : (List) source) {
                if (item instanceof List) {
                    compactJson(item);
                }
                else if (item instanceof Map) {
                    compactJson(item);
                }
            }
            ((List) source).removeAll(Collections.singleton(null));
        }
        else if (source instanceof Map) {
            List keysToRemove = new LinkedList();
            for (Object key : ((Map) source).keySet()) {
                Object value = ((Map)source).get(key);
                if (value instanceof List) {
                    if (((List) value).size() == 0)
                        keysToRemove.add(key);
                    else {
                        compactJson(value);
                    }
                } else if (value instanceof Map) {
                    if (((Map) value).size() == 0) {
                        keysToRemove.add(key);
                    } else {
                        compactJson(value);
                    }
                } else if (value == null) {
                    keysToRemove.add(key);
                }
            }
            for(Object key: keysToRemove) {
                ((Map) source).remove(key);
            }
        }
        else {
            throw new UnsupportedOperationException( "Only Map/String and List/Integer types are supported" );
        }

        return source;
    }

    /**
     * For a given non-null (json) object, save the valve in the nested path provided
     *
     * @param source the source json object
     * @param value the value to store
     * @param paths var args Object path to navigate down and store the object in
     * @return previously stored value if available, null otherwise
     */
    @SuppressWarnings( "unchecked" )
    public static <T> T store( Object source, T value, Object... paths ) {
        int destKeyIndex = paths.length - 1;
        if(destKeyIndex < 0) {
            throw new IllegalArgumentException( "No path information provided" );
        }
        if(source == null) {
            throw new NullPointerException( "source cannot be null" );
        }
        for ( int i = 0; i < destKeyIndex; i++ ) {
            Object currentPath = paths[i];
            Object nextPath = paths[i+1];
            source = getOrCreateNextObject( source, currentPath, nextPath );
        }
        Object path = paths[destKeyIndex];
        if(source instanceof Map && path instanceof String) {
            return cast( ( (Map) source ).put( path, value ) );
        }
        else if(source instanceof List && path instanceof Integer) {
            ensureListAvailability( (List) source, (int) path );
            return cast( ( (List) source ).set( (int) path, value ) );
        }
        else {
            throw new UnsupportedOperationException( "Only Map/String and List/Integer types are supported" );
        }
    }

    /**
     * For a given non-null (json) object, removes and returns the value in the nested path provided
     *
     * Warning: changes array order, to maintain order, use store(source, null, path ...) instead
     *
     * @param source the source json object
     * @param paths var args Object path to navigate down and remove
     * @return existing value if available, null otherwise
     */
    @SuppressWarnings( "unchecked" )
    public static <T> T remove( Object source, Object... paths ) {
        int destKeyIndex = paths.length - 1;
        if(destKeyIndex < 0) {
            throw new IllegalArgumentException( "No path information provided" );
        }
        if(source == null) {
            throw new NullPointerException( "source cannot be null" );
        }
        for ( int i = 0; i < destKeyIndex; i++ ) {
            Object currentPath = paths[i];
            Object nextPath = paths[i+1];
            source = getOrCreateNextObject( source, currentPath, nextPath );
        }
        Object path = paths[destKeyIndex];
        if(source instanceof Map && path instanceof String) {
            return cast( ( (Map) source ).remove( path ) );
        }
        else if(source instanceof List && path instanceof Integer) {
            ensureListAvailability( (List) source, (int) path );
            return cast( ( (List) source ).remove( (int) path) );
        }
        else {
            throw new UnsupportedOperationException( "Only Map/String and List/Integer types are supported" );
        }
    }

    @SuppressWarnings( "unchecked" )
    private static void ensureListAvailability( List source, int index ) {
        for ( int i = source.size(); i <= index; i++ ) {
            source.add( i, null );
        }
    }

    @SuppressWarnings( "unchecked" )
    private static Object getOrCreateNextObject( Object source, Object key, Object nextKey ) {
        Object value;
        if ( source instanceof Map && key instanceof String ) {
            if ( ( value = ( (Map) source ).get( key ) ) == null ) {
                Object newValue;
                if ( nextKey instanceof String ) {
                    newValue = new HashMap();
                }
                else if ( nextKey instanceof Integer ) {
                    newValue = new LinkedList();
                }
                else {
                    throw new UnsupportedOperationException( "Only String and Integer types are supported" );
                }
                ( (Map) source ).put( key, newValue );
                value = newValue;
            }
        }
        else if ( source instanceof List && key instanceof Integer ) {
            ensureListAvailability( ( (List) source ), (int) key );
            if ( ( value = ( (List) source ).get( (int) key ) ) == null ) {
                Object newValue;
                if ( nextKey instanceof String ) {
                    newValue = new HashMap();
                }
                else if ( nextKey instanceof Integer ) {
                    newValue = new LinkedList();
                }
                else {
                    throw new UnsupportedOperationException( "Only String and Integer types are supported" );
                }
                ( (List) source ).set( (int) key, newValue );
                value = newValue;
            }
        }
        else if(source == null || key == null) {
            throw new NullPointerException( "source and/or key cannot be null" );
        }
        else {
            throw new UnsupportedOperationException( "Only Map and List types are supported" );
        }

        if ( ( nextKey instanceof String && value instanceof Map ) || ( nextKey instanceof Integer && value instanceof List ) ) {
            return value;
        }
        else {
            throw new UnsupportedOperationException( "Only Map/String and List/Integer types are supported" );
        }
    }
}
