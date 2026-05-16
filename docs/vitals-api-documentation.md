# Vitals API Documentation

## Overview
The Vitals API now supports all 22 vital metrics as defined in the VitalMetricMaster table, including both body composition and clinical vitals.

## Endpoints

### 1. Record Patient Vitals
**POST** `/api/patients/{patientId}/vitals`

**Authorization:** NURSE, DOCTOR roles required

**Request Body:**
```json
{
  // Body Composition Fields (optional)
  "height": 170.5,
  "weight": 70.2,
  "bmi": 24.3,
  "bodyFatPercentage": 15.8,
  "bodyFatMass": 11.1,
  "skeletalMusclePercentage": 42.5,
  "bodyWaterPercentage": 58.2,
  "totalMoisture": 40.8,
  "extracellularWaterPct": 38.5,
  "intracellularWaterPct": 61.5,
  "basalMetabolism": 1650,
  "visceralFatLevel": 8.5,
  "protein": 12.5,
  "mineral": 3.2,
  "bodyAge": 25,
  "overall": 85,
  
  // Clinical Vitals Fields (optional)
  "temperature": 36.8,
  "systolic": 120,
  "diastolic": 80,
  "bpHeartRate": 72,
  "spo2": 98,
  "spo2HeartRate": 75,
  
  "notes": "Patient feeling well, all vitals within normal range"
}
```

**Response:**
```json
{
  "id": 123,
  // Body Composition Fields
  "height": 170.5,
  "weight": 70.2,
  "bmi": 24.3,
  "bodyFatPercentage": 15.8,
  "bodyFatMass": 11.1,
  "skeletalMusclePercentage": 42.5,
  "bodyWaterPercentage": 58.2,
  "totalMoisture": 40.8,
  "extracellularWaterPct": 38.5,
  "intracellularWaterPct": 61.5,
  "basalMetabolism": 1650,
  "visceralFatLevel": 8.5,
  "protein": 12.5,
  "mineral": 3.2,
  "bodyAge": 25,
  "overall": 85,
  
  // Clinical Vitals Fields
  "temperature": 36.8,
  "systolic": 120,
  "diastolic": 80,
  "bpHeartRate": 72,
  "spo2": 98,
  "spo2HeartRate": 75,
  
  "notes": "Patient feeling well, all vitals within normal range",
  "hasAlert": false,
  "alertResolved": true,
  "patientId": 456,
  "recordedAt": "2024-01-15T10:30:00"
}
```

### 2. Get Patient Vitals History
**GET** `/api/patients/{patientId}/vitals`

**Response:** Array of VitalResponse objects (same structure as above)

### 3. Get Latest Patient Vitals
**GET** `/api/patients/{patientId}/vitals/latest`

**Response:** Single VitalResponse object (same structure as above)

### 4. Get Active Vital Metrics Configuration
**GET** `/api/patients/{patientId}/vitals/metrics`

**Response:**
```json
[
  {
    "id": 17,
    "deviceId": 117,
    "fieldKey": "temperature",
    "displayName": "Temperature",
    "unit": "°C",
    "category": "CLINICAL",
    "mandatory": true,
    "display": true,
    "lowValue": 36.1,
    "highValue": 37.2,
    "normalRange": "36.1 - 37.2",
    "sortOrder": 17,
    "active": true
  },
  {
    "id": 18,
    "deviceId": 118,
    "fieldKey": "systolic",
    "displayName": "Systolic BP",
    "unit": "mmHg",
    "category": "CLINICAL",
    "mandatory": true,
    "display": true,
    "lowValue": 90.0,
    "highValue": 139.0,
    "normalRange": "90 - 139",
    "sortOrder": 18,
    "active": true
  }
]
```

## Alert System

### Dynamic Alert Generation
The system now dynamically generates alerts based on the VitalMetricMaster configuration:

1. **Range Checking:** Each vital metric is checked against its configured `lowValue` and `highValue`
2. **Severity Assignment:** 
   - CLINICAL category metrics → HIGH severity alerts
   - BODY_COMPOSITION category metrics → MEDIUM severity alerts
3. **Alert Messages:** Auto-generated with metric name, actual value, and normal range

### Alert Examples
```json
{
  "type": "VITALS",
  "metric": "Systolic BP",
  "actualValue": "150",
  "message": "Above normal (> 139 mmHg)",
  "severity": "HIGH"
}
```

## Field Categories

### Body Composition (16 fields)
- height, weight, bmi, bodyFatPercentage, bodyFatMass
- skeletalMusclePercentage, bodyWaterPercentage, totalMoisture
- extracellularWaterPct, intracellularWaterPct, basalMetabolism
- visceralFatLevel, protein, mineral, bodyAge, overall

### Clinical Vitals (6 fields)
- temperature, systolic, diastolic, bpHeartRate, spo2, spo2HeartRate

## Data Types
- **BigDecimal fields:** All measurements with decimal precision
- **Integer fields:** Whole number measurements (heart rates, ages, scores)
- **All fields are optional** - submit only the metrics you have data for

## Validation
- Values are validated against the VitalMetricMaster configuration
- Out-of-range values trigger automatic alerts
- Mandatory fields (if configured) should be provided for complete vital records

## Usage Notes
1. **Flexible Input:** Send only the vital metrics you have data for
2. **Dynamic Configuration:** UI should call `/metrics` endpoint to know which fields to display
3. **Alert Integration:** Alerts are automatically generated and can be viewed via the Alert API
4. **Audit Trail:** All vital recordings are automatically audited