# ═══════════════════════════════════════════════════════════════════════════════
# VEHICLE CATALOG SERVICE — COMPLETE REQUEST JSON COLLECTION
# Real-world Indian EV data: Tata, Mahindra, MG, Ather, Ola
# Follow this order — each group depends on the previous
# ═══════════════════════════════════════════════════════════════════════════════


# ─────────────────────────────────────────────────────────────────────────────
# GROUP 0: SEED CHARGING STANDARDS FIRST (reference data, done via Flyway V2)
# These are seeded automatically. If testing manually:
# POST /charging-standards
# ─────────────────────────────────────────────────────────────────────────────

# CS-1 — IEC Type 2 AC (European/Indian AC standard)
POST /charging-standards
{
  "name": "IEC 62196 Type 2 AC",
  "shortCode": "TYPE2",
  "connectorType": "TYPE2",
  "currentType": "AC",
  "maxWattage": 43500,
  "geographicRegion": "Europe/India",
  "governingBody": "IEC",
  "version": "2014",
  "description": "Standard AC connector for European and Indian EVs. Single-phase or three-phase up to 43.5 kW.",
  "iconUrl": "https://cdn.evplatform.in/icons/type2.svg"
}

# CS-2 — CCS2 (Combined Charging System 2 — dominant in India & Europe)
POST /charging-standards
{
  "name": "CCS Combo 2",
  "shortCode": "CCS2",
  "connectorType": "CCS2",
  "currentType": "BOTH",
  "maxWattage": 350000,
  "geographicRegion": "Europe/India",
  "governingBody": "IEC",
  "version": "2014",
  "description": "Combined Charging System — TYPE2 AC + DC pins in one plug. DC up to 350 kW. Dominant in India and Europe.",
  "iconUrl": "https://cdn.evplatform.in/icons/ccs2.svg"
}

# CS-3 — CHAdeMO 2.0
POST /charging-standards
{
  "name": "CHAdeMO 2.0",
  "shortCode": "CHAdeMO",
  "connectorType": "CHAdeMO",
  "currentType": "DC",
  "maxWattage": 400000,
  "geographicRegion": "Global",
  "governingBody": "CHAdeMO Association",
  "version": "2.0",
  "description": "DC fast-charge protocol developed in Japan. Legacy standard being phased out in favour of CCS.",
  "iconUrl": "https://cdn.evplatform.in/icons/chademo.svg"
}

# CS-4 — Tesla NACS / SAE J3400
POST /charging-standards
{
  "name": "Tesla NACS / SAE J3400",
  "shortCode": "TESLA_NACS",
  "connectorType": "TESLA_NACS",
  "currentType": "BOTH",
  "maxWattage": 250000,
  "geographicRegion": "USA",
  "governingBody": "SAE",
  "version": "J3400",
  "description": "North American Charging Standard adopted as SAE J3400 in 2023. Up to 250 kW DC.",
  "iconUrl": "https://cdn.evplatform.in/icons/nacs.svg"
}

# CS-5 — GB/T AC (Chinese standard)
POST /charging-standards
{
  "name": "GB/T 20234.2 AC",
  "shortCode": "GBT_AC",
  "connectorType": "GBT_AC",
  "currentType": "AC",
  "maxWattage": 43000,
  "geographicRegion": "China",
  "governingBody": "GB/T",
  "version": "2015",
  "description": "Chinese national standard for AC charging. Up to 43 kW.",
  "iconUrl": "https://cdn.evplatform.in/icons/gbt_ac.svg"
}

# CS-6 — GB/T DC (Chinese DC standard)
POST /charging-standards
{
  "name": "GB/T 20234.3 DC",
  "shortCode": "GBT_DC",
  "connectorType": "GBT_DC",
  "currentType": "DC",
  "maxWattage": 237500,
  "geographicRegion": "China",
  "governingBody": "GB/T",
  "version": "2015",
  "description": "Chinese national standard for DC fast charging. Up to 237.5 kW.",
  "iconUrl": "https://cdn.evplatform.in/icons/gbt_dc.svg"
}

# CS-7 — Bharat DC-001 (Indian DC standard)
POST /charging-standards
{
  "name": "Bharat DC-001",
  "shortCode": "BDC",
  "connectorType": "CCS2",
  "currentType": "DC",
  "maxWattage": 15000,
  "geographicRegion": "India",
  "governingBody": "BIS",
  "version": "001",
  "description": "Indian national DC charging standard mandated by BIS. 15 kW DC. Being superseded by CCS2.",
  "iconUrl": "https://cdn.evplatform.in/icons/bdc.svg"
}

# CS-8 — Bharat AC-001 (Indian AC standard)
POST /charging-standards
{
  "name": "Bharat AC-001",
  "shortCode": "BAC",
  "connectorType": "TYPE2",
  "currentType": "AC",
  "maxWattage": 10000,
  "geographicRegion": "India",
  "governingBody": "BIS",
  "version": "001",
  "description": "Indian national AC charging standard mandated by BIS. 10 kW AC. Common on older public chargers.",
  "iconUrl": "https://cdn.evplatform.in/icons/bac.svg"
}


# ═══════════════════════════════════════════════════════════════════════════════
# GROUP 1: VEHICLE MAKES
# POST /vehicle-makes
# ═══════════════════════════════════════════════════════════════════════════════

# MAKE-1 — Tata Motors
POST /vehicle-makes
{
  "name": "Tata Motors",
  "countryOfOrigin": "IN",
  "logoUrl": "https://cdn.evplatform.in/logos/tata-motors.png",
  "websiteUrl": "https://ev.tatamotors.com"
}

# MAKE-2 — Mahindra Electric
POST /vehicle-makes
{
  "name": "Mahindra Electric",
  "countryOfOrigin": "IN",
  "logoUrl": "https://cdn.evplatform.in/logos/mahindra-electric.png",
  "websiteUrl": "https://www.mahindraelectric.com"
}

# MAKE-3 — MG Motor India
POST /vehicle-makes
{
  "name": "MG Motor India",
  "countryOfOrigin": "CN",
  "logoUrl": "https://cdn.evplatform.in/logos/mg-motor.png",
  "websiteUrl": "https://www.mgmotor.co.in"
}

# MAKE-4 — Ather Energy
POST /vehicle-makes
{
  "name": "Ather Energy",
  "countryOfOrigin": "IN",
  "logoUrl": "https://cdn.evplatform.in/logos/ather-energy.png",
  "websiteUrl": "https://www.atherenergy.com"
}

# MAKE-5 — Ola Electric
POST /vehicle-makes
{
  "name": "Ola Electric",
  "countryOfOrigin": "IN",
  "logoUrl": "https://cdn.evplatform.in/logos/ola-electric.png",
  "websiteUrl": "https://www.olaelectric.com"
}

# MAKE-6 — Hyundai India
POST /vehicle-makes
{
  "name": "Hyundai India",
  "countryOfOrigin": "KR",
  "logoUrl": "https://cdn.evplatform.in/logos/hyundai.png",
  "websiteUrl": "https://www.hyundai.com/in"
}

# MAKE-7 — Kia India
POST /vehicle-makes
{
  "name": "Kia India",
  "countryOfOrigin": "KR",
  "logoUrl": "https://cdn.evplatform.in/logos/kia.png",
  "websiteUrl": "https://www.kia.com/in"
}

# MAKE-8 — BYD India
POST /vehicle-makes
{
  "name": "BYD India",
  "countryOfOrigin": "CN",
  "logoUrl": "https://cdn.evplatform.in/logos/byd.png",
  "websiteUrl": "https://www.byd.com/in"
}

# MAKE-9 — TVS Motor Company
POST /vehicle-makes
{
  "name": "TVS Motor Company",
  "countryOfOrigin": "IN",
  "logoUrl": "https://cdn.evplatform.in/logos/tvs.png",
  "websiteUrl": "https://www.tvsmotor.com"
}

# MAKE-10 — Hero Electric
POST /vehicle-makes
{
  "name": "Hero Electric",
  "countryOfOrigin": "IN",
  "logoUrl": "https://cdn.evplatform.in/logos/hero-electric.png",
  "websiteUrl": "https://www.heroelectric.in"
}


# ─────────────────────────────────────────────────────────────────────────────
# MAKE REGIONS — POST /vehicle-makes/{makeId}/regions
# ─────────────────────────────────────────────────────────────────────────────

