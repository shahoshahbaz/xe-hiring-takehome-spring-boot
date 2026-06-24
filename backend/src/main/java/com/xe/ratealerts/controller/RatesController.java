package com.xe.ratealerts.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

@RestController
@RequestMapping("/api/rates")
public class RatesController {

    @Value("${xecd.account-id}")
    private String accountId;

    @Value("${xecd.api-key}")
    private String apiKey;

    @GetMapping
    public List<Map<String, Object>> getRates() throws JsonProcessingException {
        List<Map<String, Object>> results = new ArrayList<>();

        // USD/CAD
        RestTemplate restTemplate = new RestTemplate();
        String credentials = Base64.getEncoder()
                .encodeToString((accountId + ":" + apiKey).getBytes(StandardCharsets.US_ASCII));
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + credentials);
        ResponseEntity<String> response = restTemplate.exchange(
                "https://xecdapi.xe.com/v1/convert_from.json/?from=USD&to=CAD",
                HttpMethod.GET, new HttpEntity<>(headers), String.class);
        JsonNode doc = new ObjectMapper().readTree(response.getBody());
        BigDecimal mid = doc.get("to").get(0).get("mid").decimalValue().setScale(4, RoundingMode.HALF_UP);
        String timestamp = doc.get("timestamp").asText();
        results.add(Map.of("pair", "USD/CAD", "rate", mid, "asOf", timestamp));

        // GBP/USD
        RestTemplate restTemplate2 = new RestTemplate();
        String credentials2 = Base64.getEncoder()
                .encodeToString((accountId + ":" + apiKey).getBytes(StandardCharsets.US_ASCII));
        HttpHeaders headers2 = new HttpHeaders();
        headers2.set("Authorization", "Basic " + credentials2);
        ResponseEntity<String> response2 = restTemplate2.exchange(
                "https://xecdapi.xe.com/v1/convert_from.json/?from=GBP&to=USD",
                HttpMethod.GET, new HttpEntity<>(headers2), String.class);
        JsonNode doc2 = new ObjectMapper().readTree(response2.getBody());
        BigDecimal mid2 = doc2.get("to").get(0).get("mid").decimalValue().setScale(4, RoundingMode.HALF_UP);
        String timestamp2 = doc2.get("timestamp").asText();
        results.add(Map.of("pair", "GBP/USD", "rate", mid2, "asOf", timestamp2));

        // EUR/USD
        RestTemplate restTemplate3 = new RestTemplate();
        String credentials3 = Base64.getEncoder()
                .encodeToString((accountId + ":" + apiKey).getBytes(StandardCharsets.US_ASCII));
        HttpHeaders headers3 = new HttpHeaders();
        headers3.set("Authorization", "Basic " + credentials3);
        ResponseEntity<String> response3 = restTemplate3.exchange(
                "https://xecdapi.xe.com/v1/convert_from.json/?from=EUR&to=USD",
                HttpMethod.GET, new HttpEntity<>(headers3), String.class);
        JsonNode doc3 = new ObjectMapper().readTree(response3.getBody());
        BigDecimal mid3 = doc3.get("to").get(0).get("mid").decimalValue().setScale(4, RoundingMode.HALF_UP);
        String timestamp3 = doc3.get("timestamp").asText();
        results.add(Map.of("pair", "EUR/USD", "rate", mid3, "asOf", timestamp3));

        return results;
    }
}
