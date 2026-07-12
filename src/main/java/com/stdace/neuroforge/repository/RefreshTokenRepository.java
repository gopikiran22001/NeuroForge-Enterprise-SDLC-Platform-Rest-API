package com.stdace.neuroforge.repository;

import com.stdace.neuroforge.models.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByUserId(UUID uuid);

    boolean findByToken(String token);
}
