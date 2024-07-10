package edu.stanford.bmir.radx.metadata.validator.lib.evaluators;

import edu.stanford.bmir.radx.metadata.validator.lib.*;
import org.metadatacenter.artifacts.model.core.ElementSchemaArtifact;
import org.metadatacenter.artifacts.model.core.TemplateSchemaArtifact;
import org.metadatacenter.artifacts.model.core.fields.constraints.ValueConstraints;
import org.metadatacenter.artifacts.model.visitors.TemplateReporter;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Consumer;

import static edu.stanford.bmir.radx.metadata.validator.lib.evaluators.EvaluationConstant.*;

@Component
public class CompletenessEvaluator {
  private final AttributeValueValidationUtil attributeValueValidationUtil;
  private final FieldsCollector fieldsCollector = new FieldsCollector();


  public CompletenessEvaluator(AttributeValueValidationUtil attributeValueValidationUtil) {
    this.attributeValueValidationUtil = attributeValueValidationUtil;
  }

  public void evaluate(TemplateSchemaArtifact templateSchemaArtifact, TemplateInstanceValuesReporter templateInstanceValuesReporter, Consumer<EvaluationResult> handler){
    var templateReporter = new TemplateReporter(templateSchemaArtifact);
    var values = templateInstanceValuesReporter.getValues();
    var attributeValueFields = templateInstanceValuesReporter.getAttributeValueFields();
    var allFields = fieldsCollector.getAllFields(templateSchemaArtifact);

    HashSet<String> filledRequiredFields = new HashSet<>();
    HashSet<String> filledRecommendedFields = new HashSet<>();
    HashSet<String> filledOptionalFields = new HashSet<>();
    HashSet<String> filledElements = new HashSet<>();
    HashSet<String> checkedFields = new HashSet<>();

    int requiredFieldCount = 0;
    int recommendedFieldCount = 0;
    int optionalFieldCount = 0;

    for(var field:allFields){
      var fieldConstraint = templateReporter.getValueConstraints(field);
      if (isRequiredField(fieldConstraint)) {
        requiredFieldCount++;
      } else if (isRecommendedField(field)) {
        recommendedFieldCount++;
      } else{
        optionalFieldCount++;
      }
    }

    for (Map.Entry<String, FieldValues> fieldEntry : values.entrySet()) {
      var path = fieldEntry.getKey();
      var normalizedPath = path.replaceAll("\\[\\d+\\]", "");
      var value = fieldEntry.getValue();
      var fieldConstraint = templateReporter.getValueConstraints(normalizedPath);
      if(!checkedFields.contains(normalizedPath) && !fieldsCollector.isEmptyField(value)){
        if (isRequiredField(fieldConstraint)) {
          filledRequiredFields.add(normalizedPath);
        } else if (isRecommendedField(normalizedPath) ) {
          filledRecommendedFields.add(normalizedPath);
        } else {
          filledOptionalFields.add(normalizedPath);
        }
        checkedFields.add(normalizedPath);
        filledElements.add(getParentElement(normalizedPath));
      }
    }

    int filledRequiredFieldCount = filledRequiredFields.size();
    int filledRecommendedFieldCount = filledRecommendedFields.size();
    int filledAvFieldsCount = filledAvFields(attributeValueFields);
    int filledOptionalFieldCount = filledOptionalFields.size() + filledAvFieldsCount;

    var requiredCompleteness = ((double)filledRequiredFieldCount/requiredFieldCount) * 100;
    var recommendedCompleteness = ((double)filledRecommendedFieldCount/recommendedFieldCount) * 100;
    var optionalCompleteness = ((double) filledOptionalFieldCount/ optionalFieldCount) * 100;
    var overallCompleteness = ((double) (filledRequiredFieldCount + filledRecommendedFieldCount + filledOptionalFieldCount)
        /(requiredFieldCount + recommendedFieldCount + optionalFieldCount)) * 100;

    handler.accept(new EvaluationResult(REQUIRED_FIELD_COUNT, requiredFieldCount));
    handler.accept(new EvaluationResult(RECOMMENDED_FIELD_COUNT, recommendedFieldCount));
    handler.accept(new EvaluationResult(OPTIONAL_FIELD_COUNT, optionalFieldCount));
    handler.accept(new EvaluationResult(FILLED_REQUIRED_FILEDS_COUNT, filledRequiredFieldCount));
    handler.accept(new EvaluationResult(FILLED_RECOMMENDED_FIELDS_COUNT,filledRecommendedFieldCount));
    handler.accept(new EvaluationResult(FILLED_OPTIONAL_FIELDS_COUNT, filledOptionalFieldCount));
    handler.accept(new EvaluationResult(REQUIRED_FIELDS_COMPLETENESS, requiredCompleteness));
    handler.accept(new EvaluationResult(RECOMMENDED_FIELDS_COMPLETENESS, recommendedCompleteness));
    handler.accept(new EvaluationResult(OPTIONAL_FIELDS_COMPLETENESS, optionalCompleteness));
    handler.accept(new EvaluationResult(OVERALL_COMPLETENESS, overallCompleteness));
    handler.accept(new EvaluationResult(FILLED_REQUIRED_FIELDS, filledRequiredFields));
    handler.accept(new EvaluationResult(FILLED_RECOMMENDED_FIELDS, filledRecommendedFields));
    handler.accept(new EvaluationResult(FILLED_OPTIONAL_FIELDS, filledOptionalFields));
    handler.accept(new EvaluationResult(FILLED_ELEMENTS, filledElements));
    handler.accept(new EvaluationResult(FILLED_ELEMENTS_COUNT, filledElements.size()));
  }

