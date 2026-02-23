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
        if (input == null) return "null";

        if (RegexUtils.matches(input, CALLABLE_METHOD_PATTERN)) {
            String methodName = RegexUtils.captureSingle(input, CALLABLE_METHOD_NAME);
            String argsStr = input.substring(methodName.length() + 1, input.length() - 1);
            List<String> rawArgs = RegexUtils.captureAll(argsStr, CALLABLE_METHOD_ARGS);
            List<String> resolvedArgs = new ArrayList<>();
            for (String arg : rawArgs) resolvedArgs.add(resolve(arg.trim(), context));
            String cleanName = "$" + methodName.substring(1, methodName.length() - 1);
            DslFunction fn = registry.get(cleanName);
            return fn.apply(resolvedArgs, context);
        }

        if (RegexUtils.matches(input, STATIC_METHOD_PATTERN)) {
            DslFunction fn = registry.get(input);
            return fn.apply(List.of(), context);
        }

        return input;
    }
}
