     @CT_SEPA_REALTIME
Feature: Testing fee amount for Credit Transfers SEPA Realtime

  @Standard
  Scenario Outline: Testing standard fee amount for Credit Transfers SEPA Realtime
    Given I set testId for my current testcase
      | testcaseId | <testId> |
    Given 'billing_engine/ct_standard_outgoing_sepa_realtime.json' payload with the following details is posted on 'BILLING_ENGINE' endpoint
      | body.externalTransactionCode | <TxType>            |
      | body.amount                  | <Amount>            |
      | body.urgencyType            | <UrgencyType>       |
      | body.effectiveAmount        | <EffectiveAmount>   |
      | body.initChannel            | <InitChannel>       |
      | body.transactionSource      | <TransactionSource> |
    Then an event with the following fields is posted on 'BF' topic
      | Message.GeneralInformation[3].value | <ProductId> |
      | Message.GeneralInformation[5].value | <FeePlanId> |
      | Message.GeneralInformation[3].value | <FeeAmount> |

    Examples:
      | TxType | Amount | ProductId | FeePlanId | FeeAmount | TransactionSource | UrgencyType | EffectiveAmount | InitChannel | description         | testId  |
      | DM     | 50     | 31        | 1         | 1.01      | 2                | RT6S        | EUR50           |             | low value IB        | 9499735 |
      | DM     | 50     | 31        | 1         | 1.01      | 39               | RT6S        | EUR50           |             | low value IC        | 9499736 |
      | DM     | 50     | 31        | 1         | 1.01      | 40               | RT6S        | EUR50           | IB          | low value IBP       | 9499737 |
      | DM     | 50     | 31        | 1         | 1.01      | 40               | RT6S        | EUR50           | IC          | low value IRC       | 9499738 |
      | DM     | 50     | 31        | 1         | 1.01      | 40               | RT6S        | EUR50           | FA          | low value SWIFTNEIFA| 9499739 |
      | DM     | 50     | 31        | 1         | 1.01      | 40               | RT6S        | EUR50           | TP          | low value PS02      | 9499740 |
      | DM     | 50000  | 31        | 1         | 2.01      | 39               | RT6S        | EUR50000        |             | high value IB       | 9499741 |
      | DM     | 50000  | 31        | 1         | 2.01      | 40               | RT6S        | EUR50000        |             | high value IC       | 9499742 |
      | DM     | 50000  | 31        | 1         | 2.01      | 40               | RT6S        | EUR50000        | IB          | high value IBP      | 9499743 |
      | DM     | 50000  | 31        | 1         | 2.01      | 40               | RT6S        | EUR50000        | IC          | high value IRC      | 9499744 |
      | DM     | 50000  | 31        | 1         | 2.01      | 40               | RT6S        | EUR50000        | FA          | high value SWIFTNEIFA| 9499745 |
      | DM     | 50000  | 31        | 1         | 2.01      | 40               | RT6S        | EUR50000        | TP          | high value PS02     | 9499746 |