# Regions for Tata Motors
POST /vehicle-makes/{tataMotorsMakeId}/regions
{
  "regions": [
    { "regionCode": "IN", "launchYear": 1945 },
    { "regionCode": "GB", "launchYear": 2012 },
    { "regionCode": "ZA", "launchYear": 2011 }
  ]
}

# Regions for MG Motor India
POST /vehicle-makes/{mgMotorMakeId}/regions
{
  "regions": [
    { "regionCode": "IN", "launchYear": 2019 },
    { "regionCode": "GB", "launchYear": 1924 },
    { "regionCode": "AU", "launchYear": 2018 }
  ]
}

# Regions for Hyundai India
POST /vehicle-makes/{hyundaiMakeId}/regions
{
  "regions": [
    { "regionCode": "IN", "launchYear": 1996 },
    { "regionCode": "KR", "launchYear": 1967 },
    { "regionCode": "US", "launchYear": 1986 },
    { "regionCode": "DE", "launchYear": 1991 }
  ]
}


# ═══════════════════════════════════════════════════════════════════════════════
# GROUP 2: VEHICLE MODELS
# POST /vehicle-models
# (makeId from the Make creation response above)
# ═══════════════════════════════════════════════════════════════════════════════

# ─── TATA MOTORS MODELS ───────────────────────────────────────────────────────

# MODEL-1 — Tata Tiago EV 2024
POST /vehicle-models
{
  "makeId": "{tataMotorsMakeId}",
  "name": "Tiago EV",
  "modelYear": 2024,
  "bodyType": "HATCHBACK",
  "seatingCapacity": 5,
  "driveType": "FWD"
}

# MODEL-2 — Tata Tigor EV 2024
POST /vehicle-models
{
  "makeId": "{tataMotorsMakeId}",
  "name": "Tigor EV",
  "modelYear": 2024,
  "bodyType": "SEDAN",
  "seatingCapacity": 5,
  "driveType": "FWD"
}

# MODEL-3 — Tata Nexon EV 2024
POST /vehicle-models
{
  "makeId": "{tataMotorsMakeId}",
  "name": "Nexon EV",
  "modelYear": 2024,
  "bodyType": "SUV",
  "seatingCapacity": 5,
  "driveType": "FWD"
}

# MODEL-4 — Tata Punch EV 2024
POST /vehicle-models
{
  "makeId": "{tataMotorsMakeId}",
  "name": "Punch EV",
  "modelYear": 2024,
  "bodyType": "SUV",
  "seatingCapacity": 5,
  "driveType": "FWD"
}

# MODEL-5 — Tata Curvv EV 2024
POST /vehicle-models
{
  "makeId": "{tataMotorsMakeId}",
  "name": "Curvv EV",
  "modelYear": 2024,
  "bodyType": "SUV",
  "seatingCapacity": 5,
  "driveType": "FWD"
}

# MODEL-6 — Tata Harrier EV 2025
POST /vehicle-models
{
  "makeId": "{tataMotorsMakeId}",
  "name": "Harrier EV",
  "modelYear": 2025,
  "bodyType": "SUV",
  "seatingCapacity": 5,
  "driveType": "AWD"
}

# ─── MAHINDRA ELECTRIC MODELS ─────────────────────────────────────────────────

# MODEL-7 — Mahindra XEV 9e 2024
POST /vehicle-models
{
  "makeId": "{mahindraElectricMakeId}",
  "name": "XEV 9e",
  "modelYear": 2024,
  "bodyType": "SUV",
  "seatingCapacity": 5,
  "driveType": "RWD"
}

# MODEL-8 — Mahindra BE 6e 2024
POST /vehicle-models
{
  "makeId": "{mahindraElectricMakeId}",
  "name": "BE 6e",
  "modelYear": 2024,
  "bodyType": "SUV",
  "seatingCapacity": 5,
  "driveType": "RWD"
}

# MODEL-9 — Mahindra XEV 7e 2025
POST /vehicle-models
{
  "makeId": "{mahindraElectricMakeId}",
  "name": "XEV 7e",
  "modelYear": 2025,
  "bodyType": "SUV",
  "seatingCapacity": 7,
  "driveType": "AWD"
}

# ─── MG MOTOR MODELS ──────────────────────────────────────────────────────────

# MODEL-10 — MG Windsor EV 2024
POST /vehicle-models
{
  "makeId": "{mgMotorMakeId}",
  "name": "Windsor EV",
  "modelYear": 2024,
  "bodyType": "SUV",
  "seatingCapacity": 5,
  "driveType": "FWD"
}

# MODEL-11 — MG ZS EV 2024
POST /vehicle-models
{
  "makeId": "{mgMotorMakeId}",
  "name": "ZS EV",
  "modelYear": 2024,
  "bodyType": "SUV",
  "seatingCapacity": 5,
  "driveType": "FWD"
}

# MODEL-12 — MG Comet EV 2024
POST /vehicle-models
{
  "makeId": "{mgMotorMakeId}",
  "name": "Comet EV",
  "modelYear": 2024,
  "bodyType": "HATCHBACK",
  "seatingCapacity": 4,
  "driveType": "RWD"
}

# ─── ATHER ENERGY MODELS (Two-Wheelers) ───────────────────────────────────────

# MODEL-13 — Ather 450X Gen 4 2024
POST /vehicle-models
{
  "makeId": "{atherEnergyMakeId}",
  "name": "450X Gen 4",
  "modelYear": 2024,
  "bodyType": "TWO_WHEELER",
  "seatingCapacity": 2,
  "driveType": "RWD"
}

# MODEL-14 — Ather Rizta Z 2024
POST /vehicle-models
{
  "makeId": "{atherEnergyMakeId}",
  "name": "Rizta Z",
  "modelYear": 2024,
  "bodyType": "TWO_WHEELER",
  "seatingCapacity": 2,
  "driveType": "RWD"
}

# ─── OLA ELECTRIC MODELS ──────────────────────────────────────────────────────

# MODEL-15 — Ola S1 Pro Gen 2 2024
POST /vehicle-models
{
  "makeId": "{olaElectricMakeId}",
  "name": "S1 Pro Gen 2",
  "modelYear": 2024,
  "bodyType": "TWO_WHEELER",
  "seatingCapacity": 2,
  "driveType": "RWD"
}

# MODEL-16 — Ola S1 X Plus 2024
POST /vehicle-models
{
  "makeId": "{olaElectricMakeId}",
  "name": "S1 X Plus",
  "modelYear": 2024,
  "bodyType": "TWO_WHEELER",
  "seatingCapacity": 2,
  "driveType": "RWD"
}

# ─── HYUNDAI MODELS ───────────────────────────────────────────────────────────

# MODEL-17 — Hyundai Creta Electric 2024
POST /vehicle-models
{
  "makeId": "{hyundaiMakeId}",
  "name": "Creta Electric",
  "modelYear": 2024,
  "bodyType": "SUV",
  "seatingCapacity": 5,
  "driveType": "FWD"
}

# MODEL-18 — Hyundai Ioniq 5 2024
POST /vehicle-models
{
  "makeId": "{hyundaiMakeId}",
  "name": "Ioniq 5",
  "modelYear": 2024,
  "bodyType": "SUV",
  "seatingCapacity": 5,
  "driveType": "AWD"
}

# ─── KIA MODELS ───────────────────────────────────────────────────────────────

# MODEL-19 — Kia EV6 2024
POST /vehicle-models
{
  "makeId": "{kiaMakeId}",
  "name": "EV6",
  "modelYear": 2024,
  "bodyType": "SUV",
  "seatingCapacity": 5,
  "driveType": "AWD"
}

# ─── BYD MODELS ───────────────────────────────────────────────────────────────

# MODEL-20 — BYD Atto 3 2024
POST /vehicle-models
{
  "makeId": "{bydMakeId}",
  "name": "Atto 3",
  "modelYear": 2024,
  "bodyType": "SUV",
  "seatingCapacity": 5,
  "driveType": "FWD"
}

# MODEL-21 — BYD Seal 2024
POST /vehicle-models
{
  "makeId": "{bydMakeId}",
  "name": "Seal",
  "modelYear": 2024,
  "bodyType": "SEDAN",
  "seatingCapacity": 5,
  "driveType": "AWD"
}

