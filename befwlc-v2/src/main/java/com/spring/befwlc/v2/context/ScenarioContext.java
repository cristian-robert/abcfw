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

    public void put(ContextKey key, Object value) { store.put(key, value); }

    public <T> T get(ContextKey key, Class<T> type) {
        Object value = store.get(key);
        if (value == null) throw new TestExecutionException("No value found in ScenarioContext for key: %s", key);
        if (!type.isAssignableFrom(value.getClass())) {
            throw new TestExecutionException("Type mismatch for key '%s': expected %s but found %s",
                    key, type.getSimpleName(), value.getClass().getSimpleName());
        }
        return type.cast(value);
    }

    public boolean contains(ContextKey key) { return store.containsKey(key); }

    @SuppressWarnings("unchecked")
    public Map<String, String> getPayloadValues() {
        if (!store.containsKey(ContextKey.PAYLOAD_VALUES)) return Collections.emptyMap();
        return (Map<String, String>) store.get(ContextKey.PAYLOAD_VALUES);
    }
}
