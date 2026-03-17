package com.example.piggy_saving.repository;

import com.example.piggy_saving.dto.response.statusEnum.AccountStatus;
import com.example.piggy_saving.models.AccountModel;
import com.example.piggy_saving.models.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<AccountModel, UUID> {
    List<AccountModel> findAllByUserModelId(UUID userModelId);

    boolean existsAccountModelById(UUID id);

    AccountModel findAccountModelsById(UUID id);
    Optional<AccountModel> findById(UUID id);
}
