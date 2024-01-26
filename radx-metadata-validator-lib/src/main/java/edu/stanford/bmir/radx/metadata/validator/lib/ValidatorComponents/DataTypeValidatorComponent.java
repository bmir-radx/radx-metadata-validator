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
  private final AttributeValueValidationUtil attributeValueValidationUtil;
  private final FieldSchemaValidationHelper fieldSchemaValidationHelper;

  public DataTypeValidatorComponent(TextFieldValidationUtil textFieldValidationUtil, NumericFieldValidationUtil numericFieldValidationUtil, AttributeValueValidationUtil attributeValueValidationUtil, FieldSchemaValidationHelper fieldSchemaValidationHelper) {
    this.textFieldValidationUtil = textFieldValidationUtil;
    this.numericFieldValidationUtil = numericFieldValidationUtil;
    this.attributeValueValidationUtil = attributeValueValidationUtil;
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
          //if it is text field, validate regex, length, schema
        } else if (fieldInputType == FieldInputType.TEXTFIELD){
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
            validateLinkField(id.get().toString(), handler, path);
            fieldSchemaValidationHelper.validateLinkField(value, label, type, handler, path);
          }
        //if it is multiple choice, list field, or checkbox field, validate literals
        } else if (fieldInputType == FieldInputType.RADIO || fieldInputType == FieldInputType.CHECKBOX || fieldInputType == FieldInputType.LIST) {
          if(value.isPresent()){
            validateLiterals(value.get(), valueConstraint.get(), handler, path);
            fieldSchemaValidationHelper.validateTextField(id, label, type, handler, path);
          }
        }
      }
    }

    //if it is attribute value, validate the schema
    attributeValueValidationUtil.validateAttributeValueField(templateReporter, valuesReporter.getAttributeValueFields(), handler);
  }

  public void validateTextField(String value, ValueConstraints valueConstraint, Consumer<ValidationResult> handler, String path){
    var textConstraint = valueConstraint.asTextValueConstraints();
    //validate regex
    if(!textFieldValidationUtil.matchRegex(textConstraint.regex(), value)){
      String message = String.format("\"%s\" does not follow the regex (%s)", value, textConstraint.regex().orElse(""));
      handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, message, path));
    }
    //validate length range
    var length = value.length();
    if(!textFieldValidationUtil.lengthIsInRange(textConstraint.minLength(), textConstraint.maxLength(), length)){
      String message = String.format("Input string length (%d) is out of range [%d, %d]", length, textConstraint.minLength().orElse(0), textConstraint.maxLength().orElse(Integer.MAX_VALUE));
      handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, message, path));
    }
  }

  public void validateLiterals(String value, ValueConstraints valueConstraint, Consumer<ValidationResult> handler, String path){
    var literals = valueConstraint.asTextValueConstraints().literals();
      if(literals.size() > 0){
        if(!textFieldValidationUtil.validLiteral(literals, value)){
          String message = String.format("Provided value \"%s\" does not exist in the given list", value);
          handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, message, path));
        }
      }
  }

  public void validateNumericField(String value, List<URI> type, ValueConstraints valueConstraint, Consumer<ValidationResult> handler, String path) {
    var numericConstraint = valueConstraint.asNumericValueConstraints();
    //validate it's a valid number
    if(numericFieldValidationUtil.isValidNumber(value)){
      //validate data type
      try {
        numericFieldValidationUtil.validateNumericType(value, numericConstraint.numberType(), numericConstraint.decimalPlace());
      } catch (NumberFormatException e){
        String errorMessage = String.format("Input value \"%s\" is not consistent with numeric data type %s.", value, numericConstraint.numberType());
        handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, errorMessage, path));
      } catch (JsonParseException e) {
        handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, e.getMessage(), path));
      }
      //validate range
      if(!numericFieldValidationUtil.numberIsInRange(numericConstraint.minValue(), numericConstraint.maxValue(), value)){
        String errorMessage = String.format("Input value \"%s\" must be between [%s, %s].", value, numericConstraint.minValue().orElse(Double.NEGATIVE_INFINITY), numericConstraint.maxValue().orElse(Double.POSITIVE_INFINITY));
        handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, errorMessage, path));
      }
    }else {
      String errorMessage = String.format("Invalid numeric input: \"%s\" is not a valid number.", value);
      handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, errorMessage, path));
    }

    //validate @type value
    if(type.size() > 0){
      validateTypeValue(type.get(0), numericConstraint.numberType().getText(), handler, path);
    } else{
      handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, "@type is expected in the numeric field.", path));
    }
  }


  private boolean isValidDate(String value) {
    try {
      DateTimeFormatter.ISO_LOCAL_DATE.parse(value);
      return true;
      //TODO: only allow format yyyy-MM-dd (2024-01-01), don't allow 2024/01/01?
    } catch (DateTimeParseException e) {
      return false;
    }
  }

  private boolean isValidDateTime(String value) {
    try {
      OffsetDateTime.parse(value, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
      return true;
    } catch (DateTimeParseException e) {
      return false;
    }
  }

  private boolean isValidTime(String value) {
    try {
      OffsetTime.parse(value, DateTimeFormatter.ISO_OFFSET_TIME);
      return true;
    } catch (DateTimeParseException e) {
      return false;
    }
  }

  public void validateTemporalField(String value, List<URI> type, ValueConstraints valueConstraint, Consumer<ValidationResult> handler, String path){
    var temporalConstraint = valueConstraint.asTemporalValueConstraints();
    var temporalDatatype = temporalConstraint.temporalType();
    //validate @value value
    switch (temporalDatatype) {
      case DATE:
        if(!isValidDate(value)){
          String message = String.format("Input value \"%s\" is not a valid Date", value);
          handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, message, path));
        }
        break;
      case DATETIME:
        if(!isValidDateTime(value)){
          String message = String.format("Input value \"%s\" is not a valid DateTime", value);
          handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, message, path));
        }
        break;
      case TIME:
        if(!isValidTime(value)){
          String message = String.format("Input value \"%s\" is not a valid Time", value);
          handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, message, path));
        }
        break;
    }


    //validate @type value
    if (type.size() > 0){
      validateTypeValue(type.get(0), temporalDatatype.getText(), handler, path);
    } else{
      handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, "@type is expected in the numeric field", path));
    }
  }

  public void validateLinkField(String id, Consumer<ValidationResult> handler, String path) {
    try {
      new URL(id);
    } catch (MalformedURLException e) {
      var message = String.format("Input value \"%s\" is not a valid URL", id);
      handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, message, path));
    }
  }

  public void validateTypeValue(URI inputType, String typeConstraint, Consumer<ValidationResult> handler, String path){
    String semanticInputType = inputType.toString().replace(Constants.XSD_IRI, Constants.XSD_PREFIX);
    if(!semanticInputType.equals(typeConstraint)){
      String message = String.format("Expected \"%s\" for @type, but \"%s\" is given", typeConstraint, semanticInputType);
      handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, message, path));
    }
  }

  public void validateControlledTermsField(Optional<URI> id, Consumer<ValidationResult> handler, String path){
    // Check if id is not null and a valid URI
    if (id.isPresent()) {
      try {
        new URI(id.get().toString());
      } catch (URISyntaxException e) {
        String message = String.format("Input @id \"%s\" is not a valid URI", id.get());
        handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, message, path));
      }
    }
  }

  public void validateEmailField(String value, Consumer<ValidationResult> handler, String path){
    String email_regex =
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
    Pattern email_pattern = Pattern.compile(email_regex);
    Matcher matcher = email_pattern.matcher(value);
    if(!matcher.matches()){
      String message = String.format("Input value \"%s\" is not a valid email", value);
      handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, message, path));
    }
  }
}
