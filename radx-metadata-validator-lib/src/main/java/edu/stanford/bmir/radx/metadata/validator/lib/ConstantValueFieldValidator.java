package edu.stanford.bmir.radx.metadata.validator.lib;

import com.fasterxml.jackson.core.JsonPointer;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Consumer;

public class ConstantValueFieldValidator implements LiteralFieldValidator{
  private final String expectedValue;
  private final String errorMessage;
  private final String warningMessage;

  public ConstantValueFieldValidator(String expectedValue, String errorMessage, String warningMessage) {
    this.expectedValue = expectedValue;
    this.errorMessage = errorMessage;
    this.warningMessage = warningMessage;
  }

  @Override
  public void validate(Optional<String> value, @Nullable String type, @Nullable String lang, Consumer<ValidationResult> handler, JsonPointer fieldPath) {
    if(value.isPresent()){
      if(!expectedValue.equals(value.get())){
        handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.LITERAL_FIELD_VALIDATION, errorMessage, fieldPath.toString()));
      }
    } else{
      handler.accept(new ValidationResult(ValidationLevel.WARNING, ValidationName.LITERAL_FIELD_VALIDATION, warningMessage, fieldPath.toString()));
    }

  }

  public String getExpectedValue(){
    return this.expectedValue;
  }
}
