package com.kavencore.moneyharbor.app.api.v1.controller;

import com.kavencore.moneyharbor.app.api.controller.CategoriesApi;
import com.kavencore.moneyharbor.app.api.model.CreateCategoryRequestDto;
import com.kavencore.moneyharbor.app.api.model.CategoryResponseDto;
import com.kavencore.moneyharbor.app.entity.Category;
import com.kavencore.moneyharbor.app.infrastructure.service.CategoryService;
import com.kavencore.moneyharbor.app.security.AuthFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class CategoriesController implements CategoriesApi {

    public static final String CATEGORIES_PATH = "/categories";

    private final CategoryService categoryService;
    private final AuthFacade authFacade;

    @Override
    public ResponseEntity<CategoryResponseDto> createCategory(CreateCategoryRequestDto createCategoryRequestDto) {

        CategoryResponseDto responseDto = categoryService.createCategory(createCategoryRequestDto, authFacade.userId());

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(responseDto.getId())
                .toUri();

        return ResponseEntity.created(location).body(responseDto);
    }
    @PreAuthorize("hasRole('User')")
    public ResponseEntity<Void> deleteCategory(UUID categoryId) {
        categoryService.delete(categoryId, authFacade.userId());
        return ResponseEntity.noContent().build();
    }
}
