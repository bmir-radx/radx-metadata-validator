package edu.stanford.bmir.radx.metadata.validator.lib.evaluators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.metadatacenter.artifacts.model.core.ElementInstanceArtifact;
import org.metadatacenter.artifacts.model.core.FieldInstanceArtifact;
import org.metadatacenter.artifacts.model.core.TemplateInstanceArtifact;
import org.metadatacenter.artifacts.model.core.TextFieldInstance;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;

public class UniquenessEvaluatorTest {
  private UniquenessEvaluator uniquenessEvaluator;
  private List<EvaluationResult> results;

  private Consumer<EvaluationResult> handler;

  @BeforeEach
  void setUp(){
    uniquenessEvaluator = new UniquenessEvaluator();
    results = new ArrayList<>();
    handler = results::add;
  }

  @Test
  public void testDuplicateFieldInstances(){
    String instanceName = "Template 1";
    URI isBasedOnTemplateUri = URI.create("https://repo.metadatacenter.org/templates/3232");
    String textField2Name = "Text Field 2";

    FieldInstanceArtifact textField2Instance1 = TextFieldInstance.builder().withValue("Value 2").build();
    FieldInstanceArtifact textField2Instance2 = TextFieldInstance.builder().withValue("Value 3").build();
    FieldInstanceArtifact textField2Instance3 = TextFieldInstance.builder().withValue("Value 2").build();
    List<FieldInstanceArtifact> textField2Instances = new ArrayList<>();
    textField2Instances.add(textField2Instance1);
    textField2Instances.add(textField2Instance2);
    textField2Instances.add(textField2Instance3);

    TemplateInstanceArtifact templateInstanceArtifact = TemplateInstanceArtifact.builder()
        .withName(instanceName)
        .withIsBasedOn(isBasedOnTemplateUri)
        .withMultiInstanceFieldInstances(textField2Name, textField2Instances)
        .build();

    uniquenessEvaluator.evaluate(templateInstanceArtifact, handler);
    assertEquals("1", results.get(0).getContent());
  }

  @Test
  public void testUniqueFieldInstances(){
    String instanceName = "Template 1";
    URI isBasedOnTemplateUri = URI.create("https://repo.metadatacenter.org/templates/3232");
    String textField2Name = "Text Field 2";

    FieldInstanceArtifact textField2Instance1 = TextFieldInstance.builder().withValue("Value 2").build();
    FieldInstanceArtifact textField2Instance2 = TextFieldInstance.builder().withValue("Value 3").build();
    List<FieldInstanceArtifact> textField2Instances = new ArrayList<>();
    textField2Instances.add(textField2Instance1);
    textField2Instances.add(textField2Instance2);

    TemplateInstanceArtifact templateInstanceArtifact = TemplateInstanceArtifact.builder()
        .withName(instanceName)
        .withIsBasedOn(isBasedOnTemplateUri)
        .withMultiInstanceFieldInstances(textField2Name, textField2Instances)
        .build();

    uniquenessEvaluator.evaluate(templateInstanceArtifact, handler);
    assertEquals("0", results.get(0).getContent());
  }

  @Test
  public void testDuplicateElementInstances(){
    String templateInstanceName = "Template 1";
    String elementInstanceName = "Element 1";
    URI isBasedOnTemplateUri = URI.create("https://repo.metadatacenter.org/templates/3232");
    String element1Name = "Element 1";
    String textField2Name = "Text Field 2";

    FieldInstanceArtifact textField2Instance1 = TextFieldInstance.builder().withValue("Value 2").build();
    FieldInstanceArtifact textField2Instance2 = TextFieldInstance.builder().withValue("Value 3").build();
    List<FieldInstanceArtifact> textField2Instances = new ArrayList<>();
    textField2Instances.add(textField2Instance1);
    textField2Instances.add(textField2Instance2);

    ElementInstanceArtifact elementInstanceArtifact1 = ElementInstanceArtifact.builder()
        .withName(elementInstanceName)
        .withMultiInstanceFieldInstances(textField2Name, textField2Instances)
        .build();

    ElementInstanceArtifact elementInstanceArtifact2 = ElementInstanceArtifact.builder()
        .withName(elementInstanceName)
        .withMultiInstanceFieldInstances(textField2Name, textField2Instances)
        .build();

    List<ElementInstanceArtifact> elementInstanceArtifacts = new ArrayList<>();
    elementInstanceArtifacts.add(elementInstanceArtifact1);
    elementInstanceArtifacts.add(elementInstanceArtifact2);

    TemplateInstanceArtifact templateInstanceArtifact = TemplateInstanceArtifact.builder()
        .withName(templateInstanceName)
        .withIsBasedOn(isBasedOnTemplateUri)
        .withMultiInstanceElementInstances(element1Name, elementInstanceArtifacts)
        .build();

    uniquenessEvaluator.evaluate(templateInstanceArtifact, handler);
    assertEquals("1", results.get(0).getContent());
  }

  @Test
  public void testUniqueElementInstances(){
    String templateInstanceName = "Template 1";
    String elementInstanceName = "Element 1";
    URI isBasedOnTemplateUri = URI.create("https://repo.metadatacenter.org/templates/3232");
    String element1Name = "Element 1";
    String textField2Name = "Text Field 2";

    FieldInstanceArtifact textField2Instance1 = TextFieldInstance.builder().withValue("Value 2").build();
    FieldInstanceArtifact textField2Instance2 = TextFieldInstance.builder().withValue("Value 3").build();
    List<FieldInstanceArtifact> textField2Instances = new ArrayList<>();
    textField2Instances.add(textField2Instance1);
    textField2Instances.add(textField2Instance2);

    ElementInstanceArtifact elementInstanceArtifact1 = ElementInstanceArtifact.builder()
        .withName(elementInstanceName)
        .withMultiInstanceFieldInstances(textField2Name, textField2Instances)
        .build();

    List<ElementInstanceArtifact> elementInstanceArtifacts = new ArrayList<>();
    elementInstanceArtifacts.add(elementInstanceArtifact1);

    TemplateInstanceArtifact templateInstanceArtifact = TemplateInstanceArtifact.builder()
        .withName(templateInstanceName)
        .withIsBasedOn(isBasedOnTemplateUri)
        .withMultiInstanceElementInstances(element1Name, elementInstanceArtifacts)
        .build();

    uniquenessEvaluator.evaluate(templateInstanceArtifact, handler);
    assertEquals("0", results.get(0).getContent());
  }


}
