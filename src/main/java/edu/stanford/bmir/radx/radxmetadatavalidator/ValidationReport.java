package edu.stanford.bmir.radx.radxmetadatavalidator;

import java.util.List;

public record ValidationReport(List<ValidationResult> results) {
}
