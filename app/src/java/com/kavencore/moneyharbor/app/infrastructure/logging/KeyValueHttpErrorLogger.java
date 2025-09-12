package com.kavencore.moneyharbor.app.infrastructure.logging;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KeyValueHttpErrorLogger implements HttpErrorLogger {
    @Override
    public void logClientError(HttpServletRequest req, int status, Exception ex, String extra) {
        String line = buildLine("warn", req, status, ex, extra);
        log.warn(line);
    }

    @Override
    public void logServerError(HttpServletRequest req, int status, Exception ex, String extra) {
        String line = buildLine("error", req, status, ex, extra);
        log.error(line, ex);
    }

    private String buildLine(String level, HttpServletRequest req, int status, Exception ex, String extra) {
        StringBuilder sb = new StringBuilder(160);
        sb.append("event=api_error")
                .append(" level=").append(level)
                .append(" method=").append(req.getMethod())
                .append(" path=").append(req.getRequestURI())
                .append(" status=").append(status)
                .append(" type=").append(ex.getClass().getSimpleName());
        if (ex.getMessage() != null && !ex.getMessage().isBlank()) {
            sb.append(" msg=").append(quoteIfNeeded(ex.getMessage()));
        }
        if (extra != null && !extra.isBlank()) {
            sb.append(" extra=").append(quoteIfNeeded(extra));
        }
        return sb.toString();
    }

    private String quoteIfNeeded(String v) {
        boolean need = v.indexOf(' ') >= 0 || v.indexOf('=') >= 0 || v.indexOf('"') >= 0;
        if (need) {
            return "\"" + v.replace("\"", "\\\"") + "\"";
        }
        return v;
    }
}
