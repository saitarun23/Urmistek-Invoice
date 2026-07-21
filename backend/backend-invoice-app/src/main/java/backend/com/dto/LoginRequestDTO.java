package backend.com.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequestDTO {
    @NotBlank
    private String companyCode; // "URMISTEK" or "UB_INDUSTRIES" - picked on the first screen

    @NotBlank
    private String username;

    @NotBlank
    private String password;
}
