package com.crm.organization.repository;

import com.crm.organization.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, UUID> {
    Optional<Organization> findBySubdomain(String subdomain);
    boolean existsByName(String name);
    boolean existsBySubdomain(String subdomain);
}
