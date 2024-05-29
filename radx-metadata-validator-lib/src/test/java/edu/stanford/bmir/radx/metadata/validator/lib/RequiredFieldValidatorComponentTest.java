package edu.stanford.bmir.radx.metadata.validator.lib;

import edu.stanford.bmir.radx.metadata.validator.lib.validators.RequiredFieldValidatorComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.metadatacenter.artifacts.model.core.*;
import org.metadatacenter.artifacts.model.visitors.TemplateReporter;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.net.URI;
import java.net.URISyntaxException;
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
    FieldSchemaArtifact textFieldSchemaArtifact = TextField.builder()
        .withName(textFieldName)
        .withRequiredValue(true)
        .build();

    TemplateSchemaArtifact templateSchemaArtifact = TemplateSchemaArtifact.builder()
        .withName(templateName)
        .withFieldSchema(textFieldSchemaArtifact)
        .build();

    var valueConstraintsReporter = new TemplateReporter(templateSchemaArtifact);

    String fieldPath = "/" + textFieldName;

    FieldValues fieldValues = new FieldValues(
        List.of(), Optional.empty(), Optional.empty(), Optional.empty());
    Map<String, FieldValues> values = new HashMap<>();
    values.put(fieldPath, fieldValues);

    when(valuesReporter.getValues()).thenReturn(values);

    requiredFieldValidatorComponent.validate(valueConstraintsReporter, valuesReporter, handler);

    assertEquals(1, results.size());
  }

  @Test
  void testValidateRequiredTextFieldEmptyString() {
    String textFieldName = "text field";
    String templateName = "My template";
    FieldSchemaArtifact textFieldSchemaArtifact = TextField.builder()
        .withName(textFieldName)
        .withRequiredValue(true)
        .build();

    TemplateSchemaArtifact templateSchemaArtifact = TemplateSchemaArtifact.builder()
        .withName(templateName)
        .withFieldSchema(textFieldSchemaArtifact)
        .build();

    var valueConstraintsReporter = new TemplateReporter(templateSchemaArtifact);

    String fieldPath = "/" + textFieldName;

    FieldValues fieldValues = new FieldValues(
        List.of(), Optional.empty(), Optional.of(""), Optional.empty());
    Map<String, FieldValues> values = new HashMap<>();
    values.put(fieldPath, fieldValues);

    when(valuesReporter.getValues()).thenReturn(values);

    requiredFieldValidatorComponent.validate(valueConstraintsReporter, valuesReporter, handler);

    assertEquals(1, results.size());
  }

  @Test
  void testValidateRequiredTextFieldPresent() {
    String textFieldName = "text field";
    String templateName = "My template";
    FieldSchemaArtifact textFieldSchemaArtifact = TextField.builder()
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

    FieldValues fieldValues = new FieldValues(
        List.of(), Optional.empty(), Optional.of(textFieldValue), Optional.empty());
    Map<String, FieldValues> values = new HashMap<>();
    values.put(fieldPath, fieldValues);

    when(valuesReporter.getValues()).thenReturn(values);

    requiredFieldValidatorComponent.validate(valueConstraintsReporter, valuesReporter, handler);

    assertEquals(0, results.size());
  }

  @Test
  void testValidateRequiredLinkFieldMissing() {
    String linkFieldName = "link field";
    String templateName = "My template";
    FieldSchemaArtifact linkFieldSchemaArtifact = LinkField.builder()
        .withName(linkFieldName)
        .withRequiredValue(true)
        .build();

    TemplateSchemaArtifact templateSchemaArtifact = TemplateSchemaArtifact.builder()
        .withName(templateName)
        .withFieldSchema(linkFieldSchemaArtifact)
        .build();

    var valueConstraintsReporter = new TemplateReporter(templateSchemaArtifact);

    String fieldPath = "/" + linkFieldName;

    FieldValues fieldValues = new FieldValues(
        List.of(), Optional.empty(), Optional.empty(), Optional.empty());
    Map<String, FieldValues> values = new HashMap<>();
    values.put(fieldPath, fieldValues);

    when(valuesReporter.getValues()).thenReturn(values);

    requiredFieldValidatorComponent.validate(valueConstraintsReporter, valuesReporter, handler);

    assertEquals(1, results.size());
  }

  @Test
  void testValidateRequiredLinkFieldemptyString() throws URISyntaxException {
    String linkFieldName = "link field";
    String templateName = "My template";
    FieldSchemaArtifact linkFieldSchemaArtifact = LinkField.builder()
        .withName(linkFieldName)
        .withRequiredValue(true)
        .build();

    TemplateSchemaArtifact templateSchemaArtifact = TemplateSchemaArtifact.builder()
        .withName(templateName)
        .withFieldSchema(linkFieldSchemaArtifact)
        .build();

    var valueConstraintsReporter = new TemplateReporter(templateSchemaArtifact);

    String fieldPath = "/" + linkFieldName;

    FieldValues fieldValues = new FieldValues(
        List.of(), Optional.of(new URI("")), Optional.empty(), Optional.empty());
    Map<String, FieldValues> values = new HashMap<>();
    values.put(fieldPath, fieldValues);

    when(valuesReporter.getValues()).thenReturn(values);

    requiredFieldValidatorComponent.validate(valueConstraintsReporter, valuesReporter, handler);

    assertEquals(1, results.size());
  }

  @Test
  void testValidateRequiredLinkFieldPresent() throws URISyntaxException {
    String linkFieldName = "link field";
    String templateName = "My template";
    FieldSchemaArtifact linkFieldSchemaArtifact = LinkField.builder()
        .withName(linkFieldName)
        .withRequiredValue(true)
        .build();

    TemplateSchemaArtifact templateSchemaArtifact = TemplateSchemaArtifact.builder()
        .withName(templateName)
        .withFieldSchema(linkFieldSchemaArtifact)
        .build();

    var valueConstraintsReporter = new TemplateReporter(templateSchemaArtifact);

    String fieldPath = "/" + linkFieldName;

    FieldValues fieldValues = new FieldValues(
        List.of(), Optional.of(new URI("https://example.com")), Optional.empty(), Optional.empty());
    Map<String, FieldValues> values = new HashMap<>();
    values.put(fieldPath, fieldValues);

    when(valuesReporter.getValues()).thenReturn(values);

    requiredFieldValidatorComponent.validate(valueConstraintsReporter, valuesReporter, handler);

    assertEquals(0, results.size());
  }

  @Test
  void testValidateRequiredControlledTermsFieldPresent() throws URISyntaxException {
    String fieldName = "controlled terms field";
    String templateName = "My template";
    FieldSchemaArtifact linkFieldSchemaArtifact = ControlledTermField.builder()
        .withName(fieldName)
        .withRequiredValue(true)
        .build();

    TemplateSchemaArtifact templateSchemaArtifact = TemplateSchemaArtifact.builder()
        .withName(templateName)
        .withFieldSchema(linkFieldSchemaArtifact)
        .build();

    var valueConstraintsReporter = new TemplateReporter(templateSchemaArtifact);

    String fieldPath = "/" + fieldName;

    FieldValues fieldValues = new FieldValues(
        List.of(), Optional.of(new URI("http://purl.bioontology.org/ontology/LNC/LP286655-8")), Optional.empty(), Optional.of("Neutrophils/leukocytes"));
    Map<String, FieldValues> values = new HashMap<>();
    values.put(fieldPath, fieldValues);

    when(valuesReporter.getValues()).thenReturn(values);

    requiredFieldValidatorComponent.validate(valueConstraintsReporter, valuesReporter, handler);

    assertEquals(0, results.size());
  }

  @Test
  void testValidateRequiredControlledTermsFieldIdMissing() {
    String fieldName = "controlled terms field";
    String templateName = "My template";
    FieldSchemaArtifact linkFieldSchemaArtifact = ControlledTermField.builder()
        .withName(fieldName)
        .withRequiredValue(true)
        .build();

    TemplateSchemaArtifact templateSchemaArtifact = TemplateSchemaArtifact.builder()
        .withName(templateName)
        .withFieldSchema(linkFieldSchemaArtifact)
        .build();

    var valueConstraintsReporter = new TemplateReporter(templateSchemaArtifact);

    String fieldPath = "/" + fieldName;

    FieldValues fieldValues = new FieldValues(
        List.of(), Optional.empty(), Optional.empty(), Optional.of("Neutrophils/leukocytes"));
    Map<String, FieldValues> values = new HashMap<>();
    values.put(fieldPath, fieldValues);

    when(valuesReporter.getValues()).thenReturn(values);

    requiredFieldValidatorComponent.validate(valueConstraintsReporter, valuesReporter, handler);

    assertEquals(1, results.size());
  }

  @Test
  void testValidateRequiredControlledTermsFieldLabelMissing() throws URISyntaxException {
    String fieldName = "controlled terms field";
    String templateName = "My template";
    FieldSchemaArtifact linkFieldSchemaArtifact = ControlledTermField.builder()
        .withName(fieldName)
        .withRequiredValue(true)
        .build();

    TemplateSchemaArtifact templateSchemaArtifact = TemplateSchemaArtifact.builder()
        .withName(templateName)
        .withFieldSchema(linkFieldSchemaArtifact)
        .build();

    var valueConstraintsReporter = new TemplateReporter(templateSchemaArtifact);

    String fieldPath = "/" + fieldName;

    FieldValues fieldValues = new FieldValues(
        List.of(), Optional.of(new URI("http://purl.bioontology.org/ontology/LNC/LP286655-8")), Optional.empty(), Optional.empty());
    Map<String, FieldValues> values = new HashMap<>();
    values.put(fieldPath, fieldValues);

    when(valuesReporter.getValues()).thenReturn(values);

    requiredFieldValidatorComponent.validate(valueConstraintsReporter, valuesReporter, handler);

    assertEquals(1, results.size());
  }
}
