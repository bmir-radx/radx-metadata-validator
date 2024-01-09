package edu.stanford.bmir.radx.radxmetadatavalidator;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import edu.stanford.bmir.radx.radxmetadatavalidator.ValidatorComponents.*;
import org.metadatacenter.artifacts.model.core.TemplateInstanceArtifact;
import org.metadatacenter.artifacts.model.core.TemplateSchemaArtifact;
import org.metadatacenter.artifacts.model.reader.ArtifactParseException;
import org.metadatacenter.artifacts.model.reader.JsonSchemaArtifactReader;
import org.metadatacenter.artifacts.model.visitors.TemplateReporter;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.Consumer;

@Component
public class Validator {
  private final SchemaValidatorComponent schemaValidatorComponent;
  private final CedarSchemaValidatorComponent cedarSchemaValidatorComponent;
  private final RequiredFieldValidatorComponent requiredFieldValidatorComponent;
  private final DataTypeValidatorComponent dataTypeValidatorComponent;

  public Validator(SchemaValidatorComponent schemaValidatorComponent,
                   CedarSchemaValidatorComponent cedarSchemaValidatorComponent,
                   RequiredFieldValidatorComponent requiredFieldValidatorComponent, DataTypeValidatorComponent dataTypeValidatorComponent) {
    this.schemaValidatorComponent = schemaValidatorComponent;
    this.cedarSchemaValidatorComponent = cedarSchemaValidatorComponent;
    this.requiredFieldValidatorComponent = requiredFieldValidatorComponent;
    this.dataTypeValidatorComponent = dataTypeValidatorComponent;
  }


  public ValidationReport validateInstance(Path templateFilePath, Path instanceFilePath) throws Exception {
    var results = new ArrayList<ValidationResult>();
    Consumer<ValidationResult> consumer = results::add;

    try{
      //validate the provided files are JSON file and get the templateNode and instanceNode
      var templateNode = JsonLoader.loadJson(String.valueOf(templateFilePath));
      var instanceNode = JsonLoader.loadJson(String.valueOf(instanceFilePath));

      //validate the template is CEDAR model template
      cedarSchemaValidatorComponent.validate(templateNode, consumer);

      if(results.isEmpty()){
        //Read template and get valueConstraints map
        JsonSchemaArtifactReader jsonSchemaArtifactReader = new JsonSchemaArtifactReader();
        TemplateSchemaArtifact templateSchemaArtifact = jsonSchemaArtifactReader.readTemplateSchemaArtifact((ObjectNode) templateNode);
        TemplateReporter templateValueConstraintsReporter = new TemplateReporter(templateSchemaArtifact);

        //Read instance and get values map
        TemplateInstanceArtifact templateInstanceArtifact = jsonSchemaArtifactReader.readTemplateInstanceArtifact((ObjectNode) instanceNode);
        TemplateInstanceValuesReporter templateInstanceValuesReporter = new TemplateInstanceValuesReporter(templateInstanceArtifact);


        //TODO: validate the schema:isBasedOn of the instance matches template ID?
        var templateID = templateSchemaArtifact.jsonLdId();
        var instanceID = templateInstanceArtifact.isBasedOn();

        //Compare instance JSON schema against template's
        schemaValidatorComponent.validateAgainstSchema(templateNode, instanceNode, consumer);

        if(results.isEmpty()){
          //validate required fields
          requiredFieldValidatorComponent.validate(templateValueConstraintsReporter, templateInstanceValuesReporter, consumer);

          //validate data type
          dataTypeValidatorComponent.validate(templateValueConstraintsReporter, templateInstanceValuesReporter, consumer);
        }

      }
    } catch (JsonParseException e) {
      consumer.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.JSON_VALIDATION, e.getMessage(), ""));
    } catch (ArtifactParseException e) {
      String errorMessage = e.getMessage();
      String pointer = e.getPath();
      //TODO: modify the validation name?
      consumer.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.ARTIFACT_SCHEMA_VALIDATION, errorMessage, pointer));
    } catch (ProcessingException e){
      consumer.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.SCHEMA_VALIDATION, e.getMessage(), ""));
    } catch (Exception e){
      for (StackTraceElement element : e.getStackTrace()) {
        System.out.println(element.toString());
      }
    }

    var comparator = Comparator.comparing(ValidationResult::validationLevel)
        .thenComparing(ValidationResult::validationName)
        .thenComparing(ValidationResult::pointer);

    results.sort(comparator);
    return new ValidationReport(results);
  }
}
