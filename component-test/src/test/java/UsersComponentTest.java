import com.kavencore.moneyharbor.app.entity.Role;
import com.kavencore.moneyharbor.app.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("User API — sign-up")
class UsersComponentTest extends BaseComponentTest {

    private static final String SIGN_UP = "/user/sign-up";

    @Test
    @DisplayName("POST /user/sign-up — 201: вернули Location и тело с id/email; email нормализован; пароль захэширован; роль USER выдана")
    void signUp201() throws Exception {
        String json = UserJson.SIGN_UP_OK.load();

        MockHttpServletResponse resp = postJson(SIGN_UP, json)
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isString())
                .andExpect(jsonPath("$.email").value("alice@example.com")) // нормализация
                .andReturn().getResponse();

        UUID id = TestUtils.extractIdFromLocation(resp);
        User saved = userRepository.findWithRolesById(id).orElseThrow();

        assertThat(saved.getEmail()).isEqualTo("alice@example.com");

        assertThat(passwordEncoder.matches("Abcdefg1", saved.getPassword())).isTrue();
        assertThat(saved.getPassword()).doesNotContain("Abcdefg1");

        assertThat(saved.getRoles().stream().map(Role::getName)).contains("USER");
    }

    @Test
    @DisplayName("POST /user/sign-up — 400: невалидный email/пароль → ProblemDetails с errors")
    void signUp400() throws Exception {
        String json = UserJson.SIGN_UP_INVALID.load();

        postExpectProblem(SIGN_UP, json)
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.errors[?(@ =~ /.*email.*/)]").isNotEmpty())
                .andExpect(jsonPath("$.errors[?(@ =~ /.*password.*/)]").isNotEmpty());
    }

    @Test
    @DisplayName("POST /user/sign-up — 409: email уже существует → ProblemDetails")
    void signUp409() throws Exception {
        Role roleUser = roleRepository.findByName("USER").orElseThrow();
        User existing = User.builder()
                .email("dupe@example.com")
                .password(passwordEncoder.encode("Abcdefg1"))
                .roles(Set.of(roleUser))
                .build();
        userRepository.save(existing);

        String json = UserJson.SIGN_UP_DUPE.load();
        postExpectProblem(SIGN_UP, json)
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.title").value("Conflict"))
                .andExpect(jsonPath("$.detail", org.hamcrest.Matchers.containsString("Email already registered")));
    }
}
