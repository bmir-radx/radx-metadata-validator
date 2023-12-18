package edu.stanford.bmir.radx.radxmetadatavalidator;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import edu.stanford.bmir.radx.radxmetadatavalidator.ValidatorComponents.SchemaValidatorComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import picocli.CommandLine;

import java.nio.file.Files;
import java.nio.file.Path;

@SpringBootApplication
public class RadxMetadataValidatorApplication implements CommandLineRunner {
	private final CommandLine.IFactory iFactory;
	@Autowired
	private ApplicationContext context;
	private int exitCode;

	public RadxMetadataValidatorApplication(CommandLine.IFactory iFactory) {
		this.iFactory = iFactory;
	}

	public static void main(String[] args) {
		SpringApplication.run(RadxMetadataValidatorApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		// java-schema-validator
		var validator = context.getBean(Validator.class);
		Path template = Path.of("validationFiles/ControlledTermsTemplate.json");
		Path instance = Path.of("validationFiles/DataTypeInstance.json");
		var report = validator.validateInstance(template, instance);

		var validationReportWriter = context.getBean(ValidationReportWriter.class);
		var out = Files.newOutputStream(Path.of("target/output.txt"));
		validationReportWriter.writeReport(report, out);

		var validateCommand = context.getBean(ValidateCommand.class);
		exitCode = new CommandLine(validateCommand, iFactory).execute(args);

		// cedar-artifact-library
//		var schemaArtifactCreator = context.getBean(SchemaArtifactCreator.class);
//		TemplateSchemaArtifact templateSchemaArtifact = schemaArtifactCreator.createTemplateArtifact((ObjectNode) templateSchema);
//		System.out.println("Element names: " + templateSchemaArtifact.getElementNames());
//		System.out.println("Child names: " + templateSchemaArtifact.getChildNames());
//		var elementSchemaArtifact = templateSchemaArtifact.getElementSchemaArtifact("Element A");
//		System.out.println(elementSchemaArtifact.getFieldNames()) ;
//		var fieldSchemaArtifact = elementSchemaArtifact.getFieldSchemaArtifact("Field A in Element A");
//		System.out.println("Is requried: " + fieldSchemaArtifact.requiredValue());
//		System.out.println("Min length: " + fieldSchemaArtifact.minLength());
//		System.out.println("regex: " + fieldSchemaArtifact.regex());
//		System.out.println("Type: " + fieldSchemaArtifact.jsonSchemaType());
//		System.out.println("Value Constrains: " + fieldSchemaArtifact.valueConstraints());
//		System.out.println("Is static: " + fieldSchemaArtifact.isStatic());

//		var fieldSchemaArtifact2 = templateSchemaArtifact.getFieldSchemaArtifact("Numeric Field");
//		System.out.println(fieldSchemaArtifact2.valueConstraints());
//
//
//		TemplateInstanceArtifact templateInstanceArtifact = schemaArtifactCreator.createTemplateInstanceArtifact((ObjectNode) templateInstance);
//		var fieldInstancesMap = templateInstanceArtifact.fieldInstances();
//		fieldInstancesMap.forEach((fieldInstance, fieldInstanceArtifactList)-> {
//			System.out.println("field name: ---------" + fieldInstance);
//			fieldInstanceArtifactList.forEach((fieldInstanceArtifact) ->{
//			fieldInstanceArtifact.jsonLdValue();
//			fieldInstanceArtifact.label();
//			});
//		});
//
//		System.out.println("field instances map: " + fieldInstancesMap);
//
//		var elementInstancesMap = templateInstanceArtifact.elementInstances();
//		elementInstancesMap.forEach((name, elementSchemaArtifactList)-> {
//			System.out.println("element name: ---------" + name);
//			elementSchemaArtifactList.forEach(System.out::println);
//		});

//		System.out.println("element instances map: " + elementInstancesMap);

		//load constraints
//		var constrainsMapper = context.getBean(ConstraintsMapper.class);
//		System.out.println("Field Constraints:-----------------------" + constrainsMapper.getFieldConstraints(templateSchemaArtifact));
//		System.out.println("Element Constraints: ---------------------" + constrainsMapper.getElementConstraints(templateSchemaArtifact));


		// cedar-model-validation-library
//		CedarValidator cedarValidator = new CedarValidator();
//		System.out.println(cedarValidator.validateTemplateInstance(templateInstance, templateSchema));
	}

	public int getExitCode() {
		return exitCode;
	}
}
