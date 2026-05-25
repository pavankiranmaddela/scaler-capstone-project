package com.pk.ev.vehicle.catalog.battery.mapper;

import com.pk.ev.vehicle.catalog.battery.model.BatteryPack;
import com.pk.ev.vehicle.catalog.battery.dtos.BatteryPackDtos.*;
import com.pk.ev.vehicle.catalog.battery.enums.BatteryChemistry;
import com.pk.ev.vehicle.catalog.vehiclemodel.model.VehicleModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static com.pk.ev.vehicle.catalog.mocks.CommonMocks.getVehicleModelMock;
import static org.junit.jupiter.api.Assertions.*;

class BatteryPackMapperTest {

    private BatteryPackMapper batteryPackMapper;

    @BeforeEach
    void setUp() {
        batteryPackMapper = new BatteryPackMapper();
    }

    @Test
    void testToBatteryPackResponse() {
        BatteryPack batteryPack = BatteryPack.builder()
                .id(UUID.randomUUID())
                .packName("Test Pack")
                .capacityKwh(BigDecimal.valueOf(50))
                .model(getVehicleModelMock())
                .build();

        BatteryPackResponse response = batteryPackMapper.toBatteryPackResponse(batteryPack);

        assertEquals(batteryPack.getId(), response.id());
        assertEquals(batteryPack.getPackName(), response.packName());
    }

    @Test
    void testApplyBatteryPackUpdate() {
        BatteryPack batteryPack = BatteryPack.builder()
                .packName("Old Pack")
                .capacityKwh(BigDecimal.valueOf(50))
                .usableKwh(BigDecimal.valueOf(45))
                .rangeKm(300)
                .chemistry(BatteryChemistry.LITHIUM_ION)
                .cellsConfiguration("3P6S")
                .warrantyYears(5)
                .warrantyKm(100000)
                .isActive(true)
                .build();

        UpdateBatteryPackRequest updateRequest = new UpdateBatteryPackRequest(
                "Updated Pack", BigDecimal.valueOf(60), BigDecimal.valueOf(55),
                350, BatteryChemistry.SOLID_STATE, "4P8S", 6, 120000, false
        );

        batteryPackMapper.applyBatteryPackUpdate(updateRequest, batteryPack);

        assertEquals("Updated Pack", batteryPack.getPackName());
        assertEquals(BigDecimal.valueOf(60), batteryPack.getCapacityKwh());
        assertEquals(BigDecimal.valueOf(55), batteryPack.getUsableKwh());
        assertEquals(350, batteryPack.getRangeKm());
        assertEquals(BatteryChemistry.SOLID_STATE, batteryPack.getChemistry());
        assertEquals("4P8S", batteryPack.getCellsConfiguration());
        assertEquals(6, batteryPack.getWarrantyYears());
        assertEquals(120000, batteryPack.getWarrantyKm());
        assertFalse(batteryPack.getIsActive());
    }

    @Test
    void testApplyBatteryPackUpdate_PartialUpdate() {
        BatteryPack batteryPack = BatteryPack.builder()
                .packName("Old Pack")
                .capacityKwh(BigDecimal.valueOf(50))
                .usableKwh(BigDecimal.valueOf(45))
                .rangeKm(300)
                .chemistry(BatteryChemistry.LITHIUM_ION)
                .cellsConfiguration("3P6S")
                .warrantyYears(5)
                .warrantyKm(100000)
                .isActive(true)
                .build();

        UpdateBatteryPackRequest updateRequest = new UpdateBatteryPackRequest(
                null, null, BigDecimal.valueOf(50),
                null, null, null, null, null, null
        );

        batteryPackMapper.applyBatteryPackUpdate(updateRequest, batteryPack);

        assertEquals("Old Pack", batteryPack.getPackName());
        assertEquals(BigDecimal.valueOf(50), batteryPack.getCapacityKwh());
        assertEquals(BigDecimal.valueOf(50), batteryPack.getUsableKwh());
        assertEquals(300, batteryPack.getRangeKm());
        assertEquals(BatteryChemistry.LITHIUM_ION, batteryPack.getChemistry());
        assertEquals("3P6S", batteryPack.getCellsConfiguration());
        assertEquals(5, batteryPack.getWarrantyYears());
        assertEquals(100000, batteryPack.getWarrantyKm());
        assertTrue(batteryPack.getIsActive());
    }

    @Test
    void testToBatteryPackEntity_NullFields() {
        CreateBatteryPackRequest request = new CreateBatteryPackRequest(null, null, null, null, null, null, null, null);
        VehicleModel model = getVehicleModelMock();

        BatteryPack batteryPack = batteryPackMapper.toBatteryPackEntity(request, model);

        assertNull(batteryPack.getPackName());
        assertNull(batteryPack.getCapacityKwh());
        assertNull(batteryPack.getUsableKwh());
        assertNull(batteryPack.getRangeKm());
        assertNull(batteryPack.getChemistry());
        assertNull(batteryPack.getCellsConfiguration());
        assertNull(batteryPack.getWarrantyYears());
        assertNull(batteryPack.getWarrantyKm());
        assertEquals(model, batteryPack.getModel());
    }
}
