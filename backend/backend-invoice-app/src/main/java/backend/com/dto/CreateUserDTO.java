package backend.com.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateUserDTO {
    @NotBlank
    private String username;
    @NotBlank
    private String password;
    @NotBlank
    private String displayName;
    private String role; // "ADMIN" or "STAFF" - defaults to STAFF if blank
}