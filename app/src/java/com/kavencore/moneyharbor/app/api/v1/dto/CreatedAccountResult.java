package com.kavencore.moneyharbor.app.api.v1.dto;

import com.kavencore.moneyharbor.app.api.model.AccountResponseDto;

import java.util.UUID;

public record CreatedAccountResult(UUID id, AccountResponseDto body) {}
