package com.eldercare.service.service;

import com.eldercare.service.dto.NotesRequest;
import com.eldercare.service.dto.NotesResponse;
import com.eldercare.service.entity.NotesEntity;
import com.eldercare.service.entity.PatientEntity;
import com.eldercare.service.entity.PatientJourneyEntity;
import com.eldercare.service.exception.ResourceNotFoundException;
import com.eldercare.service.repository.NotesRepository;
import com.eldercare.service.repository.PatientJourneyRepository;
import com.eldercare.service.repository.PatientRepository;
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

    public NotesService(NotesRepository notesRepository,
                        PatientRepository patientRepository,
                        PatientJourneyRepository journeyRepository) {
        this.notesRepository = notesRepository;
        this.patientRepository = patientRepository;
        this.journeyRepository = journeyRepository;
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

        // Flip journey flag on first note
        PatientJourneyEntity journey = journeyRepository.findByPatientId(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient journey", patientId));
        if (!journey.isNote()) {
            journey.setNote(true);
            journey.setUpdatedBy(currentUser);
            journeyRepository.save(journey);
        }

        log.info("Note added for patient {} by {}", patientId, currentUser);
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
        return new NotesResponse(n.getId(), n.getNotes(),
                n.getPatient().getId(), n.getCreatedBy(), n.getCreatedOn());
    }
}
