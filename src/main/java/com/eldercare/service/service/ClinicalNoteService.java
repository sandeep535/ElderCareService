package com.eldercare.service.service;

import com.eldercare.service.dto.ClinicalNoteRequest;
import com.eldercare.service.dto.ClinicalNoteResponse;
import com.eldercare.service.dto.UserInfoResponse;
import com.eldercare.service.entity.ClinicalNoteEntity;
import com.eldercare.service.entity.PatientEntity;
import com.eldercare.service.entity.UserEntity;
import com.eldercare.service.exception.ResourceNotFoundException;
import com.eldercare.service.repository.ClinicalNoteRepository;
import com.eldercare.service.repository.PatientRepository;
import com.eldercare.service.repository.UserDetailsRepository;
import com.eldercare.service.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClinicalNoteService {

    private final ClinicalNoteRepository clinicalNoteRepository;
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final UserDetailsRepository userDetailsRepository;
    private final AuditService auditService;

    public ClinicalNoteService(ClinicalNoteRepository clinicalNoteRepository,
                               PatientRepository patientRepository,
                               UserRepository userRepository,
                               UserDetailsRepository userDetailsRepository,
                               AuditService auditService) {
        this.clinicalNoteRepository = clinicalNoteRepository;
        this.patientRepository = patientRepository;
        this.userRepository = userRepository;
        this.userDetailsRepository = userDetailsRepository;
        this.auditService = auditService;
    }

    @Transactional
    public ClinicalNoteResponse add(Long patientId, ClinicalNoteRequest request) {
        PatientEntity patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", patientId));

        ClinicalNoteEntity note = new ClinicalNoteEntity();
        note.setPatient(patient);
        note.setNotes(request.notes());
        note.setNotesType(request.notesType());
        note.setPriority(request.priority());
        note.setRecordedBy(resolveCurrentUser());
        note.setCreatedBy(resolveCurrentUserName());
        clinicalNoteRepository.save(note);

        ClinicalNoteResponse response = toResponse(note);
        auditService.record(patientId, "CLINICAL_NOTE", response);
        return response;
    }

    public List<ClinicalNoteResponse> getByPatient(Long patientId) {
        return clinicalNoteRepository.findByPatientIdOrderByCreatedOnDesc(patientId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ClinicalNoteResponse update(Long patientId, Long noteId, ClinicalNoteRequest request) {
        ClinicalNoteEntity note = clinicalNoteRepository.findById(noteId)
                .orElseThrow(() -> new ResourceNotFoundException("Clinical note", noteId));
        if (!note.getPatient().getId().equals(patientId)) {
            throw new ResourceNotFoundException("Clinical note", noteId);
        }
        note.setNotes(request.notes());
        note.setNotesType(request.notesType());
        note.setPriority(request.priority());
        note.setUpdatedBy(resolveCurrentUserName());
        clinicalNoteRepository.save(note);
        return toResponse(note);
    }

    private ClinicalNoteResponse toResponse(ClinicalNoteEntity entity) {
        UserEntity recorder = entity.getRecordedBy();
        UserInfoResponse recorderInfo = recorder != null ? buildUserInfo(recorder) : null;
        return new ClinicalNoteResponse(entity.getId(), entity.getPatient().getId(), entity.getNotes(),
                entity.getNotesType(), entity.getPriority(),
                recorderInfo,
                entity.getCreatedOn());
    }

    private UserInfoResponse buildUserInfo(UserEntity user) {
        var userDetails = userDetailsRepository.findByUserId(user.getId()).orElse(null);
        return new UserInfoResponse(
                user.getId(),
                user.getUsername(),
                user.getUserType(),
                userDetails != null ? userDetails.getFirstName() : null,
                userDetails != null ? userDetails.getLastName() : null,
                userDetails != null ? userDetails.getEmail() : null,
                userDetails != null ? userDetails.getPhoneNumber() : null,
                userDetails != null ? userDetails.getDesignation() : null
        );
    }

    private UserEntity resolveCurrentUser() {
        return userRepository.findByUsername(resolveCurrentUserName()).orElse(null);
    }

    private String resolveCurrentUserName() {
        try {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            return "system";
        }
    }
}
