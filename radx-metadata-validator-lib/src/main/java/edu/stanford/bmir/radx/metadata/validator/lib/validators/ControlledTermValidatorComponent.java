package edu.stanford.bmir.radx.metadata.validator.lib.validators;

import edu.stanford.bmir.radx.metadata.validator.lib.*;
import org.metadatacenter.artifacts.model.visitors.TemplateReporter;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Consumer;

@Component
public class ControlledTermValidatorComponent {
  public void validate(TerminologyServerHandler terminologyServerHandler, TemplateReporter templateReporter, TemplateInstanceValuesReporter valuesReporter, Consumer<ValidationResult> handler){
    if (terminologyServerHandler.getTerminologyServerAPIKey() != null
        && terminologyServerHandler.getTerminologyServerEndPoint() != null){
      var values = valuesReporter.getValues();
      for (Map.Entry<String, FieldValues> fieldEntry : values.entrySet()) {
        String path = fieldEntry.getKey();
        FieldValues fieldValues = fieldEntry.getValue();
        var jsonLdId = fieldValues.jsonLdId();
        var jsonLdLabel = fieldValues.label();
        var valueConstraint = templateReporter.getValueConstraints(path);
        if(valueConstraint.isPresent() && valueConstraint.get().isControlledTermValueConstraint()){
          var controlledTermValues = terminologyServerHandler.getAllValues(valueConstraint.get().asControlledTermValueConstraints());
          if(jsonLdId.isPresent()){
            //check if @id is within values get from terminology server
            var id = jsonLdId.get().toString();
            if(controlledTermValues.containsKey(id)){
              //check prefLabel
              var prefLabel = controlledTermValues.get(id);
              if(jsonLdLabel.isPresent() && !jsonLdLabel.get().equals(prefLabel)){
                String warningMessage = String.format("Expected %s on 'rdfs:label', but %s is given.", prefLabel, jsonLdLabel.get());
                handler.accept(new ValidationResult(ValidationLevel.WARNING, ValidationName.CONTROLLED_TERM_VALIDATION, warningMessage, path));
              }
            } else{
              String errorMessage = String.format("%s is not an element of set", jsonLdId.get());
              handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.CONTROLLED_TERM_VALIDATION, errorMessage, path));
            }
          }
        }
      }
    } else if (terminologyServerHandler.getTerminologyServerAPIKey() == null
        && terminologyServerHandler.getTerminologyServerEndPoint() != null) {
      handler.accept(new ValidationResult(ValidationLevel.WARNING, ValidationName.CONTROLLED_TERM_VALIDATION, "Please provide your CEDAR API Key to validate controlled terms values", ""));
    } else if (terminologyServerHandler.getTerminologyServerAPIKey() != null
        && terminologyServerHandler.getTerminologyServerEndPoint() == null) {
      handler.accept(new ValidationResult(ValidationLevel.WARNING, ValidationName.CONTROLLED_TERM_VALIDATION, "Please provide Terminology Server End Point to validate controlled terms values", ""));
    }
  }
}
