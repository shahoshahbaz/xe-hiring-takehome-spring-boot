package com.xe.ratealerts.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xe.ratealerts.service.RateService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Refactored to use RateService — previously had the same HTTP call
 * copy-pasted three times for USD/CAD, GBP/USD, EUR/USD.
 */
@RestController
@RequestMapping("/api/rates")
public class RatesController {

    private final RateService rateService;

    public RatesController(RateService rateService) {
        this.rateService = rateService;
    }


    @GetMapping
    public List<Map<String, Object>> getRates() throws JsonProcessingException {
    return List.of(
            fetchRate("USD", "CAD"),
            fetchRate("GBP", "USD"),
            fetchRate("EUR", "USD")
    );
}

    private Map<String, Object> fetchRate(String from, String to) {
        BigDecimal midRate = rateService.getMidRate(from, to);
        return Map.of(
                "pair", from + "/" + to,
                "rate", rateService.getMidRate(from, to)
        );
    }
}
