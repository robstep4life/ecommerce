package robben.ecommerce.config;

import robben.ecommerce.entity.Role;
import robben.ecommerce.entity.User;
import robben.ecommerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository users;
    private final PasswordEncoder encoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    public AdminSeeder(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        String email = adminEmail.trim().toLowerCase();

        User admin = users.findByEmail(email).orElseGet(() -> {
            User u = new User();
            u.setEmail(email);
            u.setPasswordHash(encoder.encode(adminPassword));
            u.setRole(Role.ADMIN);
            return u;
        });

        // If user exists but isn't admin, upgrade
        if (admin.getRole() != Role.ADMIN) {
            admin.setRole(Role.ADMIN);
        }

        // If user exists but password empty or you want to enforce configured password, uncomment:
        // admin.setPasswordHash(encoder.encode(adminPassword));

        users.save(admin);

        System.out.println("✅ Admin ready: " + email);
    }
}
