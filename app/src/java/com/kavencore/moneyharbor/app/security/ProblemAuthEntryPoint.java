package com.kavencore.moneyharbor.app.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kavencore.moneyharbor.app.infrastructure.exceptionhandler.ProblemDetailsFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class ProblemAuthEntryPoint implements AuthenticationEntryPoint {
    private final ProblemDetailsFactory pdFactory;
    private final ObjectMapper om;

    @Override
    public void commence(HttpServletRequest req, HttpServletResponse res, AuthenticationException e) throws IOException {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        ProblemDetail pd = pdFactory.build(req, status, "Authentication required");
        res.setStatus(status.value());
        res.setContentType("application/problem+json");
        res.addHeader(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"MoneyHarbor API\"");
        om.writeValue(res.getOutputStream(), pd);
    }
}
