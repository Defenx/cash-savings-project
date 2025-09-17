package com.kavencore.moneyharbor.app.infrastructure.repository;

import com.kavencore.moneyharbor.app.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {
    int deleteByIdAndUserId(UUID attr0, UUID id);

    Optional<Account> findByIdAndUserId(UUID id, UUID userId);
}
