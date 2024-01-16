package edu.stanford.bmir.radx.metadata.validator.lib;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.metadatacenter.artifacts.model.core.TemplateInstanceArtifact;
import org.metadatacenter.artifacts.model.core.TemplateSchemaArtifact;
import org.metadatacenter.artifacts.model.reader.JsonSchemaArtifactReader;
import org.springframework.stereotype.Component;

@Component
public class SchemaArtifactCreator {
  private final JsonSchemaArtifactReader jsonSchemaArtifactReader = new JsonSchemaArtifactReader();
  public TemplateSchemaArtifact createTemplateArtifact(ObjectNode objectNode){
    return jsonSchemaArtifactReader.readTemplateSchemaArtifact(objectNode);
  }

  public TemplateInstanceArtifact createTemplateInstanceArtifact(ObjectNode objectNode){
    return jsonSchemaArtifactReader.readTemplateInstanceArtifact(objectNode);
  }
}
