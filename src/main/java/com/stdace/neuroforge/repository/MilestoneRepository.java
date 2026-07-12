package com.stdace.neuroforge.repository;

import com.stdace.neuroforge.dto.milestone.MilestoneResponse;
import com.stdace.neuroforge.enums.MilestoneStatus;
import com.stdace.neuroforge.models.Milestone;
import com.stdace.neuroforge.models.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Range;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MilestoneRepository extends JpaRepository<Milestone, UUID> {

    Page<Milestone> findByProjectId(UUID projectId, Pageable pageable);

    Page<Milestone> findByStatus(MilestoneStatus status, Pageable pageable);

    @Query("""
    SELECT COUNT(DISTINCT m) > 0
    FROM Milestone m
    JOIN m.project p
    LEFT JOIN p.teams t
    WHERE m.id = :milestoneId
      AND (
            p.projectManager.id = :userId
         OR t.teamLeader.id = :userId
      )
""")
    boolean isProjectManagerOrTeamLeadForMilestoneProject(
            @Param("milestoneId") UUID milestoneId,
            @Param("userId") UUID userId
    );

    Page<Milestone> findByProjectIdAndStatus(UUID projectId, MilestoneStatus status, Pageable pageable);

    @Query("""
    SELECT DISTINCT m
    FROM Milestone m
    JOIN m.project p
    LEFT JOIN p.teams t
    LEFT JOIN t.members member
    WHERE m.id = :milestoneId
      AND (
            p.projectManager.id = :userId
         OR t.teamLeader.id = :userId
         OR member.id = :userId
      )
""")
    Optional<Milestone> findByIdAndUserId(
            @Param("milestoneId") UUID milestoneId,
            @Param("userId") UUID userId
    );

    @Query("""
    SELECT DISTINCT m
    FROM Milestone m
    JOIN m.project p
    LEFT JOIN p.teams t
    LEFT JOIN t.members member
    WHERE p.projectManager.id = :userId
       OR t.teamLeader.id = :userId
       OR member.id = :userId
""")
    Page<Milestone> findByUserId(
            @Param("userId") UUID userId,
            Pageable pageable
    );

    @Query("""
    SELECT DISTINCT m
    FROM Milestone m
    JOIN m.project p
    LEFT JOIN p.teams t
    LEFT JOIN t.members member
    WHERE (
            p.projectManager.id = :userId
         OR t.teamLeader.id = :userId
         OR member.id = :userId
    )
    AND m.status = :status
""")
    Page<Milestone> findByUserIdAndStatus(
            @Param("userId") UUID userId,
            @Param("status") MilestoneStatus status,
            Pageable pageable
    );

    @Query("""
    SELECT DISTINCT m
    FROM Milestone m
    JOIN m.project p
    LEFT JOIN p.teams t
    LEFT JOIN t.members member
    WHERE p.id = :projectId
      AND (
            p.projectManager.id = :userId
         OR t.teamLeader.id = :userId
         OR member.id = :userId
      )
""")
    Page<Milestone> findByProjectIdAndUserId(
            @Param("projectId") UUID projectId,
            @Param("userId") UUID userId,
            Pageable pageable
    );

    @Query("""
    SELECT DISTINCT m
    FROM Milestone m
    JOIN m.project p
    LEFT JOIN p.teams t
    LEFT JOIN t.members member
    WHERE p.id = :projectId
      AND (
            p.projectManager.id = :userId
         OR t.teamLeader.id = :userId
         OR member.id = :userId
      )
      AND m.status = :status
""")
    Page<Milestone> findByProjectIdAndUserIdAndStatus(
            @Param("projectId") UUID projectId,
            @Param("userId") UUID userId,
            @Param("status") MilestoneStatus status,
            Pageable pageable
    );

}
