package edu.stanford.bmir.radx.metadata.validator.lib;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component("MetadataValidationReportWriter")
public class ValidationReportWriter {
  public void writeReportHeader(OutputStream outputStream) throws IOException {
    var resultWriter = new CSVPrinter(new PrintStream(outputStream, true, StandardCharsets.UTF_8), CSVFormat.DEFAULT);
    resultWriter.printRecord("LEVEL", "PATH", "VALIDATION TYPE", "MESSAGE");
  }

  public void writeReport(ValidationReport report, OutputStream outputStream) throws IOException {
    var resultWriter = new CSVPrinter(new PrintStream(outputStream, true, StandardCharsets.UTF_8), CSVFormat.DEFAULT);

    report.results()
        .stream()
        .map(ValidationReportWriter::toCsvRecord)
        .forEach(r -> {
          try {
            resultWriter.printRecord(r);
          } catch (IOException e) {
            throw new UncheckedIOException(e);
          }
        });
  }

  private static List<? extends Serializable> toCsvRecord(ValidationResult r) {
    var level = r.validationLevel();
    var path = r.pointer();
    var message = r.message();
    var validationType = r.validationName();
    return List.of(level, path, validationType, message);
  }
}
