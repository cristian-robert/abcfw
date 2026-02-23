# befwlc-v2: Clean Billing Engine Test Framework

## Context

Rewrite of the beFwLc BDD test automation framework as a new Maven module. The current framework has accumulated architectural debt: Service Locator anti-patterns, scattered configuration, hardcoded encryption, duplicated ObjectMapper instances, and a monolithic step definition class. This design preserves all functionality while leveraging modern Spring Boot 3.x patterns.

## Decisions

| Decision | Choice | Rationale |
|---|---|---|
| Schema scope | Specific to TransactionCreated | No need for generic Avro support |
| Config binding | `@ConfigurationProperties` | Type-safe, validated, IDE-friendly |
| Encryption | Spring Boot native `{cipher}` + utility class | Modern, no Jasypt dependency |
| DSL syntax | Same `$METHOD(args)` syntax | Feature file backward compatibility |
| Module structure | Separate Maven module (`befwlc-v2/`) | Clean separation, independent build |
| Polling | Awaitility library | Battle-tested, better diagnostics |
| DI pattern | Constructor injection everywhere | No Service Locator, no field injection |
| Reporting | Interface + dual impl (Extent + Azure) | Both included, Azure bugs fixed |

## Project Structure

```
befwlc-v2/
├── pom.xml
├── src/main/java/com/spring/befwlc/v2/
│   ├── BeFwLcV2Application.java
│   ├── config/
│   │   ├── KafkaProperties.java           # @ConfigurationProperties("kafka")
│   │   ├── KafkaConsumerConfig.java        # ConsumerFactory + ListenerContainer beans
│   │   ├── KafkaProducerConfig.java        # ProducerFactory + KafkaTemplate beans
│   │   ├── JacksonConfig.java              # Single ObjectMapper bean
│   │   ├── AwaitProperties.java            # @ConfigurationProperties("await")
│   │   ├── AzureProperties.java            # @ConfigurationProperties("azure")
│   │   └── EncryptionConfig.java           # TextEncryptor bean for {cipher} support
│   ├── kafka/
│   │   ├── KafkaMessageStore.java          # @ScenarioScope CopyOnWriteArrayList<ObjectNode>
│   │   ├── KafkaMessageListener.java       # @KafkaListener, Avro → ObjectNode
│   │   ├── KafkaProducerService.java       # send(TransactionCreated)
│   │   └── KafkaListenerReadyGuard.java    # Waits for partition assignment
│   ├── matching/
│   │   ├── MessageFilter.java              # key + expectedValue pair
│   │   ├── MessageFilterSet.java           # Filter collection + partial match tracking
│   │   ├── MessageMatcher.java             # Matches ObjectNode against filters
│   │   ├── JsonPathResolver.java           # Dot-path traversal with array[n]
│   │   └── WildcardMatcher.java            # % wildcard matching
│   ├── dsl/
│   │   ├── DslFunction.java                # @FunctionalInterface
│   │   ├── DslRegistry.java                # String → DslFunction map
│   │   ├── DslResolver.java                # Parses $METHOD(args), resolves recursively
│   │   └── BuiltInFunctions.java           # All 34 methods registered at startup
│   ├── context/
│   │   ├── ScenarioContext.java            # @ScenarioScope enum-keyed store
│   │   └── ContextKey.java                 # All context keys
│   ├── payload/
│   │   ├── PayloadLoader.java              # Loads JSON from classpath
│   │   └── PayloadMutator.java             # JsonPath updates with type inference
│   ├── reporting/
│   │   ├── TestReporter.java               # Interface
│   │   ├── ExtentTestReporter.java         # ExtentReports impl
│   │   └── AzureDevOpsReporter.java        # Azure DevOps Test Plans REST API
│   ├── await/
│   │   └── MessageAwaiter.java             # Awaitility-based polling
│   ├── util/
│   │   ├── DateUtils.java                  # Pure java.time
│   │   └── EncryptionHelper.java           # CLI tool for encrypting values
│   └── exception/
│       ├── TestExecutionException.java
│       └── MessageNotFoundException.java
├── src/main/avro/
│   └── transaction_created_schema.avsc
├── src/test/java/com/spring/befwlc/v2/
│   ├── TestConfiguration.java              # @EnableConfigurationProperties
│   ├── runner/CucumberRunner.java          # Correct glue, TestNG
│   ├── steps/
│   │   ├── ScenarioHooks.java              # @Before/@After
│   │   ├── PayloadSteps.java               # @Given payload setup
│   │   ├── KafkaPublishSteps.java          # @When message publishing
│   │   └── KafkaAssertSteps.java           # @Then message assertions
│   └── config/
│       └── TestKafkaConsumer.java           # Concrete listener for test topic
├── src/test/resources/
│   ├── features/test.feature
│   ├── payload/
│   └── application.yml
```

## Component Designs

### Configuration

All config grouped by prefix using Java records with validation:

