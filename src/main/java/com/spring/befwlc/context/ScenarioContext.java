package com.spring.befwlc.context;

import com.spring.befwlc.exceptions.TestExecutionException;
import com.spring.befwlc.payload.PayloadValue;
import io.cucumber.spring.ScenarioScope;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;


@Component
@ScenarioScope
@SuppressWarnings("unchecked")
public class ScenarioContext {
    private final Map<Enum<?>, Object> context = new HashMap<>();
    public void put(final Enum<?> key, Object value) {
        context.put(key, value);
    }

    public <T> T get(final Enum<?> key, Class<T> type){
        if(!containsValue(key)){
            throw new TestExecutionException("Object not found in ScenarioContext for key: %s", key.name());
        }

        Object value = context.get(key);
        if(type.isAssignableFrom(value.getClass())){
            return type.cast(value);
        }
        throw new TestExecutionException("Object in ScenarioContext for key '%s' is not of expected type '$s'", key.name(), type.getSimpleName());
    }

    public boolean containsValue(final Enum<?> key){
        return context.containsKey(key);
    }

    public Map<String, PayloadValue> getPayloadValues(){
        return containsValue(ScenarioContextKeys.PAYLOAD_VALUES) ? this.get(ScenarioContextKeys.PAYLOAD_VALUES, Map.class) : new HashMap<>();
    }
}
