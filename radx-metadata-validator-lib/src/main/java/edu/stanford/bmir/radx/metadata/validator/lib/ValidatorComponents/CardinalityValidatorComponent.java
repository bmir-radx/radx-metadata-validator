package edu.stanford.bmir.radx.metadata.validator.lib.ValidatorComponents;

import edu.stanford.bmir.radx.metadata.validator.lib.TemplateInstanceValuesReporter;
import edu.stanford.bmir.radx.metadata.validator.lib.ValidationLevel;
import edu.stanford.bmir.radx.metadata.validator.lib.ValidationName;
import edu.stanford.bmir.radx.metadata.validator.lib.ValidationResult;
import org.metadatacenter.artifacts.model.visitors.TemplateReporter;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Consumer;

@Component
public class CardinalityValidatorComponent {
  public void validate(TemplateReporter templateReporter, TemplateInstanceValuesReporter valuesReporter, Consumer<ValidationResult> handler){
    var fieldCardinals = valuesReporter.getFieldCardinals();
    var elementCardinals = valuesReporter.getElementCardinals();

    for (Map.Entry<String, Integer> fieldEntry : fieldCardinals.entrySet()) {
      String path = fieldEntry.getKey();
      int size = fieldEntry.getValue();
      var fieldSchemaArtifact = templateReporter.getFieldSchema(path);
      if(fieldSchemaArtifact.isPresent()){
        if (!fieldSchemaArtifact.get().isMultiple() && size > 1){
          String message = "Multiple instances provided for field configured to allow only one.";
          handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.CARDINAL_VALIDATION, message, path));
        }
      }
    }

    for (Map.Entry<String, Integer> elementEntry : elementCardinals.entrySet()) {
      String path = elementEntry.getKey();
      int size = elementEntry.getValue();
      var elementSchemaArtifact = templateReporter.getElementSchema(path);
      if(elementSchemaArtifact.isPresent()){
        if (!elementSchemaArtifact.get().isMultiple() && size > 1){
          String message = "Multiple instances provided for element configured to allow only one.";
          handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.CARDINAL_VALIDATION, message, path));
        }
      }
    }
  }
}
