package edu.stanford.bmir.radx.radxmetadatavalidator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;

@Component
public class JsonLoader {
    public static JsonNode loadJson(String jsonFilePath) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readTree(Objects.requireNonNull(
                JsonLoader.class.getClassLoader().getResourceAsStream(jsonFilePath)));
        } catch (IOException e) {
            throw new JsonParseException("The provided file " + jsonFilePath + " is not a valid JSON file.");
        }
    }
}
