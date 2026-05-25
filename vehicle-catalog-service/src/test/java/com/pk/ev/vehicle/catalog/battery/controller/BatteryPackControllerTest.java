package com.pk.ev.vehicle.catalog.battery.controller;

import com.pk.ev.vehicle.catalog.battery.service.BatteryPackService;
import com.pk.ev.vehicle.catalog.battery.dtos.BatteryPackDtos.*;
import com.pk.ev.vehicle.catalog.exception.DuplicateResourceException;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BatteryPackControllerTest {

    @Mock
    private BatteryPackService batteryPackService;

    @InjectMocks
    private BatteryPackController batteryPackController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAddBatteryPack() {
        UUID modelId = UUID.randomUUID();
        CreateBatteryPackRequest request = mock(CreateBatteryPackRequest.class);
        BatteryPackResponse response = mock(BatteryPackResponse.class);

        when(batteryPackService.addBatteryPack(eq(modelId), any())).thenReturn(response);

        ResponseEntity<BatteryPackResponse> result = batteryPackController.addBatteryPack(modelId, request);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(response, result.getBody());
    }

    @Test
    void testGetBatteryPacks() {
        UUID modelId = UUID.randomUUID();
        List<BatteryPackResponse> responseList = List.of(mock(BatteryPackResponse.class));

        when(batteryPackService.getBatteryPacks(modelId, true)).thenReturn(responseList);

        ResponseEntity<List<BatteryPackResponse>> result = batteryPackController.getBatteryPacks(modelId, true);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(responseList, result.getBody());
    }

    @Test
    void testAddBatteryPack_DuplicateResource() {
        UUID modelId = UUID.randomUUID();
        CreateBatteryPackRequest request = mock(CreateBatteryPackRequest.class);

        when(batteryPackService.addBatteryPack(eq(modelId), any()))
                .thenThrow(new DuplicateResourceException("Duplicate resource"));

        Exception exception = assertThrows(DuplicateResourceException.class, () ->
                batteryPackController.addBatteryPack(modelId, request));

        assertEquals("Duplicate resource", exception.getMessage());
    }

    @Test
    void testGetBatteryPacks_EmptyList() {
        UUID modelId = UUID.randomUUID();

        when(batteryPackService.getBatteryPacks(modelId, true)).thenReturn(List.of());

        ResponseEntity<List<BatteryPackResponse>> result = batteryPackController.getBatteryPacks(modelId, true);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody().isEmpty());
    }

    @Test
    void testGetBatteryPacks_InvalidModelId() {
        UUID modelId = UUID.randomUUID();

        when(batteryPackService.getBatteryPacks(modelId, true))
                .thenThrow(new ResourceNotFoundException("Model not found"));

        Exception exception = assertThrows(ResourceNotFoundException.class, () ->
                batteryPackController.getBatteryPacks(modelId, true));

        assertEquals("Model not found", exception.getMessage());
    }
}
