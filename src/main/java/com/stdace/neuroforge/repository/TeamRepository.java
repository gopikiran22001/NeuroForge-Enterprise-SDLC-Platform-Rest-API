package com.stdace.neuroforge.repository;

import com.stdace.neuroforge.enums.TeamStatus;
import com.stdace.neuroforge.models.Team;
import com.stdace.neuroforge.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TeamRepository extends JpaRepository<Team, UUID> {

    boolean existsByNameIgnoreCase(String name);

    Page<Team> findByStatus(TeamStatus status, Pageable pageable);

    @Query("""
    SELECT DISTINCT t
    FROM Team t
    LEFT JOIN t.members m
    WHERE t.teamLeader.id = :userId
       OR m.id = :userId
""")
    Page<Team> findByUserId(
            @Param("userId") UUID userId,
            Pageable pageable
    );

    @Query("""
    SELECT DISTINCT t
    FROM Team t
    LEFT JOIN t.members m
    WHERE (
            t.teamLeader.id = :userId
         OR m.id = :userId
    )
    AND t.status = :status
""")
    Page<Team> findByUserIdAndStatus(
            @Param("userId") UUID userId,
            @Param("status") TeamStatus status,
            Pageable pageable
    );

}