  private boolean isRequiredField(Optional<ValueConstraints> valueConstraints){
    return valueConstraints.map(ValueConstraints::requiredValue).orElse(false);
  }

  private boolean isRecommendedField(String fieldPath){
    return RecommendedFields.isRecommendedField(fieldPath);
  }

  private int filledAvFields(List<AttributeValueFieldValues> avFields){
    var filledAvFields = new HashSet<String>();
    for(var avField: avFields){
      var fieldValus = avField.fieldValues();
      if(!fieldsCollector.isEmptyField(fieldValus)){
        filledAvFields.add(avField.specificationPath());
      }
    }
    return filledAvFields.size();
  }

  public Collection<String> getEmptyElements(Map<String, Integer> combinedFillingReport, TemplateSchemaArtifact templateSchemaArtifact){
    Set<String> emptyElements = new HashSet<>();
    var childElements = templateSchemaArtifact.getElementNames();
    for(var childElement: childElements){
      var currentElementArtifact = templateSchemaArtifact.getElementSchemaArtifact(childElement);
      if(isEmptyElements(combinedFillingReport, currentElementArtifact, "/" + childElement)){
        emptyElements.add(childElement);
      }
    }
    return emptyElements;
  }

  private boolean isEmptyElements(Map<String, Integer> combinedFillingReport, ElementSchemaArtifact elementSchemaArtifact, String path){
    var childFields = elementSchemaArtifact.getFieldNames();
    var childElements = elementSchemaArtifact.getElementNames();
    for(var childField: childFields){
      var currentPath = path + "/" + childField;
      System.out.println(currentPath);
      if(combinedFillingReport.containsKey(currentPath) && combinedFillingReport.get(currentPath) != 0){
        return false;
      }
    }

    for(var childElement:childElements){
      var childElementSchemaArtifact = elementSchemaArtifact.getElementSchemaArtifact(childElement);
      var currentPath = path + "/" + childElement;
      if(!isEmptyElements(combinedFillingReport, childElementSchemaArtifact, currentPath)){
        return false;
      }
    }
    return true;
  }

  private String getParentElement(String path){
    return path.split("/")[1];
  }

  private Collection<String> getAllElements(TemplateSchemaArtifact templateSchemaArtifact){
    var childElements = templateSchemaArtifact.getElementNames();
    return new HashSet<>(childElements);
  }
}
