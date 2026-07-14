package com.stdace.neuroforge.controller;

import com.stdace.neuroforge.common.ApiResponse;
import com.stdace.neuroforge.common.PageResponse;
import com.stdace.neuroforge.dto.task.TaskRequest;
import com.stdace.neuroforge.dto.task.TaskResponse;
import com.stdace.neuroforge.enums.TaskStatus;
import com.stdace.neuroforge.models.TaskActivity;
import com.stdace.neuroforge.service.task.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<ApiResponse<TaskResponse>> create(@Valid @RequestBody TaskRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Task created successfully", taskService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskResponse>> update(@PathVariable UUID id, @Valid @RequestBody TaskRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Task updated successfully", taskService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        taskService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Task deleted successfully", null));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Task retrieved successfully", taskService.getById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<TaskResponse>>> search(
            @RequestParam UUID projectId,
            @RequestParam(required = false) UUID sprintId,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success("Tasks retrieved successfully",
                taskService.search(projectId, sprintId, status, search, page, size)));
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<ApiResponse<TaskResponse>> addComment(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body
    ) {
        String text = body.get("text");
        return ResponseEntity.ok(ApiResponse.success("Comment added successfully", taskService.addComment(id, text)));
    }

    @PostMapping("/{id}/attachments")
    public ResponseEntity<ApiResponse<TaskResponse>> addAttachment(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body
    ) {
        String name = body.get("name");
        String size = body.get("size");
        String url = body.get("url");
        return ResponseEntity.ok(ApiResponse.success("Attachment added successfully", taskService.addAttachment(id, name, size, url)));
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<ApiResponse<List<TaskActivity>>> getActivityHistory(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Activity history retrieved successfully", taskService.getActivityHistory(id)));
    }
}
