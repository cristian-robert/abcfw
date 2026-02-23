package com.spring.befwlc.v2.dsl;

import com.spring.befwlc.v2.context.ScenarioContext;
import java.util.List;

@FunctionalInterface
public interface DslFunction {
    String apply(List<String> args, ScenarioContext context);
}
