package edu.stanford.bmir.radx.metadata.validator.lib;

import edu.stanford.bmir.radx.metadata.validator.lib.validators.*;
import org.springframework.stereotype.Component;

@Component
public class ValidatorFactory {
  private final SchemaValidatorComponent schemaValidatorComponent;
  private final CedarSchemaValidatorComponent cedarSchemaValidatorComponent;
  private final RequiredFieldValidatorComponent requiredFieldValidatorComponent;
  private final DataTypeValidatorComponent dataTypeValidatorComponent;
  private final CardinalityValidatorComponent cardinalityValidatorComponent;
  private final SanitationChecker sanitationChecker;

  public ValidatorFactory(SchemaValidatorComponent schemaValidatorComponent,
                          CedarSchemaValidatorComponent cedarSchemaValidatorComponent,
                          RequiredFieldValidatorComponent requiredFieldValidatorComponent,
                          DataTypeValidatorComponent dataTypeValidatorComponent,
                          CardinalityValidatorComponent cardinalityValidatorComponent,
                          SanitationChecker sanitationChecker) {
    this.schemaValidatorComponent = schemaValidatorComponent;
    this.cedarSchemaValidatorComponent = cedarSchemaValidatorComponent;
    this.requiredFieldValidatorComponent = requiredFieldValidatorComponent;
    this.dataTypeValidatorComponent = dataTypeValidatorComponent;
    this.cardinalityValidatorComponent = cardinalityValidatorComponent;
    this.sanitationChecker = sanitationChecker;
  }

  public Validator createValidator(LiteralFieldValidatorsComponent literalFieldValidatorsComponent){
    return  new Validator(schemaValidatorComponent,
        cedarSchemaValidatorComponent,
        requiredFieldValidatorComponent,
        dataTypeValidatorComponent,
        cardinalityValidatorComponent,
        sanitationChecker,
        literalFieldValidatorsComponent);
  }
}
