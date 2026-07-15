package com.agrofarm.api.config;

import com.agrofarm.api.entity.UserEntity;
import com.agrofarm.api.repository.UserRepository;
import com.agrofarm.api.util.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class AdminAccountInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.security.initial-admin.username}")
    private String adminUsername;

    @Value("${app.security.initial-admin.password}")
    private String adminPassword;

    @Value("${app.security.initial-admin.email}")
    private String adminEmail;

    public AdminAccountInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.findByUsername(adminUsername).isEmpty()) {

            UserEntity admin = new UserEntity();
            admin.setUsername(adminUsername);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setEmail(adminEmail);
            admin.setRole(Role.ADMIN);
            admin.setEnabled(true);
            admin.setCreatedAt(LocalDateTime.now());

            userRepository.save(admin);

            System.out.println("BOOTSTRAP: Initial ADMIN account created!");
        }
    }
}
