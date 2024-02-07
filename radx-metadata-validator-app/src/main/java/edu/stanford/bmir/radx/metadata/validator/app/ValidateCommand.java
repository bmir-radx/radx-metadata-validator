package edu.stanford.bmir.radx.metadata.validator.app;

import edu.stanford.bmir.radx.metadata.validator.lib.ValidationReportWriter;
import edu.stanford.bmir.radx.metadata.validator.lib.ValidatorFactory;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

@Component
@Command(name = "validate", mixinStandardHelpOptions = true)
public class ValidateCommand implements Callable<Integer> {
  private final ValidatorFactory validatorFactory;
  private final ValidationReportWriter validationReportWriter;
  @Option(names = "--template", required = true, description = "Path to the JSON template file. This is optional. If it is not provided then the Radx Metadata Specification will be utilized by default.")
  private Path template;

  @Option(names = "--instance", required = true, description = "Path to the JSON instance file that you want to validate.")
  private Path instance;

  @Option(names = "--out", description = "Path to an output file where the validation report will be written. This is optional. If it is not provided then the report will be written to stdout.")
  private Path out;

  @Option(names = "--data", description = "Path to the CSV data file described by the metadata instance.")
  private Path data;

  @Option(names = "--dict", description = "Path to the CSV data dictionary file.")
  private Path dict;

  @Option(names = "--sha256", description = "SHA256 digest value.")
  private String sha256;

  public ValidateCommand(ValidatorFactory validatorFactory, ValidationReportWriter validationReportWriter) {
    this.validatorFactory = validatorFactory;
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
    if (!Files.exists(template)) {
      throw new FileNotFoundException("Template file not found: " + template);
    }
    if (!Files.exists(instance)) {
      throw new FileNotFoundException("Instance file not found: " + instance);
    }

    var out = getOutputStream();
    var validator = validatorFactory.createValidator();
    String templateContent = Files.readString(template);
    String instanceContent = Files.readString(instance);
    var report = validator.validateInstance(templateContent, instanceContent);

    validationReportWriter.writeReportHeader(out);
    validationReportWriter.writeReport(report, out);


    if(out != System.out) {
      out.close();
    }

    return 0;
  }
}
