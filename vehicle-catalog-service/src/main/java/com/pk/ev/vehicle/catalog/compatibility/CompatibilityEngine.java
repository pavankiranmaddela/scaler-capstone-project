package com.pk.ev.vehicle.catalog.compatibility;

import com.pk.ev.vehicle.catalog.chargingspec.enums.ConnectorType;
import com.pk.ev.vehicle.catalog.chargingspec.model.VehicleChargingSpec;
import com.pk.ev.vehicle.catalog.variantlisting.model.VariantListing;
import org.springframework.stereotype.Component;
import com.pk.ev.vehicle.catalog.compatibility.CompatibilityDtos.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Pure compatibility algorithm — no repository calls, no Spring @Transactional.
 *
 * Takes fully-loaded domain objects and produces CompatibilityResult records.
 * Keeping the algorithm here (not in the service) makes it trivially unit-testable
 * without mocking any persistence layer.
 *
 * Algorithm for vehicle ↔ station check:
 *   1. Collect all VehicleChargingSpecs reachable from the VariantListing
 *      (via ChargingConfiguration).
 *   2. For each spec, find station connectors with the matching ConnectorType.
 *   3. A spec is "matched" if at least one operational station connector shares
 *      the same ConnectorType.
 *   4. maxAchievableWattage = min(spec.maxAcceptedWattage, connector.maxWattage).
 *   5. estimatedCharge10To80Pct is interpolated linearly from the spec's rated
 *      charge time at the vehicle's maxAcceptedWattage, scaled to actual wattage:
 *        estimatedMinutes = ratedMinutes × (maxAccepted / achievable)
 *      If ratedMinutes is null we return null (insufficient data).
 *   6. Best match = highest maxAchievableWattage across all matched specs.
 */
@Component
public class CompatibilityEngine {

    // ─── Vehicle ↔ Station ────────────────────────────────────────────────────

    public CompatibilityDtos.CompatibilityResult checkVariantAgainstStation(
            VariantListing variant,
            UUID stationId,
            List<StationConnector> stationConnectors   // operational connectors at that station
    ) {
        List<VehicleChargingSpec> vehicleSpecs =
                variant.getChargingConfiguration().getChargingSpecs();

        List<MatchedSpec> matches = buildMatches(vehicleSpecs, stationConnectors);

        if (matches.isEmpty()) {
            String reason = buildIncompatibilityReason(vehicleSpecs, stationConnectors);
            return incompatible(variant, stationId, null, reason);
        }

        MatchedSpec best = selectBestMatch(matches);
        return compatible(variant, stationId, null, matches, best);
    }

    // ─── Vehicle ↔ ConnectorType ──────────────────────────────────────────────

    public CompatibilityResult checkVariantAgainstConnectorType(
            VariantListing variant,
            ConnectorType targetConnector,
            Integer stationMaxWattage          // optional; null means "any wattage"
    ) {
        List<VehicleChargingSpec> vehicleSpecs =
                variant.getChargingConfiguration().getChargingSpecs();

        List<MatchedSpec> matches = vehicleSpecs.stream()
                .filter(spec -> spec.getConnectorType() == targetConnector)
                .map(spec -> {
                    int connectorMax  = stationMaxWattage != null
                            ? stationMaxWattage
                            : spec.getMaxAcceptedWattage();   // assume vehicle can go full speed
                    int achievable    = Math.min(spec.getMaxAcceptedWattage(), connectorMax);
                    Integer estimated = interpolateChargeTime(
                            spec.getChargeTime10To80Pct(), spec.getMaxAcceptedWattage(), achievable);

                    return new MatchedSpec(
                            spec.getId(),
                            spec.getConnectorType(),
                            spec.getMaxAcceptedWattage(),
                            connectorMax,
                            achievable,
                            estimated,
                            spec.getCableIncluded()
                    );
                })
                .toList();

        if (matches.isEmpty()) {
            return incompatible(variant, null, targetConnector,
                    "Vehicle does not support connector type " + targetConnector.name());
        }

        MatchedSpec best = selectBestMatch(matches);
        return compatible(variant, null, targetConnector, matches, best);
    }

    // ─── Station → all compatible variants ────────────────────────────────────

    /**
     * Given a list of variants (pre-loaded with specs), returns only those
     * that are compatible with at least one station connector, with their
     * best MatchedSpec.
     */
    public List<CompatibilityResult> findCompatibleVariantsForStation(
            List<VariantListing> allVariants,
            UUID stationId,
            List<StationConnector> stationConnectors
    ) {
        return allVariants.stream()
                .map(v -> checkVariantAgainstStation(v, stationId, stationConnectors))
                .filter(CompatibilityResult::isCompatible)
                .toList();
    }

