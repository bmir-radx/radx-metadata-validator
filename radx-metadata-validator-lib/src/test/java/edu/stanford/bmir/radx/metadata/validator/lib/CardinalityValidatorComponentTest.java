package edu.stanford.bmir.radx.metadata.validator.lib;

import edu.stanford.bmir.radx.metadata.validator.lib.ValidatorComponents.CardinalityValidatorComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.metadatacenter.artifacts.model.core.ElementSchemaArtifact;
import org.metadatacenter.artifacts.model.core.FieldSchemaArtifact;
import org.metadatacenter.artifacts.model.core.TemplateSchemaArtifact;
import org.metadatacenter.artifacts.model.visitors.TemplateReporter;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class CardinalityValidatorComponentTest {
  private CardinalityValidatorComponent validator;
  private List<ValidationResult> results;
  private Consumer<ValidationResult> consumer;
  @Mock
  private TemplateInstanceValuesReporter valuesReporter;

  @BeforeEach
  void setUp(){
    MockitoAnnotations.openMocks(this);
    results = new ArrayList<>();
    consumer = results::add;
    validator = new CardinalityValidatorComponent();
  }

  @Test
  void testValidateWithCorrectCardinality() {
    String fieldName1 = "Temporal field";
    String fieldName2 = "Text field";
    String elementName = "Text element";
    String templateName = "My template";

    FieldSchemaArtifact textFieldArtifact1 = FieldSchemaArtifact.textFieldBuilder()
        .withName(fieldName1)
        .withIsMultiple(true)
        .withMinItems(1)
        .build();

    FieldSchemaArtifact textFieldArtifact2 = FieldSchemaArtifact.textFieldBuilder()
        .withName(fieldName2)
        .withIsMultiple(true)
        .withMinItems(0)
        .build();

    ElementSchemaArtifact elementSchemaArtifact = ElementSchemaArtifact.builder()
        .withName(elementName)
        .withFieldSchema(textFieldArtifact2)
        .withIsMultiple(true)
        .withMaxItems(3)
        .build();

    TemplateSchemaArtifact templateSchemaArtifact = TemplateSchemaArtifact.builder()
        .withName(templateName)
        .withFieldSchema(textFieldArtifact1)
        .withElementSchema(elementSchemaArtifact)
        .build();

    var templateReporter = new TemplateReporter(templateSchemaArtifact);

    String fieldPath1 = "/" + fieldName1;
    String elementPath = "/" + elementName;
    String fieldPath2 = elementPath + "/" + fieldName2;

    Map<String, Integer> fieldCardinalities = new HashMap<>();
    Map<String, Integer> elementCardinalities = new HashMap<>();
    fieldCardinalities.put(fieldPath1, 2);
    fieldCardinalities.put(fieldPath2, 2);
    elementCardinalities.put(elementPath, 2);

    when(valuesReporter.getFieldCardinalities()).thenReturn(fieldCardinalities);
    when(valuesReporter.getElementCardinalities()).thenReturn(elementCardinalities);

    validator.validate(templateReporter, valuesReporter, consumer);
    assertEquals(0, results.size());
  }

  @Test
  void testValidateWithIncorrectCardinality() {
    String fieldName1 = "Temporal field";
    String fieldName2 = "Text field";
    String elementName = "Text element";
    String templateName = "My template";

    FieldSchemaArtifact textFieldArtifact1 = FieldSchemaArtifact.textFieldBuilder()
        .withName(fieldName1)
        .withIsMultiple(true)
        .withMinItems(2)
        .build();

    FieldSchemaArtifact textFieldArtifact2 = FieldSchemaArtifact.textFieldBuilder()
        .withName(fieldName2)
        .withIsMultiple(true)
        .withMinItems(0)
        .build();

    ElementSchemaArtifact elementSchemaArtifact = ElementSchemaArtifact.builder()
        .withName(elementName)
        .withFieldSchema(textFieldArtifact2)
        .withIsMultiple(true)
        .withMaxItems(3)
        .build();

    TemplateSchemaArtifact templateSchemaArtifact = TemplateSchemaArtifact.builder()
        .withName(templateName)
        .withFieldSchema(textFieldArtifact1)
        .withElementSchema(elementSchemaArtifact)
        .build();

    var templateReporter = new TemplateReporter(templateSchemaArtifact);

    String fieldPath1 = "/" + fieldName1;
    String elementPath = "/" + elementName;
    String fieldPath2 = elementPath + "/" + fieldName2;

    Map<String, Integer> fieldCardinalities = new HashMap<>();
    Map<String, Integer> elementCardinalities = new HashMap<>();
    fieldCardinalities.put(fieldPath1, 1);
    fieldCardinalities.put(fieldPath2, 2);
    elementCardinalities.put(elementPath, 4);

    when(valuesReporter.getFieldCardinalities()).thenReturn(fieldCardinalities);
    when(valuesReporter.getElementCardinalities()).thenReturn(elementCardinalities);

    validator.validate(templateReporter, valuesReporter, consumer);
    assertEquals(2, results.size());
  }
}
