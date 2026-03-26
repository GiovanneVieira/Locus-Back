package com.project.locusapi.repository;

import com.project.locusapi.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    public void deleteByToken(String token);

    public Optional<RefreshToken> findByToken(String token);
}
