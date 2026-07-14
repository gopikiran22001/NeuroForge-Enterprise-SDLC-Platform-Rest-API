package com.stdace.neuroforge.service.task;

import com.stdace.neuroforge.common.PageResponse;
import com.stdace.neuroforge.dto.task.TaskRequest;
import com.stdace.neuroforge.dto.task.TaskResponse;
import com.stdace.neuroforge.enums.TaskStatus;
import com.stdace.neuroforge.models.TaskComment;
import com.stdace.neuroforge.models.TaskAttachment;
import com.stdace.neuroforge.models.TaskActivity;

import java.util.List;
import java.util.UUID;

public interface TaskService {

    TaskResponse create(TaskRequest request);

    TaskResponse update(UUID id, TaskRequest request);

    void delete(UUID id);

    TaskResponse getById(UUID id);

    PageResponse<TaskResponse> search(UUID projectId, UUID sprintId, TaskStatus status, String search, int page, int size);

    TaskResponse addComment(UUID id, String text);

    TaskResponse addAttachment(UUID id, String name, String size, String url);

    List<TaskActivity> getActivityHistory(UUID id);
}
