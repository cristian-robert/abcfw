package com.spring.befwlc.payload;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PayloadValue {

    private final String value;
    private boolean isHistory;

    public PayloadValue(final String value) {
        this.value = value;
    }
}
