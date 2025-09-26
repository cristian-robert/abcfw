package com.spring.befwlc.configuration;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AwaitConfiguration {
    private final long interval;
    private final long iterations;

    public AwaitConfiguration(long interval, long iterations){
        this.interval = interval;
        this.iterations = iterations;
    }

}
