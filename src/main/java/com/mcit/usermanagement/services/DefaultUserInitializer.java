package com.mcit.usermanagement.services;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.mcit.usermanagement.modal.Role;
import com.mcit.usermanagement.modal.User;
import com.mcit.usermanagement.repository.RoleRepository;
import com.mcit.usermanagement.repository.UserRepository;

import jakarta.transaction.Transactional;

@Component
@Order(2) // Runs after role initializer
public class DefaultUserInitializer implements CommandLineRunner {

    private static final String DEFAULT_ADMIN_EMAIL = "admin@gmail.com";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    public DefaultUserInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder,
            RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Optional<Role> OptionalAdminRole = roleRepository.findByName("ROLE_ADMIN");
        Role adminRole = OptionalAdminRole.orElseThrow(() -> new RuntimeException("ROLE_ADMIN not found"));

        if (userRepository.findByEmailIgnoreCase(DEFAULT_ADMIN_EMAIL).isEmpty()) {
            User user = new User();
            user.setFirstName("Admin");
            user.setLastName("Admin");
            user.setEmail(DEFAULT_ADMIN_EMAIL);
            user.setPassword(passwordEncoder.encode("12345"));
            user.setRoles(Collections.singleton(adminRole));
            user.setActive(true);
            user.setCreatedAt(LocalDateTime.now());

            userRepository.saveAndFlush(user);
            System.out.println("✅ Default user created: " + user.getEmail());
        } else {
            System.out.println("✅ User '" + DEFAULT_ADMIN_EMAIL + "' already exists. No default user created.");
        }
    }
}
