package edu.stanford.bmir.radx.radxmetadatavalidator.ValidatorComponents;

import edu.stanford.bmir.radx.radxmetadatavalidator.SchemaProperties;
import edu.stanford.bmir.radx.radxmetadatavalidator.TemplateInstanceValuesReporter;
import edu.stanford.bmir.radx.radxmetadatavalidator.ValidationLevel;
import edu.stanford.bmir.radx.radxmetadatavalidator.ValidationName;
import edu.stanford.bmir.radx.radxmetadatavalidator.ValidationResult;
import org.metadatacenter.artifacts.model.visitors.TemplateValueConstraintsReporter;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Consumer;

@Component
public class RequiredFieldValidatorComponent {
  public void validate(TemplateValueConstraintsReporter valueConstraintsReporter, TemplateInstanceValuesReporter valuesReporter, Consumer<ValidationResult> handler){
    var values = valuesReporter.getValues();
    //Iterate the valueConstraintsReporter
    for (Map.Entry<String, Map<SchemaProperties, Object>> entry : values.entrySet()) {
      String path = entry.getKey();
      Map<SchemaProperties, Object> fieldValue = entry.getValue();
      var valueConstraint = valueConstraintsReporter.getValueConstraints(path);
      //If it is required
      if (valueConstraint.isPresent() && valueConstraint.get().requiredValue()){
        //TODO: if it is link type, check @id
        valueConstraint.get().
        //if it is controlled term, check @label and @id
        if(valueConstraint.get().isControlledTermValueConstraint()){
          if (fieldValue.get(SchemaProperties.LABEL) == null){
            handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.REQUIREMENT_VALIDATION, "The field is required but got null", path));
          }
        } else {
          //For others, check @value
          if (fieldValue.get(SchemaProperties.VALUE) == null){
            handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.REQUIREMENT_VALIDATION, "The field is required but got null", path));
          }
        }
      }
    }

  }
}
