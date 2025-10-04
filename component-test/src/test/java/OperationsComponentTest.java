import com.kavencore.moneyharbor.app.api.v1.controller.UserController;
import com.kavencore.moneyharbor.app.entity.Account;
import com.kavencore.moneyharbor.app.entity.Currency;
import com.kavencore.moneyharbor.app.entity.Operation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.kavencore.moneyharbor.app.api.v1.controller.AccountsController.ACCOUNTS_PATH;
import static com.kavencore.moneyharbor.app.api.v1.controller.CategoriesController.CATEGORIES_PATH;
import static com.kavencore.moneyharbor.app.api.v1.controller.OperationController.OPERATIONS_PATH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@DisplayName("Operations API — компонентные тесты")
public class OperationsComponentTest extends BaseComponentTest {

    @Test
    @DisplayName("POST /operation — 201, доход, баланс увеличен")
    public void createIncome201() throws Exception {
        UUID accountId = createAccountForTestUser(AccountJson.CREATE_STANDARD_USD);
        UUID categoryId = createCategoryForTestUser(CategoryJson.CREATE_INCOME, "Зарплата");

        String body = patchJson(OperationJson.CREATE_INCOME_OK.load(), Map.of(
                "account_id", accountId.toString(),
                "category_id", categoryId.toString()
        ));

        MockHttpServletResponse response = performPostAuth(OPERATIONS_PATH, body)
                .andExpect(status().isCreated())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andReturn()
                .getResponse();

        UUID operationId = TestUtils.extractIdFromLocation(response);

        Operation saved = operationRepository.findById(operationId).orElseThrow();
        assertThat(saved.getAmount()).isEqualByComparingTo("1500.00");
        assertThat(saved.getCurrency()).isEqualTo(Currency.USD);
        assertThat(saved.getAccount().getId()).isEqualTo(accountId);
        assertThat(saved.getCategory().getId()).isEqualTo(categoryId);
        assertThat(saved.getDate()).isEqualTo(LocalDate.of(2025, 10, 4));

        Account updatedAccount = accountRepository.findById(accountId).orElseThrow();
        assertThat(updatedAccount.getAmount()).isEqualByComparingTo("1500.00");
    }

    @Test
    @DisplayName("POST /operation — 201, расход без даты, баланс уменьшен")
    public void createExpenseWithoutDate201() throws Exception {
        UUID accountId = createAccountForTestUser(AccountJson.CREATE_STANDARD_RUB);
        UUID categoryId = createCategoryForTestUser(CategoryJson.CREATE_EXPENSE, "Продукты");

        String body = patchJson(OperationJson.CREATE_EXPENSE_OK.load(), Map.of(
                "account_id", accountId.toString(),
                "category_id", categoryId.toString()
        ));

        MockHttpServletResponse response = performPostAuth(OPERATIONS_PATH, body)
                .andExpect(status().isCreated())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andReturn()
                .getResponse();

        UUID operationId = TestUtils.extractIdFromLocation(response);

        Operation saved = operationRepository.findById(operationId).orElseThrow();
        assertThat(saved.getAmount()).isEqualByComparingTo("-1250.50");
        assertThat(saved.getCurrency()).isEqualTo(Currency.RUB);
        assertThat(saved.getDate()).isEqualTo(LocalDate.now());

        Account updatedAccount = accountRepository.findById(accountId).orElseThrow();
        assertThat(updatedAccount.getAmount()).isEqualByComparingTo("-1250.50");
    }

