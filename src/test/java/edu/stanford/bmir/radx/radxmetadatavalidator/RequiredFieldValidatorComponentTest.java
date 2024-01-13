package edu.stanford.bmir.radx.radxmetadatavalidator;

import edu.stanford.bmir.radx.radxmetadatavalidator.ValidatorComponents.RequiredFieldValidatorComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.metadatacenter.artifacts.model.core.FieldSchemaArtifact;
import org.metadatacenter.artifacts.model.core.TemplateSchemaArtifact;
import org.metadatacenter.artifacts.model.visitors.TemplateReporter;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class RequiredFieldValidatorComponentTest {
  private RequiredFieldValidatorComponent requiredFieldValidatorComponent;

  @Mock
  private TemplateInstanceValuesReporter valuesReporter;

  private List<ValidationResult> results;

  private Consumer<ValidationResult> handler;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    requiredFieldValidatorComponent = new RequiredFieldValidatorComponent();
    results = new ArrayList<>();
    handler = results::add;
  }

  @Test
  void testValidateRequiredTextFieldMissing() {
    String textFieldName = "text field";
    String templateName = "My template";
    FieldSchemaArtifact textFieldSchemaArtifact = FieldSchemaArtifact.textFieldBuilder()
        .withName(textFieldName)
        .withRequiredValue(true)
        .build();

    TemplateSchemaArtifact templateSchemaArtifact = TemplateSchemaArtifact.builder()
        .withName(templateName)
        .withFieldSchema(textFieldSchemaArtifact)
        .build();

    var valueConstraintsReporter = new TemplateReporter(templateSchemaArtifact);

    String fieldPath = "/" + textFieldName;
    Map<String, Map<SchemaProperties, Optional<?>>> values = new HashMap<>();
    Map<SchemaProperties, Optional<?>> fieldValue = new HashMap<>();
    fieldValue.put(SchemaProperties.VALUE, Optional.empty()); // Simulate missing value
    values.put(fieldPath, fieldValue);

    when(valuesReporter.getValues()).thenReturn(values);

    requiredFieldValidatorComponent.validate(valueConstraintsReporter, valuesReporter, handler);

    assertEquals(1, results.size());
  }

  @Test
  void testValidateRequiredTextFieldPresent() {
    String textFieldName = "text field";
    String templateName = "My template";
    FieldSchemaArtifact textFieldSchemaArtifact = FieldSchemaArtifact.textFieldBuilder()
        .withName(textFieldName)
        .withRequiredValue(true)
        .build();

    TemplateSchemaArtifact templateSchemaArtifact = TemplateSchemaArtifact.builder()
        .withName(templateName)
        .withFieldSchema(textFieldSchemaArtifact)
        .build();

    var valueConstraintsReporter = new TemplateReporter(templateSchemaArtifact);

    String fieldPath = "/" + textFieldName;
    String textFieldValue = "text value";
    Map<String, Map<SchemaProperties, Optional<?>>> values = new HashMap<>();
    Map<SchemaProperties, Optional<?>> fieldValue = new HashMap<>();
    fieldValue.put(SchemaProperties.VALUE, Optional.of(textFieldValue));
    values.put(fieldPath, fieldValue);

    when(valuesReporter.getValues()).thenReturn(values);

    requiredFieldValidatorComponent.validate(valueConstraintsReporter, valuesReporter, handler);

    assertEquals(0, results.size());
  }

  @Test
  void testValidateRequiredLinkFieldMissing() {
    String linkFieldName = "link field";
    String templateName = "My template";
    FieldSchemaArtifact linkFieldSchemaArtifact = FieldSchemaArtifact.linkFieldBuilder()
        .withName(linkFieldName)
        .withRequiredValue(true)
        .build();

    TemplateSchemaArtifact templateSchemaArtifact = TemplateSchemaArtifact.builder()
        .withName(templateName)
        .withFieldSchema(linkFieldSchemaArtifact)
        .build();

    var valueConstraintsReporter = new TemplateReporter(templateSchemaArtifact);

    String fieldPath = "/" + linkFieldName;
    Map<String, Map<SchemaProperties, Optional<?>>> values = new HashMap<>();
    Map<SchemaProperties, Optional<?>> fieldValue = new HashMap<>();
    fieldValue.put(SchemaProperties.ID, Optional.empty());
    values.put(fieldPath, fieldValue);

    when(valuesReporter.getValues()).thenReturn(values);

    requiredFieldValidatorComponent.validate(valueConstraintsReporter, valuesReporter, handler);

    assertEquals(1, results.size());
  }

  @Test
  void testValidateRequiredLinkFieldPresent() {
    String linkFieldName = "link field";
    String templateName = "My template";
    FieldSchemaArtifact linkFieldSchemaArtifact = FieldSchemaArtifact.linkFieldBuilder()
        .withName(linkFieldName)
        .withRequiredValue(true)
        .build();

    TemplateSchemaArtifact templateSchemaArtifact = TemplateSchemaArtifact.builder()
        .withName(templateName)
        .withFieldSchema(linkFieldSchemaArtifact)
        .build();

    var valueConstraintsReporter = new TemplateReporter(templateSchemaArtifact);

    String fieldPath = "/" + linkFieldName;
    Map<String, Map<SchemaProperties, Optional<?>>> values = new HashMap<>();
    Map<SchemaProperties, Optional<?>> fieldValue = new HashMap<>();
    fieldValue.put(SchemaProperties.ID, Optional.of("https://example.com"));
    values.put(fieldPath, fieldValue);

    when(valuesReporter.getValues()).thenReturn(values);

    requiredFieldValidatorComponent.validate(valueConstraintsReporter, valuesReporter, handler);

    assertEquals(0, results.size());
  }

  @Test
  void testValidateRequiredControlledTermsFieldPresent() {
    String fieldName = "controlled terms field";
    String templateName = "My template";
    FieldSchemaArtifact linkFieldSchemaArtifact = FieldSchemaArtifact.controlledTermFieldBuilder()
        .withName(fieldName)
        .withRequiredValue(true)
        .build();

    TemplateSchemaArtifact templateSchemaArtifact = TemplateSchemaArtifact.builder()
        .withName(templateName)
        .withFieldSchema(linkFieldSchemaArtifact)
        .build();

    var valueConstraintsReporter = new TemplateReporter(templateSchemaArtifact);

    String fieldPath = "/" + fieldName;
    Map<String, Map<SchemaProperties, Optional<?>>> values = new HashMap<>();
    Map<SchemaProperties, Optional<?>> fieldValue = new HashMap<>();
    fieldValue.put(SchemaProperties.ID, Optional.of("http://purl.bioontology.org/ontology/LNC/LP286655-8"));
    fieldValue.put(SchemaProperties.LABEL, Optional.of("Neutrophils/leukocytes"));
    values.put(fieldPath, fieldValue);

    when(valuesReporter.getValues()).thenReturn(values);

    requiredFieldValidatorComponent.validate(valueConstraintsReporter, valuesReporter, handler);

    assertEquals(0, results.size());
  }

  @Test
  void testValidateRequiredControlledTermsFieldIdMissing() {
    String fieldName = "controlled terms field";
    String templateName = "My template";
    FieldSchemaArtifact linkFieldSchemaArtifact = FieldSchemaArtifact.controlledTermFieldBuilder()
        .withName(fieldName)
        .withRequiredValue(true)
        .build();

    TemplateSchemaArtifact templateSchemaArtifact = TemplateSchemaArtifact.builder()
        .withName(templateName)
        .withFieldSchema(linkFieldSchemaArtifact)
        .build();

    var valueConstraintsReporter = new TemplateReporter(templateSchemaArtifact);

    String fieldPath = "/" + fieldName;
    Map<String, Map<SchemaProperties, Optional<?>>> values = new HashMap<>();
    Map<SchemaProperties, Optional<?>> fieldValue = new HashMap<>();
    fieldValue.put(SchemaProperties.ID, Optional.empty());
    fieldValue.put(SchemaProperties.LABEL, Optional.of("Neutrophils/leukocytes"));
    values.put(fieldPath, fieldValue);

    when(valuesReporter.getValues()).thenReturn(values);

    requiredFieldValidatorComponent.validate(valueConstraintsReporter, valuesReporter, handler);

    assertEquals(1, results.size());
  }

  @Test
  void testValidateRequiredControlledTermsFieldLabelMissing() {
    String fieldName = "controlled terms field";
    String templateName = "My template";
    FieldSchemaArtifact linkFieldSchemaArtifact = FieldSchemaArtifact.controlledTermFieldBuilder()
        .withName(fieldName)
        .withRequiredValue(true)
        .build();

    TemplateSchemaArtifact templateSchemaArtifact = TemplateSchemaArtifact.builder()
        .withName(templateName)
        .withFieldSchema(linkFieldSchemaArtifact)
        .build();

    var valueConstraintsReporter = new TemplateReporter(templateSchemaArtifact);

    String fieldPath = "/" + fieldName;
    Map<String, Map<SchemaProperties, Optional<?>>> values = new HashMap<>();
    Map<SchemaProperties, Optional<?>> fieldValue = new HashMap<>();
    fieldValue.put(SchemaProperties.ID, Optional.of("http://purl.bioontology.org/ontology/LNC/LP286655-8"));
    fieldValue.put(SchemaProperties.LABEL, Optional.empty());
    values.put(fieldPath, fieldValue);

    when(valuesReporter.getValues()).thenReturn(values);

    requiredFieldValidatorComponent.validate(valueConstraintsReporter, valuesReporter, handler);

    assertEquals(1, results.size());
  }
}
