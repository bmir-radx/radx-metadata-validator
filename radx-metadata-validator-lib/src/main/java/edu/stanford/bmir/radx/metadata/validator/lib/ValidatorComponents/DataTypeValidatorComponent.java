package edu.stanford.bmir.radx.metadata.validator.lib.ValidatorComponents;

import edu.stanford.bmir.radx.metadata.validator.lib.*;
import org.metadatacenter.artifacts.model.core.fields.FieldInputType;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class DataTypeValidatorComponent {
  private final TextFieldValidationUtil textFieldValidationUtil;
  private final NumericFieldValidationUtil numericFieldValidationUtil;
  private final FieldSchemaValidationHelper fieldSchemaValidationHelper;

  public DataTypeValidatorComponent(TextFieldValidationUtil textFieldValidationUtil, NumericFieldValidationUtil numericFieldValidationUtil, FieldSchemaValidationHelper fieldSchemaValidationHelper) {
    this.textFieldValidationUtil = textFieldValidationUtil;
    this.numericFieldValidationUtil = numericFieldValidationUtil;
    this.fieldSchemaValidationHelper = fieldSchemaValidationHelper;
  }

  public void validate(TemplateReporter templateReporter, TemplateInstanceValuesReporter valuesReporter, Consumer<ValidationResult> handler){
    var values = valuesReporter.getValues();

    for (Map.Entry<String, FieldValues> FieldEntry : values.entrySet()) {
      String path = FieldEntry.getKey();
      FieldValues fieldValues = FieldEntry.getValue();
      var fieldSchemaArtifact = templateReporter.getFieldSchema(path);
      var valueConstraint = templateReporter.getValueConstraints(path);
      if (fieldSchemaArtifact.isPresent() && valueConstraint.isPresent()){
        var fieldInputType = fieldSchemaArtifact.get().fieldUi().inputType();
        var value = fieldValues.jsonLdValue();
        var type = fieldValues.jsonLdTypes();
        var id = fieldValues.jsonLdId();
        var label = fieldValues.label();

        // if it is controlled terms, validate schema and @id is a valid URI
        if (valueConstraint.get().isControlledTermValueConstraint()){
          validateControlledTermsField(id, handler, path);
          fieldSchemaValidationHelper.validateControlledTermField(id, label, value, type, handler, path);
          //if it is text field or paragraph, validate regex, length, schema
        } else if (fieldInputType == FieldInputType.TEXTFIELD || fieldInputType == FieldInputType.TEXTAREA){
          if(value.isPresent()){
            validateTextField(value.get(), valueConstraint.get(), handler, path);
            fieldSchemaValidationHelper.validateTextField(id, label, type, handler, path);
          }
        //if it is numeric field, validate range, data type, decimal place and @type value, schema
        } else if (fieldInputType == FieldInputType.NUMERIC) {
          if(value.isPresent()){
            validateNumericField(value.get(), type, valueConstraint.get(), handler, path);
            fieldSchemaValidationHelper.validateNumericAndTemporalField(id, label, handler, path);
          }
        //if it is temporal field, validate temporal data type and @type value, schema
        } else if (fieldInputType == FieldInputType.TEMPORAL) {
          if(value.isPresent()){
            validateTemporalField(value.get(), type, valueConstraint.get(), handler, path);
            fieldSchemaValidationHelper.validateNumericAndTemporalField(id, label, handler, path);
          }
        //if it is email field, validate regex
        } else if (fieldInputType == FieldInputType.EMAIL){
          if(value.isPresent()){
            validateEmailField(value.get(), handler, path);
            fieldSchemaValidationHelper.validateTextField(id, label, type, handler, path);
          }
        //if it is link field, validate input is valid URL and no @value, rdfs:label and @type
        } else if (fieldInputType == FieldInputType.LINK) {
          if(id.isPresent()){
            validateLinkField(id.toString(), handler, path);
            fieldSchemaValidationHelper.validateLinkField(value, label, type, handler, path);
          }
        //if it is multiple choice, list field, or checkbox field, validate literals
        } else if (fieldInputType == FieldInputType.RADIO || fieldInputType == FieldInputType.CHECKBOX || fieldInputType == FieldInputType.LIST) {
          if(value.isPresent()){
            validateLiterals(value, valueConstraint.get(), handler, path);
            fieldSchemaValidationHelper.validateTextField(id, label, type, handler, path);
          }
        }
        //TODO: if it is phone field
        //TODO:if it is attribute value
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
        String message = String.format("Input string length (%d) is out of range [%d, %d]", length, textConstraint.minLength().orElse(0), textConstraint.maxLength().orElse(Integer.MAX_VALUE));
        handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, message, path));
      }
    } else { //The value is not a string
      handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, "Expected a value of type String", path));
    }
  }

  public void validateLiterals(Object value, ValueConstraints valueConstraint, Consumer<ValidationResult> handler, String path){
    var literals = valueConstraint.asTextValueConstraints().literals();
      if(literals.size() > 0){
        if(!textFieldValidationUtil.validLiteral(literals, value.toString())){
          String message = String.format("%s does not exist in the given list: %s", value, literals);
          handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, message, path));
        }
      }
  }


  public void validateNumericField(Object value, List<URI> type, ValueConstraints valueConstraint, Consumer<ValidationResult> handler, String path) {
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
    if(type.size() > 1){
//      if (type.get() instanceof String numbericTypeString) {
//        validateTypeValue(numbericTypeString, numericConstraint.numberType().getText(), handler, path);
//      } else {
//        handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, "Expected a value of type String for @type", path));
//      }
      validateTypeValue(type.get(0).toString(), numericConstraint.numberType().getText(), handler, path);
    } else{
      handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, "@type is expected in the numeric field", path));
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

  public void validateTemporalField(Object value, List<URI> type, ValueConstraints valueConstraint, Consumer<ValidationResult> handler, String path){
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
    if (type.size() > 0){
//      if(type.get() instanceof String typeValue){
//        validateTypeValue(typeValue, temporalDatatype.getText(), handler, path);
//      }else{
//        handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, "Expected a value of type String for @type", path));
//      }
      validateTypeValue(type.get(0).toString(), temporalDatatype.getText(), handler, path);
    } else{
      handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, "@type is expected in the numeric field", path));
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

  public void validateControlledTermsField(Optional<URI> id, Consumer<ValidationResult> handler, String path){

    // Check if id is not null and a valid URI
    if (id.isPresent()) {
      try {
        new URI(id.toString());
      } catch (URISyntaxException e) {
        String message = "Input of \"@id\" is not a valid URI";
        handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, message, path));
      }
    }
  }

  public void validateEmailField(Object value, Consumer<ValidationResult> handler, String path){
    if(value instanceof String email){
      String email_regex =
          "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
      Pattern email_pattern = Pattern.compile(email_regex);
      Matcher matcher = email_pattern.matcher(email);
      if(!matcher.matches()){
        handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, "The provided email is not a valid email", path));
      }
    } else{
      handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, "Expected a value of type String for @type", path));
    }
  }
}
