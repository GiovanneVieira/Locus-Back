package com.project.locusapi.repository;

import com.project.locusapi.model.PersonalAddressModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PersonalAddressRepository extends JpaRepository<PersonalAddressModel, UUID> {
    Optional<PersonalAddressModel> findByUserId(UUID userId);
}
