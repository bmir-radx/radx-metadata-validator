package edu.stanford.bmir.radx.radxmetadatavalidator.ValidatorComponents;

import edu.stanford.bmir.radx.radxmetadatavalidator.*;
import org.metadatacenter.artifacts.model.core.fields.XsdNumericDatatype;
import org.metadatacenter.artifacts.model.core.fields.XsdTemporalDatatype;
import org.metadatacenter.artifacts.model.core.fields.constraints.ValueConstraints;
import org.metadatacenter.artifacts.model.visitors.TemplateReporter;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

@Component
public class DataTypeValidatorComponent {
  public void validate(TemplateReporter valueConstraintsReporter, TemplateInstanceValuesReporter valuesReporter, Consumer<ValidationResult> handler){
    var values = valuesReporter.getValues();

    for (Map.Entry<String, Map<SchemaProperties, Object>> FieldEntry : values.entrySet()) {
      String path = FieldEntry.getKey();
      Map<SchemaProperties, Object> fieldValue = FieldEntry.getValue();
      var valueConstraint = valueConstraintsReporter.getValueConstraints(path);

      if(valueConstraint.isPresent()){
        var value = fieldValue.get(SchemaProperties.VALUE);
        if (value != null) {
          //If it is text field, validate regex and length
          if (valueConstraint.get().isTextValueConstraint()){
            validateTextField(value, valueConstraint.get(), handler, path);
          //If it is numeric filed, validate range and type
          } else if (valueConstraint.get().isNumericValueConstraint()) {
            validateNumericField(value, valueConstraint.get(), handler, path);
          //If it is temporal field, validate temporal data type
          } else if (valueConstraint.get().isTemporalValueConstraint()){
            validateTemporalField(value, valueConstraint.get(), handler, path);
          }
        }

        var id = fieldValue.get(SchemaProperties.ID);
        if (id != null){
          if (valueConstraint.get().isLinkValueConstraint()){
            validateLinkField(id, valueConstraint.get(), handler, path);
          }
        }

        if (valueConstraint.get().isControlledTermValueConstraint()){
          //TODO: check controlled terms
          var controlledTermConstraint = valueConstraint.get().asControlledTermValueConstraints();
        }
      }
    }
  }

