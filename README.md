# RADx Metadata Validator

## Overview
The RADx Metadata Validator is a command line tool designed to validate RADx metadata instance against metadata template. This validator operates at four distinct levels to ensure comprehensive and accurate validation. It ensures that the metadata instances adhere to the standards defined in the RADx Metadata Template, which can be found at the [RADx Metadata Specification Docs](https://radx.github.io/radx-metadata-specification-docs/).

## Validation Workflow
Below is the workflow of RADx Metadata Validator:

![Workflow of RADx Metadata Validator](Workflow.png)

### Input Specification
The RADx Metadata Validator accepts the following command line arguments:

- `template` (Required): The file path to the metadata template file.
- `instance` (Required): The file path to the metadata instance file that needs to be validated.
- `out` (Optional): The file path where the validation report will be saved. If not provided, the validation report will be printed out to the console.

For example:

`java -jar radx-metadata-validator-app-1.0.0.jar --template validationFiles/RADxTemplate.json --instance validationFiles/RADxInstance.json --out output.csv`

### JSON File Validation
- Validates that the provided files are in proper JSON format. This is a preliminary check to ensure that the files are syntactically correct as per JSON standards.

### CEDAR Template Validation
- Validates that the provided template file is a CEDAR (Center for Expanded Data Annotation and Retrieval) compliant template. This step ensures that the template follows the specific structure and standards required by CEDAR.

### Schema Validation Against Template
- Validates that the schema of the provided instance matches the schema defined in the template.
- This level of validation ensures that the metadata instance aligns structurally with the template.

### Value Constraint Validation
- Validates other value constraints, which include:
    - Required value validation: Checks that all required data fields are present.
    - Data type validation: Ensures that the data types of the values in the metadata instance match those specified in the template.
    - Cardinality validation: Verifies that the quantity of element instances or field instances adheres to the specified minimum and maximum limits.

### Output File Format
The output of the RADx Metadata Validator is a CSV file with the following format:

- **Level**: Indicates the level of validation message, which can be either `ERROR` or `WARNING`.
- **Path**: Specifies the location within the file where the validation issue was found.
- **Validation Type**: Specifies which validation step (e.g., JSON Validation, CEDAR Model Validation, Schema Validation, Requirement Validation, Data Type Validation) generated the message.
- **Message**: Describes the validation error or warning message.

![Validation Report](ValidationReportExample.png)

### Usage
The validator can be executed through Maven's `exec:java` goal. Follow the steps below to validate your metadata instances:
1. **Navigate to the Application Directory**
   
    First, navigate to the `radx-metadata-validator-app` directory within the `radx-metadata-validator` project folder:

   ```
   cd radx-metadata-validator-app
   ```

2. **Validate Metadata Instance**
   
    To validate your metadata instances against a CEDAR template stored in a file, use the following Maven command. You need to specify the paths to your template file, instance file, and the output file where validation report will be saved:
    
   ```
   mvn exec:java 
     -Dexec.args="--template <template_file_path> 
                  --instance <instance_file_path> 
                  --out <validation_report_path>"
    ```

    e.g., 

    ```
    mvn exec:java 
     -Dexec.args="--template ../../RADxTemplate.json
                  --instance ../../RADxExampleMetadataInstance.json 
                  --out ../../Output.csv"
    ```
