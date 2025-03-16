package io.github.dayfit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

public class JSON {
    private final static ObjectMapper JSON_MAPPER = getJsonMapper();

    private static ObjectMapper getJsonMapper()
    {
        return new ObjectMapper();
    }

    public static String toJSON(Object obj) throws JsonProcessingException {
        return JSON_MAPPER.writeValueAsString(obj);
    }

    public static <T> T fromJSON(String json, Class<T> clazz) throws JsonProcessingException {
        return JSON_MAPPER.readValue(json, clazz);
    }

    public static void saveJSON(String json, Path path) throws IOException {
        if (path.getParent() != null && !Files.exists(path.getParent())) {
            throw new NoSuchFileException("No such file or directory");
        }
        Files.write(path, json.getBytes());
    }
}
