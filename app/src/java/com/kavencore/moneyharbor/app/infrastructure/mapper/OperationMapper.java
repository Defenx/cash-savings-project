package com.kavencore.moneyharbor.app.infrastructure.mapper;

import com.kavencore.moneyharbor.app.api.model.CreateOperationRequestDto;
import com.kavencore.moneyharbor.app.entity.Operation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
public interface OperationMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "account", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "currency", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "amount", ignore = true)
    Operation toEntity(CreateOperationRequestDto dto);
}
