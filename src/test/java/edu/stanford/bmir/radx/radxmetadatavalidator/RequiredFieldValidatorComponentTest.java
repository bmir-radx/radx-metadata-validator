package edu.stanford.bmir.radx.radxmetadatavalidator;

import edu.stanford.bmir.radx.radxmetadatavalidator.ValidatorComponents.RequiredFieldValidatorComponent;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.metadatacenter.artifacts.model.core.fields.XsdNumericDatatype;
import org.metadatacenter.artifacts.model.core.fields.constraints.NumericValueConstraints;
import org.metadatacenter.artifacts.model.visitors.TemplateReporter;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.AssertionErrors.assertNotNull;

public class RequiredFieldValidatorComponentTest {
  @Mock
  private TemplateReporter valueConstraintsReporter;
  @Mock
  private TemplateInstanceValuesReporter valuesReporter;
  @Mock
  private Consumer<ValidationResult> handler;

  private RequiredFieldValidatorComponent validator = new RequiredFieldValidatorComponent();

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);

    Map<String, Map<SchemaProperties, Object>> values = new HashMap<>();
    Map<SchemaProperties, Object> fieldValue = new HashMap<>();
    fieldValue.put(SchemaProperties.VALUE, null); // Simulate missing value
    values.put("fieldPath", fieldValue);
    when(valuesReporter.getValues()).thenReturn(values);

    boolean requiredValue = true;
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

    when(valueConstraintsReporter.getValueConstraints("fieldPath")).thenReturn(Optional.of(numericValueConstraints));
  }

  @Test
  public void testValidate_RequiredFieldMissing() {
    validator.validate(valueConstraintsReporter, valuesReporter, handler);
    verify(handler, times(1)).accept(any(ValidationResult.class));
  }
}
