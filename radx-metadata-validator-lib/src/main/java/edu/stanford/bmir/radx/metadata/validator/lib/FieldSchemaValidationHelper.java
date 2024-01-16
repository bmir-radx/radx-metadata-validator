package edu.stanford.bmir.radx.metadata.validator.lib;

import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.function.Consumer;

@Component
public class FieldSchemaValidationHelper {
  public void validateTextField(Optional<?> jsonLdId, Optional<?> label, Consumer<ValidationResult> handler, String path){
    if(jsonLdId.isPresent()){
      handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, "\"@id\" is not allowed for text type field", path));
    }
    if(label.isPresent()){
      handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, "\"rdfs:label\" is not allowed for text type field", path));
    }
    //TODO: add type check
  }

  public void validateLinkField(Optional<?> jsonLdValue, Optional<?> label, Consumer<ValidationResult> handler, String path){
    if(jsonLdValue.isPresent()){
      handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, "\"@value\" is not allowed for link type field", path));
    }
    if(label.isPresent()){
      handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, "\"rdfs:label\" is not allowed for link type field", path));
    }
    //TODO: add type check
  }

  public void validateControlledTermField(Optional<?> jsonLdValue, Consumer<ValidationResult> handler, String path){
    if(jsonLdValue.isPresent()){
      handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, "\"@value\" is not allowed for link type field", path));
    }
    //TODO: add type check
  }

  public void validateNumericAndTemporalField(Optional<?> jsonLdId, Optional<?> label, Consumer<ValidationResult> handler, String path){
    if(jsonLdId.isPresent()){
      handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, "\"@id\" is not allowed for text type field", path));
    }
    if(label.isPresent()){
      handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, "\"rdfs:label\" is not allowed for text type field", path));
    }
  }
}
