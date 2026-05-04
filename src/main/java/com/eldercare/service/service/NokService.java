package com.eldercare.service.service;

import com.eldercare.service.dto.NokRequest;
import com.eldercare.service.dto.NokResponse;
import com.eldercare.service.entity.NokEntity;
import com.eldercare.service.entity.PatientEntity;
import com.eldercare.service.exception.ElderCareException;
import com.eldercare.service.exception.ResourceNotFoundException;
import com.eldercare.service.repository.NokRepository;
import com.eldercare.service.repository.PatientRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NokService {

    private static final Logger log = LogManager.getLogger(NokService.class);

    private final NokRepository nokRepository;
    private final PatientRepository patientRepository;

    public NokService(NokRepository nokRepository, PatientRepository patientRepository) {
        this.nokRepository = nokRepository;
        this.patientRepository = patientRepository;
    }

    @Transactional
    public NokResponse add(Long patientId, NokRequest request) {
        PatientEntity patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", patientId));

        if (request.primaryContact() && nokRepository.existsByPatientIdAndPrimaryContactTrue(patientId)) {
            throw new ElderCareException("A primary contact already exists for this patient. Update the existing one first.");
        }

        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();

        NokEntity nok = new NokEntity();
        nok.setPatient(patient);
        nok.setFirstName(request.firstName());
        nok.setLastName(request.lastName());
        nok.setRelationship(request.relationship());
        nok.setDob(request.dob());
        nok.setGender(request.gender());
        nok.setPhoneNumber(request.phoneNumber());
        nok.setEmail(request.email());
        nok.setPrimaryContact(request.primaryContact());
        nok.setCanMakeMedical(request.canMakeMedical());
        nok.setNotes(request.notes());
        nok.setCreatedBy(currentUser);
        nokRepository.save(nok);

        log.info("NOK added for patient {} by {}", patientId, currentUser);
        return toResponse(nok);
    }

    public List<NokResponse> getByPatient(Long patientId) {
        if (!patientRepository.existsById(patientId)) {
            throw new ResourceNotFoundException("Patient", patientId);
        }
        return nokRepository.findByPatientId(patientId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public NokResponse update(Long patientId, Long nokId, NokRequest request) {
        NokEntity nok = nokRepository.findById(nokId)
                .orElseThrow(() -> new ResourceNotFoundException("NOK", nokId));

        if (!nok.getPatient().getId().equals(patientId)) {
            throw new ElderCareException("NOK does not belong to this patient");
        }

        if (request.primaryContact() && !nok.isPrimaryContact()
                && nokRepository.existsByPatientIdAndPrimaryContactTrue(patientId)) {
            throw new ElderCareException("A primary contact already exists for this patient.");
        }

        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        nok.setFirstName(request.firstName());
        nok.setLastName(request.lastName());
        nok.setRelationship(request.relationship());
        nok.setDob(request.dob());
        nok.setGender(request.gender());
        nok.setPhoneNumber(request.phoneNumber());
        nok.setEmail(request.email());
        nok.setPrimaryContact(request.primaryContact());
        nok.setCanMakeMedical(request.canMakeMedical());
        nok.setNotes(request.notes());
        nok.setUpdatedBy(currentUser);
        nokRepository.save(nok);

        return toResponse(nok);
    }

    private NokResponse toResponse(NokEntity n) {
        return new NokResponse(n.getId(), n.getFirstName(), n.getLastName(),
                n.getRelationship(), n.getDob(), n.getGender(),
                n.getPhoneNumber(), n.getEmail(), n.isPrimaryContact(),
                n.isCanMakeMedical(), n.getNotes(), n.getPatient().getId());
    }
}
