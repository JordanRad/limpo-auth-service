package limpo.authservice;

import limpo.authservice.dto.User;
import limpo.authservice.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;


@DataJpaTest
@ActiveProfiles("tests")
public class UserRepositoryTests {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository repository;


    private User userOne;

    @BeforeEach
    public void setup(){
        userOne = new User();
        userOne.setEmail("test@test.com");
        userOne.setFirstName("TestFirstName");
        userOne.setLastName("TestLastName");
        userOne.setPassword("hello");
        userOne.setRole("ROLE_ADMIN");
        entityManager.persist(userOne);
    }

    @Test
    public void shouldGetASingleUserByEmail() {
        User user = repository.findByEmail(userOne.getEmail()).orElse(null);

        Assertions.assertEquals(userOne.getFirstName(),user.getFirstName());
        Assertions.assertEquals(userOne.getLastName(),user.getLastName());
    }

    @Test
    public void shouldCreateAnUser() {
        User user = new User();
        user.setEmail("test1@test.com");
        user.setFirstName("Test1FirstName");
        user.setLastName("Test1LastName");
        user.setPassword("hello1");
        user.setRole("ROLE_ADMIN");

        User savedUser = repository.save(user);

        Assertions.assertEquals(savedUser.getFirstName(),user.getFirstName());
        Assertions.assertEquals(savedUser.getLastName(),user.getLastName());
    }
}
