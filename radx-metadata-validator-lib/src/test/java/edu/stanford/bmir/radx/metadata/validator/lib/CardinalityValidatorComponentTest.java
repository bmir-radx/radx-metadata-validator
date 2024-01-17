package edu.stanford.bmir.radx.metadata.validator.lib;

import edu.stanford.bmir.radx.metadata.validator.lib.ValidatorComponents.CardinalityValidatorComponent;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
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
  void setup() {
    MockitoAnnotations.openMocks(this);
    results = new ArrayList<>();
    consumer = results::add;
    validator = new CardinalityValidatorComponent();
  }

  @Test
  public void validateWhenMultipleInstancesAndNotMultipleChoice(){
    String fieldName = "text field";
    String templateName = "My template";
    FieldSchemaArtifact textFieldSchemaArtifact = FieldSchemaArtifact.textFieldBuilder()
        .withName(fieldName)
        .withIsMultiple(false)
        .build();

    TemplateSchemaArtifact templateSchemaArtifact = TemplateSchemaArtifact.builder()
        .withName(templateName)
        .withFieldSchema(textFieldSchemaArtifact)
        .build();

    var templateReporter = new TemplateReporter(templateSchemaArtifact);
    String fieldPath = "/" + fieldName;
    Map<String, Integer> values = new HashMap<>();
    values.put(fieldPath, 2);

    when(valuesReporter.getFieldCardinals()).thenReturn(values);

    validator.validate(templateReporter, valuesReporter, consumer);
    assertEquals(1, results.size());
  }

//  @Test
//  void validateWhenMultipleInstancesAndMultipleChoice(){
//    String fieldName = "text field";
//    String templateName = "My template";
//    FieldSchemaArtifact textFieldSchemaArtifact = FieldSchemaArtifact.textFieldBuilder()
//        .withName(fieldName)
//        .withIsMultiple(true)
//        .build();
//
//    TemplateSchemaArtifact templateSchemaArtifact = TemplateSchemaArtifact.builder()
//        .withName(templateName)
//        .withFieldSchema(textFieldSchemaArtifact)
//        .build();
//
//    var templateReporter = new TemplateReporter(templateSchemaArtifact);
//    String fieldPath = "/" + fieldName;
//    Map<String, Integer> values = new HashMap<>();
//    values.put(fieldPath, 2);
//
//    when(valuesReporter.getCardinals()).thenReturn(values);
//
//    validator.validate(templateReporter, valuesReporter, consumer);
//    assertEquals(0, results.size());
//  }
}
