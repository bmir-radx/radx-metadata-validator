package edu.stanford.bmir.radx.metadata.validator.lib;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import org.metadatacenter.artifacts.model.core.fields.constraints.ControlledTermValueConstraints;
import org.metadatacenter.artifacts.model.core.fields.constraints.ValueConstraints;
import org.metadatacenter.artifacts.util.ConnectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class TerminologyServerHandler {
  private final ObjectMapper mapper;
  private final ObjectWriter objectWriter;
  private final String terminologyServerAPIKey;
  private final String terminologyServerEndPoint;
  private final Map<ValueConstraints, Map<String, String>> cache;
  private static final Logger log = LoggerFactory.getLogger(TerminologyServerHandler.class);

  public TerminologyServerHandler(String terminologyServerAPIKey, String terminologyServerEndPoint) {
    this.terminologyServerAPIKey = terminologyServerAPIKey;
    this.terminologyServerEndPoint = terminologyServerEndPoint;

    this.mapper = new ObjectMapper();
    mapper.registerModule(new Jdk8Module());
    mapper.setSerializationInclusion(JsonInclude.Include.NON_ABSENT);
    this.objectWriter = mapper.writer().withDefaultPrettyPrinter();
    this.cache = new HashMap<>();
  }

  public String getTerminologyServerAPIKey() {
    return terminologyServerAPIKey;
  }

  public String getTerminologyServerEndPoint() {
    return terminologyServerEndPoint;
  }

  public Map<String, String> getAllValues(ControlledTermValueConstraints valueConstraints) {
    long startTime = System.nanoTime();
    int page = 1;
    int pageSize = 4999;
    try {
      if(cache.containsKey(valueConstraints)){
//        log.info("Loading <URI, prefLabel> pairs from cache");
        return cache.get(valueConstraints);
      } else{
        String vc = controlledTermValueConstraints2Json(valueConstraints);
        Map<String, Object> vcMap = mapper.readValue(vc, Map.class);
        Map<String, String> allValues = new HashMap<>();

        Map<String, Object> searchResult;
        do {
          searchResult = integratedSearch(vcMap, page, pageSize, terminologyServerEndPoint, terminologyServerAPIKey);
          List<Map<String, String>> valueDescriptions = searchResult.containsKey("collection") ?
              (List<Map<String, String>>) searchResult.get("collection") :
              new ArrayList<>();
          for (Map<String, String> description : valueDescriptions) {
            String uri = description.get("@id");
            String prefLabel = description.get("prefLabel");
            allValues.put(uri, prefLabel);
          }
          page++;
        } while (searchResult.containsKey("nextPage") && searchResult.get("nextPage") != null);

        cache.put(valueConstraints, allValues);
//        log.info("Loading <URI, prefLabel> pairs from terminology server");
        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1_000_000;
        log.info("Execution time: " + duration + " ms");
        return allValues;
      }
    } catch (IOException | RuntimeException e) {
      throw new RuntimeException("Error retrieving values from terminology server " + e.getMessage());
    }
  }

  private String controlledTermValueConstraints2Json(ControlledTermValueConstraints controlledTermValueConstraints)
  {
    try {
//      return objectWriter.writeValueAsString(controlledTermValueConstraints);
      String vc = objectWriter.writeValueAsString(controlledTermValueConstraints);
      return patchValueConstraints(vc);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Error generation value constraints object for terminology server " + e.getMessage());
    }
  }

  /***
   * The value constraints in RADx template missing valueSets entry
   * This method aims to patch value constraints with empty valueSets
   * @param valueConstraints
   * @return
   */
  private String patchValueConstraints(String valueConstraints) throws JsonProcessingException {
    ObjectNode jsonNode = (ObjectNode) mapper.readTree(valueConstraints);

    if (!jsonNode.has("valueSets")) {
      jsonNode.putArray("valueSets");
      return mapper.writeValueAsString(jsonNode);
    }

    return valueConstraints;
  }

  private Map<String, Object> integratedSearch(Map<String, Object> valueConstraints,
                                               Integer page, Integer pageSize, String integratedSearchEndpoint, String apiKey) throws IOException, RuntimeException
  {
    HttpURLConnection connection = null;
    Map<String, Object> resultsMap;
    try {
      Map<String, Object> vcMap = new HashMap<>();
//      vcMap.put("valueConstraints", valueConstraints);
      Map<String, Object> payloadMap = new HashMap<>();
//      payloadMap.put("parameterObject", vcMap);
      payloadMap.put("valueConstraints", valueConstraints);
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

  /***
   * Get URI, prefLabel key-value pairs from CEDAR terminology server
   * @param vcMap
   * @param page
   * @param pageSize
   * @return
   */
  private Map<String, String> getSingleValuesFromTerminologyServer(Map<String, Object> vcMap, int page, int pageSize){
    Map<String, String> values = new HashMap<>();

    try {
      List<Map<String, String>> valueDescriptions;
      Map<String, Object> searchResult = integratedSearch(vcMap, page, pageSize,
          terminologyServerEndPoint, terminologyServerAPIKey);

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


  private String getNextPage(Map<String, Object> vcMap, int page, int pageSize) throws IOException {
    Map<String, Object> searchResult = integratedSearch(vcMap, page, pageSize,
        terminologyServerEndPoint, terminologyServerAPIKey);
    return searchResult.get("nextPage").toString();
  }
}
