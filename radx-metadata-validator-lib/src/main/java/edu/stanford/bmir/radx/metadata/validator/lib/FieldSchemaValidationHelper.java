package edu.stanford.bmir.radx.metadata.validator.lib;

import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@Component
public class FieldSchemaValidationHelper {
  public void validateValueOnlyField(Optional<URI> jsonLdId, Optional<String> label, List<URI> jsonLdType, Consumer<ValidationResult> handler, String path, String type){
    if(jsonLdId.isPresent()){
      String errorMessage = String.format("\"@id\" is not allowed for %s field",type);
      handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, errorMessage, path));
    }
    if(label.isPresent()){
      String errorMessage = String.format("\"@id\" is not allowed for %s field",type);
      handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, errorMessage, path));
    }
    if(jsonLdType.size() > 1){
      String errorMessage = String.format("\"@id\" is not allowed for %s field",type);
      handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, errorMessage, path));
    }
  }

  public void validateTextField(Optional<URI> jsonLdId, Optional<String> label, List<URI> jsonLdType, Consumer<ValidationResult> handler, String path){
    validateValueOnlyField(jsonLdId, label, jsonLdType, handler, path, "Text");
  }

  public void validateLinkField(Optional<String> jsonLdValue, Optional<String> label, List<URI> jsonLdType, Consumer<ValidationResult> handler, String path){
    if(jsonLdValue.isPresent()){
      handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, "\"@value\" is not allowed for link type field", path));
    }
    if(label.isPresent()){
      handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, "\"rdfs:label\" is not allowed for link type field", path));
    }
    if(jsonLdType.size() > 1){
      handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, "\"@type\" is not allowed for link type field", path));
    }
  }

  public void validateNumericAndTemporalField(Optional<URI> jsonLdId, Optional<String> label, Consumer<ValidationResult> handler, String path){
    if(jsonLdId.isPresent()){
      handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, "\"@id\" is not allowed for numeric or temporal field", path));
    }
    if(label.isPresent()){
      handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, "\"rdfs:label\" is not allowed for numeric or temporal field", path));
    }
  }

  public void validateControlledTermField(Optional<URI> jsonLdId, Optional<String> label, Optional<String> jsonLdValue, List<URI> jsonLdType, Consumer<ValidationResult> handler, String path){
    if(jsonLdValue.isPresent()){
      handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, "\"@value\" is not allowed for controlled terms field", path));
    }
    if(jsonLdType.size() > 1){
      handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, "\"@type\" is not allowed for controlled terms field", path));
    }

    if(jsonLdId.isEmpty() && label.isPresent()){
      handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, "\"@id\" is expected for controlled terms field", path));
    }

    if(label.isEmpty() && jsonLdId.isPresent()){
      handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, "\"rdfs:label\" is expected for controlled terms field", path));
    }
  }

  public void validateAttributeValueField(Optional<URI> jsonLdId, Optional<String> label, List<URI> jsonLdType, Consumer<ValidationResult> handler, String path){
    validateValueOnlyField(jsonLdId, label, jsonLdType, handler, path, "Attribute Value");
  }
}
