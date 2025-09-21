package com.marcel.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtils {
    public static final ObjectMapper jsonMapper = new ObjectMapper();

    public static <T> T ParseJSON(String json, Class<T> type) {
        try {
            return jsonMapper.readValue(json, type);
        } catch (JsonProcessingException e) {
            System.err.println("> ERROR: Failed to parse JSON!");
            e.printStackTrace();
            return null;
        }
    }

    public static <T> String CreateJSON(T obj, boolean pretty) {
        try {
            return pretty ?
                    jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj) :
                    jsonMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T ParseJSONFile(String path, Class<T> type) {
        return ParseJSON(FsStuff.ReadEntireFile(path), type);
    }

    public static <T> boolean CreateJSONFile(String path, T obj, boolean pretty) {
        try {
            return FsStuff.WriteEntireFile(path, CreateJSON(obj, pretty));
        } catch (Exception e) {
            System.err.println("> WARNING: Failed to write JSON!");
            e.printStackTrace();
            return false;
        }
    }

    public static <T> boolean CreateJSONFile(String path, T obj) {
        return CreateJSONFile(path, obj, true);
    }
}
