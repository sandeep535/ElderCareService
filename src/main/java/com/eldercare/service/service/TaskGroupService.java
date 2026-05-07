package com.eldercare.service.service;

import com.eldercare.service.dto.TaskGroupRequest;
import com.eldercare.service.dto.TaskGroupResponse;
import com.eldercare.service.entity.TaskEntity;
import com.eldercare.service.entity.TaskGroupEntity;
import com.eldercare.service.exception.ElderCareException;
import com.eldercare.service.exception.ResourceNotFoundException;
import com.eldercare.service.repository.TaskGroupRepository;
import com.eldercare.service.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class TaskGroupService {

    private final TaskGroupRepository taskGroupRepository;
    private final TaskRepository taskRepository;
    private final TaskService taskService;

    public TaskGroupService(TaskGroupRepository taskGroupRepository,
                            TaskRepository taskRepository,
                            TaskService taskService) {
        this.taskGroupRepository = taskGroupRepository;
        this.taskRepository = taskRepository;
        this.taskService = taskService;
    }

    @Transactional
    public TaskGroupResponse create(TaskGroupRequest request) {
        if (taskGroupRepository.existsByGroupNameIgnoreCase(request.groupName())) {
            throw new ElderCareException("Group already exists: " + request.groupName());
        }
        TaskGroupEntity entity = new TaskGroupEntity();
        entity.setGroupName(request.groupName());
        entity.setActive(request.active() == null || request.active());
        entity.setTasks(resolveTasks(request.taskIds()));
        taskGroupRepository.save(entity);
        return toResponse(entity);
    }

    public List<TaskGroupResponse> getAll(String search) {
        if (search == null || search.isBlank()) {
            return taskGroupRepository.findByActiveTrueOrderByGroupNameAsc()
                    .stream().map(this::toResponse).toList();
        }
        return taskGroupRepository.findByGroupNameContainingIgnoreCaseAndActiveTrueOrderByGroupNameAsc(search)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public TaskGroupResponse update(Long id, TaskGroupRequest request) {
        TaskGroupEntity entity = taskGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task group", id));

        if (!entity.getGroupName().equalsIgnoreCase(request.groupName()) &&
                taskGroupRepository.existsByGroupNameIgnoreCase(request.groupName())) {
            throw new ElderCareException("Group already exists: " + request.groupName());
        }

        entity.setGroupName(request.groupName());
        if (request.active() != null) entity.setActive(request.active());
        if (request.taskIds() != null) entity.setTasks(resolveTasks(request.taskIds()));
        taskGroupRepository.save(entity);
        return toResponse(entity);
    }

    private List<TaskEntity> resolveTasks(List<Long> taskIds) {
        if (taskIds == null || taskIds.isEmpty()) return new ArrayList<>();
        return taskIds.stream()
                .map(taskId -> taskRepository.findById(taskId)
                        .orElseThrow(() -> new ResourceNotFoundException("Task", taskId)))
                .toList();
    }

    private TaskGroupResponse toResponse(TaskGroupEntity entity) {
        return new TaskGroupResponse(
                entity.getId(),
                entity.getGroupName(),
                entity.isActive(),
                entity.getTasks().stream().map(taskService::toResponse).toList()
        );
    }
}
