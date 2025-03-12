package io.github.dayfit;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

record MyObject(String name, int value) {
}

class JSONTest {

    @Test
    void toJSON_withEmptyObject_returnsEmptyJSONString() throws JsonProcessingException {
        MyObject obj = new MyObject("", 0);
        String json = JSON.toJSON(obj);
        assertNotNull(json);
        assertTrue(json.contains("\"name\":\"\""));
        assertTrue(json.contains("\"value\":0"));
    }

    @Test
    void fromJSON_withEmptyObjectString_returnsEmptyObject() throws JsonProcessingException {
        String json = "{\"name\":\"\",\"value\":0}";
        MyObject obj = JSON.fromJSON(json, MyObject.class);
        assertNotNull(obj);
        assertEquals("", obj.name());
        assertEquals(0, obj.value());
    }

    @Test
    void toJSON_withSpecialCharacters_returnsEscapedJSONString() throws JsonProcessingException {
        MyObject obj = new MyObject("test\"name", 123);
        String json = JSON.toJSON(obj);
        assertNotNull(json);
        assertTrue(json.contains("\"name\":\"test\\\"name\""));
        assertTrue(json.contains("\"value\":123"));
    }

    @Test
    void fromJSON_withSpecialCharactersString_returnsObject() throws JsonProcessingException {
        String json = "{\"name\":\"test\\\"name\",\"value\":123}";
        MyObject obj = JSON.fromJSON(json, MyObject.class);
        assertNotNull(obj);
        assertEquals("test\"name", obj.name());
        assertEquals(123, obj.value());
    }


    @Test
    void saveJSON_withValidFilePath_writesJSONStringToFile() throws IOException {
        String jsonContent = "{\"name\":\"test\",\"value\":123}";
        Path tempFile = Files.createTempFile("saveJSONTest", ".json");
        Files.deleteIfExists(tempFile);

        JSON.saveJSON(jsonContent, tempFile);
        assertTrue(Files.exists(tempFile));

        String fileContent = Files.readString(tempFile);

        assertEquals(jsonContent, fileContent);
        Files.deleteIfExists(tempFile);
    }

    @Test
    void saveJSON_withInvalidFilePath_doesNotCreateFile() throws IOException {
        String jsonContent = "{\"name\":\"test\",\"value\":123}";

        Path tempDir = Files.createTempDirectory("tempDir");
        Files.deleteIfExists(tempDir);

        Path invalidPath = tempDir.resolve("nonExistingDirectory").resolve("saveJSONTest.json");

        assertThrows(NoSuchFileException.class, () -> JSON.saveJSON(jsonContent, invalidPath));
        assertFalse(Files.exists(invalidPath));
    }
}