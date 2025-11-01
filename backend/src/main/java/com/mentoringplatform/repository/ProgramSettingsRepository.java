package com.mentoringplatform.repository;

import com.mentoringplatform.model.ProgramSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProgramSettingsRepository extends JpaRepository<ProgramSettings, Long> {
    ProgramSettings findFirstByOrderByCreatedAtDesc();
}