
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kavencore.moneyharbor.MoneyHarborApplication;
import com.kavencore.moneyharbor.app.infrastructure.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest(classes = MoneyHarborApplication.class)
@AutoConfigureMockMvc
public abstract class BaseComponentTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> PG =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withReuse(true);

    @Autowired(required = false)
    protected AccountRepository accountRepository;
    @Autowired
    protected MockMvc mvc;
    @Autowired
    protected ObjectMapper om;


    protected ResultActions performPost(String url, String body) throws Exception {
        return mvc.perform(MockMvcRequestBuilders.post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(MockMvcResultMatchers.status().isCreated());
        }

        protected ResultActions performGet (String url) throws Exception {
            return mvc.perform(MockMvcRequestBuilders.get(url)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk());
        }

        protected ResultActions performDelete (String url) throws Exception {
            return mvc.perform(MockMvcRequestBuilders.delete(url))
                    .andExpect(result -> {
                        int s = result.getResponse().getStatus();
                        if (s != 204 && s != 200) {
                            throw new AssertionError("Expected 200 or 204, got " + s);
                        }
                    });
        }
    }

