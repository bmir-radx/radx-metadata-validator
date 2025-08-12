package edu.stanford.bmir.radx.metadata.validator.lib.validators;

import edu.stanford.bmir.radx.metadata.validator.lib.*;
import org.metadatacenter.artifacts.model.core.TemplateSchemaArtifact;
import org.metadatacenter.artifacts.model.core.fields.constraints.ValueConstraints;
import org.metadatacenter.artifacts.model.visitors.TemplateReporter;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Consumer;

@Component
public class RequiredFieldValidatorComponent {
  private final FieldsCollector fieldsCollector = new FieldsCollector();

  public void validate(TemplateSchemaArtifact templateSchemaArtifact, TemplateReporter templateReporter, TemplateInstanceValuesReporter valuesReporter, Consumer<ValidationResult> handler){
    // literate template schema and get all required fields
    var requiredFields = getAllRequiredFields(templateSchemaArtifact, templateReporter);

    // literate values report and check filled required fields.
    var checkedRequiredFields = new HashSet<String>();
    var values = valuesReporter.getValues();
    // Iterate the valuesReporter
    for (Map.Entry<String, FieldValues> fieldEntry : values.entrySet()) {
      checkedRequiredFields.add(normalizePath(fieldEntry.getKey()));
      validateSingleField(fieldEntry.getKey(), fieldEntry.getValue(), templateReporter, handler);
    }

    // add error message to unfilled required fields (only if the parent/container exists)
    for (var fieldPath : requiredFields) {
      String normReq = normalizePath(fieldPath);
      if (!checkedRequiredFields.contains(normReq)) {
        String parent = getParentPath(normReq);
        // If parent container doesn't exist in the instance, skip enforcing this required child
        if (parent != null && !checkedRequiredFields.contains(parent)) {
          continue;
        }
        String errorMessage = "Missing required value at " + normReq;
        handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.REQUIREMENT_VALIDATION, errorMessage, normReq));
      }
    }
  }

  private void validateSingleField(String path, FieldValues fieldValues, TemplateReporter templateReporter, Consumer<ValidationResult> handler){
    var jsonLdValue = fieldValues.jsonLdValue();
    var jsonLdId = fieldValues.jsonLdId();
    var jsonLdLabel = fieldValues.label();
    var valueConstraint = templateReporter.getValueConstraints(path);

    if (valueConstraint.isPresent()) {
      if (valueConstraint.get().requiredValue()) {
        String errorMessage = "Missing required value at " + path;
        // If it is link type, check @id
        if (valueConstraint.get().isLinkValueConstraint()) {
          if (jsonLdId.isEmpty() || jsonLdId.get().toString().equals("")) {
            handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.REQUIREMENT_VALIDATION, errorMessage, path));
          }
        } else if (valueConstraint.get().isControlledTermValueConstraint()) { // if it is controlled term, check @label and @id
          if (jsonLdLabel.isEmpty() || jsonLdLabel.get().equals("")) {
            var errorMessage2 = "rdfs:label is missing" + " at " + path;
            handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.REQUIREMENT_VALIDATION, errorMessage2, path));
          }
          if (jsonLdId.isEmpty() || jsonLdId.get().toString().equals("")) {
            var errorMessage3 = "@id is missing" + " at " + path;
            handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.REQUIREMENT_VALIDATION, errorMessage3, path));
          }
        } else {
          // For others, check @value
          if (jsonLdValue.isEmpty() || jsonLdValue.get().equals("")) {
            handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.REQUIREMENT_VALIDATION, errorMessage, path));
          }
        }
      }
    }
  }

  private List<String> getAllRequiredFields(TemplateSchemaArtifact templateSchemaArtifact, TemplateReporter templateReporter){
    var requiredFields = new ArrayList<String>();
    var allFields = fieldsCollector.getAllFields(templateSchemaArtifact);
    for (var field : allFields) {
      var fieldConstraint = templateReporter.getValueConstraints(field);
      if (isRequiredField(fieldConstraint)) {
        requiredFields.add(field);
      }
    }
    return requiredFields;
  }

  private boolean isRequiredField(Optional<ValueConstraints> valueConstraints){
    return valueConstraints.map(ValueConstraints::requiredValue).orElse(false);
  }

  private String normalizePath(String path){
    return path.replaceAll("\\[\\d+\\]", "");
  }

  // NEW: get direct parent container path (or null if none)
  private String getParentPath(String path) {
    int idx = path.lastIndexOf('/');
    return (idx > 0) ? path.substring(0, idx) : null;
  }
}
