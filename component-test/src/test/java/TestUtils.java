import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class TestUtils {
    private TestUtils(){}

    public static UUID extractIdFromLocation(HttpServletResponse resp) {
        String loc = resp.getHeader(HttpHeaders.LOCATION);
        if (loc == null) throw new IllegalStateException("Location header is missing");
        String path = URI.create(loc).getPath();
        String idStr = path.substring(path.lastIndexOf('/') + "/".length());
        return UUID.fromString(idStr);
    }

    public static String loadResource(String path) {
        try (InputStream is = TestUtils.class.getResourceAsStream(path)) {
            if (is == null) throw new IllegalArgumentException("Resource not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
