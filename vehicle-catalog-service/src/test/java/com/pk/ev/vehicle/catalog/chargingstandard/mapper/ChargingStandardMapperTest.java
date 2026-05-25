package com.pk.ev.vehicle.catalog.chargingstandard.mapper;

import com.pk.ev.vehicle.catalog.chargingstandard.dto.ChargingStandardDtos.*;
import com.pk.ev.vehicle.catalog.chargingstandard.enums.ChargingStandardType;
import com.pk.ev.vehicle.catalog.chargingstandard.model.ChargingStandard;
import com.pk.ev.vehicle.catalog.chargingspec.enums.ConnectorType;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ChargingStandardMapperTest {

    private final ChargingStandardMapper mapper = new ChargingStandardMapper();

    @Test
    void toResponse_mapsAllFields() {
        ChargingStandard std = ChargingStandard.builder()
                .id(UUID.randomUUID())
                .name("CCS Combo 2")
                .shortCode("CCS2")
                .connectorType(ConnectorType.CCS2)
                .currentType(ChargingStandardType.BOTH)
                .maxWattage(350000)
                .geographicRegion("Europe/India")
                .governingBody("IEC")
                .version("2.0")
                .description("DC fast charging")
                .iconUrl("https://example.com/ccs2.svg")
                .isDeprecated(false)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        ChargingStandardResponse resp = mapper.toResponse(std);

        assertThat(resp.id()).isEqualTo(std.getId());
        assertThat(resp.name()).isEqualTo("CCS Combo 2");
        assertThat(resp.shortCode()).isEqualTo("CCS2");
        assertThat(resp.connectorType()).isEqualTo(ConnectorType.CCS2);
        assertThat(resp.currentType()).isEqualTo(ChargingStandardType.BOTH);
        assertThat(resp.maxWattage()).isEqualTo(350000);
        assertThat(resp.geographicRegion()).isEqualTo("Europe/India");
        assertThat(resp.governingBody()).isEqualTo("IEC");
        assertThat(resp.version()).isEqualTo("2.0");
        assertThat(resp.description()).isEqualTo("DC fast charging");
        assertThat(resp.iconUrl()).isEqualTo("https://example.com/ccs2.svg");
        assertThat(resp.isDeprecated()).isFalse();
    }

    @Test
    void toSummary_returnsSubsetOfFields() {
        ChargingStandard std = ChargingStandard.builder()
                .id(UUID.randomUUID())
                .name("Type 2")
                .shortCode("TYPE2")
                .connectorType(ConnectorType.TYPE2)
                .currentType(ChargingStandardType.AC)
                .maxWattage(22000)
                .geographicRegion("Europe")
                .isDeprecated(false)
                .build();

        ChargingStandardSummary summary = mapper.toSummary(std);

        assertThat(summary.id()).isEqualTo(std.getId());
        assertThat(summary.name()).isEqualTo("Type 2");
        assertThat(summary.shortCode()).isEqualTo("TYPE2");
        assertThat(summary.connectorType()).isEqualTo(ConnectorType.TYPE2);
        assertThat(summary.currentType()).isEqualTo(ChargingStandardType.AC);
        assertThat(summary.maxWattage()).isEqualTo(22000);
        assertThat(summary.geographicRegion()).isEqualTo("Europe");
        assertThat(summary.isDeprecated()).isFalse();
    }

    @Test
    void toPagedResponse_convertsPageCorrectly() {
        ChargingStandard std1 = ChargingStandard.builder()
                .id(UUID.randomUUID())
                .name("CCS2")
                .shortCode("CCS2")
                .connectorType(ConnectorType.CCS2)
                .currentType(ChargingStandardType.BOTH)
                .maxWattage(350000)
                .isDeprecated(false)
                .build();

        Page<ChargingStandard> page = new PageImpl<>(List.of(std1), PageRequest.of(0, 50), 1);

        PagedStandardsResponse paged = mapper.toPagedResponse(page);

        assertThat(paged.content()).hasSize(1);
        assertThat(paged.page()).isEqualTo(0);
        assertThat(paged.size()).isEqualTo(50);
        assertThat(paged.totalElements()).isEqualTo(1);
        assertThat(paged.totalPages()).isEqualTo(1);
        assertThat(paged.last()).isTrue();
    }

    @Test
    void toEntity_createsFromRequest() {
        CreateChargingStandardRequest req = new CreateChargingStandardRequest(
                "CCS Combo 2",
                "ccs2",
                ConnectorType.CCS2,
                ChargingStandardType.BOTH,
                350000,
                "Europe/India",
                "IEC",
                "2.0",
                "Fast DC",
                "https://example.com/ccs2.svg"
        );

        ChargingStandard entity = mapper.toEntity(req);

        assertThat(entity.getName()).isEqualTo("CCS Combo 2");
        assertThat(entity.getShortCode()).isEqualTo("CCS2"); // uppercased
        assertThat(entity.getConnectorType()).isEqualTo(ConnectorType.CCS2);
        assertThat(entity.getCurrentType()).isEqualTo(ChargingStandardType.BOTH);
        assertThat(entity.getMaxWattage()).isEqualTo(350000);
        assertThat(entity.getGeographicRegion()).isEqualTo("Europe/India");
        assertThat(entity.getGoverningBody()).isEqualTo("IEC");
        assertThat(entity.getVersion()).isEqualTo("2.0");
        assertThat(entity.getDescription()).isEqualTo("Fast DC");
        assertThat(entity.getIconUrl()).isEqualTo("https://example.com/ccs2.svg");
    }

    @Test
    void toEntity_uppercasesShortCode() {
        CreateChargingStandardRequest req = new CreateChargingStandardRequest(
                "Type 2", "type2", ConnectorType.TYPE2, ChargingStandardType.AC, 22000, null, null, null, null, null
        );

        ChargingStandard entity = mapper.toEntity(req);

        assertThat(entity.getShortCode()).isEqualTo("TYPE2");
    }

    @Test
    void applyUpdate_updatesOnlyProvidedFields() {
        ChargingStandard std = ChargingStandard.builder()
                .name("Old Name")
                .shortCode("OLD")
                .maxWattage(100000)
                .version("1.0")
                .isDeprecated(false)
                .build();

        UpdateChargingStandardRequest req = new UpdateChargingStandardRequest(
                "New Name",
                "new",
                null,
                null,
                150000,
                null,
                null,
                "2.0",
                null,
                null,
                true
        );

        mapper.applyUpdate(req, std);

        assertThat(std.getName()).isEqualTo("New Name");
        assertThat(std.getShortCode()).isEqualTo("NEW"); // uppercased
        assertThat(std.getMaxWattage()).isEqualTo(150000);
        assertThat(std.getVersion()).isEqualTo("2.0");
        assertThat(std.getIsDeprecated()).isTrue();
    }

    @Test
    void applyUpdate_ignoresNullValues() {
        ChargingStandard std = ChargingStandard.builder()
                .name("Name")
                .maxWattage(100000)
                .isDeprecated(false)
                .build();

        UpdateChargingStandardRequest req = new UpdateChargingStandardRequest(
                null, null, null, null, null, null, null, null, null, null, null
        );

        mapper.applyUpdate(req, std);

        assertThat(std.getName()).isEqualTo("Name");
        assertThat(std.getMaxWattage()).isEqualTo(100000);
        assertThat(std.getIsDeprecated()).isFalse();
    }

    @Test
    void getAllConnectorTypeMetadata_returnsAllConnectorTypes() {
        List<ConnectorTypeMetadata> metadata = mapper.getAllConnectorTypeMetadata();

        assertThat(metadata).isNotEmpty();
        assertThat(metadata.stream().map(ConnectorTypeMetadata::code))
                .contains("CCS1", "CCS2", "CHAdeMO", "TYPE1", "TYPE2", "GBT_AC", "GBT_DC", "TESLA_NACS");
    }

    @Test
    void getAllConnectorTypeMetadata_hasCorrectMetadata() {
        List<ConnectorTypeMetadata> metadata = mapper.getAllConnectorTypeMetadata();

        ConnectorTypeMetadata ccs2 = metadata.stream()
                .filter(m -> m.code().equals("CCS2"))
                .findFirst()
                .orElseThrow();

        assertThat(ccs2.displayName()).isEqualTo("CCS Combo 2");
        assertThat(ccs2.currentType()).isEqualTo(ChargingStandardType.BOTH);
        assertThat(ccs2.primaryRegion()).isEqualTo("Europe/India");
    }
}

