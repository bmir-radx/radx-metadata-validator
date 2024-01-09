package edu.stanford.bmir.radx.radxmetadatavalidator.ValidatorComponents;

import edu.stanford.bmir.radx.radxmetadatavalidator.*;
import org.metadatacenter.artifacts.model.visitors.TemplateReporter;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Consumer;

@Component
public class CardinalsValidatorComponent {
  public void validate(TemplateReporter valueConstraintsReporter, TemplateInstanceValuesReporter valuesReporter, Consumer<ValidationResult> handler){
    var cardinals = valuesReporter.getCardinals();

    for (Map.Entry<String, Integer> Entry : cardinals.entrySet()) {
      String path = Entry.getKey();
      int size = Entry.getValue();
      var valueConstraint = valueConstraintsReporter.getValueConstraints(path);

      if(valueConstraint.isPresent()){
        if (!valueConstraint.get().multipleChoice()){
          if (size > 1){
            String message = "The artifact is configured to disallow multiple instances, but multiple instances have been provided. ";
            handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.CARDINAL_VALIDATION, message, path));
          }
        }
      }
    }
  }
}
