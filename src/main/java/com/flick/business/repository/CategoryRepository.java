package com.flick.business.repository;

import com.flick.business.core.entity.Category;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
  Optional<Category> findByIdAndUserId(Long id, Long userId);
  List<Category> findByUserId(Long userId);
}
