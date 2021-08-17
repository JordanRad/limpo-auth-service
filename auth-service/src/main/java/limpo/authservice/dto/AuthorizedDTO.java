package limpo.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthorizedDTO {
    private String email;

    private String token;

    private String role;

    private String refreshToken;
}