- `KafkaProperties("kafka")`: bootstrap-servers, schema-registry-url, security-protocol, publish-topic, consume-topic, nested ssl/consumer/producer records
- `AwaitProperties("await")`: timeout-seconds, poll-interval-seconds, listener-timeout-seconds
- `AzureProperties("azure")`: organization, project, pat, test-plan-id, suite-id, update-results

`@EnableConfigurationProperties` declared in `TestConfiguration` to ensure binding is active.

### Kafka Infrastructure

- `KafkaMessageStore` (`@ScenarioScope`): Thread-safe message buffer with add/remove/getAll. Automatically cleared per scenario.
- `KafkaMessageListener`: Single `@KafkaListener` on `${kafka.consume-topic}`. Deserializes Avro bytes via `KafkaAvroDeserializer`, converts to `ObjectNode`, adds metadata (topic, partition, offset, timestamp, headers).
- `KafkaProducerService`: Injects `KafkaTemplate<String, TransactionCreated>`, fires `sendDefault()`.
- `KafkaListenerReadyGuard`: Polls `KafkaListenerEndpointRegistry` until all containers have assigned partitions. Called from `@Before` hook.

### Matching Engine

- `JsonPathResolver.resolve(ObjectNode root, String dotPath) → JsonNode`: Splits on dots, handles `name[index]` for arrays. Returns `MissingNode` on not-found.
- `WildcardMatcher.matches(String pattern, String value) → boolean`: `%` prefix/suffix/both semantics, exact match fallback.
- `MessageMatcher.findMatch(List<ObjectNode>, MessageFilterSet) → Optional<ObjectNode>`: Iterates messages, applies all filters, tracks partial matches for diagnostics. Returns first full match.
- `MessageFilterSet`: Constructed from `Map<String, String>` (Cucumber DataTable), resolves `$METHOD` values during construction via `DslResolver`.

### DSL System

- `DslFunction`: `String apply(List<String> args, ScenarioContext ctx)`
- `DslRegistry`: `@Component`, registers all 34 built-in functions in `@PostConstruct`. Extensible via `register(name, fn)`.
- `DslResolver`: Parses `$NAME(arg1, arg2)` and `$NAME` syntax using regex. Recursively resolves nested `$METHOD` calls in arguments. Delegates to `DslRegistry`.
- `BuiltInFunctions`: Static methods for all 34 functions (18 callable + 16 static from current framework). Registered in `DslRegistry.registerBuiltIns()`.

### Reporting

- `TestReporter` interface: `startTest`, `logPass`, `logFail`, `logInfo`, `logException`, `endTest`
- `ExtentTestReporter`: ThreadLocal-based, same as current but cleaner
- `AzureDevOpsReporter`: Fixed pass/fail logic (current has inverted boolean). Only active when `azure.update-results=true`. Uses `RestTemplate` for Azure DevOps REST API.
- `ScenarioHooks` iterates `List<TestReporter>` — all reporters called automatically.

### Encryption

- `EncryptionConfig`: Declares a `TextEncryptor` bean (AES-256-GCM) with key from `ENCRYPTION_KEY` env variable
- Spring Boot auto-decrypts `{cipher}...` values in YAML when `TextEncryptor` is available
- `EncryptionHelper`: CLI utility with `main()` method: `java EncryptionHelper encrypt "mySecret"` → outputs `{cipher}...` for pasting into YAML

### Context

- `ScenarioContext` (`@ScenarioScope`): `HashMap<ContextKey, Object>` with typed `get(key, class)` and `put(key, value)`. Fixed `%s` format string.
- `ContextKey` enum: PAYLOAD_VALUES, LAST_PAYLOAD, LAST_MATCHED_RECORD, BENEFICIARY_ID, TX_SEQ

### Utilities

- `DateUtils`: Pure `java.time` — no `Calendar`. Methods: `shortDate()`, `fullDate()`, `fullOffsetDate()`, `isoDate()`, `plusDays(n)`, `minusDays(n)`, `plusMinutes(n)`, `minusMinutes(n)`, `ofFormat(pattern)`, `reformatDate(input, fromPattern, toPattern)`.

## Bug Fixes from Current Framework

All known bugs from the current codebase are addressed by design:

1. No `encrypt/decrypt` confusion — no custom Jasypt wrapper needed
2. `bootstrapServers` properly typed as `String` in `KafkaProperties` record
3. Correct package declaration (`com.spring.befwlc.v2`)
4. Headers properly added to ObjectNode in `KafkaMessageListener`
5. DSL method arguments properly indexed
6. No stray `}` in config — `@ConfigurationProperties` handles binding
7. `ScenarioContext.get()` uses `%s` format string
8. Cucumber runner has correct `glue` path (`com.spring.befwlc.v2.steps`)
9. No space in `classpath:` prefix for resource loading
10. Property names match between YAML and Java (`azure.update-results`, not `amsAssert.check`)
