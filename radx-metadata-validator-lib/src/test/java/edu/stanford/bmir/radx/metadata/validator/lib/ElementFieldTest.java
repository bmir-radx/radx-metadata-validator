package edu.stanford.bmir.radx.metadata.validator.lib;

import edu.stanford.bmir.radx.metadata.validator.lib.validators.RequiredFieldValidatorComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.metadatacenter.artifacts.model.core.*;
import org.metadatacenter.artifacts.model.visitors.TemplateReporter;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ElementFieldTest {

    public static final String FIELD_NAME = "Person first name";

    public static final String ELEMENT_NAME = "Person details";

    public static final String TEMPLATE_NAME = "Person template";

    public final URI TEMPLATE_URI = URI.create("http://example.org/the-template" );


    private TemplateSchemaArtifact template;

    private RequiredFieldValidatorComponent validator;

    private TemplateReporter templateReporter;


    @Mock
    private Consumer<ValidationResult> validationResultHandler;


    @BeforeEach
    void setUp() {
        var field = TextField.builder()
                .withName(FIELD_NAME)
                .withRequiredValue(true)
                .build();

        var element = ElementSchemaArtifact.builder()
                .withName(ELEMENT_NAME)
                .withMinItems(0)
                .withFieldSchema(field)
                .build();

        template = TemplateSchemaArtifact.builder()
                .withJsonLdId(TEMPLATE_URI)
                .withName(TEMPLATE_NAME)
                .withElementSchema(element)
                .build();

        validator = new RequiredFieldValidatorComponent();
        templateReporter = new TemplateReporter(template);
    }

    @Test
    public void shouldPassCheckWithNoElementInstance() {
        var instance = TemplateInstanceArtifact.builder()
                .withIsBasedOn(TEMPLATE_URI)
                .build();
        var valuesReporter = new TemplateInstanceValuesReporter(instance);
        validator.validate(template, templateReporter, valuesReporter, validationResultHandler);
        verify(validationResultHandler, never()).accept(any());
    }

    @Test
    public void shouldPassCheckWithElementInstanceAndFieldInstance() {
        var fieldInstance = new TextFieldInstance.TextFieldInstanceBuilder()
                .withValue("John")
                .build();
        var elementInstance = ElementInstanceArtifact.builder()
                .withName(ELEMENT_NAME)
                .withSingleInstanceFieldInstance(FIELD_NAME, fieldInstance)
                .build();
        var templateInstance = TemplateInstanceArtifact.builder()
                .withIsBasedOn(TEMPLATE_URI)
                .withSingleInstanceElementInstance(ELEMENT_NAME, elementInstance)
                .build();
        var valuesReporter = new TemplateInstanceValuesReporter(templateInstance);
        validator.validate(template, templateReporter, valuesReporter, validationResultHandler);
        verify(validationResultHandler, never()).accept(any());
    }

    @Test
    public void shouldFailCheckWithElementInstanceAndNoFieldInstance() {
        var elementInstance = ElementInstanceArtifact.builder()
                .withName(ELEMENT_NAME)
                .build();
        var templateInstance = TemplateInstanceArtifact.builder()
                .withIsBasedOn(TEMPLATE_URI)
                .withSingleInstanceElementInstance(ELEMENT_NAME, elementInstance)
                .build();
        var valuesReporter = new TemplateInstanceValuesReporter(templateInstance);
        validator.validate(template, templateReporter, valuesReporter, validationResultHandler);
        verify(validationResultHandler, times(1)).accept(argThat(validationResult -> {
            return validationResult.validationLevel().equals(ValidationLevel.ERROR)
                    && validationResult.pointer().equals("/" + ELEMENT_NAME + "/" + FIELD_NAME);
        }));
    }

}
