package edu.stanford.bmir.radx.metadata.validator.lib;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.metadatacenter.artifacts.model.core.ElementSchemaArtifact;
import org.metadatacenter.artifacts.model.core.TemplateInstanceArtifact;
import org.metadatacenter.artifacts.model.core.TemplateSchemaArtifact;
import org.metadatacenter.artifacts.model.reader.JsonSchemaArtifactReader;
import org.metadatacenter.artifacts.model.visitors.TemplateReporter;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Component
public class MetadataFillingReport {

  public Map<String, Integer> getSingleReport(String templateContent, String instanceContent){
    Map<String, Integer> counts = new HashMap<>();
    var templateNode = JsonLoader.loadJson(templateContent, "Template");
    var instanceNode = JsonLoader.loadJson(instanceContent, "Instance");

    //Read template and get valueConstraints map
    JsonSchemaArtifactReader jsonSchemaArtifactReader = new JsonSchemaArtifactReader();
    TemplateSchemaArtifact templateSchemaArtifact = jsonSchemaArtifactReader.readTemplateSchemaArtifact((ObjectNode) templateNode);
    TemplateReporter templateReporter = new TemplateReporter(templateSchemaArtifact);

    //Read instance and get values map
    TemplateInstanceArtifact templateInstanceArtifact = jsonSchemaArtifactReader.readTemplateInstanceArtifact((ObjectNode) instanceNode);
    TemplateInstanceValuesReporter templateInstanceValuesReporter = new TemplateInstanceValuesReporter(templateInstanceArtifact);

    var values = templateInstanceValuesReporter.getValues();
    var attributeValueFields = templateInstanceValuesReporter.getAttributeValueFields();
    //TODO missing attribute value fields
    for (Map.Entry<String, FieldValues> fieldEntry : values.entrySet()) {
      String path = fieldEntry.getKey();
      String normalizedPath = path.replaceAll("\\[\\d+\\]", "");
      FieldValues fieldValues = fieldEntry.getValue();
      var jsonLdValue = fieldValues.jsonLdValue();
      var jsonLdId = fieldValues.jsonLdId();
      var jsonLdLabel = fieldValues.label();

      boolean isEmptyId = jsonLdId.map(uri -> uri.toString().isEmpty()).orElse(true);
      boolean isEmptyLabel = jsonLdLabel.map(String::isEmpty).orElse(true);
      boolean isEmptyValue = jsonLdValue.map(String::isEmpty).orElse(true);
      if (isEmptyId && isEmptyLabel && isEmptyValue) {
        if(!counts.containsKey(normalizedPath)){
          counts.put(normalizedPath, 0);
        }
      } else{
        if(!counts.containsKey(normalizedPath)){
          counts.put(normalizedPath, 1);
        }
      }
    }
    return counts;
  }

  public void updateReport(Map<String, Integer> combinedFillingReport, Map<String, Integer> singleFillingReport){
    singleFillingReport.forEach((key, value) ->
        combinedFillingReport.merge(key, value, Integer::sum));
  }

  public void writeReport(Map<String, Integer> combinedFillingReport, String templateContent, Path outputDirectory){
    // order keys
    Map<String, Integer> sortedMap = new TreeMap<>(combinedFillingReport);
    Path reportPath = Paths.get(String.valueOf(outputDirectory), "metadata_filling_report.xlsx");

    var templateSchemaArtifact = getTemplateSchemaArtifact(templateContent);
    var allElements = getAllElements(templateSchemaArtifact);
    var emptyElements = getEmptyElements(combinedFillingReport, templateSchemaArtifact);

    try (Workbook workbook = new XSSFWorkbook()) {
      // Creating the report sheet
      Sheet reportSheet = workbook.createSheet("Report");
      Row headerRow = reportSheet.createRow(0);
      headerRow.createCell(0).setCellValue("FieldPath");
      headerRow.createCell(1).setCellValue("Count");

      int rowNum = 1;
      for (Map.Entry<String, Integer> entry : sortedMap.entrySet()) {
        Row row = reportSheet.createRow(rowNum++);
        row.createCell(0).setCellValue(entry.getKey());
        row.createCell(1).setCellValue(entry.getValue());
      }

      // Creating the empty elements sheet
      Sheet emptySheet = workbook.createSheet("Empty Elements");
      Row emptyHeaderRow = emptySheet.createRow(0);
      emptyHeaderRow.createCell(0).setCellValue("Empty Elements");
      int emptyRowNum = 1;
      for (String element : emptyElements) {
        Row row = emptySheet.createRow(emptyRowNum++);
        row.createCell(0).setCellValue(element);
      }

      //Creating all elements sheet
      Sheet elementSheet = workbook.createSheet("All Elements");
      Row elementHeaderRow = elementSheet.createRow(0);
      elementHeaderRow.createCell(0).setCellValue("All Elements");
      int elementRowNum = 1;
      for (String element : allElements) {
        Row row = elementSheet.createRow(elementRowNum++);
        row.createCell(0).setCellValue(element);
      }

      // Writing the workbook to the file
      FileOutputStream fileOut = new FileOutputStream(reportPath.toString());
      workbook.write(fileOut);
    } catch (Exception e) {
      System.err.println("Error writing the Excel file: " + e.getMessage());
    }
  }

  public Collection<String> getEmptyElements(Map<String, Integer> combinedFillingReport, TemplateSchemaArtifact templateSchemaArtifact){
    Set<String> emptyElements = new HashSet<>();
    var childElements = templateSchemaArtifact.getElementNames();
    for(var childElement: childElements){
      var currentElementArtifact = templateSchemaArtifact.getElementSchemaArtifact(childElement);
      if(isEmptyElements(combinedFillingReport, currentElementArtifact, "/" + childElement)){
        emptyElements.add(childElement);
      }
    }
    return emptyElements;
  }

  private boolean isEmptyElements(Map<String, Integer> combinedFillingReport, ElementSchemaArtifact elementSchemaArtifact, String path){
    var childFields = elementSchemaArtifact.getFieldNames();
    var childElements = elementSchemaArtifact.getElementNames();
    for(var childField: childFields){
      var currentPath = path + "/" + childField;
      System.out.println(currentPath);
      if(combinedFillingReport.containsKey(currentPath) && combinedFillingReport.get(currentPath) != 0){
        return false;
      }
    }

    for(var childElement:childElements){
      var childElementSchemaArtifact = elementSchemaArtifact.getElementSchemaArtifact(childElement);
      var currentPath = path + "/" + childElement;
      if(!isEmptyElements(combinedFillingReport, childElementSchemaArtifact, currentPath)){
        return false;
      }
    }
    return true;
  }

  private Collection<String> getAllElements(TemplateSchemaArtifact templateSchemaArtifact){
    var childElements = templateSchemaArtifact.getElementNames();
    return new HashSet<>(childElements);
  }

  private TemplateSchemaArtifact getTemplateSchemaArtifact(String templateContent){
    var templateNode = JsonLoader.loadJson(templateContent, "Template");
    JsonSchemaArtifactReader jsonSchemaArtifactReader = new JsonSchemaArtifactReader();
    return jsonSchemaArtifactReader.readTemplateSchemaArtifact((ObjectNode) templateNode);
  }
}
