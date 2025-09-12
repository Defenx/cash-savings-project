package com.kavencore.moneyharbor.app.infrastructure.logging;

import jakarta.servlet.http.HttpServletRequest;

public interface HttpErrorLogger {
    void logClientError(HttpServletRequest req, int status, Exception ex, String extra);

    void logServerError(HttpServletRequest req, int status, Exception ex, String extra);
}
