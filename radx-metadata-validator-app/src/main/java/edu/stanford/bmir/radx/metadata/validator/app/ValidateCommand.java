package edu.stanford.bmir.radx.metadata.validator.app;

import edu.stanford.bmir.radx.metadata.validator.lib.*;
import edu.stanford.bmir.radx.metadata.validator.lib.validators.LiteralFieldValidatorsComponent;
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

  @Option(names = "--data", description = "CSV data file name described by the metadata instance.")
  private String data;

  @Option(names = "--dict", description = "CSV data dictionary file name.")
  private String dict;

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
    var validator = validatorFactory.createValidator(getLiteralFieldValidatorsComponent());
    String templateContent = Files.readString(template);
    String instanceContent = Files.readString(instance);
    var report = validator.validateInstance(templateContent, instanceContent);

    validationReportWriter.writeReportHeader(out);
    validationReportWriter.writeReport(report, out);
    if(isValid(report.results())){
      System.out.println(instance + " is not valid");
    } else{
      System.out.println(instance + " is valid");
    }


    if(out != System.out) {
      out.close();
    }

    return 0;
  }

  private boolean isValid(List<ValidationResult> report){
    for(ValidationResult result: report){
      if(result.validationLevel().equals(ValidationLevel.ERROR)){
        return false;
      }
    }
    return true;
  }

  private LiteralFieldValidatorsComponent getLiteralFieldValidatorsComponent(){
    var map = new HashMap<FieldPath, LiteralFieldValidator>();
    if(sha256 != null){
      var constantValueFieldValidator = new ConstantValueFieldValidator(sha256, String.format("Expected SHA256 digest equals to %s", sha256), ValidationLevel.ERROR);
      map.put(RADxSpecificFieldPath.SHA256_DIGEST.getFieldPath(), constantValueFieldValidator);
    }

    if(data != null){
      var constantValueFieldValidator = new ConstantValueFieldValidator(data, String.format("Expected File Name equals to %s", data), ValidationLevel.ERROR);
      map.put(RADxSpecificFieldPath.FILE_NAME.getFieldPath(), constantValueFieldValidator);
    }

    if(dict != null){
      var constantValueFieldValidator = new ConstantValueFieldValidator(dict, String.format("Expected Data Dictionary File Name equals to %s", dict), ValidationLevel.ERROR);
      map.put(RADxSpecificFieldPath.DATA_DICT_FILE_NAME.getFieldPath(), constantValueFieldValidator);
    }

    return new LiteralFieldValidatorsComponent(map);
  }
}
