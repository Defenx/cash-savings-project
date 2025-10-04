package com.kavencore.moneyharbor.app.infrastructure.repository;

import com.kavencore.moneyharbor.app.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

    Optional<Category> findByIdAndUserId(UUID id, UUID userid);
}