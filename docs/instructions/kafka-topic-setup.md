# Prompt: Build a Kafka Billing Processor

Copy everything below this line and paste it as a prompt to an AI agent.

---

## Task

Build a mock billing engine processor with local Kafka infrastructure. The processor sits between two Kafka topics: it reads transaction events from an inbound topic, calculates a fee based on the transaction amount, and writes the result to an outbound topic.

No SSL. No authentication. Everything runs locally via Docker.

## Architecture

```
  Topic 1                    Processor               Topic 2
  billing-engine-inbound --> (you build) ----------> billing-engine-outbound
  (Avro message)             reads amount,           (Avro message)
                             calculates fee,
                             writes result
```

## Step 1: Local Kafka + Schema Registry via Docker Compose

Create a `docker-compose.yml` with:
- Zookeeper
- Kafka broker on `localhost:9092` (PLAINTEXT, no SSL)
- Confluent Schema Registry on `localhost:8081`

Use Confluent images version `7.9.2`.

After starting, create two topics:
- `billing-engine-inbound` (1 partition, replication 1)
- `billing-engine-outbound` (1 partition, replication 1)

## Step 2: Register the Inbound Avro Schema

Register this Avro schema in Schema Registry under subject `billing-engine-inbound-value`:

```json
{
  "type": "record",
  "name": "TransactionCreated",
  "namespace": "test.prof.events",
  "fields": [
    {
      "name": "header",
      "type": {
        "type": "record",
        "name": "ProfileEventHeader",
        "fields": [
          {"name": "headerVersion", "type": "string"},
          {"name": "messageId", "type": "string"},
          {"name": "aggregateId", "type": "string"},
          {"name": "occurrenceDate", "type": "string"},
          {"name": "actor", "type": "string"},
          {"name": "source", "type": "string"},
          {"name": "eventType", "type": "string"},
          {"name": "eventTypeVersion", "type": "string"}
        ]
      }
    },
    {
      "name": "body",
      "type": {
        "type": "record",
        "name": "TransactionCreatedBody",
        "fields": [
          {"name": "transactionIdentifierSequence", "type": ["null", "string"], "default": null},
          {"name": "accountNumber", "type": ["null", "string"], "default": null},
          {"name": "systemDate", "type": ["null", "string"], "default": null},
          {"name": "branch", "type": ["null", "string"], "default": null},
          {"name": "externalTransactionCode", "type": ["null", "string"], "default": null},
          {"name": "debitCreditFlag", "type": ["null", "string"], "default": null},
          {"name": "skipStatementFlag", "type": ["null", "string"], "default": null},
          {"name": "errorCorrectionFlag", "type": ["null", "string"], "default": null},
          {"name": "reversalFlag", "type": ["null", "string"], "default": null},
          {"name": "amount", "type": ["null", "string"], "default": null},
          {"name": "interestAmount1", "type": ["null", "string"], "default": null},
          {"name": "penaltiesAmount", "type": ["null", "string"], "default": null},
          {"name": "taxOnInterestAmount", "type": ["null", "string"], "default": null},
          {"name": "otherChargesAmount", "type": ["null", "string"], "default": null},
          {"name": "endingBalanceAmount", "type": ["null", "string"], "default": null},
          {"name": "userId", "type": ["null", "string"], "default": null},
          {"name": "transactionCurrency", "type": ["null", "string"], "default": null},
          {"name": "Rate", "type": ["null", "string"], "default": null},
          {"name": "transactionEffectiveDate", "type": ["null", "string"], "default": null},
          {"name": "transactionLineOfPosting", "type": ["null", "string"], "default": null},
          {"name": "calendarDate", "type": ["null", "string"], "default": null},
          {"name": "transactionComment2", "type": ["null", "string"], "default": null},
          {"name": "transactionDetails1", "type": ["null", "string"], "default": null},
          {"name": "transactionDetails2", "type": ["null", "string"], "default": null},
          {"name": "transactionDetails3", "type": ["null", "string"], "default": null},
          {"name": "transactionDetails4", "type": ["null", "string"], "default": null},
          {"name": "cardNumber", "type": ["null", "string"], "default": null},
          {"name": "authorizationCode", "type": ["null", "string"], "default": null},
          {"name": "cardType", "type": ["null", "string"], "default": null},
          {"name": "incomingFileNameOrCaptureUser", "type": ["null", "string"], "default": null},
          {"name": "contextLogId", "type": ["null", "string"], "default": null},
          {"name": "sequenceOfFile", "type": ["null", "string"], "default": null},
          {"name": "dateOfCapture", "type": ["null", "string"], "default": null},
          {"name": "fromAccountNumber", "type": ["null", "string"], "default": null},
          {"name": "fromFinancialInstitution", "type": ["null", "string"], "default": null},
          {"name": "orderingPartyName", "type": ["null", "string"], "default": null},
          {"name": "interestAmount2", "type": ["null", "string"], "default": null},
          {"name": "merchantCityName", "type": ["null", "string"], "default": null},
          {"name": "merchantCountry", "type": ["null", "string"], "default": null},
          {"name": "transactionComment", "type": ["null", "string"], "default": null},
          {"name": "details1", "type": ["null", "string"], "default": null},
          {"name": "details2", "type": ["null", "string"], "default": null},
          {"name": "merchantTerminal", "type": ["null", "string"], "default": null},
          {"name": "toAccountNumber", "type": ["null", "string"], "default": null},
          {"name": "referenceNumber", "type": ["null", "string"], "default": null},
          {"name": "sequenceReversalTransaction", "type": ["null", "string"], "default": null},
          {"name": "toFinancialInstitution", "type": ["null", "string"], "default": null},
          {"name": "receivingPartyName", "type": ["null", "string"], "default": null},
          {"name": "transactionSource", "type": ["null", "string"], "default": null},
          {"name": "paymentSequenceNumber", "type": ["null", "string"], "default": null},
          {"name": "paymentReferenceNumber", "type": ["null", "string"], "default": null},
          {"name": "toAccountCurrency", "type": ["null", "string"], "default": null},
          {"name": "toAccountCurrencyAmount", "type": ["null", "string"], "default": null},
          {"name": "transactionFrom", "type": ["null", "string"], "default": null},
          {"name": "transactionAmount", "type": ["null", "string"], "default": null},
          {"name": "transactionAmountExponent", "type": ["null", "string"], "default": null},
          {"name": "transactionSlip", "type": ["null", "string"], "default": null},
          {"name": "transactionTo", "type": ["null", "string"], "default": null},
          {"name": "originalOrderedAccount", "type": ["null", "string"], "default": null},
          {"name": "correspondentServiceFee1", "type": ["null", "string"], "default": null},
          {"name": "correspondentServiceFee2", "type": ["null", "string"], "default": null},
          {"name": "correspondentServiceFee3", "type": ["null", "string"], "default": null},
          {"name": "cardNumber2", "type": ["null", "string"], "default": null},
          {"name": "serviceFee", "type": ["null", "string"], "default": null},
          {"name": "accountActivationBonus", "type": ["null", "string"], "default": null},
          {"name": "ecommerceAdministrationFee", "type": ["null", "string"], "default": null},
          {"name": "accountAdministrationFee1", "type": ["null", "string"], "default": null},
          {"name": "slipNumber", "type": ["null", "string"], "default": null},
          {"name": "beneficiary", "type": ["null", "string"], "default": null},
          {"name": "beneficiaryInstitution", "type": ["null", "string"], "default": null},
          {"name": "swiftCode", "type": ["null", "string"], "default": null},
          {"name": "beneficiaryInstitution1", "type": ["null", "string"], "default": null},
          {"name": "beneficiaryInstitution2", "type": ["null", "string"], "default": null},
          {"name": "beneficiaryInstitution3", "type": ["null", "string"], "default": null},
          {"name": "beneficiaryInstitution4", "type": ["null", "string"], "default": null},
          {"name": "campaign", "type": ["null", "string"], "default": null},
          {"name": "accountAdministrationFee2", "type": ["null", "string"], "default": null},
          {"name": "country", "type": ["null", "string"], "default": null},
          {"name": "cardMembershipFee1", "type": ["null", "string"], "default": null},
          {"name": "cardMembershipFee2", "type": ["null", "string"], "default": null},
          {"name": "initialTransactionDate", "type": ["null", "string"], "default": null},
          {"name": "incomeTaxForUtilitiesPaymentBonus", "type": ["null", "string"], "default": null},
          {"name": "posMaintenanceFee", "type": ["null", "string"], "default": null},
          {"name": "serviceFee1", "type": ["null", "string"], "default": null},
          {"name": "serviceFee2", "type": ["null", "string"], "default": null},
          {"name": "lowValueGarnishmentPaymentFee", "type": ["null", "string"], "default": null},
          {"name": "highValueGarnishmentPaymentFee", "type": ["null", "string"], "default": null},
          {"name": "loanLinkedCurrentAccount", "type": ["null", "string"], "default": null},
          {"name": "chargeDetail1", "type": ["null", "string"], "default": null},
          {"name": "ingFixedOfferOrFixedFee", "type": ["null", "string"], "default": null},
          {"name": "ingFixedCompleteOfferFee", "type": ["null", "string"], "default": null},
          {"name": "ingFixedOfferFee", "type": ["null", "string"], "default": null},
          {"name": "interestPaymentOption", "type": ["null", "string"], "default": null},
          {"name": "interestAmount3", "type": ["null", "string"], "default": null},
          {"name": "merchantCategoryCode", "type": ["null", "string"], "default": null},
          {"name": "maturityDate", "type": ["null", "string"], "default": null},
          {"name": "merchantName", "type": ["null", "string"], "default": null},
          {"name": "merchantName2", "type": ["null", "string"], "default": null},
          {"name": "effectiveAmount", "type": ["null", "string"], "default": null},
          {"name": "loanClosing", "type": ["null", "string"], "default": null},
          {"name": "accountOpeningFee", "type": ["null", "string"], "default": null},
          {"name": "ecommerceInstallationFee", "type": ["null", "string"], "default": null},
          {"name": "senderName", "type": ["null", "string"], "default": null},
          {"name": "ownerOfFee", "type": ["null", "string"], "default": null},
          {"name": "cardMemberPan", "type": ["null", "string"], "default": null},
          {"name": "topPhoneNumber", "type": ["null", "string"], "default": null},
          {"name": "processOwnForUtilitiesPayment", "type": ["null", "string"], "default": null},
          {"name": "nonbankTransactionBonus", "type": ["null", "string"], "default": null},
          {"name": "advocateCompingBonus", "type": ["null", "string"], "default": null},
          {"name": "allCardsCompingBonus", "type": ["null", "string"], "default": null},
          {"name": "multipleCompingBonus", "type": ["null", "string"], "default": null},
          {"name": "purposeCode", "type": ["null", "string"], "default": null},
          {"name": "supplementaryVerificationFee", "type": ["null", "string"], "default": null},
          {"name": "reportingLine1", "type": ["null", "string"], "default": null},
          {"name": "reportingLine2", "type": ["null", "string"], "default": null},
          {"name": "rolloverAmount", "type": ["null", "string"], "default": null},
          {"name": "settlementAmount", "type": ["null", "string"], "default": null},
          {"name": "masServiceFee", "type": ["null", "string"], "default": null},
          {"name": "alertServiceMonthlyFee", "type": ["null", "string"], "default": null},
          {"name": "smsServiceFee", "type": ["null", "string"], "default": null},
          {"name": "valueDate", "type": ["null", "string"], "default": null},
          {"name": "ingRate", "type": ["null", "string"], "default": null},
          {"name": "dueFrom", "type": ["null", "string"], "default": null},
          {"name": "transferFee", "type": ["null", "string"], "default": null},
          {"name": "statementFee", "type": ["null", "string"], "default": null},
          {"name": "rate2", "type": ["null", "string"], "default": null},
          {"name": "abcGoldOptionOpeningFee", "type": ["null", "string"], "default": null},
          {"name": "abcGoldOptionAdministrationFee", "type": ["null", "string"], "default": null},
          {"name": "comment1", "type": ["null", "string"], "default": null},
          {"name": "principal", "type": ["null", "string"], "default": null},
          {"name": "transactionDate", "type": ["null", "string"], "default": null},
          {"name": "refundFreeTransfersFee", "type": ["null", "string"], "default": null},
          {"name": "travelOptionAdministrationFee", "type": ["null", "string"], "default": null},
          {"name": "empowered", "type": ["null", "string"], "default": null},
          {"name": "homebankingMonthlyServiceFee", "type": ["null", "string"], "default": null},
          {"name": "taxOnInterest", "type": ["null", "string"], "default": null},
          {"name": "accountAdministrationFee3", "type": ["null", "string"], "default": null},
          {"name": "accountAdministrationFee4", "type": ["null", "string"], "default": null},
          {"name": "accountOpeningFee1", "type": ["null", "string"], "default": null},
          {"name": "accountOpeningFee2", "type": ["null", "string"], "default": null},
          {"name": "accountOpeningFee3", "type": ["null", "string"], "default": null},
          {"name": "accountOpeningFee4", "type": ["null", "string"], "default": null},
          {"name": "cardIssuanceFee", "type": ["null", "string"], "default": null},
          {"name": "cardMembershipFeesNet", "type": ["null", "string"], "default": null},
          {"name": "extractRollFileAnalysisFee", "type": ["null", "string"], "default": null},
          {"name": "accountStatementMailingFee", "type": ["null", "string"], "default": null},
          {"name": "webLogId", "type": ["null", "string"], "default": null},
          {"name": "thirdPartyProviderName", "type": ["null", "string"], "default": null},
          {"name": "availableBalance", "type": ["null", "string"], "default": null},
          {"name": "marketRate", "type": ["null", "string"], "default": null},
          {"name": "negotiatedRateIndicator", "type": ["null", "string"], "default": null},
          {"name": "fiscalRegistrationNumber", "type": ["null", "string"], "default": null},
          {"name": "sepaCustomerReference", "type": ["null", "string"], "default": null},
          {"name": "isSepaFlag", "type": ["null", "string"], "default": null},
          {"name": "comment2", "type": ["null", "string"], "default": null},
          {"name": "comment3", "type": ["null", "string"], "default": null},
          {"name": "comment4", "type": ["null", "string"], "default": null},
          {"name": "details3", "type": ["null", "string"], "default": null},
          {"name": "details4", "type": ["null", "string"], "default": null},
          {"name": "feeForUrgentPayment", "type": ["null", "string"], "default": null},
          {"name": "depositor", "type": ["null", "string"], "default": null},
          {"name": "cashLimitUpdateFee", "type": ["null", "string"], "default": null},
          {"name": "cardIssuanceFee2", "type": ["null", "string"], "default": null},
          {"name": "vodafoneRechargeCommission", "type": ["null", "string"], "default": null},
          {"name": "vodafoneRechargeCommission2", "type": ["null", "string"], "default": null},
          {"name": "orangeRechargeCommission", "type": ["null", "string"], "default": null},
          {"name": "telecouRechargeCommission", "type": ["null", "string"], "default": null},
          {"name": "instantCreditSellOfFee", "type": ["null", "string"], "default": null},
          {"name": "accountType", "type": ["null", "string"], "default": null},
          {"name": "currency", "type": ["null", "string"], "default": null},
          {"name": "initChannel", "type": ["null", "string"], "default": null},
          {"name": "urgencyType", "type": ["null", "string"], "default": null},
          {"name": "btcCode", "type": ["null", "string"], "default": null},
          {"name": "paymentType", "type": ["null", "string"], "default": null},
          {"name": "endToEndId", "type": ["null", "string"], "default": null}
        ]
      }
    }
  ]
}
```

