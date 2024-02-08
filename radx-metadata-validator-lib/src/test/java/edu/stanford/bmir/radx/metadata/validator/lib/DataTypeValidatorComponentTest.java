package edu.stanford.bmir.radx.metadata.validator.lib;

import edu.stanford.bmir.radx.metadata.validator.lib.validators.DataTypeValidatorComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.metadatacenter.artifacts.model.core.FieldSchemaArtifact;
import org.metadatacenter.artifacts.model.core.TemplateSchemaArtifact;
import org.metadatacenter.artifacts.model.core.fields.InputTimeFormat;
import org.metadatacenter.artifacts.model.core.fields.TemporalGranularity;
import org.metadatacenter.artifacts.model.core.fields.XsdNumericDatatype;
import org.metadatacenter.artifacts.model.core.fields.XsdTemporalDatatype;
import org.metadatacenter.artifacts.model.core.fields.constraints.NumericValueConstraints;
import org.metadatacenter.artifacts.model.core.fields.constraints.TemporalValueConstraints;
import org.metadatacenter.artifacts.model.core.fields.constraints.TextValueConstraints;
import org.metadatacenter.artifacts.model.core.fields.constraints.ValueConstraints;
import org.metadatacenter.artifacts.model.visitors.TemplateReporter;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.net.URI;
import java.net.URISyntaxException;
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
    AttributeValueValidationUtil attributeValueValidationUtil = new AttributeValueValidationUtil();
    FieldSchemaValidationHelper fieldSchemaValidationHelper = new FieldSchemaValidationHelper();
    validator = new DataTypeValidatorComponent(textFieldValidationUtil, numericFieldValidationUtil, attributeValueValidationUtil, fieldSchemaValidationHelper);
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
  void testValidateNumericFieldWithInvalidNumber() throws URISyntaxException {
    var value = "3sr3";
    var type = List.of(new URI("xsd:decimal"));
    XsdNumericDatatype numberType = XsdNumericDatatype.DECIMAL;
    NumericValueConstraints numericValueConstraints = NumericValueConstraints.builder()
        .withNumberType(numberType)
        .build();
    validator.validateNumericField(value, type, numericValueConstraints, consumer, "");
    assertEquals(1, results.size());
  }

  @Test
  void testValidateNumericFieldWithIncorrectType() throws URISyntaxException {
    var value = "50.2";
    var type = List.of(new URI("xsd:decimal"));
    XsdNumericDatatype numberType = XsdNumericDatatype.DOUBLE;
    NumericValueConstraints numericValueConstraints = NumericValueConstraints.builder()
        .withNumberType(numberType)
        .build();
    validator.validateNumericField(value, type, numericValueConstraints, consumer, "");
    assertEquals(1, results.size());
  }

  @Test
  void testValidateNumericFieldOutOfRange() throws URISyntaxException {
    var value = "-50.27";
    var type = List.of(new URI("xsd:decimal"));

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
    validator.validateNumericField(value, List.of(), numericValueConstraints, consumer, "");
    assertEquals(1, results.size());
  }

  @Test
  void testValidateDoubleNumericField() throws URISyntaxException {
    var value = "-50.27";
    var type = List.of(new URI("xsd:decimal"));

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
  void testValidateDecimalNumericField() throws URISyntaxException {
    var value = "3sf3";
    var type = List.of(new URI("xsd:decimal"));

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
  void testValidateIntegerNumericField() throws URISyntaxException {
    var value = "-50.27";
    var type = List.of(new URI("xsd:int"));
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
  void testValidateValidNumericField() throws URISyntaxException {
    var value = "50.27";
    var type = List.of(new URI("xsd:decimal"));
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
  void testValidateInvalidTimeField() throws URISyntaxException {
    var value = "07:00:00";
    var type = List.of(new URI("xsd:time"));
    TemporalValueConstraints temporalValueConstraints = TemporalValueConstraints.builder()
        .withTemporalType(XsdTemporalDatatype.TIME)
        .build();
    validator.validateTemporalField(value, type, temporalValueConstraints, consumer, "");
    assertEquals(1, results.size());
  }

  @Test
  void testValidateValidTimeField() throws URISyntaxException {
    var value = "01:00:00-11:00";
    var type = List.of(new URI("xsd:time"));
    TemporalValueConstraints temporalValueConstraints = TemporalValueConstraints.builder()
        .withTemporalType(XsdTemporalDatatype.TIME)
        .build();
    validator.validateTemporalField(value, type, temporalValueConstraints, consumer, "");
    assertEquals(0, results.size());
  }

  @Test
  void testValidateInvalidDateField() throws URISyntaxException {
    var value = "2024/01/01";
    var type = List.of(new URI("xsd:date"));
    TemporalValueConstraints temporalValueConstraints = TemporalValueConstraints.builder()
        .withTemporalType(XsdTemporalDatatype.DATE)
        .build();
    validator.validateTemporalField(value, type, temporalValueConstraints, consumer, "");
    assertEquals(1, results.size());
  }

  @Test
  void testValidateValidDateField() throws URISyntaxException {
    var value = "2024-01-01";
    var type = List.of(new URI("xsd:date"));
    TemporalValueConstraints temporalValueConstraints = TemporalValueConstraints.builder()
        .withTemporalType(XsdTemporalDatatype.DATE)
        .build();
    validator.validateTemporalField(value, type, temporalValueConstraints, consumer, "");
    assertEquals(0, results.size());
  }

  @Test
  void testValidateInvalidDateTimeField() throws URISyntaxException {
    var value = "2024-01-02T15:27:29";
    var type = List.of(new URI("xsd:dateTime"));
    TemporalValueConstraints temporalValueConstraints = TemporalValueConstraints.builder()
        .withTemporalType(XsdTemporalDatatype.DATETIME)
        .build();
    validator.validateTemporalField(value, type, temporalValueConstraints, consumer, "");
    assertEquals(1, results.size());
  }

  @Test
  void testValidateValidDateTimeField() throws URISyntaxException {
    var value = "2024-01-02T15:27:29-08:00";
    var type = List.of(new URI("xsd:dateTime"));
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
    validator.validateTemporalField(value, List.of(), temporalValueConstraints, consumer, "");
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
  void testValidateTypeValueWithInvalidType() {
    URI inputType = URI.create("http://www.w3.org/2001/XMLSchema#dateTime");
    String typeConstraint = "xsd:date";
    validator.validateTypeValue(inputType, typeConstraint, consumer, "emailPath");
    assertEquals(1, results.size());
  }

  @Test
  void testValidateTypeValueWithValidType() {
    URI inputType = URI.create("http://www.w3.org/2001/XMLSchema#dateTime");
    String typeConstraint = "xsd:dateTime";
    validator.validateTypeValue(inputType, typeConstraint, consumer, "emailPath");
    assertEquals(0, results.size());
  }


  @Test
  void testValidateWithValidTextField(){
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
    FieldValues fieldValues = new FieldValues(
        List.of(), Optional.empty(), Optional.of(textFieldValue), Optional.empty());
    Map<String, FieldValues> values = new HashMap<>();
    values.put(fieldPath, fieldValues);

    when(valuesReporter.getValues()).thenReturn(values);

    validator.validate(valueConstraintsReporter, valuesReporter, consumer);
    assertEquals(0, results.size());
  }

  @Test
  void testValidateWithInvalidTextField(){
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
    String textFieldValue = "a";
    FieldValues fieldValues = new FieldValues(
        List.of(), Optional.empty(), Optional.of(textFieldValue), Optional.of("label"));
    Map<String, FieldValues> values = new HashMap<>();
    values.put(fieldPath, fieldValues);

    when(valuesReporter.getValues()).thenReturn(values);

    validator.validate(valueConstraintsReporter, valuesReporter, consumer);
    assertEquals(3, results.size());
  }

  @Test
  void testValidateWithValidNumericField() throws URISyntaxException {
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
    FieldValues fieldValues = new FieldValues(
        List.of(new URI("http://www.w3.org/2001/XMLSchema#decimal")),
        Optional.empty(),
        Optional.of(numericFieldValue),
        Optional.empty());
    Map<String, FieldValues> values = new HashMap<>();
    values.put(fieldPath, fieldValues);

    when(valuesReporter.getValues()).thenReturn(values);

    validator.validate(valueConstraintsReporter, valuesReporter, consumer);
    assertEquals(0, results.size());
  }

  @Test
  void testValidateWithInvalidNumericField() throws URISyntaxException {
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
    String numericFieldValue = "-50.236";
    FieldValues fieldValues = new FieldValues(
        List.of(new URI("http://www.w3.org/2001/XMLSchema#int")),
        Optional.empty(),
        Optional.of(numericFieldValue),
        Optional.of("label"));
    Map<String, FieldValues> values = new HashMap<>();
    values.put(fieldPath, fieldValues);

    when(valuesReporter.getValues()).thenReturn(values);

    validator.validate(valueConstraintsReporter, valuesReporter, consumer);
    assertEquals(4, results.size());
  }

  @Test
  void testValidateWithValidTemporalField() throws URISyntaxException {
    String fieldName = "Temporal field";
    String templateName = "My template";
    FieldSchemaArtifact temporalFieldArtifact = FieldSchemaArtifact.temporalFieldBuilder()
        .withName(fieldName)
        .withRequiredValue(true)
        .withTemporalType(XsdTemporalDatatype.DATETIME)
        .withTemporalGranularity(TemporalGranularity.YEAR)
        .withTimeZoneEnabled(true)
        .withInputTimeFormat(InputTimeFormat.TWENTY_FOUR_HOUR)
        .build();

    TemplateSchemaArtifact templateSchemaArtifact = TemplateSchemaArtifact.builder()
        .withName(templateName)
        .withFieldSchema(temporalFieldArtifact)
        .build();

    var templateReporter = new TemplateReporter(templateSchemaArtifact);

    String fieldPath = "/" + fieldName;
    String temporalFieldValue = "2024-01-17T11:01:01-12:00";
    FieldValues fieldValues = new FieldValues(
        List.of(new URI("http://www.w3.org/2001/XMLSchema#dateTime")),
        Optional.empty(),
        Optional.of(temporalFieldValue),
        Optional.empty());
    Map<String, FieldValues> values = new HashMap<>();
    values.put(fieldPath, fieldValues);

    when(valuesReporter.getValues()).thenReturn(values);

    validator.validate(templateReporter, valuesReporter, consumer);
    assertEquals(0, results.size());
  }

  @Test
  void testValidateWithInvalidTemporalField() throws URISyntaxException {
    String fieldName = "Temporal field";
    String templateName = "My template";
    FieldSchemaArtifact temporalFieldArtifact = FieldSchemaArtifact.temporalFieldBuilder()
        .withName(fieldName)
        .withRequiredValue(true)
        .withTemporalType(XsdTemporalDatatype.DATETIME)
        .withTemporalGranularity(TemporalGranularity.YEAR)
        .withTimeZoneEnabled(true)
        .withInputTimeFormat(InputTimeFormat.TWENTY_FOUR_HOUR)
        .build();

    TemplateSchemaArtifact templateSchemaArtifact = TemplateSchemaArtifact.builder()
        .withName(templateName)
        .withFieldSchema(temporalFieldArtifact)
        .build();

    var templateReporter = new TemplateReporter(templateSchemaArtifact);

    String fieldPath = "/" + fieldName;
    String temporalFieldValue = "2024-01-17T11:01:01";
    FieldValues fieldValues = new FieldValues(
        List.of(new URI("http://www.w3.org/2001/XMLSchema#date")),
        Optional.empty(),
        Optional.of(temporalFieldValue),
        Optional.empty());
    Map<String, FieldValues> values = new HashMap<>();
    values.put(fieldPath, fieldValues);

    when(valuesReporter.getValues()).thenReturn(values);

    validator.validate(templateReporter, valuesReporter, consumer);
    assertEquals(2, results.size());
  }
}
