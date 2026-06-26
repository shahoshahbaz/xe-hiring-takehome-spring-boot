package com.xe.ratealerts.controller;

import com.xe.ratealerts.service.RateService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



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
    public List<Map<String, Object>> getRates(){
    return List.of(
            fetchRate("USD", "CAD"),
            fetchRate("GBP", "USD"),
            fetchRate("EUR", "USD")
    );
}

    private Map<String, Object> fetchRate(String from, String to) {

        return Map.of(
                "pair", from + "/" + to,
                "rate", rateService.getMidRate(from, to)
        );
    }
}
