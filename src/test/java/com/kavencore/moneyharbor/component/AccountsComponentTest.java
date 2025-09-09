package com.kavencore.moneyharbor.component;

import com.kavencore.moneyharbor.app.api.model.CreateAccountRequestDto;
import com.kavencore.moneyharbor.app.api.model.CurrencyDto;
import com.kavencore.moneyharbor.app.entity.Account;
import com.kavencore.moneyharbor.app.entity.Currency;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static com.kavencore.moneyharbor.component.TestUtils.extractIdFromLocation;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Accounts API — component tests")
class AccountsComponentTest extends BaseComponentTest {

    @Test
    @DisplayName("Post /accounts - 201, атрибуты сохранены в базе")
    void createOkMapping() throws Exception {
        CreateAccountRequestDto req =
                new CreateAccountRequestDto()
                        .currency(CurrencyDto.RUB)
                        .amount(new BigDecimal("1500.50"))
                        .title("Зарплатный");

        MockHttpServletResponse response = performPost("/accounts", req)
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.title").value("Зарплатный"))
                .andExpect(jsonPath("$.currency").value("RUB"))
                .andExpect(jsonPath("$.amount").value(1500.50))
                .andReturn().getResponse();

        UUID id = extractIdFromLocation(response);

        Optional<Account> saved = accountRepository.findById(id);
        assertThat(saved).isPresent();
        assertThat(saved.get().getTitle()).isEqualTo("Зарплатный");
        assertThat(saved.get().getCurrency().name()).isEqualTo("RUB");
        assertThat(saved.get().getAmount()).isEqualByComparingTo("1500.50");
    }

    @Test
    @DisplayName("POST без title -> title = {currency}_счет в БД")
    void createTitleGenerated() throws Exception {
        CreateAccountRequestDto req = new CreateAccountRequestDto()
                .currency(CurrencyDto.USD)
                .amount(new BigDecimal("0"));

        MockHttpServletResponse response = performPost("/accounts", req)
                .andExpect(header().exists("Location"))
                .andReturn().getResponse();

        UUID id = extractIdFromLocation(response);

        Account account = accountRepository.findById(id).orElseThrow();

        assertThat(account.getTitle()).isEqualTo("USD_счет");
        assertThat(account.getAmount()).isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("DELETE существующего -> 204 и запись удалена")
    void deleteExisting204() throws Exception {
        var acc = new Account();
        acc.setTitle("Test");
        acc.setCurrency(Currency.RUB);
        acc.setAmount(new BigDecimal("1.00"));
        acc = accountRepository.save(acc);

        performDelete("/accounts/" + acc.getId());

        assertThat(accountRepository.findById(acc.getId())).isEmpty();
    }

    @Test
    @DisplayName("DELETE несуществующего -> 200 (идемпотентно)")
    void deleteAbsent200() throws Exception {
        UUID randomId = UUID.randomUUID();
        assertThat(accountRepository.existsById(randomId)).isFalse();

        performDelete("/accounts/" + randomId)
                .andExpect(result -> assertThat(result.getResponse().getStatus()).isEqualTo(200));
    }

    @Test
    @DisplayName("GET несуществующего -> 404 ProblemDetail")
    void getAbsent404() throws Exception {
        UUID missing = UUID.randomUUID();
        mvc.perform(MockMvcRequestBuilders
                        .get("/accounts/" + missing)
                        .accept("application/problem+json"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.detail").value("Account not found: " + missing));
    }
    @Test
    @DisplayName("POST с невалидным телом (неизвестный enum) -> 400 ProblemDetail")
    void postInvalidJson400() throws Exception {
        // currency=EURO - INVALID
        String badJson = """
      { "currency": "EURO", "amount": 10.00 }
      """;

        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/accounts")
                        .contentType("application/json")
                        .accept("application/problem+json")
                        .content(badJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.detail").value("Invalid value in request body"));
    }

    @Test
    @DisplayName("GET /accounts/{id} с невалидным UUID -> 400 ProblemDetail")
    void getInvalidUuid400() throws Exception {
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .get("/accounts/abc") // невалидный UUID
                        .accept("application/problem+json"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.detail").value("Invalid UUID in path parameter 'id'"));
    }

    @Test
    @DisplayName("DELETE /accounts/{id} с невалидным UUID -> 400 ProblemDetail")
    void deleteInvalidUuid400() throws Exception {
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .delete("/accounts/abc")
                        .accept("application/problem+json"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.detail").value("Invalid UUID in path parameter 'id'"));
    }
}
