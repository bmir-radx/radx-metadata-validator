package edu.stanford.bmir.radx.radxmetadatavalidator;

import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

@Component
@Command(name = "validate")
public class ValidateCommand implements Callable<Integer> {
  private final Validator validator;
  private final ValidationReportWriter validationReportWriter;
  @Option(names = "--template", required = true, description = "Path to the JSON template file.")
  private Path template;

  @Option(names = "--instance", required = true, description = "Path to the JSON instance file.")
  private Path instance;

  @Option(names = "--out", description = "Path to an output file where the validation report will be written.")
  private Path out;

  public ValidateCommand(Validator validator, ValidationReportWriter validationReportWriter) {
    this.validator = validator;
    this.validationReportWriter = validationReportWriter;
  }

  private OutputStream getOutputStream() throws IOException {
    if(out != null) {
      return Files.newOutputStream(out);
    }
    else {
      return System.out;
    }
  }

  @Override
  public Integer call() throws Exception {
    var out = getOutputStream();
    var report = validator.validateInstance(template, instance);


//    JsonNode templateNode = JsonLoader.loadJson(String.valueOf(template));
//    JsonNode instanceNode = JsonLoader.loadJson(String.valueOf(instance));
//    ProcessingReport report = schemaValidatorComponent.validateAgainstSchema(templateNode, instanceNode);
//    schemaValidatorComponent.parseProcessingReportMessages(report);
//    var errors = schemaValidatorComponent.getErrors();
//    var warnings = schemaValidatorComponent.getWarnings();
//    var validationLevel = schemaValidatorComponent.getValidationLevel();

    validationReportWriter.writeReport(report, out);


//    out.write(parsedReport.getBytes());

    if(out != System.out) {
      out.close();
    }

    return 0;
  }
}