    @Test
    @DisplayName("POST /operation — 400, расход с положительной суммой")
    public void expensePositiveAmount400() throws Exception {
        UUID accountId = createAccountForTestUser(AccountJson.CREATE_STANDARD_RUB);
        UUID categoryId = createCategoryForTestUser(CategoryJson.CREATE_EXPENSE, "Поездки");

        String body = patchJson(OperationJson.CREATE_EXPENSE_POSITIVE.load(), Map.of(
                "account_id", accountId.toString(),
                "category_id", categoryId.toString()
        ));

        postAuthExpectProblem(OPERATIONS_PATH, body)
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("Expense amount must be negative"));
    }

    @Test
    @DisplayName("POST /operation — 400, доход с отрицательной суммой")
    public void incomeNegativeAmount400() throws Exception {
        UUID accountId = createAccountForTestUser(AccountJson.CREATE_STANDARD_USD);
        UUID categoryId = createCategoryForTestUser(CategoryJson.CREATE_INCOME, "Премия");

        String body = patchJson(OperationJson.CREATE_INCOME_NEGATIVE.load(), Map.of(
                "account_id", accountId.toString(),
                "category_id", categoryId.toString()
        ));

        postAuthExpectProblem(OPERATIONS_PATH, body)
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("Income amount must be positive"));
    }

    @Test
    @DisplayName("POST /operation — 400, сумма равна нулю")
    public void zeroAmount400() throws Exception {
        UUID accountId = createAccountForTestUser(AccountJson.CREATE_STANDARD_RUB);
        UUID categoryId = createCategoryForTestUser(CategoryJson.CREATE_EXPENSE, "Прочее");

        String body = patchJson(OperationJson.CREATE_ZERO_AMOUNT.load(), Map.of(
                "account_id", accountId.toString(),
                "category_id", categoryId.toString()
        ));

        postAuthExpectProblem(OPERATIONS_PATH, body)
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("Amount must be non-zero"));
    }

    @Test
    @DisplayName("POST /operation — 400, описание длиннее 100 символов")
    public void descriptionTooLong400() throws Exception {
        UUID accountId = createAccountForTestUser(AccountJson.CREATE_STANDARD_RUB);
        UUID categoryId = createCategoryForTestUser(CategoryJson.CREATE_EXPENSE, "Крупные покупки");

        String body = patchJson(OperationJson.CREATE_DESCRIPTION_TOO_LONG.load(), Map.of(
                "account_id", accountId.toString(),
                "category_id", categoryId.toString()
        ));

        postAuthExpectProblem(OPERATIONS_PATH, body)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]").value("description: size must be between 0 and 100"));
    }

    @Test
    @DisplayName("POST /operation — 404, категория другого пользователя")
    public void foreignCategory404() throws Exception {
        ensureSecondaryUserExists();
        UUID accountId = createAccountForTestUser(AccountJson.CREATE_STANDARD_USD);
        UUID foreignCategoryId = createCategoryForOtherUser(CategoryJson.CREATE_INCOME, "Чужой доход");

        String body = patchJson(OperationJson.CREATE_CATEGORY_FOREIGN.load(), Map.of(
                "account_id", accountId.toString(),
                "category_id", foreignCategoryId.toString()
        ));

        postAuthExpectProblem(OPERATIONS_PATH, body)
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("Category not found: " + foreignCategoryId));
    }

    @Test
    @DisplayName("POST /operation — 404, счёт другого пользователя")
    public void foreignAccount404() throws Exception {
        ensureSecondaryUserExists();
        UUID foreignAccountId = createAccountForOtherUser(AccountJson.CREATE_STANDARD_USD);
        UUID categoryId = createCategoryForTestUser(CategoryJson.CREATE_INCOME, "Собственный доход");

        Map<String, String> replacements = new HashMap<>();
        replacements.put("account_id", foreignAccountId.toString());
        replacements.put("category_id", categoryId.toString());

        String body = patchJson(OperationJson.CREATE_ACCOUNT_FOREIGN.load(), replacements);

        postAuthExpectProblem(OPERATIONS_PATH, body)
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("Account not found: " + foreignAccountId));
    }

    @Test
    @DisplayName("POST /operation — 400, невалидный JSON")
    public void invalidJson400() throws Exception {
        UUID accountId = createAccountForTestUser(AccountJson.CREATE_STANDARD_USD);
        UUID categoryId = createCategoryForTestUser(CategoryJson.CREATE_INCOME, "Некорректный");

        String body = patchJson(OperationJson.CREATE_INVALID_BODY.load(), Map.of(
                "account_id", accountId.toString(),
                "category_id", categoryId.toString()
        ));

        postAuthExpectProblem(OPERATIONS_PATH, body)
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("Invalid value in request body"));
    }

    @Test
    @DisplayName("POST /operation — 401 без авторизации")
    public void unauthorized401() throws Exception {
        UUID accountId = createAccountForTestUser(AccountJson.CREATE_STANDARD_USD);
        UUID categoryId = createCategoryForTestUser(CategoryJson.CREATE_INCOME, "Без авторизации");

        String body = patchJson(OperationJson.CREATE_INCOME_OK.load(), Map.of(
                "account_id", accountId.toString(),
                "category_id", categoryId.toString()
        ));

        performPostNoAuth(OPERATIONS_PATH, body)
                .andExpect(status().isUnauthorized())
                .andExpect(header().doesNotExist(HttpHeaders.LOCATION));
    }

    private UUID createAccountForTestUser(AccountJson template) throws Exception {
        MockHttpServletResponse response = performPostAuth(ACCOUNTS_PATH, template.load())
                .andExpect(status().isCreated())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andReturn()
                .getResponse();
        return TestUtils.extractIdFromLocation(response);
    }

    private UUID createCategoryForTestUser(CategoryJson template, String name) throws Exception {
        String body = patchJson(template.load(), Map.of("name", name));
        MockHttpServletResponse response = performPostAuth(CATEGORIES_PATH, body)
                .andExpect(status().isCreated())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andReturn()
                .getResponse();
        return TestUtils.extractIdFromLocation(response);
    }

    private void ensureSecondaryUserExists() throws Exception {
        MockHttpServletResponse response = performPostNoAuth(UserController.SIGN_UP_PATH, UserJson.SIGN_UP_OK.load())
                .andReturn()
                .getResponse();
        int status = response.getStatus();
        if (status != HttpStatus.CREATED.value() && status != HttpStatus.CONFLICT.value()) {
            throw new IllegalStateException("Unexpected status: " + status);
        }
    }

    private UUID createCategoryForOtherUser(CategoryJson template, String name) throws Exception {
        String body = patchJson(template.load(), Map.of("name", name));
        MockHttpServletResponse response = performPostAuthAs(USER_TEST_EMAIL, TEST_PASSWORD, CATEGORIES_PATH, body)
                .andExpect(status().isCreated())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andReturn()
                .getResponse();
        return TestUtils.extractIdFromLocation(response);
    }

    private UUID createAccountForOtherUser(AccountJson template) throws Exception {
        MockHttpServletResponse response = performPostAuthAs(USER_TEST_EMAIL, TEST_PASSWORD, ACCOUNTS_PATH, template.load())
                .andExpect(status().isCreated())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andReturn()
                .getResponse();
        return TestUtils.extractIdFromLocation(response);
    }
}