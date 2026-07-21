package backend.com.dto;

public record LoginResponseDTO(String token, String username, String displayName,
                                String companyCode, String companyName, String role) {
}
