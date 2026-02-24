package com.spring.befwlc.v2.payload;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class PayloadLoader {
    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper;

    public String loadJson(String jsonFile) {
        String path = jsonFile.startsWith("/") ? jsonFile : "/payload/" + jsonFile;
        Resource resource = resourceLoader.getResource("classpath:" + path);
        try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
            String content = FileCopyUtils.copyToString(reader);
            objectMapper.readTree(content);
            return content;
        } catch (Exception e) {
            log.error("Error loading JSON file {}: {}", path, e.getMessage());
            throw new RuntimeException("Error loading JSON payload: " + path, e);
        }
    }
}
