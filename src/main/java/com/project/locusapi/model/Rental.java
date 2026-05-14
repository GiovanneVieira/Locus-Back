package com.project.locusapi.model;

import com.project.locusapi.constant.RentalStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "rental_table")
public class Rental {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Quem está alugando
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="renter_id", nullable = false)
    private UserModel renter;

    // O que está sendo alugado
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rentable_address_id", nullable = false)
    private RentableAddressModel rentableAddress;

    // Auditoria: Quando o clique do aluguel aconteceu
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    // Período da Locação
    @Column(nullable = false)
    private LocalDateTime checkIn;

    @Column(nullable = false)
    private LocalDateTime checkOut;

    @Column(nullable = false)
    private Double priceAtTheTime;

    // Status para controle de fluxo
    @Enumerated(EnumType.STRING)
    private RentalStatus status; // Ex: PENDING, CONFIRMED, CANCELLED
}
