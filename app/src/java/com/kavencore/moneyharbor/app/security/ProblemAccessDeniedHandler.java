package com.kavencore.moneyharbor.app.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kavencore.moneyharbor.app.infrastructure.exceptionhandler.ProblemDetailsFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.kavencore.moneyharbor.app.security.SecurityMessages.ACCESS_DENIED;

@Component
@RequiredArgsConstructor
public class ProblemAccessDeniedHandler implements AccessDeniedHandler {
    private final ProblemDetailsFactory pdFactory;
    private final ObjectMapper om;

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse res, AccessDeniedException e) throws IOException {
        HttpStatus status = HttpStatus.FORBIDDEN;
        ProblemDetail pd = pdFactory.build(req, status, ACCESS_DENIED);
        res.setStatus(status.value());
        res.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        om.writeValue(res.getOutputStream(), pd);
    }
}