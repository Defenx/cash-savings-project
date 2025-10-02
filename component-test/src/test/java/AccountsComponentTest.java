import com.jayway.jsonpath.JsonPath;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static com.kavencore.moneyharbor.app.api.v1.controller.AccountsController.ACCOUNTS_PATH;
import static com.kavencore.moneyharbor.app.api.v1.controller.AccountsController.ACCOUNTS_PATH_WITH_SLASH;
import static com.kavencore.moneyharbor.app.api.v1.controller.UserController.GET_PROFILE_PATH;
import static com.kavencore.moneyharbor.app.api.v1.controller.UserController.SIGN_UP_PATH;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@DisplayName("Accounts API — component tests")
class AccountsComponentTest extends BaseComponentTest {

    @BeforeEach
    void ensureTestUser() throws Exception {
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
            testUserId = UUID.fromString(location.substring(location.lastIndexOf('/') + "/".length()));
        }
    }


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

        ResultActions resultActions = performGetAuth(ACCOUNTS_PATH_WITH_SLASH + id);
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Зарплатный"));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.currency").value("RUB"));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.amount").value(1500.50));
    }

    @Test
    @DisplayName("POST без title -> title = {currency}_счет_{number} в БД")
    void createTitleInFirstAccount() throws Exception {
        String json = AccountJson.CREATE_WITHOUT_TITLE.load();

        MockHttpServletResponse response = performPostAuth(ACCOUNTS_PATH, json)
                .andExpect(MockMvcResultMatchers.header().exists(HttpHeaders.LOCATION))
                .andReturn().getResponse();

        UUID id = TestUtils.extractIdFromLocation(response);

        ResultActions resultActions = performGetAuth(ACCOUNTS_PATH_WITH_SLASH + id);

        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.title").value("USD_счет_1"));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.currency").value("USD"));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.amount").value(0.00));

    }

    @Test
    @DisplayName("POST /accounts: если есть счета {валюта_счёта}_1 в разных валютах, то новый USD-счет будет USD_счет_2")
    void createTitleInNewAccountForUserWhoHaveAccounts() throws Exception {

        performPostAuth(ACCOUNTS_PATH, AccountJson.CREATE_STANDARD_USD.load())
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("USD_счет_1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.currency").value("USD"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.amount").value(0.00));


        performPostAuth(ACCOUNTS_PATH, AccountJson.CREATE_STANDARD_RUB.load())
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("RUB_счет_1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.currency").value("RUB"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.amount").value(0.00));

        performPostAuth(ACCOUNTS_PATH, AccountJson.CREATE_STANDARD_RUB.load())
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("RUB_счет_1"))
                // TODO: проанализировать возможность добавить ограничение на одинаковые названия счета у одного пользователя
                .andExpect(MockMvcResultMatchers.jsonPath("$.currency").value("RUB"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.amount").value(0.00));

        performPostAuth(ACCOUNTS_PATH, AccountJson.CREATE_WITHOUT_TITLE.load())
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("USD_счет_2"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.currency").value("USD"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.amount").value(0.00));

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

        performGetAuth(ACCOUNTS_PATH_WITH_SLASH + id)
                .andExpect(status().isNotFound());
    }


    @Test
    @DisplayName("DELETE несуществующего -> 200 (идемпотентно)")
    void deleteAbsent200() throws Exception {
        UUID randomId = UUID.randomUUID();
        performGetAuth(ACCOUNTS_PATH_WITH_SLASH + randomId)
                .andExpect(status().isNotFound());

        performDeleteAuthOk(ACCOUNTS_PATH_WITH_SLASH + randomId)
                .andExpect(result -> Assertions.assertThat(result.getResponse().getStatus()).isEqualTo(200));
    }

    @Test
    @DisplayName("GET несуществующего -> 404 ProblemDetail")
    void getAbsent404() throws Exception {
        UUID missing = UUID.randomUUID();
        performGetAuth(ACCOUNTS_PATH_WITH_SLASH + missing)
                .andExpect(status().isNotFound())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.detail").value("Account not found: " + missing));
    }

    @Test
    @DisplayName("POST с невалидным телом (неизвестный enum) -> 400 ProblemDetail")
    void postInvalidJson400() throws Exception {
        String badJson = AccountJson.CREATE_INVALID_ENUM.load();

        performPostAuth(ACCOUNTS_PATH, badJson)
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.detail").value("Invalid value in request body"));
    }

    @Test
    @DisplayName("GET /accounts/{id} с невалидным UUID -> 400 ProblemDetail")
    void getInvalidUuid400() throws Exception {
        performGetAuth(ACCOUNTS_PATH_WITH_SLASH + "abc")
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.detail").value("Invalid UUID in path parameter 'id'"));
    }

    @Test
    @DisplayName("DELETE /accounts/{id} с невалидным UUID -> 400 ProblemDetail")
    void deleteInvalidUuid400() throws Exception {
        performDeleteAuthOk(ACCOUNTS_PATH_WITH_SLASH + "abc")
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.detail").value("Invalid UUID in path parameter 'id'"));
    }

    @Test
    @DisplayName("POST: currency не из enum -> 400 ProblemDetail (HttpMessageNotReadable)")
    void postCurrencyNotInEnum400() throws Exception {
        String json = AccountJson.CREATE_WITH_CURRENCY_NOT_IN_ENUM.load();

        performPostAuth(ACCOUNTS_PATH, json)
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.detail").value("Invalid value in request body"));
    }

    @Test
    @DisplayName("POST: не передано обязательное поле currency -> 400 ProblemDetail (Bean Validation)")
    void postMissingCurrency400() throws Exception {
        String json = AccountJson.CREATE_MISSING_CURRENCY.load();

        performPostAuth(ACCOUNTS_PATH, json)
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0]").value(org.hamcrest.Matchers.containsString("currency")));
    }

    @Test
    @DisplayName("POST: amount scale != 2 -> 400 ProblemDetail (Bean Validation)")
    void postAmountScale3_400() throws Exception {
        String json = AccountJson.CREATE_AMOUNT_SCALE_3.load();

        performPostAuth(ACCOUNTS_PATH, json)
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0]").value(org.hamcrest.Matchers.containsString("amount")));
    }

    @Test
    @DisplayName("POST: amount integer digits > 17 (для NUMERIC(19,2)) -> 400 ProblemDetail (Bean Validation)")
    void postAmountIntegerTooLong400() throws Exception {
        String json = AccountJson.CREATE_AMOUNT_INTEGER_TOO_LONG.load();

        performPostAuth(ACCOUNTS_PATH, json)
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0]").value(org.hamcrest.Matchers.containsString("amount")));
    }

    @Test
    @DisplayName("POST: title некорректного типа -> 400 ProblemDetail (HttpMessageNotReadable)")
    void postTitleWrongType400() throws Exception {
        String json = AccountJson.CREATE_TITLE_WRONG_TYPE.load();

        performPostAuth(ACCOUNTS_PATH, json)
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
