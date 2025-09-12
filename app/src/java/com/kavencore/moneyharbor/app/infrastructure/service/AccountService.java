package com.kavencore.moneyharbor.app.infrastructure.service;

import com.kavencore.moneyharbor.app.api.model.AccountResponseDto;
import com.kavencore.moneyharbor.app.api.model.CreateAccountRequestDto;
import com.kavencore.moneyharbor.app.api.v1.dto.CreatedAccountResult;
import com.kavencore.moneyharbor.app.entity.Account;
import com.kavencore.moneyharbor.app.infrastructure.exception.AccountNotFoundException;
import com.kavencore.moneyharbor.app.infrastructure.mapper.AccountMapper;
import com.kavencore.moneyharbor.app.infrastructure.repository.AccountRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Validated
public class AccountService {
    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;

    private static final String TITLE_SUFFIX = "_счет";

    @Transactional
    public CreatedAccountResult createAccount(@Valid CreateAccountRequestDto createAccountRequestDto) {
        Account acc = accountMapper.toEntity(createAccountRequestDto);
        applyDefaults(acc);
        Account savedAcc = accountRepository.save(acc);
        return new CreatedAccountResult(savedAcc.getId(), accountMapper.toDto(savedAcc));
    }

    @Transactional(readOnly = true)
    public AccountResponseDto getAccountById(UUID id) {
        Account account = accountRepository.findById(id).orElseThrow(() -> new AccountNotFoundException(id));
        return accountMapper.toDto(account);
    }

    @Transactional
    public boolean delete(UUID id) {
        if (!accountRepository.existsById(id)) {
            return false;                 // 200
        }
        accountRepository.deleteById(id);
        return true;                    // 204
    }

    private void applyDefaults(Account acc) {
        if (acc.getTitle() == null) {
            acc.setTitle(acc.getCurrency().name() + TITLE_SUFFIX);
        }
        if (acc.getAmount() == null) {
            acc.setAmount(BigDecimal.ZERO);
        } else {
            acc.setAmount(acc.getAmount().setScale(2, RoundingMode.HALF_UP));
        }
    }
}
