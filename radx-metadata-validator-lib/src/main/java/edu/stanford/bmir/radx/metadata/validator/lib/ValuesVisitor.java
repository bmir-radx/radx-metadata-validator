package edu.stanford.bmir.radx.metadata.validator.lib;

import org.metadatacenter.artifacts.model.core.ElementInstanceArtifact;
import org.metadatacenter.artifacts.model.core.FieldInstanceArtifact;
import org.metadatacenter.artifacts.model.core.InstanceArtifactVisitor;
import org.metadatacenter.artifacts.model.core.TemplateInstanceArtifact;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ValuesVisitor implements InstanceArtifactVisitor {
  private final Map<String, FieldValues> values;
  private final Map<String, Integer> fieldCardinalities;
  private final Map<String, Integer> elementCardinalities;
  private final List<AttributeValueFieldValues> attributeValueFields;

  public ValuesVisitor() {
    this.values = new HashMap<>();
    this.fieldCardinalities = new HashMap<>();
    this.elementCardinalities = new HashMap<>();
    this.attributeValueFields = new ArrayList<>();
  }

  public Map<String, FieldValues> getValues() {
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

  @Override
  public void visitTemplateInstanceArtifact(TemplateInstanceArtifact templateInstanceArtifact) {
    var multiElementInstances = templateInstanceArtifact.multiInstanceElementInstances();
    var singleElementInstances = templateInstanceArtifact.singleInstanceElementInstances();
    var multiFieldInstances = templateInstanceArtifact.multiInstanceFieldInstances();
    var singleFieldInstances = templateInstanceArtifact.singleInstanceFieldInstances();
    parseMultiElementInstance(multiElementInstances, "");
    parseSingleElementInstance(singleElementInstances, "");
    parseMultiFieldInstances(multiFieldInstances, "");
    parseSingleFieldInstances(singleFieldInstances, "");
  }

  @Override
  public void visitElementInstanceArtifact(ElementInstanceArtifact elementInstanceArtifact, String path) {
    var multiElementInstances = elementInstanceArtifact.multiInstanceElementInstances();
    var singleElementInstances = elementInstanceArtifact.singleInstanceElementInstances();
    var multiFieldInstances = elementInstanceArtifact.multiInstanceFieldInstances();
    var singleFieldInstances = elementInstanceArtifact.singleInstanceFieldInstances();
    parseMultiElementInstance(multiElementInstances, path);
    parseSingleElementInstance(singleElementInstances, path);
    parseMultiFieldInstances(multiFieldInstances, path);
    parseSingleFieldInstances(singleFieldInstances, path);
  }

  @Override
  public void visitFieldInstanceArtifact(FieldInstanceArtifact fieldInstanceArtifact, String path) {
    values.put(path, new FieldValues(fieldInstanceArtifact.jsonLdTypes(),
            fieldInstanceArtifact.jsonLdId(),
            fieldInstanceArtifact.jsonLdValue(),
            fieldInstanceArtifact.label()));
  }

  @Override
  public void visitAttributeValueFieldInstanceArtifact(FieldInstanceArtifact fieldInstanceArtifact, String s, String s1) {
    attributeValueFields.add(new AttributeValueFieldValues(s, s1,
        new FieldValues(fieldInstanceArtifact.jsonLdTypes(),
            fieldInstanceArtifact.jsonLdId(),
            fieldInstanceArtifact.jsonLdValue(),
            fieldInstanceArtifact.label())
    ));
  }

  private void parseMultiFieldInstances(Map<String, List<FieldInstanceArtifact>> fieldInstances, String path){
    for (Map.Entry<String, List<FieldInstanceArtifact>> entry : fieldInstances.entrySet()) {
      String fieldName = entry.getKey();
      String childBasePath = path + "/" + fieldName;
      int size = entry.getValue().size();
      fieldCardinalities.put(childBasePath, size);
    }
  }

  private void parseSingleFieldInstances(Map<String, FieldInstanceArtifact> fieldInstances, String path){
    for (String fieldName : fieldInstances.keySet()) {
      String childBasePath = path + "/" + fieldName;
      fieldCardinalities.put(childBasePath, 1);
    }
  }

  private void parseMultiElementInstance(Map<String, List<ElementInstanceArtifact>> elementInstances, String path){
    for (Map.Entry<String, List<ElementInstanceArtifact>> entry : elementInstances.entrySet()) {
      String elementName = entry.getKey();
      String childBasePath = path + "/" + elementName;
      int size = entry.getValue().size();
      elementCardinalities.put(childBasePath, size);
    }
  }

  private void parseSingleElementInstance(Map<String, ElementInstanceArtifact> elementInstances, String path){
    for (String elementName : elementInstances.keySet()) {
      String childBasePath = path + "/" + elementName;
      elementCardinalities.put(childBasePath, 1);
    }
  }
}