    // ─── ConnectorType → all compatible variants ──────────────────────────────

    public List<CompatibilityResult> findCompatibleVariantsForConnector(
            List<VariantListing> allVariants,
            ConnectorType targetConnector,
            Integer stationMaxWattage
    ) {
        return allVariants.stream()
                .map(v -> checkVariantAgainstConnectorType(v, targetConnector, stationMaxWattage))
                .filter(CompatibilityResult::isCompatible)
                .toList();
    }

    // ─── Core match-building ──────────────────────────────────────────────────

    private List<MatchedSpec> buildMatches(
            List<VehicleChargingSpec> vehicleSpecs,
            List<StationConnector> stationConnectors
    ) {
        List<MatchedSpec> matches = new ArrayList<>();

        for (VehicleChargingSpec spec : vehicleSpecs) {
            // Find the highest-wattage operational station connector of the same type
            stationConnectors.stream()
                    .filter(sc -> sc.getConnectorType() == spec.getConnectorType()
                            && Boolean.TRUE.equals(sc.getIsOperational()))
                    .max(Comparator.comparingInt(StationConnector::getMaxWattage))
                    .ifPresent(bestConnector -> {
                        int achievable = Math.min(spec.getMaxAcceptedWattage(), bestConnector.getMaxWattage());
                        Integer estimated = interpolateChargeTime(
                                spec.getChargeTime10To80Pct(), spec.getMaxAcceptedWattage(), achievable);

                        matches.add(new MatchedSpec(
                                spec.getId(),
                                spec.getConnectorType(),
                                spec.getMaxAcceptedWattage(),
                                bestConnector.getMaxWattage(),
                                achievable,
                                estimated,
                                spec.getCableIncluded()
                        ));
                    });
        }
        return matches;
    }

    /**
     * Linear interpolation of charge time.
     *
     * If the vehicle spec says "10→80% takes 60 min at 7,200 W" and the
     * achievable wattage is 3,300 W, the estimated time is:
     *   60 × (7200 / 3300) = ~131 min.
     *
     * This is linear approximation — real charge curves are non-linear, but
     * this gives a good-enough estimate without requiring a battery model.
     */
    static Integer interpolateChargeTime(
            Integer ratedMinutes,
            int vehicleMaxWattage,
            int achievableWattage
    ) {
        if (ratedMinutes == null || achievableWattage <= 0) return null;
        if (achievableWattage >= vehicleMaxWattage) return ratedMinutes;
        double scaled = ratedMinutes * ((double) vehicleMaxWattage / achievableWattage);
        return (int) Math.ceil(scaled);
    }

    // ─── Result builders ──────────────────────────────────────────────────────

    private CompatibilityResult compatible(
            VariantListing variant,
            UUID stationId,
            ConnectorType connectorType,
            List<MatchedSpec> matches,
            MatchedSpec best
    ) {
        return new CompatibilityResult(
                variant.getId(),
                variant.getDisplayLabel(),
                stationId,
                connectorType,
                true,
                matches,
                best.maxAchievableWattage(),
                best.estimatedCharge10To80Pct(),
                null
        );
    }

    private CompatibilityResult incompatible(
            VariantListing variant,
            UUID stationId,
            ConnectorType connectorType,
            String reason
    ) {
        return new CompatibilityResult(
                variant.getId(),
                variant.getDisplayLabel(),
                stationId,
                connectorType,
                false,
                List.of(),
                null,
                null,
                reason
        );
    }

    private MatchedSpec selectBestMatch(List<MatchedSpec> matches) {
        return matches.stream()
                .max(Comparator.comparingInt(MatchedSpec::maxAchievableWattage))
                .orElseThrow();
    }

    private String buildIncompatibilityReason(
            List<VehicleChargingSpec> vehicleSpecs,
            List<StationConnector> stationConnectors
    ) {
        if (stationConnectors.isEmpty()) {
            return "No operational connectors found at station";
        }

        // Summarise what the vehicle supports vs what the station has
        String vehicleConnectors = vehicleSpecs.stream()
                .map(s -> s.getConnectorType().name())
                .distinct()
                .sorted()
                .reduce((a, b) -> a + ", " + b)
                .orElse("none");

        String stationConnectorTypes = stationConnectors.stream()
                .map(sc -> sc.getConnectorType().name())
                .distinct()
                .sorted()
                .reduce((a, b) -> a + ", " + b)
                .orElse("none");

        return "No connector type match — vehicle supports [%s], station has [%s]"
                .formatted(vehicleConnectors, stationConnectorTypes);
    }
}
