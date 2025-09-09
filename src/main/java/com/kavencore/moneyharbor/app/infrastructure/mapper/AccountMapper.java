package com.kavencore.moneyharbor.app.infrastructure.mapper;

import com.kavencore.moneyharbor.app.api.model.AccountResponseDto;
import com.kavencore.moneyharbor.app.api.model.CreateAccountRequestDto;
import com.kavencore.moneyharbor.app.entity.Account;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    AccountResponseDto toDto(Account entity);

    @Mapping(target = "id", ignore = true)
    Account toEntity(CreateAccountRequestDto req);
}

