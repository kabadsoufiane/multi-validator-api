package com.validatorapi.repository;

import com.validatorapi.model.DisposableDomain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DisposableDomainRepository extends JpaRepository<DisposableDomain, Long> {

    boolean existsByDomain(String domain);

    @Query("SELECT COUNT(d) FROM DisposableDomain d")
    long countAll();
}