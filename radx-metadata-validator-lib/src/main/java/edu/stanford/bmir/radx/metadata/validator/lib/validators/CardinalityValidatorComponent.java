package edu.stanford.bmir.radx.metadata.validator.lib.validators;

import edu.stanford.bmir.radx.metadata.validator.lib.TemplateInstanceValuesReporter;
import edu.stanford.bmir.radx.metadata.validator.lib.ValidationLevel;
import edu.stanford.bmir.radx.metadata.validator.lib.ValidationName;
import edu.stanford.bmir.radx.metadata.validator.lib.ValidationResult;
import org.metadatacenter.artifacts.model.core.ChildSchemaArtifact;
import org.metadatacenter.artifacts.model.visitors.TemplateReporter;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Consumer;

@Component("MetadataCardinalityValidatorComponent")
public class CardinalityValidatorComponent {
  public void validate(TemplateReporter templateReporter, TemplateInstanceValuesReporter valuesReporter, Consumer<ValidationResult> handler){
    var fieldCardinals = valuesReporter.getFieldCardinalities();
    var elementCardinals = valuesReporter.getElementCardinalities();

    for (Map.Entry<String, Integer> fieldEntry : fieldCardinals.entrySet()) {
      String path = fieldEntry.getKey();
      int size = fieldEntry.getValue();
      var fieldSchemaArtifact = templateReporter.getFieldSchema(path);
      fieldSchemaArtifact.ifPresent(schemaArtifact -> checkCardinality(schemaArtifact, size, handler, path));
    }

    for (Map.Entry<String, Integer> elementEntry : elementCardinals.entrySet()) {
      String path = elementEntry.getKey();
      int size = elementEntry.getValue();
      var elementSchemaArtifact = templateReporter.getElementSchema(path);
      elementSchemaArtifact.ifPresent(schemaArtifact -> checkCardinality(schemaArtifact, size, handler, path));
    }
  }

  private void checkCardinality (ChildSchemaArtifact artifact, Integer size, Consumer<ValidationResult> handler, String path){
    if(artifact.isMultiple()){
      if (artifact.minItems().isPresent() && size < artifact.minItems().get()){
        String message = String.format("Element configured to have at least %s instances, but %s is provided", artifact.minItems().get(), size) + " at " + path;
        handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.CARDINALITY_VALIDATION, message, path));
      } else if (artifact.maxItems().isPresent() && size > artifact.maxItems().get()) {
        String message = String.format("Element configured to have at most %s instances, but %s is provided", artifact.maxItems().get(), size) + " at " + path;
        handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.CARDINALITY_VALIDATION, message, path));
      }
    } else{
      if(size > 1){
        String message = "Multiple instances provided for element configured to allow only one" + " at " + path;
        handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.CARDINALITY_VALIDATION, message, path));
      }
    }
  }
}
