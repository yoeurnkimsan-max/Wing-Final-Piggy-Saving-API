package com.example.piggy_saving.repository;

import com.example.piggy_saving.models.LedgerEntryModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntryModel, UUID> {
}
