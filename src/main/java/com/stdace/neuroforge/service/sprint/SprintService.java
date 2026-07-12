package com.stdace.neuroforge.service.sprint;

import com.stdace.neuroforge.enums.SprintStatus;
import com.stdace.neuroforge.common.PageResponse;
import com.stdace.neuroforge.dto.sprint.SprintRequest;
import com.stdace.neuroforge.dto.sprint.SprintResponse;
import jakarta.validation.Valid;

import java.util.UUID;

public interface SprintService {

    SprintResponse create(SprintRequest request);

    SprintResponse getById(UUID id);

    PageResponse<SprintResponse> search(String search, SprintStatus status, int page, int size);

    PageResponse<SprintResponse> search(String search, UUID projectId, SprintStatus status, int page, int size);

    SprintResponse update(UUID id, SprintRequest request);

    void delete(UUID id);

    SprintResponse getById(UUID currentUserId, UUID id);

    PageResponse<SprintResponse> search(UUID userId, String search, SprintStatus status, int page, int size);

    PageResponse<SprintResponse> search(UUID userId, String search, UUID projectId, SprintStatus status, int page, int size);

    void managerCheck(UUID currentUserId, @Valid SprintRequest request);

    void managerCheck(UUID currentUserId, UUID id);
}
