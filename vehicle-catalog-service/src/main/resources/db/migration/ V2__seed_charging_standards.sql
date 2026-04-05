-- Flyway migration: V2__seed_charging_standards.sql
-- Seeds the ChargingStandard reference catalog with all protocols the platform supports.
-- This is the ONLY place these records are created — do not create them via API in production seeding scripts.
-- All wattages are in Watts (W). Max wattages represent protocol-defined theoretical ceilings.

INSERT INTO charging_standards (
    id, name, short_code, connector_type, current_type,
    max_wattage, geographic_region, governing_body,
    version, description, icon_url, is_deprecated,
    created_at, updated_at
) VALUES

-- ─── IEC / European Standards ─────────────────────────────────────────────

(gen_random_uuid(), 'IEC 62196 Type 2 AC',      'TYPE2',   'TYPE2',   'AC',
 43500,  'Europe/India', 'IEC',  '2014',
 'Standard AC connector for European and Indian EVs. 1-phase or 3-phase up to 43.5 kW.',
 NULL, false, NOW(), NOW()),

(gen_random_uuid(), 'CCS Combo 2',               'CCS2',    'CCS2',    'BOTH',
 350000, 'Europe/India', 'IEC',  '2014',
 'Combined Charging System — TYPE2 AC + DC pins in one plug. DC up to 350 kW. Dominant standard in Europe and India.',
 NULL, false, NOW(), NOW()),

-- ─── SAE / North American Standards ──────────────────────────────────────

(gen_random_uuid(), 'SAE J1772 Type 1 AC',       'TYPE1',   'TYPE1',   'AC',
 19200,  'USA/Japan',    'SAE',  'J1772',
 'Standard AC connector for North America and Japan. Single-phase up to 19.2 kW.',
 NULL, false, NOW(), NOW()),

(gen_random_uuid(), 'CCS Combo 1',                'CCS1',    'CCS1',    'BOTH',
 350000, 'USA',          'SAE',  'J1772',
 'Combined Charging System with J1772 AC + DC pins. DC up to 350 kW. Used in North America.',
 NULL, false, NOW(), NOW()),

(gen_random_uuid(), 'Tesla NACS / SAE J3400',     'TESLA_NACS', 'TESLA_NACS', 'BOTH',
 250000, 'USA',          'SAE',  'J3400',
 'North American Charging Standard — originally Tesla proprietary, adopted as SAE J3400 in 2023. Up to 250 kW DC.',
 NULL, false, NOW(), NOW()),

-- ─── CHAdeMO ─────────────────────────────────────────────────────────────

(gen_random_uuid(), 'CHAdeMO 2.0',                'CHAdeMO', 'CHAdeMO', 'DC',
 400000, 'Global',       'CHAdeMO Association', '2.0',
 'DC fast-charge protocol developed in Japan. Legacy standard being phased out in favour of CCS. Up to 400 kW.',
 NULL, false, NOW(), NOW()),

-- ─── GB/T — Chinese Standards ─────────────────────────────────────────────

(gen_random_uuid(), 'GB/T 20234.2 AC',            'GBT_AC',  'GBT_AC',  'AC',
 43000,  'China',        'GB/T', '2015',
 'Chinese national standard for AC charging. Single-phase and three-phase up to 43 kW.',
 NULL, false, NOW(), NOW()),

(gen_random_uuid(), 'GB/T 20234.3 DC',            'GBT_DC',  'GBT_DC',  'DC',
 237500, 'China',        'GB/T', '2015',
 'Chinese national standard for DC fast charging. Up to 237.5 kW.',
 NULL, false, NOW(), NOW()),

-- ─── BIS / Indian Bharat Standards ────────────────────────────────────────

(gen_random_uuid(), 'Bharat DC-001',              'BDC',     'TYPE1',   'DC',
 15000,  'India',        'BIS',  '001',
 'Indian national DC charging standard mandated by BIS. 15 kW DC. Being superseded by CCS2 for new installations.',
 NULL, false, NOW(), NOW()),

(gen_random_uuid(), 'Bharat AC-001',              'BAC',     'TYPE1',   'AC',
 10000,  'India',        'BIS',  '001',
 'Indian national AC charging standard mandated by BIS. 10 kW AC. Common on older public chargers.',
 NULL, false, NOW(), NOW()),

-- ─── Legacy / Deprecated ─────────────────────────────────────────────────

(gen_random_uuid(), 'CHAdeMO 1.0',                'CHAdeMO_1', 'CHAdeMO', 'DC',
 62500,  'Global',       'CHAdeMO Association', '1.0',
 'Original CHAdeMO spec limited to 62.5 kW. Superseded by CHAdeMO 2.0.',
 NULL, true, NOW(), NOW());