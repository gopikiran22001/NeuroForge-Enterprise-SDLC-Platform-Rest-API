package com.stdace.neuroforge.service.team;

import com.stdace.neuroforge.dto.milestone.MilestoneResponse;
import com.stdace.neuroforge.enums.TeamStatus;
import com.stdace.neuroforge.models.Team;
import com.stdace.neuroforge.models.User;
import com.stdace.neuroforge.common.PageResponse;
import com.stdace.neuroforge.dto.team.TeamRequest;
import com.stdace.neuroforge.dto.team.TeamResponse;
import com.stdace.neuroforge.exception.DuplicateResourceException;
import com.stdace.neuroforge.exception.ResourceNotFoundException;
import com.stdace.neuroforge.mapper.TeamMapper;
import com.stdace.neuroforge.repository.TeamRepository;
import com.stdace.neuroforge.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final TeamMapper teamMapper;

    @Override
    public TeamResponse create(TeamRequest request) {
        if (teamRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateResourceException("Team name already exists");
        }

        User teamLeader = userRepository.findById(request.getTeamLeaderId())
                .orElseThrow(() -> new ResourceNotFoundException("Team leader not found: " + request.getTeamLeaderId()));

        Set<User> members = request.getMemberIds().stream()
                .map(userId -> userRepository.findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId)))
                .collect(Collectors.toSet());

        Team team = teamMapper.toEntity(request, teamLeader, members);
        teamRepository.save(team);
        return teamMapper.toResponse(team);
    }

    @Override
    @Transactional(readOnly = true)
    public TeamResponse getById(UUID id) {
        return teamRepository.findById(id)
                .map(teamMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TeamResponse> search(String search, TeamStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        if(status != null) {
            return PageResponse.from(teamRepository.findByStatus(status, pageable).map(teamMapper::toResponse));
        }

        return PageResponse.from(teamRepository.findAll(pageable).map(teamMapper::toResponse));
    }

    @Override
    public TeamResponse update(UUID id, TeamRequest request) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found: " + id));

        if (!team.getName().equalsIgnoreCase(request.getName()) &&
                teamRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateResourceException("Team name already exists");
        }

        User teamLeader = userRepository.findById(request.getTeamLeaderId())
                .orElseThrow(() -> new ResourceNotFoundException("Team leader not found: " + request.getTeamLeaderId()));

        Set<User> members = request.getMemberIds().stream()
                .map(userId -> userRepository.findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId)))
                .collect(Collectors.toSet());

        teamMapper.updateEntity(team, request, teamLeader, members);
        return teamMapper.toResponse(teamRepository.save(team));
    }

    @Override
    public void delete(UUID id) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found: " + id));
        team.setStatus(TeamStatus.DELETED);
        teamRepository.save(team);
    }
}
