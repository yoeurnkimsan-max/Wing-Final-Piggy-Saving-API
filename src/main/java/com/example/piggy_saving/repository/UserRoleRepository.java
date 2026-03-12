package com.example.piggy_saving.repository;

import com.example.piggy_saving.models.UserRoleModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRoleRepository extends JpaRepository<UserRoleModel, UUID> {
}
