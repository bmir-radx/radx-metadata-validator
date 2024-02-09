package edu.stanford.bmir.radx.metadata.validator.lib;

public record FieldPath(String ... path) {

  public String getPath(){
    return "/" + String.join("/", path);
  }
}
