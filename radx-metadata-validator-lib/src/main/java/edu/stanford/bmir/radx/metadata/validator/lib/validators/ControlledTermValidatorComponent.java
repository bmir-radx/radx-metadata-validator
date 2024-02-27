package edu.stanford.bmir.radx.metadata.validator.lib.validators;

import edu.stanford.bmir.radx.metadata.validator.lib.*;
import org.metadatacenter.artifacts.model.visitors.TemplateReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Consumer;

@Component
public class ControlledTermValidatorComponent {
  private static final Logger log = LoggerFactory.getLogger(ControlledTermValidatorComponent.class);
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
          if(jsonLdId.isPresent()){
            Map<String, String> controlledTermValues;
            var controlledTermConstraint = valueConstraint.get().asControlledTermValueConstraints();
//            check if the valueConstrain has been cached. If so, retrieve values from cache, otherwise, call terminology server.
            if(Cache.isCached(controlledTermConstraint)){
              log.info("Loading <" + jsonLdId + ", " + jsonLdLabel + "> from Cache");
              controlledTermValues = Cache.getCachePerValueConstraint(controlledTermConstraint);
            } else{
              log.info("Loading <" + jsonLdId + ", " + jsonLdLabel + ">  from terminology server");
              controlledTermValues = terminologyServerHandler.getAllValues(controlledTermConstraint);
            }

//            controlledTermValues = terminologyServerHandler.getAllValues(controlledTermConstraint);
//            log.info("Loading <" + jsonLdId + ", " + jsonLdLabel + ">  from terminology server");

            //check if @id is within values get from terminology server
            var id = jsonLdId.get().toString();
            if(controlledTermValues.containsKey(id)){
              //check prefLabel
              var prefLabel = controlledTermValues.get(id);
              if(jsonLdLabel.isPresent() && !jsonLdLabel.get().equals(prefLabel)){
                String warningMessage = String.format("Expected %s on 'rdfs:label', but %s is given.", prefLabel, jsonLdLabel.get());
                handler.accept(new ValidationResult(ValidationLevel.WARNING, ValidationName.CONTROLLED_TERM_VALIDATION, warningMessage, path));
              } else if (jsonLdLabel.isEmpty()) {
                String warningMessage = String.format("Expected %s on 'rdfs:label', but empty is given.", prefLabel);
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
