package edu.stanford.bmir.radx.metadata.validator.lib;

import org.metadatacenter.artifacts.model.core.ElementInstanceArtifact;
import org.metadatacenter.artifacts.model.core.FieldInstanceArtifact;
import org.metadatacenter.artifacts.model.core.InstanceArtifactVisitor;
import org.metadatacenter.artifacts.model.core.TemplateInstanceArtifact;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class ValuesVisitor implements InstanceArtifactVisitor {
  private final Map<String, Map<SchemaProperties, Optional<?>>> values;
  private final Map<String, Integer> cardinals;

  public ValuesVisitor() {
    this.values = new HashMap<>();
    this.cardinals = new HashMap<>();
  }

  public Map<String, Map<SchemaProperties, Optional<?>>> getValues() {
    return this.values;
  }

  public Map<String, Integer> getCardinals() {
    return cardinals;
  }

  @Override
  public void visitTemplateInstanceArtifact(TemplateInstanceArtifact templateInstanceArtifact) {
    var elementInstances = templateInstanceArtifact.elementInstances();
    var fieldInstances = templateInstanceArtifact.fieldInstances();
    parseElementInstance(elementInstances, "");
    parseFieldInstances(fieldInstances, "");
  }

  @Override
  public void visitElementInstanceArtifact(ElementInstanceArtifact elementInstanceArtifact, String path) {
    var elementInstances = elementInstanceArtifact.elementInstances();
    var fieldInstances = elementInstanceArtifact.fieldInstances();
    parseElementInstance(elementInstances, path);
    parseFieldInstances(fieldInstances, path);
  }

  @Override
  public void visitFieldInstanceArtifact(FieldInstanceArtifact fieldInstanceArtifact, String path) {
    Map<SchemaProperties, Optional<?>> fieldValue = new HashMap<>();
    fieldValue.put(SchemaProperties.VALUE, fieldInstanceArtifact.jsonLdValue());
    fieldValue.put(SchemaProperties.ID, fieldInstanceArtifact.jsonLdId());
    fieldValue.put(SchemaProperties.LABEL, fieldInstanceArtifact.label());

    values.put(path, fieldValue);
  }

  private void parseFieldInstances(Map<String, List<FieldInstanceArtifact>> fieldInstances, String path){
    for (Map.Entry<String, List<FieldInstanceArtifact>> entry : fieldInstances.entrySet()) {
      String fieldName = entry.getKey();
      String childBasePath = path + "/" + fieldName;
      int size = entry.getValue().size();
      cardinals.put(childBasePath, size);
    }
  }

  private void parseElementInstance(Map<String, List<ElementInstanceArtifact>> elementInstances, String path){
    for (Map.Entry<String, List<ElementInstanceArtifact>> entry : elementInstances.entrySet()) {
      String elementName = entry.getKey();
      String childBasePath = "/" + elementName;
      int size = entry.getValue().size();
      cardinals.put(childBasePath, size);
    }
  }
}
