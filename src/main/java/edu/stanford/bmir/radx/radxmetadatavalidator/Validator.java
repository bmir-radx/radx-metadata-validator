package edu.stanford.bmir.radx.radxmetadatavalidator;

import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.stanford.bmir.radx.radxmetadatavalidator.ValidatorComponents.CedarSchemaValidatorComponent;
import edu.stanford.bmir.radx.radxmetadatavalidator.ValidatorComponents.JsonValidatorComponent;
import edu.stanford.bmir.radx.radxmetadatavalidator.ValidatorComponents.RequiredFieldValidatorComponent;
import edu.stanford.bmir.radx.radxmetadatavalidator.ValidatorComponents.SchemaValidatorComponent;
import org.metadatacenter.artifacts.model.core.TemplateInstanceArtifact;
import org.metadatacenter.artifacts.model.core.TemplateSchemaArtifact;
import org.metadatacenter.artifacts.model.reader.JsonSchemaArtifactReader;
import org.metadatacenter.artifacts.model.visitors.TemplateValueConstraintsReporter;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.Consumer;

@Component
public class Validator {
  private final JsonValidatorComponent jsonValidatorComponent;
  private final SchemaValidatorComponent schemaValidatorComponent;
  private final CedarSchemaValidatorComponent cedarSchemaValidatorComponent;
  private final RequiredFieldValidatorComponent requiredFieldValidatorComponent;

  public Validator(JsonValidatorComponent jsonValidatorComponent,
                   SchemaValidatorComponent schemaValidatorComponent,
                   CedarSchemaValidatorComponent cedarSchemaValidatorComponent,
                   RequiredFieldValidatorComponent requiredFieldValidatorComponent) {
    this.jsonValidatorComponent = jsonValidatorComponent;
    this.schemaValidatorComponent = schemaValidatorComponent;
    this.cedarSchemaValidatorComponent = cedarSchemaValidatorComponent;
    this.requiredFieldValidatorComponent = requiredFieldValidatorComponent;
  }


  public ValidationReport validateInstance(Path templateFilePath, Path instanceFilePath) throws Exception {
    var results = new ArrayList<ValidationResult>();
    Consumer<ValidationResult> consumer = results::add;
    var templateNode = JsonLoader.loadJson(String.valueOf(templateFilePath));
    var instanceNode = JsonLoader.loadJson(String.valueOf(instanceFilePath));

    //Check the instance file is JSON
    jsonValidatorComponent.validate(instanceFilePath, consumer);

    if (templateNode != null && instanceNode != null){
      //Check the template is CEDAR model template
      cedarSchemaValidatorComponent.validate(templateNode, consumer);

      //Compare instance JSON schema against template's
      schemaValidatorComponent.validateAgainstSchema(templateNode, instanceNode, consumer);

      //Read template and get valueConstraints map
      JsonSchemaArtifactReader jsonSchemaArtifactReader = new JsonSchemaArtifactReader();
      //TODO:handle the ArtifactParseException
      TemplateSchemaArtifact templateSchemaArtifact = jsonSchemaArtifactReader.readTemplateSchemaArtifact((ObjectNode) templateNode);
      TemplateValueConstraintsReporter templateValueConstraintsReporter = new TemplateValueConstraintsReporter(templateSchemaArtifact);

      //Read instance and get values map
      TemplateInstanceArtifact templateInstanceArtifact = jsonSchemaArtifactReader.readTemplateInstanceArtifact((ObjectNode) instanceNode);
      TemplateInstanceValuesReporter templateInstanceValuesReporter = new TemplateInstanceValuesReporter(templateInstanceArtifact);
      templateInstanceValuesReporter.printValues(templateInstanceValuesReporter.getValues());
      //Check required fields
      requiredFieldValidatorComponent.validate(templateValueConstraintsReporter, templateInstanceValuesReporter, consumer);
    }

    var comparator = Comparator.comparing(ValidationResult::validationLevel)
        .thenComparing(ValidationResult::validationName)
        .thenComparing(ValidationResult::pointer);

    results.sort(comparator);
    return new ValidationReport(results);
  }
}
