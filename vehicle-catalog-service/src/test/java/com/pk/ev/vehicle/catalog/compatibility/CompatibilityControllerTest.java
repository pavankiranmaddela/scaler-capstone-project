package com.pk.ev.vehicle.catalog.compatibility;

import com.pk.ev.vehicle.catalog.chargingspec.enums.ConnectorType;
import com.pk.ev.vehicle.catalog.compatibility.CompatibilityDtos.*;
import com.pk.ev.vehicle.catalog.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class CompatibilityControllerTest {

    @Mock
    private CompatibilityService compatibilityService;

    @InjectMocks
    private CompatibilityController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private CompatibilityResult compatibleResult(UUID variantId, UUID stationId) {
        return new CompatibilityResult(
                variantId, "Tiago EV XZ+", stationId, null,
                true, List.of(), 7200, 60, null);
    }

    private CompatibilityResult incompatibleResult(UUID variantId) {
        return new CompatibilityResult(
                variantId, "Tiago EV XE", null, ConnectorType.CCS2,
                false, List.of(), null, null, "No connector match");
    }

    // ─── checkVariantAgainstStation ──────────────────────────────────────────

    @Test
    void checkVariantAgainstStation_returns200WithResult() {
        UUID variantId = UUID.randomUUID();
        UUID stationId = UUID.randomUUID();
        CompatibilityResult expected = compatibleResult(variantId, stationId);

        when(compatibilityService.checkVariantAgainstStation(variantId, stationId)).thenReturn(expected);

        ResponseEntity<CompatibilityResult> response =
                controller.checkVariantAgainstStation(variantId, stationId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expected);
        verify(compatibilityService).checkVariantAgainstStation(variantId, stationId);
    }

    @Test
    void checkVariantAgainstStation_propagatesResourceNotFoundException() {
        UUID variantId = UUID.randomUUID();
        UUID stationId = UUID.randomUUID();

        when(compatibilityService.checkVariantAgainstStation(variantId, stationId))
                .thenThrow(new ResourceNotFoundException("VariantListing not found: " + variantId));

        assertThatThrownBy(() -> controller.checkVariantAgainstStation(variantId, stationId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(variantId.toString());
    }

    @Test
    void checkVariantAgainstStation_incompatibleResult_stillReturns200() {
        UUID variantId = UUID.randomUUID();
        UUID stationId = UUID.randomUUID();
        CompatibilityResult incompatible = new CompatibilityResult(
                variantId, "Tiago EV XE", stationId, null,
                false, List.of(), null, null, "No connector match");

        when(compatibilityService.checkVariantAgainstStation(variantId, stationId))
                .thenReturn(incompatible);

        ResponseEntity<CompatibilityResult> response =
                controller.checkVariantAgainstStation(variantId, stationId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().isCompatible()).isFalse();
        assertThat(response.getBody().incompatibilityReason()).isEqualTo("No connector match");
    }

    // ─── checkVariantAgainstConnector ────────────────────────────────────────

    @Test
    void checkVariantAgainstConnector_returns200WithResult() {
        UUID variantId = UUID.randomUUID();
        CompatibilityResult expected = new CompatibilityResult(
                variantId, "Tiago EV XZ+", null, ConnectorType.TYPE2,
                true, List.of(), 7200, 60, null);

        when(compatibilityService.checkVariantAgainstConnector(variantId, ConnectorType.TYPE2, 22000))
                .thenReturn(expected);

        ResponseEntity<CompatibilityResult> response =
                controller.checkVariantAgainstConnector(variantId, ConnectorType.TYPE2, 22000);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expected);
        verify(compatibilityService).checkVariantAgainstConnector(variantId, ConnectorType.TYPE2, 22000);
    }

    @Test
    void checkVariantAgainstConnector_withNullWattage_passesNullToService() {
        UUID variantId = UUID.randomUUID();
        CompatibilityResult expected = new CompatibilityResult(
                variantId, "Tiago EV XZ+", null, ConnectorType.TYPE2,
                true, List.of(), 7200, 60, null);

        when(compatibilityService.checkVariantAgainstConnector(variantId, ConnectorType.TYPE2, null))
                .thenReturn(expected);

        ResponseEntity<CompatibilityResult> response =
                controller.checkVariantAgainstConnector(variantId, ConnectorType.TYPE2, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(compatibilityService).checkVariantAgainstConnector(variantId, ConnectorType.TYPE2, null);
    }

    @Test
    void checkVariantAgainstConnector_propagatesNotFoundException() {
        UUID variantId = UUID.randomUUID();

        when(compatibilityService.checkVariantAgainstConnector(variantId, ConnectorType.CCS2, null))
                .thenThrow(new ResourceNotFoundException("VariantListing not found: " + variantId));

        assertThatThrownBy(() -> controller.checkVariantAgainstConnector(variantId, ConnectorType.CCS2, null))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ─── getCompatibleVariantsForStation ─────────────────────────────────────

    @Test
    void getCompatibleVariantsForStation_returns200WithResult() {
        UUID stationId = UUID.randomUUID();
        StationCompatibleVariants expected = new StationCompatibleVariants(stationId, List.of(), 0);

        when(compatibilityService.getCompatibleVariantsForStation(stationId)).thenReturn(expected);

        ResponseEntity<StationCompatibleVariants> response =
                controller.getCompatibleVariantsForStation(stationId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expected);
        verify(compatibilityService).getCompatibleVariantsForStation(stationId);
    }

    @Test
    void getCompatibleVariantsForStation_withCompatibleVariants_returnsAll() {
        UUID stationId = UUID.randomUUID();
        CompatibleVariantSummary summary = new CompatibleVariantSummary(
                UUID.randomUUID(), "Tiago EV XZ+", "Tata", "Tiago EV", 2024, 7200, 60);
        StationCompatibleVariants expected = new StationCompatibleVariants(stationId, List.of(summary), 1);

        when(compatibilityService.getCompatibleVariantsForStation(stationId)).thenReturn(expected);

        ResponseEntity<StationCompatibleVariants> response =
                controller.getCompatibleVariantsForStation(stationId);

        assertThat(response.getBody().totalCount()).isEqualTo(1);
        assertThat(response.getBody().compatibleVariants()).hasSize(1);
    }

    // ─── getCompatibleVariantsForConnector ───────────────────────────────────

    @Test
    void getCompatibleVariantsForConnector_returns200WithResult() {
        ConnectorCompatibleVariants expected =
                new ConnectorCompatibleVariants(ConnectorType.TYPE2, List.of(), 0);

        when(compatibilityService.getCompatibleVariantsForConnector(ConnectorType.TYPE2, null))
                .thenReturn(expected);

        ResponseEntity<ConnectorCompatibleVariants> response =
                controller.getCompatibleVariantsForConnector(ConnectorType.TYPE2, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expected);
        verify(compatibilityService).getCompatibleVariantsForConnector(ConnectorType.TYPE2, null);
    }

    @Test
    void getCompatibleVariantsForConnector_withMaxWattage_passesThrough() {
        ConnectorCompatibleVariants expected =
                new ConnectorCompatibleVariants(ConnectorType.CCS2, List.of(), 0);

        when(compatibilityService.getCompatibleVariantsForConnector(ConnectorType.CCS2, 50000))
                .thenReturn(expected);

        ResponseEntity<ConnectorCompatibleVariants> response =
                controller.getCompatibleVariantsForConnector(ConnectorType.CCS2, 50000);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(compatibilityService).getCompatibleVariantsForConnector(ConnectorType.CCS2, 50000);
    }

    // ─── bulkCheck ───────────────────────────────────────────────────────────

    @Test
    void bulkCheck_returns200WithAggregatedCounts() {
        UUID variantId = UUID.randomUUID();
        UUID stationId = UUID.randomUUID();
        CompatibilityResult r = compatibleResult(variantId, stationId);
        BulkCompatibilityResponse expected = new BulkCompatibilityResponse(List.of(r), 1, 1, 0);

        BulkCompatibilityRequest request = new BulkCompatibilityRequest(
                List.of(new BulkCompatibilityRequest.CompatibilityPair(variantId, stationId)));

        when(compatibilityService.bulkCheck(request)).thenReturn(expected);

        ResponseEntity<BulkCompatibilityResponse> response = controller.bulkCheck(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().totalChecked()).isEqualTo(1);
        assertThat(response.getBody().compatibleCount()).isEqualTo(1);
        assertThat(response.getBody().incompatibleCount()).isZero();
    }

    @Test
    void bulkCheck_withMixedResults_reflectsCorrectCounts() {
        UUID v1 = UUID.randomUUID(), v2 = UUID.randomUUID();
        UUID s1 = UUID.randomUUID(), s2 = UUID.randomUUID();
        CompatibilityResult r1 = compatibleResult(v1, s1);
        CompatibilityResult r2 = new CompatibilityResult(
                v2, "Unknown variant", s2, null, false, List.of(), null, null, "not found");
        BulkCompatibilityResponse expected = new BulkCompatibilityResponse(List.of(r1, r2), 2, 1, 1);

        BulkCompatibilityRequest request = new BulkCompatibilityRequest(List.of(
                new BulkCompatibilityRequest.CompatibilityPair(v1, s1),
                new BulkCompatibilityRequest.CompatibilityPair(v2, s2)));

        when(compatibilityService.bulkCheck(request)).thenReturn(expected);

        ResponseEntity<BulkCompatibilityResponse> response = controller.bulkCheck(request);

        assertThat(response.getBody().compatibleCount()).isEqualTo(1);
        assertThat(response.getBody().incompatibleCount()).isEqualTo(1);
    }
}
