package backend.com.security;

/**
 * The authenticated identity for the current request, pulled out of the
 * JWT. Controllers/services use this instead of trusting any companyId
 * the frontend might send in a request body.
 */
public record CompanyPrincipal(String username, String companyCode, Long companyId, String role) {
}
