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
        // Static methods
        registry.register("$FULL_UUID", (args, ctx) -> UUID.randomUUID().toString());
        registry.register("$SHORT_UUID", (args, ctx) -> UUID.randomUUID().toString().replace("-", ""));
        registry.register("$5_RANDOM_CHARS", (args, ctx) -> UUID.randomUUID().toString().substring(0, 5));
        registry.register("$MS_UUID", (args, ctx) -> "MS" + UUID.randomUUID().toString().replace("-", ""));
        registry.register("$SYS_SHORT_DATE", (args, ctx) -> DateUtils.shortDate());
        registry.register("$SYS_FULL_OFFSET_DATE", (args, ctx) -> DateUtils.fullOffsetDate());
        registry.register("$SYS_FULL_DATE_PLUS_ONE_HOUR", (args, ctx) -> DateUtils.fullOffsetDatePlusOneHour());
        registry.register("$SYS_FULL_DATE", (args, ctx) -> DateUtils.fullDate());
        registry.register("$SYS_ISO_DATE", (args, ctx) -> DateUtils.isoDate());
        registry.register("$MISSING", (args, ctx) -> "$MISSING");
        registry.register("$EMPTY_STRING", (args, ctx) -> "");
        registry.register("$SPOT_SECRET", (args, ctx) -> "@bracadabra");
        registry.register("$COMMA", (args, ctx) -> ",");
        registry.register("$BLANK_SPACE", (args, ctx) -> " ");
        registry.register("$TASR_REF", (args, ctx) -> { synchronized (BuiltInFunctions.class) { return DateUtils.ofFormat("MMddHmmssSSSSS"); } });
        registry.register("$LAST_PAYLOAD", (args, ctx) -> ctx.get(ContextKey.LAST_PAYLOAD, String.class));

        // Callable methods
        registry.register("$PAYLOAD_VALUE", (args, ctx) -> {
            assertArgCount(args, 1, "$PAYLOAD_VALUE");
            Map<String, String> values = ctx.getPayloadValues();
            String key = args.get(0);
            if (!values.containsKey(key)) throw new TestExecutionException("No payload value found for key: %s", key);
            return values.get(key);
        });
        registry.register("$TO_UPPER_CASE", (args, ctx) -> { assertArgCount(args, 1, "$TO_UPPER_CASE"); return args.get(0).toUpperCase(); });
        registry.register("$REMOVE_ALL_CHARS", (args, ctx) -> { assertArgCount(args, 2, "$REMOVE_ALL_CHARS"); return args.get(1).replaceAll(args.get(0), ""); });
        registry.register("$REPLACE_ALL_CHARS", (args, ctx) -> { assertArgCount(args, 3, "$REPLACE_ALL_CHARS"); return args.get(2).replaceAll(args.get(0), args.get(1)); });
        registry.register("$SYS_DATE_OF_FORMAT", (args, ctx) -> { assertArgCount(args, 1, "$SYS_DATE_OF_FORMAT"); return DateUtils.ofFormat(args.get(0)); });
        registry.register("$SYS_FULL_DATE_PLUS_MINUTES", (args, ctx) -> { assertArgCount(args, 1, "$SYS_FULL_DATE_PLUS_MINUTES"); return DateUtils.fullDatePlusMinutes(Integer.parseInt(args.get(0))); });
        registry.register("$SYS_SHORT_DATE_PLUS_DAYS", (args, ctx) -> { assertArgCount(args, 1, "$SYS_SHORT_DATE_PLUS_DAYS"); return DateUtils.shortDatePlusDays(Integer.parseInt(args.get(0))); });
        registry.register("$SYS_SHORT_DATE_MINUS_DAYS", (args, ctx) -> { assertArgCount(args, 1, "$SYS_SHORT_DATE_MINUS_DAYS"); return DateUtils.shortDateMinusDays(Integer.parseInt(args.get(0))); });
        registry.register("$SYS_FULL_DATE_MINUS_MINUTES", (args, ctx) -> { assertArgCount(args, 1, "$SYS_FULL_DATE_MINUS_MINUTES"); return DateUtils.fullDateMinusMinutes(Integer.parseInt(args.get(0))); });
        registry.register("$FORMAT_STRING_DATE", (args, ctx) -> { assertArgCount(args, 3, "$FORMAT_STRING_DATE"); return DateUtils.reformatDate(args.get(0), args.get(1), args.get(2)); });
        registry.register("$UUID_FROM_STRINGS", (args, ctx) -> { assertMinArgs(args, 1, "$UUID_FROM_STRINGS"); return UUID.nameUUIDFromBytes(String.join("", args).getBytes(StandardCharsets.UTF_8)).toString(); });
        registry.register("$MERGE_VALUES", (args, ctx) -> { assertMinArgs(args, 1, "$MERGE_VALUES"); return String.join("", args); });
        registry.register("$REMOVE_DASHES", (args, ctx) -> { assertMinArgs(args, 1, "$REMOVE_DASHES"); return String.join("", args).replace("-", ""); });
        registry.register("$LAST_MATCHED_RECORD", (args, ctx) -> { assertArgCount(args, 1, "$LAST_MATCHED_RECORD"); ObjectNode record = ctx.get(ContextKey.LAST_MATCHED_RECORD, ObjectNode.class); return JsonPathResolver.resolve(record, args.get(0)).asText(); });
        registry.register("$RANDOM_STRING_OF_LENGTH", (args, ctx) -> {
            assertArgCount(args, 1, "$RANDOM_STRING_OF_LENGTH");
            int length = Integer.parseInt(args.get(0));
            Random random = new Random();
            StringBuilder sb = new StringBuilder(length);
            for (int i = 0; i < length; i++) sb.append((char) ('a' + random.nextInt(26)));
            return sb.toString();
        });
        registry.register("$RANDOM_TEXT_OF_LENGTH", (args, ctx) -> {
            assertArgCount(args, 2, "$RANDOM_TEXT_OF_LENGTH");
            int totalLength = Integer.parseInt(args.get(0));
            int numGroups = Integer.parseInt(args.get(1));
            String raw = registry.get("$RANDOM_STRING_OF_LENGTH").apply(List.of(String.valueOf(totalLength)), ctx);
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
                    result.append(Character.toUpperCase(words[i].charAt(0))).append(words[i].substring(1));
                    if (i < words.length - 1) result.append(" ");
                }
            }
            return result.toString();
        });
    }

    private void assertArgCount(List<String> args, int expected, String methodName) {
        if (args.size() != expected) throw new TestExecutionException("%s expects %d argument(s), got %d", methodName, expected, args.size());
    }
    private void assertMinArgs(List<String> args, int min, String methodName) {
        if (args.size() < min) throw new TestExecutionException("%s expects at least %d argument(s), got %d", methodName, min, args.size());
    }
}
