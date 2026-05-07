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
import java.util.ArrayList;
import java.util.List;

@Service
public class PatientTaskService {

    private final PatientTaskRepository patientTaskRepository;
    private final PatientRepository patientRepository;
    private final TaskRepository taskRepository;
    private final TaskGroupRepository taskGroupRepository;
    private final UserRepository userRepository;
    private final UserDetailsRepository userDetailsRepository;

    public PatientTaskService(PatientTaskRepository patientTaskRepository,
                              PatientRepository patientRepository,
                              TaskRepository taskRepository,
                              TaskGroupRepository taskGroupRepository,
                              UserRepository userRepository,
                              UserDetailsRepository userDetailsRepository) {
        this.patientTaskRepository = patientTaskRepository;
        this.patientRepository = patientRepository;
        this.taskRepository = taskRepository;
        this.taskGroupRepository = taskGroupRepository;
        this.userRepository = userRepository;
        this.userDetailsRepository = userDetailsRepository;
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
        UserEntity assignedTo = resolveUser(request.assignedToUserId());
        String status = request.status() != null ? request.status().toUpperCase() : "PENDING";

        List<PatientTaskEntity> entities = new ArrayList<>();

        if (request.taskIds() != null) {
            for (Long taskId : request.taskIds()) {
                TaskEntity task = taskRepository.findById(taskId)
                        .orElseThrow(() -> new ResourceNotFoundException("Task", taskId));
                PatientTaskEntity entity = buildEntity(patient, task, null, request, status, assignedTo, currentUser);
                entities.add(entity);
            }
        }

        if (request.taskGroupIds() != null) {
            for (Long groupId : request.taskGroupIds()) {
                TaskGroupEntity group = taskGroupRepository.findById(groupId)
                        .orElseThrow(() -> new ResourceNotFoundException("Task group", groupId));
                PatientTaskEntity entity = buildEntity(patient, null, group, request, status, assignedTo, currentUser);
                entities.add(entity);
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
        entity.setScheduledDateTime(request.scheduledDateTime());
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
        if (request.assignedToUserId() != null) {
            entity.setAssignedTo(resolveUser(request.assignedToUserId()));
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
                                          PatientTaskRequest request, String status,
                                          UserEntity assignedTo, String currentUser) {
        PatientTaskEntity entity = new PatientTaskEntity();
        entity.setPatient(patient);
        entity.setTask(task);
        entity.setTaskGroup(group);
        entity.setScheduledDateTime(request.scheduledDateTime());
        entity.setStatus(status);
        entity.setNotes(request.notes());
        entity.setAssignedTo(assignedTo);
        entity.setCreatedBy(currentUser);
        return entity;
    }

    private UserEntity resolveUser(Long userId) {
        if (userId == null) return null;
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }

    private PatientTaskResponse toResponse(PatientTaskEntity e) {
        String assignedToName = null;
        if (e.getAssignedTo() != null) {
            var details = userDetailsRepository.findByUserId(e.getAssignedTo().getId()).orElse(null);
            assignedToName = details != null
                    ? details.getFirstName() + " " + details.getLastName()
                    : e.getAssignedTo().getUsername();
        }
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
                e.getAssignedTo() != null ? e.getAssignedTo().getId() : null,
                assignedToName,
                e.getCreatedOn(),
                e.getCreatedBy()
        );
    }
}
