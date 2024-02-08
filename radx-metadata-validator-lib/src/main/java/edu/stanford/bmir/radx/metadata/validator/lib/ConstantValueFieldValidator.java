package edu.stanford.bmir.radx.metadata.validator.lib;

import com.fasterxml.jackson.core.JsonPointer;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class ConstantValueFieldValidator implements LiteralFieldValidator{
  private final String expectedValue;
  private final String errorMessage;
  private final ValidationLevel validationLevel;

  public ConstantValueFieldValidator(String expectedValue, String errorMessage, ValidationLevel validationLevel) {
    this.expectedValue = expectedValue;
    this.errorMessage = errorMessage;
    this.validationLevel = validationLevel;
  }

  @Override
  public void validate(String value, @Nullable String type, @Nullable String lang, Consumer<ValidationResult> handler, JsonPointer fieldPath) {
    if(!expectedValue.equals(value)){
      handler.accept(new ValidationResult(validationLevel, ValidationName.LITERAL_FIELD_VALIDATION, errorMessage, fieldPath.toString()));
    }
  }
}
