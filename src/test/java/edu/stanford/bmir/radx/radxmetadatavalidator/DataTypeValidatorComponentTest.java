package edu.stanford.bmir.radx.radxmetadatavalidator;

import edu.stanford.bmir.radx.radxmetadatavalidator.ValidatorComponents.DataTypeValidatorComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.metadatacenter.artifacts.model.core.fields.XsdNumericDatatype;
import org.metadatacenter.artifacts.model.core.fields.XsdTemporalDatatype;
import org.metadatacenter.artifacts.model.core.fields.constraints.NumericValueConstraints;
import org.metadatacenter.artifacts.model.core.fields.constraints.TemporalValueConstraints;
import org.metadatacenter.artifacts.model.core.fields.constraints.TextValueConstraints;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;

public class DataTypeValidatorComponentTest {
  private List<ValidationResult> results;
  private Consumer<ValidationResult> consumer;
  DataTypeValidatorComponent validator = new DataTypeValidatorComponent();
  @BeforeEach
  void setup() {
    results = new ArrayList<>();
    consumer = results::add;
  }
  @Test
  public void testValidateDoubleNumericField(){
    var value = "-50.27";
    var type = "xsd:decimal";

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
  public void testValidateDecimalNumericField(){
    var value = "3sf3";
    var type = "xsd:decimal";

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
    assertEquals(2, results.size());
  }

  @Test
  public void testValidateIntegerNumericField(){
    var value = "-50.27";
    var type = "xsd:int";
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

    validator.validateNumericField(value, type, numericValueConstraints, consumer, "");
//    assertThrows(JsonParseException.class, () -> validator.validateNumericField(value, numericValueConstraints, consumer, ""));
    assertEquals(2, results.size());
  }

  @Test
  public void testValidateTextFieldRegex(){
    var value = "2345";
    TextValueConstraints textValueConstraints = TextValueConstraints.builder()
        .withMinLength(10)
        .withRegex("^\\(?([0-9]{3})\\)?[-. ]?([0-9]{3})[-. ]?([0-9]{4})$")
        .build().asTextValueConstraints();
    validator.validateTextField(value, textValueConstraints, consumer, "");
    assertEquals(2, results.size());
  }

  @Test
  public void testValidateTextFieldLiterals(){
    var value = "radx";
    TextValueConstraints textValueConstraints = TextValueConstraints.builder()
        .withChoice("cedar", false)
        .withChoice("bioportal", false)
        .withChoice("protege", false)
        .build().asTextValueConstraints();

    validator.validateTextField(value, textValueConstraints, consumer, "");
    assertEquals(1, results.size());
  }

  @Test
  public void testValidateTimeField(){
    var value = "07:00:00";
    var type = "xsd:time";
    TemporalValueConstraints temporalValueConstraints = TemporalValueConstraints.builder()
        .withTemporalType(XsdTemporalDatatype.TIME)
        .build();
    validator.validateTemporalField(value, type, temporalValueConstraints, consumer, "");
    assertEquals(1, results.size());
  }

  @Test
  public void testValidateDateField(){
    var value = "2024/01/01";
    var type = "xsd:date";
    TemporalValueConstraints temporalValueConstraints = TemporalValueConstraints.builder()
        .withTemporalType(XsdTemporalDatatype.DATE)
        .build();
    validator.validateTemporalField(value, type, temporalValueConstraints, consumer, "");
    assertEquals(1, results.size());
  }

  @Test
  public void testValidateDateTimeField(){
    var value = "2024-01-02T15:27:29";
    var type = "xsd:dateTime";
    TemporalValueConstraints temporalValueConstraints = TemporalValueConstraints.builder()
        .withTemporalType(XsdTemporalDatatype.DATETIME)
        .build();
    validator.validateTemporalField(value, type, temporalValueConstraints, consumer, "");
    assertEquals(1, results.size());
  }

  @Test
  public void testValidateLinkField(){
    var id = "www.abc.com";
    validator.validateLinkField(id, consumer, "");
    assertEquals(1, results.size());
  }

}
