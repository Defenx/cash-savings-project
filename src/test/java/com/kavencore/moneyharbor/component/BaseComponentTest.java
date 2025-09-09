package com.kavencore.moneyharbor.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kavencore.moneyharbor.app.infrastructure.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public abstract class BaseComponentTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> PG =
            new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired(required = false)
    protected AccountRepository accountRepository;
    @Autowired
    protected MockMvc mvc;
    @Autowired
    protected ObjectMapper om;

    @BeforeEach
    void cleanDb() {
        if (accountRepository != null) {
            accountRepository.deleteAll();
        }
    }

        /** POST + проверка 201 Created */
        protected ResultActions performPost (String url, Object body) throws Exception {
            return mvc.perform(post(url)
                            .contentType(APPLICATION_JSON)
                            .accept(APPLICATION_JSON)
                            .content(om.writeValueAsBytes(body)))
                    .andExpect(status().isCreated());
        }

        /** GET + проверка 200 OK */
        protected ResultActions performGet (String url) throws Exception {
            return mvc.perform(get(url)
                            .accept(APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        /** DELETE + проверка 204 или 200 */
        protected ResultActions performDelete (String url) throws Exception {
            return mvc.perform(delete(url))
                    .andExpect(result -> {
                        int s = result.getResponse().getStatus();
                        if (s != 204 && s != 200) {
                            throw new AssertionError("Expected 200 or 204, got " + s);
                        }
                    });
        }
    }

