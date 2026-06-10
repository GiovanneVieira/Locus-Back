package com.project.locusapi.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "review_table")
@Table(
        name = "review_table",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_review_author_address",
                columnNames = {"author_id", "rentable_address_id"}
        )
)
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Quem escreveu a avaliação
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private UserModel author;

    // Imóvel avaliado
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rentable_address_id", nullable = false)
    private RentableAddressModel rentableAddress;

    // Nota de 1 a 5 estrelas
    @Column(nullable = false)
    private Integer rating;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String comment;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
