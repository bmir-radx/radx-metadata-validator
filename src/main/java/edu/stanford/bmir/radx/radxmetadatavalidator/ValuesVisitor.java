package edu.stanford.bmir.radx.radxmetadatavalidator;

import org.metadatacenter.artifacts.model.core.ElementInstanceArtifact;
import org.metadatacenter.artifacts.model.core.FieldInstanceArtifact;
import org.metadatacenter.artifacts.model.core.InstanceArtifactVisitor;
import org.metadatacenter.artifacts.model.core.TemplateInstanceArtifact;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ValuesVisitor implements InstanceArtifactVisitor {
  private final Map<String, Map<SchemaProperties, Object>> values;

  public ValuesVisitor() {
    this.values = new HashMap<>();
  }

  public Map<String, Map<SchemaProperties, Object>> getValues() {
    return this.values;
  }

  @Override
  public void visitTemplateInstanceArtifact(TemplateInstanceArtifact templateInstanceArtifact) {

  }

  @Override
  public void visitElementInstanceArtifact(ElementInstanceArtifact elementInstanceArtifact, String path) {
  }

  @Override
  public void visitFieldInstanceArtifact(FieldInstanceArtifact fieldInstanceArtifact, String path) {
    Map<SchemaProperties, Object> fieldValue = new HashMap<>();
    fieldValue.put(SchemaProperties.VALUE, fieldInstanceArtifact.jsonLdValue().orElse(null));
    fieldValue.put(SchemaProperties.ID, fieldInstanceArtifact.jsonLdId().orElse(null));
    fieldValue.put(SchemaProperties.LABEL, fieldInstanceArtifact.label().orElse(null));
    values.put(path, fieldValue);
  }
}
