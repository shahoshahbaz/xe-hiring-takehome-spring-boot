package com.xe.ratealerts.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Wraps the XE Currency Data API.
 * Extracted from RatesController which had the same HTTP call
 * copy-pasted three times. AlertService also uses this to evaluate triggers.
 */
@Service
public class RateService {
    private static final Logger log = LoggerFactory.getLogger(RateService.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String credentials;
    private final String baseUrl;

    public RateService(
            @Value("${xecd.account-id}") String accountId,
            @Value("${xecd.api-key}") String apiKey,
            @Value("${xecd.base-url}") String baseUrl) {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        this.baseUrl = baseUrl;
        this.credentials = Base64.getEncoder()
                .encodeToString((accountId + ":" + apiKey).getBytes(StandardCharsets.US_ASCII));
    }

    /**
     * Fetch live mid-rate for a "USD/CAD" style pair string.
     */
    public BigDecimal getMidRate(String pair) {
        // validate pair format and split into from/to currencies
        String[] parts = pair.split("/");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid pair format: " + pair + ". Expected: USD/CAD");
        }
        return getMidRate(parts[0], parts[1]);
    }

    /**
     * Fetch live mid-rate for a given from/to currency.
     */
    public BigDecimal getMidRate(String from, String to) {
        try {
            log.debug("Fetching rate for {}/{}", from, to);
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Basic " + credentials);

            ResponseEntity<String> response = restTemplate.exchange(
                    baseUrl + "?from=" + from + "&to=" + to,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    String.class);

            JsonNode doc = objectMapper.readTree(response.getBody());
            BigDecimal rate = doc.get("to").get(0).get("mid")
                    .decimalValue()
                    .setScale(4, RoundingMode.HALF_UP);

            log.debug("Rate fetched: {}/{} = {}", from, to, rate);
            return rate;

        } catch (Exception e) {
            log.error("Failed to fetch rate for {}/{}: {}", from, to, e.getMessage());
            throw new RuntimeException("Failed to fetch rate for " + from + "/" + to, e);
        }
    }
}