package com.kavencore.moneyharbor.app.infrastructure.service;

import com.kavencore.moneyharbor.app.api.model.CreateOperationRequestDto;
import com.kavencore.moneyharbor.app.entity.Account;
import com.kavencore.moneyharbor.app.entity.Category;
import com.kavencore.moneyharbor.app.entity.Operation;
import com.kavencore.moneyharbor.app.entity.Type;
import com.kavencore.moneyharbor.app.infrastructure.exception.AccountNotFoundException;
import com.kavencore.moneyharbor.app.infrastructure.exception.CategoryNotFoundException;
import com.kavencore.moneyharbor.app.infrastructure.exception.InvalidOperationAmountException;
import com.kavencore.moneyharbor.app.infrastructure.mapper.OperationMapper;
import com.kavencore.moneyharbor.app.infrastructure.repository.AccountRepository;
import com.kavencore.moneyharbor.app.infrastructure.repository.CategoryRepository;
import com.kavencore.moneyharbor.app.infrastructure.repository.OperationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OperationService {

    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final OperationRepository operationRepository;
    private final Clock clock;
    private final OperationMapper operationMapper;


    @Transactional
    public UUID create(CreateOperationRequestDto dto, UUID userId) {
        Account account = accountRepository.findByIdAndUserId(dto.getAccountId(), userId)
                .orElseThrow(() -> new AccountNotFoundException(dto.getAccountId()));

        Category category = categoryRepository.findByIdAndUserId(dto.getCategoryId(), userId)
                .orElseThrow(() -> new CategoryNotFoundException(dto.getCategoryId()));

        BigDecimal amount = normalizeAmount(dto.getAmount());
        validateOperation(amount, dto.getDescription(), category.getType());

        Operation operation = operationMapper.toEntity(dto);
        operation.setAmount(amount);
        operation.setDate(resolveDate(dto.getDate()));
        operation.setCurrency(account.getCurrency());
        operation.attachCategory(category);

        account.addOperation(operation);

        return operationRepository.save(operation).getId();
    }

    private LocalDate resolveDate(LocalDate requestedDate) {
        return requestedDate != null ? requestedDate : LocalDate.now(clock);
    }

    private void validateOperation(BigDecimal amount, String description, Type categoryType) {
        validateAmount(amount, categoryType);
        validateDescription(description);
    }

    private BigDecimal normalizeAmount(BigDecimal value) {
        if (value == null) {
            throw new InvalidOperationAmountException("Amount must be provided");
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private void validateAmount(BigDecimal amount, Type type) {
        if (amount.signum() == 0) {
            throw new InvalidOperationAmountException("Amount must be non-zero");
        }
        if (Type.EXPENSE.equals(type) && amount.signum() >= 0) {
            throw new InvalidOperationAmountException("Expense amount must be negative");
        }
        if (Type.INCOME.equals(type) && amount.signum() <= 0) {
            throw new InvalidOperationAmountException("Income amount must be positive");
        }
    }

    private void validateDescription(String description) {
        if (description != null && description.length() > 100) {
            throw new InvalidOperationAmountException("Description must be at most 100 characters");
        }
    }
}

