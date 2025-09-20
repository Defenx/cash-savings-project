package com.kavencore.moneyharbor.app.infrastructure.mapper;

import com.kavencore.moneyharbor.app.api.model.UserProfileResponseDto;
import com.kavencore.moneyharbor.app.entity.User;
import org.mapstruct.Mapper;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
public interface UserMapper {
    UserProfileResponseDto toDto(User user);
}
