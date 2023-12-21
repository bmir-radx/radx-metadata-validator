package edu.stanford.bmir.radx.radxmetadatavalidator;

import edu.stanford.bmir.radx.radxmetadatavalidator.ValidatorComponents.DataTypeValidatorComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.metadatacenter.artifacts.model.core.fields.XsdNumericDatatype;
import org.metadatacenter.artifacts.model.core.fields.constraints.NumericValueConstraints;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

public class DataTypeValidatorComponentTest {
  private List<ValidationResult> results;
  private Consumer<ValidationResult> consumer;
  @BeforeEach
  void setup() {
    results = new ArrayList<>();
    consumer = results::add;
  }
  @Test
  public void testValidDate(){
    DataTypeValidatorComponent validator = new DataTypeValidatorComponent();
    var value = "50.26";

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

    validator.validateNumericField(value, numericValueConstraints, consumer, "");
//    assertThrows(JsonParseException.class, () -> validator.validateNumericField(value, numericValueConstraints, consumer, ""));
    assertEquals(1, results.size());
  }
}
