package com.example.piggy_saving.repository;

import com.example.piggy_saving.models.AccountModel;
import com.example.piggy_saving.models.enums.AccountType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<AccountModel, UUID> {

    @Query("SELECT a FROM AccountModel a WHERE (a.userModel.id = :userId OR a.piggyGoalModel.userModel.id = :userId) AND a.accountType = :accountType")
    List<AccountModel> findAllAccountsByUserIdAndTypeIncludingPiggy(@Param("userId") UUID userId, @Param("accountType") AccountType accountType);

    List<AccountModel> findAllByUserModelIdAndAccountType(UUID userModelId, AccountType accountType);

    @Query("""
                SELECT a
                FROM AccountModel a
                LEFT JOIN a.piggyGoalModel p
                WHERE a.userModel.id = :userId
                   OR p.userModel.id = :userId
            """)
    List<AccountModel> findAllAccountsByUserId(@Param("userId") UUID userId);

    Optional<AccountModel> findAccountModelsByUserModelIdAndAccountType(UUID userModelId, AccountType accountType);

    Optional<AccountModel> findByIdAndUserModelId(UUID id, UUID userModelId);

    Optional<AccountModel> findByPiggyGoalModelIdAndUserModelId(UUID piggyGoalModelId, UUID userModelId);


    Optional<AccountModel> findByPiggyGoalModelId(UUID piggyGoalModelId);

    Optional<AccountModel> findByUserModelIdAndAccountType(UUID userModelId, AccountType accountType);


    Optional<AccountModel> findByAccountNumberAndIsPublicTrue(String accountNumber, boolean isPublic);

    @Query("SELECT a FROM AccountModel a " +
            "WHERE a.accountNumber = :accountNumber " +
            "AND a.isPublic = true " +
            "AND a.accountType = com.example.piggy_saving.models.enums.AccountType.MAIN")
    Optional<AccountModel> findPublicMainAccountByAccountNumber(@Param("accountNumber") String accountNumber);


    @Query("SELECT a FROM AccountModel a " +
            "WHERE a.accountNumber = :accountNumber " +
            "AND a.isPublic = true " +
            "AND a.userModel.id = :userModelId "+
            "AND a.accountType = com.example.piggy_saving.models.enums.AccountType.PIGGY")
    Optional<AccountModel> findByAccountNumberAndUserModelIdAndIsPublic(@Param("accountNumber") String accountNumber,@Param("userModelId") UUID userModelId);

    Optional<AccountModel> findByAccountNumberAndUserModelId(String accountNumber, UUID userModelId);

    /**
     * find account by account number
     * @param accountNumber
     * @return
     */
    Optional<AccountModel> findByAccountNumber(String accountNumber);


    @Query("SELECT a FROM AccountModel a " +
            "LEFT JOIN FETCH a.piggyGoalModel g " +
            "LEFT JOIN FETCH a.userModel u " +
            "WHERE u.id = :userId AND a.accountType = 'PIGGY'")
    List<AccountModel> findPiggyAccountsByUserId(@Param("userId") UUID userId);

    @Modifying
    @Transactional
    @Query("UPDATE AccountModel a SET a.isPublic = :isPublic " +
            "WHERE a.accountNumber = :accountNumber AND a.userModel.id = :userId")
    int updateIsPublicByAccountNumberAndUserId(String accountNumber, UUID userId, boolean isPublic);
}
