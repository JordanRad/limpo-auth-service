package limpo.authservice.controller;

import limpo.authservice.dto.AuthorizedDTO;
import limpo.authservice.dto.Credentials;
import limpo.authservice.dto.User;
import limpo.authservice.service.JwtService;
import limpo.authservice.service.UserService;
import limpo.authservice.util.Base64Util;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@RequestMapping(AuthController.BASE_URL)
public class AuthController {

    public static final String BASE_URL = "/api/v1/auth-service";

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
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


            String stringToEncode = authorizedUser.getEmail() + ",has role," + authorizedUser.getRole();
            String refreshToken = Base64.encodeBase64String(stringToEncode.getBytes());

            AuthorizedDTO request = new AuthorizedDTO(credentials.getEmail(), token, authorizedUser.getRole(), refreshToken);

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

    @PostMapping("/refreshToken")
    public ResponseEntity<?> refreshToken(@RequestParam String refreshToken) {

        // Get decoded bytes
        byte[] decodedBytes = Base64.decodeBase64(refreshToken.getBytes());

        // Convert to String
        String[] decodedToken = Base64Util.decodeBytes(decodedBytes).split(",");

        String email = decodedToken[0];

        User user = userService.getByEmail(email);

        if(user!=null && user.getRole().equals(decodedToken[2])){
            // Generate new JWT
            String newJwt = jwtService.generateToken(email);
            AuthorizedDTO dto = new AuthorizedDTO(email,newJwt,user.getRole(),refreshToken);

            return new ResponseEntity<>(dto, HttpStatus.OK);
        }


        return new ResponseEntity<>("", HttpStatus.UNAUTHORIZED);

    }
}
