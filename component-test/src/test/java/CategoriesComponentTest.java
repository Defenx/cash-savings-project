import com.kavencore.moneyharbor.app.entity.*;
import com.kavencore.moneyharbor.app.infrastructure.repository.AccountRepository;
import com.kavencore.moneyharbor.app.infrastructure.repository.CategoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.springframework.transaction.annotation.Transactional;

import static com.kavencore.moneyharbor.app.api.v1.controller.CategoriesController.CATEGORIES_PATH;
import static com.kavencore.moneyharbor.app.api.v1.controller.UserController.SIGN_UP_PATH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.kavencore.moneyharbor.app.infrastructure.repository.OperationRepository;
import com.kavencore.moneyharbor.app.infrastructure.repository.UserRepository;

@Transactional
@DisplayName("Categories API — component tests")
class CategoriesComponentTest extends AuthenticatedComponentTestBase {

    @Autowired
    protected CategoryRepository categoryRepository;
    @Autowired
    protected OperationRepository operationRepository;

    @Autowired
    protected UserRepository userRepository;
    @Autowired
    private AccountRepository accountRepository;

    @Test
    @DisplayName("POST /categories — 201, атрибуты сохранены в базе")
    void createCategoryOkMapping() throws Exception {
        String json = CategoryJson.CREATE_OK.load();

        MockHttpServletResponse response = performPostAuth(CATEGORIES_PATH, json)
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.header().exists(HttpHeaders.LOCATION))
                .andExpect(jsonPath("$.name").value("Зарплата"))
                .andExpect(jsonPath("$.type").value("INCOME"))
                .andReturn().getResponse();

        UUID id = TestUtils.extractIdFromLocation(response);

        Optional<Category> saved = categoryRepository.findById(id);
        assertThat(saved).isPresent();
        assertThat(saved.get().getName()).isEqualTo("Зарплата");
        assertThat(saved.get().getType().name()).isEqualTo("INCOME");
        assertThat(saved.get().getUser().getId()).isEqualTo(testUserId);
    }

    @Test
    @DisplayName("POST /categories — 400, если name = null")
    void createCategoryWithNullName400() throws Exception {
        String json = CategoryJson.CREATE_WITH_NULL_NAME.load();

        performPostAuth(CATEGORIES_PATH, json)
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[0]").value(org.hamcrest.Matchers.containsString("name")));
    }

    @Test
    @DisplayName("POST /categories — 400, если type не из перечня")
    void createCategoryWithInvalidType400() throws Exception {
        String json = CategoryJson.CREATE_WITH_INVALID_TYPE.load();

        performPostAuth(CATEGORIES_PATH, json)
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value(HttpStatus.BAD_REQUEST.getReasonPhrase()));
    }

    @Test
    @DisplayName("POST /categories без авторизации — 401 Unauthorized")
    void createCategoryWithoutAuth401() throws Exception {
        String json = CategoryJson.CREATE_OK.load();

        performPostNoAuth(CATEGORIES_PATH, json)
                .andExpect(status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.header().doesNotExist(HttpHeaders.LOCATION));
    }

    @Test
    @DisplayName("Проверяем, успешное удаление категории")
    void deleteCategoryOk204() throws Exception {
        Category category = givenCategory(testUserId);
        String URL = CATEGORIES_PATH + "/" + category.getId();
        performDeleteAuthOk(URL)
                .andExpect(status().isNoContent());
        assertThat(categoryRepository.findById(category.getId())).isEmpty();
    }



    @Test
    @DisplayName("DELETE /categories/{id} — 401, попытка удаления неавторизованным пользователем")
    void deleteCategoryUnauthorizedUser401() throws Exception {

        Category category = givenCategory(testUserId);

        String URL = CATEGORIES_PATH + "/" + category.getId();

        performDeleteNoAuth(URL)
                .andExpect(status().isUnauthorized());
        assertThat(categoryRepository.findById(category.getId())).isPresent();
    }

    @Test
    @DisplayName("DELETE /categories/{id} — 403, попытка удаления категории принадлежащей другому пользователю")
    void deleteAnotherUserCategory403() throws Exception {


        String SignUpJson = UserJson.SIGN_UP_OK.load();
        MockHttpServletResponse response = performPostNoAuth(
                SIGN_UP_PATH, SignUpJson
        ).andExpect(status().isCreated())
                .andReturn().getResponse();

        UUID anotherUserId = TestUtils.extractIdFromLocation(response);


        Category anotherUserCategory = givenCategory(anotherUserId);


        String URL = CATEGORIES_PATH + "/" + anotherUserCategory.getId();

        performDeleteAuthOk(URL).
                andExpect(status().isForbidden());

        assertThat(categoryRepository.findById(anotherUserCategory.getId())).isPresent();


    }
    @Test
    @DisplayName("DELETE /categories/{id} — 409, категория используется в операции")
    void deleteCategoryUsedInOperation409() throws Exception {


        Category category = givenCategory(testUserId);

        Account userAccount = new Account();
        userAccount.setUser(userRepository.getReferenceById(testUserId));
        userAccount.setCurrency(Currency.USD);
        userAccount.setTitle("Тестовый счет для 409");
        userAccount.setAmount(BigDecimal.ZERO);
        accountRepository.save(userAccount);

        Operation operation = new Operation();
        operation.setCategory(category);
        operation.setAmount(new BigDecimal(100L));
        operation.setCurrency(Currency.USD);
        operation.setAccount(userAccount);
        operation.setDate(LocalDate.now());

        operationRepository.save(operation);

        String URL = CATEGORIES_PATH + "/" + category.getId();
        performDeleteAuthOk(URL)
                .andExpect(status().isConflict());
        assertThat(categoryRepository.findById(category.getId())).isPresent();
    }

    private Category givenCategory(UUID userId) throws Exception {
        Category category = new Category();
        category.setName("Категория для удаления");
        category.setType(Type.EXPENSE);
        category.setUser(userRepository.getReferenceById(userId));
        category = categoryRepository.save(category);
        return category;
    }
}