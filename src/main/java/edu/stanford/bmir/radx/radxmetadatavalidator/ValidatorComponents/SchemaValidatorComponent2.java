package edu.stanford.bmir.radx.radxmetadatavalidator.ValidatorComponents;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.ValidationMessage;
import edu.stanford.bmir.radx.radxmetadatavalidator.*;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.function.Consumer;

@Component
public class SchemaValidatorComponent2 {
  private static JsonNode schema = null;
  private final String WARNING = "warning";
  private final String ERROR = "error";
  private final String SCHEMA = "schema";
  private final String POINTER = "pointer";

  static{
    schema = JsonLoader.loadJson(Constants.RADX_TEMPLATE_SCHEMA_PATH);
  }


  public void validate(JsonNode instance, Consumer<ValidationResult> handler) {
    validateAgainstSchema(schema, instance, handler);
  }

  public void validateAgainstSchema(JsonNode schemaNode, JsonNode instanceNode, Consumer<ValidationResult> handler) {
    JsonSchemaFactory schemaFactory = JsonSchemaFactory.getInstance();
    JsonSchema schema = schemaFactory.getSchema(schemaNode);
    // Validate
    Set<ValidationMessage> validationResult = schema.validate(instanceNode);

    // Print validation result
    for (ValidationMessage validationMessage : validationResult) {
      var path = validationMessage.getPath();
      var message = validationMessage.getMessage();
      var schemaPath = validationMessage.getSchemaPath();
      var code = validationMessage.getCode();
      System.out.println("Code" + code + " Path " + path + " schema path: " + schemaPath + " message: " + message);
      handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.SCHEMA_VALIDATION, message, schemaPath));
    }
  }

}
