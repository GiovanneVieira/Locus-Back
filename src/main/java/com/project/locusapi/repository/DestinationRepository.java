package com.project.locusapi.repository;

import com.project.locusapi.model.Destination;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DestinationRepository extends JpaRepository<Destination, UUID> {

    Optional<Destination> findByCityIgnoreCase(String city);

    Page<Destination> findByCityContainingIgnoreCase(String city, Pageable pageable);

    boolean existsByCityIgnoreCase(String trim);
}