  public void validateTextField(Object value, ValueConstraints valueConstraint, Consumer<ValidationResult> handler, String path){
    var textConstraint = valueConstraint.asTextValueConstraints();
    if(value instanceof String textValue){
      //check regex
      if(!matchRegex(textConstraint.regex(), textValue)){
        String message = String.format("%s does not follow the regex (%s)", textValue, textConstraint.regex().orElse(""));
        handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, message, path));
      }
      //check length range
      var length = textValue.length();
      if(!lengthIsInRange(textConstraint.minLength(), textConstraint.maxLength(), length)){
        String message = String.format("Input string length (%d) is out of range [%d, %d]", length, textConstraint.minLength().orElse(0), textConstraint.maxLength().orElse(Integer.MAX_VALUE));
        handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, message, path));
      }
      //TODO:check literals?
      var literals = textConstraint.literals();
    } else { //The value is not a string
      handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, "Expected a value of type String", path));
    }
  }

  private boolean matchRegex(Optional<String> regex, String textValueString){
    if(regex.isPresent()){
      String regexString = regex.get();
      return textValueString.matches(regexString);
    }else{
      return true;
    }
  }
  private boolean lengthIsInRange(Optional<Integer> minLength, Optional<Integer> maxLength, Integer length){
    return minLength.map(min -> length >= min).orElse(true)
        && maxLength.map(max -> length <= max).orElse(true);
  }

  public void validateNumericField(Object value, ValueConstraints valueConstraint, Consumer<ValidationResult> handler, String path) {
    var numericConstraint = valueConstraint.asNumericValueConstraints();
    if (value instanceof String numbericValueString) {
      //Check range
      try {
        numberIsInRange(numericConstraint.minValue(), numericConstraint.maxValue(), numbericValueString);
      } catch (JsonParseException e) {
        handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, e.getMessage(), path));
      }
      //check data type
      try {
        isValidNumericType(numbericValueString, numericConstraint.numberType(), numericConstraint.decimalPlace());
      } catch (JsonParseException e) {
        handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, e.getMessage(), path));
      }
      //TODO: check @type in numeric filed instance?
    } else {
      handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, "Expected a value of type String", path));
    }
  }

  private void numberIsInRange(Optional<Number> minValue, Optional<Number> maxValue, String value){
    try {
      double doubleValue = Double.parseDouble(value);

      if (minValue.isPresent() && doubleValue < minValue.get().doubleValue()) {
        String errorMessage = String.format("Input value %s is below the minimum allowed value %s", value, minValue.get());
        throw new JsonParseException(errorMessage);
      } else if (maxValue.isPresent() && doubleValue > maxValue.get().doubleValue()) {
        String errorMessage = String.format("Input value %s is above the maximum allowed value %s", value, maxValue.get());
        throw new JsonParseException(errorMessage);
      }
    } catch (NumberFormatException e) {
      String errorMessage = String.format("Invalid numeric input: %s is not a valid number", value);
      throw new JsonParseException(errorMessage);
    }
  }

  private void isValidNumericType(String value, XsdNumericDatatype dataType, Optional<Integer> decimalPlace) {
    try {
      switch (dataType) {
        case INTEGER:
          Integer.parseInt(value);
          break;
        case DOUBLE:
          double doubleValue = Double.parseDouble(value);
          checkDecimalPlace(doubleValue, decimalPlace);
          break;
        case LONG:
          Long.parseLong(value);
          break;
        case SHORT:
          Short.parseShort(value);
          break;
        case FLOAT:
          float floatValue = Float.parseFloat(value);
          checkDecimalPlace(floatValue, decimalPlace);
          break;
        case BYTE:
          Byte.parseByte(value);
          break;
          //TODO case decimal???
      }
    } catch (NumberFormatException e) {
      throw new JsonParseException(String.format("Input value %s is not consistent with numeric data type %s", value, dataType));
    }
  }

  private void checkDecimalPlace(double value, Optional<Integer> decimalPlace) {
    decimalPlace.ifPresent(dp -> {
      if (getDecimalPlaces(value) > dp) {
        throw new JsonParseException(String.format("Input value %s has more than %d decimal places", value, dp));
      }
    });
  }

  private int getDecimalPlaces(double value) {
    String stringValue = Double.toString(value);
    int dotIndex = stringValue.indexOf('.');
    return dotIndex < 0 ? 0 : stringValue.length() - dotIndex - 1;
  }

  private void isValidDate(String value) {
    try {
      LocalDate.parse(value);
    } catch (DateTimeParseException e) {
      throw new JsonParseException("Input value %s is not a valid Date");
    }
  }

  private void isValidDateTime(String value) {
    try {
      LocalDateTime.parse(value);
    } catch (DateTimeParseException e) {
      throw new JsonParseException("Input value %s is not a valid DateTime");
    }
  }

  private void isValidTime(String value) {
    try {
      LocalTime.parse(value);
    } catch (DateTimeParseException e) {
      throw new JsonParseException("Input value %s is not a valid Time");
    }
  }

  public void validateTemporalField(Object value, ValueConstraints valueConstraint, Consumer<ValidationResult> handler, String path){
    var temporalConstraint = valueConstraint.asTemporalValueConstraints();
    var temporalDatatype = temporalConstraint.temporalType();
    if (value instanceof String temporalValue) {
      try {
        switch (temporalDatatype) {
          case DATE:
            isValidDate(temporalValue);
            break;
          case DATETIME:
            isValidDateTime(temporalValue);
            break;
          case TIME:
            isValidTime(temporalValue);
            break;
        }
      } catch (JsonParseException e) {
        handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, e.getMessage(), path));
      }
      //TODO: check @type value of the instance?
    } else {
      handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, "Expected a value of type String", path));
    }
  }

  public void validateLinkField(Object value, ValueConstraints valueConstraint, Consumer<ValidationResult> handler, String path) {
    if (!(value instanceof URI)) {
      var message = String.format("Input value %s is not a valid URI", value);
      handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, message, path));
    }
  }
}