# ─── TVS MODELS ───────────────────────────────────────────────────────────────

# MODEL-22 — TVS iQube ST 2024
POST /vehicle-models
{
  "makeId": "{tvsMakeId}",
  "name": "iQube ST",
  "modelYear": 2024,
  "bodyType": "TWO_WHEELER",
  "seatingCapacity": 2,
  "driveType": "RWD"
}


# ─── Model Images — POST /vehicle-models/{modelId}/images ────────────────────

# Images for Tata Tiago EV
POST /vehicle-models/{tiagoEvModelId}/images
{
  "url": "https://cdn.evplatform.in/models/tiago-ev/front.jpg",
  "isPrimary": true,
  "angle": "FRONT"
}

POST /vehicle-models/{tiagoEvModelId}/images
{
  "url": "https://cdn.evplatform.in/models/tiago-ev/side.jpg",
  "isPrimary": false,
  "angle": "SIDE"
}

POST /vehicle-models/{tiagoEvModelId}/images
{
  "url": "https://cdn.evplatform.in/models/tiago-ev/rear.jpg",
  "isPrimary": false,
  "angle": "REAR"
}

POST /vehicle-models/{tiagoEvModelId}/images
{
  "url": "https://cdn.evplatform.in/models/tiago-ev/charging-port.jpg",
  "isPrimary": false,
  "angle": "CHARGING_PORT"
}

POST /vehicle-models/{tiagoEvModelId}/images
{
  "url": "https://cdn.evplatform.in/models/tiago-ev/interior.jpg",
  "isPrimary": false,
  "angle": "INTERIOR"
}


# ═══════════════════════════════════════════════════════════════════════════════
# GROUP 3A: BATTERY PACKS
# POST /vehicle-models/{modelId}/battery-packs
# ═══════════════════════════════════════════════════════════════════════════════

# ─── TATA TIAGO EV battery packs ─────────────────────────────────────────────

# BP-1 — Tiago EV Medium Range
POST /vehicle-models/{tiagoEvModelId}/battery-packs
{
  "packName": "Medium Range",
  "capacityKwh": 19.2,
  "usableKwh": 17.1,
  "rangeKm": 250,
  "chemistry": "LFP",
  "cellsConfiguration": "48S1P",
  "warrantyYears": 8,
  "warrantyKm": 160000
}

# BP-2 — Tiago EV Long Range
POST /vehicle-models/{tiagoEvModelId}/battery-packs
{
  "packName": "Long Range",
  "capacityKwh": 24.0,
  "usableKwh": 21.5,
  "rangeKm": 315,
  "chemistry": "LFP",
  "cellsConfiguration": "60S1P",
  "warrantyYears": 8,
  "warrantyKm": 160000
}

# ─── TATA NEXON EV battery packs ─────────────────────────────────────────────

# BP-3 — Nexon EV Standard Range
POST /vehicle-models/{nexonEvModelId}/battery-packs
{
  "packName": "Standard Range",
  "capacityKwh": 30.2,
  "usableKwh": 26.5,
  "rangeKm": 312,
  "chemistry": "NMC",
  "cellsConfiguration": "72S2P",
  "warrantyYears": 8,
  "warrantyKm": 160000
}

# BP-4 — Nexon EV Long Range
POST /vehicle-models/{nexonEvModelId}/battery-packs
{
  "packName": "Long Range",
  "capacityKwh": 40.5,
  "usableKwh": 36.2,
  "rangeKm": 465,
  "chemistry": "NMC",
  "cellsConfiguration": "96S2P",
  "warrantyYears": 8,
  "warrantyKm": 160000
}

# ─── TATA PUNCH EV battery packs ─────────────────────────────────────────────

# BP-5 — Punch EV Standard Range
POST /vehicle-models/{punchEvModelId}/battery-packs
{
  "packName": "Standard Range",
  "capacityKwh": 25.0,
  "usableKwh": 22.0,
  "rangeKm": 301,
  "chemistry": "LFP",
  "cellsConfiguration": "60S1P",
  "warrantyYears": 8,
  "warrantyKm": 160000
}

# BP-6 — Punch EV Long Range
POST /vehicle-models/{punchEvModelId}/battery-packs
{
  "packName": "Long Range",
  "capacityKwh": 35.0,
  "usableKwh": 31.0,
  "rangeKm": 421,
  "chemistry": "LFP",
  "cellsConfiguration": "84S1P",
  "warrantyYears": 8,
  "warrantyKm": 160000
}

# ─── TATA CURVV EV battery packs ─────────────────────────────────────────────

# BP-7 — Curvv EV 45 kWh
POST /vehicle-models/{curvvEvModelId}/battery-packs
{
  "packName": "45 kWh Standard",
  "capacityKwh": 45.0,
  "usableKwh": 41.0,
  "rangeKm": 502,
  "chemistry": "NMC",
  "cellsConfiguration": "96S1P",
  "warrantyYears": 8,
  "warrantyKm": 160000
}

# BP-8 — Curvv EV 55 kWh
POST /vehicle-models/{curvvEvModelId}/battery-packs
{
  "packName": "55 kWh Long Range",
  "capacityKwh": 55.0,
  "usableKwh": 50.5,
  "rangeKm": 585,
  "chemistry": "NMC",
  "cellsConfiguration": "120S1P",
  "warrantyYears": 8,
  "warrantyKm": 160000
}

# ─── MAHINDRA XEV 9e battery packs ───────────────────────────────────────────

# BP-9 — XEV 9e 59 kWh
POST /vehicle-models/{xev9eModelId}/battery-packs
{
  "packName": "59 kWh",
  "capacityKwh": 59.0,
  "usableKwh": 54.0,
  "rangeKm": 542,
  "chemistry": "NMC",
  "cellsConfiguration": "108S1P",
  "warrantyYears": 8,
  "warrantyKm": 150000
}

# BP-10 — XEV 9e 79 kWh
POST /vehicle-models/{xev9eModelId}/battery-packs
{
  "packName": "79 kWh",
  "capacityKwh": 79.0,
  "usableKwh": 72.8,
  "rangeKm": 656,
  "chemistry": "NMC",
  "cellsConfiguration": "144S1P",
  "warrantyYears": 8,
  "warrantyKm": 150000
}

# ─── MG WINDSOR EV battery pack ──────────────────────────────────────────────

# BP-11 — Windsor EV 38 kWh
POST /vehicle-models/{windsorEvModelId}/battery-packs
{
  "packName": "38 kWh",
  "capacityKwh": 38.0,
  "usableKwh": 34.0,
  "rangeKm": 331,
  "chemistry": "NMC",
  "cellsConfiguration": "72S2P",
  "warrantyYears": 8,
  "warrantyKm": 150000
}

# ─── HYUNDAI CRETA ELECTRIC battery packs ────────────────────────────────────

# BP-12 — Creta Electric Standard 42 kWh
POST /vehicle-models/{cretaElectricModelId}/battery-packs
{
  "packName": "42 kWh Standard",
  "capacityKwh": 42.0,
  "usableKwh": 37.8,
  "rangeKm": 390,
  "chemistry": "NMC",
  "cellsConfiguration": "96S1P",
  "warrantyYears": 8,
  "warrantyKm": 160000
}

# BP-13 — Creta Electric Long Range 51.4 kWh
POST /vehicle-models/{cretaElectricModelId}/battery-packs
{
  "packName": "51.4 kWh Long Range",
  "capacityKwh": 51.4,
  "usableKwh": 46.3,
  "rangeKm": 473,
  "chemistry": "NMC",
  "cellsConfiguration": "108S1P",
  "warrantyYears": 8,
  "warrantyKm": 160000
}

# ─── BYD ATTO 3 battery pack ─────────────────────────────────────────────────

# BP-14 — Atto 3 60.48 kWh Blade
POST /vehicle-models/{atto3ModelId}/battery-packs
{
  "packName": "60.48 kWh Blade",
  "capacityKwh": 60.48,
  "usableKwh": 58.56,
  "rangeKm": 521,
  "chemistry": "LFP",
  "cellsConfiguration": "Blade Cell",
  "warrantyYears": 8,
  "warrantyKm": 160000
}

# ─── KIA EV6 battery packs ───────────────────────────────────────────────────

