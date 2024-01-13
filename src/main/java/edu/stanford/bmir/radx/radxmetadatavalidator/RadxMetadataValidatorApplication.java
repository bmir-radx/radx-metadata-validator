package edu.stanford.bmir.radx.radxmetadatavalidator;

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
		var validator = context.getBean(Validator.class);
		Path template = Path.of("validationFiles/CheckboxTemplate.json");
		Path instance = Path.of("validationFiles/CheckboxInstance.json");
		var report = validator.validateInstance(template, instance);

		var validationReportWriter = context.getBean(ValidationReportWriter.class);
		var out = Files.newOutputStream(Path.of("target/output.csv"));
		validationReportWriter.writeReport(report, out);

//		var validateCommand = context.getBean(ValidateCommand.class);
//		exitCode = new CommandLine(validateCommand, iFactory).execute(args);
	}

	public int getExitCode() {
		return exitCode;
	}
}
