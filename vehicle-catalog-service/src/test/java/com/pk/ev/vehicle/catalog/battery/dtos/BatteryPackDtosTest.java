package com.pk.ev.vehicle.catalog.battery.dtos;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BatteryPackDtosTest {

    @Test
    void testCreateBatteryPackRequest() {
        BatteryPackDtos.CreateBatteryPackRequest request = new BatteryPackDtos.CreateBatteryPackRequest(
                "Test Pack", null, null, 100, null, null, null, null
        );

        assertEquals("Test Pack", request.packName());
        assertEquals(100, request.rangeKm());
    }
}
