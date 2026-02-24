# Billing Processor Design

## Overview

Mock billing engine processor with local Kafka infrastructure. Reads TransactionCreated events from inbound topic, calculates fee based on amount, writes BillingResponse to outbound topic.

## Decisions

- Java 17 / Spring Boot 3.5.6 with @KafkaListener
- New Maven module: `billing-processor/`
- Runs inside Docker alongside Kafka infrastructure
- Init container creates topics + registers schemas automatically
- Single `docker-compose up` starts everything

## Architecture

```
billing-engine-inbound → BillingProcessor → billing-engine-outbound
(TransactionCreated)      fee calculation     (BillingResponse)
```

## Docker Services

| Service | Image | Port |
|---|---|---|
| zookeeper | confluentinc/cp-zookeeper:7.9.2 | 2181 |
| kafka | confluentinc/cp-kafka:7.9.2 | 9092 |
| schema-registry | confluentinc/cp-schema-registry:7.9.2 | 8081 |
| init-kafka | confluentinc/cp-kafka:7.9.2 | - |
| billing-processor | custom (Temurin 17) | 8080 |

## Fee Logic

- amount < 10000 → "1.01"
- amount >= 10000 → "2.01"

## Output Format

```json
{
  "GeneralInformation": [
    {"value": "31"},
    {"value": "1"},
    {"value": "<fee>"}
  ]
}
```
