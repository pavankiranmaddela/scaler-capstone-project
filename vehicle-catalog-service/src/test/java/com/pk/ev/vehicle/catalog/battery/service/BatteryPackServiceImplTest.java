package com.pk.ev.vehicle.catalog.battery.service;

import com.pk.ev.vehicle.catalog.battery.mapper.BatteryPackMapper;
import com.pk.ev.vehicle.catalog.battery.model.BatteryPack;
import com.pk.ev.vehicle.catalog.battery.repository.BatteryPackRepository;
import com.pk.ev.vehicle.catalog.exception.DuplicateResourceException;
import com.pk.ev.vehicle.catalog.exception.ResourceNotFoundException;
import com.pk.ev.vehicle.catalog.vehiclemodel.model.VehicleModel;
import com.pk.ev.vehicle.catalog.vehiclemodel.service.VehicleModelService;
import com.pk.ev.vehicle.catalog.battery.dtos.BatteryPackDtos.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BatteryPackServiceImplTest {

    @Mock
    private BatteryPackRepository batteryPackRepository;

    @Mock
    private VehicleModelService modelService;

    @Mock
    private BatteryPackMapper batteryPackMapper;

    @InjectMocks
    private BatteryPackServiceImpl batteryPackService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAddBatteryPack() {
        UUID modelId = UUID.randomUUID();
        CreateBatteryPackRequest request = mock(CreateBatteryPackRequest.class);
        BatteryPack batteryPack = mock(BatteryPack.class);
        BatteryPackResponse response = mock(BatteryPackResponse.class);

        when(modelService.findModelOrThrow(modelId)).thenReturn(mock(VehicleModel.class));
        when(batteryPackRepository.existsByModelIdAndCapacityKwh(eq(modelId), any())).thenReturn(false);
        when(batteryPackMapper.toBatteryPackEntity(any(), any())).thenReturn(batteryPack);
        when(batteryPackRepository.save(batteryPack)).thenReturn(batteryPack);
        when(batteryPackMapper.toBatteryPackResponse(batteryPack)).thenReturn(response);

        BatteryPackResponse result = batteryPackService.addBatteryPack(modelId, request);

        assertEquals(response, result);
    }

    @Test
    void testGetBatteryPacks() {
        UUID modelId = UUID.randomUUID();
        BatteryPack batteryPack = mock(BatteryPack.class);
        BatteryPackResponse batteryPackResponse = mock(BatteryPackResponse.class);

        when(modelService.findModelOrThrow(modelId)).thenReturn(mock(VehicleModel.class));
        when(batteryPackRepository.findByModelIdAndIsActiveTrue(modelId)).thenReturn(List.of(batteryPack));
        when(batteryPackMapper.toBatteryPackResponse(batteryPack)).thenReturn(batteryPackResponse);

        List<BatteryPackResponse> result = batteryPackService.getBatteryPacks(modelId, true);

        assertEquals(1, result.size());
        assertEquals(batteryPackResponse, result.get(0));
    }
}
