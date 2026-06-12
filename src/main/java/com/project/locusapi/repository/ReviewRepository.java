package com.project.locusapi.repository;

import com.project.locusapi.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    List<Review> findAllByRentableAddressIdOrderByCreatedAtDesc(UUID addressId);

    Optional<Review> findByRentableAddressIdAndAuthorId(UUID addressId, UUID authorId);
}
