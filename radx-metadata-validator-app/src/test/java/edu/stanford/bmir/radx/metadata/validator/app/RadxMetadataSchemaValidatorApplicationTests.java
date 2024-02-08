package edu.stanford.bmir.radx.metadata.validator.app;

import edu.stanford.bmir.radx.metadata.validator.lib.FieldPath;
import edu.stanford.bmir.radx.metadata.validator.lib.LiteralFieldValidator;
import edu.stanford.bmir.radx.metadata.validator.lib.ValidatorFactory;
import edu.stanford.bmir.radx.metadata.validator.lib.validators.LiteralFieldValidatorsComponent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
class RadxMetadataSchemaValidatorApplicationTests {
	@Autowired
	private ValidatorFactory validatorFactory;

	@Test
	void shouldValidate(@TempDir Path tempDir) throws Exception{
		Path templatePath = Files.createTempFile(tempDir, "template", ".tmp");
		Path instancePath = Files.createTempFile(tempDir, "instance", ".tmp");

		var map = new HashMap<FieldPath, LiteralFieldValidator>();
		var validator = validatorFactory.createValidator(new LiteralFieldValidatorsComponent(map));
		String templateContent = Arrays.toString(Files.readAllBytes(templatePath));
		String instanceContent = Arrays.toString(Files.readAllBytes(instancePath));
		var report = validator.validateInstance(templateContent, instanceContent);

		assertThat(report).isNotNull();
	}
}
