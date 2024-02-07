package edu.stanford.bmir.radx.metadata.validator.lib;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JsonLoader {
    public static JsonNode loadJson(String jsonContent, String file) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readTree(jsonContent);
        } catch (IOException e) {
            throw new JsonParseException(file + " is not a valid JSON file.");
        }
    }
}
