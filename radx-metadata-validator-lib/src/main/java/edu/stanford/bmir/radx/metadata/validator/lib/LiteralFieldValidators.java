package edu.stanford.bmir.radx.metadata.validator.lib;

import java.util.Map;
import java.util.Optional;

public class LiteralFieldValidators {
  private final Map<FieldPath, LiteralFieldValidator> validator;

  public LiteralFieldValidators(Map<FieldPath, LiteralFieldValidator> validator) {
    this.validator = validator;
  }

  public Optional<LiteralFieldValidator> getValidator(FieldPath fieldPath){
    return Optional.ofNullable(validator.get(fieldPath));
  }
}
