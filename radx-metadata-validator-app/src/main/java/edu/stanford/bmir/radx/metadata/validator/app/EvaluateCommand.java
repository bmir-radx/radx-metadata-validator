package edu.stanford.bmir.radx.metadata.validator.app;

import edu.stanford.bmir.radx.metadata.validator.lib.evaluators.EvaluationReportWriter;
import edu.stanford.bmir.radx.metadata.validator.lib.evaluators.Evaluator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.Callable;

@Component
@Command(name = "evaluate", mixinStandardHelpOptions = true)
public class EvaluateCommand implements Callable<Integer> {
  private final Evaluator evaluator;
  private final EvaluationReportWriter evaluationReportWriter;
  @Option(names = "--template", required = true, description = "Path to the JSON template file. This is optional. If it is not provided then the Radx Metadata Specification will be utilized by default.")
  private Path template;

  @Option(names = "--instance", required = true, description = "Path to the JSON instance file that you want to validate.")
  private Path instance;

  @Option(names = "--out", description = "Path to an output file where the evaluation report will be written. This is optional. If it is not provided then the report will be written to stdout.")
  private Path out;

  @Autowired
  public EvaluateCommand(Evaluator evaluator, EvaluationReportWriter evaluationReportWriter) {
    this.evaluator = evaluator;
    this.evaluationReportWriter = evaluationReportWriter;
  }

  private OutputStream getOutputStream() throws IOException {
    if (out != null) {
      if (out.getParent() != null && !Files.exists(out.getParent())) {
        Files.createDirectories(out.getParent());
      }
      return Files.newOutputStream(out, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    } else {
      return System.out;
    }
  }

  @Override
  public Integer call() throws Exception {
    if (!Files.exists(template)) {
      throw new FileNotFoundException("Template file not found: " + template);
    }
    if (!Files.exists(instance)) {
      throw new FileNotFoundException("Instance file not found: " + instance);
    }

    var out = getOutputStream();
    String templateContent = Files.readString(template);
    String instanceContent = Files.readString(instance);
    var report = evaluator.evaluate(templateContent, instanceContent);

    evaluationReportWriter.writeReportHeader(out);
    evaluationReportWriter.writeReport(report, out);

    if(out != System.out) {
      out.close();
    }

    return 0;
  }
}
