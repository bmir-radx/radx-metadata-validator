package edu.stanford.bmir.radx.metadata.validator.lib;

import org.metadatacenter.artifacts.model.core.fields.XsdNumericDatatype;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

@Component
public class NumericFieldValidationUtil {
  public boolean isValidNumber(String value){
    try {
      BigDecimal numberValue = new BigDecimal(value);
      return true;
    } catch (NumberFormatException e){
      return false;
    }
  }
  public boolean numberIsInRange(Optional<Number> minValue, Optional<Number> maxValue, String value) {
      BigDecimal numberValue = new BigDecimal(value);
      if (minValue.isPresent() && numberValue.compareTo(new BigDecimal(minValue.get().toString())) < 0) {
        return false;
      } else if (maxValue.isPresent() && numberValue.compareTo(new BigDecimal(maxValue.get().toString())) > 0) {
        return false;
      }
      return true;
  }

  public void validateNumericType(String value, XsdNumericDatatype dataType, Optional<Integer> decimalPlace) throws NumberFormatException, JsonParseException{
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
  }

  public void validateDecimalPlace(double value, Optional<Integer> decimalPlace) {
    decimalPlace.ifPresent(dp -> {
      //TODO: if decimal place is < dp, should we return error or repair it?
      if (getDecimalPlaces(value) > dp) {
        throw new JsonParseException(String.format("Input value %s has more than %d decimal places", value, dp));
      }
    });
  }

  public int getDecimalPlaces(double value) {
    String stringValue = Double.toString(value);
    int dotIndex = stringValue.indexOf('.');
    return dotIndex < 0 ? 0 : stringValue.length() - dotIndex - 1;
  }

  public void validateDecimalPlace(BigDecimal value, Optional<Integer> decimalPlace) {
    decimalPlace.ifPresent(dp -> {
      if (getDecimalPlaces(value) > dp) {
        throw new JsonParseException(String.format("Input value %s has more than %d decimal places", value, dp));
      }
    });
  }

  public int getDecimalPlaces(BigDecimal value) {
    return Math.max(value.scale(), 0);
  }
}
