package com.xe.ratealerts.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Immutable record representing a stored alert.
 * Chose record over class — pure data container, immutability is correct here.
 * Note: not suitable for JPA if persistence is added later.
 */
public record Alert(
        UUID id,
        String pair,
        BigDecimal threshold,
        Direction direction,
        Instant createdAt
) {}