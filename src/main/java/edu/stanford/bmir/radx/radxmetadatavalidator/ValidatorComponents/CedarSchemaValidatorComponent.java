package edu.stanford.bmir.radx.radxmetadatavalidator.ValidatorComponents;

import com.fasterxml.jackson.databind.JsonNode;
import edu.stanford.bmir.radx.radxmetadatavalidator.ValidationLevel;
import edu.stanford.bmir.radx.radxmetadatavalidator.ValidationName;
import edu.stanford.bmir.radx.radxmetadatavalidator.ValidationResult;
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
        handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.CEDAR_MODEL_VALIDATION, errorItem.getMessage(), ""));
      }
    }
  }
}