## Step 3: Register the Outbound Avro Schema

Register this Avro schema in Schema Registry under subject `billing-engine-outbound-value`:

```json
{
  "type": "record",
  "name": "BillingResponse",
  "namespace": "test.billing.events",
  "fields": [
    {
      "name": "GeneralInformation",
      "type": {
        "type": "array",
        "items": {
          "type": "record",
          "name": "InfoEntry",
          "fields": [
            {"name": "value", "type": ["null", "string"], "default": null}
          ]
        }
      }
    }
  ]
}
```

## Step 4: Build the Processor

Build a service (Python, Node.js, Go — your choice) that:

1. Connects to Kafka at `localhost:9092` and Schema Registry at `http://localhost:8081`
2. Consumes Avro messages from `billing-engine-inbound` using the `TransactionCreated` schema
3. For each message, reads `body.amount` (it's a string, parse it to a number)
4. Calculates the fee:
   - If amount < 10000 → fee is `"1.01"`
   - If amount >= 10000 → fee is `"2.01"`
5. Produces an Avro message to `billing-engine-outbound` using the `BillingResponse` schema with this structure:

```json
{
  "GeneralInformation": [
    {"value": "31"},           // index 0 — ProductId (always "31")
    {"value": "1"},            // index 1 — FeePlanId (always "1")
    {"value": "<calculated>"}  // index 2 — FeeAmount ("1.01" or "2.01")
  ]
}
```

The processor must:
- Use a consumer group (e.g. `billing-processor`)
- Start from `earliest` offset (so it doesn't miss messages)
- Process messages as they arrive (low latency)
- Log each processed message (input amount → output fee)

## What "Done" Looks Like

A Java test framework will:
1. Produce an Avro `TransactionCreated` with `body.amount = "50"` to `billing-engine-inbound`
2. Your processor reads it, calculates fee `"1.01"`, writes to `billing-engine-outbound`
3. The framework consumes from `billing-engine-outbound` and asserts:
   - `GeneralInformation[0].value == "31"`
   - `GeneralInformation[1].value == "1"`
   - `GeneralInformation[2].value == "1.01"`
4. Test passes

Same for `body.amount = "50000"` → fee `"2.01"`.

There are 12 test scenarios total. The amount is the only thing that changes the fee. All other fields (`transactionSource`, `initChannel`, `urgencyType`) do NOT affect the fee in these tests.

## Deliverables

1. `docker-compose.yml` — Kafka + Zookeeper + Schema Registry
2. A script to create topics and register both Avro schemas
3. The processor service with a `README.md` explaining how to run it
4. Everything should start with a single `docker-compose up` (or at most two commands)
