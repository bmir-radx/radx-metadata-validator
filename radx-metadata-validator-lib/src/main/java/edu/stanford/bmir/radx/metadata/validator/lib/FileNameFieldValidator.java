package edu.stanford.bmir.radx.metadata.validator.lib;

public class FileNameFieldValidator implements LiteralFieldValidator{
  private final String value;

  public FileNameFieldValidator(String value) {
    this.value = value;
  }
}
