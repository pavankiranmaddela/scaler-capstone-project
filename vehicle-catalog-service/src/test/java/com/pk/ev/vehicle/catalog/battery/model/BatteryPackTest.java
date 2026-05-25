package com.pk.ev.vehicle.catalog.battery.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class BatteryPackTest {

    @Test
    void testBatteryPackBuilder() {
        BatteryPack batteryPack = BatteryPack.builder()
                .packName("Test Pack")
                .capacityKwh(BigDecimal.valueOf(50))
                .build();

        assertEquals("Test Pack", batteryPack.getPackName());
        assertEquals(BigDecimal.valueOf(50), batteryPack.getCapacityKwh());
    }
}
