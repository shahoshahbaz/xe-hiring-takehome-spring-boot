package com.xe.ratealerts.dto;

public record CurrencyResponse(
        String iso,
        String currencyName,
        String currencySymbol

) {}