package com.eldercare.service.service;

import com.eldercare.service.dto.PatientRequest;
import com.eldercare.service.dto.PatientResponse;
import com.eldercare.service.entity.PatientEntity;
import com.eldercare.service.entity.PatientJourneyEntity;
import com.eldercare.service.exception.ElderCareException;
import com.eldercare.service.exception.ResourceNotFoundException;
import com.eldercare.service.repository.PatientJourneyRepository;
import com.eldercare.service.repository.PatientRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class PatientService {

    private static final Logger log = LogManager.getLogger(PatientService.class);

    private final PatientRepository patientRepository;
    private final PatientJourneyRepository journeyRepository;

    @Value("${app.upload.dir:uploads/patient-photos}")
    private String uploadDir;

    public PatientService(PatientRepository patientRepository,
                          PatientJourneyRepository journeyRepository) {
        this.patientRepository = patientRepository;
        this.journeyRepository = journeyRepository;
    }

    @Transactional
    public PatientResponse save(PatientRequest request) {
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();

        PatientEntity patient = new PatientEntity();
        patient.setFirstName(request.firstName());
        patient.setLastName(request.lastName());
        patient.setDob(request.dob());
        patient.setGender(request.gender());
        patient.setCreatedBy(currentUser);
        patientRepository.save(patient);

        patient.setPatientId(generatePatientId(patient.getId()));
        patientRepository.save(patient);

        PatientJourneyEntity journey = new PatientJourneyEntity();
        journey.setPatient(patient);
        journey.setBasicDetails(true);
        journey.setCreatedBy(currentUser);
        journeyRepository.save(journey);

        log.info("Patient registered: {} by {}", patient.getPatientId(), currentUser);
        return toResponse(patient, journey);
    }

    public List<PatientResponse> getAll() {
        return patientRepository.findAll().stream()
                .map(p -> {
                    PatientJourneyEntity journey = journeyRepository
                            .findByPatientId(p.getId()).orElse(null);
                    return toResponse(p, journey);
                })
                .toList();
    }

    public PatientResponse getById(Long id) {
        PatientEntity patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", id));
        PatientJourneyEntity journey = journeyRepository.findByPatientId(id).orElse(null);
        return toResponse(patient, journey);
    }

    @Transactional
    public PatientResponse update(Long id, PatientRequest request) {
        PatientEntity patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", id));

        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        patient.setFirstName(request.firstName());
        patient.setLastName(request.lastName());
        patient.setDob(request.dob());
        patient.setGender(request.gender());
        patient.setUpdatedBy(currentUser);
        patientRepository.save(patient);

        PatientJourneyEntity journey = journeyRepository.findByPatientId(id).orElse(null);
        log.info("Patient updated: {} by {}", patient.getPatientId(), currentUser);
        return toResponse(patient, journey);
    }

    @Transactional
    public PatientResponse uploadPhoto(Long id, MultipartFile file) {
        PatientEntity patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", id));

        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ElderCareException("Only image files are allowed (jpg, png, etc.)");
        }

        // Validate file size (max 5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new ElderCareException("File size must not exceed 5MB");
        }

        try {
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir);
            Files.createDirectories(uploadPath);

            // Delete old photo if exists
            if (patient.getProfilePhoto() != null) {
                Path oldFile = uploadPath.resolve(patient.getProfilePhoto());
                Files.deleteIfExists(oldFile);
            }

            // Generate unique filename: {patientId}_{uuid}.{ext}
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : ".jpg";
            String filename = patient.getPatientId() + "_" + UUID.randomUUID() + extension;

            // Save file to disk
            Path targetPath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            // Save filename in DB
            String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
            patient.setProfilePhoto(filename);
            patient.setUpdatedBy(currentUser);
            patientRepository.save(patient);

            log.info("Profile photo uploaded for patient {} by {}", patient.getPatientId(), currentUser);
        } catch (IOException e) {
            throw new ElderCareException("Failed to store photo: " + e.getMessage());
        }

        PatientJourneyEntity journey = journeyRepository.findByPatientId(id).orElse(null);
        return toResponse(patient, journey);
    }

    public Resource getPhoto(Long id) {
        PatientEntity patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", id));

        if (patient.getProfilePhoto() == null) {
            throw new ResourceNotFoundException("No profile photo found for patient: " + id);
        }

        try {
            Path filePath = Paths.get(uploadDir).resolve(patient.getProfilePhoto());
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new ResourceNotFoundException("Photo file not found for patient: " + id);
            }
            return resource;
        } catch (MalformedURLException e) {
            throw new ElderCareException("Could not read photo file: " + e.getMessage());
        }
    }

    // EC-2025-0001 format
    private String generatePatientId(Long id) {
        return String.format("EC-%d-%04d", Year.now().getValue(), id);
    }

    private PatientResponse toResponse(PatientEntity p, PatientJourneyEntity journey) {
        List<String> pending = new ArrayList<>();
        boolean isComplete = false;

        if (journey != null) {
            if (!journey.isMedical())   pending.add("MEDICAL");
            if (!journey.isAdmission()) pending.add("ADMISSION");
            if (!journey.isNote())      pending.add("NOTES");
            isComplete = pending.isEmpty();
        }

        return new PatientResponse(
                p.getId(),
                p.getPatientId(),
                p.getFirstName(),
                p.getLastName(),
                p.getDob(),
                p.getGender(),
                p.getProfilePhoto() != null ? "/api/patients/" + p.getId() + "/photo" : null,
                isComplete ? "COMPLETE" : "INCOMPLETE",
                pending
        );
    }
}
