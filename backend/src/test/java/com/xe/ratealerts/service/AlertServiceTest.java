package com.xe.ratealerts.service;


import com.xe.ratealerts.dto.AlertResponse;
import com.xe.ratealerts.dto.CreateAlertRequest;
import com.xe.ratealerts.dto.CurrencyResponse;
import com.xe.ratealerts.model.Direction;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlertServiceTest {

    @Mock
    private RateService rateService;

    @InjectMocks
    private AlertService alertService;



    @BeforeEach
    void setUp() {
        lenient().when(rateService.getSupportedCurrencies()).thenReturn(List.of(
                new CurrencyResponse("USD", "US Dollar", "$"),
                new CurrencyResponse("CAD", "Canadian Dollar", "$")
        ));
    }

    @Test
    void alert_triggered_when_rate_above_threshold() {
        when(rateService.getMidRate("USD/CAD")).thenReturn(new BigDecimal("1.3800"));
        CreateAlertRequest request = new CreateAlertRequest("USD/CAD", new BigDecimal("1.3500"), Direction.ABOVE);
        AlertResponse response = alertService.create(request);
        assertThat(response.triggered()).isTrue();
    }

    @Test
    void alert_not_triggered_when_rate_below_threshold() {
        when(rateService.getMidRate("USD/CAD")).thenReturn(new BigDecimal("1.3800"));
        CreateAlertRequest request = new CreateAlertRequest("USD/CAD", new BigDecimal("1.4000"), Direction.ABOVE);
        AlertResponse response = alertService.create(request);
        assertThat(response.triggered()).isFalse();
    }

    @Test
    void alert_triggered_when_rate_below_threshold_direction_below() {
        when(rateService.getMidRate("USD/CAD")).thenReturn(new BigDecimal("1.3800"));
        CreateAlertRequest request = new CreateAlertRequest("USD/CAD", new BigDecimal("1.4000"), Direction.BELOW);
        AlertResponse response = alertService.create(request);
        assertThat(response.triggered()).isTrue();
    }

    @Test
    void alert_not_triggered_when_rate_above_threshold_direction_below() {
        when(rateService.getMidRate("USD/CAD")).thenReturn(new BigDecimal("1.3800"));
        CreateAlertRequest request = new CreateAlertRequest("USD/CAD", new BigDecimal("1.3500"), Direction.BELOW);
        AlertResponse response = alertService.create(request);
        assertThat(response.triggered()).isFalse();
    }

    @Test
    void alert_not_triggered_when_rate_exactly_at_threshold() {
        when(rateService.getMidRate("USD/CAD")).thenReturn(new BigDecimal("1.3800"));
        CreateAlertRequest request = new CreateAlertRequest("USD/CAD", new BigDecimal("1.3800"), Direction.ABOVE);
        AlertResponse response = alertService.create(request);
        assertThat(response.triggered()).isFalse();
    }

    @Test
    void created_alert_appears_in_list() {
        when(rateService.getMidRate("USD/CAD")).thenReturn(new BigDecimal("1.3800"));
        CreateAlertRequest request = new CreateAlertRequest("USD/CAD", new BigDecimal("1.3500"), Direction.ABOVE);
        AlertResponse created = alertService.create(request);
        List<AlertResponse> all = alertService.listAll();
        assertThat(all).anyMatch(a -> a.id().equals(created.id()));
    }

    @Test
    void deleted_alert_removed_from_list() {
        when(rateService.getMidRate("USD/CAD")).thenReturn(new BigDecimal("1.3800"));
        CreateAlertRequest request = new CreateAlertRequest("USD/CAD", new BigDecimal("1.3500"), Direction.ABOVE);
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


    @Test
    void invalid_pair_throws_exception() {
        when(rateService.getSupportedCurrencies()).thenReturn(List.of(
                new CurrencyResponse("USD", "US Dollar", "$"),
                new CurrencyResponse("CAD", "Canadian Dollar", "$")
        ));
        CreateAlertRequest request = new CreateAlertRequest("XYZ/ABC", new BigDecimal("1.23"), Direction.ABOVE);
        assertThatThrownBy(() -> alertService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid pair");
    }

    @Test
    void valid_dynamic_pair_creates_alert() {
        when(rateService.getSupportedCurrencies()).thenReturn(List.of(
                new CurrencyResponse("GBP", "British Pound", "£"),
                new CurrencyResponse("CAD", "Canadian Dollar", "$")
        ));
        when(rateService.getMidRate("GBP/CAD")).thenReturn(new BigDecimal("1.2345"));
        CreateAlertRequest request = new CreateAlertRequest("GBP/CAD", new BigDecimal("1.20"), Direction.ABOVE);
        AlertResponse response = alertService.create(request);
        assertThat(response.pair()).isEqualTo("GBP/CAD");
        assertThat(response.triggered()).isTrue();
    }

    @Test
    void rate_fetch_failure_returns_evaluation_error() {
        when(rateService.getMidRate("BTC/CAD")).thenThrow(new RuntimeException("Rate unavailable"));
        when(rateService.getSupportedCurrencies()).thenReturn(List.of(
                new CurrencyResponse("BTC", "Bitcoin", "₿"),
                new CurrencyResponse("CAD", "Canadian Dollar", "$")
        ));
        CreateAlertRequest request = new CreateAlertRequest("BTC/CAD", new BigDecimal("50000"), Direction.ABOVE);
        AlertResponse response = alertService.create(request);
        assertThat(response.triggered()).isFalse();
        assertThat(response.evaluationError()).isNotNull();
    }
}