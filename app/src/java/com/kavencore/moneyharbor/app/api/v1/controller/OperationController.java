package com.kavencore.moneyharbor.app.api.v1.controller;

import com.kavencore.moneyharbor.app.api.controller.OperationApi;
import com.kavencore.moneyharbor.app.api.model.CreateOperationRequestDto;
import com.kavencore.moneyharbor.app.infrastructure.service.OperationService;
import com.kavencore.moneyharbor.app.security.AuthFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class OperationController implements OperationApi {

    public static final String OPERATIONS_PATH = "/operation";
    public static final String OPERATIONS_PATH_WITH_SLASH = OPERATIONS_PATH + "/";

    private final OperationService operationService;
    private final AuthFacade authFacade;

    @Override
    public ResponseEntity<Object> createOperation(CreateOperationRequestDto createOperationRequestDto) {
        UUID operationId = operationService.create(createOperationRequestDto, authFacade.userId());
        URI location = UriComponentsBuilder
                .fromPath(OPERATIONS_PATH_WITH_SLASH + "{id}")
                .build(operationId.toString());
        return ResponseEntity.created(location).build();
    }
}
