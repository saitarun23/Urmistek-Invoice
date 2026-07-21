package backend.com.service;

import backend.com.dto.CreateUserDTO;
import backend.com.dto.UserSummaryDTO;
import backend.com.entity.AppUser;
import backend.com.entity.Company;
import backend.com.entity.UserRole;
import backend.com.repository.AppUserRepository;
import backend.com.repository.CompanyRepository;
import backend.com.security.CompanyPrincipal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final AppUserRepository appUserRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${signature.storage-path}")
    private String signatureStoragePath;

    public UserService(AppUserRepository appUserRepository, CompanyRepository companyRepository, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.companyRepository = companyRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UserSummaryDTO> listUsers(Long companyId) {
        return appUserRepository.findByCompany_IdOrderByUsernameAsc(companyId).stream()
                .map(this::toSummary)
                .toList();
    }

    @Transactional
    public UserSummaryDTO createUser(Long companyId, CreateUserDTO request) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalStateException("Unknown company"));

        appUserRepository.findByCompany_IdAndUsername(companyId, request.getUsername())
                .ifPresent(u -> { throw new IllegalArgumentException("That username is already taken in this company"); });

        AppUser user = new AppUser();
        user.setCompany(company);
        user.setUsername(request.getUsername().trim());
        user.setDisplayName(request.getDisplayName().trim());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole("ADMIN".equalsIgnoreCase(request.getRole()) ? UserRole.ADMIN : UserRole.STAFF);
        user.setEnabled(true);

        appUserRepository.save(user);
        return toSummary(user);
    }

    @Transactional
    public void uploadSignature(CompanyPrincipal principal, MultipartFile file) {
        AppUser user = appUserRepository.findByCompany_IdAndUsername(principal.companyId(), principal.username())
                .orElseThrow(() -> new IllegalStateException("User not found"));
        try {
            Files.createDirectories(Path.of(signatureStoragePath));
            String extension = file.getOriginalFilename() != null && file.getOriginalFilename().contains(".")
                    ? file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf('.'))
                    : ".png";
            String fileName = "sig-" + principal.companyId() + "-" + user.getId() + "-" + UUID.randomUUID() + extension;
            Path target = Path.of(signatureStoragePath, fileName);
            Files.copy(file.getInputStream(), target);
            user.setSignatureImagePath(target.toString());
            appUserRepository.save(user);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save signature image: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public byte[] readSignatureBytes(Long callerCompanyId, Long targetUserId) {
        AppUser target = appUserRepository.findById(targetUserId).orElse(null);
        if (target == null || !target.getCompany().getId().equals(callerCompanyId)) return null;
        if (target.getSignatureImagePath() == null) return null;
        try {
            return Files.readAllBytes(Path.of(target.getSignatureImagePath()));
        } catch (IOException e) {
            return null;
        }
    }

    private UserSummaryDTO toSummary(AppUser user) {
        return new UserSummaryDTO(
                user.getId(), user.getUsername(), user.getDisplayName(),
                user.getRole().name(), user.isEnabled(),
                user.getSignatureImagePath() != null
        );
    }
}