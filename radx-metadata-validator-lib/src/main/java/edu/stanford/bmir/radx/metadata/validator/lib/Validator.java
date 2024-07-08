package edu.stanford.bmir.radx.metadata.validator.lib;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import edu.stanford.bmir.radx.metadata.validator.lib.validators.*;
import org.metadatacenter.artifacts.model.core.TemplateInstanceArtifact;
import org.metadatacenter.artifacts.model.core.TemplateSchemaArtifact;
import org.metadatacenter.artifacts.model.reader.ArtifactParseException;
import org.metadatacenter.artifacts.model.reader.JsonSchemaArtifactReader;
import org.metadatacenter.artifacts.model.visitors.TemplateReporter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.function.Consumer;

public class Validator {
  private final SchemaValidatorComponent schemaValidatorComponent;
  private final CedarSchemaValidatorComponent cedarSchemaValidatorComponent;
  private final RequiredFieldValidatorComponent requiredFieldValidatorComponent;
  private final DataTypeValidatorComponent dataTypeValidatorComponent;
  private final CardinalityValidatorComponent cardinalityValidatorComponent;
  private final RadxPrecisionValidatorComponent radxPrecisionValidatorComponent;
  private final SanitationChecker sanitationChecker;
  private final LiteralFieldValidators literalFieldValidators;
  private final ControlledTermValidatorComponent controlledTermValidatorComponent;
  private final TerminologyServerHandler terminologyServerHandler;


  public Validator(SchemaValidatorComponent schemaValidatorComponent,
                   CedarSchemaValidatorComponent cedarSchemaValidatorComponent,
                   RequiredFieldValidatorComponent requiredFieldValidatorComponent, DataTypeValidatorComponent dataTypeValidatorComponent, CardinalityValidatorComponent cardinalityValidatorComponent, RadxPrecisionValidatorComponent radxPrecisionValidatorComponent, SanitationChecker sanitationChecker, LiteralFieldValidators literalFieldValidators, ControlledTermValidatorComponent controlledTermValidatorComponent, TerminologyServerHandler terminologyServerHandler) {
    this.schemaValidatorComponent = schemaValidatorComponent;
    this.cedarSchemaValidatorComponent = cedarSchemaValidatorComponent;
    this.requiredFieldValidatorComponent = requiredFieldValidatorComponent;
    this.dataTypeValidatorComponent = dataTypeValidatorComponent;
    this.cardinalityValidatorComponent = cardinalityValidatorComponent;
    this.radxPrecisionValidatorComponent = radxPrecisionValidatorComponent;
    this.sanitationChecker = sanitationChecker;
    this.literalFieldValidators = literalFieldValidators;
    this.controlledTermValidatorComponent = controlledTermValidatorComponent;
    this.terminologyServerHandler = terminologyServerHandler;
  }


  public ValidationReport validateInstance(String templateContent, String instanceContent) throws Exception {
    var results = new HashSet<ValidationResult>();
    Consumer<ValidationResult> consumer = results::add;

    try{
      //validate the provided files are JSON file and get the templateNode and instanceNode
      var templateNode = JsonLoader.loadJson(templateContent, "Template");
      var instanceNode = JsonLoader.loadJson(instanceContent, "Instance");

      //validate the template is CEDAR model template
      cedarSchemaValidatorComponent.validate(templateNode, consumer);

      if(passValidation(results)){
        //Read template and get valueConstraints map
        JsonSchemaArtifactReader jsonSchemaArtifactReader = new JsonSchemaArtifactReader();
        TemplateSchemaArtifact templateSchemaArtifact = jsonSchemaArtifactReader.readTemplateSchemaArtifact((ObjectNode) templateNode);
        TemplateReporter templateReporter = new TemplateReporter(templateSchemaArtifact);

        //Read instance and get values map
        TemplateInstanceArtifact templateInstanceArtifact = jsonSchemaArtifactReader.readTemplateInstanceArtifact((ObjectNode) instanceNode);
        TemplateInstanceValuesReporter templateInstanceValuesReporter = new TemplateInstanceValuesReporter(templateInstanceArtifact);

        // Check if the instance's "isBasedOn" equals to template id
        sanitationChecker.validate(templateSchemaArtifact, templateInstanceArtifact, consumer);

        //Compare instance JSON schema against template's
        schemaValidatorComponent.validate(templateNode, instanceNode, consumer);

        if(passValidation(results)){
          //validate required fields
          requiredFieldValidatorComponent.validate(templateReporter, templateInstanceValuesReporter, consumer);

          //validate data type
          dataTypeValidatorComponent.validate(templateReporter, templateInstanceValuesReporter, consumer);

          //validate cardinality
          cardinalityValidatorComponent.validate(templateReporter, templateInstanceValuesReporter, consumer);

          //RADx specific validation
          radxPrecisionValidatorComponent.validate(literalFieldValidators, templateInstanceValuesReporter, consumer);

          //Controlled term validation
          controlledTermValidatorComponent.validate(terminologyServerHandler, templateReporter, templateInstanceValuesReporter, consumer);

        }
      }
    } catch (JsonParseException e) {
      consumer.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.JSON_VALIDATION, e.getMessage(), ""));
    } catch (ArtifactParseException e) {
      String errorMessage = e.getMessage();
      String pointer = e.getPath();
      consumer.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.ARTIFACT_SCHEMA_VALIDATION, errorMessage, pointer));
    } catch (ProcessingException e){
      consumer.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.SCHEMA_VALIDATION, e.getMessage(), ""));
    } catch (Exception e){
      consumer.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.UNKNOWN, e.getMessage(), ""));
      for (StackTraceElement element : e.getStackTrace()) {
        System.out.println(element.toString());
      }
    }

    var comparator = Comparator.comparing(ValidationResult::validationLevel)
        .thenComparing(ValidationResult::pointer)
        .thenComparing(ValidationResult::validationName);

    var resultsList = new ArrayList<>(results);
    resultsList.sort(comparator);
    return new ValidationReport(resultsList);
  }

  private boolean passValidation(HashSet<ValidationResult> results){
    for(var result : results){
      if (result.validationLevel() == ValidationLevel.ERROR){
        return false;
      }
    }
    return true;
  }
}
