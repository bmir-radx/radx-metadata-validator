package edu.stanford.bmir.radx.radxmetadatavalidator;

import org.springframework.stereotype.Component;

import java.io.OutputStream;
import java.io.PrintStream;

@Component
public class ValidationReportWriter {
  public void writeReport(ValidationReport report, OutputStream outputStream) {
    PrintStream printStream = (outputStream == System.out) ? System.out : new PrintStream(outputStream);
    try {
      report.results().forEach(result -> {
        printStream.println(formatValidationResult(result));
      });
    } finally {
      if (outputStream != System.out) {
        printStream.close();
      }
    }
  }

  private String formatValidationResult(ValidationResult result) {
    return "[" + result.validationLevel() + "]\n" +
        result.validationName() + " [POINTER: " +
        result.pointer() + "] " + " MESSAGE: " +
        result.message();
  }

}
