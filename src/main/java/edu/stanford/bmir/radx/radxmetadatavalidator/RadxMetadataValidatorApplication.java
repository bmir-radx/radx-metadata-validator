package edu.stanford.bmir.radx.radxmetadatavalidator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import picocli.CommandLine;

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
		var validateCommand = context.getBean(ValidateCommand.class);
		exitCode = new CommandLine(validateCommand, iFactory).execute(args);
	}

	public int getExitCode() {
		return exitCode;
	}
}
