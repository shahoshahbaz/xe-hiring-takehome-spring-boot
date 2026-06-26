package com.xe.ratealerts.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

/**
 * Request body for POST /api/alerts.
 * Client provides pair, threshold, and direction.
 * id and createdAt are assigned by the server.
 */
public record CreateAlertRequest(

        @NotBlank(message = "Pair is required e.g. USD/CAD")
        @Pattern(regexp = "^(USD/CAD|GBP/USD|EUR/USD)$", message = "Pair must be one of: USD/CAD, GBP/USD, EUR/USD")
        String pair,

        @NotNull(message = "Threshold is required")
        @DecimalMin(value = "0.0001", message = "Threshold must be greater than 0")
        BigDecimal threshold,

        @NotBlank(message = "Direction is required")
        @Pattern(regexp = "above|below", message = "Direction must be 'above' or 'below'")
        String direction

) {}