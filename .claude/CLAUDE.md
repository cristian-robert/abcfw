# beFwLc - Billing Engine Framework (Low Code)

## Tech Stack

- **Language:** Java 17
- **Framework:** Spring Boot 3.5.6
- **Build Tool:** Maven (with Maven Wrapper)
- **Messaging:** Apache Kafka with Avro serialization (Confluent Schema Registry)
- **Testing:** Cucumber 7.15.0 + TestNG 7.8.0 (BDD)
- **Reporting:** ExtentReports 5.1.1 + Azure DevOps Test Plans integration
- **Encryption:** Jasypt 3.0.5 + BouncyCastle 1.76
- **JSON:** Jackson 2.19.2 + JsonPath 2.9.0

## Project Purpose

BDD test automation framework for a banking billing engine. It publishes Avro-serialized transaction events to Kafka, consumes billing engine responses, and asserts expected fee calculations. Primarily tests SEPA Realtime Credit Transfer fee amounts.

## Package Structure

```
com.spring.befwlc/
  configuration/    # Spring @Configuration: Kafka, Azure, JSON, Await
  context/          # Scenario context management (ScenarioContext, keys enum)
  entry_filter/     # Kafka message filtering & matching engine
    json/           # JsonNode traversal helpers
  exceptions/       # Custom exceptions (TestExecution, MaxIteration, UnmatchedFilters)
  feature_methods/  # Dynamic value generation DSL ($METHOD(args) syntax)
  handlers/         # Polling/await logic with lock-based mechanism
  payload/          # Payload value resolution from templates
  service/          # Kafka consumer/producer infrastructure
  utils/            # Date, encryption, regex, transformation utilities
test.prof.events/   # Avro-generated event models (DO NOT EDIT - generated from .avsc)
```

## Key Patterns

- **Template Method:** `KafkaConsumer` (abstract) -> `BeKafkaConsumer` (concrete)
- **Strategy via Enums:** `CallableFeatureMethod` / `StaticFeatureMethod` with execute() methods
- **Service Locator:** `ApplicationContextProvider.getBean()` for enum access to Spring beans
- **Fluent API:** `EntryFilters.addFilter()` chaining
- **Polling:** `AwaitHandler.awaitTrue()` with configurable interval/max iterations

## Important Files

- `src/main/avro/transaction_created_schema.avsc` - Avro schema (generates event classes)
- `src/test/resources/features/test.feature` - Cucumber feature file
- `src/test/resources/payload/template.json` - Transaction payload template
- `src/test/resources/application-st-local-ipc.yml` - Test environment config (contains encrypted secrets)
- `src/test/java/.../steps/KafkaSteps.java` - Main Cucumber step definitions

## Build & Run

```bash
./mvnw clean install           # Build
./mvnw test                    # Run tests
./mvnw avro:schema             # Regenerate Avro classes
```

## Known Issues (Critical)

1. `EncryptionUtils.decrypt()` calls `.encrypt()` instead of `.decrypt()`
2. `KafkaProducerConfig.bootstrapServers` typed as `long` instead of `String`
3. `KafkaConsumerConfig` has wrong package declaration (`com.ing.billing_engine.configuration`)
4. `KafkaConsumer.addHeadersToRecord()` computes header values but never adds them to ObjectNode
5. `$REPLACE_ALL_CHARS` uses `args.get(0)` for both input and charToRemove (should use args.get(2))
6. Extra `}` in `@Value` annotations for SSL key-store-type in both Kafka configs
7. `ScenarioContext.get()` has `'$s'` instead of `'%s'` in error message format string

## Conventions

- Use Lombok (`@Getter`, `@Setter`, `@RequiredArgsConstructor`, `@Slf4j`) for boilerplate
- Cucumber step definitions in `src/test/java/.../steps/`
- Configuration classes annotated with `@Configuration` or `@ConfigurationProperties`
- Feature methods use `$METHOD_NAME(args)` syntax in Cucumber data tables
