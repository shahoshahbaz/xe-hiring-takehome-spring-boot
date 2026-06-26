package com.xe.ratealerts.dto;

import com.xe.ratealerts.model.Direction;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Response body for GET /api/alerts and POST /api/alerts.
 * triggered is evaluated at request time against live rate — not stored.
 */
public record AlertResponse(
        UUID id,
        String pair,
        BigDecimal threshold,
        Direction direction,
        boolean triggered,
        Instant createdAt
) {}