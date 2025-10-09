import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.UUID;

import static com.kavencore.moneyharbor.app.api.v1.controller.UserController.GET_PROFILE_PATH;
import static com.kavencore.moneyharbor.app.api.v1.controller.UserController.SIGN_UP_PATH;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Базовый класс для компонентных тестов, требующих аутентифицированного пользователя.
 * <p>
 * Наследники НЕ ДОЛЖНЫ иметь свои собственные @BeforeEach методы, которые конфликтуют с этим.
 */
public abstract class AuthenticatedComponentTestBase extends BaseComponentTest {

    @BeforeEach
    protected void ensureTestUser() throws Exception {

        String signUpJson = UserJson.TEST_USER.load();

        MockHttpServletResponse resp = performPostNoAuth(SIGN_UP_PATH, signUpJson)
                .andReturn().getResponse();

        if (resp.getStatus() == HttpStatus.CONFLICT.value()) {

            MockHttpServletResponse profileResp = performGetAuth(GET_PROFILE_PATH)
                    .andExpect(status().isOk())
                    .andReturn().getResponse();

            testUserId = UUID.fromString(JsonPath.read(profileResp.getContentAsString(), "$.id"));
        } else {

            String location = resp.getHeader(HttpHeaders.LOCATION);
            if (location != null) {
                testUserId = UUID.fromString(location.substring(location.lastIndexOf('/') + 1));
            } else {
                throw new IllegalStateException("User was created but Location header is missing");
            }
        }
    }
}