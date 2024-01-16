package edu.stanford.bmir.radx.metadata.validator.app;

import edu.stanford.bmir.radx.metadata.validator.lib.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import java.nio.file.Path;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;

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
