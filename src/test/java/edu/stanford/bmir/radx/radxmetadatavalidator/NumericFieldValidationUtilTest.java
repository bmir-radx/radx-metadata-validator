package edu.stanford.bmir.radx.radxmetadatavalidator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.metadatacenter.artifacts.model.core.fields.XsdNumericDatatype;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
public class NumericFieldValidationUtilTest {
  private NumericFieldValidationUtil numericFieldValidationUtil;

  @BeforeEach
  void setUp() {
    numericFieldValidationUtil = new NumericFieldValidationUtil();
  }

  @Test
  void testIsValidNumberWithValidNumber() {
    assertTrue(numericFieldValidationUtil.isValidNumber("123"));
  }

  @Test
  void testIsValidNumberWithInvalidNumber() {
    assertFalse(numericFieldValidationUtil.isValidNumber("abc"));
  }

  @Test
  void testNumberIsInRangeWithinRange() {
    assertTrue(numericFieldValidationUtil.numberIsInRange(Optional.of(10), Optional.of(20), "15"));
  }

  @Test
  void testNumberIsInRangeBelowRange() {
    assertFalse(numericFieldValidationUtil.numberIsInRange(Optional.of(10), Optional.of(20), "5"));
  }

  @Test
  void testNumberIsInRangeAboveRange() {
    assertFalse(numericFieldValidationUtil.numberIsInRange(Optional.of(10), Optional.of(20), "25"));
  }

  @Test
  void testValidateNumericTypeWithValidInteger() {
    assertDoesNotThrow(() ->
        numericFieldValidationUtil.validateNumericType("123", XsdNumericDatatype.INTEGER, Optional.empty()));
  }

  @Test
  void testValidateNumericTypeWithInvalidInteger() {
    assertThrows(NumberFormatException.class, () ->
        numericFieldValidationUtil.validateNumericType("abc", XsdNumericDatatype.INTEGER, Optional.empty()));
  }

  @Test
  void testValidateNumericTypeWithValidDouble() {
    assertDoesNotThrow(() ->
        numericFieldValidationUtil.validateNumericType("123.45", XsdNumericDatatype.DOUBLE, Optional.empty()));
  }

  @Test
  void testValidateNumericTypeWithInvalidDouble() {
    assertThrows(NumberFormatException.class, () ->
        numericFieldValidationUtil.validateNumericType("abc", XsdNumericDatatype.DOUBLE, Optional.empty()));
  }

  @Test
  void testValidateNumericTypeWithValidLong() {
    assertDoesNotThrow(() ->
        numericFieldValidationUtil.validateNumericType("1234567890", XsdNumericDatatype.LONG, Optional.empty()));
  }

  @Test
  void testValidateNumericTypeWithInvalidLong() {
    assertThrows(NumberFormatException.class, () ->
        numericFieldValidationUtil.validateNumericType("abc", XsdNumericDatatype.LONG, Optional.empty()));
  }

  @Test
  void testValidateNumericTypeWithValidShort() {
    assertDoesNotThrow(() ->
        numericFieldValidationUtil.validateNumericType("123", XsdNumericDatatype.SHORT, Optional.empty()));
  }

  @Test
  void testValidateNumericTypeWithInvalidShort() {
    assertThrows(NumberFormatException.class, () ->
        numericFieldValidationUtil.validateNumericType("abc", XsdNumericDatatype.SHORT, Optional.empty()));
  }

  @Test
  void testValidateNumericTypeWithValidFloat() {
    assertDoesNotThrow(() ->
        numericFieldValidationUtil.validateNumericType("123.45", XsdNumericDatatype.FLOAT, Optional.empty()));
  }

  @Test
  void testValidateNumericTypeWithInvalidFloat() {
    assertThrows(NumberFormatException.class, () ->
        numericFieldValidationUtil.validateNumericType("abc", XsdNumericDatatype.FLOAT, Optional.empty()));
  }

  @Test
  void testValidateNumericTypeWithValidByte() {
    assertDoesNotThrow(() ->
        numericFieldValidationUtil.validateNumericType("123", XsdNumericDatatype.BYTE, Optional.empty()));
  }

  @Test
  void testValidateNumericTypeWithInvalidByte() {
    assertThrows(NumberFormatException.class, () ->
        numericFieldValidationUtil.validateNumericType("abc", XsdNumericDatatype.BYTE, Optional.empty()));
  }

  @Test
  void testValidateNumericTypeWithValidDecimal() {
    assertDoesNotThrow(() ->
        numericFieldValidationUtil.validateNumericType("123.4567", XsdNumericDatatype.DECIMAL, Optional.of(4)));
  }

  @Test
  void testValidateNumericTypeWithInvalidDecimal() {
    assertThrows(NumberFormatException.class, () ->
        numericFieldValidationUtil.validateNumericType("abc", XsdNumericDatatype.DECIMAL, Optional.empty()));
  }

  @Test
  void validateDecimalPlaceWithDoubleWithinLimit() {
    assertDoesNotThrow(() ->
        numericFieldValidationUtil.validateDecimalPlace(123.456, Optional.of(3)));
  }

  @Test
  void validateDecimalPlaceWithDoubleExceedingLimit() {
    assertThrows(JsonParseException.class, () ->
        numericFieldValidationUtil.validateDecimalPlace(123.4567, Optional.of(3)));
  }

  @Test
  void validateDecimalPlaceWithDoubleNoLimit() {
    assertDoesNotThrow(() ->
        numericFieldValidationUtil.validateDecimalPlace(123.4567, Optional.empty()));
  }

  @Test
  void validateDecimalPlaceWithBigDecimalWithinLimit() {
    assertDoesNotThrow(() ->
        numericFieldValidationUtil.validateDecimalPlace(new BigDecimal("123.456"), Optional.of(3)));
  }

  @Test
  void validateDecimalPlaceWithBigDecimalExceedingLimit() {
    assertThrows(JsonParseException.class, () ->
        numericFieldValidationUtil.validateDecimalPlace(new BigDecimal("123.4567"), Optional.of(3)));
  }

  @Test
  void validateDecimalPlaceWithBigDecimalNoLimit() {
    assertDoesNotThrow(() ->
        numericFieldValidationUtil.validateDecimalPlace(new BigDecimal("123.4567"), Optional.empty()));
  }
}
