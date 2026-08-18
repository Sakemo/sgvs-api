package com.flick.business.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.flick.business.core.entity.GeneralSettings;
import java.util.Optional;
public interface GeneralSettingsRepository extends JpaRepository<GeneralSettings, Long> {
    Optional<GeneralSettings> findByUserId(Long userId);
}
