package backend.com.controller;

import backend.com.dto.LoginRequestDTO;
import backend.com.dto.LoginResponseDTO;
import backend.com.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO request) {
        try {
            LoginResponseDTO response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body(Map.of("message", e.getMessage()));
        }
    }
}
