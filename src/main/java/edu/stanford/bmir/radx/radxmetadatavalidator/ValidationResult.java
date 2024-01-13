package edu.stanford.bmir.radx.radxmetadatavalidator;

import jakarta.annotation.Nonnull;

import java.util.Objects;

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

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;
    ValidationResult other = (ValidationResult) obj;
    return Objects.equals(validationLevel, other.validationLevel) &&
        Objects.equals(validationName, other.validationName) &&
        Objects.equals(message, other.message) &&
        Objects.equals(pointer, other.pointer);
  }

  @Override
  public int hashCode() {
    return Objects.hash(validationLevel, validationName, message, pointer);
  }
}
