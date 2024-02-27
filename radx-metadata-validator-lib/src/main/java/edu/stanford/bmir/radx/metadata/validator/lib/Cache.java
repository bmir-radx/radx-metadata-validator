package edu.stanford.bmir.radx.metadata.validator.lib;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.metadatacenter.artifacts.model.core.fields.constraints.ValueConstraints;
import org.metadatacenter.artifacts.model.reader.JsonSchemaArtifactReader;
import org.metadatacenter.artifacts.model.visitors.TemplateReporter;

import java.util.HashMap;
import java.util.Map;

/***
 * This Cache class is used for multiple instances validation against one template.
 * All controlled term values in the template will be stored in a Map<ValueConstraints, Map<String, String>>.
 * The inner map is URI, prefLabel pair.
 */
public class Cache {
  private static final Map<ValueConstraints, Map<String, String>> cache = new HashMap<>();
  public static void init(String templateContent, String instanceContent, TerminologyServerHandler terminologyServerHandler){
    if (terminologyServerHandler.getTerminologyServerEndPoint() != null && terminologyServerHandler.getTerminologyServerAPIKey() != null) {
      var templateNode = JsonLoader.loadJson(templateContent, "Template");
      var instanceNode = JsonLoader.loadJson(instanceContent, "Instance");

      //Read template and get valueConstraints map
      var jsonSchemaArtifactReader = new JsonSchemaArtifactReader();
      var templateSchemaArtifact = jsonSchemaArtifactReader.readTemplateSchemaArtifact((ObjectNode) templateNode);
      var templateReporter = new TemplateReporter(templateSchemaArtifact);

      //Read instance and get values map
      var templateInstanceArtifact = jsonSchemaArtifactReader.readTemplateInstanceArtifact((ObjectNode) instanceNode);
      var values = new TemplateInstanceValuesReporter(templateInstanceArtifact).getValues();

      for (Map.Entry<String, FieldValues> fieldEntry : values.entrySet()) {
        String path = fieldEntry.getKey();
        var valueConstraint = templateReporter.getValueConstraints(path);
        if (valueConstraint.isPresent() && valueConstraint.get().isControlledTermValueConstraint()) {
          var valueConstraints = valueConstraint.get().asControlledTermValueConstraints();
          cache.put(valueConstraints, terminologyServerHandler.getAllValues(valueConstraints));
        }
      }
    }
  }
  public static Map<ValueConstraints, Map<String, String>> getCache(){
    return cache;
  }

  public static boolean isCached(ValueConstraints valueConstraints){
    return cache.containsKey(valueConstraints);
  }

  public static Map<String, String> getCachePerValueConstraint(ValueConstraints valueConstraints){
    return cache.get(valueConstraints);
  }
}
