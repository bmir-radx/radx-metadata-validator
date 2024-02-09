package edu.stanford.bmir.radx.metadata.validator.lib.validators;

import com.fasterxml.jackson.core.JsonPointer;
import edu.stanford.bmir.radx.metadata.validator.lib.*;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.function.Consumer;

@Component
public class RadxPrecisionValidatorComponent {
  public void validate(LiteralFieldValidators literalFieldValidators, TemplateInstanceValuesReporter valuesReporter, Consumer<ValidationResult> handler){
    var values = valuesReporter.getValues();
    var sha256DigestFieldPath = RADxSpecificFieldPath.SHA256_DIGEST.getFieldPath();
    var sha256DigestValidator = literalFieldValidators.getValidator(sha256DigestFieldPath);
    if(sha256DigestValidator.isPresent()){
      if(values.containsKey(sha256DigestFieldPath.getPath())){
        var sha256DigestValue = values.get(sha256DigestFieldPath.getPath()).jsonLdValue();
        validateSingleArg(sha256DigestValidator.get(), sha256DigestFieldPath, sha256DigestValue, handler);
      } else{
        var expectedValue = sha256DigestValidator.get().getExpectedValue();
        String warningMessage = String.format("Expected SHA256 digest equals to %s, but an empty value is received.", expectedValue);
        handler.accept(new ValidationResult(ValidationLevel.WARNING, ValidationName.LITERAL_FIELD_VALIDATION, warningMessage, sha256DigestFieldPath.toString()));
      }
    }

    var fileNameFieldPath = RADxSpecificFieldPath.FILE_NAME.getFieldPath();
    var fileNameValidator = literalFieldValidators.getValidator(fileNameFieldPath);
    if(fileNameValidator.isPresent()){
      if(values.containsKey(fileNameFieldPath.getPath())){
        var fileNameValue = values.get(fileNameFieldPath.getPath()).jsonLdValue();
        validateSingleArg(fileNameValidator.get(), fileNameFieldPath, fileNameValue, handler);
      } else{
        var expectedValue = fileNameValidator.get().getExpectedValue();
        String warningMessage = String.format("Expected File Name equals to %s, but an empty value is received.", expectedValue);
        handler.accept(new ValidationResult(ValidationLevel.WARNING, ValidationName.LITERAL_FIELD_VALIDATION, warningMessage, fileNameFieldPath.toString()));
      }
    }

    var dictFileNameFieldPath = RADxSpecificFieldPath.DATA_DICT_FILE_NAME.getFieldPath();
    var dictFileNameValidator = literalFieldValidators.getValidator(dictFileNameFieldPath);
    if(dictFileNameValidator.isPresent()){
      if(values.containsKey(dictFileNameFieldPath.getPath())){
        var dictFileNameValue = values.get(dictFileNameFieldPath.getPath()).jsonLdValue();
        validateSingleArg(dictFileNameValidator.get(), dictFileNameFieldPath, dictFileNameValue, handler);
      } else{
        var expectedValue = dictFileNameValidator.get().getExpectedValue();
        String warningMessage = String.format("Expected Data Dictionary File Name equals to %s, but an empty value is received.", expectedValue);
        handler.accept(new ValidationResult(ValidationLevel.WARNING, ValidationName.LITERAL_FIELD_VALIDATION, warningMessage, fileNameFieldPath.toString()));
      }
    }
  }

  private void validateSingleArg(LiteralFieldValidator validator, FieldPath path, Optional<String> value,  Consumer<ValidationResult> handler){
    JsonPointer fieldPath = JsonPointer.compile(path.getPath());
    validator.validate(value, null, null, handler, fieldPath);
  }
}
