package com.eldercare.service.service;

import com.eldercare.service.dto.PatientTaskRequest;
import com.eldercare.service.dto.PatientTaskResponse;
import com.eldercare.service.entity.*;
import com.eldercare.service.exception.ElderCareException;
import com.eldercare.service.exception.ResourceNotFoundException;
import com.eldercare.service.repository.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
public class PatientTaskService {

    private final PatientTaskRepository patientTaskRepository;
    private final PatientRepository patientRepository;
    private final TaskRepository taskRepository;
    private final TaskGroupRepository taskGroupRepository;

    public PatientTaskService(PatientTaskRepository patientTaskRepository,
                              PatientRepository patientRepository,
                              TaskRepository taskRepository,
                              TaskGroupRepository taskGroupRepository) {
        this.patientTaskRepository = patientTaskRepository;
        this.patientRepository = patientRepository;
        this.taskRepository = taskRepository;
        this.taskGroupRepository = taskGroupRepository;
    }

    @Transactional
    public List<PatientTaskResponse> create(Long patientId, PatientTaskRequest request) {
        PatientEntity patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", patientId));

        if ((request.taskIds() == null || request.taskIds().isEmpty()) &&
                (request.taskGroupIds() == null || request.taskGroupIds().isEmpty())) {
            throw new ElderCareException("At least one taskId or taskGroupId must be provided");
        }

        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        String status = request.status() != null ? request.status().toUpperCase() : "PENDING";

        List<PatientTaskEntity> entities = new ArrayList<>();

        if (request.taskIds() != null) {
            for (Long taskId : request.taskIds()) {
                TaskEntity task = taskRepository.findById(taskId)
                        .orElseThrow(() -> new ResourceNotFoundException("Task", taskId));
                entities.add(buildEntity(patient, task, null, request, status, currentUser));
            }
        }

        if (request.taskGroupIds() != null) {
            for (Long groupId : request.taskGroupIds()) {
                TaskGroupEntity group = taskGroupRepository.findById(groupId)
                        .orElseThrow(() -> new ResourceNotFoundException("Task group", groupId));
                entities.add(buildEntity(patient, null, group, request, status, currentUser));
            }
        }

        patientTaskRepository.saveAll(entities);
        return entities.stream().map(this::toResponse).toList();
    }

    @Transactional
    public PatientTaskResponse update(Long patientId, Long id, PatientTaskRequest request) {
        PatientTaskEntity entity = patientTaskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient task", id));

        if (!entity.getPatient().getId().equals(patientId)) {
            throw new ElderCareException("Task does not belong to this patient");
        }

        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        entity.setScheduledDateTime(parseScheduledDateTime(request.scheduledDateTime()));
        entity.setStatus(request.status() != null ? request.status().toUpperCase() : entity.getStatus());
        entity.setNotes(request.notes());
        entity.setUpdatedBy(currentUser);

        if (request.taskIds() != null && !request.taskIds().isEmpty()) {
            entity.setTask(taskRepository.findById(request.taskIds().get(0))
                    .orElseThrow(() -> new ResourceNotFoundException("Task", request.taskIds().get(0))));
            entity.setTaskGroup(null);
        }
        if (request.taskGroupIds() != null && !request.taskGroupIds().isEmpty()) {
            entity.setTaskGroup(taskGroupRepository.findById(request.taskGroupIds().get(0))
                    .orElseThrow(() -> new ResourceNotFoundException("Task group", request.taskGroupIds().get(0))));
            entity.setTask(null);
        }

        patientTaskRepository.save(entity);
        return toResponse(entity);
    }

    public List<PatientTaskResponse> getByPatient(Long patientId) {
        if (!patientRepository.existsById(patientId)) {
            throw new ResourceNotFoundException("Patient", patientId);
        }
        return patientTaskRepository.findByPatientIdOrderByScheduledDateTimeAsc(patientId)
                .stream().map(this::toResponse).toList();
    }

    public List<PatientTaskResponse> getByPatientAndDateRange(Long patientId, LocalDate from, LocalDate to) {
        if (!patientRepository.existsById(patientId)) {
            throw new ResourceNotFoundException("Patient", patientId);
        }
        return patientTaskRepository.findByPatientIdAndScheduledDateTimeBetweenOrderByScheduledDateTimeAsc(
                        patientId, from.atStartOfDay(), to.atTime(23, 59, 59))
                .stream().map(this::toResponse).toList();
    }

    private PatientTaskEntity buildEntity(PatientEntity patient, TaskEntity task, TaskGroupEntity group,
                                          PatientTaskRequest request, String status, String currentUser) {
        PatientTaskEntity entity = new PatientTaskEntity();
        entity.setPatient(patient);
        entity.setTask(task);
        entity.setTaskGroup(group);
        entity.setScheduledDateTime(parseScheduledDateTime(request.scheduledDateTime()));
        entity.setStatus(status);
        entity.setNotes(request.notes());
        entity.setCreatedBy(currentUser);
        return entity;
    }

    private LocalDateTime parseScheduledDateTime(String value) {
        if (value == null || value.isBlank()) {
            throw new ElderCareException("Scheduled date time is required");
        }
        List<DateTimeFormatter> formatters = List.of(
                DateTimeFormatter.ofPattern("dd-MM-yy HH:mm"),
                DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"),
                DateTimeFormatter.ofPattern("dd-MM-yy HH:mm:ss"),
                DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
        );
        for (DateTimeFormatter fmt : formatters) {
            try {
                return LocalDateTime.parse(value.trim(), fmt);
            } catch (DateTimeParseException ignored) {
            }
        }
        throw new ElderCareException(
                "Invalid scheduledDateTime format '" + value + "'. Expected: dd-MM-yy HH:mm (e.g. 08-05-26 20:37)");
    }

    private PatientTaskResponse toResponse(PatientTaskEntity e) {
        return new PatientTaskResponse(
                e.getId(),
                e.getPatient().getId(),
                e.getTask() != null ? e.getTask().getId() : null,
                e.getTask() != null ? e.getTask().getTaskName() : null,
                e.getTaskGroup() != null ? e.getTaskGroup().getId() : null,
                e.getTaskGroup() != null ? e.getTaskGroup().getGroupName() : null,
                e.getScheduledDateTime(),
                e.getStatus(),
                e.getNotes(),
                e.getCreatedOn(),
                e.getCreatedBy()
        );
    }
}
