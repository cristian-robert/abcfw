package com.spring.befwlc.feature_methods;


import com.fasterxml.jackson.databind.node.ObjectNode;
import com.spring.befwlc.context.ScenarioContext;
import com.spring.befwlc.context.ScenarioContextKeys;
import com.spring.befwlc.entry_filter.json.JsonNodeHelper;
import com.spring.befwlc.exceptions.TestExecutionException;
import com.spring.befwlc.payload.PayloadValue;
import com.spring.befwlc.utils.DateUtils;


import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static com.spring.befwlc.context.ApplicationContextProvider.getBean;
import static com.spring.befwlc.feature_methods.StaticFeatureMethod.*;


public enum CallableFeatureMethod implements CallableMethod {

    $PAYLOAD_VALUE("Returns payload value by given key.", "$PAYLOAD_VALUE(Company_CSR)") {
        public String execute(final List<String> args) {
            assertArgsCountEquals(args, 1);
            final String payloadKey = args.get(0);
            final Map<String, PayloadValue> payloadValues = getBean(ScenarioContext.class).getPayloadValues();
            final PayloadValue payloadValue = payloadValues.get(payloadKey);
            if (payloadValue == null) {
                throw new TestExecutionException("No such payload value found: %s", payloadKey);
            }
            return payloadValue.getValue();
        }
    },

    $TO_UPPER_CASE("Returns input to upper case", "$TO_UPPER_CASE(test)") {
        public String execute(final List<String> args) {
            assertArgsCountEquals(args, 1);
            final String input = args.get(0);
            return input.toUpperCase();
        }
    },

    $REMOVE_ALL_CHARS("Removes all chars","$REMOVE_ALL_CHARS(-,test-test)") {
        public String execute(final List<String> args) {
            assertArgsCountEquals(args, 2);
            final String input = args.get(1);
            return input.replaceAll(args.get(0), "");
        }
    },

    $REPLACE_ALL_CHARS("Replace all chars", "$REPLACE_ALL_CHARS(-, '%', test-test)") {
        public String execute(final List<String> args) {
            assertArgsCountEquals(args, 3);
            final String input = args.get(0);
            final String charToRemove = args.get(0);
            final String replacement = args.get(1);
            return input.replaceAll(charToRemove, replacement);
        }
    },

    $SYS_DATE_OF_FORMAT("Generates sys date of given format") {
        public String execute(List<String> args) {
            assertArgsCountEquals(args, 1);
            return DateUtils.sysDateOfFormat(args.get(0));
        }
    },

    $SYS_FULL_DATE_PLUS_MINUTES("Generates current timestamp plus minutes (first arg) hour of format 'Xs'", DateUtils.FULL_DATE_FORMAT) {
        public String execute(List<String> args) {
            assertArgsCountEquals(args, 1);
            return DateUtils.fullSysDatePlusMinutes(Integer.parseInt(args.get(0)));
        }
    },

    $SYS_SHORT_DATE_PLUS_DAYS("Generates current timestamp plus minutes (first arg) hour of format '%s'", DateUtils.SHORT_DATE_FORMAT) {
        public String execute(List<String> args) {
            assertArgsCountEquals(args, 1);
            return DateUtils.shortSysDatePlusDays(Integer.parseInt(args.get(0)));
        }
    },

    $SYS_SHORT_DATE_MINUS_DAYS("Generates current timestamp plus minutes (first arg) hour of format 'Xs'*, DateUtils.SHORT_DATE_FORMAT") {
        public String execute(List<String> args) {
            assertArgsCountEquals(args, 1);
            return DateUtils.shortSysDateMinusDays(Integer.parseInt(args.get(0)));
        }
    },

    $SYS_FULL_DATE_MINUS_MINUTES("Generates current timestamp plus minutes (first arg) hour of format 'Xs'", DateUtils.FULL_DATE_FORMAT) {
        public String execute(List<String> args) {
            assertArgsCountEquals(args, 1);
            return DateUtils.fullSysDateMinusMinutes(Integer.parseInt(args.get(0)));
        }
    },

    $FORMAT_STRING_DATE("Formats the input date by given date format.") {
        public String execute(List<String> args) {
            assertArgsCountEquals(args, 3);
            final String inputDateString = args.get(0);
            final String inputDateStringFormat = args.get(1);
            final String outputDateStringFormat = args.get(2);
            return DateUtils.stringDateToFormattedDate(inputDateString, inputDateStringFormat, outputDateStringFormat);
        }
    },

    //    $SHA_256("Generates SHA 256 hash using the input arguments separated by ':'") {
    //        public String execute(List<String> args) {
    //            assertArgCountGreaterThan(args, 0);
    //            return TransactionDataUtils.computeHash(args, "SHA-256");
    //        }
    //    },

    $UUID_FROM_STRINGS("Merge arguments and generates a UUID string") {
        public String execute(List<String> args) {
            assertArgsCountGtThan(args, 0);
            final String argsString = String.join("", args);
            return UUID.nameUUIDFromBytes(argsString.getBytes(StandardCharsets.UTF_8)).toString();
        }
    },

