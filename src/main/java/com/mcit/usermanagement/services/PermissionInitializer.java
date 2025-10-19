package com.mcit.usermanagement.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.mcit.usermanagement.modal.Permission;
import com.mcit.usermanagement.modal.Role;
import com.mcit.usermanagement.repository.PermissionRepository;
import com.mcit.usermanagement.repository.RoleRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PermissionInitializer {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;

    private static final String[] DEFAULT_ACTIONS = { "ADD", "UPDATE", "DELETE", "VIEW" };

    @Transactional
    public void createDefaultPermissionsForEntity(String entityName) {
        String upperEntityName = entityName.toUpperCase();

        // Get ADMIN role or create it
        Role adminRole;
        Optional<Role> optionalAdmin = roleRepository.findByName("ROLE_ADMIN");
        if (optionalAdmin.isPresent()) {
            adminRole = optionalAdmin.get();
        } else {
            adminRole = new Role();
            adminRole.setName("ROLE_ADMIN");
            adminRole.setCreatedAt(java.time.LocalDateTime.now());
            adminRole = roleRepository.save(adminRole);
        }

        // Generate CRUD permissions
        for (String action : DEFAULT_ACTIONS) {
            String permissionName = action + "_" + upperEntityName;

            // Check if permission exists
            Optional<Permission> optionalPermission = permissionRepository.findByPermissionName(permissionName);

            // Extract actual Permission object
            Permission permissionEntity;
            if (optionalPermission.isPresent()) {
                permissionEntity = optionalPermission.get();
            } else {
                permissionEntity = new Permission();
                permissionEntity.setPermissionName(permissionName);
                permissionEntity.setCreatedAt(java.time.LocalDateTime.now());
                permissionEntity = permissionRepository.save(permissionEntity);
            }

            // Assign permission to ADMIN role if not already assigned
            if (!adminRole.getPermissions().contains(permissionEntity)) {
                adminRole.getPermissions().add(permissionEntity);
            }
        }

        // Save updated ADMIN role
        roleRepository.save(adminRole);
    }
}
