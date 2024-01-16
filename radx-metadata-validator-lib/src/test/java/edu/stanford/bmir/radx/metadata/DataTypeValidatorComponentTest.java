package edu.stanford.bmir.radx.metadata;

import edu.stanford.bmir.radx.metadata.validator.lib.*;
import edu.stanford.bmir.radx.metadata.validator.lib.ValidatorComponents.DataTypeValidatorComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.metadatacenter.artifacts.model.core.FieldSchemaArtifact;
import org.metadatacenter.artifacts.model.core.TemplateSchemaArtifact;
import org.metadatacenter.artifacts.model.core.fields.XsdNumericDatatype;
import org.metadatacenter.artifacts.model.core.fields.XsdTemporalDatatype;
import org.metadatacenter.artifacts.model.core.fields.constraints.NumericValueConstraints;
import org.metadatacenter.artifacts.model.core.fields.constraints.TemporalValueConstraints;
import org.metadatacenter.artifacts.model.core.fields.constraints.TextValueConstraints;
import org.metadatacenter.artifacts.model.core.fields.constraints.ValueConstraints;
import org.metadatacenter.artifacts.model.visitors.TemplateReporter;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class DataTypeValidatorComponentTest {
  private DataTypeValidatorComponent validator;
  private List<ValidationResult> results;
  private Consumer<ValidationResult> consumer;
  @Mock
  private TemplateInstanceValuesReporter valuesReporter;

  @BeforeEach
  void setup() {
    MockitoAnnotations.openMocks(this);
    results = new ArrayList<>();
    consumer = results::add;
    TextFieldValidationUtil textFieldValidationUtil = new TextFieldValidationUtil();
    NumericFieldValidationUtil numericFieldValidationUtil = new NumericFieldValidationUtil();
    FieldSchemaValidationHelper fieldSchemaValidationHelper = new FieldSchemaValidationHelper();
    validator = new DataTypeValidatorComponent(textFieldValidationUtil, numericFieldValidationUtil, fieldSchemaValidationHelper);
  }

  @Test
  void testValidateTextFieldWithRegexMismatch(){
    var value = "2345";
    TextValueConstraints textValueConstraints = TextValueConstraints.builder()
        .withRegex("^\\(?([0-9]{3})\\)?[-. ]?([0-9]{3})[-. ]?([0-9]{4})$")
        .build().asTextValueConstraints();
    validator.validateTextField(value, textValueConstraints, consumer, "");
    assertEquals(1, results.size());
  }

  @Test
  void testValidateTextFieldWithRegexMatch(){
    var value = "1234567890";
    ValueConstraints textValueConstraints = TextValueConstraints.builder()
        .withRegex("^\\(?([0-9]{3})\\)?[-. ]?([0-9]{3})[-. ]?([0-9]{4})$")
        .build();
    validator.validateTextField(value, textValueConstraints, consumer, "");
    assertEquals(0, results.size());
  }

  @Test
  void testValidateLiteralsWithInvalidLiteral(){
    var value = "radx";
    TextValueConstraints textValueConstraints = TextValueConstraints.builder()
        .withChoice("cedar", false)
        .withChoice("bioportal", false)
        .withChoice("protege", false)
        .build().asTextValueConstraints();

    validator.validateLiterals(value, textValueConstraints, consumer, "");
    assertEquals(1, results.size());
  }

  @Test
  void testValidateLiteralsWithValidLiteral(){
    var value = "cedar";
    TextValueConstraints textValueConstraints = TextValueConstraints.builder()
        .withChoice("cedar", false)
        .withChoice("bioportal", false)
        .withChoice("protege", false)
        .build().asTextValueConstraints();

    validator.validateLiterals(value, textValueConstraints, consumer, "");
    assertEquals(0, results.size());
  }

  @Test
  void testValidateTextFieldWithLengthInRange() {
    var value = "2345";
    TextValueConstraints textValueConstraints = TextValueConstraints.builder()
        .withMinLength(1)
        .build().asTextValueConstraints();
    validator.validateTextField(value, textValueConstraints, consumer, "");
    assertEquals(0, results.size());
  }

  @Test
  void testValidateTextFieldWithLengthOutOfRange() {
    var value = "2345";
    TextValueConstraints textValueConstraints = TextValueConstraints.builder()
        .withMinLength(5)
        .build().asTextValueConstraints();
    validator.validateTextField(value, textValueConstraints, consumer, "");
    assertEquals(1, results.size());
  }

  @Test
  void testValidateTextFieldWithNonStringInput() {
    var value = 123;
    TextValueConstraints textValueConstraints = TextValueConstraints.builder()
        .build().asTextValueConstraints();
    validator.validateTextField(value, textValueConstraints, consumer, "");
    assertEquals(1, results.size());
  }

  @Test
  void testValidateNumericFieldWithInvalidNumber(){
    var value = "3sr3";
    var type = Optional.of("xsd:decimal");
    XsdNumericDatatype numberType = XsdNumericDatatype.DECIMAL;
    NumericValueConstraints numericValueConstraints = NumericValueConstraints.builder()
        .withNumberType(numberType)
        .build();
    validator.validateNumericField(value, type, numericValueConstraints, consumer, "");
    assertEquals(1, results.size());
  }

  @Test
  void testValidateNumericFieldWithIncorrectType(){
    var value = "50.2";
    var type = Optional.of("xsd:decimal");
    XsdNumericDatatype numberType = XsdNumericDatatype.DOUBLE;
    NumericValueConstraints numericValueConstraints = NumericValueConstraints.builder()
        .withNumberType(numberType)
        .build();
    validator.validateNumericField(value, type, numericValueConstraints, consumer, "");
    assertEquals(1, results.size());
  }

  @Test
  void testValidateNumericFieldOutOfRange(){
    var value = "-50.27";
    var type = Optional.of("xsd:decimal");

    XsdNumericDatatype numberType = XsdNumericDatatype.DECIMAL;
    Number minValue = 0;
    Number maxValue = 100;

    NumericValueConstraints numericValueConstraints = NumericValueConstraints.builder()
        .withNumberType(numberType)
        .withMinValue(minValue)
        .withMaxValue(maxValue)
        .build();
    validator.validateNumericField(value, type, numericValueConstraints, consumer, "");
    assertEquals(1, results.size());
  }

  @Test
  void testValidateNumericMissingType(){
    var value = "-50.27";
    XsdNumericDatatype numberType = XsdNumericDatatype.DECIMAL;
    NumericValueConstraints numericValueConstraints = NumericValueConstraints.builder()
        .withNumberType(numberType)
        .build();
    validator.validateNumericField(value, Optional.empty(), numericValueConstraints, consumer, "");
    assertEquals(1, results.size());
  }

  @Test
  void testValidateDoubleNumericField(){
    var value = "-50.27";
    var type = Optional.of("xsd:decimal");

    boolean requiredValue = false;
    boolean multipleChoice= false;
    XsdNumericDatatype numberType = XsdNumericDatatype.DOUBLE;
    String unitOfMeasure = "mm";
    Number minValue = 0;
    Number maxValue = 100;
    Integer decimalPlaces = 1;

    NumericValueConstraints numericValueConstraints = NumericValueConstraints.builder()
        .withRequiredValue(requiredValue)
        .withMultipleChoice(multipleChoice)
        .withNumberType(numberType)
        .withUnitOfMeasure(unitOfMeasure)
        .withMinValue(minValue)
        .withMaxValue(maxValue)
        .withDecimalPlaces(decimalPlaces)
        .build();

    validator.validateNumericField(value, type, numericValueConstraints, consumer, "");
//    assertThrows(JsonParseException.class, () -> validator.validateNumericField(value, numericValueConstraints, consumer, ""));
    assertEquals(3, results.size());
  }

  @Test
  void testValidateDecimalNumericField(){
    var value = "3sf3";
    var type = Optional.of("xsd:decimal");

    boolean requiredValue = false;
    boolean multipleChoice= false;
    XsdNumericDatatype numberType = XsdNumericDatatype.DECIMAL;
    String unitOfMeasure = "mm";
    Number minValue = 0;
    Number maxValue = 100;
    Integer decimalPlaces = 1;

    NumericValueConstraints numericValueConstraints = NumericValueConstraints.builder()
        .withRequiredValue(requiredValue)
        .withMultipleChoice(multipleChoice)
        .withNumberType(numberType)
        .withUnitOfMeasure(unitOfMeasure)
        .withMinValue(minValue)
        .withMaxValue(maxValue)
        .withDecimalPlaces(decimalPlaces)
        .build();

    validator.validateNumericField(value, type, numericValueConstraints, consumer, "");
    assertEquals(1, results.size());
  }

  @Test
  void testValidateIntegerNumericField(){
    var value = "-50.27";
    var type = Optional.of("xsd:int");
    boolean requiredValue = false;
    boolean multipleChoice= false;
    XsdNumericDatatype numberType = XsdNumericDatatype.INTEGER;
    String unitOfMeasure = "mm";
    Number minValue = 0;
    Number maxValue = 100;

    NumericValueConstraints numericValueConstraints = NumericValueConstraints.builder()
        .withRequiredValue(requiredValue)
        .withMultipleChoice(multipleChoice)
        .withNumberType(numberType)
        .withUnitOfMeasure(unitOfMeasure)
        .withMinValue(minValue)
        .withMaxValue(maxValue)
        .build();

    validator.validateNumericField(value, type, numericValueConstraints, consumer, "");;
    assertEquals(2, results.size());
  }

  @Test
  void testValidateValidNumericField(){
    var value = "50.27";
    var type = Optional.of("xsd:decimal");
    boolean requiredValue = false;
    boolean multipleChoice= false;
    XsdNumericDatatype numberType = XsdNumericDatatype.DECIMAL;
    String unitOfMeasure = "mm";
    Number minValue = 0;
    Number maxValue = 100;
    int decimalPlace = 2;

    NumericValueConstraints numericValueConstraints = NumericValueConstraints.builder()
        .withRequiredValue(requiredValue)
        .withMultipleChoice(multipleChoice)
        .withNumberType(numberType)
        .withUnitOfMeasure(unitOfMeasure)
        .withMinValue(minValue)
        .withMaxValue(maxValue)
        .withDecimalPlaces(decimalPlace)
        .build();

    validator.validateNumericField(value, type, numericValueConstraints, consumer, "");
    assertEquals(0, results.size());
  }


  @Test
  void testValidateInvalidTimeField(){
    var value = "07:00:00";
    var type = Optional.of("xsd:time");
    TemporalValueConstraints temporalValueConstraints = TemporalValueConstraints.builder()
        .withTemporalType(XsdTemporalDatatype.TIME)
        .build();
    validator.validateTemporalField(value, type, temporalValueConstraints, consumer, "");
    assertEquals(1, results.size());
  }

  @Test
  void testValidateValidTimeField(){
    var value = "01:00:00-11:00";
    var type = Optional.of("xsd:time");
    TemporalValueConstraints temporalValueConstraints = TemporalValueConstraints.builder()
        .withTemporalType(XsdTemporalDatatype.TIME)
        .build();
    validator.validateTemporalField(value, type, temporalValueConstraints, consumer, "");
    assertEquals(0, results.size());
  }

  @Test
  void testValidateInvalidDateField(){
    var value = "2024/01/01";
    var type = Optional.of("xsd:date");
    TemporalValueConstraints temporalValueConstraints = TemporalValueConstraints.builder()
        .withTemporalType(XsdTemporalDatatype.DATE)
        .build();
    validator.validateTemporalField(value, type, temporalValueConstraints, consumer, "");
    assertEquals(1, results.size());
  }

  @Test
  void testValidateValidDateField(){
    var value = "2024-01-01";
    var type = Optional.of("xsd:date");
    TemporalValueConstraints temporalValueConstraints = TemporalValueConstraints.builder()
        .withTemporalType(XsdTemporalDatatype.DATE)
        .build();
    validator.validateTemporalField(value, type, temporalValueConstraints, consumer, "");
    assertEquals(0, results.size());
  }

  @Test
  void testValidateInvalidDateTimeField(){
    var value = "2024-01-02T15:27:29";
    var type = Optional.of("xsd:dateTime");
    TemporalValueConstraints temporalValueConstraints = TemporalValueConstraints.builder()
        .withTemporalType(XsdTemporalDatatype.DATETIME)
        .build();
    validator.validateTemporalField(value, type, temporalValueConstraints, consumer, "");
    assertEquals(1, results.size());
  }

  @Test
  void testValidateValidDateTimeField(){
    var value = "2024-01-02T15:27:29-08:00";
    var type = Optional.of("xsd:dateTime");
    TemporalValueConstraints temporalValueConstraints = TemporalValueConstraints.builder()
        .withTemporalType(XsdTemporalDatatype.DATETIME)
        .build();
    validator.validateTemporalField(value, type, temporalValueConstraints, consumer, "");
    assertEquals(0, results.size());
  }

  @Test
  void testValidateTemporalFieldMissingType(){
    var value = "2024-01-02T15:27:29-08:00";
    TemporalValueConstraints temporalValueConstraints = TemporalValueConstraints.builder()
        .withTemporalType(XsdTemporalDatatype.DATETIME)
        .build();
    validator.validateTemporalField(value, Optional.empty(), temporalValueConstraints, consumer, "");
    assertEquals(1, results.size());
  }

  @Test
  void testValidateLinkFieldWithInvalidInput(){
    var id = "www.abc.com";
    validator.validateLinkField(id, consumer, "");
    assertEquals(1, results.size());
  }

  @Test
  void testValidateLinkFieldWithValidInput(){
    var id = "https://www.abc.com";
    validator.validateLinkField(id, consumer, "");
    assertEquals(0, results.size());
  }

  @Test
  void testValidateEmailFieldWithValidEmail() {
    String validEmail = "test@example.com";
    validator.validateEmailField(validEmail, consumer, "emailPath");
    assertEquals(0, results.size());
  }

  @Test
  void testValidateEmailFieldWithInvalidEmail() {
    String validEmail = "test_example.com";
    validator.validateEmailField(validEmail, consumer, "emailPath");
    assertEquals(1, results.size());
  }


  @Test
  void testValidateWithTextField(){
    String fieldName = "text field";
    String templateName = "My template";
    FieldSchemaArtifact textFieldSchemaArtifact = FieldSchemaArtifact.textFieldBuilder()
        .withName(fieldName)
        .withRequiredValue(true)
        .withRegex("^\\(?([0-9]{3})\\)?[-. ]?([0-9]{3})[-. ]?([0-9]{4})$")
        .withMinLength(2)
        .build();

    TemplateSchemaArtifact templateSchemaArtifact = TemplateSchemaArtifact.builder()
        .withName(templateName)
        .withFieldSchema(textFieldSchemaArtifact)
        .build();

    var valueConstraintsReporter = new TemplateReporter(templateSchemaArtifact);

    String fieldPath = "/" + fieldName;
    String textFieldValue = "1234567890";
    Map<String, Map<SchemaProperties, Optional<?>>> values = new HashMap<>();
    Map<SchemaProperties, Optional<?>> fieldValue = new HashMap<>();
    fieldValue.put(SchemaProperties.VALUE, Optional.of(textFieldValue));
    fieldValue.put(SchemaProperties.ID, Optional.empty());
    fieldValue.put(SchemaProperties.LABEL, Optional.empty());
    fieldValue.put(SchemaProperties.TYPE, Optional.empty());
    values.put(fieldPath, fieldValue);

    when(valuesReporter.getValues()).thenReturn(values);

    validator.validate(valueConstraintsReporter, valuesReporter, consumer);
    assertEquals(0, results.size());
  }

  @Test
  void testValidateWithNumericField(){
    String fieldName = "numeric field";
    String templateName = "My template";
    FieldSchemaArtifact textFieldSchemaArtifact = FieldSchemaArtifact.numericFieldBuilder()
        .withName(fieldName)
        .withRequiredValue(true)
        .withDecimalPlaces(2)
        .withNumericType(XsdNumericDatatype.DECIMAL)
        .withMinValue(0)
        .withMaxValue(100)
        .build();

    TemplateSchemaArtifact templateSchemaArtifact = TemplateSchemaArtifact.builder()
        .withName(templateName)
        .withFieldSchema(textFieldSchemaArtifact)
        .build();

    var valueConstraintsReporter = new TemplateReporter(templateSchemaArtifact);

    String fieldPath = "/" + fieldName;
    String numericFieldValue = "50.23";
    Map<String, Map<SchemaProperties, Optional<?>>> values = new HashMap<>();
    Map<SchemaProperties, Optional<?>> fieldValue = new HashMap<>();
    fieldValue.put(SchemaProperties.VALUE, Optional.of(numericFieldValue));
    fieldValue.put(SchemaProperties.ID, Optional.empty());
    fieldValue.put(SchemaProperties.LABEL, Optional.empty());
    fieldValue.put(SchemaProperties.TYPE, Optional.of("xsd:decimal"));
    values.put(fieldPath, fieldValue);

    when(valuesReporter.getValues()).thenReturn(values);

    validator.validate(valueConstraintsReporter, valuesReporter, consumer);
    assertEquals(0, results.size());
  }


}
