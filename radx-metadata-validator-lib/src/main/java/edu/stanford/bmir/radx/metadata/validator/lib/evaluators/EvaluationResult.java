package edu.stanford.bmir.radx.metadata.validator.lib.evaluators;

public class EvaluationResult {
  private EvaluationConstant evaluationConstant;
  private Number count;

  public EvaluationResult(EvaluationConstant evaluationConstant, Number count) {
    this.evaluationConstant = evaluationConstant;
    this.count = count;
  }

  public EvaluationConstant getEvaluationConstant() {
    return evaluationConstant;
  }

  public Number getCount() {
    return count;
  }
}
