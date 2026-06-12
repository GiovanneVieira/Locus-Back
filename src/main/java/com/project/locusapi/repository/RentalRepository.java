package com.project.locusapi.repository;

import com.project.locusapi.model.Rental;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RentalRepository extends JpaRepository<Rental, UUID> {

    // Reservas feitas pelo hóspede
    List<Rental> findAllByRenterIdOrderByCreatedAtDesc(UUID renterId);

    // Reservas recebidas pelo anfitrião (dono do imóvel)
    List<Rental> findAllByRentableAddress_User_IdOrderByCreatedAtDesc(UUID hostId);
}
