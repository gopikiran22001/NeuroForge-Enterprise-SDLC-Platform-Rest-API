package com.stdace.neuroforge.service.team;

import com.stdace.neuroforge.common.PageResponse;
import com.stdace.neuroforge.dto.team.TeamRequest;
import com.stdace.neuroforge.dto.team.TeamResponse;
import com.stdace.neuroforge.enums.TeamStatus;

import java.util.UUID;

public interface TeamService {

    TeamResponse create(TeamRequest request);

    TeamResponse getById(UUID id);

    PageResponse<TeamResponse> search(String search, TeamStatus status, int page, int size);

    TeamResponse update(UUID id, TeamRequest request);

    void delete(UUID id);
}
