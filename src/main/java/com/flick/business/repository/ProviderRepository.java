package com.flick.business.repository;

import com.flick.business.core.entity.Provider;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProviderRepository extends JpaRepository<Provider, Long> {
  Optional<Provider> findByIdAndUserId(Long id, Long userId);
  List<Provider> findByUserId(Long userId);
}
