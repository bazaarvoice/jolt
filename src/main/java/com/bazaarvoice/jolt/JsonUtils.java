package com.bazaarvoice.jolt;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: nate.forman
 * Date: 8/4/12
 * Time: 12:38 AM
 * To change this template use File | Settings | File Templates.
 */
public class JsonUtils {

    public static Object cloneJson(Object json) {

        // deep copy a map:
        if (json instanceof Map ) {
            Map<String, Object> jsonMap = (Map<String, Object>) json;
            Map retvalue = new HashMap();
            for (String key: jsonMap.keySet()) {
                retvalue.put( key, cloneJson( jsonMap.get( key ) ) );
            }
            return retvalue;
        }

        // deep copy a list
        if (json instanceof List ) {
            List jsonList = (List) json;
            List retvalue = new ArrayList( jsonList.size() );
            for (Object sub: jsonList) {
                retvalue.add( cloneJson( sub ) );
            }
            return retvalue;
        }

        // string, number, null doesn't need copying
        return json;
    }

    /**
     * Removes a key recursively from anywhere in a JSON document.
     * NOTE: mutates its input.
     *
     * @param json the Jackson Object version of the JSON document
     *              (contents changed by this call)
     * @param keyToRemove the key to remove from the document
     */
    public static void removeRecursive(Object json, String keyToRemove) {
        if ((json == null) || (keyToRemove == null)) {
            return;
        }
        if (json instanceof Map) {
            Map<String, Object> jsonMap = (Map<String, Object>) json;
            jsonMap.remove( keyToRemove );
            for (String subkey: jsonMap.keySet()) {
                Object value = jsonMap.get( subkey );
                removeRecursive( value, keyToRemove );
            }
        }
        if (json instanceof List) {
            for (Object value: (List) json) {
                removeRecursive( value, keyToRemove );
            }
        }
    }

    public static Map<String, Object> jsonToMap(String json)
            throws IOException {
        return jsonToMap( new ByteArrayInputStream( json.getBytes() ) );
    }

    public static Object jsonToObject(String json)
            throws IOException {
        return jsonToObject( new ByteArrayInputStream( json.getBytes() ) );
    }

    public static Object jsonToObject(InputStream in)
            throws IOException {
        JsonFactory factory = new JsonFactory();
        ObjectMapper mapper = new ObjectMapper(factory);
        return mapper.readValue(in, Object.class);
    }

    public static Map<String, Object> jsonToMap(InputStream in)
            throws IOException {
        JsonFactory factory = new JsonFactory();
        ObjectMapper mapper = new ObjectMapper(factory);
        TypeReference<HashMap<String,Object>> typeRef
                = new TypeReference<HashMap<String,Object>>() {};
        HashMap<String,Object> o = mapper.readValue(in, typeRef);
        return o;
    }

    public static <T> T jsonTo(InputStream in, TypeReference<T> typeRef)
            throws IOException {
        JsonFactory factory = new JsonFactory();
        ObjectMapper mapper = new ObjectMapper(factory);
        T o = mapper.readValue(in, typeRef);
        return o;
    }

    public static String toJsonString(Object map)
            throws IOException {
        JsonFactory factory = new JsonFactory();
        ObjectMapper mapper = new ObjectMapper(factory);
        return mapper.writeValueAsString( map );
    }
}
