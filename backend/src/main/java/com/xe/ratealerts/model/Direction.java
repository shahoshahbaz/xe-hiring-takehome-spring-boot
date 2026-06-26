package com.xe.ratealerts.model;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Direction of alert evaluation.
 * Using enum over String to enforce valid values at compile time
 * and fix Open/Closed violation — adding a new direction
 * no longer requires modifying isTriggered() logic.
 */
public enum Direction {
    ABOVE("above"),
    BELOW("below");

    private final String value;

    Direction(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}