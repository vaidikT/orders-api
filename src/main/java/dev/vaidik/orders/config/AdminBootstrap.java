package dev.vaidik.orders.config;

import dev.vaidik.orders.user.Role;
import dev.vaidik.orders.user.User;
import dev.vaidik.orders.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Creates the first admin from ADMIN_EMAIL / ADMIN_PASSWORD so no one has to insert SQL by hand.
 * Does nothing if either variable is empty or the account already exists.
 */
@Component
public class AdminBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String password;

    public AdminBootstrap(UserRepository users, PasswordEncoder passwordEncoder,
                          @Value("${app.admin.email:}") String email,
                          @Value("${app.admin.password:}") String password) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.email = email.trim().toLowerCase(Locale.ROOT);
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (email.isEmpty() || password.isEmpty() || users.existsByEmail(email)) {
            return;
        }
        users.save(new User(email, passwordEncoder.encode(password), "Administrator", Role.ADMIN));
        log.info("Created admin account {}", email);
    }
}