# BP-15 — EV6 Standard 58 kWh
POST /vehicle-models/{ev6ModelId}/battery-packs
{
  "packName": "58 kWh Standard",
  "capacityKwh": 58.0,
  "usableKwh": 53.0,
  "rangeKm": 385,
  "chemistry": "NMC",
  "cellsConfiguration": "192S1P",
  "warrantyYears": 7,
  "warrantyKm": 150000
}

# BP-16 — EV6 Long Range 77.4 kWh
POST /vehicle-models/{ev6ModelId}/battery-packs
{
  "packName": "77.4 kWh Long Range",
  "capacityKwh": 77.4,
  "usableKwh": 74.0,
  "rangeKm": 528,
  "chemistry": "NMC",
  "cellsConfiguration": "192S1P",
  "warrantyYears": 7,
  "warrantyKm": 150000
}


# ═══════════════════════════════════════════════════════════════════════════════
# GROUP 3B: MODEL TRIMS
# POST /vehicle-models/{modelId}/trims
# ═══════════════════════════════════════════════════════════════════════════════

# ─── TATA TIAGO EV trims ─────────────────────────────────────────────────────

# TRIM-1 — Tiago EV XE (base)
POST /vehicle-models/{tiagoEvModelId}/trims
{
  "trimName": "XE",
  "description": "Base trim — manual AC, basic infotainment, essential safety features",
  "hasSunroof": false,
  "hasAdas": false,
  "hasConnectedCar": false,
  "infotainmentSizeInches": null,
  "sortOrder": 1
}

# TRIM-2 — Tiago EV XT
POST /vehicle-models/{tiagoEvModelId}/trims
{
  "trimName": "XT",
  "description": "Mid trim — auto AC, 7-inch touchscreen, Leq speaker system",
  "hasSunroof": false,
  "hasAdas": false,
  "hasConnectedCar": false,
  "infotainmentSizeInches": 7,
  "sortOrder": 2
}

# TRIM-3 — Tiago EV XZ Plus
POST /vehicle-models/{tiagoEvModelId}/trims
{
  "trimName": "XZ+",
  "description": "Top trim — 10.25-inch touchscreen, Arcade.ev connected car, auto-dimming IRVM, ambient lighting",
  "hasSunroof": false,
  "hasAdas": false,
  "hasConnectedCar": true,
  "infotainmentSizeInches": 10,
  "sortOrder": 3
}

# TRIM-4 — Tiago EV XZ Plus Tech LUX
POST /vehicle-models/{tiagoEvModelId}/trims
{
  "trimName": "XZ+ Tech LUX",
  "description": "Top luxury trim — leatherette seats, air purifier, Harman branded speakers, sunroof, 360 view",
  "hasSunroof": true,
  "hasAdas": false,
  "hasConnectedCar": true,
  "infotainmentSizeInches": 10,
  "sortOrder": 4
}

# ─── TATA NEXON EV trims ─────────────────────────────────────────────────────

# TRIM-5 — Nexon EV Creative
POST /vehicle-models/{nexonEvModelId}/trims
{
  "trimName": "Creative",
  "description": "Base trim — essential features, 10.25-inch infotainment, wireless Android Auto/CarPlay",
  "hasSunroof": false,
  "hasAdas": false,
  "hasConnectedCar": true,
  "infotainmentSizeInches": 10,
  "sortOrder": 1
}

# TRIM-6 — Nexon EV Fearless
POST /vehicle-models/{nexonEvModelId}/trims
{
  "trimName": "Fearless",
  "description": "Mid trim — panoramic sunroof, ventilated seats, air purifier, 9-speaker JBL audio",
  "hasSunroof": true,
  "hasAdas": false,
  "hasConnectedCar": true,
  "infotainmentSizeInches": 10,
  "sortOrder": 2
}

# TRIM-7 — Nexon EV Empowered
POST /vehicle-models/{nexonEvModelId}/trims
{
  "trimName": "Empowered",
  "description": "Top trim — ADAS suite (autonomous emergency braking, lane keep assist, adaptive cruise control), 360 camera",
  "hasSunroof": true,
  "hasAdas": true,
  "hasConnectedCar": true,
  "infotainmentSizeInches": 10,
  "sortOrder": 3
}

# ─── TATA PUNCH EV trims ─────────────────────────────────────────────────────

# TRIM-8 — Punch EV Smart
POST /vehicle-models/{punchEvModelId}/trims
{
  "trimName": "Smart",
  "description": "Base trim — 10.25-inch infotainment, wireless charging, rear parking camera",
  "hasSunroof": false,
  "hasAdas": false,
  "hasConnectedCar": true,
  "infotainmentSizeInches": 10,
  "sortOrder": 1
}

# TRIM-9 — Punch EV Empowered Plus
POST /vehicle-models/{punchEvModelId}/trims
{
  "trimName": "Empowered+",
  "description": "Top trim — ADAS, panoramic sunroof, leatherette seats, air purifier, JBL 9-speaker",
  "hasSunroof": true,
  "hasAdas": true,
  "hasConnectedCar": true,
  "infotainmentSizeInches": 10,
  "sortOrder": 2
}

# ─── MAHINDRA XEV 9e trims ───────────────────────────────────────────────────

# TRIM-10 — XEV 9e Pack One
POST /vehicle-models/{xev9eModelId}/trims
{
  "trimName": "Pack One",
  "description": "Base — 16-inch OLED infotainment, panoramic sunroof, wireless charging, level 2 ADAS",
  "hasSunroof": true,
  "hasAdas": true,
  "hasConnectedCar": true,
  "infotainmentSizeInches": 16,
  "sortOrder": 1
}

# TRIM-11 — XEV 9e Pack Two
POST /vehicle-models/{xev9eModelId}/trims
{
  "trimName": "Pack Two",
  "description": "Mid — all Pack One features plus Harman Kardon audio, massage seats, 12-inch digital cockpit",
  "hasSunroof": true,
  "hasAdas": true,
  "hasConnectedCar": true,
  "infotainmentSizeInches": 16,
  "sortOrder": 2
}

# TRIM-12 — XEV 9e Pack Three
POST /vehicle-models/{xev9eModelId}/trims
{
  "trimName": "Pack Three",
  "description": "Top — all Pack Two plus level 3 ADAS, 360 surround view, ambient interior lighting, twin panoramic screens",
  "hasSunroof": true,
  "hasAdas": true,
  "hasConnectedCar": true,
  "infotainmentSizeInches": 16,
  "sortOrder": 3
}

# ─── HYUNDAI CRETA ELECTRIC trims ────────────────────────────────────────────

# TRIM-13 — Creta Electric Executive
POST /vehicle-models/{cretaElectricModelId}/trims
{
  "trimName": "Executive",
  "description": "Base — 10.25-inch infotainment, rear camera, dual-zone climate",
  "hasSunroof": false,
  "hasAdas": false,
  "hasConnectedCar": true,
  "infotainmentSizeInches": 10,
  "sortOrder": 1
}

# TRIM-14 — Creta Electric Excellence
POST /vehicle-models/{cretaElectricModelId}/trims
{
  "trimName": "Excellence",
  "description": "Mid — panoramic sunroof, BOSE 8-speaker audio, ventilated front seats",
  "hasSunroof": true,
  "hasAdas": false,
  "hasConnectedCar": true,
  "infotainmentSizeInches": 10,
  "sortOrder": 2
}

# TRIM-15 — Creta Electric Excellence S
POST /vehicle-models/{cretaElectricModelId}/trims
{
  "trimName": "Excellence S",
  "description": "Top — ADAS with 5 autonomous features, 12.3-inch digital cluster, 360 surround view",
  "hasSunroof": true,
  "hasAdas": true,
  "hasConnectedCar": true,
  "infotainmentSizeInches": 12,
  "sortOrder": 3
}

# ─── MG WINDSOR EV trims ─────────────────────────────────────────────────────

# TRIM-16 — Windsor EV Excite
POST /vehicle-models/{windsorEvModelId}/trims
{
  "trimName": "Excite",
  "description": "Base — 15.6-inch portrait infotainment, wireless charging, reclining rear seats",
  "hasSunroof": false,
  "hasAdas": false,
  "hasConnectedCar": true,
  "infotainmentSizeInches": 15,
  "sortOrder": 1
}

