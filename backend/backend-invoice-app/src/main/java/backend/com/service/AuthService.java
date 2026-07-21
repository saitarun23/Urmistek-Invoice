package backend.com.service;

import backend.com.dto.CompanyOptionDTO;
import backend.com.dto.LoginRequestDTO;
import backend.com.dto.LoginResponseDTO;
import backend.com.entity.AppUser;
import backend.com.entity.Company;
import backend.com.repository.AppUserRepository;
import backend.com.repository.CompanyRepository;
import backend.com.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(AppUserRepository appUserRepository,
                        CompanyRepository companyRepository,
                        PasswordEncoder passwordEncoder,
                        JwtUtil jwtUtil) {
        this.appUserRepository = appUserRepository;
        this.companyRepository = companyRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    /** Powers the login screen's two tiles: URMISTEK / UB Industries. */
    public List<CompanyOptionDTO> listCompanies() {
        return companyRepository.findAll().stream()
                .map(c -> new CompanyOptionDTO(c.getCode(), c.getName(), c.getLogoPath()))
                .toList();
    }

    public LoginResponseDTO login(LoginRequestDTO request) {
        AppUser user = appUserRepository
                .findByCompany_CodeAndUsername(request.getCompanyCode(), request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));

        if (!user.isEnabled()) {
            throw new IllegalArgumentException("This account has been disabled");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        Company company = user.getCompany();
        String token = jwtUtil.generateToken(user.getUsername(), company.getCode(), company.getId(), user.getRole().name());

        return new LoginResponseDTO(
                token,
                user.getUsername(),
                user.getDisplayName(),
                company.getCode(),
                company.getName(),
                user.getRole().name()
        );
    }
}
