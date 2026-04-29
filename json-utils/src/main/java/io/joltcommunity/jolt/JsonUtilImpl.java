/*
 * Copyright 2013-2023 Bazaarvoice, Inc.
 * Copyright 2025 Jolt Community
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
package io.joltcommunity.jolt;

import io.joltcommunity.jolt.exception.JsonMarshalException;
import io.joltcommunity.jolt.exception.JsonUnmarshalException;

import tools.jackson.core.JacksonException;
import tools.jackson.core.Version;
import tools.jackson.core.json.JsonFactory;
import tools.jackson.core.json.JsonReadFeature;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ObjectWriter;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;


import java.io.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of JsonUtil that allows the user to provide a configured
 * Jackson ObjectMapper.
 * <p>
 * All IOExceptions are caught, wrapped with context, and rethrown as RuntimeExceptions.
 */
public class JsonUtilImpl implements JsonUtil {
    // Default Encoding for String to JSON operations
    public static final String DEFAULT_ENCODING_UTF_8 = "utf-8";
    private static final TypeReference<Map<String, Object>> mapTypeReference =
            new TypeReference<>() {
            };
    private static final TypeReference<List<Object>> listTypeReference =
            new TypeReference<>() {
            };
    // thread safe: http://wiki.fasterxml.com/JacksonFAQThreadSafety
    private final ObjectMapper objectMapper;
    private final ObjectWriter prettyPrintWriter;

    /**
     * By allowing the user to provide an ObjectMapper, it can be configured with
     * knowledge of how to marshall and un-marshall your domain objects.
     *
     * @param objectMapper a configured Jackson ObjectMapper
     */
    public JsonUtilImpl(ObjectMapper objectMapper) {

        this.objectMapper = objectMapper == null ? buildStockJoltObjectMapper() : objectMapper;
        prettyPrintWriter = this.objectMapper.writerWithDefaultPrettyPrinter();
    }

    public JsonUtilImpl() {
        this.objectMapper = buildStockJoltObjectMapper();
        prettyPrintWriter = this.objectMapper.writerWithDefaultPrettyPrinter();
    }

    
    public static ObjectMapper buildStockJoltObjectMapper() {

        // All Json maps should be deserialized into LinkedHashMaps.
        SimpleModule stockModule = new SimpleModule("stockJoltMapping", new Version(1, 0, 0, null, null, null))
                .addAbstractTypeMapping(Map.class, LinkedHashMap.class);

        JsonFactory jsonFactory = JsonFactory.builder()
                .enable(JsonReadFeature.ALLOW_JAVA_COMMENTS)
                .build();

        return JsonMapper.builder(jsonFactory)
                .addModule(stockModule)
                .configure(JsonReadFeature.ALLOW_JAVA_COMMENTS, true)
                .build();
        
    }

    
    // DE-SERIALIZATION
    @Override
    public Object jsonToObject(String json) {
        return jsonToObject(json, DEFAULT_ENCODING_UTF_8);
    }

