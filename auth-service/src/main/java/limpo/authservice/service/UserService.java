package limpo.authservice.service;

import limpo.authservice.dto.Credentials;
import limpo.authservice.dto.User;
import limpo.authservice.dto.UserRegistrationDTO;
import limpo.authservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository repository;

    @Autowired
    private PasswordEncoder encoder;

    public User register(UserRegistrationDTO user) {

        try {
            // Encode the password
            String encodedPassword = encoder.encode(user.getPassword());

            User newUser = new User(0, user.getFirstName(), user.getLastName(), user.getEmail(), encodedPassword, user.getRole());
            return repository.save(newUser);
        }catch (Exception e){
            return null;
        }

    }

    public boolean login(Credentials credentials){
        User user = repository.findByEmail(credentials.getEmail()).orElse(null);
        if(user != null){
            // Check if the passwords are matching
            return encoder.matches(credentials.getPassword(),user.getPassword());
        }
        return false;
    }

    public User getByEmail(String email){
        return repository.findByEmail(email).orElse(null);
    }
}
