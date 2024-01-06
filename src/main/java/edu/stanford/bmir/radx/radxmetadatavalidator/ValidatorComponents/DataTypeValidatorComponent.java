package edu.stanford.bmir.radx.radxmetadatavalidator.ValidatorComponents;

import edu.stanford.bmir.radx.radxmetadatavalidator.*;
import org.metadatacenter.artifacts.model.core.fields.XsdNumericDatatype;
import org.metadatacenter.artifacts.model.core.fields.XsdTemporalDatatype;
import org.metadatacenter.artifacts.model.core.fields.constraints.ValueConstraints;
import org.metadatacenter.artifacts.model.visitors.TemplateReporter;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.*;
import java.time.format.DateTimeFormatter;
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
        var type = fieldValue.get(SchemaProperties.TYPE);
        if (value != null) {
          //If it is text field, validate regex, length and literals
          if (valueConstraint.get().isTextValueConstraint()){
            validateTextField(value, valueConstraint.get(), handler, path);
          //If it is numeric filed, validate range, data type and @type value
          } else if (valueConstraint.get().isNumericValueConstraint()) {
            validateNumericField(value, type, valueConstraint.get(), handler, path);
          //If it is temporal field, validate temporal data type and @type value
          } else if (valueConstraint.get().isTemporalValueConstraint()){
            validateTemporalField(value, type, valueConstraint.get(), handler, path);
          }
        }

        var id = fieldValue.get(SchemaProperties.ID);
        if (id != null){
          if (valueConstraint.get().isLinkValueConstraint()){
            validateLinkField(id.toString(), handler, path);
          }
        }

        if (valueConstraint.get().isControlledTermValueConstraint()){
          //TODO: validate controlled terms
          var controlledTermConstraint = valueConstraint.get().asControlledTermValueConstraints();
        }
      }
    }
  }

  public void validateTextField(Object value, ValueConstraints valueConstraint, Consumer<ValidationResult> handler, String path){
    var textConstraint = valueConstraint.asTextValueConstraints();
    if(value instanceof String textValue){
      //validate regex
      if(!matchRegex(textConstraint.regex(), textValue)){
        String message = String.format("%s does not follow the regex (%s)", textValue, textConstraint.regex().orElse(""));
        handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, message, path));
      }
      //validate length range
      var length = textValue.length();
      if(!lengthIsInRange(textConstraint.minLength(), textConstraint.maxLength(), length)){
        String message = String.format("Input string length (%d) is out of range [%d, %d]", length, textConstraint.minLength().orElse(0), textConstraint.maxLength().orElse(Integer.MAX_VALUE));
        handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, message, path));
      }
      //validate literals
      var literals = textConstraint.literals();
      if(literals.size() > 0){
        if(!literals.contains(textValue)){
          String message = String.format("%s does not exist in the given list: %s", textValue, literals);
          handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, message, path));
        }
      }
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

  public void validateNumericField(Object value, Object type, ValueConstraints valueConstraint, Consumer<ValidationResult> handler, String path) {
    var numericConstraint = valueConstraint.asNumericValueConstraints();
    if (value instanceof String numbericValueString) {
      //validate data type
      try {
        isValidNumericType(numbericValueString, numericConstraint.numberType(), numericConstraint.decimalPlace());
      } catch (JsonParseException e) {
        handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, e.getMessage(), path));
      }
      //validate range
      try {
        numberIsInRange(numericConstraint.minValue(), numericConstraint.maxValue(), numbericValueString);
      } catch (JsonParseException e) {
        handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, e.getMessage(), path));
      }
    } else {
      handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, "Expected a value of type String for @value", path));
    }

    //validate @type value
    var numberType = numericConstraint.numberType().getText();
    if (type instanceof String numbericTypeString) {
      if(!type.equals(numberType)){
        String message = String.format("Expected %s for @type", numbericTypeString);
        handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, message, path));
      }
    } else {
      handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, "Expected a value of type String for @type", path));
    }
  }

  private void numberIsInRange(Optional<Number> minValue, Optional<Number> maxValue, String value) {
    try {
      BigDecimal numberValue = new BigDecimal(value);

      if (minValue.isPresent() && numberValue.compareTo(new BigDecimal(minValue.get().toString())) < 0) {
        String errorMessage = String.format("Input value %s is below the minimum allowed value %s", value, minValue.get());
        throw new JsonParseException(errorMessage);
      } else if (maxValue.isPresent() && numberValue.compareTo(new BigDecimal(maxValue.get().toString())) > 0) {
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
        case INTEGER://Integer numbers
          Integer.parseInt(value);
          break;
        case DOUBLE://Double precision real numbers
          double doubleValue = Double.parseDouble(value);
          validateDecimalPlace(doubleValue, decimalPlace);
          break;
        case LONG://Long integer numbers
          Long.parseLong(value);
          break;
        case SHORT:
          Short.parseShort(value);
          break;
        case FLOAT://Single precision real numbers
          float floatValue = Float.parseFloat(value);
          validateDecimalPlace(floatValue, decimalPlace);
          break;
        case BYTE:
          Byte.parseByte(value);
          break;
        case DECIMAL://Any numbers
          var decimalValue = new BigDecimal(value);
          validateDecimalPlace(decimalValue, decimalPlace);
          break;
      }
    } catch (NumberFormatException e) {
      throw new JsonParseException(String.format("Input value %s is not consistent with numeric data type %s", value, dataType));
    }
  }

  private void validateDecimalPlace(double value, Optional<Integer> decimalPlace) {
    decimalPlace.ifPresent(dp -> {
      //TODO: if decimal place is < dp, should we return error or repair it?
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

  private void validateDecimalPlace(BigDecimal value, Optional<Integer> decimalPlace) {
    decimalPlace.ifPresent(dp -> {
      if (getDecimalPlaces(value) > dp) {
        throw new JsonParseException(String.format("Input value %s has more than %d decimal places", value, dp));
      }
    });
  }

  private int getDecimalPlaces(BigDecimal value) {
    return Math.max(value.scale(), 0);
  }

  private void isValidDate(String value) {
    try {
      DateTimeFormatter.ISO_LOCAL_DATE.parse(value);
      //TODO: only allow format yyyy-MM-dd (2024-01-01), don't allow 2024/01/01?
    } catch (DateTimeParseException e) {
      throw new JsonParseException("Input value %s is not a valid Date");
    }
  }

  private void isValidDateTime(String value) {
    try {
      OffsetDateTime.parse(value, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    } catch (DateTimeParseException e) {
      throw new JsonParseException("Input value %s is not a valid DateTime");
    }
  }

  private void isValidTime(String value) {
    try {
      OffsetTime.parse(value, DateTimeFormatter.ISO_OFFSET_TIME);
    } catch (DateTimeParseException e) {
      throw new JsonParseException("Input value %s is not a valid Time");
    }
  }

  public void validateTemporalField(Object value, Object type, ValueConstraints valueConstraint, Consumer<ValidationResult> handler, String path){
    var temporalConstraint = valueConstraint.asTemporalValueConstraints();
    var temporalDatatype = temporalConstraint.temporalType();
    //validate @value value
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
    } else {
      handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, "Expected a value of type String for @value", path));
    }

    //validate @type value
    if(type instanceof String typeValue){
      if(!type.equals(temporalDatatype.getText())){
        String message = String.format("Expected %s for @type", temporalDatatype.getText());
        handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, message, path));
      }
    }else{
      handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, "Expected a value of type String for @type", path));
    }
  }

  public void validateLinkField(String id, Consumer<ValidationResult> handler, String path) {
    try {
      new URL(id);
    } catch (MalformedURLException e) {
      var message = String.format("Input value %s is not a valid URL", id);
      handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, message, path));
    }
  }
}
