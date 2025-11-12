package com.validatorapi.repository;

import com.validatorapi.model.ValidationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ValidationHistoryRepository extends JpaRepository<ValidationHistory, Long> {
}