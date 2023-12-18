package edu.stanford.bmir.radx.radxmetadatavalidator;

import edu.stanford.bmir.radx.radxmetadatavalidator.ValidationLevel;
import edu.stanford.bmir.radx.radxmetadatavalidator.ValidationName;
import jakarta.annotation.Nonnull;

public class ValidationResult {
  private ValidationLevel validationLevel;
  private ValidationName validationName;
  private String message;
  private String pointer;

  public ValidationResult(ValidationLevel validationLevel, ValidationName validationName, String message, String pointer) {
    this.validationLevel = validationLevel;
    this.validationName = validationName;
    this.message = message;
    this.pointer = pointer;
  }

  /**
   * Retrieves the validation level of the result.
   *
   * @return The {@link ValidationLevel} indicating the severity of the validation result.
   */
  public ValidationLevel validationLevel() {
    return this.validationLevel;
  }

  /**
   * Retrieves the message associated with the validation result. This is a human-readable string that can be
   * displayed in a user-interface.
   *
   * @return A non-null string representing the message.
   */
  @Nonnull
  public String message() {
    return this.message;
  }

  /**
   * Retrieves the name of the validation.  This is a human-readable string that can be
   * displayed in a user-interface.
   *
   * @return The {@Link ValidationName} representing the name of the validation.
   */
  @Nonnull
  public ValidationName validationName() {
    return this.validationName;
  }

  /**
   * Retrieves the pointer of the validation.  This is usually the location of the error or warning
   * that the validation result pertains to.  This is a human-readable string that can be
   * displayed in a user-interface.
   *
   * @return A non-null string representing the pointer of the validation.
   */
  @Nonnull
  public String pointer() {
    return this.pointer;
  }
}
