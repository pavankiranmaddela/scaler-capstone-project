package com.pk.ev.vehicle.catalog.customer.mapper;

import com.pk.ev.vehicle.catalog.battery.model.BatteryPack;
import com.pk.ev.vehicle.catalog.chargingconfig.model.ChargingConfiguration;
import com.pk.ev.vehicle.catalog.chargingspec.enums.ConnectorType;
import com.pk.ev.vehicle.catalog.chargingspec.enums.CurrentType;
import com.pk.ev.vehicle.catalog.customer.domain.CustomerVehicle;
import com.pk.ev.vehicle.catalog.customer.dto.GarageDtos.*;
import com.pk.ev.vehicle.catalog.modeltrim.enums.VariantStatus;
import com.pk.ev.vehicle.catalog.modeltrim.model.ModelTrim;
import com.pk.ev.vehicle.catalog.variantlisting.model.VariantListing;
import com.pk.ev.vehicle.catalog.vehiclemake.model.VehicleMake;
import com.pk.ev.vehicle.catalog.vehiclemodel.model.VehicleModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GarageMapperTest {

    private GarageMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new GarageMapper();
    }

    // ─── Domain object builders ───────────────────────────────────────────────

    private VariantListing buildVariantListing() {
        VehicleMake make = new VehicleMake();
        make.setId(UUID.randomUUID());
        make.setName("Tata");

        VehicleModel model = new VehicleModel();
        model.setId(UUID.randomUUID());
        model.setName("Tiago EV");
        model.setMake(make);
        model.setModelYear(2024);

        ModelTrim trim = new ModelTrim();
        trim.setId(UUID.randomUUID());
        trim.setTrimName("XZ+");

        BatteryPack battery = BatteryPack.builder()
                .id(UUID.randomUUID())
                .capacityKwh(new BigDecimal("24.0"))
                .rangeKm(315)
                .build();

        ChargingConfiguration config = ChargingConfiguration.builder()
                .id(UUID.randomUUID())
                .onboardChargerKw(new BigDecimal("7.2"))
                .connectorType(ConnectorType.TYPE2)
                .currentType(CurrentType.AC)
                .build();

        return VariantListing.builder()
                .id(UUID.randomUUID())
                .displayLabel("Tiago EV XZ+ 24 kWh 7.2 kW AC")
                .model(model)
                .trim(trim)
                .batteryPack(battery)
                .chargingConfiguration(config)
                .status(VariantStatus.ACTIVE)
                .build();
    }

    private CustomerVehicle buildCustomerVehicle(VariantListing vl, String nickname, boolean isPrimary) {
        return CustomerVehicle.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .variantListing(vl)
                .nickname(nickname)
                .registrationNumber("TS09EF1234")
                .purchaseYear(2023)
                .isPrimary(isPrimary)
                .addedAt(Instant.now())
                .build();
    }

    // ─── toResponse ──────────────────────────────────────────────────────────

    @Test
    void toResponse_mapsAllFields() {
        VariantListing vl = buildVariantListing();
        CustomerVehicle cv = buildCustomerVehicle(vl, "My White Tiago", true);

        CustomerVehicleResponse response = mapper.toResponse(cv);

        assertThat(response.id()).isEqualTo(cv.getId());
        assertThat(response.userId()).isEqualTo(cv.getUserId());
        assertThat(response.nickname()).isEqualTo("My White Tiago");
        assertThat(response.registrationNumber()).isEqualTo("TS09EF1234");
        assertThat(response.purchaseYear()).isEqualTo(2023);
        assertThat(response.isPrimary()).isTrue();
        assertThat(response.addedAt()).isEqualTo(cv.getAddedAt());
    }

    @Test
    void toResponse_mapsVariantSummaryCorrectly() {
        VariantListing vl = buildVariantListing();
        CustomerVehicle cv = buildCustomerVehicle(vl, null, false);

        CustomerVehicleResponse response = mapper.toResponse(cv);
        VariantSummaryInGarage variant = response.variant();

        assertThat(variant.variantListingId()).isEqualTo(vl.getId());
        assertThat(variant.displayLabel()).isEqualTo("Tiago EV XZ+ 24 kWh 7.2 kW AC");
        assertThat(variant.makeName()).isEqualTo("Tata");
        assertThat(variant.modelName()).isEqualTo("Tiago EV");
        assertThat(variant.modelYear()).isEqualTo(2024);
        assertThat(variant.trimName()).isEqualTo("XZ+");
        assertThat(variant.batteryCapacityKwh()).isEqualByComparingTo("24.0");
        assertThat(variant.rangeKm()).isEqualTo(315);
        assertThat(variant.onboardChargerKw()).isEqualByComparingTo("7.2");
        assertThat(variant.connectorType()).isEqualTo(ConnectorType.TYPE2);
        assertThat(variant.variantStatus()).isEqualTo(VariantStatus.ACTIVE);
    }

    // ─── toSummary ────────────────────────────────────────────────────────────

    @Test
    void toSummary_usesNickname_whenNicknameIsSet() {
        VariantListing vl = buildVariantListing();
        CustomerVehicle cv = buildCustomerVehicle(vl, "Office Car", false);

        CustomerVehicleSummary summary = mapper.toSummary(cv);

        assertThat(summary.displayLabel()).isEqualTo("Office Car");
    }

    @Test
    void toSummary_usesVariantDisplayLabel_whenNicknameIsNull() {
        VariantListing vl = buildVariantListing();
        CustomerVehicle cv = buildCustomerVehicle(vl, null, false);

        CustomerVehicleSummary summary = mapper.toSummary(cv);

        assertThat(summary.displayLabel()).isEqualTo("Tiago EV XZ+ 24 kWh 7.2 kW AC");
    }

    @Test
    void toSummary_usesVariantDisplayLabel_whenNicknameIsBlank() {
        VariantListing vl = buildVariantListing();
        CustomerVehicle cv = buildCustomerVehicle(vl, "   ", false);

        CustomerVehicleSummary summary = mapper.toSummary(cv);

        assertThat(summary.displayLabel()).isEqualTo("Tiago EV XZ+ 24 kWh 7.2 kW AC");
    }

    @Test
    void toSummary_mapsAllFields() {
        VariantListing vl = buildVariantListing();
        CustomerVehicle cv = buildCustomerVehicle(vl, null, true);

        CustomerVehicleSummary summary = mapper.toSummary(cv);

        assertThat(summary.id()).isEqualTo(cv.getId());
        assertThat(summary.makeName()).isEqualTo("Tata");
        assertThat(summary.modelName()).isEqualTo("Tiago EV");
        assertThat(summary.modelYear()).isEqualTo(2024);
        assertThat(summary.isPrimary()).isTrue();
        assertThat(summary.onboardChargerKw()).isEqualByComparingTo("7.2");
        assertThat(summary.connectorType()).isEqualTo(ConnectorType.TYPE2);
        assertThat(summary.addedAt()).isEqualTo(cv.getAddedAt());
    }

    // ─── toGarageResponse ────────────────────────────────────────────────────

    @Test
    void toGarageResponse_setsCorrectTotalCount() {
        VariantListing vl = buildVariantListing();
        CustomerVehicle cv1 = buildCustomerVehicle(vl, null, true);
        CustomerVehicle cv2 = buildCustomerVehicle(vl, "Office Car", false);

        GarageResponse response = mapper.toGarageResponse(List.of(cv1, cv2));

        assertThat(response.totalCount()).isEqualTo(2);
        assertThat(response.vehicles()).hasSize(2);
    }

    @Test
    void toGarageResponse_setsPrimaryVehicleId() {
        VariantListing vl = buildVariantListing();
        CustomerVehicle primary = buildCustomerVehicle(vl, "My Primary", true);
        CustomerVehicle other   = buildCustomerVehicle(vl, null, false);

        GarageResponse response = mapper.toGarageResponse(List.of(primary, other));

        assertThat(response.primaryVehicleId()).isEqualTo(primary.getId());
    }

    @Test
    void toGarageResponse_primaryVehicleIdIsNull_whenNoPrimarySet() {
        VariantListing vl = buildVariantListing();
        CustomerVehicle cv1 = buildCustomerVehicle(vl, null, false);
        CustomerVehicle cv2 = buildCustomerVehicle(vl, null, false);

        GarageResponse response = mapper.toGarageResponse(List.of(cv1, cv2));

        assertThat(response.primaryVehicleId()).isNull();
    }

    @Test
    void toGarageResponse_emptyList_returnsZeroCount() {
        GarageResponse response = mapper.toGarageResponse(List.of());

        assertThat(response.totalCount()).isZero();
        assertThat(response.vehicles()).isEmpty();
        assertThat(response.primaryVehicleId()).isNull();
    }

    // ─── toAdminPageResponse ─────────────────────────────────────────────────

    @Test
    void toAdminPageResponse_mapsPageMetadata() {
        VariantListing vl = buildVariantListing();
        CustomerVehicle cv = buildCustomerVehicle(vl, "Test", false);
        Page<CustomerVehicle> page = new PageImpl<>(
                List.of(cv), PageRequest.of(1, 10), 25);

        AdminGaragePageResponse response = mapper.toAdminPageResponse(page);

        assertThat(response.page()).isEqualTo(1);
        assertThat(response.size()).isEqualTo(10);
        assertThat(response.totalElements()).isEqualTo(25);
        assertThat(response.totalPages()).isEqualTo(3);
        assertThat(response.last()).isFalse();
        assertThat(response.content()).hasSize(1);
    }

    @Test
    void toAdminPageResponse_emptyPage() {
        Page<CustomerVehicle> page = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);

        AdminGaragePageResponse response = mapper.toAdminPageResponse(page);

        assertThat(response.totalElements()).isZero();
        assertThat(response.content()).isEmpty();
        assertThat(response.last()).isTrue();
    }
}
