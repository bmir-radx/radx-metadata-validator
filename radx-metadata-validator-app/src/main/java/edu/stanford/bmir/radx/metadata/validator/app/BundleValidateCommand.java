package edu.stanford.bmir.radx.metadata.validator.app;

import edu.stanford.bmir.radx.metadata.validator.lib.*;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

@Component
@Command(name = "validate", mixinStandardHelpOptions = true)
public class BundleValidateCommand implements Callable<Integer> {
  private final ValidatorFactory validatorFactory;
  private final ValidationReportWriter validationReportWriter;
  @Option(names = "--template", required = true, description = "Path to the JSON template file. This is optional. If it is not provided then the Radx Metadata Specification will be utilized by default.")
  private Path template;

  @Option(names = "--instance", required = true, description = "Path to the JSON instance file(s) that you want to validate.")
  private Path instance;

  @Option(names = "--out", description = "Path to an output file where the validation report will be written. This is optional. If it is not provided then the report will be written to stdout.")
  private Path out;

  @Option(names = "--data", description = "CSV data file name described by the metadata instance.")
  private String data;

  @Option(names = "--dict", description = "CSV data dictionary file name.")
  private String dict;

  @Option(names = "--sha256", description = "SHA256 digest value of data file.")
  private String sha256;

  @Option(names = "--apiKey", description = "CEDAR API key.")
  private String apiKey;

  @Option(names = "--tsApi", description = "CEDAR Terminology server integrated search end point.")
  private String tsApi;

  private boolean hasCached = false;
  private boolean multipleInstances = false;
  private TerminologyServerHandler terminologyServerHandler;

  public BundleValidateCommand(ValidatorFactory validatorFactory, ValidationReportWriter validationReportWriter) {
    this.validatorFactory = validatorFactory;
    this.validationReportWriter = validationReportWriter;
  }

  private OutputStream getOutputStream(Path outputFile) throws IOException {
    if(outputFile != null) {
      return Files.newOutputStream(out);
    }
    else {
      return System.out;
    }
  }

  private Path getOutputPath(Path file){
    if(out != null){
      String outputFileName = file.getFileName().toString().replaceAll("\\.json$", "_report.json");
      return out.resolve(outputFileName);
    } else{
      return null;
    }
  }

  @Override
  public Integer call() throws Exception {
    if (!Files.exists(template)) {
      throw new FileNotFoundException("Template file not found: " + template);
    }
    String templateContent = Files.readString(template);

    // Check if the instance path is a directory
    if (Files.isDirectory(instance)) {
      multipleInstances = true;
      try (Stream<Path> paths = Files.walk(instance)) {
        paths.filter(Files::isRegularFile)
            .filter(path -> path.toString().endsWith(".json"))
            .forEach(file -> {
          // For each file, generate a report in the specified output directory
          try {
            Path outputFile = getOutputPath(file);
            validateSingleInstance(templateContent, file, outputFile);
          } catch (Exception e) {
            System.err.println("Error processing file " + file + ": " + e.getMessage());
          }
        });
      }
    } else if (Files.exists(instance)) {
      // Process a single instance file, report goes to the specified output or stdout
      validateSingleInstance(templateContent, instance, out);
    } else {
      throw new FileNotFoundException("Instance path not found: " + instance);
    }

    return 0;
  }

  private void validateSingleInstance(String templateContent, Path instanceFile, Path outputFile) throws Exception {
    var outStream = getOutputStream(outputFile);
    var validator = validatorFactory.createValidator(getLiteralFieldValidatorsComponent(), getTerminologyServerHandler());

    String instanceContent = Files.readString(instanceFile);
    if(!hasCached && multipleInstances){
      terminologyServerHandler = new TerminologyServerHandler(apiKey, tsApi);
      Cache.init(templateContent, instanceContent, terminologyServerHandler);
      hasCached = true;
    }
    var report = validator.validateInstance(templateContent, instanceContent);

    validationReportWriter.writeReportHeader(outStream);
    validationReportWriter.writeReport(report, outStream);
    int errorCount = isValid(report.results());
    if (errorCount > 0) {
      System.out.println(instanceFile + " is not valid. " + errorCount + " error(s) found.");
    } else {
      System.out.println(instanceFile + " is valid");
    }

    if (outStream != System.out) {
      outStream.close();
    }
  }

  private Integer isValid(List<ValidationResult> report){
    int errorCount = 0;
    for(ValidationResult result: report){
      if(result.validationLevel().equals(ValidationLevel.ERROR)){
        errorCount += 1;
      }
    }
    return  errorCount;
  }

  private LiteralFieldValidators getLiteralFieldValidatorsComponent(){
    var map = new HashMap<FieldPath, LiteralFieldValidator>();
    if(sha256 != null){
      String errorMessage = String.format("Expected SHA256 digest equals to %s", sha256);
      String warningMessage = String.format("Expected SHA256 digest equals to %s, but an empty value is received.", sha256);
      var constantValueFieldValidator = new ConstantValueFieldValidator(sha256, errorMessage, warningMessage);
      map.put(RADxSpecificFieldPath.SHA256_DIGEST.getFieldPath(), constantValueFieldValidator);
    }

    if(data != null){
      String errorMessage = String.format("Expected File Name equals to %s", data);
      String warningMessage = String.format("Expected File Name equals to %s, but an empty value is received.", data);
      var constantValueFieldValidator = new ConstantValueFieldValidator(data, errorMessage, warningMessage);
      map.put(RADxSpecificFieldPath.FILE_NAME.getFieldPath(), constantValueFieldValidator);
    }

    if(dict != null){
      String errorMessage = String.format("Expected Data Dictionary File Name equals to %s", dict);
      String warningMessage = String.format("Expected Data Dictionary File Name equals to %s, but an empty value is received.", dict);
      var constantValueFieldValidator = new ConstantValueFieldValidator(dict, errorMessage, warningMessage);
      map.put(RADxSpecificFieldPath.DATA_DICT_FILE_NAME.getFieldPath(), constantValueFieldValidator);
    }

    return new LiteralFieldValidators(map);
  }

  private TerminologyServerHandler getTerminologyServerHandler() {
    return new TerminologyServerHandler(apiKey, tsApi);
  }
}
