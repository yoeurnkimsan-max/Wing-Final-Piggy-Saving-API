package com.example.piggy_saving.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(UserRoleId.class)
@Table(name = "user_roles",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_roles", columnNames = {"user_id", "role_id"}),
        indexes = {
                @Index(name = "idx_user_roles_user", columnList = "user_id"),
                @Index(name = "idx_user_roles_role", columnList = "role_id")
        })
public class UserRoleModel {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Id
    @Column(name = "role_id")
    private UUID roleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private UserModel userModel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", insertable = false, updatable = false)
    private RoleModel roleModel;

    @CreationTimestamp
    @Column(name = "assigned_at", updatable = false)
    private LocalDateTime assignedAt;
}

// ID class – fields must match the entity's @Id fields (UUIDs, not entity references)
@Data
@NoArgsConstructor
@AllArgsConstructor
class UserRoleId implements Serializable {
    private UUID userId;
    private UUID roleId;
}