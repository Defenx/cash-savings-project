package com.kavencore.moneyharbor.app.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kavencore.moneyharbor.app.infrastructure.exceptionhandler.ProblemDetailsFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class ProblemAccessDeniedHandler implements AccessDeniedHandler {
    private final ProblemDetailsFactory pdFactory;
    private final ObjectMapper om;

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse res, AccessDeniedException e) throws IOException {
        HttpStatus status = HttpStatus.FORBIDDEN;
        ProblemDetail pd = pdFactory.build(req, status, "Access denied");
        res.setStatus(status.value());
        res.setContentType("application/problem+json");
        om.writeValue(res.getOutputStream(), pd);
    }
}