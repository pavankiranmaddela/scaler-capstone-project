-- Flyway migration: V1__seed_charging_standards.sql
-- Seeds the ChargingStandard reference catalog with all protocols the platform supports.
-- This is the ONLY place these records are created — do not create them via API in production seeding scripts.
-- All wattages are in Watts (W). Max wattages represent protocol-defined theoretical ceilings.

INSERT INTO charging_standards (name, short_code, connector_type, current_type,
    max_wattage, geographic_region, governing_body,
    version, description, icon_url, is_deprecated,
    created_at, updated_at
) VALUES

-- ─── IEC / European Standards ─────────────────────────────────────────────

('IEC 62196 Type 2 AC',      'TYPE2',   'TYPE2',   'AC',
 43500,  'Europe/India', 'IEC',  '2014',
 'Standard AC connector for European and Indian EVs. 1-phase or 3-phase up to 43.5 kW.',
 NULL, false, NOW(), NOW()),

('CCS Combo 2',               'CCS2',    'CCS2',    'BOTH',
 350000, 'Europe/India', 'IEC',  '2014',
 'Combined Charging System — TYPE2 AC + DC pins in one plug. DC up to 350 kW. Dominant standard in Europe and India.',
 NULL, false, NOW(), NOW()),