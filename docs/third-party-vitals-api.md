# Third-Party Vitals Integration API

## Overview
This API allows third-party devices and systems to submit vital measurements that will be automatically mapped to the internal vital tracking system.

## Device ID Mapping

| Device ID | Field Name | Measurement | Unit |
|-----------|------------|-------------|------|
| 1 | height | Height | cm |
| 2 | weight | Weight | kg |
| 3 | bmi | BMI | N.A. |
| 4 | bodyFatPercentage | Body Fat Percentage | % |
| 5 | bodyFatMass | Body Fat Mass | kg |
| 6 | skeletalMusclePercentage | Skeletal Muscle Percentage | % |
| 7 | bodyWaterPercentage | Body Water Percentage | % |
| 8 | totalMoisture | Total Moisture | kg |
| 9 | extracellularWaterPct | Extracellular Water % | % |
| 10 | intracellularWaterPct | Intracellular Water % | % |
| 11 | basalMetabolism | Basal Metabolism | kcal |
| 12 | visceralFatLevel | Visceral Fat Level | N.A. |
| 13 | protein | Protein | kg |
| 14 | mineral | Mineral | kg |
| 15 | bodyAge | Body Age | N.A. |
| 16 | overall | Overall Score | N.A. |
| 17 | temperature | Temperature | °C |
| 18 | systolic | Systolic BP | mmHg |
| 19 | diastolic | Diastolic BP | mmHg |
| 20 | bpHeartRate | BP Heart Rate | bpm |
| 21 | spo2 | SpO2 | % |
| 22 | spo2HeartRate | SpO2 Heart Rate | bpm |

## Endpoints

### 1. Submit Single Patient Vitals

**POST** `/api/third-party/vitals`

**Request Body:**
```json
{
  "timestamp": "2025-11-12T03:41:45.329019Z",
  "patient_id": "PATIENT_001",
  "readings": [
    {"device_id": 1, "value": 170, "units": "cm"},
    {"device_id": 2, "value": 70.5, "units": "kg"},
    {"device_id": 3, "value": 24.4, "units": "N.A."},
    {"device_id": 4, "value": 18.2, "units": "%"},
    {"device_id": 5, "value": 12.8, "units": "kg"},
    {"device_id": 6, "value": 45.3, "units": "%"},
    {"device_id": 7, "value": 55.1, "units": "%"},
    {"device_id": 8, "value": 38.9, "units": "kg"},
    {"device_id": 9, "value": 42.0, "units": "%"},
    {"device_id": 10, "value": 58.0, "units": "%"},
    {"device_id": 11, "value": 1600, "units": "kcal"},
    {"device_id": 12, "value": 8, "units": "N.A."},
    {"device_id": 13, "value": 10.5, "units": "kg"},
    {"device_id": 14, "value": 3.2, "units": "kg"},
    {"device_id": 15, "value": 32, "units": "N.A."},
    {"device_id": 16, "value": 85, "units": "N.A."},
    {"device_id": 17, "value": 36.6, "units": "°C"},
    {"device_id": 18, "value": 120, "units": "mmHg"},
    {"device_id": 19, "value": 80, "units": "mmHg"},
    {"device_id": 20, "value": 72, "units": "bpm"},
    {"device_id": 21, "value": 98, "units": "%"},
    {"device_id": 22, "value": 70, "units": "bpm"}
  ]
}
```

**Response (200 OK):**
```json
{
  "id": 123,
  "height": 170.0,
  "weight": 70.5,
  "bmi": 24.4,
  "bodyFatPercentage": 18.2,
  "bodyFatMass": 12.8,
  "skeletalMusclePercentage": 45.3,
  "bodyWaterPercentage": 55.1,
  "totalMoisture": 38.9,
  "extracellularWaterPct": 42.0,
  "intracellularWaterPct": 58.0,
  "basalMetabolism": 1600,
  "visceralFatLevel": 8.0,
  "protein": 10.5,
  "mineral": 3.2,
  "bodyAge": 32,
  "overall": 85,
  "temperature": 36.6,
  "systolic": 120,
  "diastolic": 80,
  "bpHeartRate": 72,
  "spo2": 98,
  "spo2HeartRate": 70,
  "notes": "Third-party vitals recorded at 2025-11-12T03:41:45.329019Z",
  "hasAlert": false,
  "alertResolved": true,
  "patientId": 456,
  "recordedAt": "2025-11-12T03:41:45.329019Z"
}
```

### 2. Submit Batch Vitals

**POST** `/api/third-party/vitals/batch`

