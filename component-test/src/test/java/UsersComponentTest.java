import com.kavencore.moneyharbor.app.entity.Currency;
import com.kavencore.moneyharbor.app.entity.Role;
import com.kavencore.moneyharbor.app.entity.RoleName;
import com.kavencore.moneyharbor.app.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;
import java.util.UUID;

import static com.kavencore.moneyharbor.app.api.v1.controller.AccountsController.ACCOUNTS_PATH;
import static com.kavencore.moneyharbor.app.api.v1.controller.UserController.GET_PROFILE_PATH;
import static com.kavencore.moneyharbor.app.api.v1.controller.UserController.SIGN_UP_PATH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("User API — sign-up")
class UsersComponentTest extends BaseComponentTest {

    @Test
    @DisplayName("POST /user/sign-up — 201: вернули Location и тело с id/email; email нормализован; пароль захэширован; роль USER выдана")
    void signUp201() throws Exception {
        String json = UserJson.SIGN_UP_OK.load();

        MockHttpServletResponse resp = postJson(SIGN_UP_PATH, json)
                .andExpect(status().isCreated())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(jsonPath("$.id").isString())
                .andExpect(jsonPath("$.email").value(USER_TEST_EMAIL))
                .andReturn().getResponse();

        UUID id = TestUtils.extractIdFromLocation(resp);
        User saved = userRepository.findWithRolesById(id).orElseThrow();

        assertThat(saved.getEmail()).isEqualTo(USER_TEST_EMAIL);

        assertThat(passwordEncoder.matches(TEST_PASSWORD, saved.getPassword())).isTrue();
        assertThat(saved.getPassword()).doesNotContain(TEST_PASSWORD);

        assertThat(saved.getRoles().stream().map(r -> r.getRoleName().name())).contains(RoleName.USER.name());
    }

    @Test
    @DisplayName("POST /user/sign-up — 400: невалидный email/пароль → ProblemDetails с errors")
    void signUp400() throws Exception {
        String json = UserJson.SIGN_UP_INVALID.load();

        postExpectProblem(SIGN_UP_PATH, json)
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                .andExpect(jsonPath("$.title").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
                .andExpect(jsonPath("$.errors[?(@ =~ /.*email.*/)]").isNotEmpty())
                .andExpect(jsonPath("$.errors[?(@ =~ /.*password.*/)]").isNotEmpty());
    }

    @Test
    @DisplayName("POST /user/sign-up — 409: email уже существует → ProblemDetails")
    void signUp409() throws Exception {
        Role roleUser = roleRepository.findByRoleName(RoleName.USER).orElseThrow();
        User existing = User.builder()
                .email("dupe@example.com")
                .password(passwordEncoder.encode(TEST_PASSWORD))
                .roles(List.of(roleUser))
                .build();
        userRepository.save(existing);

        String json = UserJson.SIGN_UP_DUPE.load();
        postExpectProblem(SIGN_UP_PATH, json)
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                .andExpect(jsonPath("$.title").value(HttpStatus.CONFLICT.getReasonPhrase()))
                .andExpect(jsonPath("$.detail", org.hamcrest.Matchers.containsString("Email already registered")));
    }

    @Test
    @DisplayName("GET /user/profile - 200, корректный маппинг")
    void getProfile200() throws Exception {
        String json = AccountJson.CREATE_OK.load();
        performPostAuth(ACCOUNTS_PATH, json);

        mvc.perform(MockMvcRequestBuilders
                        .get(GET_PROFILE_PATH)
                        .with(httpBasic(ACCOUNT_TEST_EMAIL, TEST_PASSWORD))
                        .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value(ACCOUNT_TEST_EMAIL))
                .andExpect(MockMvcResultMatchers.jsonPath("$.accounts[*].currency", hasItem(Currency.RUB.name())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.accounts[*].title", hasItem("Зарплатный")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.accounts[*].amount", hasItem(1500.50)));
    }

    @Test
    @DisplayName("GET /user/profile без авторизации -> 401 Unauthorized")
    void profileWithoutAuth401() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                        .get(GET_PROFILE_PATH)
                        .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /user/profile - 200, в случае если аккаунтов нету - возращаем пустой список")
    void getProfile200WithoutAccounts() throws Exception {
        String json = UserJson.SIGN_UP_WITHOUT_ACCOUNT.load();

        postJson(SIGN_UP_PATH, json);

        mvc.perform(MockMvcRequestBuilders
                        .get(GET_PROFILE_PATH)
                        .with(httpBasic("withoutacc@mail.ru", "Abcdefg1"))
                        .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.accounts", notNullValue()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.accounts", hasSize(0)));
    }
}
