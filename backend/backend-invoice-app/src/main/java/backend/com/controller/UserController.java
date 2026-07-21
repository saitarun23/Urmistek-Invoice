package backend.com.controller;

import backend.com.dto.CreateUserDTO;
import backend.com.dto.UserSummaryDTO;
import backend.com.security.CompanyPrincipal;
import backend.com.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<?> listUsers(@AuthenticationPrincipal CompanyPrincipal principal) {
        if (!isAdmin(principal)) return forbidden();
        return ResponseEntity.ok(userService.listUsers(principal.companyId()));
    }

    @PostMapping
    public ResponseEntity<?> createUser(@Valid @RequestBody CreateUserDTO request,
                                         @AuthenticationPrincipal CompanyPrincipal principal) {
        if (!isAdmin(principal)) return forbidden();
        try {
            return ResponseEntity.ok(userService.createUser(principal.companyId(), request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping(value = "/me/signature", consumes = "multipart/form-data")
    public ResponseEntity<?> uploadMySignature(@RequestParam("file") MultipartFile file,
                                                @AuthenticationPrincipal CompanyPrincipal principal) {
        userService.uploadSignature(principal, file);
        return ResponseEntity.ok(Map.of("status", "saved"));
    }

    @GetMapping("/{id}/signature")
    public ResponseEntity<?> getSignature(@PathVariable Long id, @AuthenticationPrincipal CompanyPrincipal principal) {
        byte[] bytes = userService.readSignatureBytes(principal.companyId(), id);
        if (bytes == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(bytes);
    }

    private boolean isAdmin(CompanyPrincipal principal) {
        return principal != null && "ADMIN".equals(principal.role());
    }

    private ResponseEntity<Map<String, String>> forbidden() {
        return ResponseEntity.status(403).body(Map.of("message", "Admin access required"));
    }
}