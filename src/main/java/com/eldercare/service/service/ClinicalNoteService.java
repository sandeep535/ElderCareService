package com.eldercare.service.service;

import com.eldercare.service.dto.ClinicalNoteRequest;
import com.eldercare.service.dto.ClinicalNoteResponse;
import com.eldercare.service.dto.UserInfoResponse;
import com.eldercare.service.entity.ClinicalNoteEntity;
import com.eldercare.service.entity.MasterTableEntity;
import com.eldercare.service.entity.PatientEntity;
import com.eldercare.service.entity.UserEntity;
import com.eldercare.service.exception.ElderCareException;
import com.eldercare.service.exception.ResourceNotFoundException;
import com.eldercare.service.repository.ClinicalNoteRepository;
import com.eldercare.service.repository.MasterTableRepository;
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
    private final MasterTableRepository masterTableRepository;
    private final AuditService auditService;

    public ClinicalNoteService(ClinicalNoteRepository clinicalNoteRepository,
                               PatientRepository patientRepository,
                               UserRepository userRepository,
                               UserDetailsRepository userDetailsRepository,
                               MasterTableRepository masterTableRepository,
                               AuditService auditService) {
        this.clinicalNoteRepository = clinicalNoteRepository;
        this.patientRepository = patientRepository;
        this.userRepository = userRepository;
        this.userDetailsRepository = userDetailsRepository;
        this.masterTableRepository = masterTableRepository;
        this.auditService = auditService;
    }

    @Transactional
    public ClinicalNoteResponse add(Long patientId, ClinicalNoteRequest request) {
        PatientEntity patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", patientId));

        MasterTableEntity notesType = masterTableRepository.findById(request.notesTypeId())
                .orElseThrow(() -> new ElderCareException("Invalid notes type id: " + request.notesTypeId()));
        MasterTableEntity priority = masterTableRepository.findById(request.priorityId())
                .orElseThrow(() -> new ElderCareException("Invalid priority id: " + request.priorityId()));

        UserEntity recordedBy = request.recordedById() != null
                ? userRepository.findById(request.recordedById())
                        .orElseThrow(() -> new ResourceNotFoundException("User", request.recordedById()))
                : resolveCurrentUser();

        String currentUser = resolveCurrentUserName();

        ClinicalNoteEntity note = new ClinicalNoteEntity();
        note.setPatient(patient);
        note.setNoteTitle(request.noteTitle());
        note.setNoteDate(request.noteDate() != null ? request.noteDate() : java.time.LocalDateTime.now());
        note.setNotes(request.notes());
        note.setNotesType(notesType.getLookupCode());
        note.setPriority(priority.getLookupCode());
        note.setRecordedBy(recordedBy);
        note.setCreatedBy(currentUser);
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

        MasterTableEntity notesType = masterTableRepository.findById(request.notesTypeId())
                .orElseThrow(() -> new ElderCareException("Invalid notes type id: " + request.notesTypeId()));
        MasterTableEntity priority = masterTableRepository.findById(request.priorityId())
                .orElseThrow(() -> new ElderCareException("Invalid priority id: " + request.priorityId()));

        UserEntity recordedBy = request.recordedById() != null
                ? userRepository.findById(request.recordedById())
                        .orElseThrow(() -> new ResourceNotFoundException("User", request.recordedById()))
                : note.getRecordedBy();

        note.setNoteTitle(request.noteTitle());
        note.setNoteDate(request.noteDate() != null ? request.noteDate() : note.getNoteDate());
        note.setNotes(request.notes());
        note.setNotesType(notesType.getLookupCode());
        note.setPriority(priority.getLookupCode());
        note.setRecordedBy(recordedBy);
        note.setUpdatedBy(resolveCurrentUserName());
        clinicalNoteRepository.save(note);
        return toResponse(note);
    }

    private ClinicalNoteResponse toResponse(ClinicalNoteEntity entity) {
        UserEntity recorder = entity.getRecordedBy();
        UserInfoResponse recorderInfo = recorder != null ? buildUserInfo(recorder) : null;

        String notesTypeDisplay = masterTableRepository
                .findByTypeAndLookupCode("NOTES_TYPE", entity.getNotesType())
                .map(m -> m.getLookupItem()).orElse(entity.getNotesType());

        String priorityDisplay = masterTableRepository
                .findByTypeAndLookupCode("CLINICAL_NOTE_PRIORITY", entity.getPriority())
                .map(m -> m.getLookupItem()).orElse(entity.getPriority());

        return new ClinicalNoteResponse(entity.getId(), entity.getPatient().getId(),
                entity.getNoteTitle(), entity.getNoteDate(),
                entity.getNotes(),
                entity.getNotesType(), notesTypeDisplay,
                entity.getPriority(), priorityDisplay,
                recorderInfo, entity.getCreatedOn());
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
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
