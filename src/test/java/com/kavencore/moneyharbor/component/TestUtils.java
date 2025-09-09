package com.kavencore.moneyharbor.component;

import jakarta.servlet.http.HttpServletResponse;

import java.net.URI;
import java.util.UUID;

public final class TestUtils {
    private TestUtils(){}

    public static UUID extractIdFromLocation(HttpServletResponse resp) {
        String loc = resp.getHeader("Location");
        if (loc == null) throw new IllegalStateException("Location header is missing");
        String path = URI.create(loc).getPath();
        String idStr = path.substring(path.lastIndexOf('/') + 1);
        return UUID.fromString(idStr);
    }
}
