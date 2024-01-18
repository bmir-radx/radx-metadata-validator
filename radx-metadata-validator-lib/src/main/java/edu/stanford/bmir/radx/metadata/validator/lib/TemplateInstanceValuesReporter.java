package edu.stanford.bmir.radx.metadata.validator.lib;

import org.metadatacenter.artifacts.model.core.TemplateInstanceArtifact;

import java.util.Map;

public class TemplateInstanceValuesReporter {
  private final Map<String, FieldValues> values;
  private final Map<String, Integer> fieldCardinals;
  private final Map<String, Integer> elementCardinals;

  public TemplateInstanceValuesReporter(TemplateInstanceArtifact templateInstanceArtifact){
    ValuesVisitor valuesVisitor = new ValuesVisitor();
    templateInstanceArtifact.accept(valuesVisitor);

    values = Map.copyOf(valuesVisitor.getValues());
    fieldCardinals = Map.copyOf(valuesVisitor.getFieldCardinals());
    elementCardinals = Map.copyOf(valuesVisitor.getElementCardinals());
  }

  public Map<String, FieldValues> getValues(){
    return this.values;
  }

  public Map<String, Integer> getFieldCardinals() {
    return fieldCardinals;
  }

  public Map<String, Integer> getElementCardinals() {
    return elementCardinals;
  }
}
