package com.xe.ratealerts.service;

import com.xe.ratealerts.dto.AlertResponse;
import com.xe.ratealerts.dto.CreateAlertRequest;
import com.xe.ratealerts.model.Alert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Core alert logic.
 * Stores alerts in-memory using ConcurrentHashMap — thread-safe without
 * synchronization blocks. Trade-off: alerts are lost on restart.
 * Would swap to a JPA repository for production persistence.
 */
@Service
public class AlertService {
    private static final Logger log = LoggerFactory.getLogger(AlertService.class);

    private final Map<UUID, Alert> alerts = new ConcurrentHashMap<>();
    private final RateService rateService;

    public AlertService(RateService rateService) {
        this.rateService = rateService;
    }

    public AlertResponse create(CreateAlertRequest request) {
        Alert alert = new Alert(
                UUID.randomUUID(),
                request.pair(),
                request.threshold(),
                request.direction(),
                Instant.now()
        );
        alerts.put(alert.id(), alert);
        log.info("Alert created: id={}, pair={}, direction={}, threshold={}",
                alert.id(), alert.pair(), alert.direction(), alert.threshold());
        return toResponse(alert);
    }

    public List<AlertResponse> listAll() {
        log.debug("Listing all alerts, count={}", alerts.size());
        return alerts.values().stream()
                .map(this::toResponse)
                .toList();
    }

    public boolean delete(UUID id) {
        boolean removed = alerts.remove(id) != null;
        if (removed) {
            log.info("Alert deleted: id={}", id);
        } else {
            log.warn("Attempted to delete unknown alert: id={}", id);
        }
        return removed;
    }

    /**
     * Evaluates whether an alert is triggered against the live rate.
     * Triggered = rate is strictly above or below threshold.
     * At threshold = not triggered (strict comparison).
     */
    boolean isTriggered(Alert alert) {
        BigDecimal liveRate = rateService.getMidRate(alert.pair());
        int comparison = liveRate.compareTo(alert.threshold());
        boolean triggered = "above".equals(alert.direction()) ? comparison > 0 : comparison < 0;
        if (triggered) {
            log.info("Alert TRIGGERED: pair={}, liveRate={}, threshold={}, direction={}",
                    alert.pair(), liveRate, alert.threshold(), alert.direction());
        } else {
            log.debug("Alert watching: pair={}, liveRate={}, threshold={}, direction={}",
                    alert.pair(), liveRate, alert.threshold(), alert.direction());
        }
        return triggered;
    }

    private AlertResponse toResponse(Alert alert) {
        return new AlertResponse(
                alert.id(),
                alert.pair(),
                alert.threshold(),
                alert.direction(),
                isTriggered(alert),
                alert.createdAt()
        );
    }
}