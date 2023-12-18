package edu.stanford.bmir.radx.radxmetadatavalidator;

import org.metadatacenter.artifacts.model.core.TemplateInstanceArtifact;

import java.util.Map;

public class TemplateInstanceValuesReporter {
  private final Map<String, Map<SchemaProperties, Object>> values;

  public TemplateInstanceValuesReporter(TemplateInstanceArtifact templateInstanceArtifact){
    ValuesVisitor valuesVisitor = new ValuesVisitor();
    templateInstanceArtifact.accept(valuesVisitor);

    values = Map.copyOf(valuesVisitor.getValues());
  }

  public Map<String, Map<SchemaProperties, Object>> getValues(){
    return this.values;
  }

  public void printValues(Map<String, Map<SchemaProperties, Object>> values){
    for (Map.Entry<String, Map<SchemaProperties, Object>> pointer : values.entrySet()) {
      System.out.println("Pointer: " + pointer.getKey());

      Map<SchemaProperties, Object> innerMap = pointer.getValue();
      for (Map.Entry<SchemaProperties, Object> property : innerMap.entrySet()) {
        System.out.println("  Property: " + property.getKey());
        System.out.println("  Value: " + property.getValue());
      }
    }
  }
}
