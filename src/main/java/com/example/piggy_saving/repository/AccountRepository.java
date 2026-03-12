package com.example.piggy_saving.repository;

import com.example.piggy_saving.models.AccountModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AccountRepository extends JpaRepository<AccountModel, UUID> {
}
