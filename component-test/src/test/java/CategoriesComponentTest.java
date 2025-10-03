import com.kavencore.moneyharbor.app.api.model.CreateCategoryRequestDto;
import com.kavencore.moneyharbor.app.entity.Category;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Optional;
import java.util.UUID;

import static com.kavencore.moneyharbor.app.api.v1.controller.CategoriesController.CATEGORIES_PATH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@org.springframework.transaction.annotation.Transactional
@DisplayName("Categories API — component tests")
class CategoriesComponentTest extends BaseComponentTest {

    @Test
    @DisplayName("POST /categories — 201, атрибуты сохранены в базе")
    void createCategoryOkMapping() throws Exception {
        CreateCategoryRequestDto request = new CreateCategoryRequestDto()
                .name("Зарплата")
                .type(CreateCategoryRequestDto.TypeEnum.INCOME);

        String json = objectMapper.writeValueAsString(request);

        var response = performPostAuth(CATEGORIES_PATH, json)
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.header().exists(HttpHeaders.LOCATION))
                .andExpect(jsonPath("$.name").value("Зарплата"))
                .andExpect(jsonPath("$.type").value("INCOME"))
                .andReturn().getResponse();

        UUID id = TestUtils.extractIdFromLocation(response);

        Optional<Category> saved = categoryRepository.findById(id);
        assertThat(saved).isPresent();
        assertThat(saved.get().getName()).isEqualTo("Зарплата");
        assertThat(saved.get().getType().name()).isEqualTo("INCOME"); // ✅ Сравниваем строки
        assertThat(saved.get().getUser().getId()).isEqualTo(testUserId);
    }

    @Test
    @DisplayName("POST /categories — 400, если name = null")
    void createCategoryWithNullName400() throws Exception {
        CreateCategoryRequestDto request = new CreateCategoryRequestDto()
                .name(null)
                .type(CreateCategoryRequestDto.TypeEnum.INCOME);

        String json = objectMapper.writeValueAsString(request);

        performPostAuth(CATEGORIES_PATH, json)
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[0]").value("name: must not be null"));
    }

    @Test
    @DisplayName("POST /categories — 400, если type не из перечня")
    void createCategoryWithInvalidType400() throws Exception {
        String invalidJson = """
            {
                "name": "Зарплата",
                "type": "INVALID_TYPE"
            }
            """;

        performPostAuth(CATEGORIES_PATH, invalidJson)
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
                .andExpect(jsonPath("$.detail").value("Invalid value in request body"));
    }

    @Test
    @DisplayName("POST /categories без авторизации — 401 Unauthorized")
    void createCategoryWithoutAuth401() throws Exception {
        CreateCategoryRequestDto request = new CreateCategoryRequestDto()
                .name("Зарплата")
                .type(CreateCategoryRequestDto.TypeEnum.INCOME);

        String json = objectMapper.writeValueAsString(request);

        performPostNoAuth(CATEGORIES_PATH, json)
                .andExpect(status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.header().doesNotExist(HttpHeaders.LOCATION));
    }
}