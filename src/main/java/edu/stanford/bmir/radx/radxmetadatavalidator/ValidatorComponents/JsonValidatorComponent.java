package edu.stanford.bmir.radx.radxmetadatavalidator.ValidatorComponents;

import edu.stanford.bmir.radx.radxmetadatavalidator.JsonLoader;
import edu.stanford.bmir.radx.radxmetadatavalidator.ValidationLevel;
import edu.stanford.bmir.radx.radxmetadatavalidator.ValidationName;
import edu.stanford.bmir.radx.radxmetadatavalidator.ValidationResult;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.function.Consumer;

@Component
public class JsonValidatorComponent {

  public void validate(Path instanceFilePath, Consumer<ValidationResult> handler){
    if(!isValidJson(instanceFilePath)){
      String message = "The provided instance is not a valid JSON file.";
      handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.JSON_VALIDATION, message, ""));
    }
  }
  public static boolean isValidJson(Path jsonFilePath) {
    var schemaNode = JsonLoader.loadJson(String.valueOf(jsonFilePath));
    return schemaNode != null;
  }
}
