package edu.stanford.bmir.radx.radxmetadatavalidator.ValidatorComponents;

import edu.stanford.bmir.radx.radxmetadatavalidator.SchemaProperties;
import edu.stanford.bmir.radx.radxmetadatavalidator.TemplateInstanceValuesReporter;
import edu.stanford.bmir.radx.radxmetadatavalidator.ValidationLevel;
import edu.stanford.bmir.radx.radxmetadatavalidator.ValidationName;
import edu.stanford.bmir.radx.radxmetadatavalidator.ValidationResult;
import org.metadatacenter.artifacts.model.visitors.TemplateReporter;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

@Component
public class RequiredFieldValidatorComponent {
  public void validate(TemplateReporter templateReporter, TemplateInstanceValuesReporter valuesReporter, Consumer<ValidationResult> handler){
    var values = valuesReporter.getValues();
    //Iterate the valuesReporter
    for (Map.Entry<String, Map<SchemaProperties, Optional<?>>> fieldEntry : values.entrySet()) {
      String path = fieldEntry.getKey();
      Map<SchemaProperties, Optional<?>> fieldValue = fieldEntry.getValue();
      var valueConstraint = templateReporter.getValueConstraints(path);
      //If it is required
      if(valueConstraint.isPresent()){
        if(valueConstraint.get().requiredValue()){
          String errorMessage = "The field is required but got null";
          // If it is link type, check @id
          if (valueConstraint.get().isLinkValueConstraint()){
            if(fieldValue.get(SchemaProperties.ID).isEmpty()){
              handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.REQUIREMENT_VALIDATION, errorMessage, path));
            }
          } else if (valueConstraint.get().isControlledTermValueConstraint()){             //if it is controlled term, check @label and @id
            if (fieldValue.get(SchemaProperties.LABEL).isEmpty() || fieldValue.get(SchemaProperties.ID).isEmpty()){
              handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.REQUIREMENT_VALIDATION, errorMessage, path));
            }
          } else {
            //For others, check @value
            if (fieldValue.get(SchemaProperties.VALUE).isEmpty()){
              handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.REQUIREMENT_VALIDATION, errorMessage, path));
            }
          }
        }
      }
    }
  }
}
