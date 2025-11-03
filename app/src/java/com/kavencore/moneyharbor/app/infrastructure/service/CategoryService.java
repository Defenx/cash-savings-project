package com.kavencore.moneyharbor.app.infrastructure.service;

import com.kavencore.moneyharbor.app.api.model.CategoryResponseDto;
import com.kavencore.moneyharbor.app.api.model.CreateCategoryRequestDto;
import com.kavencore.moneyharbor.app.entity.Category;
import com.kavencore.moneyharbor.app.entity.User;
import com.kavencore.moneyharbor.app.infrastructure.mapper.CategoryMapper;
import com.kavencore.moneyharbor.app.infrastructure.repository.CategoryRepository;
import com.kavencore.moneyharbor.app.infrastructure.repository.OperationRepository;
import com.kavencore.moneyharbor.app.infrastructure.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Validated
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final CategoryMapper categoryMapper;
    private final OperationRepository operationReposiroty;


    @Transactional
    public CategoryResponseDto createCategory(@Valid CreateCategoryRequestDto dto, UUID userId) {

        User user = userRepository.getReferenceById(userId);

        Category category = categoryMapper.toEntity(dto);
        category.setUser(user);

        Category savedCategory = categoryRepository.save(category);

        return categoryMapper.toDto(savedCategory);
    }

    public void delete(UUID currentUserId, UUID categoryId) {

        Category category = categoryRepository.findById(categoryId).
                orElseThrow(() -> new EntityNotFoundException("Категория с id=" + categoryId + " не найдена"));

        if (!category.getUser().getId().equals(currentUserId)) {
            throw new AccessDeniedException("Вы не можете удалить чужую категорию");
        }

        boolean isUsed = operationReposiroty.existsByCategoryId(categoryId);

        if(isUsed) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Категория не может быть удалена, т.к. используется в операциях");
        }


        categoryRepository.delete(category);
    }
}