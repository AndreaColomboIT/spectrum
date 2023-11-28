package io.github.giulong.spectrum.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;

@SuppressWarnings("unused")
@AllArgsConstructor
public enum Frame {

    AUTO_BEFORE("autoBefore"),
    AUTO_AFTER("autoAfter"),
    MANUAL("manual");

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }
}