# TRIM-17 — Windsor EV Essence
POST /vehicle-models/{windsorEvModelId}/trims
{
  "trimName": "Essence",
  "description": "Top — panoramic sunroof, ADAS, 8-speaker Infinity audio, ambient lighting",
  "hasSunroof": true,
  "hasAdas": true,
  "hasConnectedCar": true,
  "infotainmentSizeInches": 15,
  "sortOrder": 2
}

# ─── KIA EV6 trims ───────────────────────────────────────────────────────────

# TRIM-18 — EV6 GT Line
POST /vehicle-models/{ev6ModelId}/trims
{
  "trimName": "GT Line",
  "description": "Base — 12.3-inch dual screen, 8-speaker MERIDIAN audio, heat pump",
  "hasSunroof": false,
  "hasAdas": true,
  "hasConnectedCar": true,
  "infotainmentSizeInches": 12,
  "sortOrder": 1
}

# TRIM-19 — EV6 GT Line Plus
POST /vehicle-models/{ev6ModelId}/trims
{
  "trimName": "GT Line Plus",
  "description": "Top — panoramic sunroof, MERIDIAN 14-speaker, massage seats, head-up display",
  "hasSunroof": true,
  "hasAdas": true,
  "hasConnectedCar": true,
  "infotainmentSizeInches": 12,
  "sortOrder": 2
}


# ═══════════════════════════════════════════════════════════════════════════════
# GROUP 3C: CHARGING CONFIGURATIONS
# POST /vehicle-models/{modelId}/charging-configurations
# ═══════════════════════════════════════════════════════════════════════════════

# ─── TATA TIAGO EV charging configs ──────────────────────────────────────────

# CC-1 — Tiago EV 3.3 kW AC
POST /vehicle-models/{tiagoEvModelId}/charging-configurations
{
  "configLabel": "3.3 kW AC Standard",
  "onboardChargerKw": 3.3,
  "connectorType": "TYPE2",
  "currentType": "AC",
  "chargeTimeFullMinutes": 390,
  "chargeTime10To80Minutes": 260,
  "cableIncluded": true
}

# CC-2 — Tiago EV 7.2 kW AC Fast
POST /vehicle-models/{tiagoEvModelId}/charging-configurations
{
  "configLabel": "7.2 kW AC Fast",
  "onboardChargerKw": 7.2,
  "connectorType": "TYPE2",
  "currentType": "AC",
  "chargeTimeFullMinutes": 190,
  "chargeTime10To80Minutes": 126,
  "cableIncluded": true
}

# ─── TATA NEXON EV charging configs ──────────────────────────────────────────

# CC-3 — Nexon EV 7.2 kW AC
POST /vehicle-models/{nexonEvModelId}/charging-configurations
{
  "configLabel": "7.2 kW AC",
  "onboardChargerKw": 7.2,
  "connectorType": "TYPE2",
  "currentType": "AC",
  "chargeTimeFullMinutes": 320,
  "chargeTime10To80Minutes": 210,
  "cableIncluded": true
}

# CC-4 — Nexon EV 50 kW DC Fast Charge
POST /vehicle-models/{nexonEvModelId}/charging-configurations
{
  "configLabel": "50 kW DC Fast Charge",
  "onboardChargerKw": 50.0,
  "connectorType": "CCS2",
  "currentType": "DC",
  "chargeTimeFullMinutes": 57,
  "chargeTime10To80Minutes": 56,
  "cableIncluded": false
}

# ─── TATA PUNCH EV charging configs ──────────────────────────────────────────

# CC-5 — Punch EV 3.3 kW AC
POST /vehicle-models/{punchEvModelId}/charging-configurations
{
  "configLabel": "3.3 kW AC Standard",
  "onboardChargerKw": 3.3,
  "connectorType": "TYPE2",
  "currentType": "AC",
  "chargeTimeFullMinutes": 480,
  "chargeTime10To80Minutes": 320,
  "cableIncluded": true
}

# CC-6 — Punch EV 7.2 kW AC Fast
POST /vehicle-models/{punchEvModelId}/charging-configurations
{
  "configLabel": "7.2 kW AC Fast",
  "onboardChargerKw": 7.2,
  "connectorType": "TYPE2",
  "currentType": "AC",
  "chargeTimeFullMinutes": 292,
  "chargeTime10To80Minutes": 195,
  "cableIncluded": true
}

# CC-7 — Punch EV 25 kW DC Fast Charge
POST /vehicle-models/{punchEvModelId}/charging-configurations
{
  "configLabel": "25 kW DC Fast Charge",
  "onboardChargerKw": 25.0,
  "connectorType": "CCS2",
  "currentType": "DC",
  "chargeTimeFullMinutes": 56,
  "chargeTime10To80Minutes": 56,
  "cableIncluded": false
}

# ─── TATA CURVV EV charging configs ──────────────────────────────────────────

# CC-8 — Curvv EV 11 kW AC
POST /vehicle-models/{curvvEvModelId}/charging-configurations
{
  "configLabel": "11 kW AC",
  "onboardChargerKw": 11.0,
  "connectorType": "TYPE2",
  "currentType": "AC",
  "chargeTimeFullMinutes": 300,
  "chargeTime10To80Minutes": 200,
  "cableIncluded": true
}

# CC-9 — Curvv EV 100 kW DC Fast Charge
POST /vehicle-models/{curvvEvModelId}/charging-configurations
{
  "configLabel": "100 kW DC Fast Charge",
  "onboardChargerKw": 100.0,
  "connectorType": "CCS2",
  "currentType": "DC",
  "chargeTimeFullMinutes": 36,
  "chargeTime10To80Minutes": 40,
  "cableIncluded": false
}

# ─── MAHINDRA XEV 9e charging configs ────────────────────────────────────────

# CC-10 — XEV 9e 11.2 kW AC
POST /vehicle-models/{xev9eModelId}/charging-configurations
{
  "configLabel": "11.2 kW AC",
  "onboardChargerKw": 11.2,
  "connectorType": "TYPE2",
  "currentType": "AC",
  "chargeTimeFullMinutes": 420,
  "chargeTime10To80Minutes": 280,
  "cableIncluded": true
}

# CC-11 — XEV 9e 175 kW DC Fast Charge (Mahindra's InfinityVector platform)
POST /vehicle-models/{xev9eModelId}/charging-configurations
{
  "configLabel": "175 kW DC Fast Charge",
  "onboardChargerKw": 175.0,
  "connectorType": "CCS2",
  "currentType": "DC",
  "chargeTimeFullMinutes": 30,
  "chargeTime10To80Minutes": 20,
  "cableIncluded": false
}

# ─── MG WINDSOR EV charging config ───────────────────────────────────────────

# CC-12 — Windsor EV 11 kW AC
POST /vehicle-models/{windsorEvModelId}/charging-configurations
{
  "configLabel": "11 kW AC",
  "onboardChargerKw": 11.0,
  "connectorType": "TYPE2",
  "currentType": "AC",
  "chargeTimeFullMinutes": 210,
  "chargeTime10To80Minutes": 140,
  "cableIncluded": true
}

# CC-13 — Windsor EV 50 kW DC Fast Charge
POST /vehicle-models/{windsorEvModelId}/charging-configurations
{
  "configLabel": "50 kW DC Fast Charge",
  "onboardChargerKw": 50.0,
  "connectorType": "CCS2",
  "currentType": "DC",
  "chargeTimeFullMinutes": 60,
  "chargeTime10To80Minutes": 55,
  "cableIncluded": false
}

# ─── HYUNDAI CRETA ELECTRIC charging configs ──────────────────────────────────

# CC-14 — Creta Electric 11 kW AC
POST /vehicle-models/{cretaElectricModelId}/charging-configurations
{
  "configLabel": "11 kW AC",
  "onboardChargerKw": 11.0,
  "connectorType": "TYPE2",
  "currentType": "AC",
  "chargeTimeFullMinutes": 280,
  "chargeTime10To80Minutes": 186,
  "cableIncluded": true
}

# CC-15 — Creta Electric 50 kW DC Fast Charge
POST /vehicle-models/{cretaElectricModelId}/charging-configurations
{
  "configLabel": "50 kW DC Fast Charge",
  "onboardChargerKw": 50.0,
  "connectorType": "CCS2",
  "currentType": "DC",
  "chargeTimeFullMinutes": 58,
  "chargeTime10To80Minutes": 58,
  "cableIncluded": false
}

