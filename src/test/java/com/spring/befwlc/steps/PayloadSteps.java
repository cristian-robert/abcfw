package com.spring.befwlc.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.spring.befwlc.configuration.AzureTestReporter;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.FileCopyUtils;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class PayloadSteps {

    private final ObjectMapper objectMapper;
    private final ResourceLoader resourceLoader;
    private final AzureTestReporter azureTestReporter;

    @Given("I set testId for my current testCase")
    public void iSetTestIdForMyCurrentTestCase(DataTable dataTable) {
        Map<String, String> data = new HashMap<>(dataTable.asMap(String.class, String.class));

        String id = data.get("testCaseId");
//      azureTestReporter.setCurrentTestCaseId(id);
    }

    public String loadJsonFromPayloadFile(String jsonFile) throws Exception {
        String filePath = jsonFile.startsWith("/") ? jsonFile : "/payload/" + jsonFile;
        Resource resource = resourceLoader.getResource("classpath: " + filePath);

        try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
            String content = FileCopyUtils.copyToString(reader);
            objectMapper.readTree(content);
            return content;
        } catch (Exception e) {
            log.error("Error loading JSON file {}: {}", filePath, e.getMessage());
            throw new RuntimeException("Error loading JSON payload: " + filePath, e);
        }
    }

    public String updateJsonValue(String jsonString, String path, String value) {
        try {
            if (value == null || value.equalsIgnoreCase("null")) {
                return JsonPath.parse(jsonString)
                        .set(normalizePath(path), null)
                        .jsonString();
            }

            if (value.isEmpty()) {
                return JsonPath.parse(jsonString)
                        .set(normalizePath(path), "")
                        .jsonString();
            }

            if (value.matches("-?\\d+(\\.\\d+)?")) {
                if (value.contains(".")) {
                    return JsonPath.parse(jsonString)
                            .set(normalizePath(path), Double.parseDouble(value))
                            .jsonString();
                } else {
                    return JsonPath.parse(jsonString)
                            .set(normalizePath(path), Long.parseLong(value))
                            .jsonString();
                }
            }

            if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
                return JsonPath.parse(jsonString)
                        .set(normalizePath(path), Boolean.parseBoolean(value))
                        .jsonString();
            }

            if (value.equalsIgnoreCase("<null>") || value.equalsIgnoreCase("${null}")) {
                return JsonPath.parse(jsonString)
                        .set(normalizePath(path), null)
                        .jsonString();
            }

            return JsonPath.parse(jsonString)
                    .set(normalizePath(path), value)
                    .jsonString();
        } catch (PathNotFoundException e) {
            log.error("Path not found: {}", path);
            throw new RuntimeException("Invalid JSON path: " + path, e);
        } catch (Exception e) {
            log.error("Error updating JSON value for path: {}, value: {}", path, value);
            throw new RuntimeException("Error updating JSON value: " + e.getMessage(), e);
        }
    }

    private String normalizePath(String path) {
        return path.startsWith("$") ? path : "$." + path;
    }
}
