package edu.stanford.bmir.radx.metadata.validator.lib.validators;

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
          String errorMessage = "Missing required value at " + path;
          // If it is link type, check @id
          if (valueConstraint.get().isLinkValueConstraint()){
            if(jsonLdId.isEmpty() || jsonLdId.get().toString().equals("")){
              handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.REQUIREMENT_VALIDATION, errorMessage, path));
            }
          } else if (valueConstraint.get().isControlledTermValueConstraint()){//if it is controlled term, check @label and @id
            if (jsonLdLabel.isEmpty() || jsonLdLabel.get().equals("")){
              var errorMessage2 = "rdfs:label is missing" + " at " + path;
              handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.REQUIREMENT_VALIDATION, errorMessage2, path));
            }
            if (jsonLdId.isEmpty() || jsonLdId.get().toString().equals("")){
              var errorMessage3 = "@id is missing" + " at " + path;
              handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.REQUIREMENT_VALIDATION, errorMessage3, path));
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
