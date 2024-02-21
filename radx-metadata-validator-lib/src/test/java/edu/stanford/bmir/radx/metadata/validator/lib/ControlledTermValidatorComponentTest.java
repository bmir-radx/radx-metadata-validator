package edu.stanford.bmir.radx.metadata.validator.lib;

import edu.stanford.bmir.radx.metadata.validator.lib.validators.ControlledTermValidatorComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.metadatacenter.artifacts.model.core.FieldSchemaArtifact;
import org.metadatacenter.artifacts.model.core.TemplateSchemaArtifact;
import org.metadatacenter.artifacts.model.visitors.TemplateReporter;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class ControlledTermValidatorComponentTest {
  private ControlledTermValidatorComponent validator;
  private List<ValidationResult> results;
  private Consumer<ValidationResult> consumer;
  @Mock
  private TemplateInstanceValuesReporter valuesReporter;

  @BeforeEach
  void setup() {
    MockitoAnnotations.openMocks(this);
    results = new ArrayList<>();
    consumer = results::add;
  }

  @Test
  void testValidateControlledTermsNoApiKey() throws URISyntaxException {
    validator = new ControlledTermValidatorComponent(null);

    String fieldName = "controlled terms field";
    String templateName = "My template";
    FieldSchemaArtifact linkFieldSchemaArtifact = FieldSchemaArtifact.controlledTermFieldBuilder()
        .withName(fieldName)
        .withBranchValueConstraint(new URI("http://vocab.fairdatacollective.org/gdmt/ContributorRole"),
            "https://bioportal.bioontology.org/ontologies/FDC-GDMT",
            "FDC-GDMT",
            "FDC-GDMT",
            2147483647)
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

    validator.validate(valueConstraintsReporter, valuesReporter, consumer);

    assertEquals(0, results.size());
  }

  @Test
  void testValidateControlledTermsWrongIdWrongPrefLabel() throws URISyntaxException {
    String apiKey = "e94e265d4c3cd623ca8bde96cfc743074196409e345b164f148333dd403c3401";
    validator = new ControlledTermValidatorComponent(apiKey);

    String fieldName = "controlled terms field";
    String templateName = "My template";
    FieldSchemaArtifact linkFieldSchemaArtifact = FieldSchemaArtifact.controlledTermFieldBuilder()
        .withName(fieldName)
        .withBranchValueConstraint(new URI("http://vocab.fairdatacollective.org/gdmt/ContributorRole"),
            "https://bioportal.bioontology.org/ontologies/FDC-GDMT",
            "FDC-GDMT",
            "FDC-GDMT",
            2147483647)
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

    validator.validate(valueConstraintsReporter, valuesReporter, consumer);

    assertEquals(1, results.size());
  }

  @Test
  void testValidateControlledTermsWrongId() throws URISyntaxException {
    String apiKey = "e94e265d4c3cd623ca8bde96cfc743074196409e345b164f148333dd403c3401";
    validator = new ControlledTermValidatorComponent(apiKey);

    String fieldName = "controlled terms field";
    String templateName = "My template";
    FieldSchemaArtifact linkFieldSchemaArtifact = FieldSchemaArtifact.controlledTermFieldBuilder()
        .withName(fieldName)
        .withOntologyValueConstraint(new URI("https://bioportal.bioontology.org/ontologies/MESH"),
            "MESH",
            "MESH")
        .build();

    TemplateSchemaArtifact templateSchemaArtifact = TemplateSchemaArtifact.builder()
        .withName(templateName)
        .withFieldSchema(linkFieldSchemaArtifact)
        .build();

    var valueConstraintsReporter = new TemplateReporter(templateSchemaArtifact);

    String fieldPath = "/" + fieldName;

    FieldValues fieldValues = new FieldValues(
        List.of(), Optional.of(new URI("http://purl.bioontology.org/ontology/MESH/D000086382")), Optional.empty(), Optional.of("COVID-19"));
    Map<String, FieldValues> values = new HashMap<>();
    values.put(fieldPath, fieldValues);

    when(valuesReporter.getValues()).thenReturn(values);

    validator.validate(valueConstraintsReporter, valuesReporter, consumer);

    assertEquals(1, results.size());
  }

  @Test
  void testValidateControlledTermsCorrectIdWrongPrefLabel() throws URISyntaxException {
    String apiKey = "e94e265d4c3cd623ca8bde96cfc743074196409e345b164f148333dd403c3401";
    validator = new ControlledTermValidatorComponent(apiKey);

    String fieldName = "controlled terms field";
    String templateName = "My template";
    FieldSchemaArtifact linkFieldSchemaArtifact = FieldSchemaArtifact.controlledTermFieldBuilder()
        .withName(fieldName)
        .withBranchValueConstraint(new URI("http://vocab.fairdatacollective.org/gdmt/ContributorRole"),
            "https://bioportal.bioontology.org/ontologies/FDC-GDMT",
            "FDC-GDMT",
            "FDC-GDMT",
            2147483647)
        .build();

    TemplateSchemaArtifact templateSchemaArtifact = TemplateSchemaArtifact.builder()
        .withName(templateName)
        .withFieldSchema(linkFieldSchemaArtifact)
        .build();

    var valueConstraintsReporter = new TemplateReporter(templateSchemaArtifact);

    String fieldPath = "/" + fieldName;

    FieldValues fieldValues = new FieldValues(
        List.of(), Optional.of(new URI("http://vocab.fairdatacollective.org/gdmt/ContactPerson")), Optional.empty(), Optional.of("Neutrophils/leukocytes"));
    Map<String, FieldValues> values = new HashMap<>();
    values.put(fieldPath, fieldValues);

    when(valuesReporter.getValues()).thenReturn(values);

    validator.validate(valueConstraintsReporter, valuesReporter, consumer);

    assertEquals(1, results.size());
  }

  @Test
  void testValidateControlledTermsCorrectIdCorrectPrefLabel() throws URISyntaxException {
    String apiKey = "e94e265d4c3cd623ca8bde96cfc743074196409e345b164f148333dd403c3401";
    validator = new ControlledTermValidatorComponent(apiKey);

    String fieldName = "controlled terms field";
    String templateName = "My template";
    FieldSchemaArtifact linkFieldSchemaArtifact = FieldSchemaArtifact.controlledTermFieldBuilder()
        .withName(fieldName)
        .withBranchValueConstraint(new URI("http://vocab.fairdatacollective.org/gdmt/ContributorRole"),
            "https://bioportal.bioontology.org/ontologies/FDC-GDMT",
            "FDC-GDMT",
            "FDC-GDMT",
            2147483647)
        .build();

    TemplateSchemaArtifact templateSchemaArtifact = TemplateSchemaArtifact.builder()
        .withName(templateName)
        .withFieldSchema(linkFieldSchemaArtifact)
        .build();

    var valueConstraintsReporter = new TemplateReporter(templateSchemaArtifact);

    String fieldPath = "/" + fieldName;

    FieldValues fieldValues = new FieldValues(
        List.of(), Optional.of(new URI("http://vocab.fairdatacollective.org/gdmt/ContactPerson")), Optional.empty(), Optional.of("Contact Person"));
    Map<String, FieldValues> values = new HashMap<>();
    values.put(fieldPath, fieldValues);

    when(valuesReporter.getValues()).thenReturn(values);

    validator.validate(valueConstraintsReporter, valuesReporter, consumer);

    assertEquals(0, results.size());
  }
}
