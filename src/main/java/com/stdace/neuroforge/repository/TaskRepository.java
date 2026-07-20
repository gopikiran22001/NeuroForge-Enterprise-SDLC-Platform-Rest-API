package com.stdace.neuroforge.repository;

import com.stdace.neuroforge.enums.TaskStatus;
import com.stdace.neuroforge.models.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {

    List<Task> findByProjectId(UUID projectId);

    List<Task> findBySprintId(UUID sprintId);

    List<Task> findByAssigneeId(UUID assigneeId);

    @Query("""
    SELECT t FROM Task t
    WHERE t.project.id = :projectId
      AND (:sprintId IS NULL OR t.sprint.id = :sprintId)
      AND (:status IS NULL OR t.status = :status)
      AND (:search IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) OR LOWER(t.code) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
    """)
    Page<Task> searchTasks(
            @Param("projectId") UUID projectId,
            @Param("sprintId") UUID sprintId,
            @Param("status") TaskStatus status,
            @Param("search") String search,
            Pageable pageable
    );

    @Query("SELECT COUNT(t) FROM Task t WHERE t.project.id = :projectId AND t.status = 'DONE'")
    long countCompletedTasksByProjectId(@Param("projectId") UUID projectId);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.project.id = :projectId")
    long countTotalTasksByProjectId(@Param("projectId") UUID projectId);

    @Query("SELECT COALESCE(SUM(t.storyPoints), 0) FROM Task t WHERE t.sprint.id = :sprintId AND t.status = :status")
    long sumStoryPointsBySprintIdAndStatus(@Param("sprintId") UUID sprintId, @Param("status") TaskStatus status);

    @Query("SELECT COALESCE(SUM(t.storyPoints), 0) FROM Task t WHERE t.sprint.id = :sprintId")
    long sumTotalStoryPointsBySprintId(@Param("sprintId") UUID sprintId);

    @Query("SELECT MAX(t.code) FROM Task t WHERE t.project.id = :projectId")
    String findMaxCodeByProjectId(@Param("projectId") UUID projectId);
}
