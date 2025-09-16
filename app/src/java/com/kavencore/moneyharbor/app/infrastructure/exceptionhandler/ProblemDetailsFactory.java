package com.kavencore.moneyharbor.app.infrastructure.exceptionhandler;

import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;

import java.net.URI;

@Component
public class ProblemDetailsFactory {
    public ProblemDetail build(HttpServletRequest req, HttpStatus status, @Nullable String detail) {
        var pd = ProblemDetail.forStatus(status);
        pd.setTitle(status.getReasonPhrase());
        pd.setInstance(URI.create(req.getRequestURI()));
        if (detail != null) pd.setDetail(detail);
        return pd;
    }
}