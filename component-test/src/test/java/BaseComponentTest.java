import com.kavencore.moneyharbor.MoneyHarborApplication;
import com.kavencore.moneyharbor.app.api.model.UserSignUpRequestDto;
import com.kavencore.moneyharbor.app.api.v1.dto.SignUpResult;
import com.kavencore.moneyharbor.app.infrastructure.exception.EmailTakenException;
import com.kavencore.moneyharbor.app.infrastructure.repository.AccountRepository;
import com.kavencore.moneyharbor.app.infrastructure.repository.RoleRepository;
import com.kavencore.moneyharbor.app.infrastructure.repository.UserRepository;
import com.kavencore.moneyharbor.app.infrastructure.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
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
    protected UserService userService;
    @Autowired
    protected UserRepository userRepository;
    @Autowired
    protected AccountRepository accountRepository;
    @Autowired
    protected PasswordEncoder passwordEncoder;
    @Autowired
    protected RoleRepository roleRepository;

    protected static final String TEST_EMAIL = "test.user@example.com";
    protected static final String TEST_PASSWORD = "Passw0rd1";
    protected UUID testUserId;


    @BeforeEach
    void ensureTestUser() {
        try {
            UserSignUpRequestDto req = new UserSignUpRequestDto()
                    .email(TEST_EMAIL)
                    .password(TEST_PASSWORD);
            SignUpResult result = userService.signUp(req);
            testUserId = result.id();
        } catch (EmailTakenException ignore) {
            testUserId = userRepository.findByEmail(TEST_EMAIL).orElseThrow().getId();
        }
    }

    @ServiceConnection
    static final PostgreSQLContainer<?> PG =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withReuse(true);


    protected ResultActions performPostAuth(String path, String json) throws Exception {
        return mvc.perform(MockMvcRequestBuilders.post(path)
                .with(httpBasic(TEST_EMAIL, TEST_PASSWORD))
                .contentType("application/json")
                .accept("application/json")
                .content(json));
    }

    protected ResultActions performPostNoAuth(String path, String json) throws Exception {
        return mvc.perform(MockMvcRequestBuilders.post(path)
                .contentType("application/json")
                .accept("application/json")
                .content(json));
    }

    protected ResultActions performDeleteAuthOk(String path) throws Exception {
        return mvc.perform(MockMvcRequestBuilders.delete(path)
                .with(httpBasic(TEST_EMAIL, TEST_PASSWORD)));
    }

    protected ResultActions postJson(String path, String json) throws Exception {
        return mvc.perform(MockMvcRequestBuilders.post(path)
                .contentType("application/json")
                .accept("application/json")
                .content(json));
    }

    protected ResultActions postExpectProblem(String path, String json) throws Exception {
        return mvc.perform(MockMvcRequestBuilders.post(path)
                .contentType("application/json")
                .accept("application/problem+json")
                .content(json));
    }
}

