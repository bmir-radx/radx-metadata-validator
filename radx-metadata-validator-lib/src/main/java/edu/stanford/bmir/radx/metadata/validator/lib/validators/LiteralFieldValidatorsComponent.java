package edu.stanford.bmir.radx.metadata.validator.lib.validators;

import edu.stanford.bmir.radx.metadata.validator.lib.FieldPath;
import edu.stanford.bmir.radx.metadata.validator.lib.LiteralFieldValidator;

import java.util.Map;
import java.util.Optional;

public class LiteralFieldValidatorsComponent {
  private final Map<FieldPath, LiteralFieldValidator> validator;

  public LiteralFieldValidatorsComponent(Map<FieldPath, LiteralFieldValidator> validator) {
    this.validator = validator;
  }

  public Optional<LiteralFieldValidator> getValidator(FieldPath fieldPath){
    return Optional.ofNullable(validator.get(fieldPath));
  }
}
