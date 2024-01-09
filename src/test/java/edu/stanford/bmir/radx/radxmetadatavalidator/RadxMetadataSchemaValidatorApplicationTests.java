package edu.stanford.bmir.radx.radxmetadatavalidator;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import java.nio.file.Path;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
class RadxMetadataSchemaValidatorApplicationTests {
	@Autowired
	private ApplicationContext context;
	@Autowired
	private Validator validator;

	@Test
	void shouldValidate() throws Exception{
		Path templatePath = mock(Path.class);
		Path instancePath = mock(Path.class);

		var report = validator.validateInstance(templatePath, instancePath);

		assertThat(report).isNotNull();
	}

}
