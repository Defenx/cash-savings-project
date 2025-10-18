package com.kavencore.moneyharbor.app.infrastructure.repository;

import com.kavencore.moneyharbor.app.entity.Account;
import com.kavencore.moneyharbor.app.entity.Currency;
import com.kavencore.moneyharbor.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {
    int deleteByIdAndUserId(UUID accId, UUID userId);

    int deleteAllByUserId(UUID userId);

    List<Account> findAllByUser(User user);

    Optional<Account> findByIdAndUserId(UUID id, UUID userId);

    List<Account> findByUserIdAndCurrency(UUID userId, Currency currency);

    boolean existsAccountByUserIdAndTitleAndCurrency(UUID userId, String title, Currency currency);
}
