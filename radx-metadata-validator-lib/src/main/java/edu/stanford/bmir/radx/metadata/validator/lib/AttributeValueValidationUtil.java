package edu.stanford.bmir.radx.metadata.validator.lib;

import org.metadatacenter.artifacts.model.visitors.TemplateReporter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Consumer;

@Component
public class AttributeValueValidationUtil {
  private final FieldSchemaValidationHelper fieldSchemaValidationHelper = new FieldSchemaValidationHelper();
  private boolean isAttributeValue(TemplateReporter templateReporter, String specificationPath){
    var attributeValueInstanceArtifact = templateReporter.getFieldSchema(specificationPath);
    return attributeValueInstanceArtifact.map(fieldSchemaArtifact -> fieldSchemaArtifact.fieldUi().isAttributeValue()).orElse(false);
  }

  public void validateAttributeValueField(TemplateReporter templateReporter, List<AttributeValueFieldValues> attributeValueFieldValuesList, Consumer<ValidationResult> handler){
    for(var attributeValueFieldValues: attributeValueFieldValuesList){
      String instancePath = attributeValueFieldValues.path();
      String specificationPath = attributeValueFieldValues.specificationPath();
      var fieldValues = attributeValueFieldValues.fieldValues();

      if(isAttributeValue(templateReporter, specificationPath)){
        if(fieldValues.jsonLdValue().isPresent()){
          fieldSchemaValidationHelper.validateAttributeValueField(fieldValues.jsonLdValue(), fieldValues.jsonLdId(), fieldValues.label(), fieldValues.jsonLdTypes(), handler, instancePath);
        } else{
          handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, "@value should not be null", instancePath));
        }
      } else{
      handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.DATA_TYPE_VALIDATION, "It is not a attribute-value field", instancePath));
      }
      }
    }
  }
