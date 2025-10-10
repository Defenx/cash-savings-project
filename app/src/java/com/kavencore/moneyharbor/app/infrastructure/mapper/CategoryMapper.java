package com.kavencore.moneyharbor.app.infrastructure.mapper;

import com.kavencore.moneyharbor.app.api.model.CreateCategoryRequestDto;
import com.kavencore.moneyharbor.app.api.model.CategoryResponseDto;
import com.kavencore.moneyharbor.app.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
public interface CategoryMapper {

    CategoryResponseDto toDto(Category entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    Category toEntity(CreateCategoryRequestDto req);
}