package edu.stanford.bmir.radx.metadata.validator.lib;

import org.metadatacenter.artifacts.model.core.ParentSchemaArtifact;
import org.metadatacenter.artifacts.model.core.TemplateSchemaArtifact;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class FieldsCollector {
  public List<String> getAllFields(TemplateSchemaArtifact templateSchemaArtifact){
    return getFieldsFromParentArtifact(templateSchemaArtifact, "");
  }

  public List<String> getFieldsFromParentArtifact(ParentSchemaArtifact parentSchemaArtifact, String parentElementName){
    var fields = parentSchemaArtifact.getFieldNames();
    var fieldPath = fields.stream()
        .map(p-> parentElementName + "/" + p)
        .collect(Collectors.toCollection(ArrayList::new));
    var childElements = parentSchemaArtifact.getElementNames();
    for(var childElement: childElements){
      var childElementSchema = parentSchemaArtifact.getElementSchemaArtifact(childElement);
      fieldPath.addAll(getFieldsFromParentArtifact(childElementSchema, "/" + childElement));
    }
    return fieldPath;
  }
}
