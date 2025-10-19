package com.mcit.usermanagement.services;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.mcit.usermanagement.modal.Role;
import com.mcit.usermanagement.repository.RoleRepository;

@Component
@Order(1) // Ensures it runs before DefaultUserInitializer
public class DefaultRoleInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    public DefaultRoleInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        List<String> defaultRoles = List.of(
                "ROLE_ADMIN",
                "ROLE_USER");

        boolean anyCreated = false;

        for (String roleName : defaultRoles) {
            if (!roleRepository.existsByName(roleName)) {
                Role newRole = new Role();
                newRole.setName(roleName);
                newRole.setCreatedAt(java.time.LocalDateTime.now());
                roleRepository.saveAndFlush(newRole);
                System.out.println("✅ Role created: " + roleName);
                anyCreated = true;
            } else {
                System.out.println("ℹ️ Role already exists: " + roleName);
            }
        }

        if (!anyCreated) {
            System.out.println("✅ All default roles already exist.");
        }
    }
}
