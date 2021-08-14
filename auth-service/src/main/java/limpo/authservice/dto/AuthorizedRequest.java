package limpo.authservice.dto;

import lombok.Data;

@Data
public class AuthorizedRequest {

    private String username;

    private String token;

    private String role;
}
