package limpo.authservice;

import limpo.authservice.dto.User;
import limpo.authservice.repository.UserRepository;
import limpo.authservice.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("tests")
public class UserServiceTests {
    @Mock
    private UserRepository repository;


    @InjectMocks
    private UserService service;
    private User testUserOne;
    private User testUserTwo;
    private List<User> users;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        users = new ArrayList<>();
        testUserOne = new User(1, "Jordan", "Radushev", "dani@mail.bg", "12345", "ROLE_ADMIN");
        testUserTwo = new User(2, "Moni", "Manolov", "moni@mail.bg", "12345", "ROLE_ADMIN");
        repository.save(testUserTwo);
    }

    @AfterEach
    public void tearDown() {
        testUserOne = null;
        testUserTwo = null;
        users = null;
    }

    @Test
    void shouldGetUserByEmail() {
        when(repository.findByEmail(testUserOne.getEmail())).thenReturn(Optional.of(testUserOne));
        User registeredUser = service.getByEmail("dani@mail.bg");
        Assertions.assertEquals(testUserOne, registeredUser);
    }
}
