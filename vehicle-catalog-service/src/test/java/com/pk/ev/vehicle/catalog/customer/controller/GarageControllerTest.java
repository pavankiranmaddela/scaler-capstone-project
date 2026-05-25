package com.pk.ev.vehicle.catalog.customer.controller;

import com.pk.ev.vehicle.catalog.customer.dto.GarageDtos.*;
import com.pk.ev.vehicle.catalog.customer.service.GarageService;
import com.pk.ev.vehicle.catalog.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class GarageControllerTest {

    @Mock
    private GarageService garageService;

    @InjectMocks
    private GarageController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private CustomerVehicleResponse fakeVehicleResponse(UUID id) {
        return new CustomerVehicleResponse(id, UUID.randomUUID(), null, "My EV",
                "TS09EF1234", 2023, false, null);
    }

    private GarageResponse fakeGarageResponse() {
        return new GarageResponse(List.of(), 0, null);
    }

    // ─── addVehicle ──────────────────────────────────────────────────────────

    @Test
    void addVehicle_returns201WithBody() {
        UUID userId = UUID.randomUUID();
        UUID variantId = UUID.randomUUID();
        AddVehicleRequest request = new AddVehicleRequest(variantId, "My EV", null, null, false);
        UUID newId = UUID.randomUUID();
        CustomerVehicleResponse expected = fakeVehicleResponse(newId);

        when(garageService.addVehicle(eq(userId), eq(request))).thenReturn(expected);

        ResponseEntity<CustomerVehicleResponse> response =
                controller.addVehicle(userId.toString(), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(expected);
        verify(garageService).addVehicle(userId, request);
    }

    @Test
    void addVehicle_propagatesResourceNotFound_whenVariantMissing() {
        UUID userId = UUID.randomUUID();
        UUID variantId = UUID.randomUUID();
        AddVehicleRequest request = new AddVehicleRequest(variantId, null, null, null, null);

        when(garageService.addVehicle(eq(userId), any()))
                .thenThrow(new ResourceNotFoundException("VariantListing not found: " + variantId));

        assertThatThrownBy(() -> controller.addVehicle(userId.toString(), request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(variantId.toString());
    }

    // ─── getGarage ───────────────────────────────────────────────────────────

    @Test
    void getGarage_returns200WithGarageResponse() {
        UUID userId = UUID.randomUUID();
        GarageResponse expected = new GarageResponse(List.of(), 0, null);

        when(garageService.getGarage(userId)).thenReturn(expected);

        ResponseEntity<GarageResponse> response = controller.getGarage(userId.toString());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expected);
        verify(garageService).getGarage(userId);
    }

    @Test
    void getGarage_returnsPrimaryVehicleId_whenSet() {
        UUID userId = UUID.randomUUID();
        UUID primaryId = UUID.randomUUID();
        GarageResponse expected = new GarageResponse(List.of(), 0, primaryId);

        when(garageService.getGarage(userId)).thenReturn(expected);

        ResponseEntity<GarageResponse> response = controller.getGarage(userId.toString());

        assertThat(response.getBody().primaryVehicleId()).isEqualTo(primaryId);
    }

    // ─── getVehicleById ───────────────────────────────────────────────────────

    @Test
    void getVehicleById_returns200_whenFound() {
        UUID userId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();
        CustomerVehicleResponse expected = fakeVehicleResponse(vehicleId);

        when(garageService.getVehicleById(userId, vehicleId)).thenReturn(expected);

        ResponseEntity<CustomerVehicleResponse> response =
                controller.getVehicleById(userId.toString(), vehicleId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expected);
    }

    @Test
    void getVehicleById_propagatesNotFound_whenNotOwned() {
        UUID userId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();

        when(garageService.getVehicleById(userId, vehicleId))
                .thenThrow(new ResourceNotFoundException("Not found: " + vehicleId));

        assertThatThrownBy(() -> controller.getVehicleById(userId.toString(), vehicleId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(vehicleId.toString());
    }

    // ─── updateVehicle ────────────────────────────────────────────────────────

    @Test
    void updateVehicle_returns200WithUpdatedResponse() {
        UUID userId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();
        UpdateVehicleRequest request = new UpdateVehicleRequest("New Name", null, null, null);
        CustomerVehicleResponse expected = fakeVehicleResponse(vehicleId);

        when(garageService.updateVehicle(userId, vehicleId, request)).thenReturn(expected);

        ResponseEntity<CustomerVehicleResponse> response =
                controller.updateVehicle(userId.toString(), vehicleId, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expected);
        verify(garageService).updateVehicle(userId, vehicleId, request);
    }

    @Test
    void updateVehicle_propagatesNotFound_whenVehicleMissing() {
        UUID userId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();
        UpdateVehicleRequest request = new UpdateVehicleRequest(null, null, null, null);

        when(garageService.updateVehicle(userId, vehicleId, request))
                .thenThrow(new ResourceNotFoundException("Not found: " + vehicleId));

        assertThatThrownBy(() -> controller.updateVehicle(userId.toString(), vehicleId, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ─── removeVehicle ────────────────────────────────────────────────────────

    @Test
    void removeVehicle_returns204NoContent() {
        UUID userId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();
        doNothing().when(garageService).removeVehicle(userId, vehicleId);

        ResponseEntity<Void> response = controller.removeVehicle(userId.toString(), vehicleId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
        verify(garageService).removeVehicle(userId, vehicleId);
    }

    @Test
    void removeVehicle_propagatesNotFound() {
        UUID userId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();

        doThrow(new ResourceNotFoundException("Not found: " + vehicleId))
                .when(garageService).removeVehicle(userId, vehicleId);

        assertThatThrownBy(() -> controller.removeVehicle(userId.toString(), vehicleId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ─── setPrimary ───────────────────────────────────────────────────────────

    @Test
    void setPrimary_returns200WithResponse() {
        UUID userId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();
        CustomerVehicleResponse expected = new CustomerVehicleResponse(
                vehicleId, userId, null, null, null, null, true, null);

        when(garageService.setPrimaryVehicle(userId, vehicleId)).thenReturn(expected);

        ResponseEntity<CustomerVehicleResponse> response =
                controller.setPrimary(userId.toString(), vehicleId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().isPrimary()).isTrue();
        verify(garageService).setPrimaryVehicle(userId, vehicleId);
    }

    @Test
    void setPrimary_isIdempotent_returnsOkEvenIfAlreadyPrimary() {
        UUID userId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();
        CustomerVehicleResponse expected = new CustomerVehicleResponse(
                vehicleId, userId, null, null, null, null, true, null);

        when(garageService.setPrimaryVehicle(userId, vehicleId)).thenReturn(expected);

        ResponseEntity<CustomerVehicleResponse> response =
                controller.setPrimary(userId.toString(), vehicleId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void setPrimary_propagatesNotFound() {
        UUID userId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();

        when(garageService.setPrimaryVehicle(userId, vehicleId))
                .thenThrow(new ResourceNotFoundException("Not found: " + vehicleId));

        assertThatThrownBy(() -> controller.setPrimary(userId.toString(), vehicleId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ─── getCompatibleStations ────────────────────────────────────────────────

    @Test
    void getCompatibleStations_returns200WithStations() {
        UUID userId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();
        CompatibleStationsResponse expected = new CompatibleStationsResponse(
                vehicleId, "My EV", true, List.of(), 0);

        when(garageService.getCompatibleStations(userId, vehicleId)).thenReturn(expected);

        ResponseEntity<CompatibleStationsResponse> response =
                controller.getCompatibleStations(userId.toString(), vehicleId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expected);
        verify(garageService).getCompatibleStations(userId, vehicleId);
    }

    @Test
    void getCompatibleStations_propagatesNotFound() {
        UUID userId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();

        when(garageService.getCompatibleStations(userId, vehicleId))
                .thenThrow(new ResourceNotFoundException("Not found: " + vehicleId));

        assertThatThrownBy(() -> controller.getCompatibleStations(userId.toString(), vehicleId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ─── adminListVehicles ────────────────────────────────────────────────────

    @Test
    void adminListVehicles_returns200WithDefaultPagination() {
        AdminGaragePageResponse expected = new AdminGaragePageResponse(List.of(), 0, 20, 0, 0, true);

        when(garageService.adminListVehicles(
                eq(null),
                eq(PageRequest.of(0, 20, Sort.by("addedAt").descending()))))
                .thenReturn(expected);

        ResponseEntity<AdminGaragePageResponse> response =
                controller.adminListVehicles(null, 0, 20, "addedAt");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expected);
    }

    @Test
    void adminListVehicles_withUserIdFilter_passesFilterToService() {
        UUID filterUserId = UUID.randomUUID();
        AdminGaragePageResponse expected = new AdminGaragePageResponse(List.of(), 0, 10, 0, 0, true);

        when(garageService.adminListVehicles(
                eq(filterUserId),
                eq(PageRequest.of(0, 10, Sort.by("addedAt").descending()))))
                .thenReturn(expected);

        ResponseEntity<AdminGaragePageResponse> response =
                controller.adminListVehicles(filterUserId, 0, 10, "addedAt");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(garageService).adminListVehicles(
                eq(filterUserId),
                eq(PageRequest.of(0, 10, Sort.by("addedAt").descending())));
    }
}
