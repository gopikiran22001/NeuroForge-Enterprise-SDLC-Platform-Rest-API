package com.stdace.neuroforge.repository;

import com.stdace.neuroforge.enums.SprintStatus;
import com.stdace.neuroforge.models.Milestone;
import com.stdace.neuroforge.models.Project;
import com.stdace.neuroforge.models.Sprint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SprintRepository extends JpaRepository<Sprint, UUID> {

    Optional<Sprint> findByProjectAndStatus(Project project, SprintStatus status);

    Page<Sprint> findByStatus(SprintStatus status, Pageable pageable);

    Page<Sprint> findByProjectIdAndStatus(UUID projectId, SprintStatus status, Pageable pageable);


    Page<Sprint> findByProjectId(UUID projectId, Pageable pageable);

    @Query("""
    SELECT COUNT(DISTINCT s) > 0
    FROM Sprint s
    JOIN s.project p
    LEFT JOIN p.teams t
    LEFT JOIN t.members m
    WHERE s.id = :sprintId
      AND (
            p.projectManager.id = :userId
         OR t.teamLeader.id = :userId
         OR m.id = :userId
      )
""")
    boolean existsBySprintAndUser(
            @Param("sprintId") UUID sprintId,
            @Param("userId") UUID userId
    );

    @Query("""
    SELECT DISTINCT s
    FROM Sprint s
    JOIN s.project p
    LEFT JOIN p.teams t
    LEFT JOIN t.members m
    WHERE p.projectManager.id = :userId
       OR t.teamLeader.id = :userId
       OR m.id = :userId
""")
    Page<Sprint> findByUserId(
            @Param("userId") UUID userId,
            Pageable pageable
    );

    @Query("""
    SELECT DISTINCT s
    FROM Sprint s
    JOIN s.project p
    LEFT JOIN p.teams t
    LEFT JOIN t.members m
    WHERE (
            p.projectManager.id = :userId
         OR t.teamLeader.id = :userId
         OR m.id = :userId
    )
    AND s.status = :status
""")
    Page<Sprint> findByUserIdAndStatus(
            @Param("userId") UUID userId,
            @Param("status") SprintStatus status,
            Pageable pageable
    );

    @Query("""
    SELECT DISTINCT s
    FROM Sprint s
    JOIN s.project p
    LEFT JOIN p.teams t
    LEFT JOIN t.members m
    WHERE p.id = :projectId
      AND (
            p.projectManager.id = :userId
         OR t.teamLeader.id = :userId
         OR m.id = :userId
      )
""")
    Page<Sprint> findByProjectId(
            @Param("userId") UUID userId,
            @Param("projectId") UUID projectId,
            Pageable pageable
    );

    @Query("""
    SELECT DISTINCT s
    FROM Sprint s
    JOIN s.project p
    LEFT JOIN p.teams t
    LEFT JOIN t.members m
    WHERE p.id = :projectId
      AND s.status = :status
      AND (
            p.projectManager.id = :userId
         OR t.teamLeader.id = :userId
         OR m.id = :userId
      )
""")
    Page<Sprint> findByProjectIdAndStatus(
            @Param("userId") UUID userId,
            @Param("projectId") UUID projectId,
            @Param("status") SprintStatus status,
            Pageable pageable
    );

}
