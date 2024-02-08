package edu.stanford.bmir.radx.metadata.validator.lib;

public enum RADxSpecificFieldPath {
  SHA256_DIGEST(new FieldPath("Data File Identity", "SHA256 digest")),
  FILE_NAME(new FieldPath("Data File Identity", "File Name")),
  DATA_DICT_FILE_NAME(new FieldPath("Data File Data Dictionary", "Data Dictionary File Name"));

  private final FieldPath fieldPath;

  RADxSpecificFieldPath(FieldPath fieldPath) {
    this.fieldPath = fieldPath;
  }

  public FieldPath getFieldPath() {
    return this.fieldPath;
  }
}
