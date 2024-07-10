package edu.stanford.bmir.radx.metadata.validator.lib.evaluators;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class EvaluationReportWriter {
  public void writeReportHeader(OutputStream outputStream) throws IOException {
    var resultWriter = new CSVPrinter(new PrintStream(outputStream, true, StandardCharsets.UTF_8), CSVFormat.DEFAULT);
    resultWriter.printRecord("EVALUATION TYPE", "Content");
  }

  public void writeReport(EvaluationReport report, OutputStream outputStream) throws IOException {
    var resultWriter = new CSVPrinter(new PrintStream(outputStream, true, StandardCharsets.UTF_8), CSVFormat.DEFAULT);

    report.results()
        .stream()
        .map(EvaluationReportWriter::toCsvRecord)
        .forEach(r -> {
          try {
            resultWriter.printRecord(r);
          } catch (IOException e) {
            throw new UncheckedIOException(e);
          }
        });
  }

  private static List<? extends Serializable> toCsvRecord(EvaluationResult r) {
    var type = r.getEvaluationConstant();
    var count = r.getContent();
    String countStr;

    if (count instanceof Number) {
      countStr = count.toString();
    } else if (count instanceof Map) {
      countStr = ((Map<?, ?>) count).entrySet().stream()
          .map(e -> e.getKey() + "=" + e.getValue())
          .collect(Collectors.joining(", "));
    } else {
      countStr = count != null ? count.toString() : "null";
    }

    return List.of(type, countStr);
  }
}
