package com.kavencore.moneyharbor.app.infrastructure.service;

import com.kavencore.moneyharbor.app.api.model.CreateCategoryRequestDto;
import com.kavencore.moneyharbor.app.api.v1.dto.CreatedCategoryResult; // создадим позже
import com.kavencore.moneyharbor.app.entity.Category;
import com.kavencore.moneyharbor.app.infrastructure.mapper.CategoryMapper; // создадим позже
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
    private final CategoryMapper categoryMapper; // маппер, как в AccountService

    @Transactional
    public CreatedCategoryResult createCategory(@Valid CreateCategoryRequestDto dto, UUID userId) {
        // Проверяем, что пользователь существует
        var user = userRepository.getReferenceById(userId);

        // Создаём сущность Category
        Category category = categoryMapper.toEntity(dto);
        category.setUser(user);

        // Сохраняем
        Category savedCategory = categoryRepository.save(category);

        // Возвращаем результат
        return new CreatedCategoryResult(savedCategory.getId(), categoryMapper.toDto(savedCategory));
    }
}