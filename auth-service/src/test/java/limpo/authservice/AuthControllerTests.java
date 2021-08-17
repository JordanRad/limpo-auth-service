package limpo.authservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    private final String URL = "/api/v1/auth-service";

    @BeforeEach
    void setup() {
        testUser = new User(1, "Jordan", "Radushev", "test@mail.com", "12345678", "ROLE_ADMIN");
        repository.save(testUser);
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
    public void shouldRegisterNewUserAndStatus200() throws Exception {
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
    public void shouldNotRegisterNewUserAndStatus409() throws Exception {
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
    public void shouldCheckCorrectCredentialsAndStatus200() throws Exception {
        User newUser = new User(0, "CorrectUser", "LastName", "correct@mail.com", "12345678", "ROLE_ADMIN");
        String requestBody1 = toJSONString(newUser);

        this.mockMvc.perform(post(URL+"/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody1))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.firstName", is(newUser.getFirstName())))
                .andExpect(jsonPath("$.lastName", is(newUser.getLastName())))
                .andExpect(jsonPath("$.email", is(newUser.getEmail())))
                .andExpect(jsonPath("$.password", not(newUser.getPassword())));

        Credentials credentials = new Credentials(newUser.getEmail(), newUser.getPassword());
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
    public void shouldCheckWrongCredentialsAndStatus404() throws Exception {
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

//    @Test
//    public void shouldValidateCorrectDTOAndStatus200() throws Exception {
//
//        User newUser = new User(0, "CorrectUser", "LastName", "correct1@mail.com", "12345678", "ROLE_ADMIN");
//        String requestBody1 = toJSONString(newUser);
//
//        HttpClient client = HttpClient.newHttpClient();
//        HttpRequest request = HttpRequest.newBuilder()
//                .uri(URI.create("http://localhost:8082"+URL+"/register"))
//                .POST(HttpRequest.BodyPublishers.ofString(requestBody1))
//                .header("Content-Type","application/json;charset=UTF-8")
//                .build();
//
//        HttpResponse<String> response = client.send(request,
//                HttpResponse.BodyHandlers.ofString());
//
//        System.out.println(response.body());
//
//        AuthorizedDTO dto = new AuthorizedDTO("correct1@mail.com","eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJjb3JyZWN0MUBtYWlsLmNvbSIsInJvbGUiOiJST0xFX0FETUlOIiwiaWF0IjoxNjkxNzgzMTN9.QCxnYD5ShQ8s6kMQS3puaWbzOqSfMqFh69UNlhRIO0c","ROLE_ADMIN");
//        String requestBody = toJSONString(dto);
//
//        this.mockMvc.perform(post(URL + "/validateToken")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(requestBody))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
//                .andExpect(content().string("Valid Token"));
//    }
}
