package com.stdace.neuroforge.service.project;

import com.stdace.neuroforge.enums.ProjectStatus;
import com.stdace.neuroforge.common.PageResponse;
import com.stdace.neuroforge.dto.project.ProjectRequest;
import com.stdace.neuroforge.dto.project.ProjectResponse;

import java.util.UUID;

public interface ProjectService {

    ProjectResponse create(ProjectRequest request);

    ProjectResponse getById(UUID id);

    ProjectResponse getById(UUID userI, UUID id);

    PageResponse<ProjectResponse> search(String search, ProjectStatus status, int page, int size);

    PageResponse<ProjectResponse> search(UUID userId, String search, ProjectStatus status, int page, int size);



    ProjectResponse update(UUID id, ProjectRequest request);

    void delete(UUID id);
}
