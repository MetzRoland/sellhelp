package org.sellhelp.backend.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sellhelp.backend.entities.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class RoleRepositoryTest {

    @Autowired
    private RoleRepository roleRepository;

    private Role testRole;

    @BeforeEach
    public void init() {
        testRole = Role.builder()
                .roleName("ADMIN")
                .build();
    }

    @Test
    @DisplayName("Verify that a Role can be added to repository and database")
    public void roleCanBeAddedToRepositoryAndDB() {
        Role savedRole = roleRepository.save(testRole);

        assertNotNull(savedRole.getId());
        assertEquals("ADMIN", savedRole.getRoleName());
    }

    @Test
    @DisplayName("Verify that a Role can be updated in repository and database")
    public void roleCanBeUpdatedInRepositoryAndDB() {
        Role savedRole = roleRepository.save(testRole);

        savedRole.setRoleName("USER");
        Role updatedRole = roleRepository.save(savedRole);

        assertNotNull(updatedRole.getId());
        assertEquals("USER", updatedRole.getRoleName());
    }

    @Test
    @DisplayName("Verify that a Role can be deleted from repository and database")
    public void roleCanBeDeletedFromRepositoryAndDB() {
        Role savedRole = roleRepository.save(testRole);
        Integer roleId = savedRole.getId();

        roleRepository.delete(savedRole);

        assertFalse(roleRepository.findById(roleId).isPresent());
    }

    @Test
    @DisplayName("General CRUD functionality test for Role")
    public void roleGeneralCRUDFunctionalityTest() {
        Role savedRole = roleRepository.save(testRole);
        Integer roleId = savedRole.getId();

        assertNotNull(roleId);
        assertEquals("ADMIN", roleRepository.findById(roleId).get().getRoleName());

        savedRole.setRoleName("MODERATOR");
        Role updatedRole = roleRepository.save(savedRole);

        assertEquals("MODERATOR", updatedRole.getRoleName());

        roleRepository.delete(updatedRole);
        assertFalse(roleRepository.findById(updatedRole.getId()).isPresent());
    }
}