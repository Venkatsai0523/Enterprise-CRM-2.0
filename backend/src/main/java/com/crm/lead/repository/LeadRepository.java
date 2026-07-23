package com.crm.lead.repository;

import com.crm.lead.api.dto.LeadStatus;
import com.crm.lead.entity.Lead;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LeadRepository extends JpaRepository<Lead, UUID> {

    @Query("SELECT l FROM Lead l WHERE " +
           "(:status IS NULL OR l.status = :status) AND " +
           "(:minScore IS NULL OR l.score >= :minScore) AND " +
           "(:maxScore IS NULL OR l.score <= :maxScore)")
    Page<Lead> findWithFilters(
            @Param("status") LeadStatus status,
            @Param("minScore") Integer minScore,
            @Param("maxScore") Integer maxScore,
            Pageable pageable
    );

    long countByStatus(LeadStatus status);

    @Query("SELECT COALESCE(AVG(l.score), 0.0) FROM Lead l")
    Double getAverageScore();

    @Query("SELECT l.leadSource, COUNT(l) FROM Lead l GROUP BY l.leadSource")
    java.util.List<Object[]> countByLeadSourceGrouped();

    long countByAssignedRepId(java.util.UUID assignedRepId);
}
