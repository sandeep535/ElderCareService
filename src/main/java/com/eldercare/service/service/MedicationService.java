package com.eldercare.service.service;

import com.eldercare.service.dto.MedicationMasterRequest;
import com.eldercare.service.dto.MedicationMasterResponse;
import com.eldercare.service.dto.MedicationSlotResponse;
import com.eldercare.service.dto.PatientMedicationRequest;
import com.eldercare.service.dto.PatientMedicationResponse;
import com.eldercare.service.dto.SlotActionRequest;
import com.eldercare.service.dto.UserInfoResponse;
import com.eldercare.service.entity.MasterTableEntity;
import com.eldercare.service.entity.MedicationMasterEntity;
import com.eldercare.service.entity.MedicationSlotEntity;
import com.eldercare.service.entity.PatientEntity;
import com.eldercare.service.entity.PatientMedicationEntity;
import com.eldercare.service.entity.UserEntity;
import com.eldercare.service.exception.ElderCareException;
import com.eldercare.service.exception.ResourceNotFoundException;
import com.eldercare.service.repository.MasterTableRepository;
import com.eldercare.service.repository.MedicationMasterRepository;
import com.eldercare.service.repository.MedicationSlotRepository;
import com.eldercare.service.repository.PatientMedicationRepository;
import com.eldercare.service.repository.PatientRepository;
import com.eldercare.service.repository.UserDetailsRepository;
import com.eldercare.service.repository.UserRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class MedicationService {

    private static final Logger log = LogManager.getLogger(MedicationService.class);

    private final MedicationMasterRepository medicationMasterRepository;
    private final PatientRepository patientRepository;
    private final PatientMedicationRepository patientMedicationRepository;
    private final MedicationSlotRepository medicationSlotRepository;
    private final MasterTableRepository masterTableRepository;
    private final UserRepository userRepository;
    private final UserDetailsRepository userDetailsRepository;
    private final AuditService auditService;

    public MedicationService(MedicationMasterRepository medicationMasterRepository,
                             PatientRepository patientRepository,
                             PatientMedicationRepository patientMedicationRepository,
                             MedicationSlotRepository medicationSlotRepository,
                             MasterTableRepository masterTableRepository,
                             UserRepository userRepository,
                             UserDetailsRepository userDetailsRepository,
                             AuditService auditService) {
        this.medicationMasterRepository = medicationMasterRepository;
        this.patientRepository = patientRepository;
        this.patientMedicationRepository = patientMedicationRepository;
        this.medicationSlotRepository = medicationSlotRepository;
        this.masterTableRepository = masterTableRepository;
        this.userRepository = userRepository;
        this.userDetailsRepository = userDetailsRepository;
        this.auditService = auditService;
    }

    public List<MedicationMasterResponse> getMedicationCatalog() {
        return medicationMasterRepository.findByActiveTrueOrderByNameAsc().stream()
                .map(this::toMasterResponse).toList();
    }

    @Transactional
    public MedicationMasterResponse createMedicationMaster(MedicationMasterRequest request) {
        MedicationMasterEntity entity = new MedicationMasterEntity();
        entity.setName(request.name());
        entity.setGenericName(request.genericName());
        entity.setStrength(request.strength());
        entity.setForm(request.form());
        entity.setActive(request.active() == null || request.active());
        medicationMasterRepository.save(entity);
        return toMasterResponse(entity);
    }

    @Transactional
    public MedicationMasterResponse updateMedicationMaster(Long id, MedicationMasterRequest request) {
        MedicationMasterEntity entity = medicationMasterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medication master", id));
        entity.setName(request.name());
        entity.setGenericName(request.genericName());
        entity.setStrength(request.strength());
        entity.setForm(request.form());
        entity.setActive(request.active() == null || request.active());
        medicationMasterRepository.save(entity);
        return toMasterResponse(entity);
    }

    @Transactional
    public PatientMedicationResponse prescribe(Long patientId, PatientMedicationRequest request) {
        PatientEntity patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", patientId));

        MedicationMasterEntity medication = medicationMasterRepository.findById(request.medicationId())
                .orElseThrow(() -> new ResourceNotFoundException("Medication master", request.medicationId()));

        if (request.slotCodes() == null || request.slotCodes().size() != request.frequency()) {
            throw new ElderCareException("Slot codes must be provided and equal the requested frequency");
        }

        List<Integer> hours = resolveSlotHours(request.slotCodes());
        if (hours.size() != request.frequency()) {
            throw new ElderCareException("Slot codes count does not match frequency");
        }

        PatientMedicationEntity patientMedication = new PatientMedicationEntity();
        patientMedication.setPatient(patient);
        patientMedication.setMedication(medication);
        patientMedication.setDose(request.dose());
        patientMedication.setFrequency(request.frequency());
        patientMedication.setDurationDays(request.durationDays());
        patientMedication.setStartDate(request.startDate());
        patientMedication.setEndDate(request.startDate().plusDays(request.durationDays() - 1L));
        patientMedication.setInstructions(request.instructions());
        patientMedication.setActive(true);
        patientMedication.setCreatedBy(resolveCurrentUserName());
        patientMedicationRepository.save(patientMedication);

        List<MedicationSlotEntity> slots = new ArrayList<>();
        for (int day = 0; day < request.durationDays(); day++) {
            LocalDate date = request.startDate().plusDays(day);
            for (Integer hour : hours) {
                MedicationSlotEntity slot = new MedicationSlotEntity();
                slot.setPatientMedication(patientMedication);
                slot.setPatient(patient);
                slot.setScheduledTime(date.atTime(hour, 0));
                slot.setStatus("PENDING");
                slot.setCreatedBy(resolveCurrentUserName());
                slots.add(slot);
            }
        }
        medicationSlotRepository.saveAll(slots);

        PatientMedicationResponse response = toPatientMedicationResponse(patientMedication);
        auditService.record(patientId, "MEDICATION", response);
        log.info("Medication prescription created for patient {} by {}", patientId, resolveCurrentUserName());
        return response;
    }

    public List<PatientMedicationResponse> getActivePrescriptions(Long patientId) {
        return patientMedicationRepository.findByPatientIdAndActiveTrue(patientId).stream()
                .map(this::toPatientMedicationResponse)
                .toList();
    }

    @Transactional
    public PatientMedicationResponse stopPrescription(Long patientId, Long prescriptionId) {
        PatientMedicationEntity prescription = patientMedicationRepository.findById(prescriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient medication", prescriptionId));
        if (!prescription.getPatient().getId().equals(patientId)) {
            throw new ElderCareException("Prescription does not belong to the requested patient");
        }
        prescription.setActive(false);
        patientMedicationRepository.save(prescription);
        return toPatientMedicationResponse(prescription);
    }

    public List<MedicationSlotResponse> getNextDueSlots(Long patientId) {
        LocalDateTime window = LocalDateTime.now().plusHours(1);
        return medicationSlotRepository
                .findByPatientIdAndStatusAndScheduledTimeLessThanEqualOrderByScheduledTimeAsc(patientId, "PENDING", window)
                .stream()
                .map(this::toSlotResponse)
                .toList();
    }

    public List<MedicationSlotResponse> getSlots(Long patientId, LocalDate date) {
        List<MedicationSlotEntity> slots;
        if (date == null) {
            slots = medicationSlotRepository.findByPatientIdOrderByScheduledTimeAsc(patientId);
        } else {
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.plusDays(1).atStartOfDay();
            slots = medicationSlotRepository.findByPatientIdAndScheduledTimeBetweenOrderByScheduledTimeAsc(patientId, start, end);
        }
        return slots.stream().map(this::toSlotResponse).toList();
    }

    @Transactional
    public MedicationSlotResponse markSlotGiven(Long patientId, Long slotId, SlotActionRequest request) {
        MedicationSlotEntity slot = medicationSlotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Medication slot", slotId));
        validateSlotPatient(patientId, slot);
        slot.setStatus("GIVEN");
        slot.setGivenAt(LocalDateTime.now());
        slot.setNotes(request.notes());
        slot.setGivenBy(resolveCurrentUser());
        medicationSlotRepository.save(slot);
        updatePrescriptionCompletion(slot.getPatientMedication());
        return toSlotResponse(slot);
    }

    @Transactional
    public MedicationSlotResponse markSlotSkipped(Long patientId, Long slotId, SlotActionRequest request) {
        MedicationSlotEntity slot = medicationSlotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Medication slot", slotId));
        validateSlotPatient(patientId, slot);
        slot.setStatus("SKIPPED");
        slot.setGivenAt(LocalDateTime.now());
        slot.setNotes(request.notes());
        slot.setGivenBy(resolveCurrentUser());
        medicationSlotRepository.save(slot);
        updatePrescriptionCompletion(slot.getPatientMedication());
        return toSlotResponse(slot);
    }

    private void validateSlotPatient(Long patientId, MedicationSlotEntity slot) {
        if (!slot.getPatient().getId().equals(patientId)) {
            throw new ElderCareException("Slot does not belong to the requested patient");
        }
    }

    private void updatePrescriptionCompletion(PatientMedicationEntity prescription) {
        boolean allClosed = medicationSlotRepository.findByPatientMedicationIdOrderByScheduledTimeAsc(prescription.getId()).stream()
                .allMatch(slot -> !"PENDING".equals(slot.getStatus()));
        if (allClosed && prescription.isActive()) {
            prescription.setActive(false);
            patientMedicationRepository.save(prescription);
        }
    }

    private List<Integer> resolveSlotHours(List<String> slotCodes) {
        return slotCodes.stream()
                .map(code -> masterTableRepository.findByTypeAndLookupCode("MEDICATION_SLOT_TIME", code)
                        .orElseThrow(() -> new ElderCareException("Invalid medication slot code: " + code)))
                .map(MasterTableEntity::getLookupValue)
                .map(value -> {
                    try {
                        return Integer.parseInt(value);
                    } catch (NumberFormatException ex) {
                        throw new ElderCareException("Invalid slot hour for lookup value: " + value);
                    }
                })
                .toList();
    }

    private String resolveCurrentUserName() {
        try {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            return "system";
        }
    }

    private UserEntity resolveCurrentUser() {
        String username = resolveCurrentUserName();
        return userRepository.findByUsername(username)
                .orElse(null);
    }

    private MedicationMasterResponse toMasterResponse(MedicationMasterEntity entity) {
        return new MedicationMasterResponse(entity.getId(), entity.getName(), entity.getGenericName(),
                entity.getStrength(), entity.getForm(), entity.isActive());
    }

    private PatientMedicationResponse toPatientMedicationResponse(PatientMedicationEntity entity) {
        return new PatientMedicationResponse(entity.getId(), entity.getPatient().getId(),
                entity.getMedication().getId(), entity.getMedication().getName(), entity.getDose(),
                entity.getFrequency(), entity.getDurationDays(), entity.getStartDate(), entity.getEndDate(),
                entity.isActive(), entity.getInstructions());
    }

    private MedicationSlotResponse toSlotResponse(MedicationSlotEntity entity) {
        UserEntity givenBy = entity.getGivenBy();
        UserInfoResponse givenByInfo = givenBy != null ? buildUserInfo(givenBy) : null;
        return new MedicationSlotResponse(entity.getId(), entity.getPatientMedication().getId(),
                entity.getPatient().getId(), entity.getPatientMedication().getMedication().getName(),
                entity.getPatientMedication().getDose(), entity.getScheduledTime(), entity.getStatus(),
                entity.getGivenAt(), givenByInfo, entity.getNotes());
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
