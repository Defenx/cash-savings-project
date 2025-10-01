package com.kavencore.moneyharbor.app.api.v1.controller;

import com.kavencore.moneyharbor.app.api.controller.CategoriesApi;
import com.kavencore.moneyharbor.app.api.model.CreateCategoryRequestDto;
import com.kavencore.moneyharbor.app.api.model.CategoryResponseDto;
import com.kavencore.moneyharbor.app.api.v1.dto.CreatedCategoryResult;
import com.kavencore.moneyharbor.app.infrastructure.service.CategoryService;
import com.kavencore.moneyharbor.app.security.AuthFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequiredArgsConstructor
public class CategoriesController implements CategoriesApi {

    public static final String CATEGORIES_PATH = "/categories";
    public static final String CATEGORIES_PATH_WITH_SLASH = CATEGORIES_PATH + "/";

    private final CategoryService categoryService;
    private final AuthFacade authFacade;

    @Override
    public ResponseEntity<CategoryResponseDto> createCategory(CreateCategoryRequestDto createCategoryRequestDto) {
        CreatedCategoryResult result = categoryService.createCategory(createCategoryRequestDto, authFacade.userId());
        URI location = UriComponentsBuilder.fromPath(CATEGORIES_PATH_WITH_SLASH + "{id}").build(result.id().toString());

        return ResponseEntity.created(location).body(result.body());
    }
}