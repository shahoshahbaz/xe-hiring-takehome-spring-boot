package com.xe.ratealerts.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

// =====================================================================================
// STUB CONTROLLER - provided for the FRONTEND track.
//
// This gives frontend candidates a working /api/alerts API so they can build the alert
// management UI without writing backend code. It keeps alerts in an in-memory list and
// evaluates the "triggered" flag against the canned rates below, so creating an alert
// with a threshold on the wrong side of the canned rate will show as triggered.
//
// BACKEND-TRACK CANDIDATES: this is not a partial solution and you are not expected to
// keep it. Replace it or delete it; the alert feature is yours to design.
// =====================================================================================

@RestController
@RequestMapping("/api/alerts")
public class AlertsStubController {

    private static final Map<String, BigDecimal> CANNED_RATES = Map.of(
            "USD/CAD", new BigDecimal("1.3650"),
            "GBP/USD", new BigDecimal("1.2710"),
            "EUR/USD", new BigDecimal("1.0830"));

    private final List<Alert> alerts = new CopyOnWriteArrayList<>();

    @GetMapping
    public List<AlertView> list() {
        return alerts.stream().map(this::toView).toList();
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateAlertRequest request) {
        String pair = request.pair();
        if (pair == null || !CANNED_RATES.containsKey(pair)) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Unknown pair '" + pair + "'. The stub supports: "
                            + String.join(", ", CANNED_RATES.keySet()) + "."));
        }

        String direction = request.direction();
        if (!"above".equals(direction) && !"below".equals(direction)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Direction must be 'above' or 'below'."));
        }

        Alert alert = new Alert(UUID.randomUUID(), pair, request.threshold(), direction);
        alerts.add(alert);

        return ResponseEntity.created(URI.create("/api/alerts/" + alert.id())).body(toView(alert));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        boolean removed = alerts.removeIf(alert -> alert.id().equals(id));
        return removed ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    private AlertView toView(Alert alert) {
        return new AlertView(alert.id(), alert.pair(), alert.threshold(), alert.direction(), isTriggered(alert));
    }

    private boolean isTriggered(Alert alert) {
        BigDecimal rate = CANNED_RATES.get(alert.pair());
        return "above".equals(alert.direction())
                ? rate.compareTo(alert.threshold()) > 0
                : rate.compareTo(alert.threshold()) < 0;
    }

    public record Alert(UUID id, String pair, BigDecimal threshold, String direction) {
    }

    public record CreateAlertRequest(String pair, BigDecimal threshold, String direction) {
    }

    public record AlertView(UUID id, String pair, BigDecimal threshold, String direction, boolean triggered) {
    }
}
