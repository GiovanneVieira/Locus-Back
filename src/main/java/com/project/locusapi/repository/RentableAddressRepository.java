package com.project.locusapi.repository;

import com.project.locusapi.model.RentableAddressModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RentableAddressRepository extends JpaRepository<RentableAddressModel, UUID> {

    @Query("SELECT r FROM rentable_address_table r JOIN FETCH r.user WHERE r.user.id = :userId")
    List<RentableAddressModel> findAllByUserId(@Param("userId") UUID userId);
}