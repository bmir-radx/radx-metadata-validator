package edu.stanford.bmir.radx.metadata.validator.lib.evaluators;

import edu.stanford.bmir.radx.metadata.validator.lib.FieldValues;
import edu.stanford.bmir.radx.metadata.validator.lib.FieldsCollector;
import edu.stanford.bmir.radx.metadata.validator.lib.TemplateInstanceValuesReporter;
import org.metadatacenter.artifacts.model.visitors.TemplateReporter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static edu.stanford.bmir.radx.metadata.validator.lib.evaluators.EvaluationConstant.CONTROLLED_TERM_FREQUENCY;
import static edu.stanford.bmir.radx.metadata.validator.lib.evaluators.EvaluationConstant.FILLED_CONTROLLED_TERM_COUNT;

@Component
public class ControlledTermsEvaluator {
  private final FieldsCollector fieldsCollector = new FieldsCollector();
  public void evaluate(TemplateReporter templateReporter, TemplateInstanceValuesReporter valuesReporter, Consumer<EvaluationResult> handler){
    //todo check it is a valid controlled term
    var values = valuesReporter.getValues();
    var controlledTermCounts = new HashMap<String, Integer>();
    int filledCtFields = 0;

    for (Map.Entry<String, FieldValues> fieldEntry : values.entrySet()) {
      var path = fieldEntry.getKey();
      var fieldValue = fieldEntry.getValue();
      var valueConstraint = templateReporter.getValueConstraints(path);

      if(!fieldsCollector.isEmptyField(fieldValue) && valueConstraint.isPresent() && valueConstraint.get().isControlledTermValueConstraint()){
        filledCtFields++;
        var prefLabel = fieldValue.label();
        if(prefLabel.isPresent() && !prefLabel.get().isEmpty()){
          controlledTermCounts.merge(prefLabel.get(), 1, Integer::sum);
        }
      }
    }

    handler.accept(new EvaluationResult(FILLED_CONTROLLED_TERM_COUNT, filledCtFields));
    handler.accept(new EvaluationResult(CONTROLLED_TERM_FREQUENCY, controlledTermCounts));
  }
}