    @Override
    public Object jsonToObject(String json, String charset) {
        try (InputStream in = new ByteArrayInputStream(json.getBytes(charset))) {
            return jsonToObject(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object jsonToObject(InputStream in) {
        try {
            return objectMapper.readValue(in, Object.class);
        } catch (JacksonException e) {
            throw new JsonUnmarshalException("Unable to unmarshal JSON to an Object.", e);
        }
    }

    @Override
    public Map<String, Object> jsonToMap(String json) {
        return jsonToMap(json, DEFAULT_ENCODING_UTF_8);
    }

    @Override
    public Map<String, Object> jsonToMap(String json, String charset) {
        try (InputStream in = new ByteArrayInputStream(json.getBytes(charset))) {
            return jsonToMap(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, Object> jsonToMap(InputStream in) {
        try {
            return objectMapper.readValue(in, mapTypeReference);
        } catch (JacksonException e) {
            throw new JsonUnmarshalException("Unable to unmarshal JSON to a Map.", e);
        }
    }

    @Override
    public List<Object> jsonToList(String json) {
        return jsonToList(json, DEFAULT_ENCODING_UTF_8);
    }

    @Override
    public List<Object> jsonToList(String json, String charset) {
        try (InputStream in = new ByteArrayInputStream(json.getBytes(charset))) {
            return jsonToList(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Object> jsonToList(InputStream in) {
        try {
            return objectMapper.readValue(in, listTypeReference);
        } catch (JacksonException e) {
            throw new JsonUnmarshalException("Unable to unmarshal JSON to a List.", e);
        }
    }


    @Override
    public Object filepathToObject(String filePath) {
        try (FileInputStream fileInputStream = new FileInputStream(filePath)) {
            return jsonToObject(fileInputStream);
        } catch (IOException e) {
            throw new RuntimeException("Unable to load JSON file from: " + filePath);
        }
    }

    @Override
    public Map<String, Object> filepathToMap(String filePath) {
        try (FileInputStream fileInputStream = new FileInputStream(filePath)) {
            return jsonToMap(fileInputStream);
        } catch (IOException e) {
            throw new RuntimeException("Unable to load JSON file from: " + filePath);
        }
    }

    @Override
    public List<Object> filepathToList(String filePath) {
        try (FileInputStream fileInputStream = new FileInputStream(filePath)) {
            return jsonToList(fileInputStream);
        } catch (IOException e) {
            throw new RuntimeException("Unable to load JSON file from: " + filePath);
        }
    }

    @Override
    public Object classpathToObject(String classPath) {
        try (InputStream inputStream = this.getClass().getResourceAsStream(classPath)) {
            return jsonToObject(inputStream);
        } catch (Exception e) {
            throw new RuntimeException("Unable to load JSON object from classPath : " + classPath, e);
        }
    }

    @Override
    public Map<String, Object> classpathToMap(String classPath) {
        try (InputStream inputStream = this.getClass().getResourceAsStream(classPath)) {
            return jsonToMap(inputStream);
        } catch (Exception e) {
            throw new RuntimeException("Unable to load JSON map from classPath : " + classPath, e);
        }
    }

    @Override
    public List<Object> classpathToList(String classPath) {
        try (InputStream inputStream = this.getClass().getResourceAsStream(classPath)) {
            return jsonToList(inputStream);
        } catch (Exception e) {
            throw new RuntimeException("Unable to load JSON map from classPath : " + classPath, e);
        }
    }

    @Deprecated
    @Override
    public <T> T jsonTo(InputStream in, TypeReference<T> typeRef) {
        return streamToType(in, typeRef);
    }

    @Deprecated
    @Override
    public <T> T jsonTo(String json, TypeReference<T> typeRef) {
        return streamToType(new ByteArrayInputStream(json.getBytes()), typeRef);
    }

    @Override
    public <T> T stringToType(String json, TypeReference<T> typeRef) {
        return streamToType(new ByteArrayInputStream(json.getBytes()), typeRef);
    }

    @Override
    public <T> T stringToType(String json, Class<T> aClass) {
        return streamToType(new ByteArrayInputStream(json.getBytes()), aClass);
    }

    @Override
    public <T> T classpathToType(String classPath, TypeReference<T> typeRef) {
        return streamToType(this.getClass().getResourceAsStream(classPath), typeRef);
    }

    @Override
    public <T> T classpathToType(String classPath, Class<T> aClass) {
        return streamToType(this.getClass().getResourceAsStream(classPath), aClass);
    }

    @Override
    public <T> T fileToType(String filePath, TypeReference<T> typeRef) {
        try (FileInputStream fileInputStream = new FileInputStream(filePath)) {
            return streamToType(fileInputStream, typeRef);
        } catch (IOException e) {
            throw new RuntimeException("Unable to load JSON file from: " + filePath);
        }
    }

    @Override
    public <T> T fileToType(String filePath, Class<T> aClass) {
        try (FileInputStream fileInputStream = new FileInputStream(filePath)) {
            return streamToType(fileInputStream, aClass);
        } catch (IOException e) {
            throw new RuntimeException("Unable to load JSON file from: " + filePath);
        }
    }

    @Override
    public <T> T streamToType(InputStream in, TypeReference<T> typeRef) {
        try {
            return objectMapper.readValue(in, typeRef);
        } catch (JacksonException e) {
            throw new JsonUnmarshalException("Unable to unmarshal JSON to type: " + typeRef, e);
        }
    }

    @Override
    public <T> T streamToType(InputStream in, Class<T> aClass) {
        try {
            return objectMapper.readValue(in, aClass);
        } catch (JacksonException e) {
            throw new JsonUnmarshalException("Unable to unmarshal JSON to class: " + aClass, e);
        }
    }


    // SERIALIZATION
    @Override
    public String toJsonString(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JacksonException e) {
            throw new JsonMarshalException("Unable to serialize object : " + obj, e);
        }
    }

    @Override
    public String toPrettyJsonString(Object obj) {
        try {
            return prettyPrintWriter.writeValueAsString(obj);
        } catch (JacksonException e) {
            throw new JsonMarshalException("Unable to serialize object : " + obj, e);
        }
    }

    @Override
    public Object cloneJson(Object obj) {
        String string = this.toJsonString(obj);
        return this.jsonToObject(string);
    }
}
