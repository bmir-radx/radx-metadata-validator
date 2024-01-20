package edu.stanford.bmir.radx.metadata.validator.lib;

import org.metadatacenter.artifacts.model.core.TemplateInstanceArtifact;
import org.metadatacenter.artifacts.model.core.TemplateSchemaArtifact;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
public class SanitationChecker {
  public void validate(TemplateSchemaArtifact templateSchemaArtifact, TemplateInstanceArtifact templateInstanceArtifact, Consumer<ValidationResult> consumer){
    var templateId = templateSchemaArtifact.jsonLdId();
    var instanceId = templateInstanceArtifact.isBasedOn();
    if(templateId.isPresent() && !templateId.get().equals(instanceId)){
      String message = String.format("Instance is not based on %s", templateId.get());
      consumer.accept(new ValidationResult(ValidationLevel.WARNING, ValidationName.SANITATION_CHECK, message, ""));
    }
  }
}
