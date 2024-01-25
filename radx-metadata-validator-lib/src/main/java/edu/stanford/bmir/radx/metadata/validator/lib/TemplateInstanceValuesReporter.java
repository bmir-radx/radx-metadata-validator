package edu.stanford.bmir.radx.metadata.validator.lib;

import org.metadatacenter.artifacts.model.core.TemplateInstanceArtifact;

import java.util.List;
import java.util.Map;

public class TemplateInstanceValuesReporter {
  private final Map<String, FieldValues> values;
  private final Map<String, Integer> fieldCardinalities;
  private final Map<String, Integer> elementCardinalities;
  private final List<AttributeValueFieldValues> attributeValueFields;

  public TemplateInstanceValuesReporter(TemplateInstanceArtifact templateInstanceArtifact){
    ValuesVisitor valuesVisitor = new ValuesVisitor();
    templateInstanceArtifact.accept(valuesVisitor);

    values = Map.copyOf(valuesVisitor.getValues());
    fieldCardinalities = Map.copyOf(valuesVisitor.getFieldCardinalities());
    elementCardinalities = Map.copyOf(valuesVisitor.getElementCardinalities());
    attributeValueFields = List.copyOf(valuesVisitor.getAttributeValueFields());
  }

  public Map<String, FieldValues> getValues(){
    return this.values;
  }

  public Map<String, Integer> getFieldCardinalities() {
    return fieldCardinalities;
  }

  public Map<String, Integer> getElementCardinalities() {
    return elementCardinalities;
  }

  public List<AttributeValueFieldValues> getAttributeValueFields() {
    return attributeValueFields;
  }
}
