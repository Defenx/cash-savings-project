import com.jayway.jsonpath.JsonPath;
import com.kavencore.moneyharbor.app.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import java.util.Optional;
import java.util.UUID;

import static com.kavencore.moneyharbor.app.api.v1.controller.UserController.SIGN_UP_PATH;

/**
 * Базовый класс для компонентных тестов, требующих аутентифицированного пользователя.
 * <p>
 * Наследники НЕ ДОЛЖНЫ иметь свои собственные @BeforeEach методы, которые конфликтуют с этим.
 */
public abstract class AuthenticatedComponentTestBase extends BaseComponentTest {

    @BeforeEach
    protected void setupAuthenticatedUser() throws Exception {
        String signUpJson = UserJson.TEST_USER.load();

        var signUpResult = performPostNoAuth(SIGN_UP_PATH, signUpJson)
                .andReturn().getResponse();

        if (signUpResult.getStatus() == HttpStatus.CONFLICT.value()) {
                   String email = JsonPath.read(signUpJson, "$.email");
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isPresent()) {
                testUserId = userOpt.get().getId();
            } else {
                throw new IllegalStateException("User exists (409), but not found in DB by email: " + email);
            }
        } else {
                       String location = signUpResult.getHeader(HttpHeaders.LOCATION);
            if (location != null) {
                testUserId = UUID.fromString(location.substring(location.lastIndexOf('/') + 1));
            } else {
                throw new IllegalStateException("User was created but Location header is missing");
            }
        }
    }
}