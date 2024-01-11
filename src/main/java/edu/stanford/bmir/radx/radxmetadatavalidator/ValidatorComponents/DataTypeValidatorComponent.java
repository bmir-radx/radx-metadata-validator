package edu.stanford.bmir.radx.radxmetadatavalidator.ValidatorComponents;

import edu.stanford.bmir.radx.radxmetadatavalidator.*;
import org.metadatacenter.artifacts.model.core.fields.constraints.ValueConstraints;
import org.metadatacenter.artifacts.model.visitors.TemplateReporter;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.function.Consumer;

@Component
public class DataTypeValidatorComponent {
  private final TextFieldValidationUtil textFieldValidationUtil;
  private final NumericFieldValidationUtil numericFieldValidationUtil;

  public DataTypeValidatorComponent(TextFieldValidationUtil textFieldValidationUtil, NumericFieldValidationUtil numericFieldValidationUtil) {
    this.textFieldValidationUtil = textFieldValidationUtil;
    this.numericFieldValidationUtil = numericFieldValidationUtil;
  }

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
          //If it is numeric filed, validate range, data type, decimal place and @type value
          } else if (valueConstraint.get().isNumericValueConstraint()) {
            validateNumericField(value, type, valueConstraint.get(), handler, path);
          //If it is temporal field, validate temporal data type and @type value
          } else if (valueConstraint.get().isTemporalValueConstraint()){
            validateTemporalField(value, type, valueConstraint.get(), handler, path);
          }
        }

        //If it is link field, validate input is valid URL
        var id = fieldValue.get(SchemaProperties.ID);
        if (id != null){
          if (valueConstraint.get().isLinkValueConstraint()){
            validateLinkField(id.toString(), handler, path);
          }
        }

        if (valueConstraint.get().isControlledTermValueConstraint()){
          //TODO: validate controlled terms
//          var controlledTermConstraint = valueConstraint.get().asControlledTermValueConstraints();
          validateControlledTermsField(fieldValue, handler, path);
        }
      }
    }
  }

  public void validateTextField(Object value, ValueConstraints valueConstraint, Consumer<ValidationResult> handler, String path){
    var textConstraint = valueConstraint.asTextValueConstraints();
    if(value instanceof String textValue){
      var match = textFieldValidationUtil.matchRegex(textConstraint.regex(), textValue);
      //validate regex
      if(!textFieldValidationUtil.matchRegex(textConstraint.regex(), textValue)){
        var regex = textConstraint.regex();

        String message = String.format("%s does not follow the regex (%s)", textValue, textConstraint.regex().orElse(""));
        handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, message, path));
      }
      //validate length range
      var length = textValue.length();
      if(!textFieldValidationUtil.lengthIsInRange(textConstraint.minLength(), textConstraint.maxLength(), length)){
        var inRange = textFieldValidationUtil.lengthIsInRange(textConstraint.minLength(), textConstraint.maxLength(), length);
        String message = String.format("Input string length (%d) is out of range [%d, %d]", length, textConstraint.minLength().orElse(0), textConstraint.maxLength().orElse(Integer.MAX_VALUE));
        handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, message, path));
      }
      //validate literals
      var literals = textConstraint.literals();
      if(literals.size() > 0){
        if(!textFieldValidationUtil.validLiteral(literals, textValue)){
          String message = String.format("%s does not exist in the given list: %s", textValue, literals);
          handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, message, path));
        }
      }
    } else { //The value is not a string
      handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, "Expected a value of type String", path));
    }
  }


  public void validateNumericField(Object value, Object type, ValueConstraints valueConstraint, Consumer<ValidationResult> handler, String path) {
    var numericConstraint = valueConstraint.asNumericValueConstraints();
    if (value instanceof String numericValueString) {
      //validate it's a valid number
      if(numericFieldValidationUtil.isValidNumber(numericValueString)){
        //validate data type
        try {
          numericFieldValidationUtil.validateNumericType(numericValueString, numericConstraint.numberType(), numericConstraint.decimalPlace());
        } catch (NumberFormatException e){
          String errorMessage = String.format("Input value %s is not consistent with numeric data type %s", value, numericConstraint.numberType());
          handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, errorMessage, path));
        } catch (JsonParseException e) {
          handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, e.getMessage(), path));
        }
        //validate range
        if(!numericFieldValidationUtil.numberIsInRange(numericConstraint.minValue(), numericConstraint.maxValue(), numericValueString)){
          String errorMessage = String.format("Input value must be between [%s, %s].", numericConstraint.minValue(), numericConstraint.maxValue());
          handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, errorMessage, path));
        }
      }else {
        String errorMessage = String.format("Invalid numeric input: %s is not a valid number", value);
        handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, errorMessage, path));
      }
    } else {
      handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, "Expected a value of type String for @value", path));
    }

    //validate @type value
    if (type instanceof String numbericTypeString) {
      validateTypeValue(numbericTypeString, numericConstraint.numberType().getText(), handler, path);
    } else {
      handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, "Expected a value of type String for @type", path));
    }
  }


  private boolean isValidDate(String value) {
    try {
      DateTimeFormatter.ISO_LOCAL_DATE.parse(value);
      return true;
      //TODO: only allow format yyyy-MM-dd (2024-01-01), don't allow 2024/01/01?
    } catch (DateTimeParseException e) {
//      throw new JsonParseException("Input value %s is not a valid Date");
      return false;
    }
  }

  private boolean isValidDateTime(String value) {
    try {
      OffsetDateTime.parse(value, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
      return true;
    } catch (DateTimeParseException e) {
//      throw new JsonParseException("Input value %s is not a valid DateTime");
      return false;
    }
  }

  private boolean isValidTime(String value) {
    //TODO: return boolean and add throw error at signature
    try {
      OffsetTime.parse(value, DateTimeFormatter.ISO_OFFSET_TIME);
      return true;
    } catch (DateTimeParseException e) {
//      throw new JsonParseException("Input value %s is not a valid Time");
      return false;
    }
  }

  public void validateTemporalField(Object value, Object type, ValueConstraints valueConstraint, Consumer<ValidationResult> handler, String path){
    var temporalConstraint = valueConstraint.asTemporalValueConstraints();
    var temporalDatatype = temporalConstraint.temporalType();
    //validate @value value
    if (value instanceof String temporalValue) {
      switch (temporalDatatype) {
        case DATE:
          if(!isValidDate(temporalValue)){
            handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, "Input value %s is not a valid Date", path));
          }
          break;
        case DATETIME:
          if(!isValidDateTime(temporalValue)){
            handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, "Input value %s is not a valid DateTime", path));
          }
          break;
        case TIME:
          if(!isValidTime(temporalValue)){
            handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, "Input value %s is not a valid Time", path));
          }
          break;
      }
    } else {
      handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, "Expected a value of type String for @value", path));
    }

    //validate @type value
    if(type instanceof String typeValue){
      validateTypeValue(typeValue, temporalDatatype.getText(), handler, path);
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

  private void validateTypeValue(String inputType, String typeConstraint, Consumer<ValidationResult> handler, String path){
    if(!inputType.equals(typeConstraint)){
      String message = String.format("Expected %s for @type", typeConstraint);
      handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, message, path));
    }
  }

  public void validateControlledTermsField(Map<SchemaProperties, Object> fieldValue, Consumer<ValidationResult> handler, String path){
    var id = fieldValue.get(SchemaProperties.ID);
    var label = fieldValue.get(SchemaProperties.LABEL);

    // Check if label is not null
    if (label == null) {
      String message = "\"rdfs:label\" can not be null";
      handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, message, path));
    }

    // Check if id is not null and a valid URI
    if (id != null) {
      try {
        new URI(id.toString());
      } catch (URISyntaxException e) {
        String message = "Input of \"@id\" is not a valid URI";
        handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, message, path));
      }
    } else {
      String message = "\"@id\" can not be null";
      handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, message, path));
    }
  }
}
