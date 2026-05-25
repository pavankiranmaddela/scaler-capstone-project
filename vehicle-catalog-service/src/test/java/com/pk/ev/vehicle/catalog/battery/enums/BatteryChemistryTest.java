package com.pk.ev.vehicle.catalog.battery.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BatteryChemistryTest {

    @Test
    void testEnumValues() {
        assertEquals(4, BatteryChemistry.values().length);
        assertNotNull(BatteryChemistry.valueOf("LITHIUM_ION"));
    }
}
