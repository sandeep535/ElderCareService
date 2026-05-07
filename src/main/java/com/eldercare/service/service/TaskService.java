package com.eldercare.service.service;

import com.eldercare.service.dto.TaskRequest;
import com.eldercare.service.dto.TaskResponse;
import com.eldercare.service.entity.TaskEntity;
import com.eldercare.service.exception.ElderCareException;
import com.eldercare.service.exception.ResourceNotFoundException;
import com.eldercare.service.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Transactional
    public TaskResponse create(TaskRequest request) {
        if (taskRepository.existsByTaskNameIgnoreCase(request.taskName())) {
            throw new ElderCareException("Task already exists: " + request.taskName());
        }
        TaskEntity entity = new TaskEntity();
        entity.setTaskName(request.taskName());
        entity.setActive(request.active() == null || request.active());
        taskRepository.save(entity);
        return toResponse(entity);
    }

    public List<TaskResponse> getAll(String search) {
        if (search == null || search.isBlank()) {
            return taskRepository.findByActiveTrueOrderByTaskNameAsc()
                    .stream().map(this::toResponse).toList();
        }
        return taskRepository.findByTaskNameContainingIgnoreCaseAndActiveTrueOrderByTaskNameAsc(search)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public TaskResponse update(Long id, TaskRequest request) {
        TaskEntity entity = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", id));

        if (!entity.getTaskName().equalsIgnoreCase(request.taskName()) &&
                taskRepository.existsByTaskNameIgnoreCase(request.taskName())) {
            throw new ElderCareException("Task already exists: " + request.taskName());
        }

        entity.setTaskName(request.taskName());
        if (request.active() != null) entity.setActive(request.active());
        taskRepository.save(entity);
        return toResponse(entity);
    }

    public TaskResponse toResponse(TaskEntity entity) {
        return new TaskResponse(entity.getId(), entity.getTaskName(), entity.isActive());
    }
}
