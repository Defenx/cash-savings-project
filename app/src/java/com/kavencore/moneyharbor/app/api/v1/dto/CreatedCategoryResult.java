package com.kavencore.moneyharbor.app.api.v1.dto;

import com.kavencore.moneyharbor.app.api.model.CategoryResponseDto;
import java.util.UUID;

public record CreatedCategoryResult(UUID id, CategoryResponseDto body) {}
