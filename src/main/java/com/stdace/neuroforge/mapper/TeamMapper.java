package com.stdace.neuroforge.mapper;

import com.stdace.neuroforge.models.Team;
import com.stdace.neuroforge.models.User;
import com.stdace.neuroforge.dto.team.TeamRequest;
import com.stdace.neuroforge.dto.team.TeamResponse;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class TeamMapper {

    public Team toEntity(TeamRequest request, User teamLeader, Set<User> members) {
        Team team = new Team();
        team.setName(request.getName());
        team.setDescription(request.getDescription());
        team.setStatus(request.getStatus());
        team.setTeamLeader(teamLeader);
        team.setMembers(members);
        return team;
    }

    public TeamResponse toResponse(Team team) {
        return TeamResponse.builder()
                .id(team.getId())
                .name(team.getName())
                .description(team.getDescription())
                .status(team.getStatus())
                .teamLeaderId(team.getTeamLeader() != null ? team.getTeamLeader().getId() : null)
                .teamLeaderEmail(team.getTeamLeader() != null ? team.getTeamLeader().getEmail() : null)
                .memberIds(team.getMembers() == null ? Set.of() : team.getMembers().stream().map(User::getId).collect(Collectors.toSet()))
                .createdAt(team.getCreatedAt())
                .updatedAt(team.getUpdatedAt())
                .build();
    }

    public void updateEntity(Team team, TeamRequest request, User teamLeader, Set<User> members) {
        team.setName(request.getName());
        team.setDescription(request.getDescription());
        team.setStatus(request.getStatus());
        team.setTeamLeader(teamLeader);
        team.setMembers(members);
    }
}

