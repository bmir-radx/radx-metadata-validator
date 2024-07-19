package edu.stanford.bmir.radx.metadata.validator.lib.evaluators;

import java.util.List;

public class ComparisonResult {
  private final boolean allUnique;
  private final List<List<Integer>> identicalInstances;

  public ComparisonResult(boolean allUnique, List<List<Integer>> identicalInstances) {
    this.allUnique = allUnique;
    this.identicalInstances = identicalInstances;
  }

  public boolean areAllUnique() {
    return allUnique;
  }

  public List<List<Integer>> getIdenticalInstances() {
    return identicalInstances;
  }

  @Override
  public String toString() {
    return "identicalInstances=" + identicalInstances;
  }
}
