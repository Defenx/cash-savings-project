package com.kavencore.moneyharbor.app.infrastructure.mapper;

import com.kavencore.moneyharbor.app.api.model.AccountResponseDto;
import com.kavencore.moneyharbor.app.api.model.CreateAccountRequestDto;
import com.kavencore.moneyharbor.app.entity.Account;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
public interface AccountMapper {

    AccountResponseDto toDto(Account entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    Account toEntity(CreateAccountRequestDto req);
}

