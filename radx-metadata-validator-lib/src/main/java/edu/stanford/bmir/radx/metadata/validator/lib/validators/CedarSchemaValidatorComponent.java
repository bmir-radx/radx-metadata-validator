package edu.stanford.bmir.radx.metadata.validator.lib.validators;

import com.fasterxml.jackson.databind.JsonNode;
import edu.stanford.bmir.radx.metadata.validator.lib.ValidationLevel;
import edu.stanford.bmir.radx.metadata.validator.lib.ValidationResult;
import edu.stanford.bmir.radx.metadata.validator.lib.ValidationName;
import org.metadatacenter.model.validation.CedarValidator;
import org.metadatacenter.model.validation.ModelValidator;
import org.metadatacenter.model.validation.report.ErrorItem;
import org.metadatacenter.model.validation.report.ValidationReport;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
public class CedarSchemaValidatorComponent {
  public void validate(JsonNode templateNode, Consumer<ValidationResult> handler) throws Exception {
    ModelValidator cedarModelValidator = new CedarValidator();
    ValidationReport validationReport = cedarModelValidator.validateTemplate(templateNode);

    if (validationReport.getValidationStatus().equals("true")) {
      System.out.println("Template is valid");
    } else {
      System.out.println("Template is invalid. Found " + validationReport.getErrors().size() + " error(s)");
      for (ErrorItem errorItem : validationReport.getErrors()) {
        handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.CEDAR_MODEL_VALIDATION, "Template is invalid. " + errorItem.getMessage(), ""));
      }
    }
  }
}
