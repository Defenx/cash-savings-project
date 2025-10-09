import com.kavencore.moneyharbor.app.infrastructure.repository.CategoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Optional;
import java.util.UUID;

import com.kavencore.moneyharbor.app.entity.Category;
import org.springframework.transaction.annotation.Transactional;

import static com.kavencore.moneyharbor.app.api.v1.controller.CategoriesController.CATEGORIES_PATH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@DisplayName("Categories API — component tests")
class CategoriesComponentTest extends AuthenticatedComponentTestBase {

    @Autowired
    protected CategoryRepository categoryRepository;

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
}