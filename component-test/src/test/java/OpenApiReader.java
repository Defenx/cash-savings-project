import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;

public class OpenApiReader {
    private static final String OPENAPI_FILE = "static\\swagger\\openapi.yaml";
    private static Integer titleMaxLength = null;

    public static int getTitleMaxLength() {
        if (titleMaxLength == null) {
            titleMaxLength = readTitleMaxLengthFromOpenApi();
        }
        return titleMaxLength;
    }

    private static int readTitleMaxLengthFromOpenApi() {
        try {
            ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
            InputStream inputStream = new ClassPathResource(OPENAPI_FILE).getInputStream();
            JsonNode root = yamlMapper.readTree(inputStream);

            JsonNode maxLengthNode = root
                    .path("components")
                    .path("schemas")
                    .path("CreateAccountRequest")
                    .path("properties")
                    .path("title")
                    .path("maxLength");

            if (maxLengthNode.isMissingNode()) {
                maxLengthNode = root
                        .path("components")
                        .path("schemas")
                        .path("AccountResponse")
                        .path("properties")
                        .path("title")
                        .path("maxLength");
            }

            if (maxLengthNode.isMissingNode()) {
                throw new IllegalStateException("maxLength not found for title in OpenAPI spec");
            }

            return maxLengthNode.asInt();
        } catch (Exception e) {
            throw new RuntimeException("Failed to read maxLength from OpenAPI spec", e);
        }
    }

    public static String generateTitleOfLength(int length) {
        if (length <= 0) return "";
        return "A".repeat(length);
    }

}
