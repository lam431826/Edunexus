package com.edunexus.repository;

import com.edunexus.domain.SystemConfigEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SystemConfigRepository extends JpaRepository<SystemConfigEntry, Long> {
    Optional<SystemConfigEntry> findByConfigKey(String configKey);
}
