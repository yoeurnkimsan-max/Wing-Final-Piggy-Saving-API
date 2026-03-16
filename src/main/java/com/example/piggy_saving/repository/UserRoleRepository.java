package com.example.piggy_saving.repository;

import com.example.piggy_saving.models.UserModel;
import com.example.piggy_saving.models.UserRoleModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface UserRoleRepository extends JpaRepository<UserRoleModel, UUID> {
    @Query("SELECT ur FROM UserRoleModel ur JOIN FETCH ur.roleModel WHERE ur.userModel = :user")
    List<UserRoleModel> findByUserModelWithRoles(@Param("user") UserModel user);
    List<UserRoleModel> findByUserModel(UserModel userModel);
}
