package com.kavencore.moneyharbor.app.infrastructure.service;

import com.kavencore.moneyharbor.app.api.model.CategoryResponseDto;
import com.kavencore.moneyharbor.app.api.model.CreateCategoryRequestDto;
import com.kavencore.moneyharbor.app.entity.Category;
import com.kavencore.moneyharbor.app.entity.User;
import com.kavencore.moneyharbor.app.infrastructure.mapper.CategoryMapper;
import com.kavencore.moneyharbor.app.infrastructure.repository.CategoryRepository;
import com.kavencore.moneyharbor.app.infrastructure.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Validated
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final CategoryMapper categoryMapper;

    @Transactional
    public CategoryResponseDto createCategory(@Valid CreateCategoryRequestDto dto, UUID userId) {

        User user = userRepository.getReferenceById(userId);

        Category category = categoryMapper.toEntity(dto);
        category.setUser(user);

        Category savedCategory = categoryRepository.save(category);

        return categoryMapper.toDto(savedCategory);
    }

    @Transactional
    public boolean deleteAll(UUID userId) {
        int affected = categoryRepository.deleteAllByUserId(userId);

        return affected > 0;
    }
}