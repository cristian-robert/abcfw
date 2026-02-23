# befwlc-v2 Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Build a clean, modern Maven module (`befwlc-v2`) that replicates all beFwLc framework functionality using proper Spring Boot 3.x patterns.

**Architecture:** Separate Maven module with `@ConfigurationProperties` for all config, Awaitility for polling, registry-based DSL system (no enums, no Service Locator), clean Cucumber step separation, and interface-based reporting.

**Tech Stack:** Java 17, Spring Boot 3.5.6, Kafka + Avro + Confluent Schema Registry, Cucumber 7.15.0 + TestNG 7.8.0, Awaitility, ExtentReports 5.1.1, Azure DevOps REST API, Jackson, JsonPath.

---

### Task 1: Maven Module Scaffolding

**Files:**
- Create: `befwlc-v2/pom.xml`
- Create: `befwlc-v2/src/main/java/com/spring/befwlc/v2/BeFwLcV2Application.java`
- Create: `befwlc-v2/src/main/resources/application.properties`

**Step 1: Create the befwlc-v2 directory and pom.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.5.6</version>
        <relativePath/>
    </parent>
    <groupId>com.spring</groupId>
    <artifactId>befwlc-v2</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>befwlc-v2</name>
    <description>Clean Billing Engine Test Framework v2</description>

    <repositories>
        <repository>
            <id>confluent</id>
            <url>https://packages.confluent.io/maven/</url>
        </repository>
    </repositories>

    <properties>
        <java.version>17</java.version>
        <cucumber.version>7.15.0</cucumber.version>
        <testng.version>7.8.0</testng.version>
    </properties>

    <dependencies>
        <!-- Spring Boot -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- Kafka -->
        <dependency>
            <groupId>org.springframework.kafka</groupId>
            <artifactId>spring-kafka</artifactId>
        </dependency>
        <dependency>
            <groupId>org.apache.kafka</groupId>
            <artifactId>kafka-clients</artifactId>
            <version>3.9.1</version>
        </dependency>
        <dependency>
            <groupId>io.confluent</groupId>
            <artifactId>kafka-avro-serializer</artifactId>
            <version>7.9.2</version>
        </dependency>

        <!-- JSON -->
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
            <version>2.19.2</version>
        </dependency>
        <dependency>
            <groupId>com.jayway.jsonpath</groupId>
            <artifactId>json-path</artifactId>
            <version>2.9.0</version>
        </dependency>

        <!-- Reporting -->
        <dependency>
            <groupId>com.aventstack</groupId>
            <artifactId>extentreports</artifactId>
            <version>5.1.1</version>
        </dependency>
        <dependency>
            <groupId>tech.grasshopper</groupId>
            <artifactId>extentreports-cucumber7-adapter</artifactId>
            <version>1.14.0</version>
        </dependency>

        <!-- Await -->
        <dependency>
            <groupId>org.awaitility</groupId>
            <artifactId>awaitility</artifactId>
        </dependency>

        <!-- Encryption -->
        <dependency>
            <groupId>org.bouncycastle</groupId>
            <artifactId>bcprov-jdk18on</artifactId>
            <version>1.76</version>
        </dependency>

        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- Cucumber + TestNG (test scope) -->
        <dependency>
            <groupId>io.cucumber</groupId>
            <artifactId>cucumber-java</artifactId>
            <version>${cucumber.version}</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>io.cucumber</groupId>
            <artifactId>cucumber-spring</artifactId>
            <version>${cucumber.version}</version>
        </dependency>
        <dependency>
            <groupId>io.cucumber</groupId>
            <artifactId>cucumber-testng</artifactId>
            <version>${cucumber.version}</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testng</groupId>
            <artifactId>testng</artifactId>
            <version>${testng.version}</version>
            <scope>test</scope>
        </dependency>

        <!-- Test -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.kafka</groupId>
            <artifactId>spring-kafka-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <configuration>
                    <annotationProcessorPaths>
                        <path>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </path>
                    </annotationProcessorPaths>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.apache.avro</groupId>
                <artifactId>avro-maven-plugin</artifactId>
                <version>1.11.3</version>
                <executions>
                    <execution>
                        <phase>generate-sources</phase>
                        <goals>
                            <goal>schema</goal>
                        </goals>
                        <configuration>
                            <sourceDirectory>${project.basedir}/src/main/avro/</sourceDirectory>
                            <outputDirectory>${project.basedir}/src/main/java/</outputDirectory>
                            <includes>
                                <include>**/*.avsc</include>
                            </includes>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

**Step 2: Create Spring Boot application entry point**

Create `befwlc-v2/src/main/java/com/spring/befwlc/v2/BeFwLcV2Application.java`:

```java
package com.spring.befwlc.v2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BeFwLcV2Application {
    public static void main(String[] args) {
        SpringApplication.run(BeFwLcV2Application.class, args);
    }
}
```

**Step 3: Create empty application.properties**

Create `befwlc-v2/src/main/resources/application.properties`:

```properties
spring.main.web-application-type=none
```

**Step 4: Copy the Avro schema**

Copy `src/main/avro/transaction_created_schema.avsc` to `befwlc-v2/src/main/avro/transaction_created_schema.avsc` (exact same file).

**Step 5: Generate Avro classes and verify compile**

Run: `cd befwlc-v2 && ../mvnw compile`
Expected: BUILD SUCCESS. Avro classes generated under `src/main/java/test/prof/events/`.

**Step 6: Commit**

```
feat: scaffold befwlc-v2 Maven module with Avro schema
```

---

### Task 2: Exception Classes

**Files:**
- Create: `befwlc-v2/src/main/java/com/spring/befwlc/v2/exception/TestExecutionException.java`
- Create: `befwlc-v2/src/main/java/com/spring/befwlc/v2/exception/MessageNotFoundException.java`

**Step 1: Create TestExecutionException**

```java
package com.spring.befwlc.v2.exception;

public class TestExecutionException extends RuntimeException {

    public TestExecutionException(String message, Object... args) {
        super(args.length > 0 ? String.format(message, args) : message);
    }

    public TestExecutionException(String message, Exception cause) {
        super(message, cause);
    }
}
```

**Step 2: Create MessageNotFoundException**

```java
package com.spring.befwlc.v2.exception;

public class MessageNotFoundException extends TestExecutionException {

    public MessageNotFoundException(String message, Object... args) {
        super(message, args);
    }
}
```

**Step 3: Verify compile**

Run: `cd befwlc-v2 && ../mvnw compile`

**Step 4: Commit**

```
feat: add exception hierarchy
```

---

### Task 3: Configuration Properties Classes

**Files:**
- Create: `befwlc-v2/src/main/java/com/spring/befwlc/v2/config/KafkaProperties.java`
- Create: `befwlc-v2/src/main/java/com/spring/befwlc/v2/config/AwaitProperties.java`
- Create: `befwlc-v2/src/main/java/com/spring/befwlc/v2/config/AzureProperties.java`
- Create: `befwlc-v2/src/main/java/com/spring/befwlc/v2/config/JacksonConfig.java`

**Step 1: Create KafkaProperties**

```java
package com.spring.befwlc.v2.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "kafka")
@Validated
@Getter
@Setter
public class KafkaProperties {

    @NotBlank
    private String bootstrapServers;

    @NotBlank
    private String schemaRegistryUrl;

    @NotBlank
    private String securityProtocol;

    @NotBlank
    private String publishTopic;

    @NotBlank
    private String consumeTopic;

    private Ssl ssl = new Ssl();
    private Consumer consumer = new Consumer();
    private Producer producer = new Producer();

    @Getter
    @Setter
    public static class Ssl {
        private String keyStoreLocation;
        private String keyStorePassword;
        private String trustStoreLocation;
        private String trustStorePassword;
        private String keyPassword;
        private String keyStoreType = "pkcs12";
    }

    @Getter
    @Setter
    public static class Consumer {
        private String autoOffsetReset = "latest";
        private int pollTimeoutMs = 1000;
    }

    @Getter
    @Setter
    public static class Producer {
        private String acks = "all";
        private int retries = 10;
        private boolean enableIdempotence = false;
        private int maxInFlightRequestsPerConnection = 1;
    }
}
```

**Step 2: Create AwaitProperties**

```java
package com.spring.befwlc.v2.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "await")
@Getter
@Setter
public class AwaitProperties {
    private int timeoutSeconds = 90;
    private int pollIntervalSeconds = 1;
    private int listenerTimeoutSeconds = 60;
}
```

**Step 3: Create AzureProperties**

```java
package com.spring.befwlc.v2.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "azure")
@Getter
@Setter
public class AzureProperties {
    private String organization;
    private String project;
    private String pat;
    private String testPlanId;
    private String suiteId;
    private boolean updateResults = false;
}
```

**Step 4: Create JacksonConfig**

```java
package com.spring.befwlc.v2.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
}
```

**Step 5: Verify compile**

Run: `cd befwlc-v2 && ../mvnw compile`

**Step 6: Commit**

```
feat: add @ConfigurationProperties for Kafka, Await, Azure and Jackson config
```

---

### Task 4: Utility Classes

**Files:**
- Create: `befwlc-v2/src/main/java/com/spring/befwlc/v2/util/DateUtils.java`
- Create: `befwlc-v2/src/main/java/com/spring/befwlc/v2/util/RegexUtils.java`

**Step 1: Create DateUtils**

Port all date methods using pure `java.time`. Keep exact same format constants and method signatures:

```java
package com.spring.befwlc.v2.util;

import com.spring.befwlc.v2.exception.TestExecutionException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public final class DateUtils {

    public static final String FULL_DATE_AND_OFFSET_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX";
    public static final String FULL_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'";
    public static final String SHORT_DATE_FORMAT = "yyyy-MM-dd";
    public static final String ISO_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    private DateUtils() {}

    public static String shortDate() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern(SHORT_DATE_FORMAT));
    }

    public static String fullOffsetDate() {
        return OffsetDateTime.now().format(DateTimeFormatter.ofPattern(FULL_DATE_AND_OFFSET_FORMAT));
    }

    public static String fullOffsetDatePlusOneHour() {
        return OffsetDateTime.now().plusHours(1).format(DateTimeFormatter.ofPattern(FULL_DATE_AND_OFFSET_FORMAT));
    }

    public static String fullDatePlusMinutes(int minutes) {
        return LocalDateTime.now().plusMinutes(minutes).format(DateTimeFormatter.ofPattern(FULL_DATE_FORMAT));
    }

    public static String shortDatePlusDays(int days) {
        return LocalDate.now().plusDays(days).format(DateTimeFormatter.ofPattern(SHORT_DATE_FORMAT));
    }

    public static String shortDateMinusDays(int days) {
        return LocalDate.now().minusDays(days).format(DateTimeFormatter.ofPattern(SHORT_DATE_FORMAT));
    }

    public static String fullDateMinusMinutes(int minutes) {
        return LocalDateTime.now().minusMinutes(minutes).format(DateTimeFormatter.ofPattern(FULL_DATE_FORMAT));
    }

    public static String ofFormat(String format) {
        try {
            return LocalDateTime.now().format(DateTimeFormatter.ofPattern(format));
        } catch (Exception e) {
            throw new TestExecutionException("Invalid date format pattern: %s", format);
        }
    }

    public static String fullDate() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(FULL_DATE_FORMAT));
    }

    public static String isoDate() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(ISO_DATE_FORMAT));
    }

    public static String reformatDate(String input, String fromPattern, String toPattern) {
        return LocalDateTime.parse(input, DateTimeFormatter.ofPattern(fromPattern))
                .format(DateTimeFormatter.ofPattern(toPattern));
    }

    public static String valueDateSkippingWeekends(int spotDays) {
        LocalDate target = LocalDate.now().plusDays(spotDays);
        if (target.getDayOfWeek() == DayOfWeek.SATURDAY) {
            target = target.plusDays(2);
        } else if (target.getDayOfWeek() == DayOfWeek.SUNDAY) {
            target = target.plusDays(1);
        }
        return target.format(DateTimeFormatter.ofPattern(SHORT_DATE_FORMAT));
    }
}
```

**Step 2: Create RegexUtils**

Port exact same regex patterns and utility methods:

```java
package com.spring.befwlc.v2.util;

import com.spring.befwlc.v2.exception.TestExecutionException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class RegexUtils {

    public static final String JSON_IS_ARRAY_ACCESS = ".*\\[\\d+]$";
    public static final String JSON_ARRAY_INDEX = "\\[.*";
    public static final String STATIC_METHOD_PATTERN = "^(\\$[A-Z0-9_]+).*";
    public static final String STATIC_METHOD_NAME = "^(\\$[A-Z0-9_]+)";
    public static final String CALLABLE_METHOD_PATTERN = "^(\\$[A-Z0-9_]+\\(.*\\))";
    public static final String CALLABLE_METHOD_NAME = "^(\\$[A-Z0-9_]+\\()";
    public static final String CALLABLE_METHOD_ARGS = "(\\$[A-Z0-9_]+\\(([^,)]+(,[^,)]+)*\\))|([^,]+)";

    private RegexUtils() {}

    public static List<String> captureAll(String input, String regex) {
        return captureAll(input, regex, 0);
    }

    public static List<String> captureAll(String input, String regex, int groupIndex) {
        Matcher matcher = Pattern.compile(regex).matcher(input);
        List<String> values = new ArrayList<>();
        while (matcher.find()) {
            values.add(matcher.group(groupIndex));
        }
        return values;
    }

    public static String captureSingle(String input, String regex) {
        List<String> values = captureAll(input, regex, 1);
        if (values.size() != 1) {
            throw new TestExecutionException("Regex capture failed. Pattern: '%s', Input: '%s'", regex, input);
        }
        return values.get(0);
    }

    public static boolean matches(String input, String regex) {
        return input.matches(regex);
    }
}
```

**Step 3: Verify compile**

Run: `cd befwlc-v2 && ../mvnw compile`

**Step 4: Commit**

```
feat: add DateUtils and RegexUtils
```

---

### Task 5: Context Management

**Files:**
- Create: `befwlc-v2/src/main/java/com/spring/befwlc/v2/context/ContextKey.java`
- Create: `befwlc-v2/src/main/java/com/spring/befwlc/v2/context/ScenarioContext.java`

**Step 1: Create ContextKey enum**

```java
package com.spring.befwlc.v2.context;

public enum ContextKey {
    PAYLOAD_VALUES,
    LAST_PAYLOAD,
    LAST_MATCHED_RECORD,
    BENEFICIARY_ID,
    TX_SEQ
}
```

**Step 2: Create ScenarioContext**

```java
package com.spring.befwlc.v2.context;

import com.spring.befwlc.v2.exception.TestExecutionException;
import io.cucumber.spring.ScenarioScope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@ScenarioScope
public class ScenarioContext {

    private final Map<ContextKey, Object> store = new HashMap<>();

    public void put(ContextKey key, Object value) {
        store.put(key, value);
    }

    public <T> T get(ContextKey key, Class<T> type) {
        Object value = store.get(key);
        if (value == null) {
            throw new TestExecutionException("No value found in ScenarioContext for key: %s", key);
        }
        if (!type.isAssignableFrom(value.getClass())) {
            throw new TestExecutionException(
                    "Type mismatch for key '%s': expected %s but found %s",
                    key, type.getSimpleName(), value.getClass().getSimpleName());
        }
        return type.cast(value);
    }

    public boolean contains(ContextKey key) {
        return store.containsKey(key);
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> getPayloadValues() {
        if (!store.containsKey(ContextKey.PAYLOAD_VALUES)) {
            return Collections.emptyMap();
        }
        return (Map<String, String>) store.get(ContextKey.PAYLOAD_VALUES);
    }
}
```

**Step 3: Verify compile**

Run: `cd befwlc-v2 && ../mvnw compile`

**Step 4: Commit**

```
feat: add ScenarioContext with typed enum keys
```

---

### Task 6: Matching Engine

**Files:**
- Create: `befwlc-v2/src/main/java/com/spring/befwlc/v2/matching/JsonPathResolver.java`
- Create: `befwlc-v2/src/main/java/com/spring/befwlc/v2/matching/WildcardMatcher.java`
- Create: `befwlc-v2/src/main/java/com/spring/befwlc/v2/matching/MessageFilter.java`
- Create: `befwlc-v2/src/main/java/com/spring/befwlc/v2/matching/MatchResult.java`
- Create: `befwlc-v2/src/main/java/com/spring/befwlc/v2/matching/MessageFilterSet.java`
- Create: `befwlc-v2/src/main/java/com/spring/befwlc/v2/matching/MessageMatcher.java`

**Step 1: Create JsonPathResolver**

Replaces `JsonNodeHelper`. Same dot-path traversal with `[n]` array indexing:

```java
package com.spring.befwlc.v2.matching;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.MissingNode;

import static com.spring.befwlc.v2.util.RegexUtils.JSON_ARRAY_INDEX;
import static com.spring.befwlc.v2.util.RegexUtils.JSON_IS_ARRAY_ACCESS;

public final class JsonPathResolver {

    private JsonPathResolver() {}

    public static JsonNode resolve(JsonNode root, String dotPath) {
        String[] segments = dotPath.split("\\.");
        JsonNode current = root;

        for (String segment : segments) {
            String trimmed = segment.trim();

            if (trimmed.matches(JSON_IS_ARRAY_ACCESS)) {
                String arrayKey = trimmed.replaceAll(JSON_ARRAY_INDEX, "");
                int index = Integer.parseInt(trimmed.replaceAll("\\D", ""));

                JsonNode arrayNode = current.has(arrayKey) ? current.get(arrayKey) : current;
                current = arrayNode.path(index);
            } else {
                if (!current.has(trimmed)) {
                    return MissingNode.getInstance();
                }
                current = current.get(trimmed);
            }

            if (current.isMissingNode()) {
                return current;
            }
        }
        return current;
    }
}
```

**Step 2: Create WildcardMatcher**

Replaces `JsonNodeHelper.valuesMatches()`. Same `%` wildcard semantics:

```java
package com.spring.befwlc.v2.matching;

public final class WildcardMatcher {

    private static final String WILDCARD = "%";

    private WildcardMatcher() {}

    public static boolean matches(String pattern, String value) {
        if (pattern == null || value == null) {
            return pattern == null && value == null;
        }

        boolean startsWithWildcard = pattern.startsWith(WILDCARD);
        boolean endsWithWildcard = pattern.endsWith(WILDCARD);

        if (startsWithWildcard && endsWithWildcard) {
            // %text% → contains
            String inner = pattern.substring(1, pattern.length() - 1);
            return value.contains(inner);
        } else if (endsWithWildcard) {
            // text% → startsWith
            String prefix = pattern.substring(0, pattern.length() - 1);
            return value.startsWith(prefix);
        } else if (startsWithWildcard) {
            // %text → endsWith
            String suffix = pattern.substring(1);
            return value.endsWith(suffix);
        } else {
            return pattern.equals(value);
        }
    }
}
```

**Step 3: Create MessageFilter**

Replaces `EntryFilter`:

```java
package com.spring.befwlc.v2.matching;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public class MessageFilter {

    private final String key;
    private final String expectedValue;
    private Map<String, String> mismatchDetails;

    public MessageFilter withMismatch(String expected, String actual) {
        MessageFilter copy = new MessageFilter(this.key, this.expectedValue);
        copy.mismatchDetails = new LinkedHashMap<>();
        copy.mismatchDetails.put("Expected", expected);
        copy.mismatchDetails.put("Actual", actual);
        return copy;
    }
}
```

**Step 4: Create MatchResult**

Replaces `PartiallyMatchedEntry`:

```java
package com.spring.befwlc.v2.matching;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public class MatchResult {

    private final List<MessageFilter> matched;
    private final List<MessageFilter> unmatched;
    private final ObjectNode message;

    public boolean isFullMatch() {
        return unmatched.isEmpty();
    }

    public int matchedCount() {
        return matched.size();
    }

    public int totalFilterCount() {
        return matched.size() + unmatched.size();
    }

    public Map<String, Object> toMap() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("MATCHED", matched.stream().map(f -> f.getKey() + "=" + f.getExpectedValue()).toList());
        result.put("UNMATCHED", unmatched.stream().map(f -> {
            Map<String, String> details = new LinkedHashMap<>();
            details.put("key", f.getKey());
            if (f.getMismatchDetails() != null) {
                details.putAll(f.getMismatchDetails());
            }
            return details;
        }).toList());
        return result;
    }
}
```

**Step 5: Create MessageFilterSet**

Replaces `EntryFilters`:

```java
package com.spring.befwlc.v2.matching;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.spring.befwlc.v2.context.ScenarioContext;
import com.spring.befwlc.v2.dsl.DslResolver;
import lombok.Getter;

import java.util.*;

@Getter
public class MessageFilterSet {

    private final List<MessageFilter> filters = new ArrayList<>();
    private final List<MatchResult> partialMatches = new ArrayList<>();

    public MessageFilterSet() {}

    public MessageFilterSet(Map<String, String> dataTable, DslResolver dslResolver, ScenarioContext context) {
        dataTable.forEach((key, value) -> {
            String resolved = dslResolver.resolve(value, context);
            filters.add(new MessageFilter(key, resolved));
        });
    }

    public MessageFilterSet addFilter(String key, String value) {
        filters.add(new MessageFilter(key, value));
        return this;
    }

    public void addPartialMatch(MatchResult result) {
        boolean alreadyTracked = partialMatches.stream()
                .anyMatch(existing -> existing.getMessage().equals(result.getMessage()));
        boolean isPartial = result.getUnmatched().size() != filters.size();

        if (isPartial && !alreadyTracked) {
            partialMatches.add(result);
        }
    }

    public List<MatchResult> getBestPartialMatches() {
        List<MatchResult> sorted = new ArrayList<>(partialMatches);
        sorted.sort(Comparator.comparingInt(r -> r.getUnmatched().size()));
        return sorted;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (MessageFilter f : filters) {
            sb.append("  ").append(f.getKey()).append(" : ").append(f.getExpectedValue()).append("\n");
        }
        return sb.toString();
    }
}
```

**Step 6: Create MessageMatcher**

Replaces `EntryFinder`:

```java
package com.spring.befwlc.v2.matching;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.spring.befwlc.v2.exception.TestExecutionException;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
public final class MessageMatcher {

    private MessageMatcher() {}

    public static Optional<ObjectNode> findMatch(List<ObjectNode> messages, MessageFilterSet filterSet) {
        List<ObjectNode> matches = new ArrayList<>();

        for (ObjectNode message : messages) {
            MatchResult result = evaluateFilters(message, filterSet);
            if (result.isFullMatch()) {
                matches.add(message);
            } else {
                filterSet.addPartialMatch(result);
            }
        }

        if (matches.isEmpty()) {
            return Optional.empty();
        }

        if (matches.size() > 1) {
            throw new TestExecutionException("Multiple messages matched the filters (%d found)", matches.size());
        }

        ObjectNode matched = matches.get(0);
        messages.remove(matched);
        return Optional.of(matched);
    }

    public static void assertNoMatch(List<ObjectNode> messages, MessageFilterSet filterSet) {
        for (ObjectNode message : messages) {
            MatchResult result = evaluateFilters(message, filterSet);
            if (result.isFullMatch()) {
                throw new TestExecutionException("Expected no match but found a matching message");
            }
        }
    }

    public static void logPartialMatches(List<MatchResult> partialMatches) {
        if (partialMatches.isEmpty()) {
            log.error("No messages matched any filters");
            return;
        }
        for (MatchResult result : partialMatches) {
            log.warn("Partial match ({}/{} filters): {}",
                    result.matchedCount(), result.totalFilterCount(), result.toMap());
        }
    }

    private static MatchResult evaluateFilters(ObjectNode message, MessageFilterSet filterSet) {
        List<MessageFilter> matched = new ArrayList<>();
        List<MessageFilter> unmatched = new ArrayList<>();

        for (MessageFilter filter : filterSet.getFilters()) {
            JsonNode node = JsonPathResolver.resolve(message, filter.getKey());
            boolean found = !node.isMissingNode();
            String nodeValue = found ? node.asText() : null;
            boolean valueMatched = found && WildcardMatcher.matches(filter.getExpectedValue(), nodeValue);

            if (valueMatched) {
                matched.add(filter);
            } else {
                String actual = found ? nodeValue : String.format("Node '%s' not found", filter.getKey());
                unmatched.add(filter.withMismatch(filter.getExpectedValue(), actual));
            }
        }

        return new MatchResult(matched, unmatched, message);
    }
}
```

**Step 7: Verify compile**

Run: `cd befwlc-v2 && ../mvnw compile`

**Step 8: Commit**

```
feat: add message matching engine with JsonPath resolver and wildcard support
```

---

### Task 7: DSL System

**Files:**
- Create: `befwlc-v2/src/main/java/com/spring/befwlc/v2/dsl/DslFunction.java`
- Create: `befwlc-v2/src/main/java/com/spring/befwlc/v2/dsl/DslRegistry.java`
- Create: `befwlc-v2/src/main/java/com/spring/befwlc/v2/dsl/DslResolver.java`
- Create: `befwlc-v2/src/main/java/com/spring/befwlc/v2/dsl/BuiltInFunctions.java`

**Step 1: Create DslFunction interface**

```java
package com.spring.befwlc.v2.dsl;

import com.spring.befwlc.v2.context.ScenarioContext;

import java.util.List;

@FunctionalInterface
public interface DslFunction {
    String apply(List<String> args, ScenarioContext context);
}
```

**Step 2: Create DslRegistry**

```java
package com.spring.befwlc.v2.dsl;

import com.spring.befwlc.v2.exception.TestExecutionException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class DslRegistry {

    private final Map<String, DslFunction> functions = new LinkedHashMap<>();
    private final BuiltInFunctions builtInFunctions;

    @PostConstruct
    void init() {
        builtInFunctions.registerAll(this);
        log.info("DSL Registry initialized with {} functions", functions.size());
    }

    public void register(String name, DslFunction function) {
        functions.put(name, function);
    }

    public DslFunction get(String name) {
        DslFunction fn = functions.get(name);
        if (fn == null) {
            String available = functions.keySet().stream()
                    .sorted()
                    .collect(Collectors.joining("\n  "));
            throw new TestExecutionException(
                    "Unknown DSL method: %s%nAvailable methods:%n  %s", name, available);
        }
        return fn;
    }

    public boolean contains(String name) {
        return functions.containsKey(name);
    }
}
```

**Step 3: Create DslResolver**

Replaces `PayloadHelper`. Parses `$METHOD(args)` and `$METHOD` syntax, resolves recursively:

```java
package com.spring.befwlc.v2.dsl;

import com.spring.befwlc.v2.context.ScenarioContext;
import com.spring.befwlc.v2.util.RegexUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.spring.befwlc.v2.util.RegexUtils.*;

@Component
@RequiredArgsConstructor
public class DslResolver {

    private final DslRegistry registry;

    public String resolve(String input, ScenarioContext context) {
        if (input == null) {
            return "null";
        }

        // Check for callable method: $METHOD_NAME(arg1, arg2)
        if (RegexUtils.matches(input, CALLABLE_METHOD_PATTERN)) {
            String methodName = RegexUtils.captureSingle(input, CALLABLE_METHOD_NAME);
            String argsStr = input.substring(methodName.length() + 1, input.length() - 1);
            List<String> rawArgs = RegexUtils.captureAll(argsStr, CALLABLE_METHOD_ARGS);

            List<String> resolvedArgs = new ArrayList<>();
            for (String arg : rawArgs) {
                resolvedArgs.add(resolve(arg.trim(), context));
            }

            // methodName includes trailing '(' — strip it and add '$' prefix normalization
            String cleanName = "$" + methodName.substring(1, methodName.length() - 1);
            DslFunction fn = registry.get(cleanName);
            return fn.apply(resolvedArgs, context);
        }

        // Check for static method: $METHOD_NAME
        if (RegexUtils.matches(input, STATIC_METHOD_PATTERN)) {
            DslFunction fn = registry.get(input);
            return fn.apply(List.of(), context);
        }

        return input;
    }
}
```

**Step 4: Create BuiltInFunctions**

Registers all 34 methods (18 callable + 16 static) from the current framework:

```java
package com.spring.befwlc.v2.dsl;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.spring.befwlc.v2.context.ContextKey;
import com.spring.befwlc.v2.context.ScenarioContext;
import com.spring.befwlc.v2.exception.TestExecutionException;
import com.spring.befwlc.v2.matching.JsonPathResolver;
import com.spring.befwlc.v2.util.DateUtils;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
public class BuiltInFunctions {

    public void registerAll(DslRegistry registry) {
        // === Static methods (no args) ===

        registry.register("$FULL_UUID", (args, ctx) ->
                UUID.randomUUID().toString());

        registry.register("$SHORT_UUID", (args, ctx) ->
                UUID.randomUUID().toString().replace("-", ""));

        registry.register("$5_RANDOM_CHARS", (args, ctx) ->
                UUID.randomUUID().toString().substring(0, 5));

        registry.register("$MS_UUID", (args, ctx) ->
                "MS" + UUID.randomUUID().toString().replace("-", ""));

        registry.register("$SYS_SHORT_DATE", (args, ctx) ->
                DateUtils.shortDate());

        registry.register("$SYS_FULL_OFFSET_DATE", (args, ctx) ->
                DateUtils.fullOffsetDate());

        registry.register("$SYS_FULL_DATE_PLUS_ONE_HOUR", (args, ctx) ->
                DateUtils.fullOffsetDatePlusOneHour());

        registry.register("$SYS_FULL_DATE", (args, ctx) ->
                DateUtils.fullDate());

        registry.register("$SYS_ISO_DATE", (args, ctx) ->
                DateUtils.isoDate());

        registry.register("$MISSING", (args, ctx) ->
                "$MISSING");

        registry.register("$EMPTY_STRING", (args, ctx) ->
                "");

        registry.register("$SPOT_SECRET", (args, ctx) ->
                "@bracadabra");

        registry.register("$COMMA", (args, ctx) ->
                ",");

        registry.register("$BLANK_SPACE", (args, ctx) ->
                " ");

        registry.register("$TASR_REF", (args, ctx) -> {
            synchronized (BuiltInFunctions.class) {
                return DateUtils.ofFormat("MMddHmmssSSSSS");
            }
        });

        registry.register("$LAST_PAYLOAD", (args, ctx) ->
                ctx.get(ContextKey.LAST_PAYLOAD, String.class));

        // === Callable methods (with args) ===

        registry.register("$PAYLOAD_VALUE", (args, ctx) -> {
            assertArgCount(args, 1, "$PAYLOAD_VALUE");
            Map<String, String> values = ctx.getPayloadValues();
            String key = args.get(0);
            if (!values.containsKey(key)) {
                throw new TestExecutionException("No payload value found for key: %s", key);
            }
            return values.get(key);
        });

        registry.register("$TO_UPPER_CASE", (args, ctx) -> {
            assertArgCount(args, 1, "$TO_UPPER_CASE");
            return args.get(0).toUpperCase();
        });

        registry.register("$REMOVE_ALL_CHARS", (args, ctx) -> {
            assertArgCount(args, 2, "$REMOVE_ALL_CHARS");
            return args.get(1).replaceAll(args.get(0), "");
        });

        registry.register("$REPLACE_ALL_CHARS", (args, ctx) -> {
            assertArgCount(args, 3, "$REPLACE_ALL_CHARS");
            String charToRemove = args.get(0);
            String replacement = args.get(1);
            String input = args.get(2);
            return input.replaceAll(charToRemove, replacement);
        });

        registry.register("$SYS_DATE_OF_FORMAT", (args, ctx) -> {
            assertArgCount(args, 1, "$SYS_DATE_OF_FORMAT");
            return DateUtils.ofFormat(args.get(0));
        });

        registry.register("$SYS_FULL_DATE_PLUS_MINUTES", (args, ctx) -> {
            assertArgCount(args, 1, "$SYS_FULL_DATE_PLUS_MINUTES");
            return DateUtils.fullDatePlusMinutes(Integer.parseInt(args.get(0)));
        });

        registry.register("$SYS_SHORT_DATE_PLUS_DAYS", (args, ctx) -> {
            assertArgCount(args, 1, "$SYS_SHORT_DATE_PLUS_DAYS");
            return DateUtils.shortDatePlusDays(Integer.parseInt(args.get(0)));
        });

        registry.register("$SYS_SHORT_DATE_MINUS_DAYS", (args, ctx) -> {
            assertArgCount(args, 1, "$SYS_SHORT_DATE_MINUS_DAYS");
            return DateUtils.shortDateMinusDays(Integer.parseInt(args.get(0)));
        });

        registry.register("$SYS_FULL_DATE_MINUS_MINUTES", (args, ctx) -> {
            assertArgCount(args, 1, "$SYS_FULL_DATE_MINUS_MINUTES");
            return DateUtils.fullDateMinusMinutes(Integer.parseInt(args.get(0)));
        });

        registry.register("$FORMAT_STRING_DATE", (args, ctx) -> {
            assertArgCount(args, 3, "$FORMAT_STRING_DATE");
            return DateUtils.reformatDate(args.get(0), args.get(1), args.get(2));
        });

        registry.register("$UUID_FROM_STRINGS", (args, ctx) -> {
            assertMinArgs(args, 1, "$UUID_FROM_STRINGS");
            String joined = String.join("", args);
            return UUID.nameUUIDFromBytes(joined.getBytes(StandardCharsets.UTF_8)).toString();
        });

        registry.register("$MERGE_VALUES", (args, ctx) -> {
            assertMinArgs(args, 1, "$MERGE_VALUES");
            return String.join("", args);
        });

        registry.register("$REMOVE_DASHES", (args, ctx) -> {
            assertMinArgs(args, 1, "$REMOVE_DASHES");
            return String.join("", args).replace("-", "");
        });

        registry.register("$LAST_MATCHED_RECORD", (args, ctx) -> {
            assertArgCount(args, 1, "$LAST_MATCHED_RECORD");
            ObjectNode record = ctx.get(ContextKey.LAST_MATCHED_RECORD, ObjectNode.class);
            return JsonPathResolver.resolve(record, args.get(0)).asText();
        });

        registry.register("$RANDOM_STRING_OF_LENGTH", (args, ctx) -> {
            assertArgCount(args, 1, "$RANDOM_STRING_OF_LENGTH");
            int length = Integer.parseInt(args.get(0));
            Random random = new Random();
            StringBuilder sb = new StringBuilder(length);
            for (int i = 0; i < length; i++) {
                sb.append((char) ('a' + random.nextInt(26)));
            }
            return sb.toString();
        });

        registry.register("$RANDOM_TEXT_OF_LENGTH", (args, ctx) -> {
            assertArgCount(args, 2, "$RANDOM_TEXT_OF_LENGTH");
            int totalLength = Integer.parseInt(args.get(0));
            int numGroups = Integer.parseInt(args.get(1));
            String raw = registry.get("$RANDOM_STRING_OF_LENGTH")
                    .apply(List.of(String.valueOf(totalLength)), ctx);

            int groupSize = totalLength / numGroups;
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < raw.length(); i += groupSize) {
                if (!result.isEmpty()) result.append(" ");
                result.append(raw, i, Math.min(i + groupSize, raw.length()));
            }
            return result.substring(0, Math.min(result.length(), totalLength));
        });

        registry.register("$STRING_SPLIT", (args, ctx) -> {
            assertArgCount(args, 3, "$STRING_SPLIT");
            int numGroups = Integer.parseInt(args.get(0));
            String separator = args.get(1);
            String input = args.get(2);
            int groupSize = input.length() / numGroups;

            StringBuilder result = new StringBuilder();
            for (int i = 0; i < input.length(); i += groupSize) {
                if (!result.isEmpty()) result.append(separator);
                result.append(input, i, Math.min(i + groupSize, input.length()));
            }
            return result.toString();
        });

        registry.register("$CAPITALIZE", (args, ctx) -> {
            assertArgCount(args, 1, "$CAPITALIZE");
            String[] words = args.get(0).split(" ");
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < words.length; i++) {
                if (!words[i].isEmpty()) {
                    result.append(Character.toUpperCase(words[i].charAt(0)))
                            .append(words[i].substring(1));
                    if (i < words.length - 1) result.append(" ");
                }
            }
            return result.toString();
        });
    }

    private void assertArgCount(List<String> args, int expected, String methodName) {
        if (args.size() != expected) {
            throw new TestExecutionException("%s expects %d argument(s), got %d", methodName, expected, args.size());
        }
    }

    private void assertMinArgs(List<String> args, int min, String methodName) {
        if (args.size() < min) {
            throw new TestExecutionException("%s expects at least %d argument(s), got %d", methodName, min, args.size());
        }
    }
}
```

**Step 5: Verify compile**

Run: `cd befwlc-v2 && ../mvnw compile`

**Step 6: Commit**

```
feat: add DSL registry with all 34 built-in functions
```

---

### Task 8: Kafka Infrastructure

**Files:**
- Create: `befwlc-v2/src/main/java/com/spring/befwlc/v2/kafka/KafkaConstants.java`
- Create: `befwlc-v2/src/main/java/com/spring/befwlc/v2/kafka/KafkaMessageStore.java`
- Create: `befwlc-v2/src/main/java/com/spring/befwlc/v2/kafka/KafkaMessageListener.java`
- Create: `befwlc-v2/src/main/java/com/spring/befwlc/v2/kafka/KafkaProducerService.java`
- Create: `befwlc-v2/src/main/java/com/spring/befwlc/v2/kafka/KafkaListenerReadyGuard.java`
- Create: `befwlc-v2/src/main/java/com/spring/befwlc/v2/config/KafkaConsumerConfig.java`
- Create: `befwlc-v2/src/main/java/com/spring/befwlc/v2/config/KafkaProducerConfig.java`

**Step 1: Create KafkaConstants**

```java
package com.spring.befwlc.v2.kafka;

public final class KafkaConstants {
    public static final String TOPIC = "topic";
    public static final String PARTITION = "Partition";
    public static final String OFFSET = "Offset";
    public static final String TIMESTAMP = "Timestamp";
    public static final String HEADERS = "Headers";
    public static final String MESSAGE = "Message";

    private KafkaConstants() {}
}
```

**Step 2: Create KafkaMessageStore**

```java
package com.spring.befwlc.v2.kafka;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class KafkaMessageStore {

    private final List<ObjectNode> records = new CopyOnWriteArrayList<>();

    public void add(ObjectNode record) {
        records.add(record);
    }

    public List<ObjectNode> getAll() {
        return records;
    }

    public boolean remove(ObjectNode record) {
        return records.remove(record);
    }

    public void clear() {
        records.clear();
    }

    public int size() {
        return records.size();
    }
}
```

**Step 3: Create KafkaMessageListener**

```java
package com.spring.befwlc.v2.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.spring.befwlc.v2.exception.TestExecutionException;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static com.spring.befwlc.v2.kafka.KafkaConstants.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaMessageListener {

    private final KafkaAvroDeserializer avroDeserializer;
    private final ObjectMapper objectMapper;
    private final KafkaMessageStore messageStore;

    @KafkaListener(
            topics = "${kafka.consume-topic}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onMessage(ConsumerRecord<byte[], byte[]> record) {
        try {
            ObjectNode envelope = objectMapper.createObjectNode();
            envelope.put(TOPIC, record.topic());
            envelope.put(PARTITION, record.partition());
            envelope.put(OFFSET, record.offset());
            envelope.put(TIMESTAMP, record.timestamp());

            Object deserialized = avroDeserializer.deserialize(record.topic(), record.value());
            envelope.set(MESSAGE, objectMapper.readTree(deserialized.toString()));

            ObjectNode headers = objectMapper.createObjectNode();
            for (Header header : record.headers()) {
                byte[] bytes = header.value();
                String value = Objects.nonNull(bytes) ? new String(bytes, StandardCharsets.UTF_8) : null;
                headers.put(header.key(), value);
            }
            envelope.set(HEADERS, headers);

            messageStore.add(envelope);
            log.debug("Stored message from topic={} offset={}", record.topic(), record.offset());
        } catch (Exception e) {
            log.error("Failed to deserialize Kafka record from topic '{}' at offset {}", record.topic(), record.offset(), e);
            throw new TestExecutionException("Failed to deserialize Kafka record: %s", e.getMessage());
        }
    }
}
```

**Step 4: Create KafkaProducerService**

```java
package com.spring.befwlc.v2.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import test.prof.events.TransactionCreated;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, TransactionCreated> kafkaTemplate;

    public void sendMessage(TransactionCreated message) {
        log.info("Publishing message to Kafka default topic");
        kafkaTemplate.sendDefault(message);
    }
}
```

**Step 5: Create KafkaListenerReadyGuard**

```java
package com.spring.befwlc.v2.kafka;

import com.spring.befwlc.v2.config.AwaitProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.Awaitility;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collection;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaListenerReadyGuard {

    private final KafkaListenerEndpointRegistry registry;
    private final AwaitProperties awaitProperties;

    public void waitForListeners() {
        log.info("Waiting for Kafka listener containers to be assigned partitions...");
        Awaitility.await()
                .atMost(Duration.ofSeconds(awaitProperties.getListenerTimeoutSeconds()))
                .pollInterval(Duration.ofSeconds(1))
                .until(() -> {
                    Collection<MessageListenerContainer> containers = registry.getListenerContainers();
                    return !containers.isEmpty() && containers.stream()
                            .allMatch(c -> !c.getAssignedPartitions().isEmpty());
                });
        log.info("All Kafka listener containers are ready");
    }
}
```

**Step 6: Create KafkaConsumerConfig**

```java
package com.spring.befwlc.v2.config;

import com.spring.befwlc.v2.kafka.KafkaConstants;
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    @Bean
    public KafkaAvroDeserializer kafkaAvroDeserializer(KafkaProperties kafkaProperties) {
        CachedSchemaRegistryClient schemaRegistryClient =
                new CachedSchemaRegistryClient(kafkaProperties.getSchemaRegistryUrl(), 1000);
        return new KafkaAvroDeserializer(schemaRegistryClient);
    }

    @Bean
    public ConsumerFactory<byte[], byte[]> consumerFactory(
            KafkaProperties kafkaProperties,
            ResourceLoader resourceLoader) {

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, kafkaProperties.getConsumer().getAutoOffsetReset());
        props.put(ConsumerConfig.GROUP_ID_CONFIG,
                "test-engine-automation-" + System.getProperty("user.name", "default"));

        KafkaProperties.Ssl ssl = kafkaProperties.getSsl();
        if (ssl.getKeyStoreLocation() != null) {
            props.put("security.protocol", kafkaProperties.getSecurityProtocol());
            props.put("ssl.keystore.location", resourceLoader.getResource("file:" + ssl.getKeyStoreLocation()).getDescription());
            props.put("ssl.keystore.password", ssl.getKeyStorePassword());
            props.put("ssl.truststore.location", resourceLoader.getResource("file:" + ssl.getTrustStoreLocation()).getDescription());
            props.put("ssl.truststore.password", ssl.getTrustStorePassword());
            props.put("ssl.key.password", ssl.getKeyPassword());
            props.put("ssl.keystore.type", ssl.getKeyStoreType());
        }

        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<byte[], byte[]> kafkaListenerContainerFactory(
            ConsumerFactory<byte[], byte[]> consumerFactory,
            KafkaProperties kafkaProperties) {

        ConcurrentKafkaListenerContainerFactory<byte[], byte[]> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.getContainerProperties().setPollTimeout(kafkaProperties.getConsumer().getPollTimeoutMs());
        return factory;
    }
}
```

**Step 7: Create KafkaProducerConfig**

```java
package com.spring.befwlc.v2.config;

import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import test.prof.events.TransactionCreated;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Bean
    public ProducerFactory<String, TransactionCreated> producerFactory(
            KafkaProperties kafkaProperties,
            ResourceLoader resourceLoader) {

        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        props.put(ProducerConfig.ACKS_CONFIG, kafkaProperties.getProducer().getAcks());
        props.put(ProducerConfig.RETRIES_CONFIG, kafkaProperties.getProducer().getRetries());
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, kafkaProperties.getProducer().isEnableIdempotence());
        props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION,
                kafkaProperties.getProducer().getMaxInFlightRequestsPerConnection());
        props.put("schema.registry.url", kafkaProperties.getSchemaRegistryUrl());

        KafkaProperties.Ssl ssl = kafkaProperties.getSsl();
        if (ssl.getKeyStoreLocation() != null) {
            props.put("security.protocol", kafkaProperties.getSecurityProtocol());
            props.put("ssl.keystore.location", resourceLoader.getResource("file:" + ssl.getKeyStoreLocation()).getDescription());
            props.put("ssl.keystore.password", ssl.getKeyStorePassword());
            props.put("ssl.truststore.location", resourceLoader.getResource("file:" + ssl.getTrustStoreLocation()).getDescription());
            props.put("ssl.truststore.password", ssl.getTrustStorePassword());
            props.put("ssl.key.password", ssl.getKeyPassword());
            props.put("ssl.keystore.type", ssl.getKeyStoreType());
        }

        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, TransactionCreated> kafkaTemplate(
            ProducerFactory<String, TransactionCreated> producerFactory,
            KafkaProperties kafkaProperties) {

        KafkaTemplate<String, TransactionCreated> template = new KafkaTemplate<>(producerFactory);
        template.setDefaultTopic(kafkaProperties.getPublishTopic());
        return template;
    }
}
```

**Step 8: Verify compile**

Run: `cd befwlc-v2 && ../mvnw compile`

**Step 9: Commit**

```
feat: add Kafka infrastructure — consumer, producer, message store, listener
```

---

### Task 9: Payload Handling

**Files:**
- Create: `befwlc-v2/src/main/java/com/spring/befwlc/v2/payload/PayloadLoader.java`
- Create: `befwlc-v2/src/main/java/com/spring/befwlc/v2/payload/PayloadMutator.java`

**Step 1: Create PayloadLoader**

```java
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
            objectMapper.readTree(content); // validate JSON
            return content;
        } catch (Exception e) {
            log.error("Error loading JSON file {}: {}", path, e.getMessage());
            throw new RuntimeException("Error loading JSON payload: " + path, e);
        }
    }
}
```

**Step 2: Create PayloadMutator**

```java
package com.spring.befwlc.v2.payload;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PayloadMutator {

    public String setValue(String json, String path, String value) {
        String normalizedPath = path.startsWith("$") ? path : "$." + path;

        try {
            Object typedValue = inferType(value);
            return JsonPath.parse(json).set(normalizedPath, typedValue).jsonString();
        } catch (PathNotFoundException e) {
            log.error("Path not found: {}", path);
            throw new RuntimeException("Invalid JSON path: " + path, e);
        }
    }

    private Object inferType(String value) {
        if (value == null || value.equalsIgnoreCase("null")
                || value.equalsIgnoreCase("<null>") || value.equalsIgnoreCase("${null}")) {
            return null;
        }
        if (value.isEmpty()) {
            return "";
        }
        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
            return Boolean.parseBoolean(value);
        }
        if (value.matches("-?\\d+\\.\\d+")) {
            return Double.parseDouble(value);
        }
        if (value.matches("-?\\d+")) {
            return Long.parseLong(value);
        }
        return value;
    }
}
```

**Step 3: Verify compile**

Run: `cd befwlc-v2 && ../mvnw compile`

**Step 4: Commit**

```
feat: add payload loading and JsonPath-based mutation
```

---

### Task 10: Await Mechanism

**Files:**
- Create: `befwlc-v2/src/main/java/com/spring/befwlc/v2/await/MessageAwaiter.java`

**Step 1: Create MessageAwaiter**

```java
package com.spring.befwlc.v2.await;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.spring.befwlc.v2.config.AwaitProperties;
import com.spring.befwlc.v2.context.ContextKey;
import com.spring.befwlc.v2.context.ScenarioContext;
import com.spring.befwlc.v2.exception.MessageNotFoundException;
import com.spring.befwlc.v2.kafka.KafkaMessageStore;
import com.spring.befwlc.v2.matching.MessageFilterSet;
import com.spring.befwlc.v2.matching.MessageMatcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.Awaitility;
import org.awaitility.core.ConditionTimeoutException;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageAwaiter {

    private final KafkaMessageStore messageStore;
    private final AwaitProperties awaitProperties;
    private final ScenarioContext scenarioContext;

    public ObjectNode awaitMatch(MessageFilterSet filterSet) {
        log.info("Waiting for Kafka message matching filters:\n{}", filterSet);

        AtomicReference<ObjectNode> found = new AtomicReference<>();
        try {
            Awaitility.await()
                    .atMost(Duration.ofSeconds(awaitProperties.getTimeoutSeconds()))
                    .pollInterval(Duration.ofSeconds(awaitProperties.getPollIntervalSeconds()))
                    .until(() -> {
                        Optional<ObjectNode> match = MessageMatcher.findMatch(messageStore.getAll(), filterSet);
                        match.ifPresent(found::set);
                        return match.isPresent();
                    });
        } catch (ConditionTimeoutException e) {
            MessageMatcher.logPartialMatches(filterSet.getBestPartialMatches());
            throw new MessageNotFoundException("No Kafka message found matching the given filters after %d seconds",
                    awaitProperties.getTimeoutSeconds());
        }

        ObjectNode matched = found.get();
        scenarioContext.put(ContextKey.LAST_MATCHED_RECORD, matched);
        log.info("Found matching message at offset {}", matched.get("Offset"));
        return matched;
    }

    public void awaitNoMatch(MessageFilterSet filterSet) {
        log.info("Verifying no Kafka message matches filters:\n{}", filterSet);

        try {
            Awaitility.await()
                    .atMost(Duration.ofSeconds(awaitProperties.getTimeoutSeconds()))
                    .pollInterval(Duration.ofSeconds(awaitProperties.getPollIntervalSeconds()))
                    .until(() -> {
                        MessageMatcher.assertNoMatch(messageStore.getAll(), filterSet);
                        return false;
                    });
        } catch (ConditionTimeoutException e) {
            log.info("Confirmed: no matching message found (as expected)");
        }
    }
}
```

**Step 2: Verify compile**

Run: `cd befwlc-v2 && ../mvnw compile`

**Step 3: Commit**

```
feat: add Awaitility-based message awaiter
```

---

### Task 11: Reporting

**Files:**
- Create: `befwlc-v2/src/main/java/com/spring/befwlc/v2/reporting/TestReporter.java`
- Create: `befwlc-v2/src/main/java/com/spring/befwlc/v2/reporting/ExtentTestReporter.java`
- Create: `befwlc-v2/src/main/java/com/spring/befwlc/v2/reporting/AzureDevOpsReporter.java`

**Step 1: Create TestReporter interface**

```java
package com.spring.befwlc.v2.reporting;

import java.util.Set;

public interface TestReporter {
    void startTest(String name, Set<String> tags);
    void logPass(String message);
    void logFail(String message);
    void logInfo(String message);
    void logException(Throwable throwable);
    void endTest();
}
```

**Step 2: Create ExtentTestReporter**

```java
package com.spring.befwlc.v2.reporting;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class ExtentTestReporter implements TestReporter {

    private final ExtentReports extentReports;
    private static final ThreadLocal<ExtentTest> currentTest = new ThreadLocal<>();

    @Override
    public void startTest(String name, Set<String> tags) {
        ExtentTest test = extentReports.createTest(name, String.join(", ", tags));
        tags.forEach(tag -> test.assignCategory(tag.replace("@", "")));
        currentTest.set(test);
    }

    @Override
    public void logPass(String message) {
        currentTest.get().pass(message);
    }

    @Override
    public void logFail(String message) {
        currentTest.get().fail(message);
    }

    @Override
    public void logInfo(String message) {
        currentTest.get().info(message);
    }

    @Override
    public void logException(Throwable throwable) {
        currentTest.get().fail(throwable);
    }

    @Override
    public void endTest() {
        extentReports.flush();
    }
}
```

**Step 3: Create AzureDevOpsReporter**

Fixed logic: `scenario.isFailed()` returns `true` for failures, so `allPassed` must check that **none** are failed:

```java
package com.spring.befwlc.v2.reporting;

import com.spring.befwlc.v2.config.AzureProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Component
@ConditionalOnProperty(name = "azure.update-results", havingValue = "true")
@RequiredArgsConstructor
public class AzureDevOpsReporter implements TestReporter {

    private final AzureProperties azureProperties;
    private final RestTemplate restTemplate = new RestTemplate();

    private String currentTestCaseId;
    private final Map<String, List<Boolean>> testResults = new HashMap<>();
    private boolean currentScenarioFailed = false;

    @Override
    public void startTest(String name, Set<String> tags) {
        currentScenarioFailed = false;
    }

    @Override
    public void logPass(String message) {
        currentScenarioFailed = false;
    }

    @Override
    public void logFail(String message) {
        currentScenarioFailed = true;
    }

    @Override
    public void logInfo(String message) {}

    @Override
    public void logException(Throwable throwable) {
        currentScenarioFailed = true;
    }

    @Override
    public void endTest() {
        if (currentTestCaseId == null) return;

        testResults.putIfAbsent(currentTestCaseId, new ArrayList<>());
        testResults.get(currentTestCaseId).add(currentScenarioFailed);
    }

    public void setCurrentTestCaseId(String testCaseId) {
        this.currentTestCaseId = testCaseId;
    }

    public void flushResults(String testCaseId) {
        List<Boolean> results = testResults.remove(testCaseId);
        if (results == null || results.isEmpty()) return;

        // Fixed: allPassed = none of the scenarios failed
        boolean allPassed = results.stream().noneMatch(failed -> failed);
        String outcome = allPassed ? "Passed" : "Failed";

        try {
            updateTestResult(testCaseId, outcome, "Automated test: " + outcome);
        } catch (Exception e) {
            log.error("Failed to update Azure DevOps test result for testCaseId={}: {}", testCaseId, e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void updateTestResult(String testCaseId, String outcome, String comment) {
        String baseUrl = String.format("https://dev.azure.com/%s/%s/_apis",
                azureProperties.getOrganization(), azureProperties.getProject());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Basic " +
                Base64.getEncoder().encodeToString((":" + azureProperties.getPat()).getBytes()));

        // Get test points
        String pointsUrl = String.format("%s/test/Plans/%s/Suites/%s/points?api-version=7.1",
                baseUrl, azureProperties.getTestPlanId(), azureProperties.getSuiteId());

        ResponseEntity<Map> pointsResponse = restTemplate.exchange(
                pointsUrl, HttpMethod.GET, new HttpEntity<>(headers), Map.class);

        List<Map<String, Object>> points = (List<Map<String, Object>>) pointsResponse.getBody().get("value");
        Integer pointId = points.stream()
                .filter(p -> testCaseId.equals(String.valueOf(((Map<?, ?>) p.get("testCase")).get("id"))))
                .findFirst()
                .map(p -> (Integer) p.get("id"))
                .orElseThrow(() -> new RuntimeException("Test point not found for testCaseId: " + testCaseId));

        // Create run
        String runUrl = baseUrl + "/test/runs?api-version=7.1";
        Map<String, Object> runData = Map.of(
                "name", comment,
                "plan", Map.of("id", azureProperties.getTestPlanId()),
                "state", "InProgress");

        ResponseEntity<Map> runResponse = restTemplate.exchange(
                runUrl, HttpMethod.POST, new HttpEntity<>(runData, headers), Map.class);
        Integer runId = (Integer) runResponse.getBody().get("id");

        // Add result
        String resultUrl = String.format("%s/test/runs/%d/results?api-version=7.1", baseUrl, runId);
        Map<String, Object> result = Map.of(
                "testPoint", Map.of("id", pointId),
                "testCase", Map.of("id", testCaseId),
                "testCaseRevision", 1,
                "testCaseTitle", comment,
                "outcome", outcome,
                "state", "Completed");

        restTemplate.exchange(resultUrl, HttpMethod.POST,
                new HttpEntity<>(Collections.singletonList(result), headers), Map.class);

        // Complete run
        String completeUrl = String.format("%s/test/runs/%d?api-version=7.1", baseUrl, runId);
        HttpHeaders patchHeaders = new HttpHeaders();
        patchHeaders.putAll(headers);
        patchHeaders.set("X-HTTP-Method-Override", "PATCH");

        restTemplate.exchange(completeUrl, HttpMethod.POST,
                new HttpEntity<>(Map.of("state", "Completed"), patchHeaders), Map.class);

        log.info("Azure DevOps: Updated testCaseId={} with outcome={}", testCaseId, outcome);
    }
}
```

**Step 4: Verify compile**

Run: `cd befwlc-v2 && ../mvnw compile`

**Step 5: Commit**

```
feat: add reporting — ExtentReports and Azure DevOps with fixed pass/fail logic
```

---

### Task 12: Encryption

**Files:**
- Create: `befwlc-v2/src/main/java/com/spring/befwlc/v2/config/EncryptionConfig.java`
- Create: `befwlc-v2/src/main/java/com/spring/befwlc/v2/util/EncryptionHelper.java`

**Step 1: Create EncryptionConfig**

```java
package com.spring.befwlc.v2.config;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;

import java.security.Security;

@Configuration
public class EncryptionConfig {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Bean
    public TextEncryptor textEncryptor() {
        String key = System.getenv().getOrDefault("ENCRYPTION_KEY", "defaultDevKey2024");
        String salt = "deadbeef";
        return Encryptors.text(key, salt);
    }
}
```

**Step 2: Create EncryptionHelper**

CLI tool for developers to encrypt values:

```java
package com.spring.befwlc.v2.util;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;

import java.security.Security;

public class EncryptionHelper {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: EncryptionHelper <encrypt|decrypt> <value> [key]");
            System.out.println("  If key is not provided, uses ENCRYPTION_KEY env var or default dev key");
            return;
        }

        String action = args[0];
        String value = args[1];
        String key = args.length > 2 ? args[2]
                : System.getenv().getOrDefault("ENCRYPTION_KEY", "defaultDevKey2024");

        TextEncryptor encryptor = Encryptors.text(key, "deadbeef");

        if ("encrypt".equalsIgnoreCase(action)) {
            String encrypted = encryptor.encrypt(value);
            System.out.println("Encrypted: {cipher}" + encrypted);
            System.out.println("\nPaste this into your application.yml:");
            System.out.println("  property-name: '{cipher}" + encrypted + "'");
        } else if ("decrypt".equalsIgnoreCase(action)) {
            String cleaned = value.replace("{cipher}", "");
            String decrypted = encryptor.decrypt(cleaned);
            System.out.println("Decrypted: " + decrypted);
        } else {
            System.out.println("Unknown action: " + action + ". Use 'encrypt' or 'decrypt'.");
        }
    }
}
```

**Step 3: Verify compile**

Run: `cd befwlc-v2 && ../mvnw compile`

**Step 4: Commit**

```
feat: add encryption config and CLI helper for encrypting YAML values
```

---

### Task 13: Test Configuration and Application YAML

**Files:**
- Create: `befwlc-v2/src/test/java/com/spring/befwlc/v2/TestConfiguration.java`
- Create: `befwlc-v2/src/test/resources/application.yml`

**Step 1: Create TestConfiguration**

```java
package com.spring.befwlc.v2;

import com.spring.befwlc.v2.config.AwaitProperties;
import com.spring.befwlc.v2.config.AzureProperties;
import com.spring.befwlc.v2.config.KafkaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;

@TestConfiguration
@ComponentScan(basePackages = "com.spring.befwlc.v2")
@EnableConfigurationProperties({
        KafkaProperties.class,
        AwaitProperties.class,
        AzureProperties.class
})
public class TestConfiguration {
}
```

Note: Class name conflicts with the annotation. Rename to `BeFwLcTestConfig`:

```java
package com.spring.befwlc.v2;

import com.spring.befwlc.v2.config.AwaitProperties;
import com.spring.befwlc.v2.config.AzureProperties;
import com.spring.befwlc.v2.config.KafkaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;

@TestConfiguration
@ComponentScan(basePackages = "com.spring.befwlc.v2")
@EnableConfigurationProperties({
        KafkaProperties.class,
        AwaitProperties.class,
        AzureProperties.class
})
public class BeFwLcTestConfig {
}
```

**Step 2: Create application.yml**

Maps to the new `@ConfigurationProperties` structure:

```yaml
spring:
  main:
    web-application-type: none

kafka:
  bootstrap-servers: br201-odin-tst.io.ing.net:9093,br401-odin-tst.io.ing.net:9093
  schema-registry-url: https://sri-global-tst.ing.net:8443
  security-protocol: SSL
  publish-topic: PS2071.transaction_profile-ro-billingengine-test.C5ORQ
  consume-topic: test_consume_kafka_topic
  ssl:
    key-store-location: /cert/vespar_cert.pfx
    key-store-password: changeme
    key-store-type: pkcs12
    trust-store-location: /cert/vesparapp_ipc_st_truststore.jks
    trust-store-password: changeme
    key-password: changeme
  consumer:
    auto-offset-reset: latest
    poll-timeout-ms: 1000
  producer:
    acks: all
    retries: 10
    enable-idempotence: false
    max-in-flight-requests-per-connection: 1

await:
  timeout-seconds: 90
  poll-interval-seconds: 1
  listener-timeout-seconds: 60

azure:
  organization: MYAPP
  project: MYAPPPROJ
  pat: apat
  test-plan-id: "4065552"
  suite-id: "7781217"
  update-results: false

extent:
  reporter:
    spark:
      enable:
        logs: true
        device: true
      start: true
      out: test-output/SparkReport/Spark.html
      theme: dark
      document-title: Automation Test Report
      report-name: Test Execution Report
      timestamp-format: MMM dd, yyyy HH:mm:ss
      enable-timeline: true
      charts: true
      test-view-charts: true
```

**Step 3: Verify compile**

Run: `cd befwlc-v2 && ../mvnw compile`

**Step 4: Commit**

```
feat: add test configuration with @EnableConfigurationProperties and application.yml
```

---

### Task 14: Cucumber Steps and Hooks

**Files:**
- Create: `befwlc-v2/src/test/java/com/spring/befwlc/v2/steps/ScenarioHooks.java`
- Create: `befwlc-v2/src/test/java/com/spring/befwlc/v2/steps/PayloadSteps.java`
- Create: `befwlc-v2/src/test/java/com/spring/befwlc/v2/steps/KafkaPublishSteps.java`
- Create: `befwlc-v2/src/test/java/com/spring/befwlc/v2/steps/KafkaAssertSteps.java`

**Step 1: Create ScenarioHooks**

```java
package com.spring.befwlc.v2.steps;

import com.spring.befwlc.v2.kafka.KafkaListenerReadyGuard;
import com.spring.befwlc.v2.reporting.TestReporter;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
public class ScenarioHooks {

    private final List<TestReporter> reporters;
    private final KafkaListenerReadyGuard listenerReadyGuard;

    @Before(order = 0)
    public void waitForKafka() {
        listenerReadyGuard.waitForListeners();
    }

    @Before(order = 1)
    public void startReporting(Scenario scenario) {
        Set<String> tags = scenario.getSourceTagNames();
        reporters.forEach(r -> r.startTest(scenario.getName(), tags));
    }

    @After
    public void endReporting(Scenario scenario) {
        if (scenario.isFailed()) {
            reporters.forEach(r -> r.logFail("Scenario failed: " + scenario.getName()));
        } else {
            reporters.forEach(r -> r.logPass("Scenario passed: " + scenario.getName()));
        }
        reporters.forEach(TestReporter::endTest);
    }
}
```

**Step 2: Create PayloadSteps**

```java
package com.spring.befwlc.v2.steps;

import com.spring.befwlc.v2.payload.PayloadLoader;
import com.spring.befwlc.v2.payload.PayloadMutator;
import com.spring.befwlc.v2.reporting.AzureDevOpsReporter;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class PayloadSteps {

    private final PayloadLoader payloadLoader;
    private final PayloadMutator payloadMutator;

    @Autowired(required = false)
    private AzureDevOpsReporter azureReporter;

    @Given("I set testId for my current testCase")
    public void setTestId(DataTable dataTable) {
        Map<String, String> data = dataTable.asMap(String.class, String.class);
        String id = data.get("testCaseId");
        if (id == null) {
            id = data.get("testcaseId"); // handle both casings
        }
        if (azureReporter != null && id != null) {
            azureReporter.setCurrentTestCaseId(id);
        }
    }

    public String loadJson(String jsonFile) {
        return payloadLoader.loadJson(jsonFile);
    }

    public String updateField(String json, String path, String value) {
        return payloadMutator.setValue(json, path, value);
    }
}
```

**Step 3: Create KafkaPublishSteps**

```java
package com.spring.befwlc.v2.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.befwlc.v2.context.ContextKey;
import com.spring.befwlc.v2.context.ScenarioContext;
import com.spring.befwlc.v2.kafka.KafkaProducerService;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.SpringBootTest;
import test.prof.events.TransactionCreated;

import java.util.*;

@Slf4j
@CucumberContextConfiguration
@RequiredArgsConstructor
@SpringBootTest(classes = com.spring.befwlc.v2.BeFwLcTestConfig.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class KafkaPublishSteps {

    private final KafkaProducerService producerService;
    private final ObjectMapper objectMapper;
    private final ScenarioContext scenarioContext;
    private final PayloadSteps payloadSteps;

    @When("{string} payload with the following details is posted on {string} endpoint")
    public void publishPayload(String jsonFile, String topic, DataTable dataTable) throws Exception {
        String benefId = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        scenarioContext.put(ContextKey.BENEFICIARY_ID, benefId);

        String json = payloadSteps.loadJson(jsonFile);

        String txSeq = String.valueOf(new Random().nextInt(999999999 - 1000 + 1) + 1000);
        scenarioContext.put(ContextKey.TX_SEQ, txSeq);

        Map<String, String> modifications = new HashMap<>(dataTable.asMap(String.class, String.class));
        modifications.put("body.receivingPartyName", benefId);
        modifications.put("body.transactionIdentifierSequence", txSeq);

        for (Map.Entry<String, String> entry : modifications.entrySet()) {
            json = payloadSteps.updateField(json, entry.getKey(), entry.getValue());
        }

        scenarioContext.put(ContextKey.LAST_PAYLOAD, json);

        TransactionCreated transaction = objectMapper.readValue(json, TransactionCreated.class);
        producerService.sendMessage(transaction);
        log.info("Published message to topic '{}' with benefId={} txSeq={}", topic, benefId, txSeq);
    }
}
```

**Step 4: Create KafkaAssertSteps**

```java
package com.spring.befwlc.v2.steps;

import com.spring.befwlc.v2.await.MessageAwaiter;
import com.spring.befwlc.v2.context.ScenarioContext;
import com.spring.befwlc.v2.dsl.DslResolver;
import com.spring.befwlc.v2.kafka.KafkaConstants;
import com.spring.befwlc.v2.matching.MessageFilterSet;
import io.cucumber.java.en.Then;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class KafkaAssertSteps {

    private final MessageAwaiter messageAwaiter;
    private final DslResolver dslResolver;
    private final ScenarioContext scenarioContext;

    @Then("an event with the following fields is posted on {string} topic")
    public void assertMessagePosted(String topic, Map<String, String> filters) {
        MessageFilterSet filterSet = new MessageFilterSet(filters, dslResolver, scenarioContext);
        filterSet.addFilter(KafkaConstants.TOPIC, topic);
        messageAwaiter.awaitMatch(filterSet);
    }

    @Then("no event with the following fields is posted on {string} topic")
    public void assertMessageNotPosted(String topic, Map<String, String> filters) {
        MessageFilterSet filterSet = new MessageFilterSet(filters, dslResolver, scenarioContext);
        filterSet.addFilter(KafkaConstants.TOPIC, topic);
        messageAwaiter.awaitNoMatch(filterSet);
    }
}
```

**Step 5: Verify compile**

Run: `cd befwlc-v2 && ../mvnw compile`

**Step 6: Commit**

```
feat: add Cucumber step definitions — publish, assert, payload, hooks
```

---

### Task 15: Cucumber Runner and Feature File

**Files:**
- Create: `befwlc-v2/src/test/java/com/spring/befwlc/v2/runner/CucumberRunner.java`
- Create: `befwlc-v2/src/test/resources/features/test.feature`
- Copy: `befwlc-v2/src/test/resources/payload/template.json` (from existing)

**Step 1: Create CucumberRunner**

```java
package com.spring.befwlc.v2.runner;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

@CucumberOptions(
        features = "classpath:features",
        glue = {"com.spring.befwlc.v2.steps"},
        plugin = {
                "pretty",
                "html:target/cucumber-reports.html",
                "json:target/cucumber.json"
        }
)
public class CucumberRunner extends AbstractTestNGCucumberTests {

    static {
        System.setProperty("cucumber.publish.quiet", "true");
    }

    @Override
    @DataProvider(parallel = true)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
```

**Step 2: Copy the feature file**

Copy `src/test/resources/features/test.feature` to `befwlc-v2/src/test/resources/features/test.feature` — same content.

**Step 3: Copy the payload template**

Copy `src/test/resources/payload/template.json` to `befwlc-v2/src/test/resources/payload/template.json` — same content.

**Step 4: Verify compile**

Run: `cd befwlc-v2 && ../mvnw compile`

**Step 5: Commit**

```
feat: add Cucumber runner, feature file, and payload template
```

---

### Task 16: Final Verification and Cleanup

**Step 1: Full compile and verify**

Run: `cd befwlc-v2 && ../mvnw clean compile`
Expected: BUILD SUCCESS

**Step 2: Review all imports and fix any compilation errors**

Check for any missing imports or circular dependencies.

**Step 3: Verify the project structure matches the design**

Run: `find befwlc-v2/src -name "*.java" | sort` and compare against the design document.

**Step 4: Commit any fixes**

```
chore: fix compilation issues and finalize befwlc-v2 module
```

---

## Summary of All Files

| Package | Files | Purpose |
|---|---|---|
| `config` | KafkaProperties, AwaitProperties, AzureProperties, JacksonConfig, KafkaConsumerConfig, KafkaProducerConfig, EncryptionConfig | All configuration |
| `kafka` | KafkaConstants, KafkaMessageStore, KafkaMessageListener, KafkaProducerService, KafkaListenerReadyGuard | Kafka infrastructure |
| `matching` | JsonPathResolver, WildcardMatcher, MessageFilter, MatchResult, MessageFilterSet, MessageMatcher | Message matching engine |
| `dsl` | DslFunction, DslRegistry, DslResolver, BuiltInFunctions | $METHOD() DSL system |
| `context` | ScenarioContext, ContextKey | Per-scenario state |
| `payload` | PayloadLoader, PayloadMutator | JSON template handling |
| `await` | MessageAwaiter | Awaitility-based polling |
| `reporting` | TestReporter, ExtentTestReporter, AzureDevOpsReporter | Test result reporting |
| `util` | DateUtils, RegexUtils, EncryptionHelper | Utilities |
| `exception` | TestExecutionException, MessageNotFoundException | Exceptions |
| `steps` (test) | ScenarioHooks, PayloadSteps, KafkaPublishSteps, KafkaAssertSteps | Cucumber steps |
| `runner` (test) | CucumberRunner | TestNG runner |
