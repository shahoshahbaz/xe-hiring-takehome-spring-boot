package com.xe.ratealerts.dto;

import java.math.BigDecimal;

/**
 * Request body for POST /api/alerts.
 * Client provides pair, threshold, and direction.
 * id and createdAt are assigned by the server.
 */
public record CreateAlertRequest(
        String pair,
        BigDecimal threshold,
        String direction
) {}