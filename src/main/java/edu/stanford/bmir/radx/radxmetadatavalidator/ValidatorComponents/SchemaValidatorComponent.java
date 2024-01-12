package edu.stanford.bmir.radx.radxmetadatavalidator.ValidatorComponents;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.github.fge.jsonschema.main.JsonValidator;
import edu.stanford.bmir.radx.radxmetadatavalidator.ValidationLevel;
import edu.stanford.bmir.radx.radxmetadatavalidator.ValidationName;
import edu.stanford.bmir.radx.radxmetadatavalidator.ValidationResult;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
public class SchemaValidatorComponent {
  private static JsonNode schema = null;
  private final String WARNING = "warning";
  private final String ERROR = "error";
  private final String SCHEMA = "schema";
  private final String POINTER = "pointer";

  public void validate(JsonNode schema, JsonNode instance, Consumer<ValidationResult> handler) throws ProcessingException {
    JsonValidator validator = JsonSchemaFactory.byDefault().getValidator();
    var report = validator.validate(schema, instance);
    parseProcessingReportMessages(report, handler);
  }

  public void parseProcessingReportMessages(ProcessingReport report, Consumer<ValidationResult> handler){
    for (ProcessingMessage message : report) {
      var logLevel = message.getLogLevel().toString();
      var messageContent = message.getMessage();
      var jsonNode = message.asJson();
      var schemaNode = jsonNode.path(SCHEMA);
      var pointer = normalizePointer(schemaNode.path(POINTER).toString());
      if (logLevel.equals(WARNING)) {
        handler.accept(new ValidationResult(ValidationLevel.WARNING, ValidationName.SCHEMA_VALIDATION, messageContent, pointer));
      } else if (logLevel.equals(ERROR)) {
        handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.SCHEMA_VALIDATION, messageContent, pointer));
      }
    }
  }

  private String normalizePointer(String pointer){
    pointer = pointer.trim();
    if(pointer.startsWith("\"/properties")){
      return pointer.replaceFirst("/properties", "");
    } else{
      return pointer;
    }
  }
}
