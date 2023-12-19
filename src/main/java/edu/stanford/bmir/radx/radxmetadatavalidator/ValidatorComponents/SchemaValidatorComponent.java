package edu.stanford.bmir.radx.radxmetadatavalidator.ValidatorComponents;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.github.fge.jsonschema.main.JsonValidator;
import edu.stanford.bmir.radx.radxmetadatavalidator.Constants;
import edu.stanford.bmir.radx.radxmetadatavalidator.JsonLoader;
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

  static{
    schema = JsonLoader.loadJson(Constants.RADX_TEMPLATE_SCHEMA_PATH);
  }


  public void validate(JsonNode instance, Consumer<ValidationResult> handler) throws ProcessingException {
    validateAgainstSchema(schema, instance, handler);
  }

  public void validateAgainstSchema(JsonNode schema, JsonNode instance, Consumer<ValidationResult> handler) throws ProcessingException {
    JsonValidator validator = JsonSchemaFactory.byDefault().getValidator();
    var report = validator.validate(schema, instance);
    parseProcessingReportMessages(report, handler);
  }

  public String extractProcessingReportMessages(ProcessingReport report) {
    StringBuilder msg = new StringBuilder();
    if (report.isSuccess()) {
      msg.append("The instance you uploaded is valid. \n");}

    for (final ProcessingMessage reportLine : report) {
      msg.append(reportLine.getMessage()).append("\n");
    }
    return msg.toString();
  }

  public void parseProcessingReportMessages(ProcessingReport report, Consumer<ValidationResult> handler){
    for (ProcessingMessage message : report) {
      var logLevel = message.getLogLevel().toString();
      var messageContent = message.getMessage();
      var jsonNode = message.asJson();
      var schemaNode = jsonNode.path(SCHEMA);
      //TODO: need to modify the pointer?
      var pointer = schemaNode.path(POINTER).toString();
      if (logLevel.equals(WARNING)) {
        handler.accept(new ValidationResult(ValidationLevel.WARNING, ValidationName.SCHEMA_VALIDATION, messageContent, pointer));
      } else if (logLevel.equals(ERROR)) {
        handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.SCHEMA_VALIDATION, messageContent, pointer));
      }
    }
  }
}
