package com.pk.ev.vehicle.catalog.chargingstandard.mapper;

import com.pk.ev.vehicle.catalog.chargingstandard.dto.ChargingStandardDtos.*;
import com.pk.ev.vehicle.catalog.chargingstandard.enums.ChargingStandardType;
import com.pk.ev.vehicle.catalog.chargingspec.enums.ConnectorType;
import com.pk.ev.vehicle.catalog.chargingstandard.model.ChargingStandard;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class ChargingStandardMapper {

    // ─── Entity → Response ───────────────────────────────────────────────────

    public ChargingStandardResponse toResponse(ChargingStandard std) {
        return new ChargingStandardResponse(
                std.getId(), std.getName(), std.getShortCode(),
                std.getConnectorType(), std.getCurrentType(),
                std.getMaxWattage(), std.getGeographicRegion(),
                std.getGoverningBody(), std.getVersion(),
                std.getDescription(), std.getIconUrl(),
                std.getIsDeprecated(), std.getCreatedAt(), std.getUpdatedAt()
        );
    }

    public ChargingStandardSummary toSummary(ChargingStandard std) {
        return new ChargingStandardSummary(
                std.getId(), std.getName(), std.getShortCode(),
                std.getConnectorType(), std.getCurrentType(),
                std.getMaxWattage(), std.getGeographicRegion(),
                std.getIsDeprecated()
        );
    }

    public PagedStandardsResponse toPagedResponse(Page<ChargingStandard> page) {
        return new PagedStandardsResponse(
                page.getContent().stream().map(this::toSummary).toList(),
                page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages(), page.isLast()
        );
    }

    // ─── Request → Entity ────────────────────────────────────────────────────

    public ChargingStandard toEntity(CreateChargingStandardRequest req) {
        return ChargingStandard.builder()
                .name(req.name())
                .shortCode(req.shortCode().toUpperCase())
                .connectorType(req.connectorType())
                .currentType(req.currentType())
                .maxWattage(req.maxWattage())
                .geographicRegion(req.geographicRegion())
                .governingBody(req.governingBody())
                .version(req.version())
                .description(req.description())
                .iconUrl(req.iconUrl())
                .build();
    }

    public void applyUpdate(UpdateChargingStandardRequest req, ChargingStandard std) {
        if (req.name()              != null) std.setName(req.name());
        if (req.shortCode()         != null) std.setShortCode(req.shortCode().toUpperCase());
        if (req.connectorType()     != null) std.setConnectorType(req.connectorType());
        if (req.currentType()       != null) std.setCurrentType(req.currentType());
        if (req.maxWattage()        != null) std.setMaxWattage(req.maxWattage());
        if (req.geographicRegion()  != null) std.setGeographicRegion(req.geographicRegion());
        if (req.governingBody()     != null) std.setGoverningBody(req.governingBody());
        if (req.version()           != null) std.setVersion(req.version());
        if (req.description()       != null) std.setDescription(req.description());
        if (req.iconUrl()           != null) std.setIconUrl(req.iconUrl());
        if (req.isDeprecated()      != null) std.setIsDeprecated(req.isDeprecated());
    }

    // ─── ConnectorType metadata list ─────────────────────────────────────────
    // Static metadata map — display names, regions, current types per connector
    private static final Map<ConnectorType, ConnectorTypeMetadata> CONNECTOR_META = Map.of(
            ConnectorType.CCS1,        new ConnectorTypeMetadata("CCS1",        "CCS Combo 1",         ChargingStandardType.BOTH,  "USA",          null),
            ConnectorType.CCS2,        new ConnectorTypeMetadata("CCS2",        "CCS Combo 2",         ChargingStandardType.BOTH,  "Europe/India", null),
            ConnectorType.CHAdeMO,     new ConnectorTypeMetadata("CHAdeMO",     "CHAdeMO",             ChargingStandardType.DC,    "Global",       null),
            ConnectorType.TYPE1,       new ConnectorTypeMetadata("TYPE1",       "SAE J1772 Type 1",    ChargingStandardType.AC,    "USA/Japan",    null),
            ConnectorType.TYPE2,       new ConnectorTypeMetadata("TYPE2",       "IEC 62196 Type 2",    ChargingStandardType.AC,    "Europe/India", null),
            ConnectorType.GBT_AC,      new ConnectorTypeMetadata("GBT_AC",      "GB/T 20234.2 AC",     ChargingStandardType.AC,    "China",        null),
            ConnectorType.GBT_DC,      new ConnectorTypeMetadata("GBT_DC",      "GB/T 20234.3 DC",     ChargingStandardType.DC,    "China",        null),
            ConnectorType.TESLA_NACS,  new ConnectorTypeMetadata("TESLA_NACS",  "Tesla NACS / SAE J3400", ChargingStandardType.BOTH, "USA",        null)
    );

    public List<ConnectorTypeMetadata> getAllConnectorTypeMetadata() {
        return Arrays.stream(ConnectorType.values())
                .map(ct -> CONNECTOR_META.getOrDefault(ct,
                        new ConnectorTypeMetadata(ct.name(), ct.name(), ChargingStandardType.AC, "Global", null)))
                .toList();
    }
}
