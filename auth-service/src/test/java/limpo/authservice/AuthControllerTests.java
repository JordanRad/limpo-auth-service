package limpo.authservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import limpo.authservice.dto.AuthorizedDTO;
import limpo.authservice.dto.Credentials;
import limpo.authservice.dto.User;
import limpo.authservice.repository.UserRepository;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("tests")
public class AuthControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository repository;

    private User testUser;

    @Autowired
    private ObjectMapper mapper;

    private final String URL = "/api/v1/";

    @BeforeEach
    void setup() {
        testUser = new User(1, "Test", "Test", "test@mail.com", "12345678", "ROLE_ADMIN");
        repository.save(testUser);
        repository.save(new User(2, "Jordan", "Radushev", "dani@mail.bg", "$2a$10$fTeRDx0tePTt6tjc0dUjvOU6NNj.aLzhSXgX72eVC6bQ1gSF7W81i", "ROLE_ADMIN"));
    }

    @AfterEach
    void delete() {
        repository.deleteAll();
    }

    private String toJSONString(Object obj) throws JsonProcessingException {
        return mapper.writeValueAsString(obj);
    }

    @Test
    @Order(1)
    public void shouldRegisterNewUserAndReturnStatus200() throws Exception {
        User newUser = new User(0, "NewUser", "LastName", "newuser@mail.com", "12345678", "ROLE_ADMIN");
        String requestBody = toJSONString(newUser);

        this.mockMvc.perform(post(URL+"/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.firstName", is(newUser.getFirstName())))
                .andExpect(jsonPath("$.lastName", is(newUser.getLastName())))
                .andExpect(jsonPath("$.email", is(newUser.getEmail())))
                .andExpect(jsonPath("$.password", not(newUser.getPassword())));
    }

    @Test
    public void shouldNotRegisterNewUserAndReturnStatus409() throws Exception {
        User newUser = new User(0, "NewUser", "LastName", "test@mail.com", "12345678", "ROLE_ADMIN");
        String requestBody = toJSONString(newUser);

        this.mockMvc.perform(post(URL+"/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(content().string("Already exists"));
    }

    @Test
    public void shouldCheckCorrectCredentialsAndReturnStatus200() throws Exception {
        Credentials credentials = new Credentials("dani@mail.bg", "12345678");
        String requestBody2 = toJSONString(credentials);

        this.mockMvc.perform(post(URL+"/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody2))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.email", is(credentials.getEmail())))
                .andExpect(jsonPath("$.role", is(testUser.getRole())))
                .andExpect(jsonPath("$.token", startsWith("ey")));
    }

    @Test
    public void shouldCheckWrongCredentialsAndReturnStatus404() throws Exception {
        Credentials credentials = new Credentials("test1@mail.com", "12345678910");
        String requestBody = toJSONString(credentials);

        this.mockMvc.perform(post(URL + "/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(content().string("Wrong Credentials"));

    }

    @Test
    public void shouldValidateCorrectDTOAndReturnStatus200() throws Exception {

        AuthorizedDTO dto = new AuthorizedDTO("dani@mail.bg","eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJkYW5pQG1haWwuYmciLCJyb2xlIjoiUk9MRV9BRE1JTiIsImV4cCI6MTk5OTkyODIxOCwiaWF0IjoxNjI5ODkyMjE4fQ.qBo4tVzK9Lh7e7g5O_z4Qs1xAncsuXXk1z1Ko2vryHo","ROLE_ADMIN","");
        String requestBody = toJSONString(dto);

        this.mockMvc.perform(post(URL + "/validateToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(content().string("Valid Token"));
    }

    @Test
    public void shouldValidateIncorrectDTOAndReturnStatus409() throws Exception {

        AuthorizedDTO dto = new AuthorizedDTO("dani@mail.bg","eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJkYW5pQG1haWwuYymciLCJyb2xlIjoiUk9MRV9BRE1JTiIsImV4cCI6MTk5OTkyODIxOCwiaWF0IjoxNjI5ODkyMjE4fQ.qBo4tVzK9Lh7e7g5O_yz4Qs1xAncsuXXk1z1Ko2vryHo","ROLE_ADMIN","");
        String requestBody = toJSONString(dto);

        this.mockMvc.perform(post(URL + "/validateToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(content().string("Invalid Token"));
    }

    @Test
    public void shouldRefreshJWTAndReturnStatus200() throws Exception {
        this.mockMvc.perform(post(URL + "/refreshToken?refreshToken=ZGFuaUBtYWlsLmJnLGhhcyByb2xlLFJPTEVfQURNSU4=")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.email", is("dani@mail.bg")))
                .andExpect(jsonPath("$.role", is("ROLE_ADMIN")))
                .andExpect(jsonPath("$.token", startsWith("ey")));
    }

    @Test
    public void shouldNotRefreshJWTAndReturnStatus401() throws Exception {
        this.mockMvc.perform(post(URL + "/refreshToken?refreshToken=ZGFuaUBtYWlsLmJnLGhhcyByb2xlLFJPTEVfQURNSU43=")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }
}
