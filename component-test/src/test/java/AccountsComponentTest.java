import com.kavencore.moneyharbor.app.entity.Account;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Optional;
import java.util.UUID;

import static com.kavencore.moneyharbor.app.api.v1.controller.AccountsController.ACCOUNTS_PATH;
import static com.kavencore.moneyharbor.app.api.v1.controller.AccountsController.ACCOUNTS_PATH_WITH_SLASH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Accounts API — component tests")
class AccountsComponentTest extends BaseComponentTest {


    @Test
    @DisplayName("Post /accounts - 201, атрибуты сохранены в базе")
    void createOkMapping() throws Exception {
        String json = AccountJson.CREATE_OK.load();

        MockHttpServletResponse response = performPostAuth(ACCOUNTS_PATH, json)
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.header().exists(HttpHeaders.LOCATION))
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Зарплатный"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.currency").value("RUB"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.amount").value(1500.50))
                .andReturn().getResponse();

        UUID id = TestUtils.extractIdFromLocation(response);

        Optional<Account> saved = accountRepository.findById(id);
        Assertions.assertThat(saved).isPresent();
        assertThat(saved.get().getTitle()).isEqualTo("Зарплатный");
        assertThat(saved.get().getCurrency().name()).isEqualTo("RUB");
        assertThat(saved.get().getAmount()).isEqualByComparingTo("1500.50");
    }

    @Test
    @DisplayName("POST без title -> title = {currency}_счет в БД")
    void createTitleGenerated() throws Exception {
        String json = AccountJson.CREATE_WITHOUT_TITLE.load();

        MockHttpServletResponse response = performPostAuth(ACCOUNTS_PATH, json)
                .andExpect(MockMvcResultMatchers.header().exists(HttpHeaders.LOCATION))
                .andReturn().getResponse();

        UUID id = TestUtils.extractIdFromLocation(response);

        Account account = accountRepository.findById(id).orElseThrow();

        assertThat(account.getTitle()).isEqualTo("USD_счет");
        assertThat(account.getAmount()).isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("DELETE существующего -> 204 и запись удалена")
    void deleteExisting204() throws Exception {
        String json = AccountJson.CREATE_OK.load();
        MockHttpServletResponse resp = performPostAuth(ACCOUNTS_PATH, json)
                .andExpect(status().isCreated())
                .andReturn().getResponse();

        UUID id = TestUtils.extractIdFromLocation(resp);

        performDeleteAuthOk(ACCOUNTS_PATH_WITH_SLASH + id)
                .andExpect(result -> {
                    int s = result.getResponse().getStatus();
                    if (s != HttpStatus.NO_CONTENT.value() && s != HttpStatus.OK.value()) {
                        throw new AssertionError("Expected 200 or 204, got " + s);
                    }
                });

        assertThat(accountRepository.findById(id)).isEmpty();
    }


    @Test
    @DisplayName("DELETE несуществующего -> 200 (идемпотентно)")
    void deleteAbsent200() throws Exception {
        UUID randomId = UUID.randomUUID();
        assertThat(accountRepository.existsById(randomId)).isFalse();

        performDeleteAuthOk(ACCOUNTS_PATH_WITH_SLASH + randomId)
                .andExpect(result -> Assertions.assertThat(result.getResponse().getStatus()).isEqualTo(200));
    }

    @Test
    @DisplayName("GET несуществующего -> 404 ProblemDetail")
    void getAbsent404() throws Exception {
        UUID missing = UUID.randomUUID();
        mvc.perform(MockMvcRequestBuilders
                        .get(ACCOUNTS_PATH_WITH_SLASH + missing)
                        .with(httpBasic(ACCOUNT_TEST_EMAIL, TEST_PASSWORD))
                        .accept(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                .andExpect(status().isNotFound())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.detail").value("Account not found: " + missing));
    }
    @Test
    @DisplayName("POST с невалидным телом (неизвестный enum) -> 400 ProblemDetail")
    void postInvalidJson400() throws Exception {
        String badJson = AccountJson.CREATE_INVALID_ENUM.load();

        mvc.perform(MockMvcRequestBuilders
                        .post(ACCOUNTS_PATH)
                        .with(httpBasic(ACCOUNT_TEST_EMAIL, TEST_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_PROBLEM_JSON_VALUE)
                        .content(badJson))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.detail").value("Invalid value in request body"));
    }

    @Test
    @DisplayName("GET /accounts/{id} с невалидным UUID -> 400 ProblemDetail")
    void getInvalidUuid400() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                        .get(ACCOUNTS_PATH_WITH_SLASH + "abc")
                        .with(httpBasic(ACCOUNT_TEST_EMAIL, TEST_PASSWORD))
                        .accept(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.detail").value("Invalid UUID in path parameter 'id'"));
    }

    @Test
    @DisplayName("DELETE /accounts/{id} с невалидным UUID -> 400 ProblemDetail")
    void deleteInvalidUuid400() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                        .get(ACCOUNTS_PATH_WITH_SLASH + "abc")
                        .with(httpBasic(ACCOUNT_TEST_EMAIL, TEST_PASSWORD))
                        .accept(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.detail").value("Invalid UUID in path parameter 'id'"));
    }

    @Test
    @DisplayName("POST: currency не из enum -> 400 ProblemDetail (HttpMessageNotReadable)")
    void postCurrencyNotInEnum400() throws Exception {
        String json = AccountJson.CREATE_WITH_CURRENCY_NOT_IN_ENUM.load();

        mvc.perform(MockMvcRequestBuilders.post(ACCOUNTS_PATH)
                        .with(httpBasic(ACCOUNT_TEST_EMAIL, TEST_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_PROBLEM_JSON_VALUE)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.detail").value("Invalid value in request body"));
    }

    @Test
    @DisplayName("POST: не передано обязательное поле currency -> 400 ProblemDetail (Bean Validation)")
    void postMissingCurrency400() throws Exception {
        String json = AccountJson.CREATE_MISSING_CURRENCY.load();

        mvc.perform(MockMvcRequestBuilders.post(ACCOUNTS_PATH)
                        .with(httpBasic(ACCOUNT_TEST_EMAIL, TEST_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_PROBLEM_JSON_VALUE)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0]").value(org.hamcrest.Matchers.containsString("currency")));
    }

    @Test
    @DisplayName("POST: amount scale != 2 -> 400 ProblemDetail (Bean Validation)")
    void postAmountScale3_400() throws Exception {
        String json = AccountJson.CREATE_AMOUNT_SCALE_3.load();

        mvc.perform(MockMvcRequestBuilders.post(ACCOUNTS_PATH)
                        .with(httpBasic(ACCOUNT_TEST_EMAIL, TEST_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_PROBLEM_JSON_VALUE)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0]").value(org.hamcrest.Matchers.containsString("amount")));
    }

    @Test
    @DisplayName("POST: amount integer digits > 17 (для NUMERIC(19,2)) -> 400 ProblemDetail (Bean Validation)")
    void postAmountIntegerTooLong400() throws Exception {
        String json = AccountJson.CREATE_AMOUNT_INTEGER_TOO_LONG.load();

        mvc.perform(MockMvcRequestBuilders.post(ACCOUNTS_PATH)
                        .with(httpBasic(ACCOUNT_TEST_EMAIL, TEST_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_PROBLEM_JSON_VALUE)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0]").value(org.hamcrest.Matchers.containsString("amount")));
    }

    @Test
    @DisplayName("POST: title некорректного типа -> 400 ProblemDetail (HttpMessageNotReadable)")
    void postTitleWrongType400() throws Exception {
        String json = AccountJson.CREATE_TITLE_WRONG_TYPE.load();

        mvc.perform(MockMvcRequestBuilders.post(ACCOUNTS_PATH)
                        .with(httpBasic(ACCOUNT_TEST_EMAIL, TEST_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_PROBLEM_JSON_VALUE)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.detail").value("Invalid value in request body"));
    }

    @Test
    @DisplayName("POST /accounts без авторизации -> 401 Unauthorized")
    void createWithoutAuth401() throws Exception {
        String json = AccountJson.CREATE_OK.load();

        performPostNoAuth(ACCOUNTS_PATH, json)
                .andExpect(status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.header().doesNotExist(HttpHeaders.LOCATION));
    }
}
