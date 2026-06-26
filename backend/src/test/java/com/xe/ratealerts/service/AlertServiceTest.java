package com.xe.ratealerts.service;


import com.xe.ratealerts.dto.AlertResponse;
import com.xe.ratealerts.dto.CreateAlertRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlertServiceTest {

    @Mock
    private RateService rateService;

    @InjectMocks
    private AlertService alertService;



    @BeforeEach
    void setUp() {
        // no global mock here
    }

    @Test
    void alert_triggered_when_rate_above_threshold() {
        when(rateService.getMidRate("USD/CAD")).thenReturn(new BigDecimal("1.3800"));
        CreateAlertRequest request = new CreateAlertRequest("USD/CAD", new BigDecimal("1.3500"), "above");
        AlertResponse response = alertService.create(request);
        assertThat(response.triggered()).isTrue();
    }

    @Test
    void alert_not_triggered_when_rate_below_threshold() {
        when(rateService.getMidRate("USD/CAD")).thenReturn(new BigDecimal("1.3800"));
        CreateAlertRequest request = new CreateAlertRequest("USD/CAD", new BigDecimal("1.4000"), "above");
        AlertResponse response = alertService.create(request);
        assertThat(response.triggered()).isFalse();
    }

    @Test
    void alert_triggered_when_rate_below_threshold_direction_below() {
        when(rateService.getMidRate("USD/CAD")).thenReturn(new BigDecimal("1.3800"));
        CreateAlertRequest request = new CreateAlertRequest("USD/CAD", new BigDecimal("1.4000"), "below");
        AlertResponse response = alertService.create(request);
        assertThat(response.triggered()).isTrue();
    }

    @Test
    void alert_not_triggered_when_rate_above_threshold_direction_below() {
        when(rateService.getMidRate("USD/CAD")).thenReturn(new BigDecimal("1.3800"));
        CreateAlertRequest request = new CreateAlertRequest("USD/CAD", new BigDecimal("1.3500"), "below");
        AlertResponse response = alertService.create(request);
        assertThat(response.triggered()).isFalse();
    }

    @Test
    void alert_not_triggered_when_rate_exactly_at_threshold() {
        when(rateService.getMidRate("USD/CAD")).thenReturn(new BigDecimal("1.3800"));
        CreateAlertRequest request = new CreateAlertRequest("USD/CAD", new BigDecimal("1.3800"), "above");
        AlertResponse response = alertService.create(request);
        assertThat(response.triggered()).isFalse();
    }

    @Test
    void created_alert_appears_in_list() {
        when(rateService.getMidRate("USD/CAD")).thenReturn(new BigDecimal("1.3800"));
        CreateAlertRequest request = new CreateAlertRequest("USD/CAD", new BigDecimal("1.3500"), "above");
        AlertResponse created = alertService.create(request);
        List<AlertResponse> all = alertService.listAll();
        assertThat(all).anyMatch(a -> a.id().equals(created.id()));
    }

    @Test
    void deleted_alert_removed_from_list() {
        when(rateService.getMidRate("USD/CAD")).thenReturn(new BigDecimal("1.3800"));
        CreateAlertRequest request = new CreateAlertRequest("USD/CAD", new BigDecimal("1.3500"), "above");
        AlertResponse created = alertService.create(request);
        boolean deleted = alertService.delete(created.id());
        assertThat(deleted).isTrue();
        assertThat(alertService.listAll()).isEmpty();
    }

    @Test
    void delete_returns_false_for_unknown_id() {
        // no mock needed — never calls RateService
        boolean deleted = alertService.delete(UUID.randomUUID());
        assertThat(deleted).isFalse();
    }
}