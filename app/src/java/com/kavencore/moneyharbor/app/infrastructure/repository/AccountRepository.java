package com.kavencore.moneyharbor.app.infrastructure.repository;

import com.kavencore.moneyharbor.app.entity.Account;
import com.kavencore.moneyharbor.app.entity.Currency;
import com.kavencore.moneyharbor.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {
    int deleteByIdAndUserId(UUID accId, UUID userId);


    List<Account> findTopByUserAndCurrencyAndTitleStartingWithOrderByTitleDesc(User user, Currency currency, String prefix);

    List<Account> findAllByUser(User user);

    Optional<Account> findByIdAndUserId(UUID id, UUID userId);

    /**
     * Опишу логику, т.к. она достаточно сложная.
     * LENGTH(:titlePrefix) + 1 – получаем длину префикса в названии. {валюта}_счёт_. И сдвигаемся на одну позицию вперёд
     * SUBSTRING... as LONG стартуем с полученной позиции и приводим к числовому типу данных
     * Берём максимальное число
     * a.title ~ '_\d+$' проверяет с помощью regex оставляет только те запросы, которые содержат после счет_ - цифры
     * value - {всё выражение}, nativeQuery = true необходимо для того чтобы regex работал
    */
    @Query(value = """
        SELECT MAX(CAST(SUBSTRING(a.title, LENGTH(:titlePrefix) + 1) as BIGINT))
        from accounts a
        where a.user_id = :userId and
        a.currency = :currency and
        a.title ~ '_\\d+$'
        """, nativeQuery = true)
    Optional<Long> findMaxAccountNumberThisCurrency(
            @Param("userId") UUID user,
            @Param("currency") String currency,
            @Param("titlePrefix") String titlePrefix
    );


}
