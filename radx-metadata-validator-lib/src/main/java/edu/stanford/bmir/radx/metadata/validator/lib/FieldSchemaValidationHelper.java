package edu.stanford.bmir.radx.metadata.validator.lib;

import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@Component
public class FieldSchemaValidationHelper {
  public void validateLiteralField(Optional<String> jsonValue, Optional<URI> jsonLdId, Optional<String> label, List<URI> jsonLdType, Consumer<ValidationResult> handler, String path, String type){
    if(jsonLdId.isPresent()){
      String errorMessage = String.format("\"@id\" is not allowed for %s field",type) + " at " + path;
      handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, errorMessage, path));
    }
    if(label.isPresent()){
      String errorMessage = String.format("\"rdfs:label\" is not allowed for %s field",type) + " at " + path;
      handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, errorMessage, path));
    }
    if(jsonLdType.size() > 1){
      String errorMessage = String.format("\"@type\" is not allowed for %s field",type) + " at " + path;
      handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, errorMessage, path));
    }
    if(jsonValue.isPresent() && jsonValue.get().toString().equals("")){
      String errorMessage = "\"@value\" : \"\" is expected to be \"@value\" : null" + " at " + path;
      handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, errorMessage, path));
    }
  }

  public void validateTextField(Optional<String> jsonValue, Optional<URI> jsonLdId, Optional<String> label, List<URI> jsonLdType, Consumer<ValidationResult> handler, String path){
    validateLiteralField(jsonValue, jsonLdId, label, jsonLdType, handler, path, "Text");
  }

  public void validateLinkField(Optional<URI> jsonLdId, Optional<String> jsonLdValue, Optional<String> label, List<URI> jsonLdType, Consumer<ValidationResult> handler, String path){
    if(jsonLdValue.isPresent()){
      handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, "\"@value\" is not allowed at "  + path, path));
    }
    if(label.isPresent()){
      handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, "\"rdfs:label\" is not allowed at " + path, path));
    }
    if(jsonLdType.size() > 1){
      handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, "\"@type\" is not allowed at " + path, path));
    }
    if(jsonLdId.isPresent() && jsonLdId.get().toString().equals("")){
      String errorMessage = "\"@id\" contains an empty string, which is not permitted. Please provide a valid value or remove the \"@id\" entry entirely at " + path;
      handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, errorMessage, path));
    }
  }

  public void validateNumericAndTemporalField(Optional<URI> jsonLdId, Optional<String> label, Consumer<ValidationResult> handler, String path){
    if(jsonLdId.isPresent()){
      handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, "\"@id\" is not allowed at " + path, path));
    }
    if(label.isPresent()){
      handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, "\"rdfs:label\" is not allowed at " + path, path));
    }
  }

  public void validateControlledTermField(Optional<URI> jsonLdId, Optional<String> label, Optional<String> jsonLdValue, List<URI> jsonLdType, Consumer<ValidationResult> handler, String path){
    if(jsonLdValue.isPresent()){
      handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, "\"@value\" is not allowed at " + path, path));
    }
    if(jsonLdType.size() > 1){
      handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, "\"@type\" is not allowed at " + path, path));
    }

    if(jsonLdId.isEmpty() && (label.isPresent() && !label.equals(Optional.of("")))){
      handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, "\"@id\" is expected at " + path, path));
    }

    if(label.isEmpty() && (jsonLdId.isPresent() && !jsonLdId.get().toString().equals(""))){
      handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, "\"rdfs:label\" is expected at " + path, path));
    }

    if(jsonLdId.isEmpty() && (label.isPresent() && label.equals(Optional.of("")))){
      String errorMessage = "\"rdfs:label\" contains an empty string, which is not permitted. Please provide a valid value or remove the \"rdfs:label\" entry entirely at " + path;
      handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, errorMessage, path));
    }

    if(label.isEmpty() && (jsonLdId.isPresent() && jsonLdId.get().toString().equals(""))){
      String errorMessage = "\"@id\" contains an empty string, which is not permitted. Please provide a valid value or remove the \"@id\" entry entirely at " + path;
      handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, errorMessage, path));
    }
  }

  public void validateAttributeValueField(Optional<String> jsonValue, Optional<URI> jsonLdId, Optional<String> label, List<URI> jsonLdType, Consumer<ValidationResult> handler, String path){
    validateLiteralField(jsonValue, jsonLdId, label, jsonLdType, handler, path, "Attribute Value");
  }
}
