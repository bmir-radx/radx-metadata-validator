package edu.stanford.bmir.radx.metadata.validator.lib.evaluators;

public class EvaluationResult {
  private EvaluationConstant evaluationConstant;
  private Object content;

  public EvaluationResult(EvaluationConstant evaluationConstant, Object content) {
    this.evaluationConstant = evaluationConstant;
    this.content = content;
  }

  public EvaluationConstant getEvaluationConstant() {
    return evaluationConstant;
  }

  public Object getContent() {
    return content;
  }
}
