import com.kavencore.moneyharbor.app.entity.Account;
import com.kavencore.moneyharbor.app.entity.Currency;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static com.kavencore.moneyharbor.app.api.v1.controller.AccountsController.ACCOUNTS_PATH;
import static com.kavencore.moneyharbor.app.api.v1.controller.AccountsController.ACCOUNTS_PATH_WITH_SLASH;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Accounts API — component tests")
class AccountsComponentTest extends BaseComponentTest {


    @Test
    @DisplayName("Post /accounts - 201, атрибуты сохранены в базе")
    void createOkMapping() throws Exception {
        String json = AccountJson.CREATE_OK.load();

        MockHttpServletResponse response = performPost(ACCOUNTS_PATH, json)
                .andExpect(MockMvcResultMatchers.header().exists("Location"))
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

        MockHttpServletResponse response = performPost(ACCOUNTS_PATH, json)
                .andExpect(MockMvcResultMatchers.header().exists("Location"))
                .andReturn().getResponse();

        UUID id = TestUtils.extractIdFromLocation(response);

        Account account = accountRepository.findById(id).orElseThrow();

        assertThat(account.getTitle()).isEqualTo("USD_счет");
        assertThat(account.getAmount()).isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("DELETE существующего -> 204 и запись удалена")
    void deleteExisting204() throws Exception {
        Account acc = new Account();
        acc.setTitle("Test");
        acc.setCurrency(Currency.RUB);
        acc.setAmount(new BigDecimal("1.00"));
        acc = accountRepository.save(acc);

        performDelete(ACCOUNTS_PATH_WITH_SLASH + acc.getId());

        assertThat(accountRepository.findById(acc.getId())).isEmpty();
    }

    @Test
    @DisplayName("DELETE несуществующего -> 200 (идемпотентно)")
    void deleteAbsent200() throws Exception {
        UUID randomId = UUID.randomUUID();
        assertThat(accountRepository.existsById(randomId)).isFalse();

        performDelete(ACCOUNTS_PATH_WITH_SLASH + randomId)
                .andExpect(result -> Assertions.assertThat(result.getResponse().getStatus()).isEqualTo(200));
    }

    @Test
    @DisplayName("GET несуществующего -> 404 ProblemDetail")
    void getAbsent404() throws Exception {
        UUID missing = UUID.randomUUID();
        mvc.perform(MockMvcRequestBuilders
                        .get(ACCOUNTS_PATH_WITH_SLASH + missing)
                        .accept("application/problem+json"))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Not Found"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.detail").value("Account not found: " + missing));
    }
    @Test
    @DisplayName("POST с невалидным телом (неизвестный enum) -> 400 ProblemDetail")
    void postInvalidJson400() throws Exception {
        String badJson = AccountJson.CREATE_INVALID_ENUM.load();

        mvc.perform(MockMvcRequestBuilders
                        .post(ACCOUNTS_PATH)
                        .contentType("application/json")
                        .accept("application/problem+json")
                        .content(badJson))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Bad Request"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.detail").value("Invalid value in request body"));
    }

    @Test
    @DisplayName("GET /accounts/{id} с невалидным UUID -> 400 ProblemDetail")
    void getInvalidUuid400() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                        .get(ACCOUNTS_PATH_WITH_SLASH + "abc")
                        .accept("application/problem+json"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Bad Request"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.detail").value("Invalid UUID in path parameter 'id'"));
    }

    @Test
    @DisplayName("DELETE /accounts/{id} с невалидным UUID -> 400 ProblemDetail")
    void deleteInvalidUuid400() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                        .delete(ACCOUNTS_PATH_WITH_SLASH + "abc")
                        .accept("application/problem+json"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Bad Request"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.detail").value("Invalid UUID in path parameter 'id'"));
    }

    @Test
    @DisplayName("POST: currency не из enum -> 400 ProblemDetail (HttpMessageNotReadable)")
    void postCurrencyNotInEnum400() throws Exception {
        String json = AccountJson.CREATE_WITH_CURRENCY_NOT_IN_ENUM.load();

        mvc.perform(MockMvcRequestBuilders.post(ACCOUNTS_PATH)
                        .contentType("application/json")
                        .accept("application/problem+json")
                        .content(json))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Bad Request"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.detail").value("Invalid value in request body"));
    }

    @Test
    @DisplayName("POST: не передано обязательное поле currency -> 400 ProblemDetail (Bean Validation)")
    void postMissingCurrency400() throws Exception {
        String json = AccountJson.CREATE_MISSING_CURRENCY.load();

        mvc.perform(MockMvcRequestBuilders.post(ACCOUNTS_PATH)
                        .contentType("application/json")
                        .accept("application/problem+json")
                        .content(json))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Bad Request"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0]").value(org.hamcrest.Matchers.containsString("currency")));
    }

    @Test
    @DisplayName("POST: amount scale != 2 -> 400 ProblemDetail (Bean Validation)")
    void postAmountScale3_400() throws Exception {
        String json = AccountJson.CREATE_AMOUNT_SCALE_3.load();

        mvc.perform(MockMvcRequestBuilders.post(ACCOUNTS_PATH)
                        .contentType("application/json")
                        .accept("application/problem+json")
                        .content(json))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Bad Request"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0]").value(org.hamcrest.Matchers.containsString("amount")));
    }

    @Test
    @DisplayName("POST: amount integer digits > 17 (для NUMERIC(19,2)) -> 400 ProblemDetail (Bean Validation)")
    void postAmountIntegerTooLong400() throws Exception {
        String json = AccountJson.CREATE_AMOUNT_INTEGER_TOO_LONG.load();

        mvc.perform(MockMvcRequestBuilders.post(ACCOUNTS_PATH)
                        .contentType("application/json")
                        .accept("application/problem+json")
                        .content(json))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Bad Request"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0]").value(org.hamcrest.Matchers.containsString("amount")));
    }

    @Test
    @DisplayName("POST: title некорректного типа -> 400 ProblemDetail (HttpMessageNotReadable)")
    void postTitleWrongType400() throws Exception {
        String json = AccountJson.CREATE_TITLE_WRONG_TYPE.load();

        mvc.perform(MockMvcRequestBuilders.post(ACCOUNTS_PATH)
                        .contentType("application/json")
                        .accept("application/problem+json")
                        .content(json))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Bad Request"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.detail").value("Invalid value in request body"));
    }
}
