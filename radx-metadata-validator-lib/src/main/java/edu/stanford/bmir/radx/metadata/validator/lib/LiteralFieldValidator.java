package edu.stanford.bmir.radx.metadata.validator.lib;

import com.fasterxml.jackson.core.JsonPointer;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public interface LiteralFieldValidator {
  void validate(String value, @Nullable  String type, @Nullable String lang, Consumer<ValidationResult> handler, JsonPointer fieldPath);
}
