package edu.stanford.bmir.radx.metadata.validator.lib.evaluators;

import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.stanford.bmir.radx.metadata.validator.lib.JsonLoader;
import edu.stanford.bmir.radx.metadata.validator.lib.TemplateInstanceValuesReporter;
import org.metadatacenter.artifacts.model.core.TemplateInstanceArtifact;
import org.metadatacenter.artifacts.model.core.TemplateSchemaArtifact;
import org.metadatacenter.artifacts.model.reader.JsonSchemaArtifactReader;
import org.metadatacenter.artifacts.model.visitors.TemplateReporter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.function.Consumer;

@Component
public class Evaluator {
  private final CompletenessEvaluator completenessEvaluator;

  public Evaluator(CompletenessEvaluator completenessEvaluator) {
    this.completenessEvaluator = completenessEvaluator;
  }

  public EvaluationReport evaluate(String templateContent, String instanceContent){
    var results = new ArrayList<EvaluationResult>();
    Consumer<EvaluationResult> consumer = results::add;

    var templateNode = JsonLoader.loadJson(templateContent, "Template");
    var instanceNode = JsonLoader.loadJson(instanceContent, "Instance");

    JsonSchemaArtifactReader jsonSchemaArtifactReader = new JsonSchemaArtifactReader();
    TemplateSchemaArtifact templateSchemaArtifact = jsonSchemaArtifactReader.readTemplateSchemaArtifact((ObjectNode) templateNode);
    TemplateReporter templateReporter = new TemplateReporter(templateSchemaArtifact);

    TemplateInstanceArtifact templateInstanceArtifact = jsonSchemaArtifactReader.readTemplateInstanceArtifact((ObjectNode) instanceNode);
    TemplateInstanceValuesReporter templateInstanceValuesReporter = new TemplateInstanceValuesReporter(templateInstanceArtifact);

    completenessEvaluator.evaluate(templateSchemaArtifact, templateInstanceValuesReporter, consumer);

    return new EvaluationReport(results);
  }
}
