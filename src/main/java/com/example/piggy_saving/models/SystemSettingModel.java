package com.example.piggy_saving.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "system_settings")
public class SystemSettingModel {

    @Id
    @Column(name = "key", nullable = false, length = 100)
    private String key;

    @Column(name = "value")
    private String value;

    @Column(name = "description")
    private String description;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}