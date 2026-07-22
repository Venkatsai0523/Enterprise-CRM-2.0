package com.crm.task.repository;

import com.crm.task.api.dto.TaskStatus;
import com.crm.task.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {

    @Query("SELECT t FROM Task t WHERE " +
           "(:assignedTo IS NULL OR t.assignedTo = :assignedTo) AND " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:relatedToType IS NULL OR t.relatedToType = :relatedToType) AND " +
           "(:relatedToId IS NULL OR t.relatedToId = :relatedToId)")
    Page<Task> findWithFilters(
            @Param("assignedTo") UUID assignedTo,
            @Param("status") TaskStatus status,
            @Param("relatedToType") String relatedToType,
            @Param("relatedToId") UUID relatedToId,
            Pageable pageable
    );

    List<Task> findByRelatedToTypeAndRelatedToId(String relatedToType, UUID relatedToId);

    long countByStatus(TaskStatus status);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.status != 'COMPLETED' AND t.dueDate < :now")
    long countOverdueTasks(@Param("now") java.time.Instant now);
}