# ─── KIA EV6 charging configs ─────────────────────────────────────────────────

# CC-16 — EV6 11 kW AC
POST /vehicle-models/{ev6ModelId}/charging-configurations
{
  "configLabel": "11 kW AC",
  "onboardChargerKw": 11.0,
  "connectorType": "TYPE2",
  "currentType": "AC",
  "chargeTimeFullMinutes": 420,
  "chargeTime10To80Minutes": 280,
  "cableIncluded": true
}

# CC-17 — EV6 800V 220 kW Ultra-Fast DC
POST /vehicle-models/{ev6ModelId}/charging-configurations
{
  "configLabel": "800V 220 kW Ultra-Fast DC",
  "onboardChargerKw": 220.0,
  "connectorType": "CCS2",
  "currentType": "DC",
  "chargeTimeFullMinutes": 25,
  "chargeTime10To80Minutes": 18,
  "cableIncluded": false
}

# ─── BYD ATTO 3 charging configs ─────────────────────────────────────────────

# CC-18 — Atto 3 7 kW AC
POST /vehicle-models/{atto3ModelId}/charging-configurations
{
  "configLabel": "7 kW AC",
  "onboardChargerKw": 7.0,
  "connectorType": "TYPE2",
  "currentType": "AC",
  "chargeTimeFullMinutes": 520,
  "chargeTime10To80Minutes": 347,
  "cableIncluded": true
}

# CC-19 — Atto 3 80 kW DC Fast Charge
POST /vehicle-models/{atto3ModelId}/charging-configurations
{
  "configLabel": "80 kW DC Fast Charge",
  "onboardChargerKw": 80.0,
  "connectorType": "CCS2",
  "currentType": "DC",
  "chargeTimeFullMinutes": 50,
  "chargeTime10To80Minutes": 45,
  "cableIncluded": false
}


# ═══════════════════════════════════════════════════════════════════════════════
# GROUP 3D: VEHICLE CHARGING SPECS
# POST /vehicle-models/{modelId}/charging-configurations/{configId}/specs
# ─ The chargingStandardId values are the UUIDs from the seeded standards above
# ═══════════════════════════════════════════════════════════════════════════════

# ─── Specs for Tiago EV 3.3 kW AC config ─────────────────────────────────────

# SPEC-1 — TYPE2 AC spec on Tiago 3.3 kW config
POST /vehicle-models/{tiagoEvModelId}/charging-configurations/{cc1ConfigId}/specs
{
  "chargingStandardId": "{type2StandardId}",
  "connectorType": "TYPE2",
  "currentType": "AC",
  "maxAcceptedWattage": 3300,
  "onboardChargerWattage": 3300,
  "chargeTime10To80Pct": 260,
  "chargeTimeToFullMinutes": 390,
  "cableIncluded": true,
  "notes": "Standard AC charging using TYPE2 connector. Compatible with all TYPE2 wall boxes and public chargers."
}

# ─── Specs for Tiago EV 7.2 kW AC config ─────────────────────────────────────

# SPEC-2 — TYPE2 AC spec on Tiago 7.2 kW config
POST /vehicle-models/{tiagoEvModelId}/charging-configurations/{cc2ConfigId}/specs
{
  "chargingStandardId": "{type2StandardId}",
  "connectorType": "TYPE2",
  "currentType": "AC",
  "maxAcceptedWattage": 7200,
  "onboardChargerWattage": 7200,
  "chargeTime10To80Pct": 126,
  "chargeTimeToFullMinutes": 190,
  "cableIncluded": true,
  "notes": "Fast AC charging — 7.2 kW onboard charger. Compatible with all 7+ kW TYPE2 wall boxes."
}

# ─── Specs for Nexon EV 7.2 kW AC config ─────────────────────────────────────

# SPEC-3 — TYPE2 AC on Nexon 7.2 kW
POST /vehicle-models/{nexonEvModelId}/charging-configurations/{cc3ConfigId}/specs
{
  "chargingStandardId": "{type2StandardId}",
  "connectorType": "TYPE2",
  "currentType": "AC",
  "maxAcceptedWattage": 7200,
  "onboardChargerWattage": 7200,
  "chargeTime10To80Pct": 210,
  "chargeTimeToFullMinutes": 320,
  "cableIncluded": true,
  "notes": "AC charging for 30.2 kWh Standard Range pack."
}

# ─── Specs for Nexon EV 50 kW DC config ──────────────────────────────────────

# SPEC-4 — CCS2 DC on Nexon 50 kW config
POST /vehicle-models/{nexonEvModelId}/charging-configurations/{cc4ConfigId}/specs
{
  "chargingStandardId": "{ccs2StandardId}",
  "connectorType": "CCS2",
  "currentType": "DC",
  "maxAcceptedWattage": 50000,
  "onboardChargerWattage": null,
  "chargeTime10To80Pct": 56,
  "chargeTimeToFullMinutes": null,
  "cableIncluded": false,
  "notes": "DC fast charge via CCS2. Station cable required. Suitable for both Standard and Long Range packs."
}

# ─── Specs for Mahindra XEV 9e 175 kW DC config ──────────────────────────────

# SPEC-5 — CCS2 175 kW DC on XEV 9e
POST /vehicle-models/{xev9eModelId}/charging-configurations/{cc11ConfigId}/specs
{
  "chargingStandardId": "{ccs2StandardId}",
  "connectorType": "CCS2",
  "currentType": "DC",
  "maxAcceptedWattage": 175000,
  "onboardChargerWattage": null,
  "chargeTime10To80Pct": 20,
  "chargeTimeToFullMinutes": 30,
  "cableIncluded": false,
  "notes": "Ultra-fast DC charging. Requires 175 kW CCS2 charger. Available only on 79 kWh pack variants."
}

# ─── Specs for KIA EV6 800V Ultra-Fast DC config ─────────────────────────────

# SPEC-6 — CCS2 220 kW on EV6
POST /vehicle-models/{ev6ModelId}/charging-configurations/{cc17ConfigId}/specs
{
  "chargingStandardId": "{ccs2StandardId}",
  "connectorType": "CCS2",
  "currentType": "DC",
  "maxAcceptedWattage": 220000,
  "onboardChargerWattage": null,
  "chargeTime10To80Pct": 18,
  "chargeTimeToFullMinutes": 25,
  "cableIncluded": false,
  "notes": "800V architecture — 10→80% in 18 minutes at 220 kW charger. At a 50 kW station, estimated 79 minutes."
}


# ═══════════════════════════════════════════════════════════════════════════════
# GROUP 3E: VARIANT LISTINGS (The sellable SKUs)
# POST /variant-listings
# Format: Model + Trim + BatteryPack + ChargingConfig = one VariantListing
# ═══════════════════════════════════════════════════════════════════════════════

# ─── TATA TIAGO EV variant listings (7 SKUs — matches the price sheet) ────────

# VL-1 — Tiago EV XE + 19.2 kWh + 3.3 kW AC
POST /variant-listings
{
  "modelId": "{tiagoEvModelId}",
  "trimId": "{xeTrimId}",
  "batteryPackId": "{bp1MediumRangeId}",
  "chargingConfigurationId": "{cc1_33kwAcId}",
  "priceInr": 849000,
  "launchDate": "2023-01-23",
  "status": "ACTIVE",
  "weightKg": 1205,
  "sortOrder": 1
}

# VL-2 — Tiago EV XT + 19.2 kWh + 3.3 kW AC
POST /variant-listings
{
  "modelId": "{tiagoEvModelId}",
  "trimId": "{xtTrimId}",
  "batteryPackId": "{bp1MediumRangeId}",
  "chargingConfigurationId": "{cc1_33kwAcId}",
  "priceInr": 909000,
  "launchDate": "2023-01-23",
  "status": "ACTIVE",
  "weightKg": 1215,
  "sortOrder": 2
}

# VL-3 — Tiago EV XT + 24 kWh + 3.3 kW AC
POST /variant-listings
{
  "modelId": "{tiagoEvModelId}",
  "trimId": "{xtTrimId}",
  "batteryPackId": "{bp2LongRangeId}",
  "chargingConfigurationId": "{cc1_33kwAcId}",
  "priceInr": 999000,
  "launchDate": "2023-01-23",
  "status": "ACTIVE",
  "weightKg": 1225,
  "sortOrder": 3
}

