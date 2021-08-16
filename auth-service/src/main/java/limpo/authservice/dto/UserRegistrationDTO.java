package limpo.authservice.dto;

import lombok.Data;

@Data
public class UserRegistrationDTO {
    private String firstName;

    private String lastName;

    private String email;

    private String password;

    private String role;
}
