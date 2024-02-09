package edu.stanford.bmir.radx.metadata.validator.lib;

import edu.stanford.bmir.radx.metadata.validator.lib.validators.RadxPrecisionValidatorComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class RadxPrecisionValidatorComponentTest {
  private RadxPrecisionValidatorComponent radxPrecisionValidatorComponent;
  private LiteralFieldValidators literalFieldValidators;
  @Mock
  private TemplateInstanceValuesReporter valuesReporter;

  private List<ValidationResult> results;

  private Consumer<ValidationResult> handler;
  private HashMap<FieldPath, LiteralFieldValidator> map;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    radxPrecisionValidatorComponent = new RadxPrecisionValidatorComponent();
    map = new HashMap<>();
    results = new ArrayList<>();
    handler = results::add;
  }

  @Test
  void testValidateSha256DigestPass(){
    String expectedValue = "sha256";
    String fieldPath = "/Data File Identity/SHA256 digest";
    FieldValues fieldValues = new FieldValues(
        List.of(), Optional.empty(), Optional.of("sha256"), Optional.empty());
    Map<String, FieldValues> values = new HashMap<>();
    values.put(fieldPath, fieldValues);
    when(valuesReporter.getValues()).thenReturn(values);

    String errorMessage = "Expected SHA256 digest equals to sha256";
    String warningMessage = "Expected SHA256 digest equals to sha256, but an empty value is received.";
    map.put(RADxSpecificFieldPath.SHA256_DIGEST.getFieldPath(),
        new ConstantValueFieldValidator(expectedValue, errorMessage, warningMessage));

    literalFieldValidators = new LiteralFieldValidators(map);

    radxPrecisionValidatorComponent.validate(literalFieldValidators, valuesReporter, handler);
    assertEquals(0, results.size());
  }

  @Test
  void testValidateSha256DigestWarning(){
    String expectedValue = "sha256";
    String fieldPath = "/Data File Identity/SHA256 digest";
    FieldValues fieldValues = new FieldValues(
        List.of(), Optional.empty(), Optional.empty(), Optional.empty());
    Map<String, FieldValues> values = new HashMap<>();
    values.put(fieldPath, fieldValues);
    when(valuesReporter.getValues()).thenReturn(values);

    String errorMessage = "Expected SHA256 digest equals to sha256";
    String warningMessage = "Expected SHA256 digest equals to sha256, but an empty value is received.";
    map.put(RADxSpecificFieldPath.SHA256_DIGEST.getFieldPath(),
        new ConstantValueFieldValidator(expectedValue, errorMessage, warningMessage));

    literalFieldValidators = new LiteralFieldValidators(map);

    radxPrecisionValidatorComponent.validate(literalFieldValidators, valuesReporter, handler);
    assertEquals(1, results.size());
  }

  @Test
  void testValidateSha256DigestError(){
    String expectedValue = "sha256";
    String fieldPath = "/Data File Identity/SHA256 digest";
    FieldValues fieldValues = new FieldValues(
        List.of(), Optional.empty(), Optional.of("sha"), Optional.empty());
    Map<String, FieldValues> values = new HashMap<>();
    values.put(fieldPath, fieldValues);
    when(valuesReporter.getValues()).thenReturn(values);

    String errorMessage = "Expected SHA256 digest equals to sha256";
    String warningMessage = "Expected SHA256 digest equals to sha256, but an empty value is received.";
    map.put(RADxSpecificFieldPath.SHA256_DIGEST.getFieldPath(),
        new ConstantValueFieldValidator(expectedValue, errorMessage, warningMessage));

    literalFieldValidators = new LiteralFieldValidators(map);

    radxPrecisionValidatorComponent.validate(literalFieldValidators, valuesReporter, handler);
    assertEquals(1, results.size());
  }

  @Test
  void testValidateFileNameDigestPass(){
    String expectedValue = "file name";
    String fieldPath = "/Data File Identity/File Name";
    FieldValues fieldValues = new FieldValues(
        List.of(), Optional.empty(), Optional.of("file name"), Optional.empty());
    Map<String, FieldValues> values = new HashMap<>();
    values.put(fieldPath, fieldValues);
    when(valuesReporter.getValues()).thenReturn(values);

    String errorMessage = "Expected File Name equals to file name";
    String warningMessage = "Expected File Name equals to file name, but an empty value is received.";
    map.put(RADxSpecificFieldPath.FILE_NAME.getFieldPath(),
        new ConstantValueFieldValidator(expectedValue, errorMessage, warningMessage));

    literalFieldValidators = new LiteralFieldValidators(map);

    radxPrecisionValidatorComponent.validate(literalFieldValidators, valuesReporter, handler);
    assertEquals(0, results.size());
  }

  @Test
  void testValidateFileNameDigestWarning(){
    String expectedValue = "file name";
    String fieldPath = "/Data File Identity/File Name";
    FieldValues fieldValues = new FieldValues(
        List.of(), Optional.empty(), Optional.empty(), Optional.empty());
    Map<String, FieldValues> values = new HashMap<>();
    values.put(fieldPath, fieldValues);
    when(valuesReporter.getValues()).thenReturn(values);

    String errorMessage = "Expected File Name equals to file name";
    String warningMessage = "Expected File Name equals to file name, but an empty value is received.";
    map.put(RADxSpecificFieldPath.FILE_NAME.getFieldPath(),
        new ConstantValueFieldValidator(expectedValue, errorMessage, warningMessage));

    literalFieldValidators = new LiteralFieldValidators(map);

    radxPrecisionValidatorComponent.validate(literalFieldValidators, valuesReporter, handler);
    assertEquals(1, results.size());
  }

  @Test
  void testValidateFileNameDigestError(){
    String expectedValue = "file name";
    String fieldPath = "/Data File Identity/File Name";
    FieldValues fieldValues = new FieldValues(
        List.of(), Optional.empty(), Optional.of("file name1"), Optional.empty());
    Map<String, FieldValues> values = new HashMap<>();
    values.put(fieldPath, fieldValues);
    when(valuesReporter.getValues()).thenReturn(values);

    String errorMessage = "Expected File Name equals to file name";
    String warningMessage = "Expected File Name equals to file name, but an empty value is received.";
    map.put(RADxSpecificFieldPath.FILE_NAME.getFieldPath(),
        new ConstantValueFieldValidator(expectedValue, errorMessage, warningMessage));

    literalFieldValidators = new LiteralFieldValidators(map);

    radxPrecisionValidatorComponent.validate(literalFieldValidators, valuesReporter, handler);
    assertEquals(1, results.size());
  }

  @Test
  void testValidateDictFileNameDigestPass(){
    String expectedValue = "dict file name";
    String fieldPath = "/Data File Data Dictionary/Data Dictionary File Name";
    FieldValues fieldValues = new FieldValues(
        List.of(), Optional.empty(), Optional.of(expectedValue), Optional.empty());
    Map<String, FieldValues> values = new HashMap<>();
    values.put(fieldPath, fieldValues);
    when(valuesReporter.getValues()).thenReturn(values);

    String errorMessage = "Expected Data Dict File Name equals to file name";
    String warningMessage = "Expected Data Dict File Name equals to file name, but an empty value is received.";
    map.put(RADxSpecificFieldPath.DATA_DICT_FILE_NAME.getFieldPath(),
        new ConstantValueFieldValidator(expectedValue, errorMessage, warningMessage));

    literalFieldValidators = new LiteralFieldValidators(map);

    radxPrecisionValidatorComponent.validate(literalFieldValidators, valuesReporter, handler);
    assertEquals(0, results.size());
  }

  @Test
  void testValidateDictFileNameDigestWarning(){
    String expectedValue = "dict file name";
    String fieldPath = "/Data File Data Dictionary/Data Dictionary File Name";
    FieldValues fieldValues = new FieldValues(
        List.of(), Optional.empty(), Optional.empty(), Optional.empty());
    Map<String, FieldValues> values = new HashMap<>();
    values.put(fieldPath, fieldValues);
    when(valuesReporter.getValues()).thenReturn(values);

    String errorMessage = "Expected Data Dict File Name equals to file name";
    String warningMessage = "Expected Data Dict File Name equals to file name, but an empty value is received.";
    map.put(RADxSpecificFieldPath.DATA_DICT_FILE_NAME.getFieldPath(),
        new ConstantValueFieldValidator(expectedValue, errorMessage, warningMessage));

    literalFieldValidators = new LiteralFieldValidators(map);

    radxPrecisionValidatorComponent.validate(literalFieldValidators, valuesReporter, handler);
    assertEquals(1, results.size());
  }

  @Test
  void testValidateDictFileNameDigestError(){
    String expectedValue = "dict file name";
    String fieldPath = "/Data File Data Dictionary/Data Dictionary File Name";
    FieldValues fieldValues = new FieldValues(
        List.of(), Optional.empty(), Optional.of("data dict file name"), Optional.empty());
    Map<String, FieldValues> values = new HashMap<>();
    values.put(fieldPath, fieldValues);
    when(valuesReporter.getValues()).thenReturn(values);

    String errorMessage = "Expected Data Dict File Name equals to file name";
    String warningMessage = "Expected Data Dict File Name equals to file name, but an empty value is received.";
    map.put(RADxSpecificFieldPath.DATA_DICT_FILE_NAME.getFieldPath(),
        new ConstantValueFieldValidator(expectedValue, errorMessage, warningMessage));

    literalFieldValidators = new LiteralFieldValidators(map);

    radxPrecisionValidatorComponent.validate(literalFieldValidators, valuesReporter, handler);
    assertEquals(1, results.size());
  }


}
