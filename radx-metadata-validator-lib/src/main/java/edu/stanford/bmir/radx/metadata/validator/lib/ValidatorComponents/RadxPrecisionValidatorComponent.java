package edu.stanford.bmir.radx.metadata.validator.lib.ValidatorComponents;

import edu.stanford.bmir.radx.metadata.validator.lib.*;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Consumer;

@Component
public class RadxPrecisionValidatorComponent {
  public void validate(TemplateInstanceValuesReporter valuesReporter, Path data, Path dict, String sha256, Consumer<ValidationResult> handler){
    //if data, dict and sha 256 are provided, validate the value in metadata instance against provided value
    if (data != null){
      String provided_file_name = data.toString();
      var metadata_file_name = valuesReporter.getValues().get(Constants.FILE_NAME_PATH).jsonLdValue();
      validateSingleArg(provided_file_name, metadata_file_name, "File Name", Constants.FILE_NAME_PATH, handler);
    }

    if(dict != null){
      String provided_dict_file_name = dict.toString();
      var metadata_dict_file_name = valuesReporter.getValues().get(Constants.DATA_DICTIONARY_FILE_NAME_PATH).jsonLdValue();
      validateSingleArg(provided_dict_file_name, metadata_dict_file_name, "Data Dictionary File Name", Constants.DATA_DICTIONARY_FILE_NAME_PATH, handler);
      }

    if(sha256 != null){
      var metadata_sha256_digest = valuesReporter.getValues().get(Constants.SHA256_DIGEST_PATH).jsonLdValue();
      validateSingleArg(sha256, metadata_sha256_digest, "SHA256 digest", Constants.SHA256_DIGEST_PATH, handler);
    }
  }

  private void validateSingleArg(String providedValue, Optional<String> metadataValue, String fileName, String path, Consumer<ValidationResult> handler){
    // if value in metadata instance is null, populate warning
    if(metadataValue.isEmpty()){
      String message = String.format("The expected %s is %s, but an empty value is received." , fileName, providedValue);
      handler.accept(new ValidationResult(ValidationLevel.WARNING, ValidationName.RADX_PRECISION_VALIDATION, message, path));
    } else {
      if (!metadataValue.get().equals(providedValue)) {
        String message = String.format("The expected %s is %s, but %s is received.", fileName, providedValue, metadataValue.get());
        handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.RADX_PRECISION_VALIDATION, message, path));
      }
    }
  }
}
