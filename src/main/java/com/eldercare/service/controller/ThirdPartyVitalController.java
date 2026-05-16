package com.eldercare.service.controller;

import com.eldercare.service.dto.ThirdPartyVitalRequest;
import com.eldercare.service.dto.VitalResponse;
import com.eldercare.service.service.ThirdPartyVitalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/third-party")
public class ThirdPartyVitalController {

    private final ThirdPartyVitalService thirdPartyVitalService;

    public ThirdPartyVitalController(ThirdPartyVitalService thirdPartyVitalService) {
        this.thirdPartyVitalService = thirdPartyVitalService;
    }

    @PostMapping("/vitals")
    public ResponseEntity<VitalResponse> receiveVitals(@RequestBody ThirdPartyVitalRequest request) {
        VitalResponse response = thirdPartyVitalService.processThirdPartyVitals(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/vitals/batch")
    public ResponseEntity<String> receiveVitalsBatch(@RequestBody ThirdPartyVitalRequest[] requests) {
        int processed = 0;
        int failed = 0;
        
        for (ThirdPartyVitalRequest request : requests) {
            try {
                thirdPartyVitalService.processThirdPartyVitals(request);
                processed++;
            } catch (Exception e) {
                failed++;
            }
        }
        
        return ResponseEntity.ok(String.format("Processed: %d, Failed: %d", processed, failed));
    }
}