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
            String available = functions.keySet().stream().sorted().collect(Collectors.joining("\n  "));
            throw new TestExecutionException("Unknown DSL method: %s%nAvailable methods:%n  %s", name, available);
        }
        return fn;
    }

    public boolean contains(String name) { return functions.containsKey(name); }
}
