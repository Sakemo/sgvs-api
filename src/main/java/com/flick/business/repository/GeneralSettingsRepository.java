package com.flick.business.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.flick.business.core.entity.GeneralSettings;

@Repository
public interface GeneralSettingsRepository extends JpaRepository<GeneralSettings, Long> {

}
