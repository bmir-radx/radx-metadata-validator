package edu.stanford.bmir.radx.radxmetadatavalidator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.metadatacenter.artifacts.model.core.fields.constraints.LiteralValueConstraint;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TextFieldValidationUtilTest {

  private TextFieldValidationUtil textFieldValidationUtil;

  @BeforeEach
  void setUp() {
    textFieldValidationUtil = new TextFieldValidationUtil();
  }

  @Test
  void testMatchRegexWhenRegexIsPresentAndMatches() {
    Optional<String> regex = Optional.of("^\\(?([0-9]{3})\\)?[-. ]?([0-9]{3})[-. ]?([0-9]{4})$");
    String textValueString = "1234567890";
    assertTrue(textFieldValidationUtil.matchRegex(regex, textValueString));
  }

  @Test
  void testMatchRegexWhenRegexIsPresentAndDoesNotMatch() {
    Optional<String> regex = Optional.of("^[a-z]+$");
    String textValueString = "123";
    assertFalse(textFieldValidationUtil.matchRegex(regex, textValueString));
  }

  @Test
  void testMatchRegexWhenRegexIsNotPresent() {
    Optional<String> regex = Optional.empty();
    String textValueString = "anytext";
    assertTrue(textFieldValidationUtil.matchRegex(regex, textValueString));
  }

  @Test
  void testLengthIsInRangeWhenBothBoundsPresentAndWithinRange() {
    Optional<Integer> minLength = Optional.of(3);
    Optional<Integer> maxLength = Optional.of(5);
    Integer length = 4;
    assertTrue(textFieldValidationUtil.lengthIsInRange(minLength, maxLength, length));
  }

  @Test
  void testLengthIsInRangeWhenBelowMinLength() {
    Optional<Integer> minLength = Optional.of(5);
    Optional<Integer> maxLength = Optional.of(10);
    Integer length = 4;
    assertFalse(textFieldValidationUtil.lengthIsInRange(minLength, maxLength, length));
  }

  @Test
  void testLengthIsInRangeWhenAboveMaxLength() {
    Optional<Integer> minLength = Optional.of(1);
    Optional<Integer> maxLength = Optional.of(5);
    Integer length = 6;
    assertFalse(textFieldValidationUtil.lengthIsInRange(minLength, maxLength, length));
  }

  @Test
  void testLengthIsInRangeWhenOnlyMinLengthPresent() {
    Optional<Integer> minLength = Optional.of(5);
    Optional<Integer> maxLength = Optional.empty();
    Integer length = 7;
    assertTrue(textFieldValidationUtil.lengthIsInRange(minLength, maxLength, length));
  }

  @Test
  void testLengthIsInRangeWhenOnlyMaxLengthPresent() {
    Optional<Integer> minLength = Optional.empty();
    Optional<Integer> maxLength = Optional.of(5);
    Integer length = 3;
    assertTrue(textFieldValidationUtil.lengthIsInRange(minLength, maxLength, length));
  }

  @Test
  void testValidLiteralWhenMatchExists() {
    var literalValueConstraint_1 = new LiteralValueConstraint("Apple", false);
    var literalValueConstraint_2 = new LiteralValueConstraint("Orange", false);
    var literalValueConstraint_3 = new LiteralValueConstraint("Cherry", false);
    List<LiteralValueConstraint> literals = List.of(literalValueConstraint_1, literalValueConstraint_2, literalValueConstraint_3);
    assertTrue(textFieldValidationUtil.validLiteral(literals, "Apple"));
  }

  @Test
  void testValidLiteralWhenNoMatchExists() {
    var literalValueConstraint_1 = new LiteralValueConstraint("Apple", false);
    var literalValueConstraint_2 = new LiteralValueConstraint("Orange", false);
    var literalValueConstraint_3 = new LiteralValueConstraint("Cherry", false);
    List<LiteralValueConstraint> literals = List.of(literalValueConstraint_1, literalValueConstraint_2, literalValueConstraint_3);
    assertFalse(textFieldValidationUtil.validLiteral(literals, "Cat"));
  }
}