**Request Body:**
```json
[
  {
    "timestamp": "2025-11-12T03:41:45.329019Z",
    "patient_id": "PATIENT_001",
    "readings": [
      {"device_id": 17, "value": 36.6, "units": "°C"},
      {"device_id": 18, "value": 120, "units": "mmHg"},
      {"device_id": 19, "value": 80, "units": "mmHg"}
    ]
  },
  {
    "timestamp": "2025-11-12T03:42:00.000000Z",
    "patient_id": "PATIENT_002",
    "readings": [
      {"device_id": 17, "value": 37.1, "units": "°C"},
      {"device_id": 21, "value": 95, "units": "%"}
    ]
  }
]
```

**Response (200 OK):**
```json
"Processed: 2, Failed: 0"
```

## Example Use Cases

### 1. Body Composition Scanner Data
```json
{
  "timestamp": "2025-11-12T10:30:00.000Z",
  "patient_id": "EC-2026-0001",
  "readings": [
    {"device_id": 1, "value": 168.5, "units": "cm"},
    {"device_id": 2, "value": 72.3, "units": "kg"},
    {"device_id": 3, "value": 25.5, "units": "N.A."},
    {"device_id": 4, "value": 20.1, "units": "%"},
    {"device_id": 7, "value": 57.8, "units": "%"},
    {"device_id": 12, "value": 9.2, "units": "N.A."}
  ]
}
```

### 2. Blood Pressure Monitor Data
```json
{
  "timestamp": "2025-11-12T14:15:30.000Z",
  "patient_id": "EC-2026-0002",
  "readings": [
    {"device_id": 18, "value": 135, "units": "mmHg"},
    {"device_id": 19, "value": 85, "units": "mmHg"},
    {"device_id": 20, "value": 78, "units": "bpm"}
  ]
}
```

### 3. Pulse Oximeter Data
```json
{
  "timestamp": "2025-11-12T16:45:00.000Z",
  "patient_id": "EC-2026-0003",
  "readings": [
    {"device_id": 21, "value": 97, "units": "%"},
    {"device_id": 22, "value": 72, "units": "bpm"}
  ]
}
```

### 4. Thermometer Data
```json
{
  "timestamp": "2025-11-12T08:00:00.000Z",
  "patient_id": "EC-2026-0001",
  "readings": [
    {"device_id": 17, "value": 36.8, "units": "°C"}
  ]
}
```

## cURL Examples

### Single Patient Vitals
```bash
curl -X POST "http://localhost:8083/api/third-party/vitals" \
  -H "Content-Type: application/json" \
  -d '{
    "timestamp": "2025-11-12T03:41:45.329019Z",
    "patient_id": "PATIENT_001",
    "readings": [
      {"device_id": 17, "value": 36.8, "units": "°C"},
      {"device_id": 18, "value": 120, "units": "mmHg"},
      {"device_id": 19, "value": 80, "units": "mmHg"},
      {"device_id": 21, "value": 98, "units": "%"}
    ]
  }'
```

### Batch Vitals
```bash
curl -X POST "http://localhost:8083/api/third-party/vitals/batch" \
  -H "Content-Type: application/json" \
  -d '[
    {
      "timestamp": "2025-11-12T03:41:45.329019Z",
      "patient_id": "PATIENT_001",
      "readings": [
        {"device_id": 17, "value": 36.8, "units": "°C"}
      ]
    }
  ]'
```

## Error Responses

### Patient Not Found (404)
```json
{
  "timestamp": "2025-11-12T03:41:45.329019Z",
  "status": 404,
  "error": "Not Found",
  "message": "Patient not found with external ID: PATIENT_001",
  "path": "/api/third-party/vitals"
}
```

### Invalid Data (400)
```json
{
  "timestamp": "2025-11-12T03:41:45.329019Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid vital data format",
  "path": "/api/third-party/vitals"
}
```

## Notes

1. **Patient Identification**: The `patient_id` should match the `patient_number` field in your patient records
2. **Flexible Readings**: You can send any combination of device readings - not all 22 are required
3. **Automatic Alerts**: The system will automatically generate alerts if values are outside normal ranges
4. **Data Validation**: Values are automatically converted to appropriate data types (Integer/BigDecimal)
5. **Audit Trail**: All submissions are logged and audited
6. **Unknown Devices**: Unknown device_ids are logged but ignored (won't cause errors)

## Integration Steps

1. **Map your device IDs** to the standard device ID mapping above
2. **Format your data** according to the request structure
3. **Submit to the endpoint** using POST requests
4. **Handle responses** and error cases appropriately
5. **Monitor logs** for any mapping issues or errors