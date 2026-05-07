package com.eldercare.service.controller;

import com.eldercare.service.dto.TaskGroupRequest;
import com.eldercare.service.dto.TaskGroupResponse;
import com.eldercare.service.service.TaskGroupService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/task-groups")
public class TaskGroupController {

    private final TaskGroupService taskGroupService;

    public TaskGroupController(TaskGroupService taskGroupService) {
        this.taskGroupService = taskGroupService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'NURSE', 'DOCTOR')")
    public ResponseEntity<TaskGroupResponse> create(@Valid @RequestBody TaskGroupRequest request) {
        return ResponseEntity.ok(taskGroupService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<TaskGroupResponse>> getAll(@RequestParam(required = false) String search) {
        return ResponseEntity.ok(taskGroupService.getAll(search));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'NURSE', 'DOCTOR')")
    public ResponseEntity<TaskGroupResponse> update(@PathVariable Long id,
                                                    @Valid @RequestBody TaskGroupRequest request) {
        return ResponseEntity.ok(taskGroupService.update(id, request));
    }
}
