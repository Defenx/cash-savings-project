package com.kavencore.moneyharbor.app.infrastructure.repository;

import com.kavencore.moneyharbor.app.entity.Account;
import com.kavencore.moneyharbor.app.entity.Currency;
import com.kavencore.moneyharbor.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {
    int deleteByIdAndUserId(UUID attr0, UUID id);

    long countByUserAndCurrencyAndTitleStartingWith(User user, Currency currency, String StartingWithQuery);

    List<Account> findAllByUser(User user);

    Optional<Account> findByIdAndUserId(UUID id, UUID userId);


}
