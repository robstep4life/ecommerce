package robben.ecommerce.controller;

import jakarta.validation.Valid;
import robben.ecommerce.dto.AuthRequests;
import robben.ecommerce.dto.AuthResponses;
import robben.ecommerce.entity.Role;
import robben.ecommerce.entity.User;
import robben.ecommerce.repository.UserRepository;
import robben.ecommerce.security.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authManager;
    private final JwtService jwtService;

    public AuthController(UserRepository users, PasswordEncoder encoder,
                          AuthenticationManager authManager, JwtService jwtService) {
        this.users = users;
        this.encoder = encoder;
        this.authManager = authManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody AuthRequests.SignupRequest req) {
        String email = req.email().trim().toLowerCase();

        if (users.existsByEmail(email)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email already in use"));
        }

        User u = new User();
        u.setEmail(email);
        u.setPasswordHash(encoder.encode(req.password()));
        u.setRole(Role.USER);

        users.save(u);

        String token = jwtService.generateToken(u.getEmail(), u.getRole().name());
        return ResponseEntity.ok(new AuthResponses.AuthResponse(token, "Bearer"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequests.LoginRequest req) {
        String email = req.email().trim().toLowerCase();

        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, req.password())
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Role from DB = source of truth
        User u = users.findByEmail(email).orElseThrow();
        String token = jwtService.generateToken(u.getEmail(), u.getRole().name());

        return ResponseEntity.ok(new AuthResponses.AuthResponse(token, "Bearer"));
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponses.MeResponse> me() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        User u = users.findByEmail(email).orElseThrow();
        return ResponseEntity.ok(new AuthResponses.MeResponse(u.getId(), u.getEmail(), u.getRole().name()));
    }
}
