package edu.stanford.bmir.radx.metadata.validator.lib.validators;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import edu.stanford.bmir.radx.metadata.validator.lib.*;
import org.metadatacenter.artifacts.model.core.fields.constraints.ControlledTermValueConstraints;
import org.metadatacenter.artifacts.model.visitors.TemplateReporter;
import org.metadatacenter.artifacts.util.ConnectionUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ControlledTermValidatorComponent {
  private final ObjectMapper mapper;
  private final ObjectWriter objectWriter;
  private final String terminologyServerAPIKey;

  public ControlledTermValidatorComponent(String terminologyServerAPIKey) {
    this.terminologyServerAPIKey = terminologyServerAPIKey;

    this.mapper = new ObjectMapper();
    mapper.registerModule(new Jdk8Module());
    mapper.setSerializationInclusion(JsonInclude.Include.NON_ABSENT);
    this.objectWriter = mapper.writer().withDefaultPrettyPrinter();
  }

  public void validate(TemplateReporter templateReporter, TemplateInstanceValuesReporter valuesReporter, Consumer<ValidationResult> handler){
    if (this.terminologyServerAPIKey != null){
      var values = valuesReporter.getValues();

      for (Map.Entry<String, FieldValues> fieldEntry : values.entrySet()) {
        String path = fieldEntry.getKey();
        FieldValues fieldValues = fieldEntry.getValue();
        var jsonLdId = fieldValues.jsonLdId();
        var jsonLdLabel = fieldValues.label();
        var valueConstraint = templateReporter.getValueConstraints(path);
        if(valueConstraint.isPresent() && valueConstraint.get().isControlledTermValueConstraint()){
          var controlledTermValues = getValuesFromTerminologyServer(valueConstraint.get().asControlledTermValueConstraints());
          if(jsonLdId.isPresent()){
            //check if @id is within values get from terminology server
            var id = jsonLdId.get().toString();
            if(controlledTermValues.containsKey(id)){
              //check prefLabel
              var prefLabel = controlledTermValues.get(id);
              if(jsonLdLabel.isPresent() && !jsonLdLabel.get().equals(prefLabel)){
                String warningMessage = String.format("Expected %s on 'rdfs:label', but %s is given.", prefLabel, jsonLdLabel.get());
                handler.accept(new ValidationResult(ValidationLevel.WARNING, ValidationName.CONTROLLED_TERM_VALIDATION, warningMessage, path));
              }
            } else{
              String errorMessage = String.format("%s is not an element of set", jsonLdId.get());
              handler.accept(new ValidationResult(ValidationLevel.ERROR, ValidationName.CONTROLLED_TERM_VALIDATION, errorMessage, path));
            }
          }
        }
      }
    }
  }

  /***
   * Get prefLabel, URI key-value pairs from CEDAR terminology server
   * @param valueConstraints
   * @return
   */
  private Map<String, String> getValuesFromTerminologyServer(ControlledTermValueConstraints valueConstraints){
    Map<String, String> values = new HashMap<>();

    try {
      String vc = controlledTermValueConstraints2Json(valueConstraints);
      Map<String, Object> vcMap = mapper.readValue(vc, Map.class);

      List<Map<String, String>> valueDescriptions;
      Map<String, Object> searchResult = integratedSearch(vcMap, 1, 4999,
          Constants.TERMINOLOGY_SERVER_INTEGRATED_SEARCH_ENDPOINT, terminologyServerAPIKey);
      valueDescriptions = searchResult.containsKey("collection") ?
          (List<Map<String, String>>)searchResult.get("collection") :
          new ArrayList<>();
      if (valueDescriptions.size() > 0) {
        for (int valueDescriptionsIndex = 0; valueDescriptionsIndex < valueDescriptions.size(); valueDescriptionsIndex++) {
          String uri = valueDescriptions.get(valueDescriptionsIndex).get("@id");
          String prefLabel = valueDescriptions.get(valueDescriptionsIndex).get("prefLabel");
          values.put(uri, prefLabel);
        }
      }
    } catch (IOException | RuntimeException e) {
      throw new RuntimeException("Error retrieving values from terminology server " + e.getMessage());
    }
    return values;
  }

  private String controlledTermValueConstraints2Json(ControlledTermValueConstraints controlledTermValueConstraints)
  {
    try {
      return objectWriter.writeValueAsString(controlledTermValueConstraints);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Error generation value constraints object for terminology server " + e.getMessage());
    }
  }

  private Map<String, Object> integratedSearch(Map<String, Object> valueConstraints,
                                               Integer page, Integer pageSize, String integratedSearchEndpoint, String apiKey) throws IOException, RuntimeException
  {
    HttpURLConnection connection = null;
    Map<String, Object> resultsMap;
    try {
      Map<String, Object> vcMap = new HashMap<>();
      vcMap.put("valueConstraints", valueConstraints);
      Map<String, Object> payloadMap = new HashMap<>();
      payloadMap.put("parameterObject", vcMap);
      payloadMap.put("page", page);
      payloadMap.put("pageSize", pageSize);
      String payload = mapper.writeValueAsString(payloadMap);
      connection = ConnectionUtil.createAndOpenConnection("POST", integratedSearchEndpoint, apiKey);
      OutputStream os = connection.getOutputStream();
      os.write(payload.getBytes());
      os.flush();
      int responseCode = connection.getResponseCode();
      if (responseCode >= HttpURLConnection.HTTP_BAD_REQUEST) {
        String message = "Error running integrated search. Response code: " + responseCode + "; Payload: " + payload;
        throw new RuntimeException(message);
      } else {
        String response = ConnectionUtil.readResponseMessage(connection.getInputStream());
        resultsMap = mapper.readValue(response, HashMap.class);
      }
    } finally {
      if (connection != null) {
        connection.disconnect();
      }
    }
    return resultsMap;
  }
}
