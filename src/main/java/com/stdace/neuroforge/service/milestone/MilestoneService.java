package com.stdace.neuroforge.service.milestone;

import com.stdace.neuroforge.enums.MilestoneStatus;
import com.stdace.neuroforge.common.PageResponse;
import com.stdace.neuroforge.dto.milestone.MilestoneRequest;
import com.stdace.neuroforge.dto.milestone.MilestoneResponse;
import jakarta.validation.Valid;

import java.util.UUID;

public interface MilestoneService {

    MilestoneResponse create(MilestoneRequest request);

    MilestoneResponse getById(UUID id);

    PageResponse<MilestoneResponse> search(String search, MilestoneStatus status, int page, int size);

    PageResponse<MilestoneResponse> search(String search, UUID projectId, MilestoneStatus status, int page, int size);

    PageResponse<MilestoneResponse> search(UUID userId, String search, MilestoneStatus status, int page, int size);

    PageResponse<MilestoneResponse> search(UUID userId, String search, UUID projectId, MilestoneStatus status, int page, int size);

    MilestoneResponse update(UUID id, MilestoneRequest request);

    void delete(UUID id);

    void managerCheck(UUID currentUserId, @Valid MilestoneRequest request);

    void managerCheck(UUID currentUserId, UUID milestoneId);

    boolean isAllowed(UUID id, UUID currentUserId);

    MilestoneResponse getById(UUID currentUserId, UUID id);
}
