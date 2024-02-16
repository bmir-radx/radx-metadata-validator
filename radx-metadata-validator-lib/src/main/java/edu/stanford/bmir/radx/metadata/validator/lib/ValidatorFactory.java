package edu.stanford.bmir.radx.metadata.validator.lib;

import edu.stanford.bmir.radx.metadata.validator.lib.validators.*;
import org.springframework.stereotype.Component;

@Component
public class ValidatorFactory {
  private final SchemaValidatorComponent schemaValidatorComponent;
  private final CedarSchemaValidatorComponent cedarSchemaValidatorComponent;
  private final RequiredFieldValidatorComponent requiredFieldValidatorComponent;
  private final DataTypeValidatorComponent dataTypeValidatorComponent;
  private final MetadataCardinalityValidatorComponent metadataCardinalityValidatorComponent;
  private final RadxPrecisionValidatorComponent radxPrecisionValidatorComponent;
  private final SanitationChecker sanitationChecker;

  public ValidatorFactory(SchemaValidatorComponent schemaValidatorComponent,
                          CedarSchemaValidatorComponent cedarSchemaValidatorComponent,
                          RequiredFieldValidatorComponent requiredFieldValidatorComponent,
                          DataTypeValidatorComponent dataTypeValidatorComponent,
                          MetadataCardinalityValidatorComponent metadataCardinalityValidatorComponent,
                          RadxPrecisionValidatorComponent radxPrecisionValidatorComponent, SanitationChecker sanitationChecker) {
    this.schemaValidatorComponent = schemaValidatorComponent;
    this.cedarSchemaValidatorComponent = cedarSchemaValidatorComponent;
    this.requiredFieldValidatorComponent = requiredFieldValidatorComponent;
    this.dataTypeValidatorComponent = dataTypeValidatorComponent;
    this.metadataCardinalityValidatorComponent = metadataCardinalityValidatorComponent;
    this.radxPrecisionValidatorComponent = radxPrecisionValidatorComponent;
    this.sanitationChecker = sanitationChecker;
  }

  public Validator createValidator(LiteralFieldValidators literalFieldValidators){
    return  new Validator(schemaValidatorComponent,
        cedarSchemaValidatorComponent,
        requiredFieldValidatorComponent,
        dataTypeValidatorComponent,
        metadataCardinalityValidatorComponent,
        radxPrecisionValidatorComponent,
        sanitationChecker,
        literalFieldValidators);
  }
}
