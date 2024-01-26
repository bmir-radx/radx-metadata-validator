package edu.stanford.bmir.radx.metadata.validator.lib.ValidatorComponents;

import edu.stanford.bmir.radx.metadata.validator.lib.*;
import org.metadatacenter.artifacts.model.visitors.TemplateReporter;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Consumer;

@Component
public class RequiredFieldValidatorComponent {
  public void validate(TemplateReporter templateReporter, TemplateInstanceValuesReporter valuesReporter, Consumer<ValidationResult> handler){
    var values = valuesReporter.getValues();
    //Iterate the valuesReporter
    for (Map.Entry<String, FieldValues> fieldEntry : values.entrySet()) {
      String path = fieldEntry.getKey();
      FieldValues fieldValues = fieldEntry.getValue();
      var jsonLdValue = fieldValues.jsonLdValue();
      var jsonLdId = fieldValues.jsonLdId();
      var jsonLdLabel = fieldValues.label();
      var valueConstraint = templateReporter.getValueConstraints(path);
      //If it is required
      if(valueConstraint.isPresent()){
        if(valueConstraint.get().requiredValue()){
          String errorMessage = "The field is required but a null value is received";
          // If it is link type, check @id
          if (valueConstraint.get().isLinkValueConstraint()){
            if(jsonLdId.isEmpty() || jsonLdId.get().toString().equals("")){
              handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.REQUIREMENT_VALIDATION, errorMessage, path));
            }
          } else if (valueConstraint.get().isControlledTermValueConstraint()){//if it is controlled term, check @label and @id
            if (jsonLdLabel.isEmpty() || jsonLdLabel.get().equals("")){
              handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.REQUIREMENT_VALIDATION, "rdfs:label is required but a null value is received", path));
            }
            if (jsonLdId.isEmpty() || jsonLdId.get().toString().equals("")){
              handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.REQUIREMENT_VALIDATION, "@id is required but a null value is received", path));
            }
          } else {
            //For others, check @value
            if (jsonLdValue.isEmpty() || jsonLdValue.get().equals("")){
              handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.REQUIREMENT_VALIDATION, errorMessage, path));
            }
          }
        }
      }
    }
  }
}
