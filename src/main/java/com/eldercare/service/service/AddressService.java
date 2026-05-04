package com.eldercare.service.service;

import com.eldercare.service.dto.AddressRequest;
import com.eldercare.service.dto.AddressResponse;
import com.eldercare.service.entity.AddressEntity;
import com.eldercare.service.exception.ResourceNotFoundException;
import com.eldercare.service.repository.AddressRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AddressService {

    private static final Logger log = LogManager.getLogger(AddressService.class);

    private final AddressRepository addressRepository;

    public AddressService(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    @Transactional
    public AddressResponse save(AddressRequest request) {
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        String type = request.type().toUpperCase();

        // Upsert — one address per type+typeId
        AddressEntity address = addressRepository
                .findByTypeAndTypeId(type, request.typeId())
                .orElse(new AddressEntity());

        boolean isNew = address.getId() == null;
        address.setAddress(request.address());
        address.setCity(request.city());
        address.setState(request.state());
        address.setZipCode(request.zipCode());
        address.setType(type);
        address.setTypeId(request.typeId());

        if (isNew) address.setCreatedBy(currentUser);
        else address.setUpdatedBy(currentUser);

        addressRepository.save(address);
        log.info("Address saved for type={} typeId={} by {}", type, request.typeId(), currentUser);
        return toResponse(address);
    }

    public AddressResponse getByTypeAndTypeId(String type, Long typeId) {
        return addressRepository.findByTypeAndTypeId(type.toUpperCase(), typeId)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Address not found for type " + type + " and id " + typeId));
    }

    private AddressResponse toResponse(AddressEntity a) {
        return new AddressResponse(a.getId(), a.getAddress(), a.getCity(),
                a.getState(), a.getZipCode(), a.getType(), a.getTypeId());
    }
}
