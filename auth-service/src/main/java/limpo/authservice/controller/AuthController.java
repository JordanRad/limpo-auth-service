package limpo.authservice.controller;

import limpo.authservice.dto.AuthorizedDTO;
import limpo.authservice.dto.Credentials;
import limpo.authservice.dto.User;
import limpo.authservice.dto.UserRegistrationDTO;
import limpo.authservice.service.JwtService;
import limpo.authservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(AuthController.BASE_URL)
public class AuthController {

    public static final String BASE_URL = "/api/v1/auth-service";

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserRegistrationDTO user) {
        User registeredUser = userService.register(user);

        if (registeredUser == null) {
            return new ResponseEntity<>("Already exists", HttpStatus.CONFLICT);
        }

        return new ResponseEntity<>(registeredUser, HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Credentials credentials) {
        boolean areCredentialsValid = userService.login(credentials);

        if (areCredentialsValid) {
            String token = jwtService.generateToken(credentials.getEmail());
            User authorizedUser = userService.getByEmail(credentials.getEmail());

            AuthorizedDTO request = new AuthorizedDTO(credentials.getEmail(), token, authorizedUser.getRole());

            return new ResponseEntity<>(request, HttpStatus.OK);
        }

        return new ResponseEntity<>("Wrong Credentials", HttpStatus.NOT_FOUND);
    }

    @PostMapping("/validateToken")
    public ResponseEntity<?> validateToken(@RequestBody AuthorizedDTO request) {

        try {
            boolean isTokenValid = jwtService.validateToken(request.getToken(), request.getEmail());
            return isTokenValid ? new ResponseEntity<>("Valid Token", HttpStatus.OK)
                    : new ResponseEntity<>("Invalid Token", HttpStatus.CONFLICT);
        } catch (Exception e) {
            return new ResponseEntity<>("Invalid Token", HttpStatus.CONFLICT);
        }

    }
}
