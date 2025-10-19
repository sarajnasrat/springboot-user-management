package com.mcit.usermanagement.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.metamodel.EntityType;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.mcit.usermanagement.services.PermissionInitializer;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class StartupPermissionSeeder implements CommandLineRunner {

    private final EntityManager entityManager;
    private final PermissionInitializer permissionInitializer;

    @Override
    public void run(String... args) {
        // Get all JPA entity names dynamically
        Set<EntityType<?>> entities = entityManager.getMetamodel().getEntities();

        for (EntityType<?> entityType : entities) {
            String entityName = entityType.getName();
            permissionInitializer.createDefaultPermissionsForEntity(entityName);
            System.out.println("✅ Permissions generated for entity: " + entityName);
        }
    }
}
