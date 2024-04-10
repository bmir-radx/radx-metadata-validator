//package edu.stanford.bmir.radx.metadata.validator.lib;
//
//import edu.stanford.bmir.radx.metadata.validator.lib.validators.ControlledTermValidatorComponent;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.metadatacenter.artifacts.model.core.FieldSchemaArtifact;
//import org.metadatacenter.artifacts.model.core.TemplateSchemaArtifact;
//import org.metadatacenter.artifacts.model.visitors.TemplateReporter;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//
//import java.net.URI;
//import java.net.URISyntaxException;
//import java.util.*;
//import java.util.function.Consumer;
//
//import static org.junit.Assert.assertEquals;
//import static org.mockito.Mockito.when;
//
//public class ControlledTermValidatorComponentTest {
//  private String apiKey = "e94e265d4c3cd623ca8bde96cfc743074196409e345b164f148333dd403c3401";
//  private String terminologyServerEndPoint = "https://terminology.metadatacenter.org/bioportal/integrated-search/" ;
//  String fieldName = "controlled terms field";
//  private ControlledTermValidatorComponent validator;
//  private TerminologyServerHandler terminologyServerHandler;
//  private List<ValidationResult> results;
//  private Consumer<ValidationResult> consumer;
//  @Mock
//  private TemplateInstanceValuesReporter valuesReporter;
//  private TemplateReporter valueConstraintsReporter;
//
//  @BeforeEach
//  void setup() throws URISyntaxException {
//    MockitoAnnotations.openMocks(this);
//    results = new ArrayList<>();
//    consumer = results::add;
//    validator = new ControlledTermValidatorComponent();
//
//    String templateName = "My template";
//    FieldSchemaArtifact fieldSchemaArtifact = FieldSchemaArtifact.controlledTermFieldBuilder()
//        .withName(fieldName)
//        .withBranchValueConstraint(new URI("http://vocab.fairdatacollective.org/gdmt/ContributorRole"),
//            "https://bioportal.bioontology.org/ontologies/FDC-GDMT",
//            "FDC-GDMT",
//            "FDC-GDMT",
//            2147483647)
//        .build();
//
//    TemplateSchemaArtifact templateSchemaArtifact = TemplateSchemaArtifact.builder()
//        .withName(templateName)
//        .withFieldSchema(fieldSchemaArtifact)
//        .build();
//
//    valueConstraintsReporter = new TemplateReporter(templateSchemaArtifact);
//  }
//
//  @Test
//  void testValidateControlledTermsNoApiKeyNoTsApi() throws URISyntaxException {
//    terminologyServerHandler = new TerminologyServerHandler(null, null);
//
//    String fieldPath = "/" + fieldName;
//    FieldValues fieldValues = new FieldValues(
//        List.of(), Optional.of(new URI("http://purl.bioontology.org/ontology/LNC/LP286655-8")), Optional.empty(), Optional.of("Neutrophils/leukocytes"));
//    Map<String, FieldValues> values = new HashMap<>();
//    values.put(fieldPath, fieldValues);
//
//    when(valuesReporter.getValues()).thenReturn(values);
//
//    validator.validate(terminologyServerHandler, valueConstraintsReporter, valuesReporter, consumer);
//
//    assertEquals(0, results.size());
//  }
//
//  @Test
//  void testValidateControlledTermsNoApiKey() throws URISyntaxException {
//    terminologyServerHandler = new TerminologyServerHandler(null, terminologyServerEndPoint);
//
//    String fieldPath = "/" + fieldName;
//    FieldValues fieldValues = new FieldValues(
//        List.of(), Optional.of(new URI("http://purl.bioontology.org/ontology/LNC/LP286655-8")), Optional.empty(), Optional.of("Neutrophils/leukocytes"));
//    Map<String, FieldValues> values = new HashMap<>();
//    values.put(fieldPath, fieldValues);
//
//    when(valuesReporter.getValues()).thenReturn(values);
//
//    validator.validate(terminologyServerHandler, valueConstraintsReporter, valuesReporter, consumer);
//
//    assertEquals(1, results.size());
//  }
//
//  @Test
//  void testValidateControlledTermsWrongIdWrongPrefLabel() throws URISyntaxException {
//    terminologyServerHandler = new TerminologyServerHandler(apiKey, terminologyServerEndPoint);
//
//    String fieldPath = "/" + fieldName;
//    FieldValues fieldValues = new FieldValues(
//        List.of(), Optional.of(new URI("http://purl.bioontology.org/ontology/LNC/LP286655-8")), Optional.empty(), Optional.of("Neutrophils/leukocytes"));
//    Map<String, FieldValues> values = new HashMap<>();
//    values.put(fieldPath, fieldValues);
//
//    when(valuesReporter.getValues()).thenReturn(values);
//
//    validator.validate(terminologyServerHandler, valueConstraintsReporter, valuesReporter, consumer);
//
//    assertEquals(1, results.size());
//  }
//
////  @Test
////  void testValidateControlledTermsWrongId() throws URISyntaxException {
////    terminologyServerHandler = new TerminologyServerHandler(apiKey, terminologyServerEndPoint);
////
////    String fieldPath = "/" + fieldName;
////    FieldValues fieldValues = new FieldValues(
////        List.of(), Optional.of(new URI("http://purl.bioontology.org/ontology/MESH/D000086382")), Optional.empty(), Optional.of("COVID-19"));
////    Map<String, FieldValues> values = new HashMap<>();
////    values.put(fieldPath, fieldValues);
////
////    when(valuesReporter.getValues()).thenReturn(values);
////
////    validator.validate(terminologyServerHandler, valueConstraintsReporter, valuesReporter, consumer);
////
////    assertEquals(1, results.size());
////  }
//
//  @Test
//  void testValidateControlledTermsCorrectIdWrongPrefLabel() throws URISyntaxException {
//    terminologyServerHandler = new TerminologyServerHandler(apiKey, terminologyServerEndPoint);
//
//    String fieldPath = "/" + fieldName;
//    FieldValues fieldValues = new FieldValues(
//        List.of(), Optional.of(new URI("http://vocab.fairdatacollective.org/gdmt/ContactPerson")), Optional.empty(), Optional.of("Neutrophils/leukocytes"));
//    Map<String, FieldValues> values = new HashMap<>();
//    values.put(fieldPath, fieldValues);
//
//    when(valuesReporter.getValues()).thenReturn(values);
//
//    validator.validate(terminologyServerHandler, valueConstraintsReporter, valuesReporter, consumer);
//
//    assertEquals(1, results.size());
//  }
//
//  @Test
//  void testValidateControlledTermsCorrectIdCorrectPrefLabel() throws URISyntaxException {
//    terminologyServerHandler = new TerminologyServerHandler(apiKey, terminologyServerEndPoint);
//
//    String fieldPath = "/" + fieldName;
//    FieldValues fieldValues = new FieldValues(
//        List.of(), Optional.of(new URI("http://vocab.fairdatacollective.org/gdmt/ContactPerson")), Optional.empty(), Optional.of("Contact Person"));
//    Map<String, FieldValues> values = new HashMap<>();
//    values.put(fieldPath, fieldValues);
//
//    when(valuesReporter.getValues()).thenReturn(values);
//
//    validator.validate(terminologyServerHandler, valueConstraintsReporter, valuesReporter, consumer);
//
//    assertEquals(0, results.size());
//  }
//
//  @Test
//  void testLoadFromCache() throws URISyntaxException {
//    terminologyServerHandler = new TerminologyServerHandler(apiKey, terminologyServerEndPoint);
//    String fieldName1 = "f1";
//    String fieldName2 = "f2";
//
//    String templateName = "My template";
//    FieldSchemaArtifact fieldSchemaArtifact1 = FieldSchemaArtifact.controlledTermFieldBuilder()
//        .withName(fieldName1)
//        .withBranchValueConstraint(new URI("http://vocab.fairdatacollective.org/gdmt/ContributorRole"),
//            "https://bioportal.bioontology.org/ontologies/FDC-GDMT",
//            "FDC-GDMT",
//            "FDC-GDMT",
//            2147483647)
//        .build();
//
//    FieldSchemaArtifact fieldSchemaArtifact2 = FieldSchemaArtifact.controlledTermFieldBuilder()
//        .withName(fieldName2)
//        .withBranchValueConstraint(new URI("http://vocab.fairdatacollective.org/gdmt/ContributorRole"),
//            "https://bioportal.bioontology.org/ontologies/FDC-GDMT",
//            "FDC-GDMT",
//            "FDC-GDMT",
//            2147483647)
//        .build();
//
//    TemplateSchemaArtifact templateSchemaArtifact = TemplateSchemaArtifact.builder()
//        .withName(templateName)
//        .withFieldSchema(fieldSchemaArtifact1)
//        .withFieldSchema(fieldSchemaArtifact2)
//        .build();
//
//    var templateReporter = new TemplateReporter(templateSchemaArtifact);
//    String fieldPath1 = "/" + fieldName1;
//    String fieldPath2 = "/" + fieldName2;
//    FieldValues fieldValues = new FieldValues(
//        List.of(), Optional.of(new URI("http://vocab.fairdatacollective.org/gdmt/ContactPerson")), Optional.empty(), Optional.of("Contact Person"));
//    Map<String, FieldValues> values = new HashMap<>();
//    values.put(fieldPath1, fieldValues);
//    values.put(fieldPath2,fieldValues);
//
//    when(valuesReporter.getValues()).thenReturn(values);
//    validator.validate(terminologyServerHandler, templateReporter, valuesReporter, consumer);
//
//    assertEquals(0, results.size());
//  }
//}
