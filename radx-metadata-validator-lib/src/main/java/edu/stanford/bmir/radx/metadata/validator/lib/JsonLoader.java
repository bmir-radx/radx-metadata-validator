package edu.stanford.bmir.radx.metadata.validator.lib;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Component
public class JsonLoader {
    public static JsonNode loadJson(String jsonFilePath) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            File file = new File(jsonFilePath);
            if(!file.exists()){
                throw new JsonParseException("File not found: " + jsonFilePath);
            }
//            return objectMapper.readTree(Objects.requireNonNull(
//                JsonLoader.class.getClassLoader().getResourceAsStream(jsonFilePath)));
            return objectMapper.readTree(new FileInputStream((file)));
        } catch (IOException e) {
            throw new JsonParseException("The provided file " + jsonFilePath + " is not a valid JSON file.");
        } catch (NullPointerException e){
            throw new JsonParseException("The file could not be found.");
        }
    }
}
