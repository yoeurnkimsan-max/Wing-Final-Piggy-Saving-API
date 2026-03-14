package com.example.piggy_saving.repository;

import com.example.piggy_saving.models.RoleModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<RoleModel, UUID> {
    Optional<RoleModel> findByName(String name);
}
