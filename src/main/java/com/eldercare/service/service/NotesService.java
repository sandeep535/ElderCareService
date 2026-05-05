package com.eldercare.service.service;

import com.eldercare.service.dto.NotesRequest;
import com.eldercare.service.dto.NotesResponse;
import com.eldercare.service.dto.UserInfoResponse;
import com.eldercare.service.entity.NotesEntity;
import com.eldercare.service.entity.PatientEntity;
import com.eldercare.service.entity.PatientJourneyEntity;
import com.eldercare.service.entity.UserEntity;
import com.eldercare.service.exception.ElderCareException;
import com.eldercare.service.exception.ResourceNotFoundException;
import com.eldercare.service.repository.MasterTableRepository;
import com.eldercare.service.repository.MasterTableRepository;
import com.eldercare.service.repository.NotesRepository;
import com.eldercare.service.repository.PatientJourneyRepository;
import com.eldercare.service.repository.PatientRepository;
import com.eldercare.service.repository.UserDetailsRepository;
import com.eldercare.service.repository.UserRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotesService {

    private static final Logger log = LogManager.getLogger(NotesService.class);

    private final NotesRepository notesRepository;
    private final PatientRepository patientRepository;
    private final PatientJourneyRepository journeyRepository;
    private final UserRepository userRepository;
    private final UserDetailsRepository userDetailsRepository;
    private final MasterTableRepository masterTableRepository;
    private final AuditService auditService;

    public NotesService(NotesRepository notesRepository,
                        PatientRepository patientRepository,
                        PatientJourneyRepository journeyRepository,
                        UserRepository userRepository,
                        UserDetailsRepository userDetailsRepository,
                        MasterTableRepository masterTableRepository,
                        AuditService auditService) {
        this.notesRepository = notesRepository;
        this.patientRepository = patientRepository;
        this.journeyRepository = journeyRepository;
        this.userRepository = userRepository;
        this.userDetailsRepository = userDetailsRepository;
        this.masterTableRepository = masterTableRepository;
        this.auditService = auditService;
    }

    @Transactional
    public NotesResponse add(Long patientId, NotesRequest request) {
        PatientEntity patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", patientId));

        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();

        NotesEntity note = new NotesEntity();
        note.setPatient(patient);
        note.setNotes(request.notes());
        note.setCreatedBy(currentUser);
        notesRepository.save(note);

        NotesResponse response = toResponse(note);
        auditService.record(patientId, "NOTES", response);

        PatientJourneyEntity journey = journeyRepository.findByPatientId(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient journey", patientId));
        if (!journey.isNote()) {
            journey.setNote(true);
            journey.setUpdatedBy(currentUser);
            journeyRepository.save(journey);
        }

        log.info("Note added for patient {} by {}", patientId, currentUser);
        return response;
    }

    @Transactional
    public NotesResponse update(Long patientId, Long noteId, NotesRequest request) {
        NotesEntity note = notesRepository.findById(noteId)
                .orElseThrow(() -> new ResourceNotFoundException("Note", noteId));

        if (!note.getPatient().getId().equals(patientId)) {
            throw new ResourceNotFoundException("Note", noteId);
        }

        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        note.setNotes(request.notes());
        note.setUpdatedBy(currentUser);
        notesRepository.save(note);

        log.info("Note {} updated for patient {} by {}", noteId, patientId, currentUser);
        return toResponse(note);
    }

    public List<NotesResponse> getByPatient(Long patientId) {
        if (!patientRepository.existsById(patientId)) {
            throw new ResourceNotFoundException("Patient", patientId);
        }
        return notesRepository.findByPatientIdOrderByCreatedOnDesc(patientId)
                .stream().map(this::toResponse).toList();
    }

    private NotesResponse toResponse(NotesEntity n) {
        UserEntity user = userRepository.findByUsername(n.getCreatedBy()).orElse(null);
        UserInfoResponse userInfo = user != null ? buildUserInfo(user) : null;

        String noteTypeDisplay = null;
        if (n.getNoteType() != null) {
            noteTypeDisplay = masterTableRepository
                    .findByTypeAndLookupCode("NOTES_TYPE", n.getNoteType())
                    .map(m -> m.getLookupItem())
                    .orElse(n.getNoteType());
        }

        return new NotesResponse(n.getId(), n.getNotes(), n.getNoteType(), noteTypeDisplay,
                n.getPatient().getId(), userInfo, n.getCreatedOn());
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
}
