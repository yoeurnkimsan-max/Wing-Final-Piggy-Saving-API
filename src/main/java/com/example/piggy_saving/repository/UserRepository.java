package com.example.piggy_saving.repository;

import com.example.piggy_saving.models.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserModel, UUID> {
    Optional<UserModel> findByEmail(String email);

    boolean existsUserModelByEmail(String email);
    boolean existsUserModelByPhone(String phone);
    boolean existsUserModelById(UUID userId);


    UserModel findUserModelByEmail(String email);

    Optional<UserModel> findUserModelById(UUID id);

    Optional<UserModel> findUserModelByEmailAndEmailVerified(String email, boolean emailVerified);

}
