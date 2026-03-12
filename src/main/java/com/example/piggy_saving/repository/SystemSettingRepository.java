package com.example.piggy_saving.repository;

import com.example.piggy_saving.models.SystemSettingModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SystemSettingRepository extends JpaRepository<SystemSettingModel, UUID> {
}
