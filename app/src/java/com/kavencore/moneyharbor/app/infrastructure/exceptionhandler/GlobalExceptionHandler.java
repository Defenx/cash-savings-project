package com.kavencore.moneyharbor.app.infrastructure.exceptionhandler;

import com.kavencore.moneyharbor.app.infrastructure.exception.*;
import com.kavencore.moneyharbor.app.infrastructure.logging.HttpErrorLogger;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.net.URI;
import java.util.List;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final HttpErrorLogger errorLogger;
    private final ProblemDetailsFactory pdFactory;


    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(AccountNotFoundException ex, HttpServletRequest req) {
        ProblemDetail pd = getProblemDetail(req, HttpStatus.NOT_FOUND);
        pd.setDetail(ex.getMessage());
        errorLogger.logClientError(req, HttpStatus.NOT_FOUND.value(), ex, null);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(pd);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ProblemDetail> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        ProblemDetail pd = getProblemDetail(req, HttpStatus.BAD_REQUEST);
        String name = ex.getName();
        if (ex.getRequiredType() == java.util.UUID.class) {
            pd.setDetail("Invalid UUID in path parameter '" + name + "'");
        } else {
            pd.setDetail("Invalid value for '" + name + "'");
        }
        errorLogger.logClientError(req, HttpStatus.BAD_REQUEST.value(), ex, name);
        return ResponseEntity.badRequest().body(pd);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleBadJson(HttpMessageNotReadableException ex, HttpServletRequest req) {
        ProblemDetail pd = getProblemDetail(req, HttpStatus.BAD_REQUEST);
        pd.setDetail("Invalid value in request body");
        errorLogger.logClientError(req, HttpStatus.BAD_REQUEST.value(), ex, null);
        return ResponseEntity.badRequest().body(pd);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        ProblemDetail pd = getProblemDetail(req, HttpStatus.BAD_REQUEST);
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> String.format("%s: %s", fe.getField(), fe.getDefaultMessage()))
                .toList();
        pd.setProperty("errors", errors);
        errorLogger.logClientError(req, HttpStatus.BAD_REQUEST.value(), ex, "fieldErrors=" + firstOrNull(errors));
        return ResponseEntity.badRequest().body(pd);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraint(ConstraintViolationException ex, HttpServletRequest req) {
        ProblemDetail pd = getProblemDetail(req, HttpStatus.BAD_REQUEST);
        pd.setDetail("Validation failed");
        List<String> errors = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .toList();
        pd.setProperty("errors", errors);
        errorLogger.logClientError(req, HttpStatus.BAD_REQUEST.value(), ex, "violations=" + firstOrNull(errors));
        return ResponseEntity.badRequest().body(pd);
    }

    @ExceptionHandler(EmailTakenException.class)
    public ResponseEntity<ProblemDetail> handleEmailTaken(EmailTakenException ex, HttpServletRequest req) {
        ProblemDetail pd = getProblemDetail(req, HttpStatus.CONFLICT);
        pd.setDetail("Email already registered: " + ex.getEmail());
        errorLogger.logClientError(req, HttpStatus.CONFLICT.value(), ex, ex.getEmail());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(pd);
    }

    @ExceptionHandler(MissingRoleException.class)
    public ResponseEntity<ProblemDetail> handleMissingRole(MissingRoleException ex, HttpServletRequest req) {
        ProblemDetail pd = getProblemDetail(req, HttpStatus.INTERNAL_SERVER_ERROR);
        pd.setDetail("Server misconfiguration: required role is missing");
        errorLogger.logServerError(req, HttpStatus.INTERNAL_SERVER_ERROR.value(), ex, ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(pd);
    }

    @ExceptionHandler(ErrorResponseException.class)
    public ResponseEntity<ProblemDetail> handleErrorResponse(ErrorResponseException ex, HttpServletRequest req) {
        ProblemDetail pd = ex.getBody();
        if (pd.getInstance() == null) {
            pd.setInstance(URI.create(req.getRequestURI()));
        }
        int status = ex.getStatusCode().value();
        if (ex.getStatusCode().is4xxClientError()) {
            errorLogger.logClientError(req, status, ex, null);
        } else {
            errorLogger.logServerError(req, status, ex, null);
        }
        return ResponseEntity.status(ex.getStatusCode()).body(pd);
    }

    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleCategoryNotFound(CategoryNotFoundException ex, HttpServletRequest req) {
        ProblemDetail pd = getProblemDetail(req, HttpStatus.NOT_FOUND);
        pd.setDetail("Category not found: " + ex.getCategoryId());
        errorLogger.logClientError(req, HttpStatus.NOT_FOUND.value(), ex, ex.getCategoryId().toString());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(pd);
    }

    @ExceptionHandler(InvalidOperationAmountException.class)
    public ResponseEntity<ProblemDetail> handleInvalidOperationAmount(InvalidOperationAmountException ex, HttpServletRequest req) {
        ProblemDetail pd = getProblemDetail(req, HttpStatus.BAD_REQUEST);
        pd.setDetail(ex.getMessage());
        errorLogger.logClientError(req, HttpStatus.BAD_REQUEST.value(), ex, ex.getMessage());
        return ResponseEntity.badRequest().body(pd);
    }

    private ProblemDetail getProblemDetail(HttpServletRequest req, HttpStatus status) {
        return pdFactory.build(req, status, null);
    }

    private String firstOrNull(List<String> list) {
        return (list == null || list.isEmpty()) ? null : list.getFirst();
    }
}