# VL-4 — Tiago EV XZ+ + 24 kWh + 3.3 kW AC
POST /variant-listings
{
  "modelId": "{tiagoEvModelId}",
  "trimId": "{xzPlusTrimId}",
  "batteryPackId": "{bp2LongRangeId}",
  "chargingConfigurationId": "{cc1_33kwAcId}",
  "priceInr": 1079000,
  "launchDate": "2023-01-23",
  "status": "ACTIVE",
  "weightKg": 1240,
  "sortOrder": 4
}

# VL-5 — Tiago EV XZ+ Tech LUX + 24 kWh + 3.3 kW AC
POST /variant-listings
{
  "modelId": "{tiagoEvModelId}",
  "trimId": "{xzPlusTechLuxTrimId}",
  "batteryPackId": "{bp2LongRangeId}",
  "chargingConfigurationId": "{cc1_33kwAcId}",
  "priceInr": 1129000,
  "launchDate": "2023-01-23",
  "status": "ACTIVE",
  "weightKg": 1250,
  "sortOrder": 5
}

# VL-6 — Tiago EV XZ+ + 24 kWh + 7.2 kW AC (fast charger option)
POST /variant-listings
{
  "modelId": "{tiagoEvModelId}",
  "trimId": "{xzPlusTrimId}",
  "batteryPackId": "{bp2LongRangeId}",
  "chargingConfigurationId": "{cc2_72kwAcId}",
  "priceInr": 1129000,
  "launchDate": "2023-01-23",
  "status": "ACTIVE",
  "weightKg": 1240,
  "sortOrder": 6
}

# VL-7 — Tiago EV XZ+ Tech LUX + 24 kWh + 7.2 kW AC
POST /variant-listings
{
  "modelId": "{tiagoEvModelId}",
  "trimId": "{xzPlusTechLuxTrimId}",
  "batteryPackId": "{bp2LongRangeId}",
  "chargingConfigurationId": "{cc2_72kwAcId}",
  "priceInr": 1179000,
  "launchDate": "2023-01-23",
  "status": "ACTIVE",
  "weightKg": 1250,
  "sortOrder": 7
}

# ─── TATA NEXON EV variant listings ──────────────────────────────────────────

# VL-8 — Nexon EV Creative + Standard 30.2 kWh + 7.2 kW AC
POST /variant-listings
{
  "modelId": "{nexonEvModelId}",
  "trimId": "{creativeTrimId}",
  "batteryPackId": "{bp3StandardRangeId}",
  "chargingConfigurationId": "{cc3_72kwAcId}",
  "priceInr": 1449000,
  "launchDate": "2023-09-15",
  "status": "ACTIVE",
  "weightKg": 1497,
  "sortOrder": 1
}

# VL-9 — Nexon EV Creative + Standard 30.2 kWh + 50 kW DC
POST /variant-listings
{
  "modelId": "{nexonEvModelId}",
  "trimId": "{creativeTrimId}",
  "batteryPackId": "{bp3StandardRangeId}",
  "chargingConfigurationId": "{cc4_50kwDcId}",
  "priceInr": 1649000,
  "launchDate": "2023-09-15",
  "status": "ACTIVE",
  "weightKg": 1510,
  "sortOrder": 2
}

# VL-10 — Nexon EV Fearless + Long Range 40.5 kWh + 50 kW DC
POST /variant-listings
{
  "modelId": "{nexonEvModelId}",
  "trimId": "{fearlessTrimId}",
  "batteryPackId": "{bp4LongRangeId}",
  "chargingConfigurationId": "{cc4_50kwDcId}",
  "priceInr": 1849000,
  "launchDate": "2023-09-15",
  "status": "ACTIVE",
  "weightKg": 1534,
  "sortOrder": 3
}

# VL-11 — Nexon EV Empowered + Long Range 40.5 kWh + 50 kW DC
POST /variant-listings
{
  "modelId": "{nexonEvModelId}",
  "trimId": "{empoweredTrimId}",
  "batteryPackId": "{bp4LongRangeId}",
  "chargingConfigurationId": "{cc4_50kwDcId}",
  "priceInr": 2099000,
  "launchDate": "2023-09-15",
  "status": "ACTIVE",
  "weightKg": 1547,
  "sortOrder": 4
}

# ─── TATA PUNCH EV variant listings ──────────────────────────────────────────

# VL-12 — Punch EV Smart + Standard 25 kWh + 3.3 kW AC
POST /variant-listings
{
  "modelId": "{punchEvModelId}",
  "trimId": "{smartTrimId}",
  "batteryPackId": "{bp5StandardRangeId}",
  "chargingConfigurationId": "{cc5_33kwAcId}",
  "priceInr": 999000,
  "launchDate": "2024-01-17",
  "status": "ACTIVE",
  "weightKg": 1315,
  "sortOrder": 1
}

# VL-13 — Punch EV Smart + Standard 25 kWh + 25 kW DC
POST /variant-listings
{
  "modelId": "{punchEvModelId}",
  "trimId": "{smartTrimId}",
  "batteryPackId": "{bp5StandardRangeId}",
  "chargingConfigurationId": "{cc7_25kwDcId}",
  "priceInr": 1199000,
  "launchDate": "2024-01-17",
  "status": "ACTIVE",
  "weightKg": 1325,
  "sortOrder": 2
}

# VL-14 — Punch EV Empowered+ + Long Range 35 kWh + 25 kW DC
POST /variant-listings
{
  "modelId": "{punchEvModelId}",
  "trimId": "{empoweredPlusTrimId}",
  "batteryPackId": "{bp6LongRangeId}",
  "chargingConfigurationId": "{cc7_25kwDcId}",
  "priceInr": 1499000,
  "launchDate": "2024-01-17",
  "status": "ACTIVE",
  "weightKg": 1380,
  "sortOrder": 3
}

# ─── MAHINDRA XEV 9e variant listings ────────────────────────────────────────

# VL-15 — XEV 9e Pack One + 59 kWh + 11.2 kW AC
POST /variant-listings
{
  "modelId": "{xev9eModelId}",
  "trimId": "{packOneTrimId}",
  "batteryPackId": "{bp9_59kwhId}",
  "chargingConfigurationId": "{cc10_112kwAcId}",
  "priceInr": 2199000,
  "launchDate": "2024-11-19",
  "status": "ACTIVE",
  "weightKg": 2155,
  "sortOrder": 1
}

# VL-16 — XEV 9e Pack Two + 79 kWh + 175 kW DC
POST /variant-listings
{
  "modelId": "{xev9eModelId}",
  "trimId": "{packTwoTrimId}",
  "batteryPackId": "{bp10_79kwhId}",
  "chargingConfigurationId": "{cc11_175kwDcId}",
  "priceInr": 2699000,
  "launchDate": "2024-11-19",
  "status": "ACTIVE",
  "weightKg": 2210,
  "sortOrder": 2
}

# VL-17 — XEV 9e Pack Three + 79 kWh + 175 kW DC
POST /variant-listings
{
  "modelId": "{xev9eModelId}",
  "trimId": "{packThreeTrimId}",
  "batteryPackId": "{bp10_79kwhId}",
  "chargingConfigurationId": "{cc11_175kwDcId}",
  "priceInr": 2999000,
  "launchDate": "2024-11-19",
  "status": "ACTIVE",
  "weightKg": 2225,
  "sortOrder": 3
}

# ─── HYUNDAI CRETA ELECTRIC variant listings ──────────────────────────────────

# VL-18 — Creta Electric Executive + 42 kWh + 11 kW AC
POST /variant-listings
{
  "modelId": "{cretaElectricModelId}",
  "trimId": "{executiveTrimId}",
  "batteryPackId": "{bp12_42kwhId}",
  "chargingConfigurationId": "{cc14_11kwAcId}",
  "priceInr": 1799000,
  "launchDate": "2024-01-17",
  "status": "ACTIVE",
  "weightKg": 1680,
  "sortOrder": 1
}

# VL-19 — Creta Electric Excellence + 51.4 kWh + 50 kW DC
POST /variant-listings
{
  "modelId": "{cretaElectricModelId}",
  "trimId": "{excellenceTrimId}",
  "batteryPackId": "{bp13_514kwhId}",
  "chargingConfigurationId": "{cc15_50kwDcId}",
  "priceInr": 2099000,
  "launchDate": "2024-01-17",
  "status": "ACTIVE",
  "weightKg": 1720,
  "sortOrder": 2
}