    $MERGE_VALUES("Merge arguments in one string value") {
        public String execute(List<String> args) {
            assertArgsCountGtThan(args, 0);
            return String.join("", args);
        }
    },

    $REMOVE_DASHES("Removes dashes from the concatenated strings passed as arguments") {
        public String execute(List<String> args) {
            assertArgsCountGtThan(args, 0);
            return String.join("", args).replaceAll("-", "");
        }
    },

    $LAST_MATCHED_RECORD("Returns the value of the last matched record node key passed as argument") {
        public String execute(List<String> args) {
            assertArgsCountEquals(args, 1);
            ObjectNode lastMatchedRecord = getBean(ScenarioContext.class).get(ScenarioContextKeys.LAST_MATCHED_RECORD, ObjectNode.class);
            return JsonNodeHelper.extractNodeInstanceByKey(lastMatchedRecord, args.get(0)).getNodeValue().asText();
        }
    },

    $RANDOM_STRING_OF_LENGTH("Generates a random string of given <length>", "$RANDOM_STRING_OF_LENGTH(20)") {
        public String execute(List<String> args) {
            assertArgsCountEquals(args, 1);
            int leftLimit = 97; // letter 'a'
            int rightLimit = 122; // letter 'z'
            StringBuilder stringBuilder = new StringBuilder();
            Iterator<String> iterator = args.listIterator();

            while (iterator.hasNext()) {
                int targetStringLength = Integer.parseInt(iterator.next());
                Random random = new Random();
                StringBuilder buffer = new StringBuilder(targetStringLength);

                for (int i = 0; i < targetStringLength; i++) {
                    int randomLimitedInt = leftLimit + (int)
                            (random.nextFloat() * (rightLimit - leftLimit + 1));
                    buffer.append((char) randomLimitedInt);
                }

                stringBuilder.append(buffer);
                if (iterator.hasNext()) {
                    stringBuilder.append(" ");
                }
            }

            return stringBuilder.toString();
        }
    },

    $RANDOM_TEXT_OF_LENGTH("Generates a random text of given <stringLength> separated into groups by <groupSize>",
            "$RANDOM_TEXT_OF_LENGTH(<stringLength>,<groupSize>)") {
        public String execute(List<String> args) {
            assertArgsCountEquals(args, 2);
            String stringLength = args.get(0);
            String input = CallableFeatureMethod.$RANDOM_STRING_OF_LENGTH.execute(Collections.singletonList(stringLength));

            int noOfGroups = Integer.parseInt(args.get(1));
            int groupOfCharSize = Integer.parseInt(stringLength) / noOfGroups;
            int len = input.length();
            int groupSize = Math.min((int) Math.ceil((double) len / noOfGroups), groupOfCharSize);
            int totalChars = 0;

            StringBuilder result = new StringBuilder();
            for (int i = 0; i < len && totalChars < len; i += groupSize) {
                if (result.length() > 0) {
                    result.append(" ");
                    totalChars++;
                }
                int end = Math.min(i + groupSize, len);
                result.append(input.substring(i, end));
                totalChars += (end - i);
            }

            return result.substring(0, len);
        }
    },
    $STRING_SPLIT("Splits the given string into group of chars with length of given <noOfGroups>, separated by <separator>", "$STRING_SPLIT(<noOfGroups>,<separator>,test-test)") {
        public String execute(List<String> args){
            assertArgsCountEquals(args, 3);
            int noOfGroups = Integer.parseInt(args.get(0));
            String separator = args.get(1);
            String input = args.get(2);
            String stringLength = String.valueOf(input.length());
            int groupSize = Integer.parseInt(stringLength) / noOfGroups;
            int len = input.length();

            StringBuilder result = new StringBuilder();
            for (int i = 0; i < len; i += groupSize){
                if (result.length() > 0){
                    result.append(separator);
                }
                int end = Math.min(i + groupSize, len);
                result.append(input, i, end);
            }
            return result.toString();
        }
    },
    $CAPITALIZE("Capitalize each word of given text") {
        public String execute(List<String> args) {
            assertArgsCountEquals(args, 1);
            List<String> words = List.of(args.get(0).split(" "));
            StringBuilder result = new StringBuilder();

            Iterator<String> wordsIterator = words.listIterator();
            while (wordsIterator.hasNext()){
                String word = wordsIterator.next();
                if (!word.isEmpty()){
                    result.append(Character.toUpperCase(word.charAt(0)))
                            .append(word.substring(1));
                    if (wordsIterator.hasNext()) {
                        result.append(" ");
                    }
                }
            }

            return result.toString();
        }
    };

    private final String description;

    CallableFeatureMethod(final String description, final Object... args) {
        this.description = args != null && args.length > 0 ? String.format(description, args) : description;
    }

    public String description(){
        return description;
    }
}
