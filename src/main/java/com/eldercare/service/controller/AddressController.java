package com.eldercare.service.controller;

import com.eldercare.service.dto.AddressRequest;
import com.eldercare.service.dto.AddressResponse;
import com.eldercare.service.service.AddressService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('NURSE', 'DOCTOR')")
    public ResponseEntity<AddressResponse> save(@Valid @RequestBody AddressRequest request) {
        return ResponseEntity.ok(addressService.save(request));
    }

    @GetMapping("/{type}/{typeId}")
    public ResponseEntity<AddressResponse> get(@PathVariable String type,
                                               @PathVariable Long typeId) {
        return ResponseEntity.ok(addressService.getByTypeAndTypeId(type, typeId));
    }
}