# VL-20 — Creta Electric Excellence S + 51.4 kWh + 50 kW DC
POST /variant-listings
{
  "modelId": "{cretaElectricModelId}",
  "trimId": "{excellenceSTrimId}",
  "batteryPackId": "{bp13_514kwhId}",
  "chargingConfigurationId": "{cc15_50kwDcId}",
  "priceInr": 2299000,
  "launchDate": "2024-01-17",
  "status": "ACTIVE",
  "weightKg": 1735,
  "sortOrder": 3
}

# ─── KIA EV6 variant listings ─────────────────────────────────────────────────

# VL-21 — EV6 GT Line + 58 kWh Standard + 220 kW DC
POST /variant-listings
{
  "modelId": "{ev6ModelId}",
  "trimId": "{gtLineTrimId}",
  "batteryPackId": "{bp15_58kwhId}",
  "chargingConfigurationId": "{cc17_220kwDcId}",
  "priceInr": 6095000,
  "launchDate": "2022-06-02",
  "status": "ACTIVE",
  "weightKg": 2125,
  "sortOrder": 1
}

# VL-22 — EV6 GT Line Plus + 77.4 kWh Long Range + 220 kW DC (AWD)
POST /variant-listings
{
  "modelId": "{ev6ModelId}",
  "trimId": "{gtLinePlusTrimId}",
  "batteryPackId": "{bp16_774kwhId}",
  "chargingConfigurationId": "{cc17_220kwDcId}",
  "priceInr": 6595000,
  "launchDate": "2022-06-02",
  "status": "ACTIVE",
  "weightKg": 2235,
  "sortOrder": 2
}


# ═══════════════════════════════════════════════════════════════════════════════
# GROUP 5: COMPATIBILITY CHECK
# (All GET — no body needed. Shown with example UUIDs for reference)
# ═══════════════════════════════════════════════════════════════════════════════

# Check if Tiago EV XZ+ 24 kWh 7.2 kW AC can charge at a specific station
GET /compatibility/vehicle/{tiagoXzPlus72kwVariantId}/station/{stationId}

# Check if Tiago EV XE 19.2 kWh 3.3 kW AC is compatible with TYPE2 connector
GET /compatibility/vehicle/{tiagoXe33kwVariantId}/connector/TYPE2

# Check the same variant against a specific 7.2 kW station connector
GET /compatibility/vehicle/{tiagoXe33kwVariantId}/connector/TYPE2?stationMaxWattage=7200

# Find all vehicles compatible with a CCS2 50 kW station
GET /compatibility/station/{50kwCcs2StationId}/vehicles

# Find all vehicles that support CCS2 connector (with minimum 50 kW achievable)
GET /compatibility/connector/CCS2/vehicles?maxWattage=50000

# Bulk check — POST /compatibility/bulk-check (used by Reservation service)
POST /compatibility/bulk-check
{
  "pairs": [
    {
      "variantListingId": "{tiagoXzPlus72kwVariantId}",
      "stationId": "{bangaloreMg22kwStationId}"
    },
    {
      "variantListingId": "{nexonEvEmpowered50kwVariantId}",
      "stationId": "{bangaloreMg22kwStationId}"
    },
    {
      "variantListingId": "{xev9ePack3VariantId}",
      "stationId": "{tataPowerDc150kwStationId}"
    },
    {
      "variantListingId": "{ev6GtLinePlusVariantId}",
      "stationId": "{tataPowerDc150kwStationId}"
    },
    {
      "variantListingId": "{tiagoXe33kwVariantId}",
      "stationId": "{tataPowerDc150kwStationId}"
    }
  ]
}

# Expected response for the bulk check above:
# Pair 1 (Tiago XZ+ 7.2kW vs 22kW TYPE2 station): COMPATIBLE — maxAchievable 7200W, ~126 min
# Pair 2 (Nexon EV 50kW DC vs 22kW TYPE2 station): COMPATIBLE via AC — maxAchievable 7200W
# Pair 3 (XEV 9e 175kW DC vs 150kW CCS2 station): COMPATIBLE — maxAchievable 150000W, ~23 min
# Pair 4 (EV6 220kW DC vs 150kW CCS2 station):    COMPATIBLE — maxAchievable 150000W
# Pair 5 (Tiago XE 3.3kW vs DC-only station):     INCOMPATIBLE — vehicle supports [TYPE2], station has [CCS2]


# ═══════════════════════════════════════════════════════════════════════════════
# GROUP 6: CUSTOMER GARAGE
# POST /garage/vehicles (JWT required — userId extracted from token)
# ═══════════════════════════════════════════════════════════════════════════════

# Add Tiago EV XZ+ 24 kWh 7.2 kW AC to garage (first vehicle — auto set as primary)
POST /garage/vehicles
Authorization: Bearer <customer_jwt_token>
{
  "variantListingId": "{tiagoXzPlus72kwVariantId}",
  "nickname": "My White Tiago",
  "registrationNumber": "TS09EF1234",
  "purchaseYear": 2024,
  "isPrimary": true
}

# Add Nexon EV Empowered as secondary vehicle
POST /garage/vehicles
Authorization: Bearer <customer_jwt_token>
{
  "variantListingId": "{nexonEvEmpoweredVariantId}",
  "nickname": "Family SUV",
  "registrationNumber": "AP28CD5678",
  "purchaseYear": 2023,
  "isPrimary": false
}

# Add Ather 450X (two-wheeler) to garage
POST /garage/vehicles
Authorization: Bearer <customer_jwt_token>
{
  "variantListingId": "{ather450xVariantId}",
  "nickname": "Office Scooter",
  "registrationNumber": "KA03MN9012",
  "purchaseYear": 2024,
  "isPrimary": false
}

# Update vehicle nickname and registration
PUT /garage/vehicles/{customerVehicleId}
Authorization: Bearer <customer_jwt_token>
{
  "nickname": "My Red Nexon",
  "registrationNumber": "AP28CD5678",
  "purchaseYear": 2023,
  "isPrimary": null
}

# Promote the Nexon to primary vehicle
PUT /garage/vehicles/{nexonCustomerVehicleId}/set-primary
Authorization: Bearer <customer_jwt_token>
(no body required)

# Get compatible stations for the Tiago in the garage
GET /garage/vehicles/{tiagoCustomerVehicleId}/compatible-stations
Authorization: Bearer <customer_jwt_token>

# Admin view — get all vehicles for a specific user
GET /garage/vehicles/admin?userId={specificUserId}&page=0&size=20&sort=addedAt
Authorization: Bearer <admin_jwt_token>


# ═══════════════════════════════════════════════════════════════════════════════
# STATION CONNECTORS — sync from Station Management service
# These would normally come via Kafka events, but can be created directly for testing
# POST (internal endpoint not in public API — admin use only)
# ═══════════════════════════════════════════════════════════════════════════════

# Station: Tata Power EV — Indiranagar Bangalore (22 kW AC)
# (This would be published by Station Management service)
{
  "stationId": "{bangaloreTataPowerStationId}",
  "connectorType": "TYPE2",
  "currentType": "AC",
  "maxWattage": 22000,
  "isOperational": true
}

# Station: Tata Power EV — Forum Mall Hyderabad (150 kW CCS2 DC)
{
  "stationId": "{hyderabadForumMallStationId}",
  "connectorType": "CCS2",
  "currentType": "DC",
  "maxWattage": 150000,
  "isOperational": true
}

# Station: ChargeZone — Hitec City Hyderabad (dual: 7.2 kW TYPE2 AC + 50 kW CCS2 DC)
{
  "stationId": "{hyderabadHitecCityStationId}",
  "connectorType": "TYPE2",
  "currentType": "AC",
  "maxWattage": 7200,
  "isOperational": true
}
{
  "stationId": "{hyderabadHitecCityStationId}",
  "connectorType": "CCS2",
  "currentType": "DC",
  "maxWattage": 50000,
  "isOperational": true
}

# Station: Fortum — Delhi Airport T3 (350 kW CCS2 ultra-fast DC)
{
  "stationId": "{delhiAirportT3StationId}",
  "connectorType": "CCS2",
  "currentType": "DC",
  "maxWattage": 350000,
  "isOperational": true
}
