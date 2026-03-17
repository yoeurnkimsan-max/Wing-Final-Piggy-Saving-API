package com.example.piggy_saving.repository;

import com.example.piggy_saving.models.AccountModel;
import com.example.piggy_saving.models.enums.AccountType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<AccountModel, UUID> {
    List<AccountModel> findAllByUserModelId(UUID userModelId);

    boolean existsAccountModelById(UUID id);

    AccountModel findAccountModelsById(UUID id);

    Optional<AccountModel> findById(UUID id);

    List<AccountModel> findAllByAccountType(AccountType accountType);

    List<AccountModel> getAccountModelByUserModelIdAndAccountType(UUID userModelId, AccountType accountType);

    @Query("SELECT a FROM AccountModel a WHERE a.userModel.id = :userId OR a.piggyGoalModel.userModel.id = :userId")
    List<AccountModel> findAllAccountsByUserIdIncludingPiggy(@Param("userId") UUID userId);

    @Query("SELECT a FROM AccountModel a WHERE (a.userModel.id = :userId OR a.piggyGoalModel.userModel.id = :userId) AND a.accountType = :accountType")
    List<AccountModel> findAllAccountsByUserIdAndTypeIncludingPiggy(@Param("userId") UUID userId, @Param("accountType") AccountType accountType);

    @Query("SELECT a FROM AccountModel a WHERE a.userModel.id = :userId AND a.accountType IN :types")
    List<AccountModel> findAllByUserIdAndTypes(
            @Param("userId") UUID userId,
            @Param("types") List<AccountType> types
    );

    @Query("""
                SELECT a
                FROM AccountModel a
                LEFT JOIN a.piggyGoalModel p
                WHERE a.userModel.id = :userId
                   OR p.userModel.id = :userId
            """)
    List<AccountModel> findAllAccountsByUserId(@Param("userId") UUID userId);
}
