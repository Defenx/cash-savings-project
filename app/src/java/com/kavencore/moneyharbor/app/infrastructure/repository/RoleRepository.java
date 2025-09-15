package com.kavencore.moneyharbor.app.infrastructure.repository;

import com.kavencore.moneyharbor.app.entity.Role;
import com.kavencore.moneyharbor.app.entity.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {
    @Transactional(readOnly = true)
    Optional<Role> findByRoleName(RoleName roleName);
}
