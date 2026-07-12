package com.stdace.neuroforge.repository;

import com.stdace.neuroforge.enums.ProjectStatus;
import com.stdace.neuroforge.models.Project;
import com.stdace.neuroforge.models.Team;
import com.stdace.neuroforge.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Range;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {

    Page<Project> findByStatus(ProjectStatus status, Pageable pageable);

    @Query("""
    SELECT DISTINCT p
    FROM Project p
    LEFT JOIN p.teams t
    LEFT JOIN t.members m
    WHERE p.projectManager.id = :userId
       OR t.teamLeader.id = :userId
       OR m.id = :userId
""")
    Page<Project> findMyProjects(
            @Param("userId") UUID userId,
            Pageable pageable
    );

    @Query("""
    SELECT DISTINCT p
    FROM Project p
    LEFT JOIN p.teams t
    LEFT JOIN t.members m
    WHERE (
            p.projectManager.id = :userId
         OR t.teamLeader.id = :userId
         OR m.id = :userId
    )
    AND p.status = :status
""")
    Page<Project> findMyProjectsByStatus(
            @Param("userId") UUID userId,
            @Param("status") ProjectStatus status,
            Pageable pageable
    );

    @Query("""
    SELECT COUNT(DISTINCT p) > 0
    FROM Project p
    LEFT JOIN p.teams t
    LEFT JOIN t.members m
    WHERE p.id = :projectId
      AND (
            p.projectManager.id = :userId
         OR t.teamLeader.id = :userId
         OR m.id = :userId
      )
""")
    boolean existsByProjectAndUser(
            @Param("projectId") UUID projectId,
            @Param("userId") UUID userId
    );

}
