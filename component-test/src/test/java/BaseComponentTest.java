import com.kavencore.moneyharbor.MoneyHarborApplication;
import com.kavencore.moneyharbor.app.infrastructure.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;

@SpringBootTest(classes = MoneyHarborApplication.class)
@AutoConfigureMockMvc
public abstract class BaseComponentTest {

    @Autowired
    protected MockMvc mvc;
    @Autowired
    protected UserRepository userRepository;
    @Autowired
    protected PasswordEncoder passwordEncoder;

    protected static final String ACCOUNT_TEST_EMAIL = "test.user@example.com";
    protected static final String USER_TEST_EMAIL = "alice@example.com";
    protected static final String TEST_PASSWORD = "Abcdefg1";
    protected UUID testUserId;


    @ServiceConnection
    static final PostgreSQLContainer<?> PG =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withReuse(true);


    protected ResultActions performPostAuth(String path, String json) throws Exception {
        return mvc.perform(MockMvcRequestBuilders.post(path)
                .with(httpBasic(ACCOUNT_TEST_EMAIL, TEST_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .content(json));
    }

    protected ResultActions performPostNoAuth(String path, String json) throws Exception {
        return mvc.perform(MockMvcRequestBuilders.post(path)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .content(json));
    }

    protected ResultActions performDeleteAuthOk(String path) throws Exception {
        return mvc.perform(MockMvcRequestBuilders.delete(path)
                .with(httpBasic(ACCOUNT_TEST_EMAIL, TEST_PASSWORD)));
    }

    protected ResultActions postJson(String path, String json) throws Exception {
        return mvc.perform(MockMvcRequestBuilders.post(path)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .content(json));
    }

    protected ResultActions postExpectProblem(String path, String json) throws Exception {
        return mvc.perform(MockMvcRequestBuilders.post(path)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_PROBLEM_JSON_VALUE)
                .content(json));
    }

    protected ResultActions performGetAuth(String path) throws Exception {
        return mvc.perform(MockMvcRequestBuilders.get(path)
                .with(httpBasic(ACCOUNT_TEST_EMAIL, TEST_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE));
    }
}

