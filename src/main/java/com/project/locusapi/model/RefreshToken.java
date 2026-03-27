package com.project.locusapi.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity(name = "refresh_token")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "refresh_token", nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private Instant expiresAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserModel user;

}
