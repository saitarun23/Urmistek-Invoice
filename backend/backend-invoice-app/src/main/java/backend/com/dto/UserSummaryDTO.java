package backend.com.dto;

public record UserSummaryDTO(
        Long id,
        String username,
        String displayName,
        String role,
        boolean enabled,
        boolean hasSignature
) {
}